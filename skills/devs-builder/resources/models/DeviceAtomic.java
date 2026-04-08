package Models.java;


import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.Port;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Device atomic model.
 * - Provides ports for memory, port, timer requests and interrupts.
 * - Can be extended for specific peripherals (disk, NIC, etc).
 */
public class DeviceAtomic extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    // Inputs
    protected final Port<String> inMemReq;     // memory read/write requests
    protected final Port<String> inPortReq;    // I/O port read/write requests
    protected final Port<String> inTimerReq;   // timer requests

    // Outputs
    protected final Port<String> outMemResp;   // memory response
    protected final Port<String> outPortResp;  // port response
    protected final Port<String> outTimerResp; // timer response
    protected final Port<Boolean> irq;         // interrupt line

    // Internal state
    protected String lastMemOp;
    protected String lastPortOp;
    protected String lastTimerOp;
    protected boolean raiseIrq;

    public DeviceAtomic(String name) {
        super(name);

        // Define ports
        inMemReq = addInputPort("inMemReq", String.class);
        inPortReq = addInputPort("inPortReq", String.class);
        inTimerReq = addInputPort("inTimerReq", String.class);

        outMemResp = addOutputPort("outMemResp", String.class);
        outPortResp = addOutputPort("outPortResp", String.class);
        outTimerResp = addOutputPort("outTimerResp", String.class);
        irq = addOutputPort("irq", Boolean.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        lastMemOp = null;
        lastPortOp = null;
        lastTimerOp = null;
        raiseIrq = false;
        passivate();
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        MessageBagImpl out = new MessageBagImpl();
        if (lastMemOp != null) {
            out.add(outMemResp, "MemResp:" + lastMemOp);
        }
        if (lastPortOp != null) {
            out.add(outPortResp, "PortResp:" + lastPortOp);
        }
        if (lastTimerOp != null) {
            out.add(outTimerResp, "TimerResp:" + lastTimerOp);
        }
        if (raiseIrq) {
            out.add(irq, Boolean.TRUE);
        }
        return out;
    }

    @Override
    public void internalTransition() {
        // Clear outputs after emission
        lastMemOp = null;
        lastPortOp = null;
        lastTimerOp = null;
        raiseIrq = false;
        passivate();
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        List<String> memOps = getMessages(input, inMemReq);
        List<String> portOps = getMessages(input, inPortReq);
        List<String> timerOps = getMessages(input, inTimerReq);

        if (!memOps.isEmpty()) {
            lastMemOp = memOps.get(0);
            raiseIrq = true; // signal interrupt on memory op
            holdIn("respond", 0.0);
        }
        if (!portOps.isEmpty()) {
            lastPortOp = portOps.get(0);
            raiseIrq = true;
            holdIn("respond", 0.0);
        }
        if (!timerOps.isEmpty()) {
            lastTimerOp = timerOps.get(0);
            raiseIrq = true;
            holdIn("respond", 0.0);
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
    }

    @SuppressWarnings("unchecked")
    protected static List getMessages(MessageBag bag, Port port) {
            return ((MessageBagImpl) bag).getMessages(port);
    }
}
