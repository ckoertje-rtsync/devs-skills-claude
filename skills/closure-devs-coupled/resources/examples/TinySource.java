import java.io.Serializable;

import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;

/**
 * Minimal deterministic Closure-DEVS atomic used by the Ef/Efp coupled examples.
 * Emits a String on `out` every `period` seconds.
 */
public class TinySource extends Atomic {
    private static final long serialVersionUID = 1L;

    public final Port out = addOutputPort("out", String.class);

    private double sigma;
    private final double period;
    private int count;

    public TinySource(String name, double period) {
        super(name);
        this.period = period;
    }

    @Override
    public void initialize() {
        super.initialize();
        count = 0;
        sigma = period;
    }

    @Override
    public void internalTransition() {
        count++;
        sigma = period;
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;
    }

    @Override
    public MessageBag getOutput() {
        MessageBag bag = new MessageBag();
        bag.add(out.createMessage("tick-" + count));
        return bag;
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }
}
