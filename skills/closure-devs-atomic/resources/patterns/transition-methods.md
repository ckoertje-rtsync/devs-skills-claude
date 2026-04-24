# DEVS transition methods (exact Closure-DEVS signatures)

All four methods are declared on the base class. Override them with `@Override`.

## `void initialize()`

Called once at simulation start. Set initial state here and schedule the first internal event via `holdIn(phase, sigma)`. Always call `super.initialize()` first.

```java
@Override
public void initialize() {
    super.initialize();
    count = 0;
    holdIn("active", 2.0);
}
```

## `void internalTransition()`

Fires when `sigma` elapses. Mutate state and reschedule. Typical pattern: dispatch on `phase`.

```java
@Override
public void internalTransition() {
    if (phaseIs("active")) {
        count++;
        holdIn("active", period);      // or passivate(), or holdIn(otherPhase, ...)
        return;
    }
    if (phaseIs("other")) {
        passivate();
        return;
    }
}
```

## `void externalTransition(double timeElapsed, MessageBag input)`

Fires when external messages arrive before the scheduled internal event. Always decrement sigma first, then inspect input.

```java
@Override
public void externalTransition(double timeElapsed, MessageBag input) {
    sigma -= timeElapsed;  // remaining time in current phase
    if (!input.getMessages(start).isEmpty()) holdIn("active", period);
    if (!input.getMessages(stop ).isEmpty()) passivate();
}
```

**Invariant:** after `externalTransition` returns, `sigma` must be a valid non-negative time advance (or `Double.POSITIVE_INFINITY` for passive).

## `void confluentTransition(MessageBag input)`

Fires when internal and external events coincide. Default behavior is usable as-is; only override if the ordering matters for your model:

```java
@Override
public void confluentTransition(MessageBag input) {
    internalTransition();
    externalTransition(0, input);
}
```

## `Double getTimeAdvance()`

Returns remaining time until next internal event. Usually just returns the `sigma` field:

```java
@Override
public Double getTimeAdvance() {
    return sigma;
}
```

## `MessageBag getOutput()`

See `port-message-idiom.md`. Called immediately before `internalTransition()`.

## Method call order (one internal event cycle)

1. Scheduler advances simulation clock by `getTimeAdvance()`.
2. `getOutput()` returns messages for routing.
3. `internalTransition()` mutates state.
4. `getTimeAdvance()` queried again for next scheduling.
