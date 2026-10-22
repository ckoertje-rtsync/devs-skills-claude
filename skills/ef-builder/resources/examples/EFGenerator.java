package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.List;

/**
 * DEVS Atomic Model: EFGenerator (Experimental Frame Generator)
 *
 * Part of the Experimental Frame for the Family Life System.
 * Generates periodic control directives and emotional climate signals
 * that are sent to the LifeSystem model through its external input ports.
 * These stimuli arrive at FS alongside GG's internal outputs, allowing
 * the experimental frame to inject controlled perturbations.
 *
 * Modeled after Genr.java (MS4Systems reference generator pattern).
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 *
 * States (S): GENERATING, PASSIVE
 * Input (X):  stop : Port<String> (from EFTransducer — observation complete)
 * Output (Y): controlExtF       : Port<String> (to LifeSystem.controlExtF)
 *             emotionalClimate  : Port<String> (to LifeSystem.emotionalClimate)
 *
 * delta_int: GENERATING -> GENERATING (count < maxCount)
 *            GENERATING -> PASSIVE    (count >= maxCount)
 * delta_ext: any + stop -> PASSIVE
 * lambda:    GENERATING -> controlExtF + emotionalClimate
 * ta:        GENERATING=period (default 15.0), PASSIVE=INFINITY
 *
 * Starts in GENERATING (ta=period) to inject first stimulus.
 */
public class EFGenerator extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    private Display display = new Display();

    // Output Ports — stimuli to LifeSystem
    public final Port<String> controlExtF;
    public final Port<String> emotionalClimate;

    // Input Ports — stop signal from Transducer
    public final Port<String> stop;

    // State variables
    private double period;
    private int count = 0;
    private int maxCount;

    public EFGenerator(String name, double period, int maxCount) {
        super(name);
        this.period = period;
        this.maxCount = maxCount;
        controlExtF = addOutputPort("controlExtF", String.class);
        emotionalClimate = addOutputPort("emotionalClimate", String.class);
        stop = addInputPort("stop", String.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        count = 0;
        holdIn("GENERATING", period);
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        if (phaseIs("GENERATING")) {
            MessageBagImpl out = new MessageBagImpl();
            int nextCount = count + 1;
            out.add(controlExtF,
                    "EF_GEN_CTRL:stimulus=" + nextCount + ",period=" + period);
            out.add(emotionalClimate,
                    "EF_GEN_CLIMATE:stimulus=" + nextCount + ",tone=experimental");
            System.out.println("[EFGen] === Stimulus " + nextCount
                    + " === Sending controlExtF + emotionalClimate to LifeSystem");
            return out;
        }
        return MessageBag.EMPTY;
    }

    @Override
    public void internalTransition() {
        if (phaseIs("GENERATING")) {
            count++;
            if (count < maxCount) {
                holdIn("GENERATING", period);
            } else {
                passivateIn("PASSIVE");
                System.out.println("[EFGen] Max stimuli reached (" + maxCount
                        + "). Generator going PASSIVE.");
            }
        }
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;

        List stopMsgs = getMessages(input, stop);
        if (!stopMsgs.isEmpty()) {
            String stopData = (String) getFirstMessageValue(stopMsgs);
            System.out.println("[EFGen] Received stop signal: " + stopData);
            passivateIn("PASSIVE");
        }
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
        display.PhaseString(getName() + ": " + phase);
    }

    public int getCount() { return count; }
    public double getPeriod() { return period; }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
}
