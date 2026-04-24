
package Models.java;/* 

platform:/resource/ParaFinance_IH_Table_Read_N_Stock/src/Models/dnl/CallGenerator.dnl
-1562206028
  */


import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.message.impl.Port;
import com.ms4systems.devs.core.message.impl.MessageBag;
import com.ms4systems.devs.core.model.impl.Atomic;
import com.ms4systems.devs.core.simulation.impl.AtomicSimulator;
import com.ms4systems.devs.extensions.PhaseBased;
import com.ms4systems.devs.extensions.StateVariableBased;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
// 
// 

@SuppressWarnings("unused")
public class CallGenerator extends Atomic implements PhaseBased,
    StateVariableBased {
    private static final long serialVersionUID = 1L;

    //ID:SVAR:0
    private static final int ID_GENERATIONTIME = 0;

    //ENDID
    //ID:SVAR:1
    private static final int ID_DAYNAME = 1;

    //ENDID
    //ID:SVAR:2
    private static final int ID_CALLTYPE = 2;

    //ENDID
    //ID:SVAR:3
    private static final int ID_PROB = 3;

    //ENDID
    //ID:SVAR:4
    private static final int ID_CLOSE = 4;

    //ENDID
    //ID:SVAR:5
    private static final int ID_SHARES = 5;

    //ENDID
    //ID:SVAR:6
    private static final int ID_INDEX = 6;

    //ENDID
    //ID:SVAR:7
    private static final int ID_DAYORDER = 7;

    //ENDID
    //ID:SVAR:8
    private static final int ID_OUTNOMORE = 8;

    //ENDID
    //ID:SVAR:9
    private static final int ID_CLOSEVALS = 9;

    //ENDID
    //ID:SVAR:10
    private static final int ID_TRACKING = 10;

    // Declare state variables
    private PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    protected double generationTime = 1;
    protected String DayName;
    protected String CallType;
    protected double Prob;
    protected double Close;
    protected int[] Shares;
    protected int Index;
    protected WorkDays[] DayOrder;
    protected int outNoMore;
    protected double[] CloseVals;
    protected int Tracking;

    //ENDID
    String phase = "Activate";
    String previousPhase = null;
    Double sigma = 0.0;
    Double previousSigma = Double.NaN;

    // End state variables

    // Input ports
    // End input ports

    // Output ports
    //ID:OUTP:0
    public final Port outBuy =
        addOutputPort("outBuy", Serializable.class);

    //ENDID
    //ID:OUTP:1
    public final Port outSell =
        addOutputPort("outSell", Serializable.class);

    //ENDID
    //ID:OUTP:2
    public final Port outHolding =
        addOutputPort("outHolding", Serializable.class);

    //ENDID
    //ID:OUTP:3
    public final Port outProb =
        addOutputPort("outProb", Serializable.class);

    //ENDID
    // End output ports
    // 
    protected double currentTime;

    // This variable is just here so we can use @SuppressWarnings("unused")
    private final int unusedIntVariableForWarnings = 0;

    public CallGenerator() {
        this("CallGenerator");
    }

    public CallGenerator(String name) {
        this(name, null);
    }

    public CallGenerator(String name, AtomicSimulator simulator) {
        super(name);
    }

    public void initialize() {
        super.initialize();

        currentTime = 0;

        // Default state variable initialization
        generationTime = 1;

        holdIn("Activate", 0.0);

        // Initialize Variables
        //ID:INIT

        // To run one iteration set call type to whatevs and run ses simulator
        // open in text and then run simulate now..

        //ENDID
        // End initialize variables
    }

    @Override
    public void internalTransition() {
        currentTime += sigma;

        if (phaseIs("Activate")) {
            //getAtomicSimulator()//.modelMessage("Internal transition from Activate");

            //ID:TRA:Activate
            holdIn("GenerateCall", 1.0);

            //ENDID
            // Internal event code
            //ID:INT:Activate
            //place your own code for internal event here.

            //ENDID
            // End internal event code
            return;
        }
        if (phaseIs("GenerateCall")) {
            //getAtomicSimulator()//.modelMessage("Internal transition from GenerateCall");

            //ID:TRA:GenerateCall
            passivateIn("passive");

            //ENDID
            // Internal event code
            //ID:INT:GenerateCall
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
        MessageBag output = new MessageBag();

        if (phaseIs("GenerateCall")) {

            // Output event code
            //ID:OUT:GenerateCall
            //Add your own code
            
            // Get the current time
       //     double currentTime = getAtomicSimulator().getCurrentTime();

            // Set the generation time
            setGenerationTime(currentTime);

            // Get the day name and shares from the state variables
            String DayName = getDayName();
            int[] Shares = getShares();
            
            // Get the close value from the state variable
            double Close = getClose();
            
            // Get the call type from the state variable
            String CallType = getCallType();
            
            // Get the index from the state variable
            int Index = getIndex();
            
            // Get the day order from the state variable
            WorkDays[] DayOrder = getDayOrder();
        	String [] info_array = DayName.split("-");
        	
        	// WIP -- decoding of state information, assumed to be used by who ever generated q table
			int index = 0;
			for (int r = 0; r < Shares.length; r++) {
				index = (int) Math.round(index + Shares[r]*Math.pow((Integer.parseInt(info_array[2])+1),r));
			}
			
			// unpacking row with prob content from q table
            WorkDays WeekDay = new WorkDays();
            WeekDay = DayOrder[index];
            DayName = WeekDay.getDayName();

            // Packing Action information into prob data
            int s_action = Integer.parseInt(CallType);
            // action logic
            int stock_index = (int) Math.round(s_action/10)-1 ;
            int actual_action = (int) Math.round(s_action - (stock_index + 1)*10);
            int actual_action_index = 0;
            
            // WIP -- map from action to q table index assumed to be used by who ever generated q table
            if (actual_action == 0) {
            	actual_action_index = 0;
            }else {
            	actual_action_index = 2*stock_index + 2-actual_action + 1;
            }
            
            Prob = WeekDay.getCallProb()[actual_action_index];
            Prob = Prob + s_action;
            
            // sending out data will always do and outbuy, outbuy equivalent to general "modify" action
            output.add(outBuy, Prob);

        }
        return output;
    }

    // Custom function definitions

    // End custom function definitions
    public static void main(String[] args) {
        // 

        // Uncomment the following line to disable SimViewer for this model
        // options.setDisableViewer(true);

        // Uncomment the following line to disable plotting for this model
        // options.setDisablePlotting(true);

        // Uncomment the following line to disable logging for this model
        // options.setDisableLogging(true);
        CallGenerator model = new CallGenerator();
        // 

        //  // Command line output only
        //     /* =
        //         new Simulation("CallGenerator Simulation", model, options);
        //     sim.startSimulation(0);
        //     */ //
        // 
        //     
        //     viewer.open(model, options);
        // }
    }

    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    // Getter/setter for generationTime
    public void setGenerationTime(double generationTime) {
        propertyChangeSupport.firePropertyChange("generationTime",
            this.generationTime, this.generationTime = generationTime);
    }

    public double getGenerationTime() {
        return this.generationTime;
    }

    // End getter/setter for generationTime

    // Getter/setter for DayName
    public void setDayName(String DayName) {
        propertyChangeSupport.firePropertyChange("DayName", this.DayName,
            this.DayName = DayName);
    }

    public String getDayName() {
        return this.DayName;
    }

    // End getter/setter for DayName

    // Getter/setter for CallType
    public void setCallType(String CallType) {
        propertyChangeSupport.firePropertyChange("CallType", this.CallType,
            this.CallType = CallType);
    }

    public String getCallType() {
        return this.CallType;
    }

    // End getter/setter for CallType

    // Getter/setter for Prob
    public void setProb(double Prob) {
        propertyChangeSupport.firePropertyChange("Prob", this.Prob,
            this.Prob = Prob);
    }

    public double getProb() {
        return this.Prob;
    }

    // End getter/setter for Prob

    // Getter/setter for Close
    public void setClose(double Close) {
        propertyChangeSupport.firePropertyChange("Close", this.Close,
            this.Close = Close);
    }

    public double getClose() {
        return this.Close;
    }

    // End getter/setter for Close

    // Getter/setter for Shares
    public void setShares(int[] Shares) {
        propertyChangeSupport.firePropertyChange("Shares", this.Shares,
            this.Shares = Shares);
    }

    public int[] getShares() {
        return this.Shares;
    }

    // End getter/setter for Shares

    // Getter/setter for Index
    public void setIndex(int Index) {
        propertyChangeSupport.firePropertyChange("Index", this.Index,
            this.Index = Index);
    }

    public int getIndex() {
        return this.Index;
    }

    // End getter/setter for Index

    // Getter/setter for DayOrder
    public void setDayOrder(WorkDays[] DayOrder) {
        propertyChangeSupport.firePropertyChange("DayOrder", this.DayOrder,
            this.DayOrder = DayOrder);
    }

    public WorkDays[] getDayOrder() {
        return this.DayOrder;
    }

    // End getter/setter for DayOrder

    // Getter/setter for outNoMore
    public void setOutNoMore(int outNoMore) {
        propertyChangeSupport.firePropertyChange("outNoMore", this.outNoMore,
            this.outNoMore = outNoMore);
    }

    public int getOutNoMore() {
        return this.outNoMore;
    }

    // End getter/setter for outNoMore

    // Getter/setter for CloseVals
    public void setCloseVals(double[] CloseVals) {
        propertyChangeSupport.firePropertyChange("CloseVals", this.CloseVals,
            this.CloseVals = CloseVals);
    }

    public double[] getCloseVals() {
        return this.CloseVals;
    }

    // End getter/setter for CloseVals

    // Getter/setter for Tracking
    public void setTracking(int Tracking) {
        propertyChangeSupport.firePropertyChange("Tracking", this.Tracking,
            this.Tracking = Tracking);
    }

    public int getTracking() {
        return this.Tracking;
    }

    // End getter/setter for Tracking

    // State variables
    public String[] getStateVariableNames() {
        return new String[] {
            "generationTime", "DayName", "CallType", "Prob", "Close", "Shares",
            "Index", "DayOrder", "outNoMore", "CloseVals", "Tracking"
        };
    }

    public Object[] getStateVariableValues() {
        return new Object[] {
            generationTime, DayName, CallType, Prob, Close, Shares, Index,
            DayOrder, outNoMore, CloseVals, Tracking
        };
    }

    public Class[] getStateVariableTypes() {
        return new Class[] {
            Double.class, String.class, String.class, Double.class, Double.class,
            Integer.class, Integer.class, WorkDays[].class, Integer.class,
            double[].class, Integer.class
        };
    }

    public void setStateVariableValue(int index, Object value) {
        switch (index) {

            case ID_GENERATIONTIME:
                setGenerationTime((Double) value);
                return;

            case ID_DAYNAME:
                setDayName((String) value);
                return;

            case ID_CALLTYPE:
                setCallType((String) value);
                return;

            case ID_PROB:
                setProb((Double) value);
                return;

            case ID_CLOSE:
                setClose((Double) value);
                return;

            case ID_SHARES:
                if (value instanceof Integer) {
                    setShares(new int[] { (Integer) value });
                } else if (value instanceof int[]) {
                    setShares((int[]) value);
                }
                return;
            case ID_INDEX:
                setIndex((Integer) value);
                return;

            case ID_DAYORDER:
                setDayOrder((WorkDays[]) value);
                return;

            case ID_OUTNOMORE:
                setOutNoMore((Integer) value);
                return;

            case ID_CLOSEVALS:
                setCloseVals((double[]) value);
                return;

            case ID_TRACKING:
                setTracking((Integer) value);
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

    public void holdIn(String phase, Double sigma) {
        this.phase = phase;
        this.sigma = sigma;
        //getAtomicSimulator()
            //.modelMessage("Holding in phase " + phase + " for time " + sigma);
    }

    protected static File getModelsDirectory() {
        URI dirUri;
        File dir;
        try {
            dirUri = CallGenerator.class.getResource(".").toURI();
            dir = new File(dirUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(
                "Could not find Models directory. Invalid model URL: " +
                CallGenerator.class.getResource(".").toString());
        }
        boolean foundModels = false;
        while (dir != null && dir.getParentFile() != null) {
            if (dir.getName().equalsIgnoreCase("java") &&
                  dir.getParentFile().getName().equalsIgnoreCase("models")) {
                return dir.getParentFile();
            }
            dir = dir.getParentFile();
        }
        throw new RuntimeException(
            "Could not find Models directory from model path: " +
            dirUri.toASCIIString());
    }

    protected static File getDataFile(String fileName) {
        return getDataFile(fileName, "txt");
    }

    protected static File getDataFile(String fileName, String directoryName) {
        File modelDir = getModelsDirectory();
        File dir = new File(modelDir, directoryName);
        if (dir == null) {
            throw new RuntimeException("Could not find '" + directoryName +
                "' directory from model path: " + modelDir.getAbsolutePath());
        }
        File dataFile = new File(dir, fileName);
        if (dataFile == null) {
            throw new RuntimeException("Could not find '" + fileName +
                "' file in directory: " + dir.getAbsolutePath());
        }
        return dataFile;
    }

    protected void msg(String msg) {
        //getAtomicSimulator()//.modelMessage(msg);
    }

    // Phase display
    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    public String getPhase() {
        return phase;
    }

    public String[] getPhaseNames() {
        return new String[] { "Activate", "GenerateCall", "passive" };
    }
}
