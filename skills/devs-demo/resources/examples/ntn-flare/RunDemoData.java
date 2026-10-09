package Models.java;

import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;
import com.ms4systems.devs.markov.ContinuousTimeMarkov;
import com.ms4systems.devs.morphism.DevsMorphismProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Writes demos/data/demo-data.json and demo-data.js for the NTN flare demo page.
 *
 * For every (EF instance, resolution, flare time): N untraced replications for the
 * statistics panel, then replication 0 again with a {@link DemoTraceRecorder} for the
 * animation. Same pruned frame, morphism, seeding and closure reset as
 * {@link RunMorphismQuality}; results stay in memory (no CSV). Replication r uses seed
 * baseSeed + r, so the traced run is the first sampled one, which main checks.
 *
 * args: [reps] [efs|all] [seed] [flares]   defaults: 200 all 2349991 inf,0.0001
 */
public class RunDemoData extends RunMorphismQuality {
	static final String DEMO = folder + "demos" + File.separator + "data" + File.separator;
	static final String JSON_DIR = folder + "src" + File.separator + "Models" + File.separator + "json" + File.separator;
	static final double[] DEMO_FLARES = { Double.POSITIVE_INFINITY, 0.0001 };

	boolean tracing;
	Map<Port, List<Port>> routes;		// built once per harness: the pruned graph is reused across runs
	DemoTraceRecorder recorder;
	Map<String, Object> initial;
	List<Map<String, Object>> steps;
	Double tReport, tStart, tFlareSeen;	// last run, Double.MAX_VALUE -> null
	Boolean success;
	int dropped, unexplained;

	public RunDemoData(String ef, String resolution) throws IOException {
		super(ef, resolution);
	}

	// After resetClosures, startSimulation and seeding, before the first event.
	@Override
	protected void afterStart() {
		if (!tracing) return;
		if (routes == null) routes = DemoTraceRecorder.flatten(model);
		recorder = new DemoTraceRecorder(routes, sim.getMaxSimulationTime());
		DemoTraceRecorder.attach(sim.getCoupledCoordinator(), model, recorder);
		initial = new LinkedHashMap<>();
		steps = new ArrayList<>();
		for (Atomic a : getAtomicModels(model))
			initial.put(a.getName(), DemoTraceRecorder.snapshot(a));
	}

	@Override
	protected void afterStep() {
		if (!tracing) return;
		double t = sim.getCurrentSimulationTime();
		Map<String, Object> step = recorder.drain(t);
		if (step != null && t <= sim.getMaxSimulationTime()) steps.add(step);
	}

	@Override
	public void getAndAnalyzeResults() {
		tReport = finite(getValue("TransdFlare", "TReport"));
		tStart = finite(getValue("TransdFlare", "TStart"));
		tFlareSeen = finite(getValue("TransdFlare", "TFlare"));
		success = (Boolean) getValue("TransdFlare", "Success");
		if (tracing) {
			dropped = recorder.dropped;
			unexplained = recorder.unexplained;
		}
	}

	static Double finite(Object o) {
		if (!(o instanceof Number)) return null;
		double d = ((Number) o).doubleValue();
		return d == Double.MAX_VALUE || Double.isInfinite(d) || Double.isNaN(d) ? null : d;
	}

	// ---- bundle sections -----------------------------------------------------

	static Map<String, Object> scenario(String ef, String res, double tFlare, RunDemoData sm, List<Double> delivered,
			int successes, int reps) {
		Map<String, Object> s = new LinkedHashMap<>();
		s.put("ef", ef);
		s.put("resolution", res);
		s.put("tFlare", Double.isInfinite(tFlare) ? null : tFlare);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("tReport", sm.tReport);
		result.put("success", sm.success);
		result.put("tStart", sm.tStart);
		result.put("tFlare", sm.tFlareSeen);
		result.put("dropped", sm.dropped);
		s.put("result", result);
		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("n", reps);
		stats.put("successes", successes);
		stats.put("tReport", delivered);
		double mean = Double.NaN, std = Double.NaN;
		if (!delivered.isEmpty()) {
			double sum = 0;
			for (double d : delivered) sum += d;
			mean = sum / delivered.size();
			double ss = 0;
			for (double d : delivered) ss += (d - mean) * (d - mean);
			std = delivered.size() > 1 ? Math.sqrt(ss / (delivered.size() - 1)) : 0;
		}
		stats.put("mean", mean);
		stats.put("std", std);
		s.put("stats", stats);
		Map<String, Object> trace = new LinkedHashMap<>();
		trace.put("initial", sm.initial);
		trace.put("steps", sm.steps);
		s.put("trace", trace);
		return s;
	}

	static double num(Map<String, Object> p, String key) {
		Object v = p.get(key);
		if (v instanceof Number) return ((Number) v).doubleValue();
		if (v == null) throw new IllegalArgumentException("missing parameter " + key);
		return Double.parseDouble(v.toString());
	}

	/** The list-of-single-entry-maps shape of json_parameters.json flattened into one map. */
	static Map<String, Object> params(List<?> list) {
		Map<String, Object> p = new LinkedHashMap<>();
		for (Object o : list)
			for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet())
				p.put(String.valueOf(e.getKey()),
						e.getValue() instanceof Number ? ((Number) e.getValue()).doubleValue() : e.getValue());
		return p;
	}

	static Map<String, Object> budget(Map<String, Object> p, double extraLossDb) {
		LinkBudget b = new LinkBudget(num(p, "Frequency"), num(p, "Bandwidth"), num(p, "System_Noise_Temperature"),
				num(p, "Packet_Size"), num(p, "Environment_Loss") + extraLossDb, num(p, "Spectral_Efficency"),
				num(p, "TX_Power"), num(p, "TX_Antenna"), num(p, "RX_Antenna"), num(p, "Path_Distance"));
		Map<String, Object> d = new LinkedHashMap<>();
		d.put("snrDb", b.snrDb());
		d.put("ber", b.ber());
		d.put("pConnect", b.pConnect());
		d.put("packetTime", b.packetTime());
		d.put("expectedLatency", b.expectedLatency());
		d.put("usable", b.pConnect() >= CellSAT.P_CONNECT_FLOOR);
		return d;
	}

	static List<Map<String, Object>> instances(double flareLossDb) throws IOException {
		List<Map<String, Object>> out = new ArrayList<>();
		List<?> arr = (List<?>) DevsMorphismProcessor
				.parseJson(DevsMorphismProcessor.readFileAsString(JSON_DIR + "json_parameters.json"));
		for (Object o : arr) {
			Map<?, ?> m = (Map<?, ?>) o;
			Map<String, Object> inst = new LinkedHashMap<>();
			inst.put("name", m.get("InstanceName"));
			Map<String, Object> base = params((List<?>) m.get("BaseModelParameters"));
			inst.put("base", base);
			inst.put("target", params((List<?>) m.get("TargetModelParameters")));
			inst.put("derived", budget(base, 0));
			inst.put("derivedFlare", budget(base, flareLossDb));
			out.add(inst);
		}
		return out;
	}

	/** Cost,PConnect,MeanLatency,AverageTReport,StdTReport rows of the Phase I aggregated CSVs. */
	static List<Map<String, Object>> csvRows(String file) {
		List<Map<String, Object>> rows = new ArrayList<>();
		Path p = Path.of(file);
		if (!Files.exists(p)) {
			System.err.println("RunDemoData: missing " + file);
			return rows;
		}
		try {
			List<String> lines = Files.readAllLines(p);
			for (int i = 1; i < lines.size(); i++) {
				String[] f = lines.get(i).trim().split(",");
				if (f.length < 5) continue;
				Map<String, Object> r = new LinkedHashMap<>();
				r.put("cost", Double.parseDouble(f[0]));
				r.put("pConnect", Double.parseDouble(f[1]));
				r.put("meanLatency", Double.parseDouble(f[2]));
				r.put("avg", Double.parseDouble(f[3]));
				r.put("std", Double.parseDouble(f[4]));
				rows.add(r);
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("RunDemoData: cannot read " + file + ": " + e);
		}
		return rows;
	}

	static String git(String... args) {
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add("git");
			cmd.addAll(Arrays.asList(args));
			Process p = new ProcessBuilder(cmd).directory(new File(folder)).redirectErrorStream(true).start();
			String out = new String(p.getInputStream().readAllBytes()).trim();
			return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0 && !out.isEmpty() ? out : "unknown";
		} catch (Exception e) {
			return "unknown";
		}
	}

	// ---- JSON --------------------------------------------------------------

	static void json(Object v, StringBuilder sb) {
		if (v == null) {
			sb.append("null");
		} else if (v instanceof String) {
			quote((String) v, sb);
		} else if (v instanceof Boolean) {
			sb.append(v);
		} else if (v instanceof Double || v instanceof Float) {
			double d = ((Number) v).doubleValue();
			sb.append(Double.isFinite(d) ? Double.toString(d) : "null");
		} else if (v instanceof Number) {
			sb.append(v);
		} else if (v instanceof Map) {
			sb.append('{');
			String sep = "";
			for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
				sb.append(sep);
				quote(String.valueOf(e.getKey()), sb);
				sb.append(':');
				json(e.getValue(), sb);
				sep = ",";
			}
			sb.append('}');
		} else if (v instanceof Iterable) {
			sb.append('[');
			String sep = "";
			for (Object o : (Iterable<?>) v) {
				sb.append(sep);
				json(o, sb);
				sep = ",";
			}
			sb.append(']');
		} else {
			quote(v.toString(), sb);
		}
	}

	static void quote(String s, StringBuilder sb) {
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"': sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			case '\b': sb.append("\\b"); break;
			case '\f': sb.append("\\f"); break;
			default:
				if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
				else sb.append(c);
			}
		}
		sb.append('"');
	}

	// ---- main ----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		int reps = args.length > 0 ? Integer.parseInt(args[0]) : 200;
		List<String> efs = new ArrayList<>();
		if (args.length > 1 && !args[1].equalsIgnoreCase("all"))
			efs.addAll(Arrays.asList(args[1].split(",")));
		else
			for (Object ef : DevsMorphismProcessor.getExperimentalFrames())
				efs.add((String) ef);
		long baseSeed = args.length > 2 ? Long.parseLong(args[2]) : ContinuousTimeMarkov.Seed;
		double[] flares = args.length > 3 ? parseFlares(args[3]) : DEMO_FLARES;

		double flareLossDb = new CellSAT().getFlareLoss_dB();
		double degradation = new SatCTM().getLatencyDegredationFactor();
		Map<String, Object> topology = new LinkedHashMap<>();
		List<Map<String, Object>> scenarios = new ArrayList<>();
		double maxSimTime = Double.NaN;
		boolean ok = true;
		long t0 = System.currentTimeMillis();
		System.out.printf("%-10s %-7s %-7s %-14s %-5s %5s %5s %5s %-12s %-12s %s%n", "EF", "res", "flare", "rep0 TReport",
				"ok", "steps", "N", "succ", "mean", "std", "dropped/unexplained");
		for (String ef : efs) {
			for (String resolution : RESOLUTIONS) {
				RunDemoData sm = new RunDemoData(ef, resolution);
				for (double tFlare : flares) {
					List<Double> delivered = new ArrayList<>();
					int successes = 0;
					Double rep0T = null;
					Boolean rep0S = null;
					for (int rep = 0; rep < reps; rep++) {
						sm.run(tFlare, rep, baseSeed + rep);
						if (Boolean.TRUE.equals(sm.success) && sm.tReport != null) {
							successes++;
							delivered.add(sm.tReport);
						}
						if (rep == 0) {
							rep0T = sm.tReport;
							rep0S = sm.success;
						}
					}
					Collections.sort(delivered);
					sm.tracing = true;
					sm.run(tFlare, 0, baseSeed);
					sm.tracing = false;
					maxSimTime = sm.sim.getMaxSimulationTime();
					boolean same = Objects.equals(sm.tReport, rep0T) && Objects.equals(sm.success, rep0S);
					if (!same) {
						ok = false;
						System.err.println("traced run differs from replication 0: " + ef + "/" + resolution + "/" + tFlare
								+ " (" + sm.tReport + "," + sm.success + " vs " + rep0T + "," + rep0S + ")");
					}
					if (sm.unexplained > 0) ok = false;
					Map<String, Object> sc = scenario(ef, resolution, tFlare, sm, delivered, successes, reps);
					scenarios.add(sc);
					@SuppressWarnings("unchecked")
					Map<String, Object> st = (Map<String, Object>) sc.get("stats");
					System.out.printf("%-10s %-7s %-7s %-14s %-5s %5d %5d %5d %-12s %-12s %d/%d%n", ef, resolution,
							Double.isInfinite(tFlare) ? "none" : "" + tFlare, "" + sm.tReport, same ? "ok" : "DIFF",
							sm.steps.size(), reps, successes, fmt(st.get("mean")), fmt(st.get("std")), sm.dropped,
							sm.unexplained);
				}
				if (!topology.containsKey(resolution)) {
					Map<String, Object> top = new LinkedHashMap<>();
					top.put("models", DemoTraceRecorder.models(sm.model));
					List<Map<String, Object>> routes = DemoTraceRecorder.routesJson(sm.routes);
					top.put("routes", routes);
					topology.put(resolution, top);
					System.out.println(resolution + ": " + routes.size() + " routes, "
							+ ((List<?>) top.get("models")).size() + " leaf models");
					if (routes.size() != 24) {
						ok = false;
						System.err.println("expected 24 flattened routes for " + resolution + ", got " + routes.size());
						for (Map<String, Object> r : routes) System.err.println("  " + r);
					}
				}
			}
		}

		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("generated", Instant.now().toString());
		meta.put("commit", git("rev-parse", "--short", "HEAD"));
		meta.put("branch", git("rev-parse", "--abbrev-ref", "HEAD"));
		meta.put("reps", reps);
		meta.put("seed", baseSeed);
		meta.put("maxSimTime", maxSimTime);
		List<Object> flareTimes = new ArrayList<>();
		for (double f : flares) flareTimes.add(Double.isInfinite(f) ? null : f);
		meta.put("flareTimes", flareTimes);
		meta.put("flareLossDb", flareLossDb);
		meta.put("latencyDegradationFactor", degradation);
		meta.put("pConnectFloor", CellSAT.P_CONNECT_FLOOR);

		Map<String, Object> timeVsCost = new LinkedHashMap<>();
		timeVsCost.put("flare", csvRows(foldertxt + "AggregatedData.csv"));
		timeVsCost.put("noFlare", csvRows(foldertxt + "AggregatedData_noFlare.csv"));
		Map<String, Object> legacy = new LinkedHashMap<>();
		legacy.put("timeVsCost", timeVsCost);

		Map<String, Object> bundle = new LinkedHashMap<>();
		bundle.put("meta", meta);
		bundle.put("instances", instances(flareLossDb));
		bundle.put("topology", topology);
		bundle.put("scenarios", scenarios);
		bundle.put("legacy", legacy);

		StringBuilder sb = new StringBuilder(1 << 20);
		json(bundle, sb);
		Files.createDirectories(Path.of(DEMO));
		Files.writeString(Path.of(DEMO, "demo-data.json"), sb.toString() + "\n");
		Files.writeString(Path.of(DEMO, "demo-data.js"), "window.NTN_DEMO = " + sb + ";\n");
		System.out.println(scenarios.size() + " scenarios written to " + DEMO + "demo-data.{json,js} (" + sb.length()
				/ 1024 + " KB) in " + (System.currentTimeMillis() - t0) / 1000 + " s" + (ok ? "" : "  WITH ERRORS"));
		if (!ok) System.exit(1);
	}

	static String fmt(Object o) {
		if (!(o instanceof Double) || !Double.isFinite((Double) o)) return "-";
		return String.format("%.4g", (Double) o);
	}
}
