//Package Declerations; DO NOT MODIFY
package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple atomic model.
 * - Sends out outputs to another atomic model
 * - Receives responses and inputs from another atomic model
 */

public class AtomicModelTemplate extends PhaseAtomic {

    private static final long serialVersionUID = 1L;
    
    // --- Outputs: requests sent to other atomic models or coupled models ---
    //Note: List can include as many portnames as needed
    public final Port<String> outPortnameOne;    // Portname One of the output ports
    public final Port<String> outPortnameTwo;    // Portname Two of the output ports


    // --- Inputs: responses received from other atomic models or coupled models---
    //Note: List can include as many portnames as needed
    public final Port<String> inPortnameOne;    // Portname One of the input ports
    public final Port<String> inPortnameTwo;   // Portname Two of the input ports 

    //Constructor for Atomic Model; ONLY MODIFY THE PORT CONSTRUCTORS
    public AtomicModelTemplate(String name) {
        super(name);

        // Define all input and output ports previously constructed
        outPortnameOne   = addOutputPort("outPortnameOne", String.class);
        outPortnameTwo  = addOutputPort("outPortnameTwo", String.class);

        inPortnameOne   = addInputPort("inPortnameOne", String.class);
        inPortnameTwo  = addInputPort("inPortnameTwo", String.class);

    }

    // Local control; Initialize phase step; DO NOT MODIFY
    private int step = 0;


    @Override
    //Initialize method; Should begin at initial phase of the atomic model
    public void initialize() {
        super.initialize();
        step = 0;
        holdIn("initial phase", 0.0);
    }

    @Override
    //getTimeAdvance method; DO NOT MODIFY
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    //getOutput method; Determines the message bag output for each respective case based on the receieved input; Modify for the respective input port
    public MessageBag getOutput() {
        if (!phaseIs("phase name")) return MessageBag.EMPTY;
        MessageBagImpl out = new MessageBagImpl();
        switch (step) {
            //Cases defined for each phase in the atomic model; List all input ports with their respective step and write data
            case 0:
                out.add(inPortnameOne, "Print out data for Case 0 when PortnameOne is receieved.");
                break;
            case 1:
                out.add(inPortnameTwo, "Print out data for Case 1 when PortnameOne is receieved.");
                break;
            default:
                break;
        }
        return out;
    }

    @Override
    //internalTransition method; Determines the phase based on the current step; Once phase is completed, set the new time advance sigma
    public void internalTransition() {
        //Modify numSteps and ta (time advance)
        int numSteps = 0;
        int ta = 0; 
        if (phaseIs("phase name")) {
            step++;
            if (step < numSteps-1) {
                holdIn("phase name", ta); // schedule next request
            } else {
                passivate();
            }
        }
    }

    @Override
    //externalTransition method; List all input portnames
    public void externalTransition(double timeElapsed, MessageBag input) {
        List<String> portnameOneResp = getMessages(input, inPortnameOne);
        List<String> portnameTwoResp = getMessages(input, inPortnameTwo);


        if (!portnameOneResp.isEmpty()) {
            System.out.println("[AtomicModelName] Received portnameOneResp: " + portnameOneResp.get(0));
        }
        if (!portnameTwoResp.isEmpty()) {
            System.out.println("[AtomicModelName] Received portnameTwoResp: " + portnameTwoResp.get(0));

        }
    }

    //DO NOT MODIFY
    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
    }

    //DO NOT MODIFY
    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    //DO NOT MODIFY
    private static Object getFirstMessageValue(List ll) {
              MessageImpl    m = (MessageImpl) ll.get(0);
              return m.getData();		
      }
}