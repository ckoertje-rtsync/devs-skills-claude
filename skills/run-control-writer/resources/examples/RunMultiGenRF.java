package Models.java;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.ms4systems.devs.analytics.InternalUseSeS;
import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.core.util.SimulateModel;
import com.ms4systems.devs.core.util.fileHandler;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * RunMultiGenRF - run control for the MultiGenRF (Random Forest) model, ms4me flavor.
 *
 * Reconstructed from the code figures of "Conducting Experiments on RandomForest Model
 * with Run Control Experimental Framework" (MS4 Me, July 2022). The figures are joined
 * into one class; WriteRFSeS is that project's helper that writes the SES before a run.
 *
 * Experiment: for ten input indices, prune the forest SES with the "mode" metric, set the
 * tree count and node response time, run, and record per-tree time / prediction / accuracy.
 */
public class RunMultiGenRF extends SimulateModel {

    // ---- 1. Experiment variables -------------------------------------------------
    public static int numTrees = 10;          // trees in the forest
    public static int inputIndex;             // advanced by main() before each run
    public static double responseTime = 1.0;  // every YesNo node's response time

    static String sesfile = "GenNRForest.ses";
    static String pesfile = "GenNRForest.pes";

    public RunMultiGenRF() {
        super();
    }

    // ---- 2. Define the model -------------------------------------------------------
    @Override
    public void defineModel() {
        sesRelationExtend ses = InternalUseSeS.getSesFromFile(sesfile);

        String sesContents = fileHandler.getContentsAsString(folderses + sesfile);
        String pesContents = fileHandler.getContentsAsString(folderpes + pesfile);

        // selections: the SES declares the "metric" specialization; force the "mode" choice
        String[] metric = getChoices(ses, "metric");       // e.g. {"average", "mode"}
        String choice = selectFrom(metric);
        pesContents = pesContents.replaceAll(choice, "mode");

        sesRelationExtend rses = InternalUseSeS.getSesFromContents(sesContents, pesContents);
        model = (CoupledModelImpl) InternalUseSeS.pruneNTransToGetModelInstanceWContents(rses, pesContents);
    }

    // ---- 3. Set parameters ---------------------------------------------------------
    @Override
    public void setParams() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // set number of trees on the chosen specialization of RunningTransd
        setValue("mode_RunningTransd", "NumTrees", numTrees, int.class);
        // set response time on every node (each node is a specialization of YesNo)
        setValuesForAll("YesNo", "responseTime", responseTime, double.class);
    }

    // ---- 4. Get and analyze results -------------------------------------------------
    @Override
    public void getAndAnalyzeResults() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ArrayList<Double> resultsPred = (ArrayList<Double>) GetValue("mode_RunningTransd", "RunningResults");
        ArrayList<Double> resultsTime = (ArrayList<Double>) GetValue("mode_RunningTransd", "RunningTime");
        ArrayList<Double> resultsAcc  = (ArrayList<Double>) GetValue("mode_RunningTransd", "Accuracy");

        for (int i = 0; i < resultsTime.size(); i++) {
            fileHandler.appendToFile(resultsFile,
                    "time, " + resultsTime.get(i) + ", Prediction, " + resultsPred.get(i) + ", Accuracy, " + resultsAcc.get(i));
        }
    }

    // ---- 5. Main: the experiment ------------------------------------------------------
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        RunMultiGenRF sm = new RunMultiGenRF();
        sm.setResultsFile("Selections.txt");
        fileHandler.writeToFile(sm.resultsFile, "");   // clears current file

        final long startTime = System.currentTimeMillis();

        WriteRFSeS.WriteForestSeS();                   // project helper: writes GenNRForest.ses
        inputIndex = -1;
        for (int n = 0; n < 10; n++) {
            inputIndex++;                              // advance input index
            sm.execute();
            // sm.executeWView();                      // same run with the SimViewer; slow
        }

        final long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000);
    }
}
