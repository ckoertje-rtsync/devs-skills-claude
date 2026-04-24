# Port / MessageBag idiom

## Declaring ports

Ports are declared as `public final` fields initialized via helper methods on the parent class. These calls must be on fields (not in the constructor before `super(name)`), because the parent registers them:

```java
public final Port start = addInputPort("start");
public final Port stop  = addInputPort("stop");
public final Port out   = addOutputPort("out", Job.class);
```

The 2-argument `addInputPort(name, Class)` and `addOutputPort(name, Class)` overloads record the expected message type for type-checking. The 1-argument form defaults to `Object`.

## Reading input (externalTransition)

Input arrives as a `MessageBag`. Two idioms:

```java
// Idiom 1 — presence check, no payload access:
if (!input.getMessages(start).isEmpty()) {
    // ...
}

// Idiom 2 — consume payload:
ArrayList<Message> msgs = input.getMessages(myPort);
if (!msgs.isEmpty()) {
    MyPayloadType data = (MyPayloadType) msgs.get(0).getData();
}
```

`input.hasMessages(port)` is a shorthand for `!input.getMessages(port).isEmpty()`.

## Emitting output (getOutput)

Build a fresh `MessageBag`, add one or more messages keyed by output port, return it:

```java
@Override
public MessageBag getOutput() {
    MessageBag output = new MessageBag();
    if (phaseIs("GenerateCall")) {
        output.add(out, new Job(count));
    }
    return output;
}
```

Alternative, equivalent:

```java
output.add(out.createMessage(new Job(count)));
```

## When getOutput fires

`getOutput()` is invoked **immediately before** the corresponding `internalTransition()` — the sigma countdown has expired, the output is sampled, then the internal transition mutates state. This is the canonical "output-then-transition" DEVS semantics. So output rules should be keyed on the *current* phase (the one about to leave), not the one it will transition into.
