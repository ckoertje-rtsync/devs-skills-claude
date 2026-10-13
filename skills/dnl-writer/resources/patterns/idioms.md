# DNL idioms

The shapes almost every atomic model is built from. Each is valid under the checker; combine them.

## 1. Output in response to an input (zero-time state)

Output happens only when a hold state's time elapses (User's Guide p. 163). To reply to an input, pass through a transient state:

```
to start passivate in Wait!
when in Wait and receive Hello go to Response!
hold in Response for time 0!
after Response output Hi!
from Response go to Wait!
```

Use a small positive time (`1.0`) instead of `0` when the reply should be delayed.

## 2. Periodic generator with a stop input

A hold state that loops on itself emits forever; an input moves it to a passive sink.

```
to start hold in generate for time 10!
after generate output Job!
from generate go to generate!
when in generate and receive Stop then go to passive!
passivate in passive!
```

Stop after N emissions from code instead:

```
use count with type int and default "0"!
use numberOfJobs with type int and default "50"!
internal event for generate
<%
    count++;
    if (count >= numberOfJobs) passivateIn("passive");
%>!
```

## 3. Processor with a data-dependent service time

Store the payload in the external event and override the hold time with `holdIn`; build the output from the stored payload.

```
accepts input on Job with type WorkToDo!
generates output on Job with type WorkToDo!
use storedJob with type WorkToDo and default "new WorkToDo()"!

to start passivate in waitForJob!
when in waitForJob and receive Job then go to sendJob!
hold in sendJob for time 50!
after sendJob output Job!
from sendJob go to waitForJob!

external event for waitForJob with Job
<%
    storedJob = (WorkToDo) messageList.get(0).getData();
    holdIn("sendJob", storedJob.getProcessingTime());
%>!
output event for sendJob
<%
    output.add(outJob, storedJob);
%>!
```

Inputs that arrive while in `sendJob` are ignored because no `when in sendJob …` line exists. Add one (`when in sendJob and receive Job go to sendJob eventually!` plus a queue variable) to buffer them.

## 4. Observer whose timer keeps running

A transducer counts arrivals for a fixed observation window. `eventually` keeps the remaining time instead of restarting it.

```
use observationTime with type double and default "10000"!
to start hold in observe for time "observationTime"!
after observe output Stop!
from observe go to done!
when in observe and receive Ariv then go to observe eventually!
when in observe and receive Solved then go to observe eventually!
passivate in done!
```

## 5. Record every port present in a bag, in any state

The state-specific part of an external event fires for one port only (file order wins). Phase-less `external event with M` blocks run for every port that has messages, in every state, before the state-specific handler. Pair them with a zero-time state so the record is visible.

```
accepts input on Start!
accepts input on Report!
use TStart with type double and default "Double.MAX_VALUE"!

to start passivate in waitForInput!
external event with Start
<%
    TStart = currentTime;
    holdIn("Record", 0.0);
%>!
external event with Report
<%
    System.out.println(getName() + " latency " + (currentTime - TStart));
    holdIn("Record", 0.0);
%>!
hold in Record for time 0!
from Record go to waitForInput!
```

The checker reports these blocks as `dnl.undocumented-form` (info): they are accepted by MS4 Me but absent from the guide.

## 6. A message class declared in the same file

```
generates output on Job with type WorkToDo!
WorkToDo has id, startTime and processingTime!
range of WorkToDo's id is int!
range of WorkToDo's startTime is double!
range of WorkToDo's processingTime is double!
use wtd with type WorkToDo!
initialize variables
<%
    wtd = new WorkToDo();
    wtd.setId(0);
%>!
```

Declare the class in exactly one `.dnl` of the project (MS4 Me writes `WorkToDo.java` from it); other models just use the type.

## 7. Hold time from a variable

```
use serviceTime with type double and default "2.5"!
hold in busy for time "serviceTime"!
```

The quoted text is pasted into `holdIn("busy", serviceTime)`, so it may be any Java expression over the variables (`"serviceTime * 2"`), but a bare variable name keeps the file readable and lets the checker verify it.

## 8. Choosing the next state in code

Preferred: one `from S go to N!` as the default, and `holdIn`/`passivateIn` in `internal event for S` for the other cases.

```
hold in testConnection for time 1!
from testConnection go to connected!
internal event for testConnection
<%
    if (!linkUp) passivateIn("notConnected");
%>!
```

Several `from S go to …` lines for one state are accepted by MS4 Me (warning `dnl.extra-internal-transition`); only tag code then decides. Reserve that form for files the Markov editor generated.

## 9. Terminal and sink states

A passive state with no `when in` lines is terminal. Use one to end a run cleanly (`done`, `passive`, `finished`).

## 10. Several messages on one port in a bag

`messageList.get(0)` reads one message. Iterate when a bag can carry more:

```
external event for waitForInput with Release
<%
    for (Message<?> msg : messageList) {
        IntEnt r = (IntEnt) msg.getData();
        total += r.getValue();
    }
%>!
```

## 11. Per-instance parameters from the model name

When an SES instantiates several copies, `getName()` distinguishes them:

```
initialize variables
<%
    if (getName().contains("Low")) moleculesPresent = 10;
%>!
```
