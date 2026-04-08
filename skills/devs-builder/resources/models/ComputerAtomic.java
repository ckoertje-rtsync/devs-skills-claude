package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.Port;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Computer atomic model.
 * - Issues memory, port, and timer requests to a Device.
 * - Receives responses and IRQs.
 */
public class ComputerAtomic extends PhaseAtomic {

    private static final long serialVersionUID = 1L;
    
    // --- Outputs: requests sent to Device ---
    public final Port<String> outMemReq;    // memory read/write request
    public final Port<String> outPortReq;   // I/O port read/write request
    public final Port<String> outTimerReq;  // timer request

    // --- Inputs: responses received from Device ---
    public final Port<String> inMemResp;    // memory response
    public final Port<String> inPortResp;   // port response
    public final Port<String> inTimerResp;  // timer response
    public final Port<Boolean> inIrq;       // interrupt line

    public ComputerAtomic(String name) {
        super(name);

        // Define ports
        outMemReq   = addOutputPort("outMemReq", String.class);
        outPortReq  = addOutputPort("outPortReq", String.class);
        outTimerReq = addOutputPort("outTimerReq", String.class);

        inMemResp   = addInputPort("inMemResp", String.class);
        inPortResp  = addInputPort("inPortResp", String.class);
        inTimerResp = addInputPort("inTimerResp", String.class);
        inIrq       = addInputPort("inIrq", Boolean.class);
    }



    


    // Local control
    private int step = 0;



    @Override
    public void initialize() {
        super.initialize();
        step = 0;
        holdIn("drive", 0.0);
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
                out.add(inMemResp, "Write addr=0x1000 val=42");
                break;
            case 1:
                out.add(inPortResp, "Read port=0x3F8");
                break;
            case 2:
                out.add(inTimerResp, "Set timer=100ms");
                break;
            default:
                break;
        }
        return out;
    }

    @Override
    public void internalTransition() {
        if (phaseIs("drive")) {
            step++;
            if (step < 3) {
                holdIn("drive", 0.001); // schedule next request
            } else {
                passivate();
            }
        }
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        List<String> memResp = getMessages(input, inMemResp);
        List<String> portResp = getMessages(input, inPortResp);
        List<String> timerResp = getMessages(input, inTimerResp);
        List<Boolean> irqs = getMessages(input, inIrq);

        if (!memResp.isEmpty()) {
            System.out.println("[Computer] Received MemResp: " + memResp.get(0));
        }
        if (!portResp.isEmpty()) {
            System.out.println("[Computer] Received PortResp: " + portResp.get(0));
        }
        if (!timerResp.isEmpty()) {
            System.out.println("[Computer] Received TimerResp: " + timerResp.get(0));
        }
        if (!irqs.isEmpty() && Boolean.TRUE.equals(irqs.get(0))) {
            System.out.println("[Computer] IRQ received!");
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
    }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port port) {
        return ((MessageBagImpl) bag).getMessages(port);
}
}