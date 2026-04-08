package Models.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * Example DEVS PIT (Programmable Interval Timer) model built on the
 * Basic-DEVS-Java-Markov-SES framework.
 *
 * - Atomic models extend PhaseAtomic (provided in Models.java package).
 * - Coupled model extends CoupledModelImpl and composes multiple atomics.
 * - A simple main() is provided to exercise the PIT behavior without any
 *   external libraries beyond those in the repository.
 *
 * Notes
 * - This example focuses on channel 0 and supports modes 0 and 2,
 *   mirroring the key behavior illustrated in pit.cpp.
 * - Time units are seconds; frequency is 1.1931816666667 MHz.
 */
public class PITSystem extends CoupledModelImpl {

    private static final long serialVersionUID = 1L;

    public PITSystem() {
        this("PITSystem");
    }

    public PITSystem(String name) {
        super(name);

        // Children
        PITChannel0 pit0 = new PITChannel0("pit0");
        PITTestbench tb = new PITTestbench("tb");

        // Register children
        addChildModel(pit0);
        addChildModel(tb);

        // Wire couplings (tb -> pit)
        addCoupling(tb.getOutputPort("outCmd"), pit0.getInputPort("inCmd"));
        addCoupling(tb.getOutputPort("outData"), pit0.getInputPort("inData"));
        addCoupling(tb.getOutputPort("outReadReq"), pit0.getInputPort("inReadReq"));

        // Wire couplings (pit -> tb)
        addCoupling(pit0.getOutputPort("outData"), tb.getInputPort("inReadData"));
        addCoupling(pit0.getOutputPort("irq"), tb.getInputPort("inIrq"));
    }

    /**
     * Atomic model implementing core PIT channel-0 behavior.
     */
    static class PITChannel0 extends PhaseAtomic {

        private static final long serialVersionUID = 1L;

        // Ports
        private final Port<Byte> inCmd;
        private final Port<Byte> inData;
        private final Port<Integer> inReadReq;

        private final Port<Integer> outData;
        private final Port<Boolean> irq;

        // State (mirrors pit.cpp fields conceptually)
        private static final double FREQ_HZ = 1.1931816666667e6; // 1.193181 MHz
        private static final int U16_MAX = 0xFFFF;

        private int operating_mode = 0;   // modes 0,2,4 considered; we support 0 and 2
        private int access_mode = 0;      // 1=LSB,2=MSB,3=LSB+MSB
        private int reload = 0;           // 16-bit reload register
        private int counter = 0;          // 16-bit current counter value

        private int writeLatch = 0;       // temp for assembling 16-bit reload when access_mode==3
        private int writeCount = 0;       // how many bytes written in current sequence

        // For read behavior (simple "read whole counter" request)
        private boolean respondRead = false;
        private int readValue = 0;

        // Used to account for elapsed time between external events
        private double timeSinceLastEvent = 0.0;

        public PITChannel0(String name) {
            super(name);

            // Inputs
            inCmd = addInputPort("inCmd", Byte.class);
            inData = addInputPort("inData", Byte.class);          // write to channel 0 data port (0x40)
            inReadReq = addInputPort("inReadReq", Integer.class); // request to read N bytes (1 or 2). We respond with 16b Integer

            // Outputs
            outData = addOutputPort("outData", Integer.class);    // read result (16-bit)
            irq = addOutputPort("irq", Boolean.class);            // IRQ pulse
        }

        @Override
        public void initialize() {
            super.initialize();
            // Default is mode 2 (rate generator) with no reload until written.
            operating_mode = 2;
            access_mode = 3; // expect LSB then MSB by default
            reload = 0;
            counter = 0;
            writeLatch = 0;
            writeCount = 0;
            respondRead = false;
            readValue = 0;
            timeSinceLastEvent = 0.0;

            // No timer scheduled until we receive a reload (same as pit.cpp)
            passivate();
        }

        @Override
        public Double getTimeAdvance() {
            return sigma;
        }

        @Override
        public MessageBag getOutput() {
            // Output occurs right before an internal event.
            if (phaseIs("irq")) {
                MessageBagImpl out = new MessageBagImpl();
                out.add(irq, Boolean.TRUE);
                return out;
            } else if (phaseIs("respond_read")) {
                MessageBagImpl out = new MessageBagImpl();
                out.add(outData, Integer.valueOf(readValue & U16_MAX));
                return out;
            }
            return MessageBag.EMPTY;
        }

        @Override
        public void internalTransition() {
            if (phaseIs("irq")) {
                // Timer reached zero
                counter = 0;

                // Reschedule according to mode
                switch (operating_mode) {
                    case 0: // Interrupt on terminal count (one-shot)
                        passivate();
                        break;
                    case 2: // Rate generator (periodic)
                        // Immediately schedule the next expiration using the reload
                        scheduleFromReload();
                        break;
                    case 4: // Software triggered strobe (basic pulse)
                        passivate();
                        break;
                    default:
                        // Unsupported -> passivate to be safe
                        passivate();
                        break;
                }
            } else if (phaseIs("respond_read")) {
                // Just completed a synchronous read response
                passivate();
            } else {
                passivate();
            }

            // Reset the time accumulator after any internal transition
            timeSinceLastEvent = 0.0;
        }

        @Override
        public void externalTransition(double timeElapsed, MessageBag input) {
            // Account for elapsed time since last internal event (for simple counter update)
            if (timeElapsed > 0) {
                timeSinceLastEvent += timeElapsed;
                decrementCounterByElapsed(timeElapsed);
            }

            // Process any incoming messages (command / data / read)
            // Assumes MessageBagImpl semantics: getMessages(port) returns a List<T> or empty list.
            List<Byte> cmds = getMessages(input, inCmd);
            List<Byte> datas = getMessages(input, inData);
            List<Integer> reads = getMessages(input, inReadReq);

            // Command write(s)
            if (!cmds.isEmpty()) {
                for (Byte b : cmds) {
                    int cmd = b & 0xFF;

                    // Only channel 0 is modeled; command format per 8253/8254
                    int chSel = (cmd >> 6) & 0x03;
                    if (chSel == 0) {
                        access_mode = (cmd >> 4) & 0x03;
                        if (access_mode == 0) {
                            // Illegal in 8253; ignore
                            access_mode = 3;
                        }
                        operating_mode = (cmd >> 1) & 0x07; // we will support 0,2,4 similarly to pit.cpp
                        // Reset write sequencing
                        writeCount = 0;
                        writeLatch = 0;

                        // Cancel any running timer and set IRQ gate baseline per pit.cpp behavior
                        passivate();
                    }
                    // Read-back/latch commands are omitted for brevity in this example
                }
            }

            // Data write(s) to reload register
            if (!datas.isEmpty()) {
                for (Byte b : datas) {
                    int val = b & 0xFF;
                    switch (access_mode) {
                        case 1: // LSB only
                            reload = (reload & 0xFF00) | val;
                            writeCount = 2; // treat as complete
                            break;
                        case 2: // MSB only
                            reload = (reload & 0x00FF) | (val << 8);
                            writeCount = 2; // treat as complete
                            break;
                        case 3: // LSB then MSB
                        default:
                            if (writeCount == 0) {
                                writeLatch = val & 0xFF;
                                writeCount = 1;
                            } else {
                                reload = ((val & 0xFF) << 8) | (writeLatch & 0xFF);
                                writeCount = 2; // complete
                            }
                            break;
                    }

                    // Start timer once we have a full reload value
                    if (writeCount >= 2) {
                        writeCount = 0;
                        startFromReload();
                    }
                }
            }

            // Read request: respond with the current 16-bit counter value
            if (!reads.isEmpty()) {
                // Here, we simply return the "whole" counter (16-bit)
                readValue = counter & U16_MAX;
                holdIn("respond_read", 0.0);
            }
        }

        @Override
        public void confluentTransition(MessageBag input) {
            // Process internal, then immediate external with 0 elapsed, as in AtomicModelImpl
            internalTransition();
            externalTransition(0.0, input);
        }

        // Helpers

        private void startFromReload() {
            // Per 8253: a reload of 0 implies 2^16 (65536)
            int load = (reload == 0) ? (U16_MAX + 1) : (reload & U16_MAX);
            counter = (reload == 0) ? U16_MAX : (reload & U16_MAX);

            double seconds = ((double) load) / FREQ_HZ;

            // Choose phase according to mode; we output IRQ at the end of sigma
            switch (operating_mode) {
                case 0: // one-shot
                    holdIn("irq", seconds);
                    break;
                case 2: // periodic rate generator
                    holdIn("irq", seconds);
                    break;
                case 4: // strobe; approximate with single pulse
                    holdIn("irq", seconds);
                    break;
                default:
                    // Unsupported -> passivate
                    passivate();
                    break;
            }
        }

        private void scheduleFromReload() {
            // For periodic modes, the counter is reloaded identically
            int load = (reload == 0) ? (U16_MAX + 1) : (reload & U16_MAX);
            counter = (reload == 0) ? U16_MAX : (reload & U16_MAX);
            double seconds = ((double) load) / FREQ_HZ;
            holdIn("irq", seconds);
        }

        private void decrementCounterByElapsed(double elapsedSeconds) {
            if (counter <= 0) return;
            long ticks = (long) Math.floor(elapsedSeconds * FREQ_HZ);
            if (ticks <= 0) return;

            if (ticks < counter) {
                counter -= (int) ticks;
            } else {
                // emulate wrap behavior as in pit.cpp's update_counter()
                int rem = (int) (ticks % (U16_MAX + 1L));
                counter = U16_MAX - rem;
            }
        }

        // Type-safe extraction from MessageBag (assumes MessageBagImpl)
        @SuppressWarnings("unchecked")
        private static <T extends Serializable> List<T> getMessages(MessageBag bag, Port<T> port) {
            if (bag == null || bag == MessageBag.EMPTY) return new ArrayList<T>(0);
            try {
                // MessageBagImpl exposes getMessages(Port<T>) in the MS4 DEVS library.
                // return ((MessageBagImpl) bag).getMessages(port);
                return null;
            } catch (Exception e) {
                return new ArrayList<T>(0);
            }
        }
    }

    /**
     * A simple testbench atomic:
     * - Programs channel 0 to mode 2, access mode LSB+MSB, binary (command 0x34).
     * - Writes a 1ms period (approximately 1193 counts).
     * - Requests a few reads while PIT runs and prints IRQ observations.
     */
    static class PITTestbench extends PhaseAtomic {

        private static final long serialVersionUID = 1L;

        // Outputs to PIT
        private final Port<Byte> outCmd;
        private final Port<Byte> outData;
        private final Port<Integer> outReadReq;

        // Inputs from PIT
        private final Port<Integer> inReadData;
        private final Port<Boolean> inIrq;

        // Local control
        private int step = 0;
        private int readsRemaining = 3;

        // Captured observations
        private final ArrayList<Integer> observedReads = new ArrayList<Integer>();
        private int irqCount = 0;

        public PITTestbench(String name) {
            super(name);

            // Outputs
            outCmd = addOutputPort("outCmd", Byte.class);
            outData = addOutputPort("outData", Byte.class);
            outReadReq = addOutputPort("outReadReq", Integer.class);

            // Inputs
            inReadData = addInputPort("inReadData", Integer.class);
            inIrq = addInputPort("inIrq", Boolean.class);
        }

        @Override
        public void initialize() {
            super.initialize();
            step = 0;
            readsRemaining = 3;
            observedReads.clear();
            irqCount = 0;
            holdIn("drive", 0.0); // start immediately
        }

        @Override
        public Double getTimeAdvance() {
            return sigma;
        }

        @Override
        public MessageBag getOutput() {
            if (!phaseIs("drive")) return MessageBag.EMPTY;

            MessageBagImpl out = new MessageBagImpl();

            switch (step) {
                case 0:
                    // Write command: ch=0, access=LSB+MSB (3), mode=2, binary=0 -> 0x34
                    out.add(outCmd, (byte) 0x34);
                    break;
                case 1:
                    // Write LSB for ~1ms period (1193 decimal -> 0x04A9 -> LSB=0xA9, MSB=0x04)
                    out.add(outData, (byte) 0xA9);
                    break;
                case 2:
                    // Write MSB
                    out.add(outData, (byte) 0x04);
                    break;
                default:
                    // After programming, periodically issue read requests
                    if (readsRemaining > 0) {
                        out.add(outReadReq, Integer.valueOf(2)); // request 2 bytes (we return full 16-bit)
                    }
                    break;
            }
            return out;
        }

        @Override
        public void internalTransition() {
            if (phaseIs("drive")) {
                step++;
                if (step <= 2) {
                    // Send next programming byte immediately
                    holdIn("drive", 0.0);
                } else if (readsRemaining > 0) {
                    // Space read requests about 2 ms apart
                    readsRemaining--;
                    holdIn("drive", 0.002);
                } else {
                    // Done driving; wait a bit then passivate
                    passivate();
                    System.out.println("[TB] Observed IRQs=" + irqCount + " reads=" + observedReads);
                }
            } else {
                passivate();
            }
        }

        @Override
        public void externalTransition(double timeElapsed, MessageBag input) {
            // Capture any incoming read data
            List<Integer> reads = getMessages(input, inReadData);
            if (!reads.isEmpty()) {
                observedReads.add(reads.get(0));
            }
            // Count IRQ pulses
            List<Boolean> irqs = getMessages(input, inIrq);
            if (!irqs.isEmpty()) {
                for (Boolean b : irqs) if (Boolean.TRUE.equals(b)) irqCount++;
            }
        }

        @Override
        public void confluentTransition(MessageBag input) {
            internalTransition();
            externalTransition(0.0, input);
        }

        @SuppressWarnings("unchecked")
        private static <T extends Serializable> List<T> getMessages(MessageBag bag, Port<T> port) {
            if (bag == null || bag == MessageBag.EMPTY) return new ArrayList<T>(0);
            try {
                // return ((MessageBagImpl) bag).getMessages(port);
                // bag.add(out, msg);
                // return bag;
                return null;
            } catch (Exception e) {
                return new ArrayList<T>(0);
            }
        }
    }

    /**
     * Simple driver that demonstrates the PIT behavior by manually stepping
     * the atomic model in a DEVS-like manner.
     *
     * This avoids any external frameworks beyond those in the repository and
     * can be used to sanity-check the PITChannel0 logic and the testbench.
     */
    public static void main(String[] args) throws ClassNotFoundException {
        // Build coupled model with children
        PITSystem top = new PITSystem();

        // Fetch references to children
        PITChannel0 pit = (PITChannel0) top.getComponentWithName("pit0");
        PITTestbench tb = (PITTestbench) top.getComponentWithName("tb");

        // Initialize models (normally the coordinator would do this)
        top.initialize();
        pit.initialize();
        tb.initialize();

        // Very small event loop to mimic coordinator behavior for this example.
        // We alternate among the two atomics, always taking the next event
        // in time (min sigma), generating outputs, and applying internal transitions.
        double t = 0.0;
        final double until = 0.02; // run ~20 ms of simulated time

        // while (t < until) {
        //     double taPit = pit.getTimeAdvance();
        //     double taTb = tb.getTimeAdvance();

        //     // Next imminent time
        //     double next = Math.min(taPit, taTb);
        //     if (Double.isInfinite(next)) break;

        //     // Advance "simulation time"
        //     t += next;

            // Collect and deliver outputs scheduled at this time
            // // 1) PIT outputs
            // if (taPit == next) {
            //     MessageBag y = pit.getOutput();
            //     if (y != MessageBag.EMPTY) {
            //         // Deliver to TB through the same ports the coupled model wires
            //         // (irq, outData -> inIrq, inReadData)
            //         tb.externalTransition(0.0, y);
            //     }
            //     pit.internalTransition();
            // } else {
            //     // Let PIT know time elapsed without input
            //     pit.externalTransition(next, MessageBag.EMPTY);
            // }

            // // 2) TB outputs
            // if (taTb == next) {
            //     MessageBag y = tb.getOutput();
            //     if (y != MessageBag.EMPTY) {
            //         // Deliver to PIT (inCmd, inData, inReadReq)
            //         pit.externalTransition(0.0, y);
            //     } else {
            //         // If TB produced no output, still inform PIT about elapsed time
            //         pit.externalTransition(0.0, MessageBag.EMPTY);
            //     }
            //     tb.internalTransition();
            // } else {
            //     // Let TB know time elapsed without input
            //     tb.externalTransition(next, MessageBag.EMPTY);
            // }
        // }

        System.out.println("PIT demo complete at t=" + t + " seconds.");

         sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println(ses.printTreeString());
        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModel cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

    }

    
}