package Models.java;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import Models.java.DataStructures.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * PediatricDigitalTwinAtomic
 *
 * Implements the abstracted Pediatric Digital Twin pipeline described in the proposal:
 *  - Thrust 1: Harmonization and physiological state estimation from pediatric EHR RWD
 *  - Thrust 2: Pediatric PK/PD dose computation to achieve adult-equivalent exposure
 *  - Thrust 3: In-silico trial simulation for planning/forecasting
 *
 * Notes:
 *  - Ports accept/emit Serializable payloads (e.g., HashMap<String,Object>) for easy integration
 *    without introducing new message classes.
 *  - This atomic model extends PhaseAtomic (provided in Models.java) and uses its phase/sigma helpers.
 *  - No external DEVS libraries are referenced; only the provided MS4 DEVS core is used.
 *
 * Inputs (Serializable Maps expected):
 *  - inEHR:          { patientId, ageMonths, weightKg, heightCm, sex, labs:{...}, comorbidities:[...], meds:[...], renalGfrMlMin1p73, hepaticAlt, ... }
 *  - inDoseRequest:  { drugName, adultAUCTargetMgPerLh (optional), adultDoseMg (optional), adultCL_LperHr (optional),
 *                      safetyMaxMgPerKg (optional), safetyMaxTotalMg (optional) }
 *  - inTrialCfg:     { trialName, doseSchedule:{mg:..., intervalH:...}, nVirtual:int (optional), include/exclude criteria (optional) }
 *
 * Outputs (Serializable Maps):
 *  - outTwinState:        { patientId, ageMonths, weightKg, heightCm, bmi, gfrAdj, hepaticAdj, cl_LperHr, v_L, maturationFactor, notes }
 *  - outDoseRecommendation:{ drugName, recommendedDoseMg, recommendedDoseMgPerKg, capped:Boolean, rationale, exposure:{aucTarget, aucPred} }
 *  - outTrialOutcome:     { trialName, n, accrualForecast, responseRateEst, aeRateEst, arms:[...], notes }
 *  - outLog:              { phase, msg, t }
 */
public class PediatricDigitalTwinAtomic extends PhaseAtomic {

    // Ports
    private Port<Serializable> inEHR;
    private Port<Serializable> inDoseRequest;
    private Port<Serializable> inTrialCfg;

    private Port<Serializable> outTwinState;
    private Port<Serializable> outDoseRecommendation;
    private Port<Serializable> outTrialOutcome;
    private Port<Serializable> outLog;

    // Internal state
    private Map<String, Object> latestEHR = null;
    private Map<String, Object> twinState = null;
    private Map<String, Object> lastDoseReq = null;
    private Map<String, Object> lastDoseRec = null;
    private Map<String, Object> lastTrialCfg = null;
    private Map<String, Object> lastTrialOutcome = null;

    private final Random rng = new Random(42);

    public PediatricDigitalTwinAtomic() {
        this("PediatricDigitalTwinAtomic");
    }

    public PediatricDigitalTwinAtomic(String name) {
        super(name);

        // Define ports (Serializable payloads)
        inEHR = this.addInputPort("inEHR", Serializable.class);
        inDoseRequest = this.addInputPort("inDoseRequest", Serializable.class);
        inTrialCfg = this.addInputPort("inTrialCfg", Serializable.class);

        outTwinState = this.addOutputPort("outTwinState", Serializable.class);
        outDoseRecommendation = this.addOutputPort("outDoseRecommendation", Serializable.class);
        outTrialOutcome = this.addOutputPort("outTrialOutcome", Serializable.class);
        outLog = this.addOutputPort("outLog", Serializable.class);
    }

    public static void main(String[] args) {
        // PediatricDigitalTwinAtomic model = new PediatricDigitalTwinAtomic();
        // model = model;
        System.out.println("Hello");
    }

    @Override
    public void initialize() {
        super.initialize();
        passivateIn("idle");
    }

    @Override
    public Double getTimeAdvance() {
        return this.sigma;
    }

    @Override
    public void internalTransition() {
        // After emitting on any phase, return to idle
        if (!"idle".equals(this.phase)) {
            passivateIn("idle");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        // Handle events in priority order: EHR -> DoseRequest -> TrialCfg
        boolean handled = false;

        try {
            // 1) EHR -> update twin state
            ArrayList<Serializable> ehrMsgs = safeGetMessagesOn(input, inEHR);
            if (!handled && ehrMsgs != null && !ehrMsgs.isEmpty()) {
                Map<String, Object> ehr = asMap(ehrMsgs.get(0));
                this.latestEHR = ehr;
                this.twinState = buildTwinFromEHR(ehr);
                holdIn("emitTwin", 0.0);
                handled = true;
            }

            // 2) Dose request -> compute dose using current (or newly built) twin state
            ArrayList<Serializable> doseMsgs = safeGetMessagesOn(input, inDoseRequest);
            if (!handled && doseMsgs != null && !doseMsgs.isEmpty()) {
                Map<String, Object> req = asMap(doseMsgs.get(0));
                this.lastDoseReq = req;

                if (this.twinState == null && this.latestEHR != null) {
                    this.twinState = buildTwinFromEHR(this.latestEHR);
                }
                Map<String, Object> rec = computeDose(req, this.twinState);
                this.lastDoseRec = rec;

                holdIn("emitDose", 0.0);
                handled = true;
            }

            // 3) Trial config -> in silico trial
            ArrayList<Serializable> trialMsgs = safeGetMessagesOn(input, inTrialCfg);
            if (!handled && trialMsgs != null && !trialMsgs.isEmpty()) {
                Map<String, Object> cfg = asMap(trialMsgs.get(0));
                this.lastTrialCfg = cfg;

                if (this.twinState == null && this.latestEHR != null) {
                    this.twinState = buildTwinFromEHR(this.latestEHR);
                }
                Map<String, Object> outcome = simulateTrial(cfg, this.twinState, this.lastDoseRec);
                this.lastTrialOutcome = outcome;

                holdIn("emitTrial", 0.0);
                handled = true;
            }

            // If nothing recognized, remain idle
            if (!handled) {
                // no-op; remain in current phase
            }
        } catch (Exception ex) {
            // Emit a log message on error and return to idle
            this.lastTrialOutcome = null;
            this.lastDoseRec = null;
            holdIn("emitLogOnly", 0.0);
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        // default behavior: internal, then external(0,input)
        super.confluentTransition(input);
    }

    @Override
    public MessageBag getOutput() {
        MessageBagImpl out = new MessageBagImpl();

        long tnow = System.currentTimeMillis();

        if ("emitTwin".equals(this.phase) && this.twinState != null) {
            out.add(outTwinState, asSerializableCopy(this.twinState));
            out.add(outLog, logMsg("emitTwin", "TwinState emitted", tnow));
        } else if ("emitDose".equals(this.phase) && this.lastDoseRec != null) {
            out.add(outDoseRecommendation, asSerializableCopy(this.lastDoseRec));
            out.add(outLog, logMsg("emitDose", "Dose recommendation emitted", tnow));
        } else if ("emitTrial".equals(this.phase) && this.lastTrialOutcome != null) {
            out.add(outTrialOutcome, asSerializableCopy(this.lastTrialOutcome));
            out.add(outLog, logMsg("emitTrial", "Trial outcome emitted", tnow));
        } else if ("emitLogOnly".equals(this.phase)) {
            out.add(outLog, logMsg("error", "Processing error; check inputs", tnow));
        }

        return out;
    }

    // ---------- Helpers: Message handling ----------

    @SuppressWarnings("unchecked")
    private ArrayList<Serializable> safeGetMessagesOn(MessageBag bag, Port<? extends Serializable> p) {
        // if (bag == null || p == null) return null;

        // try {
        //     // Preferred API if available
        //     return (ArrayList<Serializable>) bag.getMessages(this);
        // } catch (Throwable t1) {
        //     // Fallback: some implementations might use a different method name
        //     try {
        //         return (ArrayList<Serializable>) bag.getMessages(p);
        //     } catch (Throwable t2) {
        //         return null;
        //     }
        // }

        if (bag.hasMessages(inEHR)) {
                ArrayList<Message<Serializable>> messageList =
                    inEHR.getMessages(bag);

                holdIn("", 50.0);
                // Fire state and port specific external transition functions
                //ID:EXT:waitForJob:inJob
                Object StoredJob = messageList.get(0).getData();
                holdIn("sendJob", 0.5);

                //ENDID
                // End external event code
            }
        return new ArrayList<Serializable>();
    }

    private Serializable logMsg(String phase, String msg, long t) {
        HashMap<String, Object> m = new HashMap<String, Object>();
        m.put("phase", phase);
        m.put("msg", msg);
        m.put("t", t);
        return m;
    }

    // ---------- Thrust 1: Harmonization + Physiology estimation ----------

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildTwinFromEHR(Map<String, Object> ehr) {
        HashMap<String, Object> twin = new HashMap<String, Object>();

        String patientId = str(ehr.get("patientId"), "unknown");
        double ageMonths = dbl(ehr.get("ageMonths"), 0.0);
        double weightKg = dbl(ehr.get("weightKg"), 10.0);
        double heightCm = dbl(ehr.get("heightCm"), 75.0);

        // Derived anthropometrics
        double heightM = Math.max(0.3, heightCm / 100.0);
        double bmi = weightKg / (heightM * heightM);

        // Organ function adjustments (very simplified placeholders)
        Double gfr = ehr.containsKey("renalGfrMlMin1p73") ? dbl(ehr.get("renalGfrMlMin1p73"), 90.0) : 90.0;
        double gfrAdj = clamp(gfr / 90.0, 0.25, 1.5); // relative to nominal 90
        Double alt = ehr.containsKey("hepaticAlt") ? dbl(ehr.get("hepaticAlt"), 25.0) : 25.0;
        double hepaticAdj = clamp(1.0 - Math.max(0, (alt - 40.0)) / 200.0, 0.5, 1.0); // crude

        // Maturation factor (0.2 at birth -> ~1 by ~6 years)
        double maturation = maturationFactor(ageMonths);

        // Very simplified PBPK-ish parameters
        double v_L = Math.max(0.5, 0.7 * weightKg); // apparent V
        double clStd_LperHr = 15.0; // adult nominal CL for reference drug, can be overridden per-drug upstream
        double allom = Math.pow(weightKg / 70.0, 0.75);
        double cl_LperHr = clStd_LperHr * allom * (0.2 + 0.8 * maturation) * gfrAdj * hepaticAdj;

        twin.put("patientId", patientId);
        twin.put("ageMonths", ageMonths);
        twin.put("weightKg", weightKg);
        twin.put("heightCm", heightCm);
        twin.put("bmi", bmi);
        twin.put("gfrAdj", gfrAdj);
        twin.put("hepaticAdj", hepaticAdj);
        twin.put("maturationFactor", maturation);
        twin.put("cl_LperHr", cl_LperHr);
        twin.put("v_L", v_L);
        twin.put("notes", "Harmonized from RWD; parameters simplified for demo");

        return twin;
    }

    private double maturationFactor(double ageMonths) {
        // Simple saturating function (0 months -> ~0; 72 months -> ~1.0)
        double m = ageMonths / 72.0;
        return clamp(m, 0.0, 1.0);
    }

    // ---------- Thrust 2: Dose computation for adult-equivalent exposure ----------

    private Map<String, Object> computeDose(Map<String, Object> doseReq, Map<String, Object> twin) {
        HashMap<String, Object> out = new HashMap<String, Object>();
        if (doseReq == null) doseReq = new HashMap<String, Object>();
        if (twin == null) twin = new HashMap<String, Object>();

        String drugName = str(doseReq.get("drugName"), "unknownDrug");

        double clPed = dbl(twin.get("cl_LperHr"), 10.0);
        double weightKg = dbl(twin.get("weightKg"), 10.0);

        // Determine adult AUC target
        Double aucTarget = doseReq.containsKey("adultAUCTargetMgPerLh")
                ? dbl(doseReq.get("adultAUCTargetMgPerLh"), 0.0)
                : null;

        if (aucTarget == null || aucTarget <= 0.0) {
            double adultDoseMg = dbl(doseReq.get("adultDoseMg"), 500.0);
            double adultCL = dbl(doseReq.get("adultCL_LperHr"), 15.0);
            aucTarget = adultDoseMg / Math.max(0.1, adultCL);
        }

        // Compute pediatric dose to match target exposure (AUC ~= Dose/CL)
        double doseMg = aucTarget * clPed;

        // Safety caps
        double mgPerKgCap = doseReq.containsKey("safetyMaxMgPerKg") ? dbl(doseReq.get("safetyMaxMgPerKg"), Double.POSITIVE_INFINITY) : Double.POSITIVE_INFINITY;
        double totalCap = doseReq.containsKey("safetyMaxTotalMg") ? dbl(doseReq.get("safetyMaxTotalMg"), Double.POSITIVE_INFINITY) : Double.POSITIVE_INFINITY;
        double cappedDose = doseMg;
        boolean capped = false;

        if (mgPerKgCap < Double.POSITIVE_INFINITY) {
            double byKg = mgPerKgCap * weightKg;
            if (cappedDose > byKg) {
                cappedDose = byKg;
                capped = true;
            }
        }
        if (totalCap < Double.POSITIVE_INFINITY && cappedDose > totalCap) {
            cappedDose = totalCap;
            capped = true;
        }

        double mgPerKg = cappedDose / Math.max(1e-6, weightKg);
        double aucPred = cappedDose / Math.max(0.1, clPed);

        out.put("drugName", drugName);
        out.put("recommendedDoseMg", round1(cappedDose));
        out.put("recommendedDoseMgPerKg", round2(mgPerKg));
        out.put("capped", capped);
        out.put("rationale", capped ? "Dose capped by safety threshold(s)" : "Dose matches adult-equivalent exposure");
        HashMap<String, Object> exposure = new HashMap<String, Object>();
        exposure.put("aucTargetMgPerLh", round2(aucTarget));
        exposure.put("aucPredMgPerLh", round2(aucPred));
        exposure.put("clPed_LperHr", round2(clPed));
        out.put("exposure", exposure);

        return out;
    }

    // ---------- Thrust 3: In-silico trial simulation (toy implementation) ----------

    private Map<String, Object> simulateTrial(Map<String, Object> trialCfg,
                                              Map<String, Object> twin,
                                              Map<String, Object> doseRec) {
        HashMap<String, Object> out = new HashMap<String, Object>();
        String trialName = str(trialCfg.get("trialName"), "trial");

        int n = (int) Math.max(10, Math.round(dbl(trialCfg.get("nVirtual"), 100.0)));
        double baseResp = 0.55; // placeholder base response probability
        double baseAE = 0.12;   // placeholder AE probability

        // Use predicted exposure to modulate response
        double cl = twin != null ? dbl(twin.get("cl_LperHr"), 10.0) : 10.0;
        double doseMg = 0.0;
        if (trialCfg.containsKey("doseSchedule") && trialCfg.get("doseSchedule") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> ds = (Map<String, Object>) trialCfg.get("doseSchedule");
            doseMg = dbl(ds.get("mg"), 0.0);
        } else if (doseRec != null) {
            doseMg = dbl(doseRec.get("recommendedDoseMg"), 0.0);
        }
        double exposure = doseMg / Math.max(0.1, cl); // mg*h/L (proxy)

        // Very simple exposure-response: p = logistic(a + b*exposure)
        double a = -0.2, b = 0.03;
        double pResp = clamp(sigmoid(a + b * exposure), 0.05, 0.95);
        // AE increases mildly with exposure
        double pAE = clamp(baseAE + 0.002 * exposure, 0.02, 0.35);

        // Simulate counts
        int respCount = binomial(n, pResp);
        int aeCount = binomial(n, pAE);

        HashMap<String, Object> accrual = new HashMap<String, Object>();
        accrual.put("n", n);
        accrual.put("expectedPerMonth", Math.max(5, Math.min(25, (int) (n / 6.0)))); // toy forecast

        out.put("trialName", trialName);
        out.put("n", n);
        out.put("accrualForecast", accrual);
        out.put("responseRateEst", round3(respCount / (double) n));
        out.put("aeRateEst", round3(aeCount / (double) n));
        out.put("exposureProxy", round2(exposure));
        out.put("notes", "Toy in-silico trial; for planning only");

        return out;
    }

    // ---------- Utility ----------

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return new HashMap<String, Object>();
    }

    private Serializable asSerializableCopy(Map<String, Object> m) {
        return new HashMap<String, Object>(m);
    }

    private String str(Object o, String dflt) {
        return o == null ? dflt : String.valueOf(o);
    }

    private double dbl(Object o, double dflt) {
        if (o == null) return dflt;
        try {
            if (o instanceof Number) return ((Number) o).doubleValue();
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return dflt;
        }
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private int binomial(int n, double p) {
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (rng.nextDouble() < p) k++;
        }
        return k;
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }
}