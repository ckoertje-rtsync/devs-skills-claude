package Models.java;

import com.ms4systems.devs.core.model.CoupledModel;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;


public class PedTwinTest {
    public static void main (String[] args) throws ClassNotFoundException {
        PediatricDigitalTwinAtomic model = new PediatricDigitalTwinAtomic();

        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(model);
        System.out.println(ses.printTreeString());
        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModel cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

    }
}
