package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.List;

/**
 * DEVS Atomic Model: HealthEFGenerator (Experimental Frame Generator)
 *
 * Part of the Experimental Frame for the Health System.
 * Generates periodic scenario-based stimuli that are sent to the
 * HealthSystemModel through its four input ports: career_stress,
 * wealth_funding, family_support, and social_support.
 *
 * Each stimulus cycle injects a predefined scenario that exercises
 * different behavioral regions of the Health System model:
 *   Scenario 1: Baseline                (moderate stress, adequate funding)
 *   Scenario 2: Rising stress           (elevated stress, reduced support)
 *   Scenario 3: Financial strain        (high stress, low funding)
 *   Scenario 4: Combined pressure       (crisis-inducing levels)
 *   Scenario 5: Support surge           (recovery-enabling support)
 *   Scenario 6: Recovery consolidation  (sustained positive inputs)
 *   Scenario 7: Stabilization           (near-optimal conditions)
 *   Scenario 8: Optimal                 (best-case scenario)
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 *
 * States (S): GENERATING, PASSIVE
 * Input (X):  stop : Port<String> (from HealthEFTransducer)
 * Output (Y): career_stress  : Port<String> (to HealthSystemModel)
 *             wealth_funding : Port<String> (to HealthSystemModel)
 *             family_support : Port<String> (to HealthSystemModel)
 *             social_support : Port<String> (to HealthSystemModel)
 *
 * ta: GENERATING=period (default 10.0), PASSIVE=INFINITY
 */
public class HealthEFGenerator extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    private Display display = new Display();

    // ── Output Ports — stimuli to HealthSystemModel ─────────
    public final Port<String> career_stress;
    public final Port<String> wealth_funding;
    public final Port<String> family_support;
    public final Port<String> social_support;

    // ── Input Ports — stop signal from Transducer ───────────
    public final Port<String> stop;

    // ── State variables ─────────────────────────────────────
    private double period;
    private int count = 0;
    private int maxCount;

    // ── Scenario table ──────────────────────────────────────
    // Each row: { careerStress, wealthFunding, familySupport, socialSupport }
    private static final double[][] SCENARIOS = {
        // Scenario 1: Baseline — moderate conditions
        { 0.30, 300.0, 0.50, 0.50 },
        // Scenario 2: Rising stress — elevated work pressure
        { 0.60, 250.0, 0.50, 0.40 },
        // Scenario 3: Financial strain — reduced funding + high stress
        { 0.70, 100.0, 0.40, 0.30 },
        // Scenario 4: Combined pressure — crisis-inducing
        { 0.90,  50.0, 0.20, 0.20 },
        // Scenario 5: Support surge — recovery enablers
        { 0.10, 400.0, 0.80, 0.80 },
        // Scenario 6: Recovery consolidation
        { 0.15, 350.0, 0.70, 0.70 },
        // Scenario 7: Stabilization — near-optimal
        { 0.10, 300.0, 0.60, 0.60 },
        // Scenario 8: Optimal — best-case
        { 0.05, 450.0, 0.80, 0.80 }
    };

    private static final String[] SCENARIO_NAMES = {
        "Baseline", "Rising Stress", "Financial Strain",
        "Combined Pressure", "Support Surge", "Recovery Consolidation",
        "Stabilization", "Optimal"
    };

    public HealthEFGenerator(String name, double period, int maxCount) {
        super(name);
        this.period = period;
        this.maxCount = maxCount;

        career_stress  = addOutputPort("career_stress",  String.class);
        wealth_funding = addOutputPort("wealth_funding", String.class);
        family_support = addOutputPort("family_support", String.class);
        social_support = addOutputPort("social_support", String.class);
        stop           = addInputPort("stop", String.class);
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

            int scenarioIndex = count % SCENARIOS.length;
            double[] scenario = SCENARIOS[scenarioIndex];
            String scenarioName = SCENARIO_NAMES[scenarioIndex];

            out.add(career_stress,  String.valueOf(scenario[0]));
            out.add(wealth_funding, String.valueOf(scenario[1]));
            out.add(family_support, String.valueOf(scenario[2]));
            out.add(social_support, String.valueOf(scenario[3]));

            System.out.println("[HealthEFGen] === Stimulus " + (count + 1)
                    + " [" + scenarioName + "] ==="
                    + " stress=" + scenario[0]
                    + ", funding=" + scenario[1]
                    + ", family=" + scenario[2]
                    + ", social=" + scenario[3]);

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
                System.out.println("[HealthEFGen] Max stimuli reached ("
                        + maxCount + "). Generator going PASSIVE.");
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
            System.out.println("[HealthEFGen] Received stop signal: " + stopData);
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
