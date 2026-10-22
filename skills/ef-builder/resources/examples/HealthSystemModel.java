package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.List;

/**
 * DEVS Atomic Model: HealthSystemModel
 *
 * Represents the Health Subsystem from "The Six Subsystems of Human Life"
 * (Wang, Starowa Life Academy). A whole-person health simulation tracking
 * physical health, mental health, stress accumulation, energy level, and
 * burnout risk. Responds to career stress, wealth funding, family support,
 * and social support inputs.
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 *
 * States (S): IDLE, MAINTAINING, DECLINING, CRISIS, RECOVERING
 *
 * Input (X):
 *   career_stress         : Port<String>  (work pressure)
 *   wealth_funding        : Port<String>  (financial resources)
 *   family_support        : Port<String>  (family involvement)
 *   social_support        : Port<String>  (community network)
 *
 * Output (Y):
 *   health_status         : Port<String>  (clinical status report)
 *   health_to_career      : Port<String>  (health impact on career)
 *   health_to_relationship: Port<String>  (health impact on relationships)
 *
 * ta: IDLE=INF, MAINTAINING=5.0, DECLINING=3.0, CRISIS=2.0, RECOVERING=4.0
 */
public class HealthSystemModel extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    private Display display = new Display();

    // ── Input Ports ──────────────────────────────────────────
    public final Port<String> career_stress;
    public final Port<String> wealth_funding;
    public final Port<String> family_support;
    public final Port<String> social_support;

    // ── Output Ports ─────────────────────────────────────────
    public final Port<String> health_status;
    public final Port<String> health_to_career;
    public final Port<String> health_to_relationship;

    // ── State Variables ──────────────────────────────────────
    private double physicalHealth = 0.70;
    private double mentalHealth = 0.70;
    private double stressAccumulation = 0.20;
    private double energyLevel = 0.56;
    private double burnoutRisk = 0.20;

    // Cross-domain support variables (updated by external inputs)
    private double familySupportLevel = 0.50;
    private double socialSupportLevel = 0.50;
    private double healthFunding = 250.0;

    // Cycle counter
    private int cycleCount = 0;

    // ── Time Advance Constants ───────────────────────────────
    private static final double TA_MAINTAINING = 5.0;
    private static final double TA_DECLINING = 3.0;
    private static final double TA_CRISIS = 2.0;
    private static final double TA_RECOVERING = 4.0;

    public HealthSystemModel(String name) {
        super(name);
        career_stress = addInputPort("career_stress", String.class);
        wealth_funding = addInputPort("wealth_funding", String.class);
        family_support = addInputPort("family_support", String.class);
        social_support = addInputPort("social_support", String.class);
        health_status = addOutputPort("health_status", String.class);
        health_to_career = addOutputPort("health_to_career", String.class);
        health_to_relationship = addOutputPort("health_to_relationship", String.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        physicalHealth = 0.70;
        mentalHealth = 0.70;
        stressAccumulation = 0.20;
        energyLevel = 0.56;
        burnoutRisk = 0.20;
        familySupportLevel = 0.50;
        socialSupportLevel = 0.50;
        healthFunding = 250.0;
        cycleCount = 0;
        passivateIn("IDLE");
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    // ══════════════════════════════════════════════════════════
    // OUTPUT FUNCTION (lambda) — fires before delta_int
    // ══════════════════════════════════════════════════════════
    @Override
    public MessageBag getOutput() {
        if (phaseIs("IDLE")) return MessageBag.EMPTY;

        MessageBagImpl out = new MessageBagImpl();

        // health_status always output in every active state
        String statusMsg = "phase=" + phase
                + ",physicalHealth=" + fmt(physicalHealth)
                + ",mentalHealth=" + fmt(mentalHealth)
                + ",stress=" + fmt(stressAccumulation)
                + ",energy=" + fmt(energyLevel)
                + ",burnout=" + fmt(burnoutRisk)
                + ",cycle=" + cycleCount;
        out.add(health_status, statusMsg);

        // health_to_career: output when DECLINING or CRISIS
        if (phaseIs("DECLINING") || phaseIs("CRISIS")) {
            double careerCapacity = Math.max(0.0, energyLevel * (1.0 - burnoutRisk));
            String limitation = phaseIs("CRISIS") ? "severe" : "moderate";
            out.add(health_to_career,
                    "capacity=" + fmt(careerCapacity) + ",limitation=" + limitation
                    + ",phase=" + phase);
        }

        // health_to_relationship: output in every active state
        double relQuality = (mentalHealth * 0.6 + physicalHealth * 0.2
                + (1.0 - stressAccumulation) * 0.2);
        String availability = energyLevel > 0.5 ? "available" : "limited";
        out.add(health_to_relationship,
                "quality=" + fmt(relQuality) + ",availability=" + availability
                + ",phase=" + phase);

        return out;
    }

    // ══════════════════════════════════════════════════════════
    // INTERNAL TRANSITION (delta_int) — apply state formulas
    // ══════════════════════════════════════════════════════════
    @Override
    public void internalTransition() {
        cycleCount++;

        if (phaseIs("MAINTAINING")) {
            applyMaintainingFormulas();
            // Transition check: declining conditions
            if (stressAccumulation > 0.4 || physicalHealth < 0.5
                    || mentalHealth < 0.5) {
                holdIn("DECLINING", TA_DECLINING);
                System.out.println("[Health] MAINTAINING -> DECLINING"
                        + " (stress=" + fmt(stressAccumulation)
                        + ",phys=" + fmt(physicalHealth)
                        + ",mental=" + fmt(mentalHealth) + ")");
            } else {
                holdIn("MAINTAINING", TA_MAINTAINING);
            }

        } else if (phaseIs("DECLINING")) {
            applyDecliningFormulas();
            // Transition check: crisis conditions
            if (stressAccumulation > 0.6 || physicalHealth < 0.3
                    || mentalHealth < 0.3) {
                holdIn("CRISIS", TA_CRISIS);
                System.out.println("[Health] DECLINING -> CRISIS"
                        + " (stress=" + fmt(stressAccumulation)
                        + ",phys=" + fmt(physicalHealth)
                        + ",mental=" + fmt(mentalHealth) + ")");
            } else if (physicalHealth > 0.6 && mentalHealth > 0.6
                    && stressAccumulation < 0.3) {
                holdIn("MAINTAINING", TA_MAINTAINING);
                System.out.println("[Health] DECLINING -> MAINTAINING (early recovery)");
            } else {
                holdIn("DECLINING", TA_DECLINING);
            }

        } else if (phaseIs("CRISIS")) {
            applyCrisisFormulas();
            // Always transition to RECOVERING after crisis period
            holdIn("RECOVERING", TA_RECOVERING);
            System.out.println("[Health] CRISIS -> RECOVERING"
                    + " (phys=" + fmt(physicalHealth)
                    + ",mental=" + fmt(mentalHealth)
                    + ",burnout=" + fmt(burnoutRisk) + ")");

        } else if (phaseIs("RECOVERING")) {
            applyRecoveringFormulas();
            // Transition check: recovered enough?
            if (physicalHealth > 0.6 && mentalHealth > 0.6) {
                holdIn("MAINTAINING", TA_MAINTAINING);
                System.out.println("[Health] RECOVERING -> MAINTAINING"
                        + " (phys=" + fmt(physicalHealth)
                        + ",mental=" + fmt(mentalHealth) + ")");
            } else {
                holdIn("RECOVERING", TA_RECOVERING);
            }
        }

        display.PhaseString(getName() + ": " + phase);
    }

    // ══════════════════════════════════════════════════════════
    // EXTERNAL TRANSITION (delta_ext) — process cross-domain inputs
    // ══════════════════════════════════════════════════════════
    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;

        // Career Stress: stressAccumulation += level * 0.1
        List stressMsgs = getMessages(input, career_stress);
        if (!stressMsgs.isEmpty()) {
            double level = parseValue((String) getFirstMessageValue(stressMsgs));
            stressAccumulation += level * 0.1;
            stressAccumulation = clamp(stressAccumulation);
            System.out.println("[Health] Received career_stress: level="
                    + fmt(level) + " -> stress=" + fmt(stressAccumulation));
        }

        // Wealth Funding: physicalHealth += min(0.05, funding / 5000)
        List fundingMsgs = getMessages(input, wealth_funding);
        if (!fundingMsgs.isEmpty()) {
            double funding = parseValue((String) getFirstMessageValue(fundingMsgs));
            healthFunding = funding;
            physicalHealth += Math.min(0.05, funding / 5000.0);
            physicalHealth = clamp(physicalHealth);
            System.out.println("[Health] Received wealth_funding: amount="
                    + fmt(funding) + " -> phys=" + fmt(physicalHealth));
        }

        // Family Support: mentalHealth += supportLevel * 0.03
        List familyMsgs = getMessages(input, family_support);
        if (!familyMsgs.isEmpty()) {
            double support = parseValue((String) getFirstMessageValue(familyMsgs));
            familySupportLevel = support;
            mentalHealth += support * 0.03;
            mentalHealth = clamp(mentalHealth);
            System.out.println("[Health] Received family_support: level="
                    + fmt(support) + " -> mental=" + fmt(mentalHealth));
        }

        // Social Support: stressAccumulation *= (1 - socialSupport * 0.3)
        List socialMsgs = getMessages(input, social_support);
        if (!socialMsgs.isEmpty()) {
            double support = parseValue((String) getFirstMessageValue(socialMsgs));
            socialSupportLevel = support;
            stressAccumulation *= (1.0 - support * 0.3);
            stressAccumulation = clamp(stressAccumulation);
            System.out.println("[Health] Received social_support: level="
                    + fmt(support) + " -> stress=" + fmt(stressAccumulation));
        }

        // Recompute energy after all input effects
        energyLevel = clamp((physicalHealth + mentalHealth) / 2.0
                * (1.0 - stressAccumulation));

        // IDLE -> MAINTAINING on first external event
        if (phaseIs("IDLE")) {
            holdIn("MAINTAINING", TA_MAINTAINING);
            System.out.println("[Health] IDLE -> MAINTAINING (first input received)");
        }

        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
        display.PhaseString(getName() + ": " + phase);
    }

    // ══════════════════════════════════════════════════════════
    // STATE FORMULAS (from the document)
    // ══════════════════════════════════════════════════════════

    private void applyMaintainingFormulas() {
        physicalHealth += 0.05 * (healthFunding / 500.0);
        mentalHealth += 0.03 * (socialSupportLevel + familySupportLevel);
        stressAccumulation -= 0.05;
        physicalHealth = clamp(physicalHealth);
        mentalHealth = clamp(mentalHealth);
        stressAccumulation = clamp(stressAccumulation);
        energyLevel = clamp((physicalHealth + mentalHealth) / 2.0
                * (1.0 - stressAccumulation));
        burnoutRisk = clamp(stressAccumulation * 0.3);
    }

    private void applyDecliningFormulas() {
        physicalHealth -= 0.02;
        mentalHealth -= 0.03;
        energyLevel -= 0.05;
        stressAccumulation += 0.03;
        physicalHealth = clamp(physicalHealth);
        mentalHealth = clamp(mentalHealth);
        stressAccumulation = clamp(stressAccumulation);
        energyLevel = clamp(energyLevel);
        burnoutRisk = clamp(stressAccumulation * 0.8);
    }

    private void applyCrisisFormulas() {
        physicalHealth -= 0.05;
        mentalHealth -= 0.05;
        burnoutRisk = 1.0;
        physicalHealth = clamp(physicalHealth);
        mentalHealth = clamp(mentalHealth);
        energyLevel = clamp((physicalHealth + mentalHealth) / 2.0
                * (1.0 - stressAccumulation));
    }

    private void applyRecoveringFormulas() {
        physicalHealth += 0.08;
        mentalHealth += 0.06;
        stressAccumulation -= 0.1;
        energyLevel += 0.05;
        physicalHealth = clamp(physicalHealth);
        mentalHealth = clamp(mentalHealth);
        stressAccumulation = clamp(stressAccumulation);
        energyLevel = clamp(energyLevel);
        burnoutRisk = clamp(stressAccumulation * 0.5);
    }

    // ── Accessors for EF monitoring ──────────────────────────
    public double getPhysicalHealth() { return physicalHealth; }
    public double getMentalHealth() { return mentalHealth; }
    public double getStressAccumulation() { return stressAccumulation; }
    public double getEnergyLevel() { return energyLevel; }
    public double getBurnoutRisk() { return burnoutRisk; }
    public double getHealthFunding() { return healthFunding; }
    public int getCycleCount() { return cycleCount; }

    // ── Helpers ──────────────────────────────────────────────
    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static String fmt(double v) {
        return String.format("%.4f", v);
    }

    private static double parseValue(String msg) {
        try {
            return Double.parseDouble(msg.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
}
