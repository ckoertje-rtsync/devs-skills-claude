package __PACKAGE__;

import com.ms4systems.devs.core.message.impl.Coupling;
import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.model.impl.Atomic;
import com.ms4systems.devs.core.model.impl.ClosureToAtomic;
import com.ms4systems.devs.core.model.impl.Coupled;
import com.ms4systems.devs.core.model.impl.DEVS;
import com.ms4systems.devs.core.simulation.impl.AtomicSimulator;
import com.ms4systems.devs.core.simulation.impl.CoupledCoordinator;
import com.ms4systems.devs.events.SimulationEvent;
import com.ms4systems.devs.events.SimulationEventListener;
import com.ms4systems.devs.events.SimulationEventType;
import com.ms4systems.devs.extensions.PhaseBased;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Records one Closure-DEVS run as a step-by-step trace for a demo page (devs-demo skill).
 * Attach one instance per traced run to every leaf simulator with {@link #attach}; drain it
 * after each simulateIterations(1) with {@link #drain}.
 *
 * Closure-DEVS 1.0.0 fires TRANSITION_* before the model transitions and never fires
 * MESSAGE_SENT under a coordinator, so outputs are read by evaluating the model's lambda on
 * internal and confluent events, and the post-transition state is snapshotted when the step
 * is drained. Output messages are routed to their leaf destinations through a coupling table
 * flattened across ClosureToAtomic boundaries by Port identity ({@link #flatten}).
 *
 * Subclass in the model's package and override {@link #details} to add model state
 * (protected fields are reachable there), {@link #role} and {@link #index} for the topology.
 */
public class TraceRecorder extends SimulationEventListener {
	final Map<Port, List<Port>> routes;
	final double tEnd;
	final List<Map<String, Object>> events = new ArrayList<>();
	final List<Map<String, Object>> msgs = new ArrayList<>();
	final Set<Atomic> touched = new LinkedHashSet<>();	// identity set: DEVS does not override equals
	public int dropped;		// events past tEnd (the simulator executes the first event past its limit)
	public int unexplained;	// input ports of ext/con events not matched by a routed message in the same step

	public TraceRecorder(Map<Port, List<Port>> routes, double tEnd) {
		super(SimulationEventType.TRANSITION_INTERNAL, SimulationEventType.TRANSITION_EXTERNAL,
				SimulationEventType.TRANSITION_CONFLUENT);
		this.routes = routes;
		this.tEnd = tEnd;
	}

	// Fires before the model transitions: getPhase() is the phase before, getOutput() the lambda of that phase.
	@Override
	public void eventOccurred(SimulationEvent e) {
		if (e.getTime() > tEnd) {
			dropped++;
			return;
		}
		if (!(e.getModel() instanceof Atomic) || e.getModel() instanceof ClosureToAtomic)
			return;
		Atomic a = (Atomic) e.getModel();
		SimulationEventType t = e.getEventType();
		Map<String, Object> ev = new LinkedHashMap<>();
		ev.put("model", a.getName());
		ev.put("type", t == SimulationEventType.TRANSITION_INTERNAL ? "int"
				: t == SimulationEventType.TRANSITION_EXTERNAL ? "ext" : "con");
		ev.put("phaseBefore", phase(a));
		if (t != SimulationEventType.TRANSITION_EXTERNAL) {
			List<String> out = new ArrayList<>();
			MessageBag bag = a.getOutput();
			if (bag != null)
				for (Port p : bag.portsWithMessages()) {
					out.add(p.getName());
					List<Port> dests = routes.get(p);
					if (dests == null || dests.isEmpty())
						System.err.println("TraceRecorder: no route for " + a.getName() + "." + p.getName());
					else
						for (Port d : dests)
							msgs.add(msg(a.getName(), p.getName(), d.getModel().getName(), d.getName()));
				}
			ev.put("out", out);
		}
		if (t != SimulationEventType.TRANSITION_INTERNAL) {
			List<String> in = new ArrayList<>();
			MessageBag bag = e.getMessage();
			if (bag != null)
				for (Port p : bag.portsWithMessages())
					in.add(p.getName());
			ev.put("in", in);
		}
		events.add(ev);
		touched.add(a);
	}

	/** One root step; null when nothing was recorded. Clears the buffers. */
	public Map<String, Object> drain(double t) {
		if (events.isEmpty()) {
			msgs.clear();
			touched.clear();
			return null;
		}
		for (Map<String, Object> ev : events) {
			@SuppressWarnings("unchecked")
			List<String> in = (List<String>) ev.get("in");
			if (in == null) continue;
			for (String port : in) {
				boolean found = false;
				for (Map<String, Object> m : msgs)
					if (m.get("to").equals(ev.get("model")) && m.get("toPort").equals(port)) { found = true; break; }
				if (!found) unexplained++;
			}
		}
		Map<String, Object> step = new LinkedHashMap<>();
		step.put("t", t);
		step.put("events", new ArrayList<>(events));
		step.put("msgs", new ArrayList<>(msgs));
		Map<String, Object> after = new LinkedHashMap<>();
		for (Atomic a : touched)
			after.put(a.getName(), snapshot(a));
		step.put("after", after);
		events.clear();
		msgs.clear();
		touched.clear();
		return step;
	}

	static Map<String, Object> msg(String from, String port, String to, String toPort) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("from", from);
		m.put("port", port);
		m.put("to", to);
		m.put("toPort", toPort);
		return m;
	}

	static String phase(Atomic a) {
		return a instanceof PhaseBased ? ((PhaseBased) a).getPhase() : "";
	}

	/** Phase, sigma (null when infinite) and the project's details. */
	public Map<String, Object> snapshot(Atomic a) {
		Map<String, Object> s = new LinkedHashMap<>();
		s.put("phase", phase(a));
		Double ta = a.getTimeAdvance();
		s.put("sigma", ta == null || ta.isInfinite() || ta.isNaN() ? null : ta);
		details(a, s);
		return s;
	}

	/** Override: add model-specific state (numbers, booleans, strings) to the snapshot. */
	protected void details(Atomic a, Map<String, Object> s) {
	}

	/** Override: the role the page draws ("sat", "gs", "ef", ...). Default: the behaviour class name. */
	protected String role(Atomic a) {
		return behaviourClass(a);
	}

	/** Override: the index used for geometry (ring position, chain order). Default: trailing digits of the name. */
	protected Integer index(Atomic a) {
		Matcher m = Pattern.compile("(\\d+)(?!.*\\d)").matcher(a.getName());
		return m.find() ? Integer.valueOf(m.group(1)) : null;
	}

	// ---- topology ---------------------------------------------------------

	static boolean isLeaf(DEVS m) {
		return m instanceof Atomic && !(m instanceof ClosureToAtomic);
	}

	/** Leaf atomics in tree order, through every ClosureToAtomic. */
	public static List<Atomic> leaves(Coupled cm) {
		List<Atomic> out = new ArrayList<>();
		for (Atomic child : cm.getChildren())
			if (child instanceof ClosureToAtomic) out.addAll(leaves(((ClosureToAtomic) child).CModel));
			else out.add(child);
		return out;
	}

	static List<Coupling> allCouplings(Coupled cm) {
		List<Coupling> all = new ArrayList<>(cm.getCouplings());
		for (Atomic child : cm.getChildren())
			if (child instanceof ClosureToAtomic)
				all.addAll(allCouplings(((ClosureToAtomic) child).CModel));
		return all;
	}

	/** Leaf output port -> leaf input ports reached through EOC, IC and EIC, keyed by Port identity. */
	public static Map<Port, List<Port>> flatten(Coupled root) {
		Map<Port, List<Port>> next = new HashMap<>();
		for (Coupling c : allCouplings(root))
			next.computeIfAbsent(c.getSourcePort(), k -> new ArrayList<>()).add(c.getDestinationPort());
		Map<Port, List<Port>> routes = new LinkedHashMap<>();
		for (Atomic a : leaves(root))
			for (Port out : a.getOutputPorts()) {
				List<Port> dests = new ArrayList<>();
				Deque<Port> todo = new ArrayDeque<>(next.getOrDefault(out, List.of()));
				Set<Port> seen = new HashSet<>();
				while (!todo.isEmpty()) {
					Port p = todo.pop();
					if (!seen.add(p)) continue;
					if (isLeaf(p.getModel())) dests.add(p);
					else todo.addAll(next.getOrDefault(p, List.of()));
				}
				if (!dests.isEmpty()) routes.put(out, dests);
			}
		return routes;
	}

	/** Attach to every leaf simulator through each ClosureToAtomic's own coordinator; never to wrappers or the root. */
	public static void attach(CoupledCoordinator coord, Coupled cm, SimulationEventListener l) {
		for (Atomic child : cm.getChildren()) {
			if (child instanceof ClosureToAtomic) {
				ClosureToAtomic cta = (ClosureToAtomic) child;
				attach(cta.internalCoord, cta.CModel, l);
			} else {
				AtomicSimulator s = coord.getSimulator(child);
				if (s == null)
					throw new IllegalStateException(child.getName() + " has no simulator under " + cm.getName());
				s.addSimulationEventListener(l, false);
			}
		}
	}

	/** The behaviour class: climb generated subclasses to the one directly under Atomic. */
	public static String behaviourClass(Atomic a) {
		Class<?> c = a.getClass();
		while (c.getSuperclass() != null && c.getSuperclass() != Atomic.class)
			c = c.getSuperclass();
		return c.getSimpleName();
	}

	/** Leaf models with role, index and enclosing coupled, for the bundle's topology section. */
	public List<Map<String, Object>> models(Coupled cm) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Atomic child : cm.getChildren()) {
			if (child instanceof ClosureToAtomic) {
				list.addAll(models(((ClosureToAtomic) child).CModel));
			} else {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("name", child.getName());
				m.put("class", behaviourClass(child));
				m.put("role", role(child));
				m.put("ring", index(child));
				m.put("parent", cm.getName());
				list.add(m);
			}
		}
		return list;
	}

	public static List<Map<String, Object>> routesJson(Map<Port, List<Port>> routes) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Map.Entry<Port, List<Port>> e : routes.entrySet())
			for (Port d : e.getValue())
				list.add(msg(e.getKey().getModel().getName(), e.getKey().getName(), d.getModel().getName(), d.getName()));
		list.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("from"))
				.thenComparing(m -> (String) m.get("port")).thenComparing(m -> (String) m.get("to")));
		return list;
	}
}
