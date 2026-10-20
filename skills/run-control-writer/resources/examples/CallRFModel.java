package Models.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import com.ms4systems.devs.core.util.fileHandler;

/**
 * CallRFModel - outer-level run control around RunMultiGenRF (section 6 of the
 * documentation). Each cycle runs the inner run control on its own thread, copies its
 * results into a cumulative file, and after every ten runs tests an accuracy criterion;
 * when it fails, the forest specification in the shared request file is grown so the
 * Python side builds a bigger model for the next runs.
 *
 * Reconstructed from the documentation's code figures. The shared-file names are not shown
 * there and are chosen here; everything else follows the figures.
 */
public class CallRFModel {
    static String folder = System.getProperty("user.dir") + File.separator;
    static String foldertxt = folder + "src" + File.separator + "Models" + File.separator + "txt" + File.separator;

    static String results = foldertxt + "AllSelections.txt";   // cumulative results of every run
    static String request = foldertxt + "Request.txt";         // model spec requested from Python
    static String sim     = foldertxt + "SimSettings.txt";     // run count and delay, shared with Python

    static int numTrees = 10;
    static int maxDepth = 3;
    static ArrayList<Double> tempResults = new ArrayList<Double>();

    public static void main(String[] args) {
        fileHandler.writeToFile(results, "");
        fileHandler.writeToFile(request, "Number of trees, " + numTrees + ", Max depth, " + maxDepth);

        int count = 100;
        int delay = 300;   // in seconds; must exceed one RunMultiGenRF.main() wall time
        fileHandler.writeToFile(sim, "Count, " + count + ", Delay, " + delay);

        try {
            new Try().main(count, 1000L * delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Assigns a thread per run and sleeps for the characteristic run time between starts. */
    static class Try implements Runnable {

        public void main(int maxTimes, long period) throws InterruptedException {
            Try obj = new Try();
            int count = 0;

            while (count < maxTimes) {
                count++;
                System.out.println("When the thread starts again");
                System.out.println(Calendar.getInstance().getTimeInMillis());
                Thread thread = new Thread(obj);
                thread.start();
                Thread.sleep(period);
                System.out.println("When the thread finishes sleeping");
                System.out.println(Calendar.getInstance().getTimeInMillis());
            }
        }

        @Override
        public void run() {
            doJavaCycle();
        }
    }

    /** One run of the inner run control plus the experiment's bookkeeping and stopping criterion. */
    static void doJavaCycle() {
        try {
            RunMultiGenRF.main(null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String selections = "Selections.txt";
        String selectContents = fileHandler.getContentsAsString(foldertxt + selections);
        fileHandler.appendToFile(results, selectContents);

        String prediction = foldertxt + "Prediction.txt";
        String readPre = fileHandler.readFromFile(prediction);
        tempResults.add(Double.parseDouble(readPre));

        if (tempResults.size() == 10) {
            // test accuracy over the last ten runs
            double total = 0;
            for (double i : tempResults) {
                total += i;
            }
            double accuracy = total / 10;
            if (accuracy < 0.8) {
                numTrees++;
                maxDepth++;
            }
            fileHandler.appendToFile(request, "Number of trees, " + numTrees + ", Max depth, " + maxDepth);
            tempResults = new ArrayList<Double>();
        }
    }
}
