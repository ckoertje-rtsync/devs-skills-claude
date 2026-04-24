import java.util.ArrayList;

import com.ms4systems.devs.core.message.impl.Message;
import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;

/**
 * Minimal deterministic Closure-DEVS atomic used by the Efp coupled example.
 * Prints every String that arrives on `in`. Passive otherwise.
 */
public class TinySink extends Atomic {
    private static final long serialVersionUID = 1L;

    public final Port in = addInputPort("in", String.class);

    public TinySink(String name) {
        super(name);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void internalTransition() {
        // passive
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        ArrayList<Message> msgs = input.getMessages(in);
        for (Message m : msgs) {
            System.out.println("[" + getName() + "] received: " + m.getData());
        }
    }

    @Override
    public MessageBag getOutput() {
        return new MessageBag();
    }

    @Override
    public Double getTimeAdvance() {
        return Double.POSITIVE_INFINITY;
    }
}
