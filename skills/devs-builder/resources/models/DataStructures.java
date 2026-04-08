package Models.java;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DataStructures {

	// Provenance metadata for audit trails
	public class Provenance implements Serializable {
	    public String sourceSystem;
	    public String siteId;
	    public Instant extractedAt;
	    public String cohortId;
	    public Map<String, String> annotations;
	}

	// Raw pediatric EHR record (pre-harmonization)
	public class EHRRecord implements Serializable {
	    public String patientId;
	    public int ageMonths;
	    public double weightKg;
	    public double heightCm;
	    public Map<String, Double> labs;               // e.g., creatinine, ALT, AST
	    public List<String> conditions;                 // chronic conditions / ICD codes
	    public List<String> medications;                // active meds, strings or RXNorm
	    public Provenance provenance;
	}

	// Harmonized pediatric physiology (digital twin state)
	public class TwinState implements Serializable {
	    public String patientId;
	    public int ageMonths;
	    public double weightKg;
	    public double heightCm;
	    public String maturationClass;                  // e.g., neonate/infant/toddler/child/adolescent
	    public double gfrEstimateMlMin173;             // renal function normalized
	    public double liverFunctionScore;              // simplified composite
	    public double growthZScore;                     // CDC/WHO curve-based
	    public Map<String, Object> qualityChecks;       // completeness, outlier flags
	    public Provenance provenance;                   // carries upstream provenance
	}

	// Adult exposure targets to match (e.g., AUC/Cmax ranges)
	public class AdultExposureTarget implements Serializable {
	    public String drugName;
	    public double targetAUC;        // area under curve
	    public double targetCmax;
	    public String exposureUnits;    // e.g., mg�h/L
	    public String reference;        // publication or label source
	}

	// PK/PD parameter set calibrated to local pediatric data
	public class PKPDParams implements Serializable {
	    public String drugName;
	    public double clearanceLPerHr;
	    public double volumeL;
	    public double kaPerHr;          // absorption rate
	    public double hillCoeff;        // PD sigmoidicity (optional)
	    public Map<String, Object> fitDiagnostics;  // convergence, residuals
	    public Provenance provenance;   // calibration data lineage
	    public PKPDParams() {}
	}

	// Dose recommendation for a twin or cohort
	public class DoseRecommendation implements Serializable {
	    public String drugName;
	    public double doseMg;
	    public String route;            // e.g., IV, oral
	    public String frequency;        // e.g., q12h
	    public String justification;    // exposure-matching rationale
	    public Map<String, Object> constraints; // safety bounds applied
	}

	// Trial configuration: eligibility, stratification, visits
	public class TrialConfig implements Serializable {
	    public String trialId;
	    public String drugName;
	    public Map<String, Object> eligibility;     // rules (e.g., age ranges, GFR thresholds)
	    public Map<String, Object> stratification;  // strata (e.g., age bands, organ function level)
	    public List<Map<String, Object>> visitSchedule; // visit timing + labs
	    public Map<String, Object> ops;             // screen fail rates, site capacity
	    public Provenance provenance;
	}

	// Forecast of enrollment and exposure distributions
	public class EnrollmentForecast implements Serializable {
	    public String trialId;
	    public int projectedEnrolled;
	    public Map<String, Object> timelines;       // accrual curves
	    public Map<String, Object> exposureStats;   // AUC/Cmax distributions by stratum
	    public Map<String, Object> screenFailStats; // reasons and rates
	    public Provenance provenance;
	}

	// Safety projection summary
	public class SafetyOutcome implements Serializable {
	    public String trialId;
	    public Map<String, Object> adverseEventRisk;   // by grade, mechanism
	    public Map<String, Object> thresholdBreaches;  // lab/PK thresholds exceeded
	    public Map<String, Object> mitigation;         // adjusted visits, monitoring
	    public Provenance provenance;
	}
	
	public static void main(String[] args){
		DataStructures m = new DataStructures();
        PKPDParams p =  m.new PKPDParams();
	}
}
