package __PACKAGE__;

import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;
import com.ms4systems.devs.core.model.impl.Coupled;
import com.ms4systems.devs.core.simulation.impl.CoupledCoordinator;
import com.ms4systems.devs.core.simulation.impl.Simulation;

import java.io.File;
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
 * Standalone demo-data harness (devs-demo skill). Writes demos/data/demo-data.{json,js}.
 *
 * For every scenario: N untraced runs for the statistics panel, then one traced run (the
 * same seed as run 0) with a TraceRecorder attached. If the project already has a harness
 * with a per-event loop, extend that instead and call the same recorder from afterStart()
 * and afterStep() hooks (see sda-pwsa RunDemoData, which extends RunMorphismQuality).
 *
 * TODO markers show what is project-specific.
 */
public class DemoDataRunner {
	static final String ROOT = System.getProperty("user.dir") + File.separator;
	static final String DEMO = ROOT + "demos" + File.separator + "data" + File.separator;
	static final String GLOBAL = "window.__GLOBAL__";		// TODO the page's global, e.g. window.NTN_DEMO
	static final double MAX_SIM_TIME = 1000;				// TODO

	/** TODO: one row per scenario family x variant x stimulus. */
	static class Scenario {
		final String instance, resolution;
		final Double stimulusTime;	// null = none
		Scenario(String i, String r, Double s) { instance = i; resolution = r; stimulusTime = s; }
	}

	/** TODO: build the coupled model for this scenario (hand-coupled constructor or SES pruning). */
	Coupled buildModel(Scenario sc) {
		throw new UnsupportedOperationException("buildModel");
	}

	/** TODO: push the scenario's parameters onto the leaves (setters, setValuesForAll, ...). */
	void applyParams(Coupled model, Scenario sc) {
	}

	/** TODO: seed any Markov RNGs for replication rep (see docs/CLOSURE_DEVS_TRACING.md). */
	void seed(Coupled model, long seed) {
	}

	/** TODO: read the transducer: at least {tReport: number|null, success: boolean}. */
	Map<String, Object> result(Coupled model) {
		throw new UnsupportedOperationException("result");
	}

	/** TODO: a subclass of TraceRecorder with details() for this project's leaves. */
	TraceRecorder recorder(Map<Port, List<Port>> routes, double tEnd) {
		return new TraceRecorder(routes, tEnd);
	}

	Map<Port, List<Port>> routes;
	List<Map<String, Object>> models;

	/** One run; traced when rec != null. Returns {result, initial?, steps?, dropped?, unexplained?}. */
	Map<String, Object> run(Scenario sc, long seed, boolean traced) {
		Coupled model = buildModel(sc);
		applyParams(model, sc);
		Simulation sim = new Simulation("demo", new CoupledCoordinator(model));
		sim.setMaxSimulationTime((long) MAX_SIM_TIME);
		sim.startSimulation(0);
		seed(model, seed);
		TraceRecorder rec = null;
		Map<String, Object> initial = null;
		List<Map<String, Object>> steps = null;
		if (traced) {
			if (routes == null) routes = TraceRecorder.flatten(model);
			rec = recorder(routes, sim.getMaxSimulationTime());
			if (models == null) models = rec.models(model);
			TraceRecorder.attach(sim.getCoupledCoordinator(), model, rec);
			initial = new LinkedHashMap<>();
			for (Atomic a : TraceRecorder.leaves(model)) initial.put(a.getName(), rec.snapshot(a));
			steps = new ArrayList<>();
		}
		while (!sim.isFinished()) {
			sim.simulateIterations(1);
			if (rec != null) {
				double t = sim.getCurrentSimulationTime();
				Map<String, Object> step = rec.drain(t);
				if (step != null && t <= sim.getMaxSimulationTime()) steps.add(step);
			}
		}
		sim.stopSimulation();
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("result", result(model));
		if (rec != null) {
			out.put("initial", initial);
			out.put("steps", steps);
			out.put("dropped", rec.dropped);
			out.put("unexplained", rec.unexplained);
		}
		return out;
	}

	// ---- bundle --------------------------------------------------------------

	Map<String, Object> scenario(Scenario sc, Map<String, Object> traced, List<Double> delivered, int successes, int reps) {
		Map<String, Object> s = new LinkedHashMap<>();
		s.put("ef", sc.instance);
		s.put("resolution", sc.resolution);
		s.put("tFlare", sc.stimulusTime);
		@SuppressWarnings("unchecked")
		Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) traced.get("result"));
		result.put("dropped", traced.get("dropped"));
		s.put("result", result);
		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("n", reps);
		stats.put("successes", successes);
		stats.put("tReport", delivered);
		double mean = Double.NaN, std = Double.NaN;
		if (!delivered.isEmpty()) {
			double sum = 0; for (double d : delivered) sum += d; mean = sum / delivered.size();
			double ss = 0; for (double d : delivered) ss += (d - mean) * (d - mean);
			std = delivered.size() > 1 ? Math.sqrt(ss / (delivered.size() - 1)) : 0;
		}
		stats.put("mean", mean);
		stats.put("std", std);
		s.put("stats", stats);
		Map<String, Object> trace = new LinkedHashMap<>();
		trace.put("initial", traced.get("initial"));
		trace.put("steps", traced.get("steps"));
		s.put("trace", trace);
		return s;
	}

	static String git(String... args) {
		try {
			List<String> cmd = new ArrayList<>(); cmd.add("git"); cmd.addAll(Arrays.asList(args));
			Process p = new ProcessBuilder(cmd).directory(new File(ROOT)).redirectErrorStream(true).start();
			String out = new String(p.getInputStream().readAllBytes()).trim();
			return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0 && !out.isEmpty() ? out : "unknown";
		} catch (Exception e) {
			return "unknown";
		}
	}

	// ---- JSON (no library on the classpath) ------------------------------------

	static void json(Object v, StringBuilder sb) {
		if (v == null) sb.append("null");
		else if (v instanceof String) quote((String) v, sb);
		else if (v instanceof Boolean) sb.append(v);
		else if (v instanceof Double || v instanceof Float) { double d = ((Number) v).doubleValue(); sb.append(Double.isFinite(d) ? Double.toString(d) : "null"); }
		else if (v instanceof Number) sb.append(v);
		else if (v instanceof Map) {
			sb.append('{'); String sep = "";
			for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) { sb.append(sep); quote(String.valueOf(e.getKey()), sb); sb.append(':'); json(e.getValue(), sb); sep = ","; }
			sb.append('}');
		} else if (v instanceof Iterable) {
			sb.append('['); String sep = "";
			for (Object o : (Iterable<?>) v) { sb.append(sep); json(o, sb); sep = ","; }
			sb.append(']');
		} else quote(v.toString(), sb);
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
			default: if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
			}
		}
		sb.append('"');
	}

	// ---- main ------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		int reps = args.length > 0 ? Integer.parseInt(args[0]) : 200;
		long baseSeed = args.length > 1 ? Long.parseLong(args[1]) : 2349991L;
		List<Scenario> scenarios = new ArrayList<>();		// TODO fill: instances x resolutions x stimulus times
		DemoDataRunner r = new DemoDataRunner();
		Map<String, Object> topology = new LinkedHashMap<>();
		List<Map<String, Object>> out = new ArrayList<>();
		boolean ok = true;
		for (Scenario sc : scenarios) {
			List<Double> delivered = new ArrayList<>();
			int successes = 0;
			Object rep0T = null, rep0S = null;
			for (int rep = 0; rep < reps; rep++) {
				@SuppressWarnings("unchecked")
				Map<String, Object> res = (Map<String, Object>) r.run(sc, baseSeed + rep, false).get("result");
				if (Boolean.TRUE.equals(res.get("success")) && res.get("tReport") instanceof Number) { successes++; delivered.add(((Number) res.get("tReport")).doubleValue()); }
				if (rep == 0) { rep0T = res.get("tReport"); rep0S = res.get("success"); }
			}
			Collections.sort(delivered);
			r.routes = null;	// routes depend on the pruned graph; rebuild per resolution if graphs differ
			Map<String, Object> traced = r.run(sc, baseSeed, true);
			@SuppressWarnings("unchecked")
			Map<String, Object> tres = (Map<String, Object>) traced.get("result");
			boolean same = Objects.equals(tres.get("tReport"), rep0T) && Objects.equals(tres.get("success"), rep0S);
			if (!same || (Integer) traced.get("unexplained") > 0) ok = false;
			out.add(r.scenario(sc, traced, delivered, successes, reps));
			System.out.printf("%-12s %-8s %-8s rep0=%s %s steps=%d succ=%d/%d dropped=%s unexplained=%s%n", sc.instance, sc.resolution,
					sc.stimulusTime == null ? "none" : sc.stimulusTime, tres.get("tReport"), same ? "ok" : "DIFF",
					((List<?>) traced.get("steps")).size(), successes, reps, traced.get("dropped"), traced.get("unexplained"));
			if (!topology.containsKey(sc.resolution)) {
				Map<String, Object> top = new LinkedHashMap<>();
				top.put("models", r.models);
				top.put("routes", TraceRecorder.routesJson(r.routes));
				topology.put(sc.resolution, top);
				System.out.println(sc.resolution + ": " + ((List<?>) top.get("routes")).size() + " routes");	// TODO compare with the hand count
				r.models = null;
			}
		}
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("generated", Instant.now().toString());
		meta.put("commit", git("rev-parse", "--short", "HEAD"));
		meta.put("branch", git("rev-parse", "--abbrev-ref", "HEAD"));
		meta.put("reps", reps);
		meta.put("seed", baseSeed);
		meta.put("maxSimTime", MAX_SIM_TIME);
		// TODO meta.put("flareTimes", ...) and the instances section
		Map<String, Object> bundle = new LinkedHashMap<>();
		bundle.put("meta", meta);
		bundle.put("instances", new ArrayList<>());	// TODO
		bundle.put("topology", topology);
		bundle.put("scenarios", out);
		StringBuilder sb = new StringBuilder(1 << 20);
		json(bundle, sb);
		Files.createDirectories(Path.of(DEMO));
		Files.writeString(Path.of(DEMO, "demo-data.json"), sb.toString() + "\n");
		Files.writeString(Path.of(DEMO, "demo-data.js"), GLOBAL + " = " + sb + ";\n");
		System.out.println(out.size() + " scenarios written to " + DEMO + (ok ? "" : "  WITH ERRORS"));
		if (!ok) System.exit(1);
	}
}
