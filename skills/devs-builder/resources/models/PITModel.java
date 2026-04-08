package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

import java.io.Serializable;

/**
 * PITModel
 * A DEVS Atomic model that emulates a minimal Intel 8253/8254 PIT channel 0 behavior,
 * inspired by the provided pit.cpp reference. It supports operating modes 0, 2 and 4,
 * and access modes 1 (low), 2 (high), and 3 (low then high).
 *
 * Notes:
 * - This model avoids external libraries beyond the provided DEVS framework.
 * - It extends PhaseAtomic (provided in this project) to leverage phase/sigma helpers.
 * - For simplicity and portability within the provided framework, MessageBag I/O is
 *   not populated; instead, console prints indicate IRQ activity during getOutput().
 * - Public write/read helpers are provided to drive the model from main() without
 *   requiring a specific MessageBag payload structure.
 */
public class PITModel extends PhaseAtomic {

    // Ports (declared for completeness; not used to pack/unpack MessageBag here)
    private Port<Boolean> outIRQ;
    private Port<Integer> outRead0x40;
    private Port<Integer> inWrite0x40;
    private Port<Integer> inCmd0x43;
    private Port<Integer> inRead0x40;

    // Constants
    // PIT clock: 1.1931816666667 MHz
    private static final double FREQ_HZ = 1.1931816666667E6;
    private static final int MAX16 = 0xFFFF; // 16-bit counter
    private static final int CHANNEL0 = 0;   // Only channel 0 supported here

    // State (mirrors pit.cpp where applicable)
    private int operatingMode;      // Modes: 0, 2, 4 supported
    private int accessMode;         // 1=low, 2=high, 3=low then high (0 unsupported)
    private int reload;             // Reload register
    private int counter;            // Current counter
    private int latch;              // Latched counter value
    private int latchReadCount;     // Latch read remaining bytes (0,1,2)
    private int counterReadCount;   // Counter read rotating state for mode 3 access (2->1->0->2)
    private int reloaded;           // Tracks partial writes for access mode 3: 0->1 (low written)->3 (high written)
    private boolean nullCountFlag;  // True until a new count is loaded
    private boolean outGate;        // Output gate (represented as current IRQ level)
    private boolean statusLatched;  // Status latch flag
    private int statusLatch;        // Latched status byte (not fully modeled here)
    private boolean irq;            // Current IRQ line state (for visibility)

    // Internal helpers
    private double periodSeconds;   // Current period for the running counter (derived from reload)

    public PITModel() {
        this("PITModel");
    }

    public PITModel(String name) {
        super(name);
        // Define ports in constructor (safe prior to initialize())
        outIRQ = addOutputPort("out_irq", Boolean.class);
        outRead0x40 = addOutputPort("out_read_0x40", Integer.class);
        inWrite0x40 = addInputPort("in_write_0x40", Integer.class);
        inCmd0x43 = addInputPort("in_cmd_0x43", Integer.class);
        inRead0x40 = addInputPort("in_read_0x40", Integer.class);

        // Initialize default state (mirrors pit.cpp constructor behavior)
        operatingMode = 0;
        accessMode = 0;
        reload = counter = latch = 0;
        latchReadCount = 0;
        counterReadCount = 2;
        reloaded = 0;
        nullCountFlag = true;
        outGate = false;
        statusLatched = false;
        statusLatch = 0;
        irq = false; // set_irq(0, out_gate=false)
        periodSeconds = Double.POSITIVE_INFINITY;

        passivate(); // Start in passive phase with sigma = +inf
    }

    // PhaseAtomic/AtomicModelImpl overrides

    @Override
    public void initialize() {
        // Reset to initial conditions for a new simulation
        operatingMode = 0;
        accessMode = 0;
        reload = counter = latch = 0;
        latchReadCount = 0;
        counterReadCount = 2;
        reloaded = 0;
        nullCountFlag = true;
        outGate = false;
        statusLatched = false;
        statusLatch = 0;
        irq = false;
        periodSeconds = Double.POSITIVE_INFINITY;
        passivate();
        super.initialize();
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public void internalTransition() {
        // Emulate timer expiration behavior (see pit.cpp::timer_expires)
        // Counter reaches zero -> execute mode-specific actions
        counter = 0;
        switch (operatingMode) {
            case 0: // Interrupt on terminal count (one-shot)
                // set_irq(0, true)
                setIRQ(true);
                // One-shot: remains asserted; stop the timer
                passivate();
                break;

            case 2: // Rate generator (periodic)
                // Pulse: set_irq(false) then set_irq(true), then reload next period
                setIRQ(false);
                setIRQ(true);
                // Restart the timer for the next period from reload
                startFromReload();
                break;

            case 4: // Software triggered strobe (one-shot pulse)
                // Pulse: set_irq(false) then set_irq(true), then stop
                setIRQ(false);
                setIRQ(true);
                passivate();
                break;

            default:
                // Unsupported mode: remain passive
                System.err.println(getName() + ": Operating mode 0x" + Integer.toHexString(operatingMode) + " not supported; passivating.");
                passivate();
                break;
        }
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        // For simplicity, this example does not extract inputs from MessageBag.
        // Drive the model using the public helper methods writeCommand()/writeData() from main().
        // If timeElapsed > 0 and we wanted to decrement the counter, we could approximate ticks here.
        // However, in this simplified example the counter is advanced via DEVS scheduling only.
        if (timeElapsed > 0 && phaseIs("running")) {
            // No-op approximation: DEVS engine advances directly to internal event via time advance.
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        // Default policy: internal, then external (from AtomicModelImpl)
        // Here we explicitly follow the same policy using base implementation.
        super.confluentTransition(input);
    }

    @Override
    public MessageBag getOutput() {
        // Called immediately prior to an internal event
        // Emit a simple console trace to observe IRQ pulses.
        if (phaseIs("running")) {
            switch (operatingMode) {
                case 0:
                    System.out.println(getName() + " OUTPUT (mode 0): IRQ -> true");
                    break;
                case 2:
                    System.out.println(getName() + " OUTPUT (mode 2): IRQ pulse");
                    break;
                case 4:
                    System.out.println(getName() + " OUTPUT (mode 4): IRQ pulse");
                    break;
                default:
                    System.out.println(getName() + " OUTPUT (mode ?): unsupported");
            }
        }
        // No actual messages placed in MessageBag for portability; return EMPTY.
        return MessageBag.EMPTY;
    }

    // Public API to drive the model (simulates PIO writes/reads to ports 0x40 and 0x43)

    /**
     * Simulate write to command register (port 0x43) for channel 0 only.
     * Bits: [7:6]=channel, [5:4]=access, [3:1]=mode, [0]=BCD (must be 0)
     */
    public void writeCommand(int cmd) {
        cmd &= 0xFF;
        // Read-back and latch commands are not modeled here; handle "set mode" for channel 0 only
        int channel = (cmd >> 6) & 0x03;
        if (channel != CHANNEL0) {
            // Only channel 0 supported in this model
            return;
        }

        int acc = (cmd >> 4) & 0x03;
        if (acc == 0) {
            // Access mode 0 is unsupported for writing new counts
            System.err.println(getName() + ": writeCommand with access mode 0 is unsupported.");
            return;
        }
        accessMode = acc;

        int mode = (cmd >> 1) & 0x07;
        operatingMode = mode;

        // BCD bit (bit0) must be 0 for binary
        int bcd = cmd & 0x01;
        if (bcd != 0) {
            System.err.println(getName() + ": BCD mode not supported (bit0 must be 0).");
        }

        // Emulate pit.cpp behavior on mode set: cancel timer and set IRQ baseline
        cancelTimer();
        switch (operatingMode) {
            case 2: // Rate generator
                // set_irq(0, true) baseline (as in pit.cpp)
                setIRQ(true);
                break;
            case 0: // One-shot (interrupt on terminal count)
                setIRQ(false);
                break;
            case 4: // Software triggered strobe
                setIRQ(true);
                break;
            default:
                System.err.println(getName() + ": Operating mode 0x" + Integer.toHexString(operatingMode) + " not supported.");
                break;
        }
        // Reset partial write trackers
        reloaded = 0;
        latchReadCount = 0;
        counterReadCount = 2;
        statusLatched = false;
    }

    /**
     * Simulate write to data port (0x40) for channel 0.
     * Behavior depends on current accessMode: 1=low, 2=high, 3=low then high.
     * When a full reload value has been written, the timer is (re)started.
     */
    public void writeData(int value) {
        value &= 0xFF;

        switch (accessMode) {
            case 1: // low byte only
                reload = (reload & 0xFF00) | value;
                reloaded = 0x03; // treat as complete
                break;

            case 2: // high byte only
                reload = (reload & 0x00FF) | (value << 8);
                reloaded = 0x03; // treat as complete
                break;

            case 3: // low then high
                if (reloaded == 0) {
                    // First: low
                    reload = (reload & 0xFF00) | value;
                    reloaded = 0x01;
                } else if (reloaded == 0x01) {
                    // Second: high
                    reload = (reload & 0x00FF) | (value << 8);
                    reloaded = 0x03;
                } else {
                    // Already complete; restart sequence with new low
                    reload = (reload & 0xFF00) | value;
                    reloaded = 0x01;
                }
                break;

            default:
                System.err.println(getName() + ": writeData with unsupported access mode " + accessMode);
                return;
        }

        // When full reload is written, start the counter
        if (reloaded == 0x03) {
            nullCountFlag = true;
            reloaded = 0;
            switch (operatingMode) {
                case 0:
                case 2:
                case 4:
                    startFromReload();
                    break;
                default:
                    System.err.println(getName() + ": start with unsupported operating mode " + operatingMode);
                    break;
            }
        }
    }

    /**
     * Simulate a read from data port (0x40). Returns 0..65535 value per access mode.
     * For demonstration we return the current counter (or latch if latched).
     */
    public int readData() {
        // If latched, return latch first depending on access mode
        if (latchReadCount > 0) {
            int result;
            if (accessMode == 1) {
                // low byte only
                result = latch & 0x00FF;
                latchReadCount = 0;
                return result;
            } else if (accessMode == 2) {
                // high byte only
                result = (latch >> 8) & 0x00FF;
                latchReadCount = 0;
                return result;
            } else {
                // 3: low then high
                if (latchReadCount == 2) {
                    result = latch & 0x00FF;
                    latchReadCount = 1;
                    return result;
                } else {
                    result = (latch >> 8) & 0x00FF;
                    latchReadCount = 0;
                    return result;
                }
            }
        }
        // Not latched: read current counter similarly
        int result;
        if (accessMode == 1) {
            result = counter & 0x00FF;
            return result;
        } else if (accessMode == 2) {
            result = (counter >> 8) & 0x00FF;
            return result;
        } else {
            // 3: alternating low/high
            if (counterReadCount == 2) {
                result = counter & 0x00FF;
                counterReadCount = 1;
            } else {
                result = (counter >> 8) & 0x00FF;
                counterReadCount = 2;
            }
            return result;
        }
    }

    /**
     * Simulate a latch command: captures current counter into latch.
     */
    public void latchCounter() {
        latch = counter & 0xFFFF;
        latchReadCount = 2;
        statusLatched = false;
    }

    // Internal helper methods

    private void cancelTimer() {
        passivate();
        periodSeconds = Double.POSITIVE_INFINITY;
    }

    private void setIRQ(boolean level) {
        irq = level;
        outGate = level;
    }

    private double countToSeconds(int count) {
        int c = (count & 0xFFFF);
        if (c == 0) {
            c = 0x10000; // 65536 for zero reload semantics
        }
        return c / FREQ_HZ;
    }

    private void startFromReload() {
        nullCountFlag = false;
        counter = (reload == 0) ? MAX16 : (reload & 0xFFFF);
        periodSeconds = countToSeconds(counter);
        holdIn("running", periodSeconds);
    }

    // Simple text-based demo using the DEVS time advance
    public static void main(String[] args) throws ClassNotFoundException {
        PITModel pit = new PITModel("PIT0");
        pit.initialize();

        // Example: Configure mode 2 (rate generator), access mode 3 (low then high), binary
        // Command: channel 0 (00), access 11, mode 010, bcd 0 -> 00 11 010 0 = 0x34
        int cmd = 0x34;
        System.out.println("Writing command 0x" + Integer.toHexString(cmd));
        pit.writeCommand(cmd);

        // Set reload for approx 1kHz: 1193182 Hz / 1000 ≈ 1193 (0x04A9)
        int reloadLow = 0xA9;
        int reloadHigh = 0x04;
        System.out.println("Writing reload low=0x" + Integer.toHexString(reloadLow));
        pit.writeData(reloadLow);
        System.out.println("Writing reload high=0x" + Integer.toHexString(reloadHigh));
        pit.writeData(reloadHigh);

        // Run a few firings; at each internal event, getOutput() is called first by us
        for (int i = 0; i < 5; i++) {
            Double ta = pit.getTimeAdvance();
            System.out.printf("Time advance to next event: %.9f seconds%n", ta);
            // Emulate DEVS: output then internal transition
            pit.getOutput();
            pit.internalTransition();
        }

        // Switch to mode 0 (one-shot) and load a shorter count
        cmd = 0x30; // channel 0, access 11, mode 000, bcd 0
        System.out.println("\nSwitching to mode 0 (one-shot): cmd=0x" + Integer.toHexString(cmd));
        pit.writeCommand(cmd);
        // Reload = 5000 (arbitrary)
        pit.writeData(0x88); // low
        pit.writeData(0x13); // high

        Double ta = pit.getTimeAdvance();
        System.out.printf("Time advance to next event: %.9f seconds%n", ta);
        pit.getOutput();
        pit.internalTransition();

        System.out.println("\nDemo complete. IRQ final state: " + pit.irq);

        
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(pit);
        System.out.println(ses.printTreeString());
        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModel cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

    }
}