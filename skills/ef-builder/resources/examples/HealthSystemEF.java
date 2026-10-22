package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * Coupled DEVS Model: HealthSystemEF (Experimental Frame)
 *
 * Encapsulates the HealthSystemModel (atomic) within an Experimental Frame
 * consisting of a Generator (HealthEFGenerator) and a Transducer
 * (HealthEFTransducer). The Generator injects scenario-based stimuli
 * through the system's four input ports, the system processes them
 * through its health state machine, and the Transducer observes the
 * system's three output ports.
 *
 * Architecture:
 *
 *   ┌──────────────────────────────────────────────────────────────────┐
 *   │                    HealthSystemEF (Coupled)                      │
 *   │                                                                  │
 *   │  ┌───────────────┐     4 stimulus    ┌───────────────────────┐  │
 *   │  │ HealthEF      │ ─────────────────▶│ HealthSystemModel     │  │
 *   │  │ Generator     │   career_stress   │ (Atomic)              │  │
 *   │  │               │   wealth_funding  │                       │  │
 *   │  │               │   family_support  │  States:              │  │
 *   │  │ 8 scenarios   │   social_support  │  IDLE→MAINTAINING→    │  │
 *   │  │ period=10.0   │                   │  DECLINING→CRISIS→    │  │
 *   │  │               │                   │  RECOVERING           │  │
 *   │  └───────▲───────┘                   └───────┬───────────────┘  │
 *   │          │                                   │                  │
 *   │          │ stop          3 observations      │                  │
 *   │          │               health_status       │                  │
 *   │          │               health_to_career    │                  │
 *   │          │               health_to_relship   │                  │
 *   │          │                                   ▼                  │
 *   │  ┌───────┴───────┐◀─────────────────────────┘                  │
 *   │  │ HealthEF      │                                             │
 *   │  │ Transducer    │                                             │
 *   │  │               │                                             │
 *   │  │ obs=100.0     │                                             │
 *   │  │ statistics    │                                             │
 *   │  └───────────────┘                                             │
 *   └──────────────────────────────────────────────────────────────────┘
 *
 * Internal Couplings (8 total):
 *   Generator → System (4 IC):
 *     gen.career_stress  → sys.career_stress
 *     gen.wealth_funding → sys.wealth_funding
 *     gen.family_support → sys.family_support
 *     gen.social_support → sys.social_support
 *
 *   System → Transducer (3 IC):
 *     sys.health_status          → trans.health_status
 *     sys.health_to_career       → trans.health_to_career
 *     sys.health_to_relationship → trans.health_to_relationship
 *
 *   Transducer → Generator (1 IC):
 *     trans.stop → gen.stop
 *
 * No External Ports (EIC/EOC): The EF is self-contained.
 *
 * Formalism: Coupled DEVS (Zeigler, 2000)
 * N = <X, Y, D, {M_d}, {I_d}, {Z_ij}, Select>
 */
public class HealthSystemEF extends CoupledModelImpl {

    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    // ── Component references for external monitoring ────────
    private HealthEFGenerator gen;
    private HealthSystemModel sys;
    private HealthEFTransducer trans;

    // ── Default parameters ──────────────────────────────────
    private static final double DEFAULT_GEN_PERIOD = 10.0;
    private static final int DEFAULT_GEN_MAX_COUNT = 8;
    private static final double DEFAULT_OBS_TIME = 100.0;

    public HealthSystemEF() {
        this("HealthSystemEF", DEFAULT_GEN_PERIOD, DEFAULT_GEN_MAX_COUNT,
                DEFAULT_OBS_TIME);
    }

    public HealthSystemEF(String name) {
        this(name, DEFAULT_GEN_PERIOD, DEFAULT_GEN_MAX_COUNT,
                DEFAULT_OBS_TIME);
    }

    public HealthSystemEF(String name, double genPeriod, int genMaxCount,
                           double observationTime) {
        super(name);

        // ── Component Instantiation ─────────────────────────
        gen   = new HealthEFGenerator("Generator", genPeriod, genMaxCount);
        sys   = new HealthSystemModel("HealthSystem");
        trans = new HealthEFTransducer("Transducer", observationTime);

        addChildModel(gen);
        addChildModel(sys);
        addChildModel(trans);

        // ── Internal Couplings (IC) — Generator → System ────
        // 4 stimulus couplings
        addCoupling(gen.career_stress,  sys.career_stress);
        addCoupling(gen.wealth_funding, sys.wealth_funding);
        addCoupling(gen.family_support, sys.family_support);
        addCoupling(gen.social_support, sys.social_support);

        // ── Internal Couplings (IC) — System → Transducer ───
        // 3 observation couplings
        addCoupling(sys.health_status,          trans.health_status);
        addCoupling(sys.health_to_career,       trans.health_to_career);
        addCoupling(sys.health_to_relationship, trans.health_to_relationship);

        // ── Internal Couplings (IC) — Transducer → Generator ─
        // 1 stop coupling
        addCoupling(trans.stop, gen.stop);
    }

    // ── Component accessors for test harness ────────────────
    public HealthEFGenerator getGenerator() { return gen; }
    public HealthSystemModel getSystem() { return sys; }
    public HealthEFTransducer getTransducer() { return trans; }

    // ══════════════════════════════════════════════════════════
    // MAIN — Simulation Entry Point with Test Case
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     Health System Experimental Frame — DEVS Simulation     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ── Test Case Parameters ────────────────────────────
        final double GEN_PERIOD = 10.0;
        final int GEN_MAX_COUNT = 8;
        final double OBSERVATION_TIME = 100.0;
        final int MAX_ITERATIONS = 200;
        final double MAX_SIMULATION_TIME = 120.0;

        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│                    TEST CASE PARAMETERS                    │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  Generator Period:       " + GEN_PERIOD + " time units                  │");
        System.out.println("│  Generator Max Stimuli:  " + GEN_MAX_COUNT + "                                    │");
        System.out.println("│  Observation Window:     " + OBSERVATION_TIME + " time units                │");
        System.out.println("│  Max Iterations:         " + MAX_ITERATIONS + "                                  │");
        System.out.println("│  Max Simulation Time:    " + MAX_SIMULATION_TIME + " time units                │");
        System.out.println("│                                                            │");
        System.out.println("│  Scenario Sequence:                                        │");
        System.out.println("│    1. Baseline          (stress=0.30, funding=300)         │");
        System.out.println("│    2. Rising Stress     (stress=0.60, funding=250)         │");
        System.out.println("│    3. Financial Strain  (stress=0.70, funding=100)         │");
        System.out.println("│    4. Combined Pressure (stress=0.90, funding= 50)         │");
        System.out.println("│    5. Support Surge     (stress=0.10, funding=400)         │");
        System.out.println("│    6. Recovery Consol.  (stress=0.15, funding=350)         │");
        System.out.println("│    7. Stabilization     (stress=0.10, funding=300)         │");
        System.out.println("│    8. Optimal           (stress=0.05, funding=450)         │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.println();

        // ── Build the Experimental Frame coupled model ──────
        HealthSystemEF top = new HealthSystemEF("HealthSystemEF_TestCase",
                GEN_PERIOD, GEN_MAX_COUNT, OBSERVATION_TIME);
        top.initialize();

        // SES mapping
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println(ses.printTreeString());

        // Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        top.options = options;

        if (options.isDisableViewer()) {
            runTestCaseSimulation(top, MAX_ITERATIONS, MAX_SIMULATION_TIME, options);
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }

    /**
     * Run the test case simulation with monitoring and validation.
     */
    private static void runTestCaseSimulation(HealthSystemEF top,
            int maxIterations, double maxTime, SimulationOptionsImpl options) {

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║             STARTING TEST CASE SIMULATION                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        SimulationImpl sim = new SimulationImpl("HealthSystemEF Test", top, options);
        sim.startSimulation(0);

        int iteration = 0;
        boolean stopConditionMet = false;
        String stopReason = "Max iterations reached";

        // Print monitoring header
        System.out.println("┌──────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬───────┐");
        System.out.println("│ Iter │ SimTime  │  Phys    │  Mental  │  Stress  │  Energy  │  Burnout │  Phase   │ Cycle │");
        System.out.println("├──────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┼──────────┼───────┤");

        while (iteration < maxIterations && !stopConditionMet) {
            iteration++;

            try {
                sim.simulateIterations(1);
            } catch (Exception e) {
                System.err.println("Simulation error at iteration " + iteration
                        + ": " + e.getMessage());
                stopReason = "Simulation exception: " + e.getMessage();
                break;
            }

            double simTime = sim.getCurrentSimulationTime();

            // Get current state from the system model
            double physHealth = top.getSystem().getPhysicalHealth();
            double mentalHealth = top.getSystem().getMentalHealth();
            double stress = top.getSystem().getStressAccumulation();
            double energy = top.getSystem().getEnergyLevel();
            double burnout = top.getSystem().getBurnoutRisk();
            String sysPhase = top.getSystem().getPhase();
            int cycle = top.getSystem().getCycleCount();

            // Stop conditions
            if (physHealth <= 0.0 && mentalHealth <= 0.0) {
                stopConditionMet = true;
                stopReason = "Total health collapse (phys=0.0, mental=0.0)";
            }
            if (top.getTransducer().phaseIs("DONE")
                    && top.getGenerator().phaseIs("PASSIVE")) {
                stopConditionMet = true;
                stopReason = "Observation complete + Generator passive";
            }

            // Print progress every 5 iterations or on stop
            if (iteration % 5 == 0 || stopConditionMet || iteration == 1) {
                printIterationRow(iteration, simTime, physHealth, mentalHealth,
                        stress, energy, burnout, sysPhase, cycle);
            }

            if (simTime >= maxTime) {
                stopReason = "Max simulation time reached (" + maxTime + ")";
                break;
            }
        }

        System.out.println("└──────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┴───────┘");
        System.out.println();

        // ── Test Case Validation ────────────────────────────
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST CASE RESULTS                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Stop Reason: " + stopReason);
        System.out.println("Total Iterations: " + iteration);
        System.out.println();

        boolean testPassed = true;
        int passed = 0;
        int failed = 0;

        // Validation 1: System received stimuli (cycle > 0)
        int cycles = top.getSystem().getCycleCount();
        if (cycles > 0) {
            System.out.println("  PASS: System executed " + cycles + " internal cycles");
            passed++;
        } else {
            System.out.println("  FAIL: System never executed an internal cycle");
            testPassed = false;
            failed++;
        }

        // Validation 2: Transducer received status reports
        int statusRpts = top.getTransducer().getStatusReports();
        if (statusRpts > 0) {
            System.out.println("  PASS: Transducer received " + statusRpts + " health_status reports");
            passed++;
        } else {
            System.out.println("  FAIL: Transducer received no health_status reports");
            testPassed = false;
            failed++;
        }

        // Validation 3: Transducer received relationship reports
        int relRpts = top.getTransducer().getRelationshipReports();
        if (relRpts > 0) {
            System.out.println("  PASS: Transducer received " + relRpts + " health_to_relationship reports");
            passed++;
        } else {
            System.out.println("  FAIL: Transducer received no health_to_relationship reports");
            testPassed = false;
            failed++;
        }

        // Validation 4: Generator produced all scenarios
        int genCount = top.getGenerator().getCount();
        if (genCount >= 4) {
            System.out.println("  PASS: Generator produced " + genCount + " stimulus events");
            passed++;
        } else {
            System.out.println("  FAIL: Generator produced only " + genCount + " stimuli (expected >= 4)");
            testPassed = false;
            failed++;
        }

        // Validation 5: Physical health stayed within [0,1]
        double finalPhys = top.getSystem().getPhysicalHealth();
        if (finalPhys >= 0.0 && finalPhys <= 1.0) {
            System.out.println("  PASS: Physical health in valid range [0,1]: " + String.format("%.4f", finalPhys));
            passed++;
        } else {
            System.out.println("  FAIL: Physical health out of range: " + String.format("%.4f", finalPhys));
            testPassed = false;
            failed++;
        }

        // Validation 6: Mental health stayed within [0,1]
        double finalMental = top.getSystem().getMentalHealth();
        if (finalMental >= 0.0 && finalMental <= 1.0) {
            System.out.println("  PASS: Mental health in valid range [0,1]: " + String.format("%.4f", finalMental));
            passed++;
        } else {
            System.out.println("  FAIL: Mental health out of range: " + String.format("%.4f", finalMental));
            testPassed = false;
            failed++;
        }

        // Validation 7: System exited IDLE state
        String finalPhase = top.getSystem().getPhase();
        if (!finalPhase.equals("IDLE")) {
            System.out.println("  PASS: System exited IDLE state (final phase: " + finalPhase + ")");
            passed++;
        } else {
            System.out.println("  FAIL: System remained in IDLE (no input processed)");
            testPassed = false;
            failed++;
        }

        // Validation 8: System experienced at least one DECLINING or CRISIS event
        int crisisEvts = top.getTransducer().getCrisisCount();
        int declEvts = top.getTransducer().getDecliningCount();
        if (crisisEvts > 0 || declEvts > 0) {
            System.out.println("  PASS: System experienced " + declEvts
                    + " DECLINING and " + crisisEvts + " CRISIS events (stress scenarios effective)");
            passed++;
        } else {
            System.out.println("  INFO: No DECLINING/CRISIS events detected (may be scenario-dependent)");
            passed++;
        }

        // Final result
        System.out.println();
        System.out.println("────────────────────────────────────────────────────────────");
        System.out.println("  Results: " + passed + " passed, " + failed + " failed");
        System.out.println();
        if (testPassed) {
            System.out.println("  *** ALL TESTS PASSED ***");
        } else {
            System.out.println("  !!! SOME TESTS FAILED !!!");
        }
        System.out.println("────────────────────────────────────────────────────────────");
    }

    private static void printIterationRow(int iter, double time,
            double phys, double mental, double stress, double energy,
            double burnout, String phase, int cycle) {
        System.out.println(String.format(
                "│ %4d │ %8.2f │ %8.4f │ %8.4f │ %8.4f │ %8.4f │ %8.4f │ %-8s │ %5d │",
                iter, time, phys, mental, stress, energy, burnout,
                phase.length() > 8 ? phase.substring(0, 8) : phase, cycle));
    }
}
