package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.ArrayList;
import java.util.List;

/**
 * DEVS Atomic Model: EFTransducer (Experimental Frame Transducer)
 *
 * Part of the Experimental Frame for the Family Life System.
 * Receives family status reports from the LifeSystem model through
 * its fStatusExt output port. Collects statistics over an observation
 * window and outputs a stop signal when the window expires.
 *
 * Modeled after Transd.java (MS4Systems reference transducer pattern).
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 *
 * States (S): OBSERVING, DONE
 * Input (X):  fStatusExt : Port<String> (from LifeSystem.fStatusExt)
 * Output (Y): stop       : Port<String> (to EFGenerator — end experiment)
 *
 * delta_int: OBSERVING -> DONE (prints statistics, goes passive)
 * delta_ext: OBSERVING + fStatusExt -> OBSERVING (collect data, no reschedule)
 * lambda:    OBSERVING -> stop
 * ta:        OBSERVING=observationTime (default 100.0), DONE=INFINITY
 *
 * Starts in OBSERVING (ta=observationTime).
 */
public class EFTransducer extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    private Display display = new Display();

    // Input Ports — status from LifeSystem
    public final Port<String> fStatusExt;

    // Output Ports — stop signal to Generator
    public final Port<String> stop;

    // State variables
    private double observationTime;
    private double clock = 0.0;
    private int reportsReceived = 0;
    private List<String> statusLog = new ArrayList<String>();

    public EFTransducer(String name, double observationTime) {
        super(name);
        this.observationTime = observationTime;
        fStatusExt = addInputPort("fStatusExt", String.class);
        stop = addOutputPort("stop", String.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        clock = 0.0;
        reportsReceived = 0;
        statusLog = new ArrayList<String>();
        // Start observing immediately
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
            // Observation window expiring — send stop signal to Generator
            MessageBagImpl out = new MessageBagImpl();
            out.add(stop, "EF_STOP:observationTime=" + observationTime
                    + ",reportsReceived=" + reportsReceived);
            System.out.println("[EFTrans] Observation window expired. Sending stop to Generator.");
            return out;
        }
        return MessageBag.EMPTY;
    }

    @Override
    public void internalTransition() {
        if (phaseIs("OBSERVING")) {
            clock += sigma;
            passivateIn("DONE");

            // Print experiment statistics
            double throughput = (clock > 0.0 && reportsReceived > 0)
                    ? reportsReceived / clock : 0.0;

            System.out.println();
            System.out.println("[EFTrans] ════════════════════════════════════════════════════");
            System.out.println("[EFTrans]  TRANSDUCER OBSERVATION SUMMARY");
            System.out.println("[EFTrans] ════════════════════════════════════════════════════");
            System.out.println("[EFTrans]  Observation Time:   " + observationTime);
            System.out.println("[EFTrans]  Final Clock:        " + clock);
            System.out.println("[EFTrans]  Reports Received:   " + reportsReceived);
            System.out.println("[EFTrans]  Throughput:          " + String.format("%.4f", throughput)
                    + " reports/time-unit");
            if (!statusLog.isEmpty()) {
                System.out.println("[EFTrans]  Last Report:        " + statusLog.get(statusLog.size() - 1));
            }
            System.out.println("[EFTrans]  Status:             OBSERVATION COMPLETE");
            System.out.println("[EFTrans] ════════════════════════════════════════════════════");
            System.out.println();
        }
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        sigma -= timeElapsed;
        clock += timeElapsed;

        List fStatusMsgs = getMessages(input, fStatusExt);
        if (!fStatusMsgs.isEmpty()) {
            String statusData = (String) getFirstMessageValue(fStatusMsgs);
            reportsReceived++;
            statusLog.add("t=" + String.format("%.2f", clock) + " " + statusData);
            System.out.println("[EFTrans] Received fStatusExt #" + reportsReceived
                    + " at t=" + String.format("%.2f", clock) + ": " + statusData);
        }
        display.PhaseString(getName() + ": " + phase);
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
        display.PhaseString(getName() + ": " + phase);
    }

    public double getClock() { return clock; }
    public double getObservationTime() { return observationTime; }
    public int getReportsReceived() { return reportsReceived; }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
}
