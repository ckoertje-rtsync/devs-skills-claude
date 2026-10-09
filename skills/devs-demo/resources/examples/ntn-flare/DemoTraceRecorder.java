package Models.java;

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
 * Records one run of the NTN flare frame as a step-by-step trace for the demo page
 * (demos/ntn-flare-demo.html). One instance per traced run, attached to every leaf
 * atomic's simulator ({@link #attach}); RunDemoData drains it after each root event
 * (RunMorphismQuality.afterStep).
 *
 * Closure-DEVS 1.0.0 fires TRANSITION_* before the model transitions and, under a
 * coordinator, never fires MESSAGE_SENT (see StructuralDistance.Recorder). So outputs
 * are read by evaluating the model's lambda on an internal or confluent event, and the
 * post-transition state is snapshotted when the step is drained. Each output message is
 * routed to its leaf destinations through a coupling table flattened across the pruned
 * ClosureToAtomic boundaries ({@link #flatten}): the wrapper exposes the same Port
 * objects as its CModel, so routes follow Port identity and need no name matching.
 */
public class DemoTraceRecorder extends SimulationEventListener {
	final Map<Port, List<Port>> routes;
	final double tEnd;
	final List<Map<String, Object>> events = new ArrayList<>();
	final List<Map<String, Object>> msgs = new ArrayList<>();
	final Set<Atomic> touched = new LinkedHashSet<>();	// identity: DEVS does not override equals
	int dropped;		// events past tEnd (the simulator executes the first event past its limit)
	int unexplained;	// input ports of ext/con events not matched by a routed message in the same step

	public DemoTraceRecorder(Map<Port, List<Port>> routes, double tEnd) {
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
		ev.put("phaseBefore", ((PhaseBased) a).getPhase());
		if (t != SimulationEventType.TRANSITION_EXTERNAL) {
			List<String> out = new ArrayList<>();
			MessageBag bag = a.getOutput();
			if (bag != null)
				for (Port p : bag.portsWithMessages()) {
					out.add(p.getName());
					List<Port> dests = routes.get(p);
					if (dests == null || dests.isEmpty())
						System.err.println("DemoTraceRecorder: no route for " + a.getName() + "." + p.getName());
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

	/** One root step (one simulateIterations(1)); null when nothing was recorded. Clears the buffers. */
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
					if (m.get("to").equals(ev.get("model")) && m.get("toPort").equals(port)) {
						found = true;
						break;
					}
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

	/** Phase, sigma and the demo-relevant state of one leaf atomic. Infinite sigma is null. */
	static Map<String, Object> snapshot(Atomic a) {
		Map<String, Object> s = new LinkedHashMap<>();
		s.put("phase", ((PhaseBased) a).getPhase());
		Double ta = a.getTimeAdvance();
		s.put("sigma", ta == null || ta.isInfinite() || ta.isNaN() ? null : ta);
		if (a instanceof CellSAT) {
			CellSAT c = (CellSAT) a;
			if (c.Budget != null) {
				s.put("snrDb", c.Budget.snrDb());
				s.put("pConnect", c.Budget.pConnect());
				s.put("expectedLatency", c.Budget.expectedLatency());
				s.put("usable", c.Budget.pConnect() >= CellSAT.P_CONNECT_FLOOR);
			}
			s.put("environmentLoss", c.getEnvironment_Loss());
			s.put("preferredReceiver", c.getPreferredReceiver());
		} else if (a instanceof SatCTM) {
			SatCTM m = (SatCTM) a;
			s.put("pConnect", m.getP_Connect());
			s.put("expectLatency", m.getExpect_Latency());
			s.put("preferredReceiver", m.getPreferredReceiver());
			s.put("nextState", m.ctm == null ? null : m.ctm.getNextState());
		}
		return s;
	}

	// ---- topology ---------------------------------------------------------

	static boolean isLeaf(DEVS m) {
		return m instanceof Atomic && !(m instanceof ClosureToAtomic);
	}

	/** Every coupling of the frame and of every pruned coupled inside it, recursively. */
	static List<Coupling> allCouplings(Coupled cm) {
		List<Coupling> all = new ArrayList<>(cm.getCouplings());
		for (Atomic child : cm.getChildren())
			if (child instanceof ClosureToAtomic)
				all.addAll(allCouplings(((ClosureToAtomic) child).CModel));
		return all;
	}

	/** Leaf output port -> leaf input ports reached through EOC, IC and EIC, keyed by Port identity. */
	static Map<Port, List<Port>> flatten(Coupled root) {
		Map<Port, List<Port>> next = new HashMap<>();
		for (Coupling c : allCouplings(root))
			next.computeIfAbsent(c.getSourcePort(), k -> new ArrayList<>()).add(c.getDestinationPort());
		Map<Port, List<Port>> routes = new LinkedHashMap<>();
		for (Atomic a : SimulateModel.getAtomicModels(root))
			for (Port out : a.getOutputPorts()) {
				List<Port> dests = new ArrayList<>();
				Deque<Port> todo = new ArrayDeque<>(next.getOrDefault(out, List.of()));
				Set<Port> seen = new HashSet<>();
				while (!todo.isEmpty()) {
					Port p = todo.pop();
					if (!seen.add(p)) continue;
					if (isLeaf(p.getModel())) dests.add(p);				// a hop ends at a leaf input port
					else todo.addAll(next.getOrDefault(p, List.of()));	// a coupled port: keep following
				}
				if (!dests.isEmpty()) routes.put(out, dests);
			}
		return routes;
	}

	/** Attach to every leaf simulator, through each ClosureToAtomic's own coordinator; never to wrappers or the root. */
	static void attach(CoupledCoordinator coord, Coupled cm, SimulationEventListener l) {
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

	static final Pattern RING = Pattern.compile("X0Y(\\d)");

	static String role(Atomic a) {
		return (a instanceof CellSAT || a instanceof SatCTM) ? "sat" : a instanceof GS ? "gs" : "ef";
	}

	static Integer ring(Atomic a) {
		Matcher m = RING.matcher(a.getName());
		return m.find() ? Integer.valueOf(m.group(1)) : null;
	}

	/** The behaviour class: climb the generated subclasses (X0Y0_CellSAT, Transmitter_GS) to the one under Atomic. */
	static String behaviourClass(Atomic a) {
		Class<?> c = a.getClass();
		while (c.getSuperclass() != null && c.getSuperclass() != Atomic.class)
			c = c.getSuperclass();
		return c.getSimpleName();
	}

	/** Leaf models in tree order with their enclosing pruned coupled. */
	static List<Map<String, Object>> models(Coupled cm) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Atomic child : cm.getChildren()) {
			if (child instanceof ClosureToAtomic) {
				list.addAll(models(((ClosureToAtomic) child).CModel));
			} else {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("name", child.getName());
				m.put("class", behaviourClass(child));
				m.put("role", role(child));
				m.put("ring", ring(child));
				m.put("parent", cm.getName());
				list.add(m);
			}
		}
		return list;
	}

	static List<Map<String, Object>> routesJson(Map<Port, List<Port>> routes) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Map.Entry<Port, List<Port>> e : routes.entrySet())
			for (Port d : e.getValue())
				list.add(msg(e.getKey().getModel().getName(), e.getKey().getName(), d.getModel().getName(), d.getName()));
		list.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("from"))
				.thenComparing(m -> (String) m.get("port")).thenComparing(m -> (String) m.get("to")));
		return list;
	}
}
