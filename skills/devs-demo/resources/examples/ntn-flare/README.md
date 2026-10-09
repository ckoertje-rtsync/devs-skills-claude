# NTN Flare Demo

A local, self-contained page that replays the Phase I transport use case: one message crossing the
8-satellite ring from `Transmitter_GS` to `Receiver_GS` while a solar flare degrades the link at `X0Y1`,
at both resolutions of the constellation (`NTNBase`, 8 x `CellSAT` with a deterministic link budget, and
`NTNMarkov`, 8 x `SatCTM` with `P_Connect` and `Expect_Latency`), for the nine parameter instances of
`src/Models/json/json_parameters.json`. Every animation frame comes from a recorded Closure-DEVS run; nothing
is simulated in the browser. Background: `docs/morphism-quality.md`, Sections 3, 12, 14, 15 and 17.

## Open it

Double-click `demos/ntn-flare-demo.html`. It needs no server and no network: the page loads
`demos/data/demo-data.js` with a plain script tag, which works over `file://`. It is not published anywhere.

Tested in Firefox 140; the page uses only standard HTML, CSS and SVG (no libraries).

## Regenerate the data

```
sh ./gradlew buildDemoData                      # defaults: 200 replications, all instances, seed 2349991, flares inf,0.0001
sh ./gradlew buildDemoData -Preps=50 -Pefs=X_Band,Ka_Band -Pseed=7 -Pflares=inf,0.0001,1
```

`RunDemoData` (`src/Models/java/RunDemoData.java`) runs, for every (instance, resolution, flare time),
N untraced replications for the statistics panel and then replication 0 again with a
`DemoTraceRecorder` attached (`src/Models/java/DemoTraceRecorder.java`). Replication r uses seed
`seed + r`, as `RunMorphismQuality` does, so the traced run is the first sampled one; the task checks that
and that the flattened coupling table has 24 routes. It writes `demos/data/demo-data.json` and the same
object as `demos/data/demo-data.js`; commit both. Another bundle can be dropped on the page or chosen with
the file picker (`.json` or `.js`).

## Panels

**Scenario.** Instance, resolution, flare. The summary line shows the contract values the morphism
assigned to `SatCTM`, the link budget `CellSAT` derives from its ten parameters, and the result of the
traced run with the path the message took.

**Ring.** A polar view: Earth in the centre, the eight satellites on their orbit (clockwise is the `R`
direction, `outRSignal -> inLSignal`), the two ground stations on the Earth's rim under `X0Y0` and `X0Y2`,
the Sun on the `X0Y1` radial (`GenFlare`), and the frame components in the tray. Nodes are coloured by
phase category (passive, connecting, sending, waiting, failed; a green ring marks `Connected`). Message
tokens move along the links for the messages of the current step: filled circle = handshake request,
hollow circle = ack, square = data, diamond = Start/Report, orange = flare. Links turn blue once a hop is
acknowledged and dashed red when a request is never acknowledged. Hover or click a node for its phase, σ,
next event time and link-budget numbers.

**Replay.** Event-stepped: one step is one DEVS imminent set, so several steps can share the same `t`
(the Δt readout shows `+0`). Play, step, scrub, speed, and keyboard (Space, arrows, Home, End). "Stop at
delivery" pauses once at the Report; play again to see the tail, which is `X0Y2` waiting out its 10.0
handshake timeout. The step description lists every transition (δint, δext, δcon), the ports used and any
state variable that changed (the flare's added loss, the reroute's `preferredReceiver`, the Markov draw).

**Event log.** One row per step; click to jump. The flare and delivery rows are marked.

**Phase lanes.** Base and Markov traces for the same instance and flare, rows per model, columns per
step (uniform spacing; the time ticks are irregular by design), coloured by phase category, with message
arrows, a flare marker and a delivery marker. The active lane's cursor follows the replay; the other lane's
cursor is placed at the last column whose time is not after the active time, so the two resolutions can be
compared at equal simulation time. Click a column to jump (clicking the other lane switches resolution).

**Delivery time statistics.** For the chosen instance, Base (a deterministic point) against the Markov
replications (dots, quartile box, range, mean) for no flare and flare on a shared log axis, with the
success counts and the behavioral-distance point estimates computed in the page:
δT = |mean(Markov) − Base| / Base over successful runs (1 if only one side ever succeeds, 0 if neither),
δP = |P(success, Markov) − P(success, Base)|. These are the conventions of
`docs/morphism-quality.md` Section 11.2 without the confidence bounds. The nine small multiples share one
log axis; click one to select that instance. The last chart is the Phase I result from
`src/Models/txt/AggregatedData*.csv` (average TReport vs cost, 1000 MS4 Me runs per point, old
`SignalToInterference` physics, flare at TFlare = 1); it is shown for the record and is not comparable with
the traces above.

**Morphism.** The ten base parameters with units, the link budget derived from them (the page re-derives
it in JavaScript from `LinkBudget.java`'s formulas and the test checks it against the Java values), the JSON
targets with their relative difference, and the flare row: +3 dB on Base against ×2 on Markov, with a
sentence that says what that does to this instance.

## Caveats

- The trace is one replication (`meta.seed`); the statistics are `meta.reps` replications. For Mid_X a
  Markov hop fails in about 1 run in 300, so the traced run may differ from the typical one.
- `SatCTM`'s `Failed` state holds `Double.MAX_VALUE`, shown as ∞ and treated as passive.
- Flare handling is outside the morphism contract (finding B5): `CellSAT` adds `FlareLoss_dB` to its
  environment loss, `SatCTM` multiplies `Expect_Latency` by `LatencyDegredationFactor`.
- Ka_Band never delivers on the Markov side (`P_Connect` 2.2e-5, no retransmission, finding B11), while
  the Base side delivers in 6.5 and, with the flare, in 26.1 via the reverse path.
- Robust_X never connects on either side (`P_Connect` underflows to 0).
- For Mid_Ka and Fast_Ka the flare pulse at t = 1e-4 arrives after delivery, so it only shows in the tail.
- The legacy chart is Phase I data with the old physics and `TFlare = 1`.

## Verification

```
node demos/test/check-demo.js       # runs the page's pure functions against the bundle: paths, delivery
                                    # times, link budget vs Java, distances, formatting, offline guarantee
sh ./gradlew buildDemoData          # prints one summary line per scenario; routes must be 24
```

Manual checks in a browser: cycle the 36 scenarios; X_Band/Base/no flare delivers at 7.2e-4 in three
hops; Ka_Band/Base/flare reroutes counter-clockwise and delivers at about 26; Ka_Band/Markov shows `X0Y0`
in `Failed`; Robust_X leaves `X0Y0` in `waitForSignal` on both sides; no horizontal page scroll at
1280, 1024, 768 and 390 px; dark mode follows the OS setting.

## Files

```
demos/ntn-flare-demo.html        the page (inline CSS and JS)
demos/data/demo-data.js, .json   generated bundle
demos/test/check-demo.js         node check
src/Models/java/DemoTraceRecorder.java, RunDemoData.java
build.gradle                     task buildDemoData
```
