---
name: "DEVS Builder"
description: "Create Discrete Event System Specification (DEVS) models for any given system. Takes a brief description, autonomously infers the complete DEVS structure, generates an outline template, produces a runnable .java DEVS model, then validates and tests the model in the console. Use when modeling, simulating, or analyzing discrete event systems."
---

# DEVS Builder

## What This Skill Does

Builds complete DEVS (Discrete Event System Specification) models through a structured, concurrent workflow with semantic continuity across all phases.

## Workflow Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        MODEL CONTEXT (Persists Across Phases)           │
│  ┌──────────────┬──────────────┬──────────────┬──────────────────────┐  │
│  │ model_name   │ states       │ transitions  │ time_advances        │  │
│  │ description  │ inputs/outputs│ patterns    │ test_scenarios       │  │
│  │ hierarchy    │ test_case    │ syntax_check │ deployment           │  │
│  └──────────────┴──────────────┴──────────────┴──────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                         │
    ┌────────────────────────────────────┼────────────────────────────────┐
    ▼                                    │                                │
┌─────────┐                              │                                │
│ Phase 0 │  CONCURRENT:                 │                                │
│  SCAN   │  • Glob models/*.java        │                                │
│         │  • Glob core/**/*.java       │                                │
│         │  • Glob analytics/*.java     │                                │
└────┬────┘                              │                                │
     │ HANDOFF: catalog                  │                                │
     ▼                                   │                                │
┌─────────┐                              │                                │
│ Phase 1 │  CONCURRENT:                 │                                │
│ DISCOVER│  • Parse states from desc    │                                │
│         │  • Map ports (in/out)        │                                │
│         │  • Calc transitions          │                                │
└────┬────┘                              │                                │
     │ HANDOFF: analysis                 │                                │
     ▼                                   │                                │
┌─────────┐                              │                                │
│ Phase 2 │  CONCURRENT:                 │                                │
│ OUTLINE │  • Gen formal spec           │                                │
│         │  • Gen test scenarios        │                                │
│         │  • Write outline file        │                                │
│         │  • Output hierarchy diagram  │  ◄── NEW: Model composition    │
└────┬────┘                              │                                │
     │ HANDOFF: outline + hierarchy      │                                │
     ▼                                   │                                │
┌─────────┐                              │                                │
│ Phase 3 │  CONCURRENT:                 │                                │
│ GENERATE│  • Gen state enum            │                                │
│         │  • Gen DEVS methods          │                                │
│         │  • Gen simulator + main()    │                                │
│         │  • Gen self-contained test   │  ◄── NEW: Test case in main()  │
└────┬────┘                              │                                │
     │ HANDOFF: java file path + test    │                                │
     ▼                                   │                                │
┌─────────┐                              │                                │
│ Phase 4 │  CONCURRENT:                 │                                │
│ VALIDATE│  • javac compile             │                                │
│         │  • Run tests                 │                                │
│         │  • Verify transitions        │                                │
│         │  • Verify syntax correctness │  ◄── NEW: Syntax verification  │
└────┬────┘                              │                                │
     │ HANDOFF: validation results       │                                │
     ▼                                   │                                │
┌─────────┐                              │                                │
│ Phase 5 │  SEQUENTIAL:                 │                                │
│ DEPLOY  │  • Copy to MS4ME workspace   │                                │
│         │  • Verify deployment         │                                │
│         │  • Report final paths        │                                │
└─────────┘◄─────────────────────────────┴────────────────────────────────┘
                                  MODEL_CONTEXT flows through all phases
```

## Model Context Object

All phases read from and write to a shared **Model Context** that maintains semantic consistency:

```
MODEL_CONTEXT = {
  // From Phase 0 (Reference Scan)
  catalog: {
    models: [...],           // 36 reference DEVS models
    core: {...},             // Core framework interfaces
    analytics: {...},        // Distribution samplers
    markov: {...},           // Markov model support
    patterns: [...]          // Extracted model patterns
  },

  // From Phase 1 (System Discovery)
  model: {
    name: "",                // Model identifier
    description: "",         // Purpose statement
    goal: ""                 // Expected behavior
  },
  states: [...],             // Phase enum values
  inputs: [...],             // Input port definitions
  outputs: [...],            // Output port definitions
  transitions: {
    internal: [...],         // delta_int mappings
    external: [...]          // delta_ext mappings
  },
  time_advances: {...},      // ta() values per state
  lambda_outputs: {...},     // Output function mappings

  // From Phase 2 (Outline)
  outline_path: "",          // docs/[name]_DEVS_Outline.md
  hierarchy: {               // NEW: Model hierarchy and composition
    type: "",                // "Atomic" or "Coupled"
    root: "",                // Root model name
    components: [...],       // List of atomic model names (for coupled)
    couplings: {
      IC: [...],             // Internal couplings
      EIC: [...],            // External input couplings
      EOC: [...]             // External output couplings
    },
    depth: int,              // Hierarchy depth (1 for atomic, 2+ for coupled)
    diagram: ""              // ASCII/text hierarchy diagram
  },

  // From Phase 3 (Generation)
  java_path: "",             // src/[name]Model.java
  test_case: {               // NEW: Self-contained test case
    name: "",                // Test case name
    parameters: {...},       // Test parameters for each component
    assertions: [...],       // Expected behaviors to verify
    stop_conditions: [...],  // Conditions that halt simulation
    validation_rules: [...]  // Pass/fail criteria
  },

  // From Phase 4 (Validation)
  validation: {
    compiled: bool,
    tests_passed: int,
    tests_failed: int,
    syntax_check: {          // NEW: Syntactic correctness verification
      passed: bool,
      errors: [...],         // Compilation errors
      warnings: [...],       // Potential issues
      port_consistency: bool,// All port types match
      state_coverage: bool,  // All states reachable
      coupling_validity: bool// All couplings valid (for coupled models)
    }
  },

  // From Phase 5 (Deployment)
  deployment: {
    deployed: bool,
    target_dir: "",          // MS4ME workspace path
    deployed_files: [...],   // List of deployed file paths
    timestamp: ""            // Deployment timestamp
  }
}
```

## Prerequisites

- Java JDK 11+ installed and `javac`/`java` on PATH
- MS4Systems DEVS Framework (Basic-DEVS library JAR) on classpath
- A system or process to model (physical, computational, organizational, etc.)

## Java Project Structure

Generated models follow the MS4Systems Basic-DEVS project structure:

```
project/
├── src/
│   └── Models/
│       └── java/
│           ├── PhaseAtomic.java      # Base class (copy from resources)
│           ├── [SystemName]Model.java # Generated atomic model
│           └── [SystemName]System.java # Generated coupled model (if applicable)
├── lib/
│   └── basic-devs.jar                # MS4Systems framework JAR
└── docs/
    └── [SystemName]_DEVS_Outline.md  # Formal specification
```

**Package Convention:** All models use `package Models.java;` per MS4Systems convention.

## Reference Library

This skill includes a comprehensive reference library with 67 Java files organized into categories.

### Resource Paths (Relative to Skill Directory)

```
devs-builder/
└── resources/
    ├── models/          # 36 reference DEVS models
    │   ├── HelloWorldDEVS.java      # Minimal standalone DEVS
    │   ├── AtomicModelTemplate.java # Atomic model template
    │   ├── CoupledModelTemplate.java# Coupled model template
    │   ├── Genr.java                # Generator pattern
    │   ├── Proc.java                # Processor pattern
    │   ├── Transd.java              # Transducer pattern
    │   ├── GeneratorOfJobs.java     # Job generator
    │   ├── ProcessorOfJobs.java     # Job processor
    │   └── ... (28 more models)
    ├── core/            # Core DEVS framework
    │   ├── model/       # AtomicModel, CoupledModel interfaces
    │   ├── message/     # Port, MessageBag, Coupling
    │   └── simulation/  # Simulator, Coordinator
    ├── analytics/       # 15 files - Statistical distributions
    │   ├── SampleFromNormal.java
    │   ├── SampleFromExponential.java
    │   ├── SampleFromUniform.java
    │   ├── SampleFromPareto.java
    │   ├── SampleFromLogNormal.java
    │   └── ... (10 more)
    ├── markov/          # 6 files - Markov model support
    │   └── ContinuousTimeMarkov.java
    └── templates/       # Reusable templates
```

---

## Phase 0: Reference Library Scan (Automatic)

**Purpose:** Build the component catalog that Phase 1 and subsequent phases will reference.

**Executes:** Automatically on skill invocation, BEFORE user interaction.

### Concurrent Execution

Execute Steps 0.1, 0.2, and 0.3 **in parallel** using concurrent tool calls:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 0: SCAN                                │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Step 0.1       │  │ Step 0.2       │  │ Step 0.3       │ │
│  │ Glob: models/  │  │ Glob: core/    │  │ Glob: analytics/│ │
│  │ *.java         │  │ **/*.java      │  │ *.java         │ │
│  └───────┬────────┘  └───────┬────────┘  └───────┬────────┘ │
│          │                   │                   │           │
│          └───────────────────┼───────────────────┘           │
│                              ▼                                │
│                    ┌─────────────────┐                        │
│                    │ Step 0.4       │                        │
│                    │ Parse & Index  │                        │
│                    │ (sequential)   │                        │
│                    └───────┬────────┘                        │
│                            ▼                                  │
│                    ┌─────────────────┐                        │
│                    │ Step 0.5       │                        │
│                    │ Build Catalog  │                        │
│                    └───────┬────────┘                        │
│                            ▼                                  │
│              MODEL_CONTEXT.catalog = {...}                    │
└─────────────────────────────────────────────────────────────────┘
```

**Steps 0.1, 0.2, 0.3 (CONCURRENT):**
```
// Execute these Glob calls in a single message with parallel tool calls:
Glob: resources/models/*.java      → model_files[]
Glob: resources/core/**/*.java     → core_files[]
Glob: resources/analytics/*.java   → analytics_files[]
```

**Step 0.4 (Sequential, depends on 0.1-0.3):**
For each .java file, extract:
- Class name and inheritance hierarchy
- Input/Output port definitions
- State enum values
- Transition patterns (delta_int, delta_ext)
- Time advance patterns

**Step 0.5: Build Catalog → MODEL_CONTEXT.catalog**
```
MODEL_CONTEXT.catalog = {
  models: [
    { name: "HelloWorldDEVS", path: "resources/models/HelloWorldDEVS.java",
      pattern: "minimal", states: ["PASSIVE", "DONE"] },
    { name: "Genr", path: "resources/models/Genr.java",
      pattern: "generator", ports: ["out"] },
    { name: "Proc", path: "resources/models/Proc.java",
      pattern: "processor", ports: ["in", "out"] }
  ],
  templates: {
    "PhaseAtomic": {
      path: "resources/models/PhaseAtomic.java",
      extends: "AtomicModelImpl",
      features: ["holdIn(phase, sigma)", "passivate()", "passivateIn(phase)", "phaseIs(phase)", "getPhase()"],
      use_for: "Base class for all atomic models - MUST be copied to src/Models/java/",
      required: true  // Always copy this file when generating any model
    },
    "AtomicModelTemplate": {
      path: "resources/models/AtomicModelTemplate.java",
      extends: "PhaseAtomic",
      features: ["step-based state machine", "holdIn/passivate helpers", "MessageBag I/O"],
      use_for: "Single atomic models with phase-based transitions"
    },
    "CoupledModelTemplate": {
      path: "resources/models/CoupledModelTemplate.java",
      extends: "CoupledModelImpl",
      features: ["addChildModel", "addCoupling", "SimViewer integration", "SES mapping"],
      use_for: "Multi-component systems with port couplings"
    }
  },
  core: {
    "AtomicModel": {
      methods: ["getTimeAdvance", "internalTransition", "externalTransition",
                "confluentTransition", "getOutput", "initialize"],
      signature: "extends AtomicModelImpl"
    },
    "CoupledModel": {
      methods: ["addChildModel", "addCoupling", "getChildren", "getCouplings"],
      signature: "extends CoupledModelImpl"
    },
    "PhaseAtomic": {
      methods: ["holdIn", "passivate", "passivateIn", "phaseIs", "getPhase"],
      pattern: "phase-based state machine helper",
      state_vars: ["phase (String)", "sigma (Double)"]
    },
    "Port": { types: ["addInputPort", "addOutputPort"], generic: "Port<T>" },
    "MessageBag": { methods: ["add", "getMessages", "isEmpty"], impl: "MessageBagImpl" }
  },
  analytics: {
    "SampleFromNormal": { params: ["mean", "sigma"], method: "getSample()" },
    "SampleFromExponential": { params: ["mean"], method: "getSample()" },
    "SampleFromUniform": { params: ["min", "max"], method: "getSample()" }
  },
  patterns: [
    { type: "generator", components: ["Genr", "GeneratorOfJobs"], example: "Genr.java" },
    { type: "processor", components: ["Proc", "ProcessorOfJobs"], example: "Proc.java" },
    { type: "transducer", components: ["Transd", "Transducer"], example: "Transd.java" },
    { type: "coupled_system", components: ["ComputerSystem", "PITSystem"], example: "ComputerSystem.java" }
  ]
}
```

### Phase 0 → Phase 1 Handoff

```
HANDOFF_0_TO_1 = {
  catalog: MODEL_CONTEXT.catalog,
  similar_models: [...]  // Models matching user's domain (if detectable)
}
```

---

## Quick Start

When invoked, follow this exact sequence:

### Phase 1: System Discovery (Autonomous Inference)

The user provides a brief description of the system they want to model (one sentence to one paragraph). From that description, autonomously analyze the system and produce answers to all 12 DEVS modeling questions below. Do NOT ask the user these questions individually. Instead, use domain knowledge and reasoning to infer the complete DEVS structure, then present the full analysis to the user for confirmation.

### Concurrent Execution

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 1: DISCOVER                             │
│                                                                 │
│  Input: HANDOFF_0_TO_1 (catalog + similar_models)              │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Step 1.1: Collect user description (if not provided)    │   │
│  └────────────────────────┬────────────────────────────────┘   │
│                           ▼                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Step 1.2a  │ │ Step 1.2b  │ │ Step 1.2c  │ │ Step 1.2d│ │
│  │ Parse      │ │ Map ports  │ │ Calc       │ │ Identify │ │
│  │ states     │ │ (in/out)   │ │ transitions│ │ patterns │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ │
│        │              │              │              │        │
│        └──────────────┴──────────────┴──────────────┘        │
│                              ▼                                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ Step 1.3: Merge & present analysis for user confirmation│ │
│  └────────────────────────┬────────────────────────────────┘ │
│                           ▼                                   │
│            MODEL_CONTEXT.{model, states, inputs,              │
│                          outputs, transitions,                │
│                          time_advances, ...} = {...}          │
└─────────────────────────────────────────────────────────────────┘
```

**Steps 1.2a, 1.2b, 1.2c, 1.2d (CONCURRENT after 1.1):**

| Step | Input | Output |
|------|-------|--------|
| 1.2a | User description | States set (S) |
| 1.2b | User description | Input/Output ports (X, Y) |
| 1.2c | States + description | Transitions (δint, δext) + ta |
| 1.2d | `catalog.patterns` | Matched patterns, distribution hints |

**Step 1.1: Collect the user's brief description.**
If the user has not already provided one in their message, ask a single question using `AskUserQuestion`:
- "Briefly describe the system you want to model (e.g., 'a traffic light controller that cycles through green, yellow, and red phases with a pedestrian crossing button')."

**Step 1.2: Self-answer the 12 DEVS modeling questions.**
Analyze the user's description and infer answers to each of these. Apply domain knowledge, standard modeling conventions, and reasonable defaults. When multiple interpretations are possible, choose the most standard/canonical one.

1. **System name and description** - Extract or synthesize a clear model name and one-sentence purpose.
2. **Modeling goal** - Infer what behavior or property the simulation should demonstrate (e.g., correct phase sequencing, throughput analysis, protocol correctness).
3. **Atomic vs Coupled** - Determine from the description whether the system is a single component or a network. Default to Atomic DEVS unless the description explicitly mentions multiple interacting subsystems.
4. **State set** - Identify all distinct phases the system passes through. Use domain-standard naming (e.g., IDLE, BUSY, WAIT_ACK). Include passive/waiting states where applicable.
5. **Input set (X)** - Identify external events the system can receive. Name input ports using snake_case (e.g., `request_in`, `ack_received`). If the system is purely autonomous with no external triggers, set X = empty set.
6. **Output set (Y)** - Identify what the system emits. Name output ports using snake_case (e.g., `signal_out`, `job_done`).
7. **Internal transitions (delta_int)** - For each active state, determine the next state when the time advance expires.
8. **External transitions (delta_ext)** - For each state that can receive input, determine the resulting state for each input event.
9. **Time advance (ta)** - Assign durations to each state. Use reasonable domain-appropriate defaults (e.g., 30s for a green light, 5s for yellow). Use `INFINITY` for passive/waiting states that only transition on external input.
10. **Output function (lambda)** - Determine what output each state produces just before its internal transition fires. States that transition only via external input produce no output.
11. **Test scenarios** - Design 2-3 test scenarios that exercise: (a) the nominal/happy path through all states, (b) an external input interrupting a timed state, and (c) an edge case such as input arriving in a passive state or at a boundary time.
12. **Invariants and constraints** - Identify safety properties or logical constraints (e.g., mutual exclusion, bounded queues, no simultaneous conflicting outputs).

**Step 1.3: Present the complete analysis for user confirmation.**
Output all 12 self-answered questions in a clearly formatted block, then ask the user a single confirmation question using `AskUserQuestion`:

```
Based on your description, here is the DEVS model analysis:

Q1  System:              [inferred answer]
Q2  Modeling Goal:       [inferred answer]
Q3  Type:                [Atomic/Coupled]
Q4  States:              [list]
Q5  Inputs (X):          [list with port names]
Q6  Outputs (Y):         [list with port names]
Q7  Internal Transitions: [state -> state mappings]
Q8  External Transitions: [state + input -> state mappings]
Q9  Time Advances:       [state -> duration mappings]
Q10 Output Function:     [state -> output mappings]
Q11 Test Scenarios:      [2-3 scenarios with inputs/expected outputs]
Q12 Invariants:          [constraints]
```

Ask: "Does this analysis look correct, or would you like to adjust any of the answers above?"

Options:
- **Looks good, proceed** - Continue to Phase 2 with the inferred answers as-is.
- **Adjust** - User provides corrections; incorporate them and re-present only the changed items for final confirmation.

**Inference guidelines:**
- Prefer simplicity: use the fewest states that fully capture the described behavior.
- When the user mentions timing (e.g., "waits 10 seconds"), use those exact values. When no timing is given, choose domain-reasonable defaults and note them as assumed.
- When the description is ambiguous about whether something is an input or an internal transition, prefer modeling it as an external input (more flexible, testable).
- For coupled models, identify each subsystem as a separate atomic model and define the coupling topology.
- Always include at least one passive (INFINITY) state if the system can idle or wait for input.
- **Reference MODEL_CONTEXT.catalog.patterns** to identify similar model structures from the reference library.
- **Reference MODEL_CONTEXT.catalog.analytics** when stochastic timing is needed (exponential, normal, uniform distributions).

### Phase 1 → Phase 2 Handoff

```
HANDOFF_1_TO_2 = {
  // All of MODEL_CONTEXT from Phase 0 & 1
  catalog: MODEL_CONTEXT.catalog,
  model: MODEL_CONTEXT.model,
  states: MODEL_CONTEXT.states,
  inputs: MODEL_CONTEXT.inputs,
  outputs: MODEL_CONTEXT.outputs,
  transitions: MODEL_CONTEXT.transitions,
  time_advances: MODEL_CONTEXT.time_advances,
  lambda_outputs: MODEL_CONTEXT.lambda_outputs,
  test_scenarios: MODEL_CONTEXT.test_scenarios,
  invariants: MODEL_CONTEXT.invariants,

  // Pre-computed for Phase 2
  matched_patterns: [...],    // From catalog.patterns
  distribution_hints: {...}   // From catalog.analytics (if stochastic)
}
```

---

### Phase 2: DEVS Model Outline

**Depends on:** Full MODEL_CONTEXT from Phases 0 and 1

### Concurrent Execution

Execute Steps 2.1, 2.2, 2.3, and 2.4 **in parallel**:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 2: OUTLINE                             │
│                                                                 │
│  Input: HANDOFF_1_TO_2 (full MODEL_CONTEXT)                    │
│                                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Step 2.1   │ │ Step 2.2   │ │ Step 2.3   │ │ Step 2.4 │ │
│  │ Gen formal │ │ Gen test   │ │ Pre-compute│ │ Gen model│ │
│  │ spec       │ │ scenarios  │ │ Java maps  │ │ hierarchy│ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ │
│        │              │              │              │        │
│        └──────────────┴──────────────┴──────────────┘        │
│                              ▼                                │
│                    ┌─────────────────┐                        │
│                    │ Step 2.5       │                        │
│                    │ Assemble &     │                        │
│                    │ Write Outline  │                        │
│                    └───────┬────────┘                        │
│                            ▼                                  │
│              MODEL_CONTEXT.outline_path = "docs/..."          │
│              MODEL_CONTEXT.hierarchy = {...}                  │
└─────────────────────────────────────────────────────────────────┘
```

**Steps 2.1, 2.2, 2.3, 2.4 (CONCURRENT):**

| Step | Input | Output |
|------|-------|--------|
| 2.1 | `states`, `transitions`, `time_advances` | Formal M = <X,Y,S,δint,δext,λ,ta> |
| 2.2 | `test_scenarios`, `invariants` | Formatted test scenario blocks |
| 2.3 | `states`, `transitions` | `enum_states[]`, `switch_cases{}` for Phase 3 |
| 2.4 | `model_type`, `components`, `couplings` | Hierarchical composition diagram |

**Step 2.4: Generate Model Hierarchy Diagram (NEW)**

For **Coupled Models**, output a hierarchical composition diagram showing:
- The root coupled model
- All atomic model components
- Port connections (IC, EIC, EOC)
- Data flow direction

```
============================================================
MODEL HIERARCHY: [SystemName]System
============================================================

                    ┌─────────────────────────────────────┐
                    │         [SystemName]System          │
                    │         (Coupled Model)             │
                    │                                     │
                    │  External Ports:                    │
                    │    IN:  [port_list]                 │
                    │    OUT: [port_list]                 │
                    └───────────────┬─────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐         ┌───────────────┐         ┌───────────────┐
│ [Component1]  │         │ [Component2]  │         │ [Component3]  │
│ (Atomic)      │────────▶│ (Atomic)      │────────▶│ (Atomic)      │
│               │         │               │         │               │
│ States:       │         │ States:       │         │ States:       │
│  • [state1]   │         │  • [state1]   │         │  • [state1]   │
│  • [state2]   │         │  • [state2]   │         │  • [state2]   │
│               │         │               │         │               │
│ Ports:        │         │ Ports:        │         │ Ports:        │
│  IN:  [list]  │         │  IN:  [list]  │         │  IN:  [list]  │
│  OUT: [list]  │         │  OUT: [list]  │         │  OUT: [list]  │
└───────────────┘         └───────────────┘         └───────────────┘

COUPLINGS:
───────────
IC (Internal):
  • [Component1].out → [Component2].in
  • [Component2].out → [Component3].in

EIC (External Input):
  • [SystemName].external_in → [Component1].in

EOC (External Output):
  • [Component3].out → [SystemName].external_out

COMPOSITION SUMMARY:
────────────────────
  Model Type:     Coupled DEVS
  Hierarchy Depth: 2
  Total Atomics:   3
  Total Couplings: 5 (IC: 2, EIC: 1, EOC: 2)

============================================================
```

For **Atomic Models**, output a simpler structure:

```
============================================================
MODEL HIERARCHY: [ModelName]Model
============================================================

┌─────────────────────────────────────┐
│         [ModelName]Model            │
│         (Atomic Model)              │
│                                     │
│  States:                            │
│    • [STATE1] (ta = X.X)            │
│    • [STATE2] (ta = Y.Y)            │
│    • [STATE3] (ta = INFINITY)       │
│                                     │
│  Ports:                             │
│    IN:  [port1], [port2]            │
│    OUT: [port3], [port4]            │
│                                     │
│  Transitions:                       │
│    δint: [STATE1] → [STATE2]        │
│    δext: [STATE3] + input → [STATE1]│
│                                     │
└─────────────────────────────────────┘

COMPOSITION SUMMARY:
────────────────────
  Model Type:     Atomic DEVS
  Hierarchy Depth: 1
  Total States:   3
  Total Ports:    4 (IN: 2, OUT: 2)

============================================================
```

**Hierarchy stored in MODEL_CONTEXT:**
```
MODEL_CONTEXT.hierarchy = {
  type: "Coupled",                    // or "Atomic"
  root: "[SystemName]System",
  components: [
    { name: "[Component1]", class: "[Component1]Model", states: [...], ports: {...} },
    { name: "[Component2]", class: "[Component2]Model", states: [...], ports: {...} }
  ],
  couplings: {
    IC: [{ source: "[Component1].out", sink: "[Component2].in" }],
    EIC: [{ source: "[SystemName].ext_in", sink: "[Component1].in" }],
    EOC: [{ source: "[Component2].out", sink: "[SystemName].ext_out" }]
  },
  depth: 2,
  diagram: "[ASCII diagram string]"
}
```

After the user confirms the inferred analysis (or provides adjustments), generate the formal outline. Save it to `docs/` directory (create if needed).

```
============================================================
DEVS MODEL OUTLINE: [System Name]
============================================================

1. MODEL IDENTIFICATION
   - Name:        [SystemName]Model
   - Type:        Atomic DEVS / Coupled DEVS
   - Purpose:     [Modeling goal from Q2]
   - Formalism:   Basic DEVS (Zeigler, 2000)

2. FORMAL SPECIFICATION (Atomic DEVS)
   M = <X, Y, S, delta_int, delta_ext, lambda, ta>

   2.1 Input Set (X):
       Port: [port_name] -> Type: [event_type]
       ...

   2.2 Output Set (Y):
       Port: [port_name] -> Type: [event_type]
       ...

   2.3 State Set (S):
       State: [state_name] | Description: [what this state represents]
       ...
       Phase variable:  phase in {[state1], [state2], ...}
       Sigma variable:  sigma in R+ union {INFINITY}

   2.4 Internal Transition Function (delta_int):
       [state1] -> [state2]
       ...

   2.5 External Transition Function (delta_ext):
       ([state], [input_port], [event]) -> [new_state]
       ...

   2.6 Output Function (lambda):
       [state] -> ([output_port], [value])
       ...
       (Fires immediately before delta_int)

   2.7 Time Advance Function (ta):
       ta([state1]) = [duration]
       ta([state2]) = [duration]
       ...
       ta([passive_state]) = INFINITY

3. COUPLED DEVS STRUCTURE (if applicable)
   N = <X, Y, D, {M_d}, {I_d}, {Z_ij}, Select>

   3.1 Component Set (D):
       [component1]: [AtomicModel1]
       [component2]: [AtomicModel2]
       ...

   3.2 Coupling Specification:
       External Input Coupling (EIC):
           N.[input_port] -> [component].[port]
       Internal Coupling (IC):
           [component1].[output_port] -> [component2].[input_port]
       External Output Coupling (EOC):
           [component].[output_port] -> N.[output_port]

   3.3 Tie-Breaking / Select Function:
       Priority: [component ordering for simultaneous events]

4. TEST SCENARIOS
   4.1 Scenario: [Name]
       Initial State: [state]
       Input Sequence:
         t=0:   [event]
         t=X:   [event]
       Expected Output:
         t=Y:   [output]
       Expected Final State: [state]

   4.2 Scenario: [Name]
       ...

5. INVARIANTS AND CONSTRAINTS
   - [Constraint 1]
   - [Constraint 2]

============================================================
```

### Phase 2 → Phase 3 Handoff

```
HANDOFF_2_TO_3 = {
  // Full MODEL_CONTEXT
  ...MODEL_CONTEXT,

  // Generated artifacts
  outline_path: "docs/[name]_DEVS_Outline.md",

  // NEW: Model hierarchy (from Step 2.4)
  hierarchy: MODEL_CONTEXT.hierarchy,

  // Model type selection (determined by Q3 Type)
  model_type: "Atomic" | "Coupled",
    // Atomic = Single component (extends PhaseAtomic, uses AtomicModelTemplate.java)
    // Coupled = Multi-component (extends CoupledModelImpl, uses CoupledModelTemplate.java)

  // Template path
  template_path: "resources/models/AtomicModelTemplate.java"  // or CoupledModelTemplate.java

  // Pre-computed for Phase 3
  class_name: "[SystemName]Model",
  phase_strings: [...],         // Phase strings for holdIn() (e.g., "idle", "active")
  port_definitions: [...],      // Input/output port code
  ta_switch_cases: [...],       // Time advance switch body
  lambda_switch_cases: [...],   // Output function switch body
  delta_int_cases: [...],       // Internal transition code
  delta_ext_cases: [...],       // External transition code

  // For Coupled models
  components: [...],            // List of atomic model instances
  couplings: {
    IC: [...],                  // Internal couplings
    EIC: [...],                 // External input couplings
    EOC: [...]                  // External output couplings
  },

  // NEW: Test case specification (for Phase 3 to implement)
  test_case_spec: {
    parameters: {...},          // Suggested test parameters
    assertions: [...],          // Expected behaviors
    stop_conditions: [...]      // Simulation termination criteria
  }
}
```

---

### Phase 3: Java Code Generation

**Depends on:** Full MODEL_CONTEXT from Phases 0, 1, and 2

### Concurrent Execution

Execute Steps 3.1, 3.2, 3.3, and 3.4 **in parallel**:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 3: GENERATE                            │
│                                                                 │
│  Input: HANDOFF_2_TO_3 (full MODEL_CONTEXT + pre-computed)     │
│                                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Step 3.1   │ │ Step 3.2   │ │ Step 3.3   │ │ Step 3.4 │ │
│  │ Gen phases │ │ Gen DEVS   │ │ Copy Phase │ │ Gen test │ │
│  │ + ports    │ │ methods    │ │ Atomic     │ │ case     │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ │
│        │              │              │              │        │
│        └──────────────┴──────────────┴──────────────┘        │
│                              ▼                                │
│                    ┌─────────────────┐                        │
│                    │ Step 3.5       │                        │
│                    │ Assemble &     │                        │
│                    │ Write Java     │                        │
│                    └───────┬────────┘                        │
│                            ▼                                  │
│              MODEL_CONTEXT.java_path = "src/..."              │
│              MODEL_CONTEXT.test_case = {...}                  │
└─────────────────────────────────────────────────────────────────┘
```

**Steps 3.1, 3.2, 3.3, 3.4 (CONCURRENT):**

| Step | Input | Output |
|------|-------|--------|
| 3.1 | `phase_strings`, `inputs`, `outputs` | Port declarations, holdIn() phase names |
| 3.2 | `ta_switch_cases`, `delta_int_cases`, `delta_ext_cases`, `lambda_cases` | DEVS method bodies using phaseIs() |
| 3.3 | `model_type` | Copy PhaseAtomic.java + generate main() with SimulationImpl |
| 3.4 | `test_case_spec`, `hierarchy`, `components` | Self-contained test case in main() |

**Step 3.3 Detail - Required Files:**
- **Always copy:** `PhaseAtomic.java` from `resources/models/` to `src/Models/java/`
- **For Atomic:** Generate `[Name]Model.java` with main() using `SimulationImpl`
- **For Coupled:** Generate `[Name]System.java` + all component `[Comp]Model.java` files

---

**Step 3.4: Generate Self-Contained Test Case (NEW)**

For **Coupled Models**, generate a comprehensive test case in the main() method that:
- Configures parameters for all component atomic models
- Tracks key state variables during simulation
- Implements stop conditions (e.g., stop if inventory < 0)
- Validates expected behaviors with pass/fail reporting

**Test Case Template for Coupled Models:**

```java
// ── Main: Simulation Entry Point with Test Case ───────────
public static void main(String[] args) throws ClassNotFoundException {
    System.out.println("╔════════════════════════════════════════════════════════════╗");
    System.out.println("║     [SystemName] DEVS Simulation - TEST CASE               ║");
    System.out.println("╚════════════════════════════════════════════════════════════╝");
    System.out.println();

    // ══════════════════════════════════════════════════════════════
    // TEST CASE PARAMETERS
    // ══════════════════════════════════════════════════════════════

    // [Component1] Parameters
    final [type1] [PARAM1] = [value1];
    final [type2] [PARAM2] = [value2];

    // [Component2] Parameters
    final [type3] [PARAM3] = [value3];
    final [type4] [PARAM4] = [value4];

    // Simulation Parameters
    final int MAX_ITERATIONS = [N];
    final double MAX_SIMULATION_TIME = [T];

    // Print parameter table
    System.out.println("┌────────────────────────────────────────────────────────────┐");
    System.out.println("│                    TEST CASE PARAMETERS                    │");
    System.out.println("├────────────────────────────────────────────────────────────┤");
    // ... parameter output ...
    System.out.println("└────────────────────────────────────────────────────────────┘");

    // Build coupled model with test case parameters
    [SystemName]System top = new [SystemName]System("TestCase",
        [PARAM1], [PARAM2], [PARAM3], [PARAM4]);
    top.initialize();

    // SES mapping
    sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
    System.out.println(ses.printTreeString());

    // Simulation options
    SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);

    if (options.isDisableViewer()) {
        runTestCaseSimulation(top, MAX_ITERATIONS, MAX_SIMULATION_TIME, options);
    } else {
        SimViewer viewer = new SimViewer();
        viewer.open(top, options);
    }
}

/**
 * Run the test case simulation with monitoring and validation.
 */
private static void runTestCaseSimulation([SystemName]System top, int maxIterations,
                                          double maxTime, SimulationOptionsImpl options) {
    System.out.println("╔════════════════════════════════════════════════════════════╗");
    System.out.println("║                STARTING TEST CASE SIMULATION               ║");
    System.out.println("╚════════════════════════════════════════════════════════════╝");

    SimulationImpl sim = new SimulationImpl("Test Case", top, options);
    sim.startSimulation(0);

    // Tracking variables
    int iteration = 0;
    boolean stopConditionMet = false;
    String stopReason = "Max iterations reached";

    // Print header
    System.out.println("┌──────────┬────────────┬────────────┬────────────┬───────────┐");
    System.out.println("│ Iter     │ Sim Time   │ [State1]   │ [State2]   │ Status    │");
    System.out.println("├──────────┼────────────┼────────────┼────────────┼───────────┤");

    // Simulation loop with monitoring
    while (iteration < maxIterations && !stopConditionMet) {
        iteration++;
        sim.simulateIterations(1);

        double simTime = sim.getSimulationTime();

        // Get current state from components
        [type] state1 = top.get[Component1]().get[StateVar1]();
        [type] state2 = top.get[Component2]().get[StateVar2]();

        // Check stop conditions
        if ([STOP_CONDITION_1]) {
            stopConditionMet = true;
            stopReason = "[STOP_REASON_1]";
        }
        if ([STOP_CONDITION_2]) {
            stopConditionMet = true;
            stopReason = "[STOP_REASON_2]";
        }

        // Print progress (every N iterations or on events)
        if (iteration % 5 == 0 || stopConditionMet) {
            printIterationRow(iteration, simTime, state1, state2,
                              stopConditionMet ? "STOPPED" : "RUNNING");
        }

        if (simTime >= maxTime) {
            stopReason = "Max simulation time reached";
            break;
        }
    }

    System.out.println("└──────────┴────────────┴────────────┴────────────┴───────────┘");

    // ══════════════════════════════════════════════════════════════
    // TEST CASE VALIDATION
    // ══════════════════════════════════════════════════════════════
    System.out.println();
    System.out.println("╔════════════════════════════════════════════════════════════╗");
    System.out.println("║                    TEST CASE RESULTS                       ║");
    System.out.println("╚════════════════════════════════════════════════════════════╝");

    System.out.println("Stop Reason: " + stopReason);
    System.out.println();

    boolean testPassed = true;

    // Validation 1: [ASSERTION_1]
    if ([ASSERTION_1_CONDITION]) {
        System.out.println("✓ PASS: [ASSERTION_1_DESCRIPTION]");
    } else {
        System.out.println("✗ FAIL: [ASSERTION_1_DESCRIPTION]");
        testPassed = false;
    }

    // Validation 2: [ASSERTION_2]
    if ([ASSERTION_2_CONDITION]) {
        System.out.println("✓ PASS: [ASSERTION_2_DESCRIPTION]");
    } else {
        System.out.println("✗ FAIL: [ASSERTION_2_DESCRIPTION]");
        testPassed = false;
    }

    // Final result
    System.out.println();
    if (testPassed) {
        System.out.println("★★★ ALL TESTS PASSED ★★★");
    } else {
        System.out.println("✗✗✗ SOME TESTS FAILED ✗✗✗");
    }
}

private static void printIterationRow(int iter, double time,
                                       [type] val1, [type] val2, String status) {
    System.out.println(String.format("│ %-8d │ %10.2f │ %10s │ %10s │ %-9s │",
                       iter, time, val1, val2, status));
}
```

**Test Case Configuration in MODEL_CONTEXT:**

```
MODEL_CONTEXT.test_case = {
  name: "[SystemName]_TestCase",
  parameters: {
    "[Component1]": {
      "[param1]": { type: "int", value: 10, description: "..." },
      "[param2]": { type: "double", value: 5.0, description: "..." }
    },
    "[Component2]": {
      "[param3]": { type: "int", value: 100, description: "..." }
    },
    "simulation": {
      "max_iterations": 100,
      "max_time": 168.0
    }
  },
  assertions: [
    { name: "State1 >= 0", condition: "state1 >= 0", description: "State1 should never go negative" },
    { name: "Reports collected", condition: "reports > 0", description: "At least one report" }
  ],
  stop_conditions: [
    { condition: "state1 < 0", reason: "State1 went negative" },
    { condition: "simTime >= maxTime", reason: "Max simulation time reached" }
  ],
  validation_rules: [
    "All state variables within expected bounds",
    "All couplings functioning (data flows between components)",
    "System reaches expected steady-state or termination"
  ]
}
```

**For Atomic Models**, generate a simpler test case:

```java
public static void main(String[] args) {
    System.out.println("=== [ModelName] DEVS Model Test ===");

    // Create model with test parameters
    [ModelName]Model model = new [ModelName]Model("TestModel");
    model.initialize();

    // Run simulation
    SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
    SimulationImpl sim = new SimulationImpl("[ModelName] Test", model, options);
    sim.startSimulation(0);

    // Test: Execute N iterations and verify final state
    sim.simulateIterations([N]);

    // Validate
    boolean passed = true;
    if (model.phaseIs("[EXPECTED_FINAL_PHASE]")) {
        System.out.println("✓ PASS: Final phase is [EXPECTED_FINAL_PHASE]");
    } else {
        System.out.println("✗ FAIL: Expected phase [EXPECTED_FINAL_PHASE], got " + model.getPhase());
        passed = false;
    }

    System.out.println(passed ? "★★★ TEST PASSED ★★★" : "✗✗✗ TEST FAILED ✗✗✗");
}
```

**Template Selection Based on System Type:**

| System Type | Template | Base Class | Output Files |
|-------------|----------|------------|--------------|
| Simple (single component) | `AtomicModelTemplate.java` | `PhaseAtomic` | `src/[Name]Model.java` |
| Complex (multiple components) | `CoupledModelTemplate.java` + `AtomicModelTemplate.java` | `CoupledModelImpl` + `PhaseAtomic` | `src/[Name]System.java` + `src/[Component]Model.java` (×N) |

**All models use framework templates.** Read the template file, perform placeholder substitution with values from MODEL_CONTEXT.

**Reference catalog.templates for code generation:**
- `AtomicModelTemplate.java` - Phase-based atomic with ports (extends PhaseAtomic)
- `CoupledModelTemplate.java` - Multi-component system (extends CoupledModelImpl)

**Reference catalog.models for patterns:**
- `Genr.java` - Generator pattern (autonomous output)
- `Proc.java` - Processor pattern (input-output transformation)
- `Transd.java` - Transducer pattern (statistics collection)
- `PhaseAtomic.java` - Base class with holdIn(), passivate() helpers

---

## DEVS Model Hierarchy

All DEVS models follow a hierarchical structure using the framework templates:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DEVS MODEL HIERARCHY                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │              COUPLED MODEL (CoupledModelTemplate.java)           │   │
│  │                     extends CoupledModelImpl                     │   │
│  │                                                                  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │   │
│  │  │ AtomicModel1 │  │ AtomicModel2 │  │ AtomicModel3 │   ...    │   │
│  │  │ (PhaseAtomic)│  │ (PhaseAtomic)│  │ (PhaseAtomic)│          │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │   │
│  │         │                 │                 │                   │   │
│  │         └────── COUPLINGS (IC/EIC/EOC) ─────┘                   │   │
│  │                                                                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              OR                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │              ATOMIC MODEL (AtomicModelTemplate.java)             │   │
│  │                       extends PhaseAtomic                        │   │
│  │                   (for simple single-component)                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Decision Logic (Phase 1, Q3):

| System Description | Model Type | Templates Used |
|--------------------|------------|----------------|
| Single component, one behavior | **Atomic** | `AtomicModelTemplate.java` only |
| Multiple interacting components | **Coupled** | `CoupledModelTemplate.java` + multiple `AtomicModelTemplate.java` |
| "System", "network", "pipeline" | **Coupled** | `CoupledModelTemplate.java` + multiple `AtomicModelTemplate.java` |
| Producer-consumer, client-server | **Coupled** | `CoupledModelTemplate.java` + multiple `AtomicModelTemplate.java` |

### Default: Complex Systems → Coupled Model

When the system involves:
- Multiple distinct behaviors or components
- Data flow between components
- Any mention of "system", "network", "workflow", "pipeline"
- Producer/consumer, client/server, or similar patterns

**Always generate as Coupled Model** with each component as a separate Atomic Model.

---

## PhaseAtomic Base Class (Required)

**Source:** `resources/models/PhaseAtomic.java`

This base class MUST be copied to `src/Models/java/` before generating any atomic model.
It provides the phase-based state machine helpers used by all atomic models.

```java
package Models.java;

import com.ms4systems.devs.core.model.impl.AtomicModelImpl;

/**
 * PhaseAtomic - Base class for phase-based atomic DEVS models.
 * Provides holdIn/passivate helpers for managing phase and sigma.
 */
class PhaseAtomic extends AtomicModelImpl {
    protected String phase = "passive";
    protected Double sigma = Double.POSITIVE_INFINITY;

    public PhaseAtomic() { this("PhaseAtomic"); }
    public PhaseAtomic(String nm) { super(nm); }

    public String getPhase() { return phase; }
    public boolean phaseIs(String phase) { return this.phase.equals(phase); }

    public void holdIn(String phase, double sigma) {
        this.phase = phase;
        this.sigma = sigma;
    }

    public void passivateIn(String phase) {
        this.phase = phase;
        this.sigma = Double.POSITIVE_INFINITY;
    }

    public void passivate() {
        this.phase = "passive";
        this.sigma = Double.POSITIVE_INFINITY;
    }
}
```

**Inheritance Hierarchy:**
```
AtomicModelImpl (MS4Systems framework)
    └── PhaseAtomic (phase/sigma helpers)
            └── [YourModel]Model (generated atomic model)
```

---

## Atomic Model Template (AtomicModelTemplate.java)

**Source:** `resources/models/AtomicModelTemplate.java`

Use this when integrating with the MS4 DEVS framework. Extends `PhaseAtomic` for cleaner
phase-based state machines with `holdIn()`, `passivate()`, and `phaseIs()` helpers.

**DEVS Method Mapping (Formal → Framework):**

| DEVS Formal | Framework (PhaseAtomic) | Return Type |
|------------|-------------------------|-------------|
| `timeAdvance()` | `getTimeAdvance()` | `Double` |
| `deltaInt()` | `internalTransition()` | `void` |
| `deltaExt(e, input)` | `externalTransition(double timeElapsed, MessageBag input)` | `void` |
| `lambda()` | `getOutput()` | `MessageBag` |
| — | `confluentTransition(MessageBag input)` | `void` |
| — | `initialize()` | `void` |

**PhaseAtomic Helper Methods:**

| Method | Description |
|--------|-------------|
| `holdIn(phase, sigma)` | Set phase and schedule next internal transition |
| `passivate()` | Set phase="passive", sigma=INFINITY |
| `passivateIn(phase)` | Set custom phase with sigma=INFINITY |
| `phaseIs(phase)` | Check if current phase matches |
| `getPhase()` | Return current phase string |

### AtomicModelTemplate.java

```java
//Package Declarations; DO NOT MODIFY
package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DEVS Model: [SystemName]
 * Generated by DEVS Builder Skill (Framework Mode)
 *
 * Formalism: Basic DEVS (Zeigler, 2000)
 * M = <X, Y, S, delta_int, delta_ext, lambda, ta>
 */
public class [SystemName]Model extends PhaseAtomic {

    private static final long serialVersionUID = 1L;

    // ── Output Ports ─────────────────────────────────────────
    // Modify port names and types as needed
    public final Port<String> [outPortName1];    // Output port 1
    public final Port<String> [outPortName2];    // Output port 2

    // ── Input Ports ──────────────────────────────────────────
    // Modify port names and types as needed
    public final Port<String> [inPortName1];     // Input port 1
    public final Port<String> [inPortName2];     // Input port 2

    // ── Step counter for multi-step phases ───────────────────
    private int step = 0;

    // ── Constructor ──────────────────────────────────────────
    public [SystemName]Model(String name) {
        super(name);

        // Define all input and output ports
        [outPortName1] = addOutputPort("[outPortName1]", String.class);
        [outPortName2] = addOutputPort("[outPortName2]", String.class);

        [inPortName1] = addInputPort("[inPortName1]", String.class);
        [inPortName2] = addInputPort("[inPortName2]", String.class);
    }

    @Override
    public void initialize() {
        super.initialize();
        step = 0;
        holdIn("[INITIAL_PHASE]", [INITIAL_TA]);  // e.g., holdIn("active", 1.0)
    }

    @Override
    public Double getTimeAdvance() {
        return sigma;
    }

    @Override
    public MessageBag getOutput() {
        if (!phaseIs("[ACTIVE_PHASE]")) return MessageBag.EMPTY;

        MessageBagImpl out = new MessageBagImpl();
        switch (step) {
            case 0:
                out.add([outPortName1], "[OUTPUT_DATA_STEP_0]");
                break;
            case 1:
                out.add([outPortName2], "[OUTPUT_DATA_STEP_1]");
                break;
            default:
                break;
        }
        return out;
    }

    @Override
    public void internalTransition() {
        int numSteps = [NUM_STEPS];  // Total steps in active phase
        double ta = [TIME_ADVANCE];  // Time advance per step

        if (phaseIs("[ACTIVE_PHASE]")) {
            step++;
            if (step < numSteps) {
                holdIn("[ACTIVE_PHASE]", ta);  // Schedule next step
            } else {
                passivate();  // Done, go passive
            }
        }
    }

    @Override
    public void externalTransition(double timeElapsed, MessageBag input) {
        List<String> port1Messages = getMessages(input, [inPortName1]);
        List<String> port2Messages = getMessages(input, [inPortName2]);

        if (!port1Messages.isEmpty()) {
            System.out.println("[[SystemName]] Received on [inPortName1]: " + port1Messages.get(0));
            // Process input and update state
            holdIn("[ACTIVE_PHASE]", [PROCESSING_TIME]);
        }
        if (!port2Messages.isEmpty()) {
            System.out.println("[[SystemName]] Received on [inPortName2]: " + port2Messages.get(0));
            // Process input and update state
        }
    }

    @Override
    public void confluentTransition(MessageBag input) {
        internalTransition();
        externalTransition(0.0, input);
    }

    // ── Helper: Extract messages from a port ─────────────────
    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
}
```

**Code Generation Rules for Atomic Models:**
- Replace `[SystemName]` with the model name (e.g., `TrafficLight`)
- Replace `[outPortName1]`, `[inPortName1]`, etc. with actual port names
- Replace `[INITIAL_PHASE]` with the starting phase (e.g., `"idle"`, `"green"`)
- Replace `[INITIAL_TA]` with initial time advance (e.g., `0.0` or `30.0`)
- Replace `[ACTIVE_PHASE]` with the active processing phase name
- Replace `[NUM_STEPS]` with number of steps in the phase (or remove step logic if single-step)
- Replace `[TIME_ADVANCE]` and `[PROCESSING_TIME]` with actual durations

---

## Coupled Model Template (CoupledModelTemplate.java)

**Source:** `resources/models/CoupledModelTemplate.java`

Use this when building multi-component systems with port couplings between atomic models.
Includes SimViewer integration and SES (System Entity Structure) mapping.

### CoupledModelTemplate.java

```java
//Package Declarations; DO NOT MODIFY
package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;

/**
 * Coupled DEVS Model: [SystemName]
 * Generated by DEVS Builder Skill (Framework Mode)
 *
 * Formalism: Coupled DEVS (Zeigler, 2000)
 * N = <X, Y, D, {M_d}, {I_d}, {Z_ij}, Select>
 */
public class [SystemName]System extends CoupledModelImpl {
    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    public [SystemName]System() {
        this("[SystemName]System");
    }

    public [SystemName]System(String name) {
        super(name);

        // ── Component Instantiation ──────────────────────────
        // Create atomic model instances
        [AtomicModel1] [component1] = new [AtomicModel1]("[Component1Name]");
        [AtomicModel2] [component2] = new [AtomicModel2]("[Component2Name]");

        // Add components to coupled model
        addChildModel([component1]);
        addChildModel([component2]);

        // ── Internal Couplings (IC) ──────────────────────────
        // [component1].output -> [component2].input
        addCoupling([component1].getOutputPort("[outPortName]"),
                    [component2].getInputPort("[inPortName]"));

        // [component2].output -> [component1].input (feedback)
        addCoupling([component2].getOutputPort("[outPortName]"),
                    [component1].getInputPort("[inPortName]"));

        // ── External Input Couplings (EIC) ───────────────────
        // Connect coupled model input ports to component inputs
        // addCoupling(this.getInputPort("external_in"), [component1].getInputPort("in"));

        // ── External Output Couplings (EOC) ──────────────────
        // Connect component outputs to coupled model output ports
        // addCoupling([component2].getOutputPort("out"), this.getOutputPort("external_out"));
    }

    // ── Main: Simulation Entry Point ─────────────────────────
    public static void main(String[] args) throws ClassNotFoundException {
        // Build coupled model
        [SystemName]System top = new [SystemName]System();
        top.initialize();

        // SES mapping (System Entity Structure)
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println("=== SES Tree ===");
        System.out.println(ses.printTreeString());

        sesRelationExtend rses = new sesRelationExtend(ses);
        CoupledModelImpl cm = myDEVSToSES.pruneNTransformForInstance(rses);
        System.out.println("=== PlantUML Coupled Model ===");
        System.out.println(myDEVSToSES.mapCoupledToPlantUML(cm));

        // Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        top.options = options;

        if (options.isDisableViewer()) {
            // Console mode
            SimulationImpl sim = new SimulationImpl("[SystemName] Simulation", top, options);
            sim.startSimulation(0);
            sim.simulateIterations(20);
            System.out.println("Simulation complete.");
        } else {
            // GUI mode with SimViewer
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }
}
```

**Code Generation Rules for Coupled Models:**
- Replace `[SystemName]` with the coupled model name (e.g., `ComputerSystem`)
- Replace `[AtomicModel1]`, `[AtomicModel2]` with actual atomic model class names
- Replace `[component1]`, `[component2]` with instance variable names
- Replace `[Component1Name]`, `[Component2Name]` with string names for instances
- Replace port names in `addCoupling()` calls with actual port names
- Add EIC/EOC couplings if the coupled model has external ports
- Adjust `simulateIterations(N)` for desired simulation length

**Coupling Types:**

| Type | Description | Example |
|------|-------------|---------|
| IC (Internal) | Component → Component | `addCoupling(genr.out, proc.in)` |
| EIC (External Input) | Coupled.in → Component.in | `addCoupling(this.getInputPort("ext_in"), comp.in)` |
| EOC (External Output) | Component.out → Coupled.out | `addCoupling(comp.out, this.getOutputPort("ext_out"))` |

### Phase 3 → Phase 4 Handoff

```
HANDOFF_3_TO_4 = {
  // Full MODEL_CONTEXT from all previous phases
  ...MODEL_CONTEXT,

  // Generated artifacts from Phase 3
  java_path: "src/Models/java/[SystemName]Model.java",  // or [SystemName]System.java
  generated_files: [
    "PhaseAtomic.java",           // Base class (copied)
    "[SystemName]Model.java",     // Main model
    "[DataClass].java"            // Custom data types (if any)
  ],

  // Test case specification (from Step 3.4)
  test_case: MODEL_CONTEXT.test_case,

  // Hierarchy information (from Phase 2)
  hierarchy: MODEL_CONTEXT.hierarchy,

  // For validation
  expected_states: MODEL_CONTEXT.states,
  expected_ports: {
    inputs: MODEL_CONTEXT.inputs,
    outputs: MODEL_CONTEXT.outputs
  },
  expected_transitions: MODEL_CONTEXT.transitions,
  expected_time_advances: MODEL_CONTEXT.time_advances
}
```

---

### Phase 4: Validate and Test

### Concurrent Execution

Execute validation checks **in parallel**:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 4: VALIDATE                            │
│                                                                 │
│  Input: HANDOFF_3_TO_4 (java_path + MODEL_CONTEXT)             │
│                                                                 │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐       │
│  │ Step 4.1 │ │ Step 4.2 │ │ Step 4.3 │ │ Step 4.4 │       │
│  │ javac    │ │ Static   │ │ Run tests│ │ Syntax   │       │
│  │ compile  │ │ checks   │ │          │ │ verify   │       │
│  └─────┬────┘ └─────┬────┘ └─────┬────┘ └─────┬────┘       │
│        │            │            │            │             │
│        └────────────┴────────────┴────────────┘             │
│                              ▼                               │
│                    ┌─────────────────┐                       │
│                    │ Step 4.5       │                       │
│                    │ Gen Validation │  ◄── NEW: Full report │
│                    │ Report         │                       │
│                    └───────┬────────┘                       │
│                            ▼                                 │
│                    ┌─────────────────┐                       │
│                    │ Step 4.6       │  ◄── NEW: Append      │
│                    │ Append to      │       to outline      │
│                    │ Outline.md     │                       │
│                    └───────┬────────┘                       │
│                            ▼                                 │
│              MODEL_CONTEXT.validation = {...}                │
│              MODEL_CONTEXT.validation.syntax_check = {...}   │
│              MODEL_CONTEXT.validation.report_path = "..."    │
└─────────────────────────────────────────────────────────────────┘
```

**Steps 4.1, 4.2, 4.3, 4.4 (CONCURRENT where possible):**

| Step | Command/Check | Output |
|------|---------------|--------|
| 4.1 | Compile with framework classpath | compiled: true/false |
| 4.2 | Verify all phases reachable, ta > 0 | static_check: pass/fail |
| 4.3 | Run simulation with framework (depends on 4.1) | simulation output |
| 4.4 | Verify syntactic correctness | syntax_check: pass/fail |
| 4.5 | Generate comprehensive validation report | validation_report: string |
| 4.6 | Append validation report to outline.md | report_path: string |

---

**Step 4.4: Verify Syntactic Correctness (NEW)**

Perform comprehensive syntactic verification of the generated DEVS model to ensure:
- All Java syntax is valid
- Port types are consistent across couplings
- All states are reachable
- Coupling connections are valid
- No dead code or unreachable transitions

**Syntactic Verification Checklist:**

```
┌────────────────────────────────────────────────────────────────────────┐
│                    SYNTACTIC CORRECTNESS VERIFICATION                   │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. COMPILATION CHECK                                                   │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ javac compiles without errors                             │    │
│     │ □ No unresolved symbols or imports                          │    │
│     │ □ All generic types properly parameterized                  │    │
│     │ □ No deprecated API warnings (or documented exceptions)     │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  2. PORT CONSISTENCY CHECK                                              │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ Input port types match expected message types             │    │
│     │ □ Output port types match expected message types            │    │
│     │ □ All declared ports are used in transitions                │    │
│     │ □ No ports declared but never connected (for coupled)       │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  3. STATE REACHABILITY CHECK                                            │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ All declared states are reachable from initial state      │    │
│     │ □ No orphan states (states with no incoming transitions)    │    │
│     │ □ No dead-end states (non-passive states with no outgoing)  │    │
│     │ □ Initial state is properly defined in initialize()         │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  4. TRANSITION CONSISTENCY CHECK                                        │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ delta_int defined for all non-passive states              │    │
│     │ □ delta_ext handles all declared input ports                │    │
│     │ □ lambda output matches state (output before transition)    │    │
│     │ □ Confluent transition properly combines int + ext          │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  5. TIME ADVANCE CHECK                                                  │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ All states have defined time advance (ta > 0 or INFINITY) │    │
│     │ □ No states with ta = 0 (causes infinite loops)             │    │
│     │ □ Passive states use INFINITY (Double.POSITIVE_INFINITY)    │    │
│     │ □ Active states use finite positive values                  │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  6. COUPLING VALIDITY CHECK (Coupled Models Only)                       │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ All IC couplings connect valid output→input ports         │    │
│     │ □ All EIC couplings connect system input→component input    │    │
│     │ □ All EOC couplings connect component output→system output  │    │
│     │ □ Port types match across couplings                         │    │
│     │ □ No circular dependencies that cause deadlock              │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  7. DATA CLASS CONSISTENCY CHECK                                        │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │ □ All custom data classes (DeliveryEvent, etc.) exist       │    │
│     │ □ Data classes have required constructors                   │    │
│     │ □ Data classes are Serializable (if needed)                 │    │
│     │ □ Getter/setter methods match port usage                    │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                                                                         │
└────────────────────────────────────────────────────────────────────────┘
```

**Verification Commands:**

```bash
# 1. Compile check - capture all errors and warnings
javac -Xlint:all -cp "lib/basic-devs.jar;src" src/Models/java/*.java 2>&1

# 2. Static analysis (if tools available)
# - Check for unreachable code
# - Verify all switch cases covered
# - Detect potential null pointer issues
```

**Verification Output Format:**

```
============================================================
SYNTACTIC CORRECTNESS VERIFICATION: [SystemName]
============================================================

COMPILATION STATUS:
  ✓ All files compiled successfully
  ✓ No compilation errors
  ⚠ 2 warnings (deprecated API usage)

PORT CONSISTENCY:
  ✓ 4 input ports verified
  ✓ 3 output ports verified
  ✓ All port types match across couplings

STATE REACHABILITY:
  ✓ All 5 states reachable from initial state
  ✓ No orphan states detected
  ✓ No dead-end states detected

TRANSITION CONSISTENCY:
  ✓ delta_int: 4 states covered
  ✓ delta_ext: All 4 input ports handled
  ✓ lambda: Output defined for all active states
  ✓ confluent: Properly implements int + ext

TIME ADVANCE:
  ✓ All states have valid time advances
  ✓ No zero-time states detected
  ✓ 2 passive states use INFINITY

COUPLING VALIDITY (Coupled Model):
  ✓ 4 IC couplings valid
  ✓ 0 EIC couplings (no external inputs)
  ✓ 1 EOC coupling valid
  ✓ No type mismatches
  ✓ No circular dependencies

DATA CLASSES:
  ✓ 5 data classes verified
  ✓ All constructors present
  ✓ All getters/setters match usage

────────────────────────────────────────────────────────────
VERIFICATION RESULT: ✓ PASSED (7/7 checks)
────────────────────────────────────────────────────────────
```

**Stored in MODEL_CONTEXT:**

```
MODEL_CONTEXT.validation.syntax_check = {
  passed: true,
  timestamp: "[ISO timestamp]",
  errors: [],                    // Empty if passed
  warnings: [
    "Line 45: Use of deprecated API MessageBagImpl.add()"
  ],
  checks: {
    compilation: { passed: true, errors: 0, warnings: 2 },
    port_consistency: { passed: true, input_ports: 4, output_ports: 3 },
    state_reachability: { passed: true, total_states: 5, reachable: 5 },
    transition_consistency: { passed: true, delta_int: 4, delta_ext: 4 },
    time_advance: { passed: true, passive: 2, active: 3, zero: 0 },
    coupling_validity: { passed: true, IC: 4, EIC: 0, EOC: 1 },
    data_classes: { passed: true, count: 5 }
  }
}
```

**Error Handling:**

If syntactic verification fails, generate a detailed error report:

```
============================================================
SYNTACTIC VERIFICATION FAILED: [SystemName]
============================================================

ERRORS FOUND: 3

ERROR 1: PORT TYPE MISMATCH
  Location: IRPSystem.java:120
  Coupling: vehicle.deliver_goods → retailer.receive_delivery
  Expected: Port<DeliveryEvent> → Port<DeliveryEvent>
  Actual:   Port<DeliveryEvent> → Port<String>
  Fix: Change retailer.receive_delivery to Port<DeliveryEvent>

ERROR 2: UNREACHABLE STATE
  Location: RetailerModel.java
  State: "MAINTENANCE"
  Issue: No transition leads to MAINTENANCE state
  Fix: Add transition or remove unused state

ERROR 3: ZERO TIME ADVANCE
  Location: VehicleModel.java:95
  State: "LOADING"
  Issue: ta(LOADING) = 0.0 causes infinite loop
  Fix: Set ta(LOADING) > 0 or use INFINITY for passive

────────────────────────────────────────────────────────────
ACTION REQUIRED: Fix 3 errors before deployment
────────────────────────────────────────────────────────────
```

---

**Step 4.5: Generate Comprehensive Validation Report (NEW)**

Generate a detailed validation report that includes port definitions, coupling validity, data flow diagrams, and state reachability analysis for all atomic models. This report provides complete traceability of the DEVS model structure.

**Validation Report Template:**

```markdown
╔════════════════════════════════════════════════════════════════════════════════╗
║           [SystemName] DEVS MODEL VALIDATION REPORT                            ║
║           Port Consistency, Coupling Validity & State Reachability             ║
╚════════════════════════════════════════════════════════════════════════════════╝

════════════════════════════════════════════════════════════════════════════════
1. ATOMIC MODEL PORT DEFINITIONS
════════════════════════════════════════════════════════════════════════════════

[For each atomic model, generate:]

┌──────────────────────────────────────────────────────────────────────────────┐
│ [AtomicModelName]                                                            │
├──────────────────────────────────────────────────────────────────────────────┤
│ INPUT PORTS:                                                                 │
│   • [port_name]      : Port<[DataType]>                                      │
│                                                                              │
│ OUTPUT PORTS:                                                                │
│   • [port_name]      : Port<[DataType]>                                      │
└──────────────────────────────────────────────────────────────────────────────┘

[For the coupled model:]

┌──────────────────────────────────────────────────────────────────────────────┐
│ [SystemName]System (Coupled Model)                                           │
├──────────────────────────────────────────────────────────────────────────────┤
│ EXTERNAL INPUT PORTS:                                                        │
│   • [port_name]      : Port<[DataType]>    (or "none")                       │
│                                                                              │
│ EXTERNAL OUTPUT PORTS:                                                       │
│   • [port_name]      : Port<[DataType]>                                      │
└──────────────────────────────────────────────────────────────────────────────┘


════════════════════════════════════════════════════════════════════════════════
2. COUPLING VALIDITY & PORT TYPE MATCHING
════════════════════════════════════════════════════════════════════════════════

INTERNAL COUPLINGS (IC):
────────────────────────────────────────────────────────────────────────────────

┌─────┬──────────────────────────────────┬──────────────────────────────────────┐
│ IC# │ SOURCE                           │ SINK                                 │
├─────┼──────────────────────────────────┼──────────────────────────────────────┤
│  1  │ [component1].[output_port]       │ [component2].[input_port]            │
│     │ Port<[DataType]>                 │ Port<[DataType]>                     │
│     │                    ✓ TYPE MATCH  (or ✗ TYPE MISMATCH)                   │
└─────┴──────────────────────────────────┴──────────────────────────────────────┘

EXTERNAL OUTPUT COUPLINGS (EOC):
────────────────────────────────────────────────────────────────────────────────

┌─────┬──────────────────────────────────┬──────────────────────────────────────┐
│ EOC#│ SOURCE                           │ SINK                                 │
├─────┼──────────────────────────────────┼──────────────────────────────────────┤
│  1  │ [component].[output_port]        │ [SystemName].[external_port]         │
│     │ Port<[DataType]>                 │ Port<[DataType]>                     │
│     │                    ✓ TYPE MATCH                                         │
└─────┴──────────────────────────────────┴──────────────────────────────────────┘

EXTERNAL INPUT COUPLINGS (EIC):
────────────────────────────────────────────────────────────────────────────────
    [List EIC couplings or "(none - system has no external inputs)"]


════════════════════════════════════════════════════════════════════════════════
3. DATA FLOW DIAGRAM
════════════════════════════════════════════════════════════════════════════════

[Generate ASCII art showing data flow between components:]

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              [SystemName]System (Coupled)                       │
│                                                                                 │
│  ┌─────────────────┐     [DataType1]        ┌─────────────────┐                │
│  │  [Component1]   │ ───────────────────────▶│  [Component2]   │                │
│  │                 │                         │                 │                │
│  │ [output_port]   │                         │ [input_port]    │                │
│  │                 │◀─────────────────────── │                 │                │
│  │ [input_port]    │     [DataType2]         │ [output_port]   │                │
│  └─────────────────┘                         └────────┬────────┘                │
│                                                       │                         │
│                                              [DataType3]                        │
│                                                       │                         │
│                                                       ▼                         │
│                                              ┌─────────────────┐                │
│                                              │  [Component3]   │                │
│                                              │                 │                │
│                                              │ [input_port]    │                │
│                                              │                 │                │
│                                              │ [output_port] ──┼────────────────▶ [external_out]
│                                              └─────────────────┘                │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘


════════════════════════════════════════════════════════════════════════════════
4. STATE REACHABILITY ANALYSIS
════════════════════════════════════════════════════════════════════════════════

[For each atomic model, generate state transition diagram:]

┌──────────────────────────────────────────────────────────────────────────────┐
│ [AtomicModelName] - STATES: [STATE1], [STATE2], [STATE3], ...                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   Initial ──▶ [INITIAL_STATE]                                                │
│                    │                                                         │
│                    │ δint ([condition])                                      │
│                    ▼                                                         │
│               [STATE2] ────── δint ──────▶ [STATE3]                          │
│                    │                           │                             │
│                    │ δext ([input_port])       │ δint                        │
│                    ▼                           ▼                             │
│               [STATE4] ◀──────────────── [INITIAL_STATE]                     │
│                                                                              │
│  ✓ All N states reachable from initial state                                 │
│  ✓ No dead-end states (passive states exit via δext)                         │
│  ✓ No orphan states                                                          │
└──────────────────────────────────────────────────────────────────────────────┘


════════════════════════════════════════════════════════════════════════════════
5. TIME ADVANCE VALIDATION
════════════════════════════════════════════════════════════════════════════════

┌───────────────────────┬─────────────────────────┬────────────────┬───────────┐
│ Model                 │ State                   │ Time Advance   │ Valid?    │
├───────────────────────┼─────────────────────────┼────────────────┼───────────┤
│ [AtomicModel1]        │ [STATE1]                │ [value/∞]      │ ✓ valid   │
│                       │ [STATE2]                │ [value]        │ ✓ > 0     │
├───────────────────────┼─────────────────────────┼────────────────┼───────────┤
│ [AtomicModel2]        │ [STATE1]                │ [value]        │ ✓ > 0     │
│                       │ [STATE2]                │ ∞ (passive)    │ ✓ valid   │
└───────────────────────┴─────────────────────────┴────────────────┴───────────┘

✓ No zero-time states detected (avoids infinite loops)
✓ All passive states correctly use ∞ (POSITIVE_INFINITY)


════════════════════════════════════════════════════════════════════════════════
6. DATA CLASSES USED IN COUPLINGS
════════════════════════════════════════════════════════════════════════════════

┌───────────────────┬───────────────────────────────────────────────────────────┐
│ Class             │ Used In Coupling                                          │
├───────────────────┼───────────────────────────────────────────────────────────┤
│ [DataClass1]      │ [component1].[port] → [component2].[port]                 │
│ [DataClass2]      │ [component2].[port] → [component3].[port]                 │
└───────────────────┴───────────────────────────────────────────────────────────┘

Required data classes: [DataClass1], [DataClass2], [DataClass3], ...


════════════════════════════════════════════════════════════════════════════════
7. VALIDATION SUMMARY
════════════════════════════════════════════════════════════════════════════════

┌────────────────────────────────────────────────────────────────────────────────┐
│                         VALIDATION RESULTS                                     │
├────────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│  PORT CONSISTENCY:                                                             │
│    ✓ [N] Internal Couplings (IC)     - All port types match                    │
│    ✓ [N] External Output Couplings   - Port types match                        │
│    ✓ [N] External Input Couplings    - Port types match (or N/A)               │
│                                                                                │
│  STATE REACHABILITY:                                                           │
│    ✓ [Model1]  - [N]/[N] states reachable                                      │
│    ✓ [Model2]  - [N]/[N] states reachable                                      │
│    ✓ [Model3]  - [N]/[N] states reachable                                      │
│                                                                                │
│  COUPLING VALIDITY:                                                            │
│    ✓ No type mismatches detected                                               │
│    ✓ No circular dependencies that cause deadlock                              │
│    ✓ All declared ports are connected                                          │
│                                                                                │
│  TIME ADVANCE:                                                                 │
│    ✓ [N]/[N] states have valid time advances                                   │
│    ✓ No zero-time states                                                       │
│    ✓ [N] passive states use INFINITY correctly                                 │
│                                                                                │
├────────────────────────────────────────────────────────────────────────────────┤
│                                                                                │
│                    ★★★ VALIDATION PASSED (ALL CHECKS) ★★★                      │
│                    (or ✗✗✗ VALIDATION FAILED - SEE ERRORS ✗✗✗)                 │
│                                                                                │
└────────────────────────────────────────────────────────────────────────────────┘
```

**Report Generation Process:**

1. **Extract Port Definitions:** Read each atomic model's Java file and extract:
   - Input port declarations: `addInputPort("[name]", [Type].class)`
   - Output port declarations: `addOutputPort("[name]", [Type].class)`

2. **Extract Couplings:** Read the coupled model's `setupCouplings()` method and extract:
   - IC: `addCoupling(component1.port, component2.port)`
   - EIC: `addCoupling(this.getInputPort(...), component.port)`
   - EOC: `addCoupling(component.port, this.getOutputPort(...))`

3. **Verify Port Types:** For each coupling, verify source and sink port types match.

4. **Analyze State Reachability:** For each atomic model:
   - Extract all state constants (e.g., `private static final String STATE = "STATE"`)
   - Trace transitions from initial state in `initialize()`
   - Follow `holdIn()`, `passivate()`, `passivateIn()` calls in `internalTransition()`
   - Follow state changes in `externalTransition()`
   - Verify all declared states are reachable

5. **Validate Time Advances:** Check that no state has `ta = 0` (causes infinite loops).

---

**Step 4.6: Append Validation Report to Outline (NEW)**

After generating the validation report, append it to the DEVS outline document created in Phase 2.

**Append to:** `docs/[SystemName]_DEVS_Outline.md`

**Append Format:**

```markdown

---

## 6. VALIDATION REPORT

[Insert full validation report generated in Step 4.5]

### Validation Timestamp
- Generated: [ISO timestamp]
- Model Version: [version or commit hash if available]
- Validator: DEVS Builder Skill v1.0

### Files Validated
- [SystemName]System.java (coupled model)
- [Component1]Model.java (atomic model)
- [Component2]Model.java (atomic model)
- [DataClass1].java, [DataClass2].java, ... (data classes)

---
```

**Implementation:**

```bash
# Append validation report to outline file
cat >> "docs/[SystemName]_DEVS_Outline.md" << 'EOF'

---

## 6. VALIDATION REPORT

[Generated validation report content]

EOF
```

**Stored in MODEL_CONTEXT:**

```
MODEL_CONTEXT.validation.report = {
  generated: true,
  timestamp: "[ISO timestamp]",
  report_content: "[full validation report string]",
  appended_to: "docs/[SystemName]_DEVS_Outline.md",

  // Detailed results
  port_definitions: {
    "[Model1]": { inputs: [...], outputs: [...] },
    "[Model2]": { inputs: [...], outputs: [...] }
  },
  couplings: {
    IC: [{ source: "...", sink: "...", type_match: true }],
    EIC: [...],
    EOC: [...]
  },
  state_reachability: {
    "[Model1]": { total: N, reachable: N, unreachable: [] },
    "[Model2]": { total: N, reachable: N, unreachable: [] }
  },
  time_advances: {
    "[Model1]": { "[STATE1]": { value: "X.X", valid: true }, ... }
  },
  data_classes: ["DataClass1", "DataClass2", ...]
}
```

---

After generating the `.java` files, execute these steps using Bash:

**For MS4Systems IDE Projects (Recommended):**
```bash
# Models are compiled and run within the MS4Systems DEVS IDE
# Copy generated files to the IDE's Models/java directory
# Use IDE's built-in simulation runner with SimViewer
```

**For Standalone Compilation (requires framework JAR):**
```bash
# 1. Ensure PhaseAtomic.java is in src/Models/java/
# 2. Compile with framework classpath
javac -cp "lib/basic-devs.jar:src" src/Models/java/[SystemName]Model.java

# 3. Run the simulation
java -cp "lib/basic-devs.jar:src" Models.java.[SystemName]Model

# Windows uses semicolon instead of colon for classpath:
javac -cp "lib/basic-devs.jar;src" src/Models/java/[SystemName]Model.java
java -cp "lib/basic-devs.jar;src" Models.java.[SystemName]Model
```

**Alternative: Run in MS4Systems SimViewer:**
```bash
# For coupled models, SimViewer provides graphical simulation
java -cp "lib/basic-devs.jar:src" Models.java.[SystemName]System
```

**Interpret the output:**
- Verify each test scenario shows expected state transitions in the simulation log
- Confirm all tests report `PASS`
- If any test fails, analyze the log, identify the incorrect transition or timing, fix the Java source, and recompile/rerun
- Report the final results to the user with a summary of what was validated

**Validation checks to perform:**
- All states are reachable (no dead states)
- Time advances are positive (or INFINITY for passive states)
- Every internal transition has a corresponding lambda output (or explicit null)
- External transitions handle all declared input ports
- No state is entered without a defined time advance

### Phase 4 → Phase 5 Handoff

```
HANDOFF_4_TO_5 = {
  // Full MODEL_CONTEXT from all previous phases
  ...MODEL_CONTEXT,

  // Validation results from Phase 4
  validation: {
    compiled: true,              // javac succeeded
    tests_passed: N,             // Number of tests that passed
    tests_failed: 0,             // Number of tests that failed
    syntax_check: MODEL_CONTEXT.validation.syntax_check
  },

  // Files ready for deployment (from Phase 3)
  java_path: MODEL_CONTEXT.java_path,
  generated_files: HANDOFF_3_TO_4.generated_files,

  // Deployment configuration
  source_dir: "src/Models/java/",
  target_dir: "C:\\Users\\carbo\\workspace-ms4me-llm-generated\\Basic-DEVS\\src\\Models\\java\\",

  // Deployment decision
  ready_for_deploy: validation.compiled && validation.tests_failed == 0 && validation.syntax_check.passed
}
```

---

### Phase 5: Deploy to MS4ME Workspace

**Purpose:** Copy the generated DEVS model files to the MS4Systems IDE workspace for integration and execution.

**Depends on:** Phase 4 validation passing (or user override)

**Target Directory:** `C:\Users\carbo\workspace-ms4me-llm-generated\Basic-DEVS\src\Models\java\`

### Deployment Execution

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 5: DEPLOY                              │
│                                                                 │
│  Input: HANDOFF_4_TO_5 (java_path + validation + syntax_check) │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Step 5.1: Create target directory if needed             │   │
│  │                                                         │   │
│  │ Target: C:\Users\carbo\workspace-ms4me-llm-generated\   │   │
│  │         Basic-DEVS\src\Models\java\                     │   │
│  └────────────────────────┬────────────────────────────────┘   │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Step 5.2: Copy generated files                          │   │
│  │                                                         │   │
│  │ Source: src/Models/java/*.java                          │   │
│  │ • PhaseAtomic.java (base class)                         │   │
│  │ • [Model]Model.java (main atomic model)                 │   │
│  │ • [DataClass].java (custom data types)                  │   │
│  │ • [System]System.java (coupled model, if applicable)    │   │
│  └────────────────────────┬────────────────────────────────┘   │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Step 5.3: Verify deployment                             │   │
│  └────────────────────────┬────────────────────────────────┘   │
│                           ▼                                     │
│            MODEL_CONTEXT.deployment = {                         │
│              deployed: true,                                    │
│              target_dir: "C:\Users\carbo\workspace-ms4me-...",  │
│              deployed_files: [...],                             │
│              timestamp: "..."                                   │
│            }                                                    │
└─────────────────────────────────────────────────────────────────┘
```

### Step 5.1: Create Target Directory

```bash
# Create the MS4ME workspace directory structure if it doesn't exist
mkdir -p "C:/Users/carbo/workspace-ms4me-llm-generated/Basic-DEVS/src/Models/java"
```

### Step 5.2: Copy Generated Files

```bash
# Copy generated Java files to the MS4ME workspace (EXCLUDING PhaseAtomic.java)
# PhaseAtomic.java is assumed to already exist in the target directory
for file in src/Models/java/*.java; do
    filename=$(basename "$file")
    if [ "$filename" != "PhaseAtomic.java" ]; then
        cp "$file" "C:/Users/carbo/workspace-ms4me-llm-generated/Basic-DEVS/src/Models/java/"
    fi
done
```

**Files to Deploy:**

| File Type | Example | Deploy? |
|-----------|---------|---------|
| Base class | `PhaseAtomic.java` | **NO** (already exists in target) |
| Atomic model | `[Name]Model.java` | Yes |
| Data classes | `DeliveryEvent.java`, `CostReport.java` | If custom types used |
| Coupled model | `[Name]System.java` | If coupled model |
| Component models | `[Comp]Model.java` | If coupled model |

**Note:** `PhaseAtomic.java` is assumed to already exist in the target MS4ME workspace. It is NOT copied during deployment to avoid overwriting any customizations.

### Step 5.3: Verify Deployment

```bash
# List deployed files to verify
ls -la "C:/Users/carbo/workspace-ms4me-llm-generated/Basic-DEVS/src/Models/java/"
```

### Phase 5 → Final Output

```
DEPLOYMENT COMPLETE
===================

Target Directory: C:\Users\carbo\workspace-ms4me-llm-generated\Basic-DEVS\src\Models\java\

Deployed Files:
  ✓ [Model]Model.java
  ✓ [DataClass1].java
  ✓ [DataClass2].java
  (PhaseAtomic.java skipped - already exists)

To run in MS4Systems IDE:
  1. Open MS4ME (MS4 Modeling Environment)
  2. Import project from: C:\Users\carbo\workspace-ms4me-llm-generated\Basic-DEVS\
  3. Navigate to src/Models/java/[Model]Model.java
  4. Right-click → Run As → DEVS Simulation
  5. Use SimViewer for graphical output

MODEL_CONTEXT.deployment = {
  deployed: true,
  target_dir: "C:\\Users\\carbo\\workspace-ms4me-llm-generated\\Basic-DEVS\\src\\Models\\java\\",
  deployed_files: ["[Model]Model.java", "[DataClass].java", ...],
  skipped_files: ["PhaseAtomic.java"],
  timestamp: "[ISO timestamp]"
}
```

### Deployment Notes

- **Existing files are overwritten** - The deployment will replace any existing model files with the same name
- **PhaseAtomic.java is NEVER copied** - Assumes it already exists in the target workspace
- **Package convention** - All files use `package Models.java;` per MS4Systems convention
- **IDE refresh required** - After deployment, refresh the project in MS4ME to see new files

---

## Common Scenarios

### Scenario 1: Simple Atomic Model (e.g., Traffic Light)
Use when modeling a single component with timed phase transitions and optional external interrupts. The autonomous inference handles these well from a brief description. Typically produces 3-6 states with fixed time advances.

### Scenario 2: Server/Queue Model (e.g., M/M/1 Queue)
Use when modeling processing systems with arrivals and departures. States typically include IDLE and BUSY. External inputs are arrivals; internal transitions are service completions. Time advances may be stochastic (use `Math.random()` for exponential distributions).

### Scenario 3: Coupled DEVS (e.g., Producer-Consumer Network)
Use when the system has multiple interacting components. Generate one atomic model class per component, then build a coupled model that wires them together via port couplings. The test harness drives the top-level coupled model.

### Scenario 4: Protocol Model (e.g., Request-Reply with Timeout)
Use when modeling communication protocols. States include IDLE, WAIT_ACK, TIMEOUT. External transitions handle incoming messages. Time advances model timeout durations.

---

## Troubleshooting

### Issue: `javac` not found
**Cause**: Java JDK not installed or not on PATH.
**Solution**:
```bash
# Check Java installation
java -version
javac -version

# Windows: install via winget
winget install Microsoft.OpenJDK.21

# Verify PATH includes JDK bin directory
```

### Issue: Compilation errors in generated code
**Cause**: Placeholder not fully replaced or type mismatch.
**Solution**: Read the compiler error, locate the line in the generated file, and fix the specific issue. Common fixes:
- Missing `break` in switch cases
- Enum constant name mismatch (case-sensitive)
- Incorrect port name string comparison

### Issue: Test scenario fails unexpectedly
**Cause**: Transition logic doesn't match the formal specification.
**Solution**: Compare the DEVS outline (Phase 2) against the Java implementation (Phase 3) line by line. Check:
- Is `delta_int` firing before `delta_ext` when `ta` expires first?
- Is `lambda` returning the correct output for the current phase?
- Is `sigma` being reset correctly after each transition?

### Issue: Infinite loop in simulation
**Cause**: A state has `ta = 0` or transitions cycle without advancing time.
**Solution**: Ensure every state has `ta > 0` or `ta = INFINITY`. Check for cycles like `A -> B -> A` where both have `ta = 0`.

---

## Semantic Continuity Summary

| Phase | Reads From | Writes To | Key Consistency Checks |
|-------|------------|-----------|------------------------|
| 0 | resources/ | `catalog` (incl. `templates`) | Templates exist at expected paths |
| 1 | `catalog`, user input | `model`, `states`, `inputs`, `outputs`, `transitions`, `time_advances` | Patterns exist in `catalog.patterns` |
| 2 | All Phase 0 & 1 context | `outline_path`, `model_type`, **`hierarchy`** | States match `transitions`; type selected; hierarchy diagram generated |
| 3 | All previous context + template | `java_path`, **`test_case`** | All placeholders replaced; switch cases covered; test case embedded |
| 4 | All previous context | `validation`, **`syntax_check`** | Compile succeeds, tests pass, syntax verified |
| 5 | `java_path`, `validation` | `deployment` | Files copied to MS4ME workspace |

**Cross-Phase References:**
- Phase 1 uses `catalog.patterns` to identify similar model structures
- Phase 1 uses `catalog.analytics` when stochastic timing is needed
- Phase 2 determines `model_type` (Atomic vs Coupled) based on Q3 Type
- **Phase 2 generates `hierarchy` diagram showing model composition (NEW)**
- Phase 3 reads `catalog.templates` to get template source path
- Phase 3 uses `AtomicModelTemplate.java` for single atomic models
- Phase 3 uses `CoupledModelTemplate.java` for multi-component systems
- **Phase 3 generates `test_case` with parameters, assertions, and stop conditions (NEW)**
- Phase 4 validates against `transitions` and `time_advances`
- **Phase 4 performs `syntax_check` for port consistency, state reachability, coupling validity (NEW)**
- Phase 5 reads `java_path` to locate generated files for deployment
- Phase 5 copies all files from `src/Models/java/` to MS4ME workspace

**New Phase Outputs Summary:**

| Phase | New Output | Description |
|-------|------------|-------------|
| 2 | `MODEL_CONTEXT.hierarchy` | Hierarchical composition diagram (ASCII art) showing coupled/atomic structure |
| 3 | `MODEL_CONTEXT.test_case` | Self-contained test case with parameters, assertions, stop conditions |
| 4 | `MODEL_CONTEXT.validation.syntax_check` | Syntactic verification results (ports, states, couplings, time advances) |

**Template File Paths:**
```
catalog.templates = {
  PhaseAtomic: "resources/models/PhaseAtomic.java"           # Required base class
  AtomicModelTemplate: "resources/models/AtomicModelTemplate.java"
  CoupledModelTemplate: "resources/models/CoupledModelTemplate.java"
}
```

**Files Generated Per Model Type:**

| Model Type | Files Generated |
|------------|-----------------|
| Atomic | `PhaseAtomic.java` (copy) + `[Name]Model.java` |
| Coupled | `PhaseAtomic.java` (copy) + `[Comp1]Model.java` + `[Comp2]Model.java` + ... + `[Name]System.java` |

---

## Analytics Reference (from catalog.analytics)

Use these for stochastic DEVS models with variable timing:

| Distribution | Class | Parameters | Use Case |
|--------------|-------|------------|----------|
| Normal | `SampleFromNormal` | mean, sigma | Processing times with variation |
| Exponential | `SampleFromExponential` | mean | Arrival processes (Poisson) |
| Uniform | `SampleFromUniform` | min, max | Bounded random delays |
| Pareto | `SampleFromPareto` | scale, shape | Heavy-tailed service times |
| LogNormal | `SampleFromLogNormal` | mu, sigma | Multiplicative processes |

**Example stochastic time advance:**
```java
// For exponential inter-arrival times (mean = 5.0)
private SampleFromExponential arrivalDist = new SampleFromExponential(5.0);

public double timeAdvance() {
    if (phase == Phase.GENERATING) {
        return arrivalDist.getSample();  // Random exponential delay
    }
    return Double.POSITIVE_INFINITY;
}
```

---

## Model Patterns Reference (from catalog.patterns)

| Pattern | Components | Description | Example |
|---------|------------|-------------|---------|
| Generator | Genr, GeneratorOfJobs | Autonomous output production | Job arrivals |
| Processor | Proc, ProcessorOfJobs | Input-output transformation | Service stations |
| Transducer | Transd, Transducer | Measurement/observation | Statistics collection |
| Coupled System | ComputerSystem, PITSystem | Multi-component composition | Queuing networks |

---

## Related Skills
- [My First Skill](../my-first-skill/) - Basic DEVS outline template
- [Power DEVS Builder](../power-devs-builder/) - Continuous system simulation (QSS methods)
- [SPARC Methodology](../sparc-methodology/) - Structured development workflow
