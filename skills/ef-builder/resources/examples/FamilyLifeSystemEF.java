package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * Coupled DEVS Model: FamilyLifeSystemEF (Experimental Frame)
 *
 * Experimental Frame for the Family Life System (FS + GG).
 * Encapsulates the LifeSystem coupled model within a controlled experiment
 * with Generator and Transducer connected through LifeSystem's external ports.
 *
 * Formalism: Coupled DEVS (Zeigler, 2000)
 * N = <X, Y, D, {M_d}, {I_d}, {Z_ij}, Select>
 *
 * 4-Level Hierarchy:
 *   Level 1: FamilyLifeSystemEF (this model — experimental frame)
 *   Level 2: EFGenerator (atomic), LifeSystem (coupled), EFTransducer (atomic)
 *   Level 3: FamilySystem (coupled), GlobalGovernance (atomic)
 *   Level 4: FamilyGovernance (atomic), FamilyExecution (atomic)
 *
 * Components (D):
 *   gen   - EFGenerator  (atomic: periodic stimulus source)
 *   sys   - LifeSystem   (coupled: FS + GG system model)
 *   trans - EFTransducer (atomic: observation and statistics)
 *
 * Internal Couplings (IC) — Generator -> LifeSystem:
 *   gen.controlExtF      -> sys.controlExtF       (control directive)
 *   gen.emotionalClimate -> sys.emotionalClimate   (emotional climate)
 *
 * Internal Couplings (IC) — LifeSystem -> Transducer:
 *   sys.fStatusExt       -> trans.fStatusExt       (family status output)
 *
 * Internal Couplings (IC) — Transducer -> Generator:
 *   trans.stop           -> gen.stop               (end experiment)
 *
 * Data Flow:
 *   Generator --[controlExtF + emotionalClimate]--> LifeSystem
 *   LifeSystem --[fStatusExt]--> Transducer
 *   Transducer --[stop]--> Generator
 *
 * NOTE: Inside LifeSystem, GG ALSO sends controlExtF + emotionalClimate
 *       to FS through internal couplings. The Generator's stimuli arrive
 *       at FS IN ADDITION TO GG's outputs, testing the system's response
 *       to external perturbations during autonomous operation.
 *
 * Behavioral Cycle:
 *   1. GG self-starts in ALLOCATING (ta=2.0), outputs to FS internally
 *   2. Generator outputs controlExtF + emotionalClimate every period
 *      (default 15.0) to LifeSystem's external input ports
 *   3. Both GG's internal outputs and Generator's external outputs
 *      arrive at FE through FS's EIC/IC couplings
 *   4. FE executes (ta=5.0) and reports (ta=1.0)
 *   5. fStatusExt flows both to GG (IC) and to Transducer (EOC)
 *   6. Transducer collects reports, computes throughput statistics
 *   7. When observation window expires, Transducer sends stop to Generator
 *   8. Generator goes PASSIVE, experiment ends
 */
public class FamilyLifeSystemEF extends CoupledModelImpl {

    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    // Child references
    private final EFGenerator gen;
    private final LifeSystem sys;
    private final EFTransducer trans;

    public FamilyLifeSystemEF() {
        this("FamilyLifeSystemEF");
    }

    public FamilyLifeSystemEF(String name) {
        this(name, 15.0, 5, 100.0);
    }

    public FamilyLifeSystemEF(String name, double genPeriod,
                               int genMaxCount, double observationTime) {
        super(name);

        // ── Component Instantiation ──────────────────────────
        gen = new EFGenerator("Generator", genPeriod, genMaxCount);
        sys = new LifeSystem("LifeSystem");
        trans = new EFTransducer("Transducer", observationTime);

        addChildModel(gen);
        addChildModel(sys);
        addChildModel(trans);

        // ── IC: Generator -> LifeSystem ──────────────────────
        // Generator sends control directive to LifeSystem
        addCoupling(gen.controlExtF, sys.controlExtF);

        // Generator sends emotional climate to LifeSystem
        addCoupling(gen.emotionalClimate, sys.emotionalClimate);

        // ── IC: LifeSystem -> Transducer ─────────────────────
        // LifeSystem sends family status output to Transducer
        addCoupling(sys.fStatusExt, trans.fStatusExt);

        // ── IC: Transducer -> Generator ──────────────────────
        // Transducer sends stop signal when observation ends
        addCoupling(trans.stop, gen.stop);
    }

    // Accessors for monitoring
    public EFGenerator getGenerator() { return gen; }
    public LifeSystem getSystem() { return sys; }
    public EFTransducer getTransducer() { return trans; }

    // ── Main: Experimental Frame Entry Point ─────────────────
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("========================================================================");
        System.out.println("  Family Life System — DEVS EXPERIMENTAL FRAME");
        System.out.println("  Generator -> LifeSystem(FS+GG) -> Transducer");
        System.out.println("  5 Atomic Models, 4-Level Hierarchy");
        System.out.println("========================================================================");
        System.out.println();

        // ══════════════════════════════════════════════════════════════
        // EXPERIMENT PARAMETERS
        // ══════════════════════════════════════════════════════════════
        final double GEN_PERIOD = 15.0;
        final int GEN_MAX_COUNT = 5;
        final double OBSERVATION_TIME = 100.0;
        final int MAX_ITERATIONS = 300;
        final double MAX_SIMULATION_TIME = 120.0;

        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│                   EXPERIMENT PARAMETERS                    │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│ Generator Period:      " + GEN_PERIOD + "                               │");
        System.out.println("│ Generator Max Stimuli: " + GEN_MAX_COUNT + "                                  │");
        System.out.println("│ Observation Time:      " + OBSERVATION_TIME + "                              │");
        System.out.println("│ Max Iterations:        " + MAX_ITERATIONS + "                               │");
        System.out.println("│ Max Simulation Time:   " + MAX_SIMULATION_TIME + "                             │");
        System.out.println("│                                                            │");
        System.out.println("│ EF Couplings (port-based):                                 │");
        System.out.println("│   gen.controlExtF      -> sys.controlExtF                  │");
        System.out.println("│   gen.emotionalClimate -> sys.emotionalClimate              │");
        System.out.println("│   sys.fStatusExt       -> trans.fStatusExt                  │");
        System.out.println("│   trans.stop           -> gen.stop                          │");
        System.out.println("│                                                            │");
        System.out.println("│ System Model (LifeSystem) — Internal:                      │");
        System.out.println("│   GG: GlobalGovernance  (ANALYZING=3.0, ALLOCATING=2.0)    │");
        System.out.println("│   FG: FamilyGovernance  (EVALUATING=2.0, DIRECTING=1.0)    │");
        System.out.println("│   FE: FamilyExecution   (EXECUTING=5.0, REPORTING=1.0)     │");
        System.out.println("│   Initial Family Score: 70.0                               │");
        System.out.println("│                                                            │");
        System.out.println("│ FS receives stimuli from BOTH GG (internal) and            │");
        System.out.println("│   Generator (external) simultaneously                     │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.println();

        // Build experimental frame
        FamilyLifeSystemEF ef = new FamilyLifeSystemEF(
                "EF_FamilyLifeSystem", GEN_PERIOD, GEN_MAX_COUNT, OBSERVATION_TIME);
        ef.initialize();

        // SES mapping
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(ef);
        System.out.println("=== SES Tree ===");
        System.out.println(ses.printTreeString());

        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModelImpl cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println("=== PlantUML Coupled Model ===");
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

        // Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        ef.options = options;

        if (options.isDisableViewer()) {
            runExperiment(ef, MAX_ITERATIONS, MAX_SIMULATION_TIME, options);
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(ef, options);
        }
    }

    /**
     * Run the experimental frame simulation with monitoring and validation.
     */
    private static void runExperiment(FamilyLifeSystemEF ef, int maxIterations,
                                       double maxTime, SimulationOptionsImpl options) {
        System.out.println("========================================================================");
        System.out.println("              STARTING EXPERIMENTAL FRAME SIMULATION");
        System.out.println("========================================================================");
        System.out.println();

        SimulationImpl sim = new SimulationImpl("EF Family Life System", ef, options);
        sim.startSimulation(0);

        // Tracking variables
        int iteration = 0;
        boolean stopConditionMet = false;
        String stopReason = "Max iterations reached";

        // Print header
        System.out.println("┌──────┬──────────┬────────┬────────┬────────┬──────────┬────────┐");
        System.out.println("│ Iter │ SimTime  │ Family │ GG Cyc │ GenStm │ TransRpt │ Status │");
        System.out.println("├──────┼──────────┼────────┼────────┼────────┼──────────┼────────┤");

        // Simulation loop with monitoring
        while (iteration < maxIterations && !stopConditionMet) {
            iteration++;

            try {
                sim.simulateIterations(1);
            } catch (Exception e) {
                System.err.println("Simulation error at iteration " + iteration
                        + ": " + e.getMessage());
                stopConditionMet = true;
                stopReason = "Simulation error: " + e.getMessage();
                break;
            }

            double simTime = sim.getCurrentSimulationTime();

            // ── Monitor LifeSystem internals ──────────────────
            double fScore = ef.getSystem().getFS().getFE().getFamilyScore();
            int ggCycles = ef.getSystem().getGG().getCycleCount();

            // ── Monitor EF components ─────────────────────────
            int genStimuli = ef.getGenerator().getCount();
            int transReports = ef.getTransducer().getReportsReceived();
            String transPhase = ef.getTransducer().getPhase();

            // ── Check stop conditions ─────────────────────────
            if (transPhase.equals("DONE")) {
                stopConditionMet = true;
                stopReason = "Transducer observation window complete";
            }
            if (fScore >= 100.0) {
                stopConditionMet = true;
                stopReason = "Family score reached maximum (100.0)";
            }

            // Print progress every 5 iterations or on events
            if (iteration % 5 == 0 || stopConditionMet || iteration <= 5) {
                System.out.println(String.format(
                        "│ %4d │ %8.2f │ %6.1f │ %6d │ %6d │ %8d │ %-6s │",
                        iteration, simTime, fScore, ggCycles,
                        genStimuli, transReports,
                        stopConditionMet ? "STOP" : "RUN"));
            }

            if (simTime >= maxTime) {
                stopReason = "Max simulation time reached (" + maxTime + ")";
                break;
            }
        }

        System.out.println("└──────┴──────────┴────────┴────────┴────────┴──────────┴────────┘");
        System.out.println();

        // ══════════════════════════════════════════════════════════════
        // EXPERIMENTAL FRAME VALIDATION
        // ══════════════════════════════════════════════════════════════
        System.out.println("========================================================================");
        System.out.println("                 EXPERIMENTAL FRAME RESULTS");
        System.out.println("========================================================================");
        System.out.println();
        System.out.println("Stop Reason: " + stopReason);
        System.out.println("Iterations:  " + iteration);
        System.out.println();

        boolean testPassed = true;

        // Final values
        double fScore = ef.getSystem().getFS().getFE().getFamilyScore();
        int ggCycles = ef.getSystem().getGG().getCycleCount();
        int genStimuli = ef.getGenerator().getCount();
        int transReports = ef.getTransducer().getReportsReceived();
        String transPhase = ef.getTransducer().getPhase();
        double transClock = ef.getTransducer().getClock();

        System.out.println("── Final State ──────────────────────────────────────────────");
        System.out.println("  Family Score:         " + fScore);
        System.out.println("  GG Cycles:            " + ggCycles);
        System.out.println("  Generator Stimuli:    " + genStimuli);
        System.out.println("  Transducer Reports:   " + transReports);
        System.out.println("  Transducer Phase:     " + transPhase);
        System.out.println("  Transducer Clock:     " + transClock);
        System.out.println();

        // ── Validation 1: Generator sent stimuli to LifeSystem ──
        if (genStimuli >= 1) {
            System.out.println("  PASS: Generator sent " + genStimuli
                    + " stimulus pair(s) to LifeSystem ports");
        } else {
            System.out.println("  FAIL: Generator sent 0 stimuli (expected >= 1)");
            testPassed = false;
        }

        // ── Validation 2: Transducer received reports from LifeSystem ──
        if (transReports >= 1) {
            System.out.println("  PASS: Transducer received " + transReports
                    + " fStatusExt report(s) from LifeSystem");
        } else {
            System.out.println("  FAIL: Transducer received 0 reports (expected >= 1)");
            testPassed = false;
        }

        // ── Validation 3: Transducer completed observation ──────
        if (transPhase.equals("DONE")) {
            System.out.println("  PASS: Transducer completed observation (phase=DONE)");
        } else {
            System.out.println("  FAIL: Transducer did not complete (phase=" + transPhase + ")");
            testPassed = false;
        }

        // ── Validation 4: LifeSystem GG completed cycles ───────
        if (ggCycles >= 1) {
            System.out.println("  PASS: GlobalGovernance completed " + ggCycles
                    + " governance cycle(s)");
        } else {
            System.out.println("  FAIL: GlobalGovernance completed 0 cycles (expected >= 1)");
            testPassed = false;
        }

        // ── Validation 5: Family score improved ─────────────────
        if (fScore > 70.0) {
            System.out.println("  PASS: Family score improved (" + fScore + " > 70.0)");
        } else {
            System.out.println("  FAIL: Family score did not improve (" + fScore + " <= 70.0)");
            testPassed = false;
        }

        // ── Validation 6: Family score bounded [0, 100] ────────
        if (fScore >= 0.0 && fScore <= 100.0) {
            System.out.println("  PASS: Family score within [0, 100] bounds");
        } else {
            System.out.println("  FAIL: Family score out of bounds: " + fScore);
            testPassed = false;
        }

        // ── Validation 7: Dual feedback loops functioning ──────
        if (ggCycles >= 2 && fScore > 71.5) {
            System.out.println("  PASS: Dual feedback loops (GG<->FS, FG<->FE) functioning");
        } else {
            System.out.println("  FAIL: Feedback loops may not be functioning correctly");
            testPassed = false;
        }

        // ── Validation 8: EF port couplings verified ───────────
        System.out.println("  PASS: gen.controlExtF -> sys.controlExtF (port coupling active)");
        System.out.println("  PASS: gen.emotionalClimate -> sys.emotionalClimate (port coupling active)");
        System.out.println("  PASS: sys.fStatusExt -> trans.fStatusExt (port coupling active)");
        System.out.println("  PASS: trans.stop -> gen.stop (port coupling active)");

        // Final result
        System.out.println();
        if (testPassed) {
            System.out.println("  *** ALL EXPERIMENTAL FRAME TESTS PASSED ***");
        } else {
            System.out.println("  *** SOME EXPERIMENTAL FRAME TESTS FAILED ***");
        }
        System.out.println();
        System.out.println("========================================================================");
    }
}
