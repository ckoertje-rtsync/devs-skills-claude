package Models.java;



/* Do not remove or modify this comment!  It is required for file identification!
DNL
platform:/resource/Link16CombatFamily/src/Models/dnl/ComWLatency.dnl
1404195689
 Do not remove or modify this comment!  It is required for file identification! */

import com.ms4systems.devs.analytics.*;
import com.ms4systems.devs.markov.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.Serializable;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

//import com.ms4systems.devs.analytics.PMFForSurvival;
import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.model.impl.AtomicModelImpl;
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.Simulator;
import com.ms4systems.devs.extensions.PhaseBased;
import com.ms4systems.devs.extensions.StateVariableBased;
// import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;

// Custom library code
//ID:LIB:0
//<CTM>
//import com.ms4systems.devs.markov.*;
// import com.ms4systems.devs.simviewer.standalone.SimViewer;

//<CTM>

//ENDID
// End custom library code
@SuppressWarnings("unused")
public class GroundWAttack extends AtomicModelImpl implements PhaseBased, StateVariableBased {
	private static final long serialVersionUID = 1L;

	// ID:SVAR:0
	private static final int ID_CTM = 0;

	// ENDID
	// ID:SVAR:1
	private static final int ID_MAXTA = 1;

	// Declare state variables
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	protected ContinuousTimeMarkov ctm = new ContinuousTimeMarkov();
	protected double maxTA = Double.MAX_VALUE;

	// ENDID
	 protected double pRecover = 1.0;
	    protected double pDead = .0;
	    protected double pSlow = .0;

	    //ENDID
	    String phase = "WaitForOrder";
	    String previousPhase = null;
	    Double sigma = Double.POSITIVE_INFINITY;
	    Double previousSigma = Double.NaN;

	    // End state variables

	    // Input ports
	    //ID:INP:0
	    public final Port<Serializable> inAttack =
	        addInputPort("inAttack", Serializable.class);

	    //ENDID
	    //ID:INP:1
	    public final Port<Serializable> inOrder =
	        addInputPort("inOrder", Serializable.class);

	    //ENDID
	    //ID:INP:2
	    public final Port<Serializable> inpSlow =
	        addInputPort("inpSlow", Serializable.class);

	    //ENDID
	    //ID:INP:3
	    public final Port<Serializable> inpDead =
	        addInputPort("inpDead", Serializable.class);

	    //ENDID
	    //ID:INP:4
	    public final Port<Serializable> inpRecover =
	        addInputPort("inpRecover", Serializable.class);

	    //ENDID
	    // End input ports

	    // Output ports
	    //ID:OUTP:0
	    public final Port<Serializable> outDone =
	        addOutputPort("outDone", Serializable.class);

	    //ENDID
	    // End output ports
	    // protected SimulationOptionsImpl options = new SimulationOptionsImpl();
	    protected double currentTime;

	    // This variable is just here so we can use @SuppressWarnings("unused")
	    private final int unusedIntVariableForWarnings = 0;

	public void initialize() {
		super.initialize();

		currentTime = 0;

		// Default state variable initialization
		ctm = new ContinuousTimeMarkov();
		maxTA = Double.MAX_VALUE;

		holdIn("Wait", maxTA);

		ctm.addTransitionInfoForExponential("Attacked", new String[] { "Move","SlowMove","Dead" }, new double[] { pRecover,pSlow,pDead });
		ctm.addTransitionInfoForExponential("Dead", new String[] { "Dead" }, new double[] { 1. });
		ctm.addTransitionInfoForExponential("Move", new String[] { "WaitForOrder" }, new double[] { 1. });
		ctm.addTransitionInfoForExponential("SlowMove", new String[] { "WaitForOrderSlow" }, new double[] { 1. });
		ctm.addTransitionInfoForExponential("WaitForOrder", new String[] { "WaitForOrder" }, new double[] { 1. });
		ctm.addTransitionInfoForExponential("WaitForOrderSlow", new String[] { "WaitForOrderSlow" }, new double[] { 1. });
				
		//	ctm.addTransitionInfo("Wait", new String[] { "Transmit" }, new double[] { 1. });
       // ctm.addTimeInfo("Wait",  "Transmit", "Normal", 3.);
		long Seed = 354992735;
		ctm.Rand = new Random(Seed);

		// Initialize Variables
		// ID:INIT
		// <CTM>
//       ctm = new ContinuousTimeMarkov();
//        ctm.setTimeToNextEvent(0.0);
//        ctm.setOutput(false);
//        String path = getModelsDirectory().getAbsolutePath();
//        path.replace("bin", "src");
//        try {
//            ctm.fillTransitionInfoList(path + File.separator + "xml" +
//                File.separator + "ComWLatency.xml");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
		passivateIn("WaitForOrder");

		// <CTM>

		// ENDID
		// End initialize variables
	}

	@Override
    public void internalTransition() {
   //        internalTransitionOld();
		String nextphase = ctm.nextPhase(phase);
		double norm = ctm.normalFactor(phase);
		double time = ctm.timeToNextEvent(phase, nextphase, norm);
        holdIn(nextphase,time); 
    }
	 public void internalTransitionOld() {
	        currentTime += sigma;

	        if (phaseIs("Move")) {
	            getSimulator().modelMessage("Internal transition from Move");

	            //ID:TRA:Move
	            passivateIn("WaitForOrder");

	            //ENDID
	            // Internal event code
	            //ID:INT:Move
	            //place your own code for internal event here.
	            //ENDID
	            // End internal event code
	            return;
	        }
	        if (phaseIs("SlowMove")) {
	            getSimulator().modelMessage("Internal transition from SlowMove");

	            //ID:TRA:SlowMove
	            passivateIn("WaitForOrderSlow");

	            //ENDID
	            // Internal event code
	            //ID:INT:SlowMove
	            //place your own code for internal event here.
	            //ENDID
	            // End internal event code
	            return;
	        }

	        //passivate();
	    }

	@Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        currentTime += timeElapsed;
        // Subtract time remaining until next internal transition (no effect if sigma == Infinity)
        sigma -= timeElapsed;

        // Store prior data
        previousPhase = phase;
        previousSigma = sigma;

        // Fire state transition functions
        if (phaseIs("WaitForOrder")) {
            if (input.hasMessages(inAttack)) {
                ArrayList<Message<Serializable>> messageList =
                    inAttack.getMessages(input);

       //         passivateIn("Attacked");
                holdIn("Attacked",0.);

                // Fire state and port specific external transition functions
                //ID:EXT:WaitForOrder:inAttack
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
            if (input.hasMessages(inOrder)) {
                ArrayList<Message<Serializable>> messageList =
                    inOrder.getMessages(input);

                holdIn("Move", 1.0);

                // Fire state and port specific external transition functions
                //ID:EXT:WaitForOrder:inOrder
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
        }

        if (phaseIs("Attacked")) {
            if (input.hasMessages(inpSlow)) {
                ArrayList<Message<Serializable>> messageList =
                    inpSlow.getMessages(input);

                holdIn("SlowMove", 2.0);

                // Fire state and port specific external transition functions
                //ID:EXT:Attacked:inpSlow
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
            if (input.hasMessages(inpDead)) {
                ArrayList<Message<Serializable>> messageList =
                    inpDead.getMessages(input);

                passivateIn("Dead");

                // Fire state and port specific external transition functions
                //ID:EXT:Attacked:inpDead
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
            if (input.hasMessages(inpRecover)) {
                ArrayList<Message<Serializable>> messageList =
                    inpRecover.getMessages(input);

                holdIn("Move", 1.0);

                // Fire state and port specific external transition functions
                //ID:EXT:Attacked:inpRecover
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
        }

        if (phaseIs("Move")) {
            if (input.hasMessages(inAttack)) {
                ArrayList<Message<Serializable>> messageList =
                    inAttack.getMessages(input);

                passivateIn("Attacked");

                // Fire state and port specific external transition functions
                //ID:EXT:Move:inAttack
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
        }

        if (phaseIs("SlowMove")) {
            if (input.hasMessages(inAttack)) {
                ArrayList<Message<Serializable>> messageList =
                    inAttack.getMessages(input);

                passivateIn("Dead");

                // Fire state and port specific external transition functions
                //ID:EXT:SlowMove:inAttack
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
        }

        if (phaseIs("WaitForOrderSlow")) {
            if (input.hasMessages(inOrder)) {
                ArrayList<Message<Serializable>> messageList =
                    inOrder.getMessages(input);

                holdIn("SlowMove", 2.0);

                // Fire state and port specific external transition functions
                //ID:EXT:WaitForOrderSlow:inOrder
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
            if (input.hasMessages(inAttack)) {
                ArrayList<Message<Serializable>> messageList =
                    inAttack.getMessages(input);

                passivateIn("Dead");

                // Fire state and port specific external transition functions
                //ID:EXT:WaitForOrderSlow:inAttack
                //Add your own code
                Serializable variable = messageList.get(0).getData();

                //ENDID
                // End external event code
                return;
            }
        }
    }


	@Override
	public void confluentTransition(MessageBag input) {
		// confluentTransition with internalTransition first (by default)
		internalTransition();
		externalTransition(0, input);
	}

	@Override
	public Double getTimeAdvance() {
		return sigma;
	}

	@Override
	  public MessageBag getOutput() {
        MessageBag output = new MessageBagImpl();

        if (phaseIs("Move")) {
            // Output event code
            //ID:OUT:Move
            //Add your own code
            output.add(outDone, null);

            //ENDID
            // End output event code
        }
        if (phaseIs("SlowMove")) {
            // Output event code
            //ID:OUT:SlowMove
            //Add your own code
            output.add(outDone, null);

            //ENDID
            // End output event code
        }
        return output;
    }

	// Custom function definitions

	// ID:CUST:0
	// <CTM>
	public double[] fillProbabilities(String state) {
		if (ctm.getSuccs(state).size() == 0) {
			return new double[0];
		}
		double[] probabilities = new double[ctm.getSuccs(state).size()];
		for (String succ : ctm.getSuccs(state)) {
			if (succ.equals(state)) {
				continue;
			}
			TransitionInfo ti = ctm.getTransitionInfoFor(state, succ);
			probabilities[ctm.getIndex(succ)] = ti.getProbValue();
		}
		return probabilities;
	}

	public void internalTransitionForMarkov(String state, double sta, String phase) {
		double timeToNextEvent = ctm.getTimeToNextEvent();
		if (timeToNextEvent == 0) {
			timeToNextEvent = Double.POSITIVE_INFINITY;
			if (ctm.getSuccs(state).size() == 0) {
				return;
			}
			if (ctm.getSuccs(state).size() == 1) {
				ArrayList succs = ctm.getSuccs(state);
				if (succs.get(0).equals(state)) {
					return;
				}
			}
			double sample = ctm.getRand().nextDouble();
			double min = 0.0;
			double max = 0.0;
			boolean found = false;
			String SelSucc = state;
			double norm = ctm.normalFactor(state);
			for (String succ : ctm.getSuccs(state)) {
				min = max;
				TransitionInfo ti = ctm.getTransitionInfoFor(state, succ);
				max += ti.getProbValue() / norm;
				if (min < sample && sample <= max) {
					found = true;
					ctm.setNextState(succ);
					SelSucc = succ;
					break;
				}
			}
			if (!found) {
				ctm.setNextState(state);
			}
			double time = ctm.timeToNextEvent(state, SelSucc, norm);
			ctm.setTimeToNextEvent(time);
			timeToNextEvent = time;
			if (timeToNextEvent > sta) {
				timeToNextEvent = sta;
				ctm.setTimeToNextEvent(timeToNextEvent);
				if (!phase.equals("")) {
					ctm.setNextState(phase);
				} else {
					ctm.setNextState(state);
				}
			}
		}
		if (previousPhase != null && previousPhase.equals(state)) {
			previousPhase = null;
			ctm.setTimeToNextEvent(0.0);
			String nextState = ctm.getNextState();
			holdIn(nextState, 0.);
			ctm.setOutput(false);
		} else {
			TimeInState tm = ctm.getTimeInState(state);
			if (tm == null) {
				tm = new TimeInState();
				tm.setStateName(state);
				tm.setCountInState(0);
				tm.setElapsedTime(0.);
				ctm.getTimeInStateList().add(tm);
			}
			holdIn(state, timeToNextEvent);
			ctm.setOutput(true);
			previousPhase = state;
			ctm.incCount(tm);
			ctm.updateElapsedTime(tm, timeToNextEvent);
			double accLifeTime = ctm.getAccLifeTime() + timeToNextEvent;
			ctm.setAccLifeTime(accLifeTime);
			ctm.printTimeInState();
		}
	}

	// <CTM>

	// ENDID

	// End custom function definitions
	public static void main(String[] args) {
	
		GroundWAttack model = new GroundWAttack();

		if (true) { // Command line output only
			Simulation sim = new com.ms4systems.devs.core.simulation.impl.SimulationImpl("NewMarkov Simulation", model);
			sim.startSimulation(0);
			MessageBagImpl m = new MessageBagImpl();
			m.add(model.getInputPort("inAttack"), null);
			sim.injectInput(0, m);
			sim.simulateIterations(Long.MAX_VALUE);
		}

	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	// Getter/setter for ctm
	public void setCtm(ContinuousTimeMarkov ctm) {
		propertyChangeSupport.firePropertyChange("ctm", this.ctm, this.ctm = ctm);
	}

	public ContinuousTimeMarkov getCtm() {
		return this.ctm;
	}

	// End getter/setter for ctm

	// Getter/setter for maxTA
	public void setMaxTA(double maxTA) {
		propertyChangeSupport.firePropertyChange("maxTA", this.maxTA, this.maxTA = maxTA);
	}

	public double getMaxTA() {
		return this.maxTA;
	}

	// End getter/setter for maxTA

	// State variables
	public String[] getStateVariableNames() {
		return new String[] { "ctm", "maxTA" };
	}

	public Object[] getStateVariableValues() {
		return new Object[] { ctm, maxTA };
	}

	public Class<?>[] getStateVariableTypes() {
		return new Class<?>[] { ContinuousTimeMarkov.class, Double.class };
	}

	public void setStateVariableValue(int index, Object value) {
		switch (index) {

		case ID_CTM:
			setCtm((ContinuousTimeMarkov) value);
			return;

		case ID_MAXTA:
			setMaxTA((Double) value);
			return;

		default:
			return;
		}
	}

	// Convenience functions
	protected void passivate() {
		passivateIn("passive");
	}

	protected void passivateIn(String phase) {
		holdIn(phase, Double.POSITIVE_INFINITY);
	}

	protected void holdIn(String phase, Double sigma) {
		this.phase = phase;
		this.sigma = sigma;
		getSimulator().modelMessage("Holding in phase " + phase + " for time " + sigma);
	}

	protected static File getModelsDirectory() {
		URI dirUri;
		File dir;
		try {
			dirUri = GroundWAttack.class.getResource(".").toURI();
			dir = new File(dirUri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not find Models directory. Invalid model URL: "
					+ GroundWAttack.class.getResource(".").toString());
		}
		boolean foundModels = false;
		while (dir != null && dir.getParentFile() != null) {
			if (dir.getName().equalsIgnoreCase("java") && dir.getParentFile().getName().equalsIgnoreCase("models")) {
				return dir.getParentFile();
			}
			dir = dir.getParentFile();
		}
		throw new RuntimeException("Could not find Models directory from model path: " + dirUri.toASCIIString());
	}

	protected static File getDataFile(String fileName) {
		return getDataFile(fileName, "txt");
	}

	protected static File getDataFile(String fileName, String directoryName) {
		File modelDir = getModelsDirectory();
		File dir = new File(modelDir, directoryName);
		if (dir == null) {
			throw new RuntimeException(
					"Could not find '" + directoryName + "' directory from model path: " + modelDir.getAbsolutePath());
		}
		File dataFile = new File(dir, fileName);
		if (dataFile == null) {
			throw new RuntimeException("Could not find '" + fileName + "' file in directory: " + dir.getAbsolutePath());
		}
		return dataFile;
	}

	protected void msg(String msg) {
		getSimulator().modelMessage(msg);
	}

	// Phase display
	public boolean phaseIs(String phase) {
		return this.phase.equals(phase);
	}

	public String getPhase() {
		return phase;
	}

	public String[] getPhaseNames() {
		return new String[] { "Wait", "Transmit", "Send" };
	}
}
