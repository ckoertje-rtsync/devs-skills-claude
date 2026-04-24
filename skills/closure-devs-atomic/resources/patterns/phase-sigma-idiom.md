# Phase / sigma idiom (deterministic Atomic)

The base `com.ms4systems.devs.core.model.impl.Atomic` class does **not** declare `phase`, `sigma`, or the helper methods `holdIn`, `phaseIs`, `passivate`. Every deterministic subclass must declare them itself.

## Minimum instance fields

```java
private String phase = "passive";
private Double sigma = Double.POSITIVE_INFINITY;
```

## Required helper methods (copy verbatim)

```java
public void holdIn(String phase, Double sigma) {
    this.phase = phase;
    this.sigma = sigma;
}

protected void passivate() {
    passivateIn("passive");
}

protected void passivateIn(String phase) {
    holdIn(phase, Double.POSITIVE_INFINITY);
}

public boolean phaseIs(String phase) {
    return this.phase.equals(phase);
}

public String getPhase() {
    return phase;
}
```

## Interaction with `externalTransition`

In `externalTransition(double timeElapsed, MessageBag input)`, always decrement sigma first, *then* branch on inputs:

```java
sigma -= timeElapsed;
if (!input.getMessages(start).isEmpty()) {
    holdIn("active", 1.0);
}
```

Why: if no input causes a rephase, the model should continue toward its already-scheduled internal event, which requires `sigma` to reflect remaining time.

## Interaction with `getTimeAdvance`

`getTimeAdvance()` should typically be one line:

```java
@Override
public Double getTimeAdvance() {
    return sigma;
}
```

Avoid recomputing time advance from scratch — always reflect the state set by `holdIn`.
