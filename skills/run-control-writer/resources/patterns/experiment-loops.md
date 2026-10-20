# Experiment loop patterns

Shapes for `main()` and `defineModel()` beyond the single sweep in the template. All use the static-variable idiom: `main()` sets a static field, `execute()` regenerates the model, `defineModel()` / `setParams()` read the field.

## 1. Parameter sweep (the template's default)

```java
sm.setResultsFile("Sweep.txt");
fileHandler.writeToFile(sm.resultsFile, "inputIndex, time, prediction, accuracy");
for (int i = 0; i < 10; i++) {
    inputIndex = i;          // static; read by setParams()
    sm.execute();            // closure: sm.execute(numIterations)
}
```

Nested sweeps are nested loops; write every swept value into the CSV row so the file is self-describing.

## 2. Selection sweep (every alternative of a specialization)

```java
// defineModel() replaces "select " + current + " from metric for RunningTransd" with metricChoice
static String metricChoice;

sesRelationExtend ses = InternalUseSeS.getSesFromFile(sesfile);
for (String c : getChoices(ses, "metric")) {   // the SES's own alternatives, nothing invented
    metricChoice = c;
    sm.execute();
}
```

Random pick instead: `metricChoice = selectFrom(getChoices(ses, "metric"));`.

## 3. Multiplicity sweep (size of a multi-aspect)

```java
static int numWorkers;
// defineModel(): pesContents = pesContents.replace("[3] for Worker", "[" + numWorkers + "] for Worker");
for (numWorkers = 1; numWorkers <= 8; numWorkers *= 2) {
    sm.execute();
}
```

`setValuesForAll("Worker", "serviceTime", t, double.class)` then reaches every instance regardless of count.

## 4. Replications

```java
for (int rep = 0; rep < 30; rep++) {
    replication = rep;       // goes into the CSV row; Markov models draw fresh randoms per run
    sm.execute();
}
```

## 5. Outer loop on a thread with shared files (co-simulation with another process)

From the documentation's section 6: MS4 Me simulates, writes a request file, and a Python process rebuilds the model between runs; both sides read and write files under `src/Models/txt/`. `examples/CallRFModel.java` is the full reconstruction.

```java
public class CallModel implements Runnable {
    public void run() { doJavaCycle(); }           // one inner-run-control run plus bookkeeping

    public void main(int maxTimes, long periodMillis) throws InterruptedException {
        int count = 0;
        while (count < maxTimes) {
            count++;
            new Thread(new CallModel()).start();   // launches run()
            Thread.sleep(periodMillis);            // must exceed one run's wall time
        }
    }
}
```

Sizing the delay: wrap one `sm.execute()` in

```java
final long startTime = System.currentTimeMillis();
sm.execute();
final long endTime = System.currentTimeMillis();
System.out.println((endTime - startTime) / 1000);
```

and use a sleep comfortably above the printed seconds. Too short a delay means the next thread writes a new SES and regenerates models while the previous run is still simulating, and that run finishes against the wrong models.

Adaptive stopping (same section): after every N runs average a result read back from the shared file; if it misses the target, bump the structural variable, append the new spec to the request file, reset the buffer.

## 6. Hand-built coupled model (closure flavor, no SES)

```java
@Override
public void defineModel() {
    model = new MyCoupled("MyCoupled");       // closure-devs-coupled output
}
```

Everything else is unchanged; `setValue` works on any atomic with the accessor.
