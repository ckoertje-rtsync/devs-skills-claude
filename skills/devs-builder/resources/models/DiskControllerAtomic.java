package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.Port;

public class DiskControllerAtomic extends DeviceAtomic {
    private static final long serialVersionUID = 1L;

    private String currentCommand;
    private String diskBuffer;
    private boolean operationComplete;

    public DiskControllerAtomic(String name) {
        super(name);
        currentCommand = null;
        diskBuffer = "InitialDiskData";
        operationComplete = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        currentCommand = null;
        operationComplete = false;
        passivate();
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        MessageBagImpl out = new MessageBagImpl();

        if (operationComplete && currentCommand != null) {
            if (currentCommand.startsWith("READ")) {
                out.add(outMemResp, "DiskReadResp:" + diskBuffer);
            } else if (currentCommand.startsWith("WRITE")) {
                out.add(outMemResp, "DiskWriteResp:OK");
            } else if (currentCommand.equals("STATUS")) {
                out.add(outPortResp, "DiskStatus:READY");
            }
            out.add(irq, Boolean.TRUE);
        }
        return out;
    }

    @Override
    public void internalTransition() {
        currentCommand = null;
        operationComplete = false;
        passivate();
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        java.util.List memOps = getMessages(input, inMemReq);
        java.util.List portOps = getMessages(input, inPortReq);

        if (!memOps.isEmpty()) {
            currentCommand = (String) ((Message) memOps.get(0)).getData();
            if (currentCommand.startsWith("WRITE")) {
                String[] parts = currentCommand.split(":", 2);
                if (parts.length == 2) {
                    diskBuffer = parts[1];
                }
            }
            holdIn("respond", 0.5);
            operationComplete = true;
        }

        if (!portOps.isEmpty()) {
            currentCommand = (String) ((Message) portOps.get(0)).getData();
            if ("STATUS".equals(currentCommand)) {
                holdIn("respond", 0.1);
                operationComplete = true;
            }
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
    }
}