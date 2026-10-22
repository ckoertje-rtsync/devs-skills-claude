package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.ArrayList;
import java.util.List;

/**
 * DEVS Atomic Model: HealthEFTransducer (Experimental Frame Transducer)
 *
 * Part of the Experimental Frame for the Health System.
 * Receives health status reports, career impact reports, and
 * relationship impact reports from the HealthSystemModel through
 * its three output ports. Collects statistics over an observation
 * window and outputs a stop signal when the window expires.
 *
 * Tracks:
 *   - Number of reports received per port
 *   - State transition history
 *   - Physical health and mental health trends
 *   - Stress accumulation peaks
 *   - Burnout risk levels
 *   - Career limitation events
 *   - Relationship quality trends
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 *
 * States (S): OBSERVING, DONE
 * Input (X):  health_status          : Port<String> (from HealthSystemModel)
 *             health_to_career       : Port<String> (from HealthSystemModel)
 *             health_to_relationship : Port<String> (from HealthSystemModel)
 * Output (Y): stop                   : Port<String> (to HealthEFGenerator)
 *
 * ta: OBSERVING=observationTime (default 100.0), DONE=INFINITY
 */
public class HealthEFTransducer extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    private Display display = new Display();

    // ── Input Ports — status from HealthSystemModel ─────────
    public final Port<String> health_status;
    public final Port<String> health_to_career;
    public final Port<String> health_to_relationship;

    // ── Output Ports — stop signal to Generator ─────────────
    public final Port<String> stop;

    // ── State variables ─────────────────────────────────────
    private double observationTime;
    private double clock = 0.0;

    // Report counters
    private int statusReports = 0;
    private int careerReports = 0;
    private int relationshipReports = 0;

    // Logs
    private List<String> statusLog = new ArrayList<String>();
    private List<String> careerLog = new ArrayList<String>();
    private List<String> relationshipLog = new ArrayList<String>();

    // Tracking variables
    private int crisisCount = 0;
    private int decliningCount = 0;
    private int careerLimitationCount = 0;

    public HealthEFTransducer(String name, double observationTime) {
        super(name);
        this.observationTime = observationTime;

        health_status          = addInputPort("health_status", String.class);
        health_to_career       = addInputPort("health_to_career", String.class);
        health_to_relationship = addInputPort("health_to_relationship", String.class);
        stop                   = addOutputPort("stop", String.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        clock = 0.0;
        statusReports = 0;
        careerReports = 0;
        relationshipReports = 0;
        statusLog = new ArrayList<String>();
        careerLog = new ArrayList<String>();
        relationshipLog = new ArrayList<String>();
        crisisCount = 0;
        decliningCount = 0;
        careerLimitationCount = 0;
        holdIn("OBSERVING", observationTime);
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        if (phaseIs("OBSERVING")) {
            MessageBagImpl out = new MessageBagImpl();
            int totalReports = statusReports + careerReports + relationshipReports;
            out.add(stop, "EF_STOP:observationTime=" + observationTime
                    + ",totalReports=" + totalReports
                    + ",statusReports=" + statusReports
                    + ",careerReports=" + careerReports
                    + ",relationshipReports=" + relationshipReports);
            System.out.println("[HealthEFTrans] Observation window expired."
                    + " Sending stop to Generator.");
            return out;
        }
        return MessageBag.EMPTY;
    }

    @Override
    public void internalTransition() {
        if (phaseIs("OBSERVING")) {
            clock += sigma;
            passivateIn("DONE");

            int totalReports = statusReports + careerReports + relationshipReports;
            double throughput = (clock > 0.0 && totalReports > 0)
                    ? totalReports / clock : 0.0;

            System.out.println();
            System.out.println("[HealthEFTrans] ════════════════════════════════════════════════════════════════");
            System.out.println("[HealthEFTrans]  HEALTH SYSTEM TRANSDUCER — OBSERVATION SUMMARY");
            System.out.println("[HealthEFTrans] ════════════════════════════════════════════════════════════════");
            System.out.println("[HealthEFTrans]  Observation Time:       " + observationTime);
            System.out.println("[HealthEFTrans]  Final Clock:            " + String.format("%.2f", clock));
            System.out.println("[HealthEFTrans]  ────────────────────────────────────────────────────");
            System.out.println("[HealthEFTrans]  REPORT COUNTS:");
            System.out.println("[HealthEFTrans]    health_status:          " + statusReports);
            System.out.println("[HealthEFTrans]    health_to_career:       " + careerReports);
            System.out.println("[HealthEFTrans]    health_to_relationship: " + relationshipReports);
            System.out.println("[HealthEFTrans]    TOTAL:                  " + totalReports);
            System.out.println("[HealthEFTrans]  ────────────────────────────────────────────────────");
            System.out.println("[HealthEFTrans]  Throughput:              "
                    + String.format("%.4f", throughput) + " reports/time-unit");
            System.out.println("[HealthEFTrans]  ────────────────────────────────────────────────────");
            System.out.println("[HealthEFTrans]  STATE TRANSITION EVENTS:");
            System.out.println("[HealthEFTrans]    CRISIS events:          " + crisisCount);
            System.out.println("[HealthEFTrans]    DECLINING events:       " + decliningCount);
            System.out.println("[HealthEFTrans]    Career limitations:     " + careerLimitationCount);

            if (!statusLog.isEmpty()) {
                System.out.println("[HealthEFTrans]  ────────────────────────────────────────────────────");
                System.out.println("[HealthEFTrans]  LAST STATUS REPORT:");
                System.out.println("[HealthEFTrans]    " + statusLog.get(statusLog.size() - 1));
            }
            if (!careerLog.isEmpty()) {
                System.out.println("[HealthEFTrans]  LAST CAREER REPORT:");
                System.out.println("[HealthEFTrans]    " + careerLog.get(careerLog.size() - 1));
            }
            if (!relationshipLog.isEmpty()) {
                System.out.println("[HealthEFTrans]  LAST RELATIONSHIP REPORT:");
                System.out.println("[HealthEFTrans]    " + relationshipLog.get(relationshipLog.size() - 1));
            }

            System.out.println("[HealthEFTrans]  ────────────────────────────────────────────────────");
            System.out.println("[HealthEFTrans]  Status:                 OBSERVATION COMPLETE");
            System.out.println("[HealthEFTrans] ════════════════════════════════════════════════════════════════");
            System.out.println();
        }
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;
        clock += timeElapsed;

        // health_status reports
        List hStatusMsgs = getMessages(input, health_status);
        if (!hStatusMsgs.isEmpty()) {
            String data = (String) getFirstMessageValue(hStatusMsgs);
            statusReports++;
            statusLog.add("t=" + String.format("%.2f", clock) + " " + data);
            System.out.println("[HealthEFTrans] Received health_status #"
                    + statusReports + " at t=" + String.format("%.2f", clock)
                    + ": " + data);

            // Track state transitions
            if (data.contains("phase=CRISIS")) {
                crisisCount++;
            } else if (data.contains("phase=DECLINING")) {
                decliningCount++;
            }
        }

        // health_to_career reports
        List careerMsgs = getMessages(input, health_to_career);
        if (!careerMsgs.isEmpty()) {
            String data = (String) getFirstMessageValue(careerMsgs);
            careerReports++;
            careerLog.add("t=" + String.format("%.2f", clock) + " " + data);
            System.out.println("[HealthEFTrans] Received health_to_career #"
                    + careerReports + " at t=" + String.format("%.2f", clock)
                    + ": " + data);

            if (data.contains("limitation=severe") || data.contains("limitation=moderate")) {
                careerLimitationCount++;
            }
        }

        // health_to_relationship reports
        List relMsgs = getMessages(input, health_to_relationship);
        if (!relMsgs.isEmpty()) {
            String data = (String) getFirstMessageValue(relMsgs);
            relationshipReports++;
            relationshipLog.add("t=" + String.format("%.2f", clock) + " " + data);
            System.out.println("[HealthEFTrans] Received health_to_relationship #"
                    + relationshipReports + " at t=" + String.format("%.2f", clock)
                    + ": " + data);
        }

        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
        display.PhaseString(getName() + ": " + phase);
    }

    // ── Accessors for external monitoring ───────────────────
    public double getClock() { return clock; }
    public double getObservationTime() { return observationTime; }
    public int getStatusReports() { return statusReports; }
    public int getCareerReports() { return careerReports; }
    public int getRelationshipReports() { return relationshipReports; }
    public int getCrisisCount() { return crisisCount; }
    public int getDecliningCount() { return decliningCount; }
    public int getCareerLimitationCount() { return careerLimitationCount; }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
}
