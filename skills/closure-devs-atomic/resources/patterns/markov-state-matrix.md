# Markov state / transition rate matrix

For continuous-time Markov atomic models, extend `com.ms4systems.devs.markovmodel.ContinuousTimeMarkov`. It already provides:

- `phase`, `sigma` fields and `holdIn()`, `phaseIs()`, `passivate()`, `passivateIn()`
- `TransitionInfoList` — `ArrayList<TransitionInfo>` of state → state rates
- `TimeInStateList` — occupancy log per state
- `timeToNextEvent(double lambda)` — draws an exponentially distributed sample via `-ln(U)/λ`
- `internalTransitionFor(String state)` — picks the earliest successor event, logs, and calls `holdIn(nextState, ...)`
- Output ports `outTimeInState` (String) and `outTimeInStateList` (ArrayList)

## Populating the rate matrix

Two equivalent APIs are provided:

### (a) `addTransitionInfo(state, successors[], rates[])` — preferred for hand-written models

```java
private void installRateMatrix() {
    // From "Up": fail at rate 0.01 → "Down"
    addTransitionInfo("Up",   new String[] { "Down" }, new double[] { 0.01 });
    // From "Down": repair at rate 0.1 → "Up"
    addTransitionInfo("Down", new String[] { "Up"   }, new double[] { 0.1  });
}
```

Call this from the constructor (after `super(name)` and after initializing `TransitionInfoList`).

### (b) `setInitialStateAndTransitionMatrix(MarkovMat mm)` — for loading from a `MarkovMat` object

Reserved for interop with XML-defined matrices or programmatically generated ones. Use (a) unless you already have a `MarkovMat`.

## Initializing in `initialize()`

Always set the RNG seed and the starting phase:

```java
@Override
public void initialize() {
    super.initialize();
    setRand(new Random(2349991L));
    holdIn("Up", 0.0);   // starting state, zero sigma triggers immediate internalTransitionFor
}
```

## Sigma comes from exponential sampling

You do not manage `sigma` directly in a Markov model — `internalTransitionFor(phase)` (called automatically) draws `timeToNextEvent(lambda)` per successor and `holdIn`s the earliest. Overriding `getTimeAdvance()` is unnecessary (the inherited one returns `sigma`).

## Caveat: rates vs probabilities

The field is named `ProbValue` on `TransitionInfo`, but in the continuous-time case it is treated as a **rate** (λ of an exponential, not a probability). Keep that in mind when interpreting values.
