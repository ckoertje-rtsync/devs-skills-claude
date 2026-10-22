import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.impl.Message;
import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;

/**
 * JobProcessor — single-server processor without a queue (Closure-DEVS).
 *
 * Accepts one job on {@code in} while passive, holds in {@code busy} for a
 * fixed processing time, emits the job on {@code out}, and returns to
 * {@code passive}. Jobs arriving while busy are dropped.
 */
public class JobProcessor extends Atomic {
    private static final long serialVersionUID = 1L;

    // ─── Ports ────────────────────────────────────────────────────────────
    public final Port in  = addInputPort("in", Serializable.class);
    public final Port out = addOutputPort("out", Serializable.class);

    // ─── State ────────────────────────────────────────────────────────────
    private String phase = "passive";
    private Double sigma = Double.POSITIVE_INFINITY;
    private Serializable job = null;
    private int processed = 0;
    private final double processingTime;

    public JobProcessor() {
        this("JobProcessor", 1.0);
    }

    public JobProcessor(String name, double processingTime) {
        super(name);
        this.processingTime = processingTime;
    }

    @Override
    public void initialize() {
        super.initialize();
        job = null;
        processed = 0;
        passivateIn("passive");
    }

    @Override
    public void internalTransition() {
        if (phaseIs("busy")) {
            processed++;
            job = null;
            passivateIn("passive");
        }
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;
        ArrayList<Message> msgs = input.getMessages(in);
        if (phaseIs("passive") && !msgs.isEmpty()) {
            job = (Serializable) msgs.get(0).getData();
            holdIn("busy", processingTime);
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0, input);
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        MessageBag output = new MessageBag();
        if (phaseIs("busy")) {
            output.add(out, job);
        }
        return output;
    }

    // ─── Phase/sigma helpers (not inherited from Atomic) ─────────────────
    public void holdIn(String phase, Double sigma) {
        this.phase = phase;
        this.sigma = sigma;
    }

    protected void passivateIn(String phase) {
        holdIn(phase, Double.POSITIVE_INFINITY);
    }

    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    public int getProcessed() {
        return processed;
    }
}
