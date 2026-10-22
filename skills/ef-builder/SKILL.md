---
name: "ef-builder"
description: "Create DEVS Experimental Frames (EFs) grounded in the EFSUT (Experimental Frame-System Under Test) methodology (Zeigler, 2026). Supports multiple EFs per system, EF partial ordering via derivability, and applicability mapping between frames and models. Two major phases: (1) Conceptual Phase -- analyzes stakeholder questions and generates candidate experimental frames for user selection, producing an EF Analysis Report; (2) Developmental Phase -- generates the selected EF as compilable DEVS Java source files (Generator, Transducer, Coupled EF) for the MS4 Systems framework. Use when designing controlled experiments for DEVS simulation models."
---

Base directory for this skill: C:\Users\carbo\OneDrive\Desktop\Claude\.claude\skills\ef-builder

## Output Directory Configuration

All generated files are saved to a **per-project folder** within the output directory. Each project folder is named after the system and contains `docs/` and `java/` subdirectories:

```
output/
  [SystemName]/
    docs/
      [SystemName]_EF_Analysis.md
      [SystemName]_EFSUT_Table.md
    java/
      [SystemName]Model.java
      [SystemName]_[FrameName]_EFGenerator.java
      [SystemName]_[FrameName]_EFTransducer.java
      [SystemName]_[FrameName]_EF.java
```

| File Type | Output Path | Example |
|-----------|------------|---------|
| EF Analysis Report | `output/[SystemName]/docs/` | `output/MySystem/docs/MySystem_EF_Analysis.md` |
| Stakeholder Requirements EFSUT Table | `output/[SystemName]/docs/` | `output/MySystem/docs/MySystem_EFSUT_Table.md` |
| System Model Java (SUT) | `output/[SystemName]/java/` | `output/MySystem/java/MySystemModel.java` |
| EF Generator Java | `output/[SystemName]/java/` | `output/MySystem/java/MySystem_Perf_EFGenerator.java` |
| EF Transducer Java | `output/[SystemName]/java/` | `output/MySystem/java/MySystem_Perf_EFTransducer.java` |
| EF Coupled Model Java | `output/[SystemName]/java/` | `output/MySystem/java/MySystem_Perf_EF.java` |

**OUTPUT_ROOT:** `C:\Users\carbo\OneDrive\Desktop\Claude\output`
**PROJECT_DIR:** `C:\Users\carbo\OneDrive\Desktop\Claude\output\[SystemName]`

---

# EF Builder

## What This Skill Does

Designs complete DEVS Experimental Frames grounded in the **EFSUT (Experimental Frame-System Under Test)** methodology (Zeigler, 2026). The EFSUT framework provides a principled foundation for model choice and lifecycle management by formalizing the relationship between stakeholder questions, experimental frames, and models.

The skill produces four primary outputs:

| Output | Produced After | Contents |
|--------|---------------|----------|
| **EF Analysis Report** | Conceptual Phase | Stakeholder questions, candidate frames, EF partial order, best-model table, user selection rationale |
| **Stakeholder Requirements EFSUT Table** | Conceptual Phase | Maps each stakeholder question to its corresponding experimental frame, potential DEVS model, and model objectives |
| **System Model Java (SUT)** | Developmental Phase | One self-contained atomic DEVS model (extends `PhaseAtomic`) declaring ALL ports referenced across all selected frames, with complete state machine and accessor methods |
| **EF DEVS Java Files** | Developmental Phase | Three compilable Java source files per frame (Generator, Transducer, Coupled EF) for the MS4 Systems DEVS framework |

### Core EFSUT Principles

1. **Multiple EFs per system**: Any given system can have many experimental frames, each addressing different stakeholder questions and observing different aspects of the system.
2. **EF Partial Order**: Experimental frames are organized by a derivability relation using three operations -- selection, aggregation, and coarsening -- forming a partial order from finer to coarser observation.
3. **Applicability Relation**: An EF is applicable to a model if and only if the model's ports are compatible with the frame's observation and stimulation requirements.
4. **Best-Model Table**: For each EF in the partial order, there exists a minimally sufficient accommodating model -- the simplest model that can answer the questions posed by that frame.
5. **Mission Thread**: A sequence of EF-Model pairs that together support end-to-end decision making.

### EF Architecture

An Experimental Frame encapsulates a System Under Test (SUT) within a controlled experiment consisting of:

- A **Generator** (atomic) that injects scenario-based stimuli into the SUT's input ports
- A **Transducer** (atomic) that observes the SUT's output ports, collects per-port statistics, and computes throughput
- An **EF Coupled Model** that wires Generator -> System -> Transducer -> Generator(stop) via Internal Couplings

```
+--------------------------------------------------------------------+
|                  Experimental Frame (Coupled)                       |
|                                                                     |
|  +--------------+   N stimulus    +------------------------------+ |
|  |  Generator   | -------------->|  System Under Test (SUT)      | |
|  |  (Atomic)    |   ports        |  (Atomic or Coupled)          | |
|  |              |                |                               | |
|  |  Scenarios   |                |  [DEVS Model]                 | |
|  |  period, N   |                |                               | |
|  +------^-------+                +-----------+-------------------+ |
|         |                                    |                     |
|         | stop           M observation       |                     |
|         |                ports               |                     |
|         |                                    v                     |
|  +------+-------+<--------------------------+                     |
|  |  Transducer  |                                                  |
|  |  (Atomic)    |                                                  |
|  |              |                                                  |
|  |  Statistics  |                                                  |
|  |  obs window  |                                                  |
|  +--------------+                                                  |
+--------------------------------------------------------------------+

Couplings (IC only -- EF is self-contained):
  Generator.out_ports -> SUT.in_ports      (N stimulus couplings)
  SUT.out_ports -> Transducer.in_ports     (M observation couplings)
  Transducer.stop -> Generator.stop        (1 control coupling)
  Total: N + M + 1
```

---

## EFSUT Theoretical Foundation

### Formal Definitions (Zeigler, 2026)

**Experimental Frame (EF)**: A specification of the conditions under which a system is observed or experimented with. Formally, an EF specifies:
- **Admissible input segments** (Generator): the set of input trajectories that may be applied
- **Observation conditions** (Transducer): what outputs are measured and how they are processed
- **Control conditions**: when the experiment starts and stops

**System Under Test (SUT)**: The DEVS model (atomic or coupled) being experimented upon.

**Derivability Relation**: EF1 <= EF2 (EF1 is derivable from EF2) if EF1's data can be obtained from EF2's data by applying one or more of:
- **Selection**: Restricting the time interval or choosing a subset of input/output trajectories
- **Aggregation**: Combining multiple observations into summary statistics (e.g., averaging over time)
- **Coarsening**: Reducing resolution of observation (e.g., discretizing continuous outputs)

**Applicability**: An EF is applicable to a model M if M has the required input/output ports and can generate the behaviors the EF needs to observe. The model must be able to "answer" all questions posed by the frame.

**Best-Model Table**: Maps each EF to the simplest (most abstract) model that can accommodate it. This prevents over-engineering by ensuring analysts use the minimally sufficient model for each question.

### Three Uses of EFSUT

| Use | Description | Example |
|-----|-------------|---------|
| **Model Development** | EFs guide what models to build and at what fidelity | "We need a model that can answer latency questions under node failure" |
| **System Testing** | EFs define test harnesses derived from verification/validation requirements | "Test that end-to-end latency < 200ms under 10% packet loss" |
| **Decision Support** | Mission threads (sequences of EF-Model pairs) support architecture trades | "Compare Tranche 2 vs Tranche 3 transport layer performance" |

### EFSUT Development Pipeline

```
Stakeholder      EF Partial       Model           Applicability    Mission        Decision
Questions   -->  Order        --> Family       --> Mapping      --> Thread    -->  Support

"What if...?"    E1 <= E2 <= E3   M_base          E1 -> M_base     [E1,M1] ->    Trade
"How does...?"   E4 <= E5         M_lumped        E2 -> M_lumped   [E2,M2] ->    studies,
"Can we...?"     E6               M_detailed      E3 -> M_detail   [E3,M3]       risk
                                                  E4 -> M_lumped                  assess.
```

---

## Two Major Phases and Their Outputs

The EF Builder operates in two sequential major phases, each producing a distinct deliverable:

```
+=========================================================================+
|                         CONCEPTUAL PHASE                                 |
|  Analyze stakeholder questions -> Generate candidate EFs -> User selects |
|                                                                          |
|  OUTPUT 1a: EF Analysis Report (.md)                                     |
|  OUTPUT 1b: Stakeholder Requirements EFSUT Table (.md)                   |
+=========================================================================+
         |
         | User-approved EF selection
         v
+=========================================================================+
|                        DEVELOPMENTAL PHASE                               |
|  Generate EF as DEVS Java files -> Validate and write compilable source  |
|                                                                          |
|  OUTPUT 2: System Model + EF DEVS Java Files (.java) per frame           |
|            (1 System Model + 3 EF files per frame = 4 files per frame)   |
+=========================================================================+
```

---

## EF_CONTEXT Object

All steps read from and write to a shared **EF_CONTEXT** that maintains semantic consistency across both phases:

```
EF_CONTEXT = {
  // -- System Identification ----------------------------------
  system: {
    name: "",                     // System name
    description: "",              // System description
    type: "atomic" | "coupled",   // Model type
    input_ports: [
      { name: "", type: "", description: "" }
    ],
    output_ports: [
      { name: "", type: "", description: "" }
    ],
    states: [...],                // Known states
    state_variables: [...]        // Observable variables
  },

  // -- Conceptual Phase Outputs -------------------------------
  stakeholder_questions: [
    { id: "Q1", question: "", category: "", priority: "" }
  ],
  candidate_frames: [
    {
      id: "EF_1",
      name: "",
      description: "",
      questions_addressed: ["Q1", "Q2"],
      generator: {
        stimulus_ports: [...],
        scenario_types: [...],
        scenario_count: 0,
        period: 0.0
      },
      transducer: {
        observation_ports: [...],
        statistics: [...],
        event_trackers: [...],
        observation_time: 0.0
      },
      control: {
        stop_criteria: "",
        observation_window: 0.0
      },
      derivability: {
        derived_from: [],
        derives_to: [],
        operations: []
      },
      applicable_models: []
    }
  ],
  ef_partial_order: {
    frames: [...],
    derivability_edges: [
      { from: "EF_1", to: "EF_2", operation: "selection|aggregation|coarsening" }
    ]
  },
  best_model_table: [
    { frame_id: "EF_1", model: "", justification: "" }
  ],
  derivability_relation: {
    frames: [],                   // Indexed frame list (E₁, E₂, ...)
    edges: [],                    // { from, to, operation, description }
    transitive_closure: [],       // Transitive chains
    properties: {
      greatest_element: "",       // Frame with no parent
      independent_branches: [],   // Frames incomparable with main chain
      width: 0,                   // Size of largest antichain
      height: 0                   // Length of longest chain
    }
  },
  applicability_matrix: {
    models: [],                   // Model list
    matrix: {}                    // Frame -> { model: "Best"|"Yes"|"—" }
  },
  selected_frames: [],

  // -- Stakeholder Requirements EFSUT Table -------------------
  efsut_table: [
    {
      stakeholder_question: "",     // Plain-text question as stated by stakeholder
      experimental_frame: "",       // Frame identifier (e.g., "EF_SolarFlareLatency")
      primary_model: "",            // Model name (e.g., "Stochastic DEVS Model")
      modeling_objectives: ""       // Semicolon-separated objectives
    }
  ],

  // -- Developmental Phase Outputs ----------------------------
  java_structures: [
    {
      frame_id: "EF_1",
      generator_class: "",        // Generator class name
      transducer_class: "",       // Transducer class name
      coupled_class: "",          // Coupled EF class name
      stimulus_ports: [...],      // Port names for Generator output
      observation_ports: [...],   // Port names for Transducer input
      scenarios: [...],           // Scenario table (double[][])
      scenario_names: [...],      // Scenario name array
      event_trackers: [...],      // Domain-specific trackers
      couplings: {                // Coupling specification
        stimulus: [...],          // gen.port -> sys.port
        observation: [...],       // sys.port -> trans.port
        control: "trans.stop -> gen.stop"
      },
      default_period: 0.0,        // Generator period
      default_max_count: 0,       // Generator max stimuli
      default_obs_time: 0.0       // Transducer observation time
    }
  ],

  // -- Verification Results ------------------------------------
  verification: {
    structural: {
      package_declaration: bool,
      required_imports: bool,
      class_inheritance: bool,
      serial_version_uid: bool,
      port_declarations: bool,
      constructor_signatures: bool,
      devs_methods: bool,
      helper_methods: bool
    },
    efsut_consistency: {
      questions_covered: bool,
      ports_compatible: bool,
      coupling_completeness: bool,
      scenario_completeness: bool,
      timing_constraints: bool,
      accessor_coverage: bool
    },
    maximal_ef: {
      port_union_input: [],         // Union of ALL stimulus ports across all frames
      port_union_output: [],        // Union of ALL observation ports across all frames
      combined_scenario_count: 0,   // Total scenarios across all frames
      state_coverage: [],           // States reachable by combined scenarios
      coupling_count_max: 0,        // N_max + M_max + 1
      cv_passed: bool,              // Conceptual Verification passed
      dv_passed: bool               // Developmental Verification passed
    },
    errors: [],
    warnings: []
  },

  // -- Generated File Paths ----------------------------------
  outputs: {
    analysis_report: "",          // Path to EF Analysis Report
    efsut_table: "",              // Path to Stakeholder Requirements EFSUT Table
    java_files: []                // Paths to EF DEVS Java files
  }
}
```

---

## Workflow Overview

```
+-------------------------------------------------------------------------+
|                     EF_CONTEXT (Persists Across All Steps)                |
+-------------------------------------------------------------------------+
                                     |
  +------------------------------------------------------------------+
  |                     STEP 0: SYSTEM INTAKE                         |
  |  Collect system description, ports, states                        |
  +------------------------------------------------------------------+
                                     |
  +==================================================================+
  ||                    CONCEPTUAL PHASE                              ||
  +==================================================================+
  |  C1: Stakeholder       C2: Candidate EF     C3: EF Partial       |
  |      Question              Generation            Order &         |
  |      Analysis                                    Best-Model      |
  |                                                  Table           |
  |                                                                  |
  |  C4: Present EFs       C5: User Selection                        |
  |      to User               & Approval                            |
  +==================================================================+
                                     |
                 OUTPUT 1a: EF Analysis Report (.md)
                 OUTPUT 1b: Stakeholder Requirements EFSUT Table (.md)
                                     |
  +==================================================================+
  ||              CV: CONCEPTUAL VERIFICATION                         ||
  ||              (Maximal EF Lattice Construction)                    ||
  +==================================================================+
  |  CV1: Construct        CV2: Port             CV3: State           |
  |       Maximal EF            Completeness          Reachability    |
  |       (lattice top)         Audit                 Audit           |
  |                                                                   |
  |  CV4: Derivability     CV5: Generate Verification Report          |
  |       Soundness                                                   |
  +===================================================================+
                                     |
                          User-approved selection
                                     |
  +==================================================================+
  ||                   DEVELOPMENTAL PHASE                            ||
  +==================================================================+
  |  D1: Java Class        D2: Generate EF       D3: Generate EF     |
  |      Structure             Generator              Transducer      |
  |      Design                Java File              Java File       |
  |                                                                   |
  |  D4: Generate          D5: Validate &                             |
  |      Coupled EF            Write Java                             |
  |      Java File             Files                                  |
  +===================================================================+
                                     |
  +==================================================================+
  ||              DV: DEVELOPMENTAL VERIFICATION                      ||
  ||              (Maximal EF Cross-Check)                             ||
  +==================================================================+
  |  DV1: System Model     DV2: Accessor         DV3: Port           |
  |       vs Maximal EF         Coverage              Activity        |
  |       Port Audit            Matrix                Matrix          |
  |                                                                   |
  |  DV4: Coupling         DV5: State             DV6: Generate      |
  |       Subsumption           Reachability           Verification   |
  |       Check                 Trace                  Report         |
  +===================================================================+
                                     |
                 OUTPUT 2: EF DEVS Java Files (.java) per frame
                            (3 files per frame)
```

---

# STEP 0: SYSTEM INTAKE

**Purpose:** Collect the system description, identify its interface (input/output ports), states, and observable variables. This information is used by both phases.

### Execution

**Step 0.1: Collect System Information**

If the user has provided a system description, extract the following. Otherwise, use AskUserQuestion:

"Describe the system you want to design experimental frames for. Include what the system does, what inputs it receives, and what outputs it produces."

**Step 0.2: Identify System Interface**

From the description, determine:

| Element | Description | Example |
|---------|-------------|---------|
| **System name** | Identifier for the SUT | "TransportLayerNode" |
| **Input ports (X)** | Signals/data the system receives | `message_in`, `noise_level` |
| **Output ports (Y)** | Signals/data the system produces | `message_out`, `latency_report` |
| **States (S)** | Distinct operational phases | IDLE, PROCESSING, DEGRADED, FAILED |
| **State variables** | Observable quantities | `queue_depth`, `error_rate`, `throughput` |

**Step 0.3: Confirm with User**

Present the extracted system interface:

```
System: [SystemName]
Description: [brief]

Input Ports:
  [name] : [type] -- [description]
  ...

Output Ports:
  [name] : [type] -- [description]
  ...

States: [list]
Observable Variables: [list]

Does this capture the system correctly?
```

**Write to EF_CONTEXT.system.**

---

# CONCEPTUAL PHASE

The Conceptual Phase analyzes what experiments are meaningful for the system and generates candidate experimental frames. The user reviews and selects which frames to develop. This phase embodies the EFSUT principle that **stakeholder questions drive frame formulation**.

## C1: Stakeholder Question Analysis

**Purpose:** Elicit, categorize, and prioritize the questions that stakeholders want answered about the system.

### Execution

**Step C1.1: Collect Questions**

If the user has provided stakeholder questions, parse and categorize them. Otherwise, use AskUserQuestion:

"What questions do you want this experiment to answer about the system? You can provide multiple questions."

Additionally, infer implicit questions from the system description (e.g., a description mentioning "failure" implies resilience questions; ports named "error_rate" imply performance questions).

**Step C1.2: Categorize Questions**

Classify each question into one of these EFSUT categories:

| Category | Description | Example |
|----------|-------------|---------|
| **Performance** | Throughput, latency, capacity | "What is the maximum throughput under load?" |
| **Resilience** | Behavior under degradation or failure | "How does the system respond to node loss?" |
| **Behavioral** | State reachability, transition correctness | "Does the system reach all declared states?" |
| **Parametric** | Sensitivity to parameter changes | "How does response time change with load?" |
| **Comparative** | Contrasting configurations or designs | "Which configuration yields lower latency?" |
| **Boundary** | Edge-case and limit behavior | "What happens at maximum capacity?" |

**Step C1.3: Prioritize Questions**

Assign priority based on:
- Stakeholder emphasis (explicit requests)
- Coverage of system behavior (questions that exercise more states/ports rank higher)
- Experiment feasibility (questions answerable with available ports)

**Write to EF_CONTEXT.stakeholder_questions:**
```
EF_CONTEXT.stakeholder_questions = [
  { id: "Q1", question: "...", category: "Performance", priority: "High" },
  { id: "Q2", question: "...", category: "Resilience", priority: "High" },
  { id: "Q3", question: "...", category: "Behavioral", priority: "Medium" },
  ...
]
```

---

## C2: Candidate Experimental Frame Generation

**Purpose:** For each stakeholder question (or group of related questions), formulate one or more candidate experimental frames that could answer those questions.

### Execution

**Step C2.1: Map Questions to Frames**

For each question or cluster of related questions, define a candidate EF by specifying:

1. **Name**: Descriptive identifier (e.g., `EF_throughput`, `EF_resilience_nodeFailure`)
2. **Questions Addressed**: Which stakeholder questions this frame answers
3. **Admissible Inputs (Generator specification)**:
   - Which SUT input ports are exercised
   - What input ranges and patterns are needed
   - What scenarios represent the question's conditions
4. **Observation Conditions (Transducer specification)**:
   - Which SUT output ports are observed
   - What statistics are computed (counts, rates, distributions)
   - What domain-specific events are tracked
5. **Control Conditions**:
   - Observation window duration
   - Stop criteria (time-based, event-based, or condition-based)

**Step C2.2: Generate Diverse Frames**

For a system with N input ports and M output ports, consider generating frames along these dimensions:

| Frame Type | Ports Used | Complexity | Typical Purpose |
|------------|-----------|------------|-----------------|
| **Full-scope** | All N inputs, all M outputs | High | Comprehensive system characterization |
| **Single-concern** | Subset of ports related to one question | Low | Targeted investigation of one aspect |
| **Stress** | All inputs at extreme values, all outputs | High | Boundary and failure behavior |
| **Comparative** | Same ports, different scenario sets | Medium | A/B comparison of configurations |
| **Focused-output** | Minimal inputs, specific outputs | Low | Isolate one observable behavior |

**Frame Generation Rules:**
- Every stakeholder question must be addressed by at least one candidate frame
- Frames should vary in scope (some broad, some narrow)
- Frame names should be semantically meaningful

**Step C2.3: Define Admissible Inputs and Observations Per Frame**

For each candidate frame, specify:

```
Candidate Frame: EF_[name]
  Questions:    [Q1, Q3]
  Generator:
    Stimulus ports:   [subset of SUT input ports]
    Scenario types:   [baseline, stress, edge, recovery, ...]
    Scenario count:   [3-8]
    Period:           [inter-stimulus interval]
  Transducer:
    Observation ports: [subset of SUT output ports]
    Statistics:        [counts, rates, logs]
    Event trackers:    [domain-specific events]
  Control:
    Observation time:  [computed from scenario count and timing]
    Stop criteria:     [time-based | event-based]
```

**Scenario Design Rules:**
```
  1. Baseline scenario      -- nominal/default values for all ports
  2. Stress scenario        -- push one or two ports toward adverse values
  3. Edge case scenario     -- boundary values (0.0, 1.0, max, min)
  4. Combined pressure      -- multiple ports at adverse values simultaneously
  5. Recovery scenario      -- favorable values to enable recovery transitions
  6. Stabilization          -- return to near-nominal conditions
  7. Optimal scenario       -- best-case values for all ports
  8. Adversarial scenario   -- worst-case combination

  Each scenario should target a different SUT state or transition.
  The scenario sequence should follow a narrative arc:
    stable -> degrading -> crisis -> recovery -> stable
```

**Timing Rules:**
```
generator.period       = max(SUT time advances) * 2      (default: 10.0)
generator.scenario_count = number of scenarios            (default: 8)
transducer.observation_time = period * scenario_count * 1.25 (default: 100.0)

CONSTRAINT: observation_time >= period * scenario_count
```

**Write to EF_CONTEXT.candidate_frames.**

---

## C3: EF Partial Order and Best-Model Table

**Purpose:** Organize the candidate frames into a partial order using the derivability relation, and map each frame to the minimally sufficient model.

### Execution

**Step C3.1: Establish Derivability Relations**

For each pair of candidate frames (EF_i, EF_j), determine if EF_i <= EF_j (EF_i is derivable from EF_j) by checking whether EF_i's data can be obtained from EF_j's data through:

| Operation | Test | Example |
|-----------|------|---------|
| **Selection** | EF_i observes a time-subset or port-subset of EF_j | EF_i observes port A only; EF_j observes ports A,B,C |
| **Aggregation** | EF_i computes a summary statistic from EF_j's raw data | EF_i computes average throughput; EF_j logs per-event throughput |
| **Coarsening** | EF_i uses lower resolution than EF_j | EF_i categorizes output as {low,medium,high}; EF_j records exact values |

**Derivability Notation:**
```
EF_focused <= EF_comprehensive    (via selection: fewer ports)
EF_summary <= EF_detailed         (via aggregation: averages vs. raw)
EF_coarse  <= EF_fine             (via coarsening: categories vs. values)
```

**Step C3.2: Build Partial Order Diagram**

Present the partial order as a directed graph (finer at top, coarser at bottom):

```
EF Partial Order for [SystemName]:

    EF_comprehensive (all ports, all scenarios)
       |            |
       | selection  | aggregation
       v            v
    EF_perf      EF_summary
       |
       | coarsening
       v
    EF_perf_coarse

    EF_resilience (independent branch)
```

**Step C3.3: Construct Best-Model Table**

For each frame in the partial order, identify the minimally sufficient model:

| Frame | Accommodating Model | Justification |
|-------|--------------------|---------------|
| EF_comprehensive | Full DEVS Model (all states, all ports) | Needs complete port interface |
| EF_perf | Reduced Model (performance-relevant states only) | Only needs throughput-related transitions |
| EF_resilience | Stochastic Model (failure states) | Needs failure modes and recovery transitions |
| EF_summary | Lumped Model (aggregated outputs) | Summary statistics derivable from lumped model |

**Step C3.4: Formal Derivability Relation**

Express the partial order as a formal mathematical structure. For each system:

1. **Index the frames**: Assign E₁, E₂, ..., Eₙ ordered from finest (most ports/scenarios) to coarsest.

2. **List derivability edges with operations**: For each edge Eⱼ ≤ Eᵢ, specify the operation (σ for selection, α for aggregation, γ for coarsening) and what ports/data are transformed.

3. **Compute transitive closure**: Identify all transitive chains (e.g., E₄ ≤ E₂ ≤ E₁ implies E₄ ≤ E₁ via σ₃ ∘ σ₁).

4. **Draw Hasse diagram**: ASCII diagram showing only the covering relations (direct edges, no transitive shortcuts).

5. **Document port reduction per edge**: For each derivability edge, list which input/output ports are removed, retained, or aggregated.

6. **Compute partial order properties**:
   - Greatest element (finest frame, if unique)
   - Independent branches (frames incomparable with the main chain)
   - Width (size of the largest antichain)
   - Height (length of the longest chain)

**Template:**
```
Let E = {E₁, E₂, ..., Eₙ} where:
  E₁ = EF_[finest]
  E₂ = EF_[next]
  ...

Derivability edges (≤):
  Eⱼ ≤ Eᵢ  via [operation] [label]: [port/data transformation]
  ...

Transitive closure:
  Eₖ ≤ Eᵢ  (Eₖ ≤ ... ≤ Eᵢ) via [composed operations]
  ...

Hasse Diagram:
  [ASCII diagram with operation labels on edges]

Port Reduction per Edge:
  Eⱼ ≤ Eᵢ: remove IN [ports]; retain [N] IN, [M] OUT
  ...

Properties:
  Greatest element:     E₁ ([frame name])
  Independent branches: {Eₖ} ([frame name])
  Width:                [number]
  Height:               [number] ([chain])
```

**Step C3.5: Applicability Matrix**

Construct a matrix showing which models each frame is applicable to. For each frame-model pair, determine applicability by checking whether the model has the required input/output ports and can generate the behaviors the frame needs to observe.

1. **List all models** from the model family (columns).
2. **List all frames** from the partial order (rows).
3. **For each cell**, assign one of:
   - **Best**: The minimally sufficient model (from the best-model table in C3.3)
   - **Yes**: The model can accommodate the frame but is over-specified (has more ports/states than needed)
   - **—**: The model cannot accommodate the frame (missing required ports or behaviors)

4. **Add per-model observations**: For each model, summarize how many frames it accommodates.

**Template:**
```
| | Model₁ | Model₂ | ... | Modelₖ |
|---|:-:|:-:|:-:|:-:|
| EF_frame1 | **Best** | Yes | — | — |
| EF_frame2 | Yes | **Best** | — | — |
| ... | ... | ... | ... | ... |

Legend: **Best** = minimally sufficient model; Yes = applicable but
over-specified; — = not applicable (missing required ports or behaviors)

Observations:
  Model₁: Accommodates [X] of [N] frames ([list or description])
  Model₂: Accommodates [Y] of [N] frames ([list or description])
  ...
```

**Applicability Rules:**
- The Full/Base DEVS model typically accommodates all frames (it has all ports and states)
- Lumped/reduced models accommodate only frames whose port requirements fall within their reduced interface
- Stochastic models accommodate frames that exercise probabilistic behaviors but may lack deterministic protocol logic (e.g., handoff, rerouting)
- Operational models accommodate frames focused on their specific domain (e.g., adversary interaction) but may lack ports for other domains (e.g., solar physics)

**Write to EF_CONTEXT.ef_partial_order, EF_CONTEXT.best_model_table, EF_CONTEXT.derivability_relation, and EF_CONTEXT.applicability_matrix.**

---

## C3.6: Generate Stakeholder Requirements EFSUT Table

**Purpose:** Synthesize the results of C1 (stakeholder questions), C2 (candidate frames), and C3 (best-model table) into a structured table that maps each stakeholder question to its corresponding experimental frame, the potential DEVS model that could be created, and the objectives that model aims to achieve. This table provides a concise, decision-oriented view of the entire EFSUT mapping.

### Execution

**Step C3.6.1: Build Table Entries**

For each stakeholder question in `EF_CONTEXT.stakeholder_questions`, construct a row containing:

1. **Stakeholder Question**: The plain-text question from C1 (no IDs, categories, or priorities in the cell -- just the question itself)
2. **Experimental Frame (EF)**: The name of the candidate frame from C2 that addresses this question (e.g., `EF_CustodyContinuity`, `EF_HandoffDecision`)
3. **Primary Model**: The best model from C3's best-model table for the corresponding frame (e.g., "DEVSPWSA Tracking Atomic Model", "Stochastic DEVS Markov Model")
4. **Modeling Objectives**: Semicolon-separated list of what the model aims to achieve when answering this stakeholder question, derived from:
   - The question's category (Performance -> latency/throughput metrics; Resilience -> failure/recovery behavior; etc.)
   - The frame's observation ports (what is measured)
   - The frame's scenario design (what conditions are tested)

**Step C3.6.2: Format the EFSUT Table**

The table uses a clean 4-column format with one row per stakeholder question:

```
| Stakeholder Question | Experimental Frame (EF) | Primary Model | Modeling Objectives |
|---------------------|------------------------|--------------|-------------------|
| [Question text as plain sentence] | EF_[Name] | [Model Name] | [Objective 1]; [Objective 2]; [Objective 3]. |
| [Question text] | EF_[Name] | [Model Name] | [Objective 1]; [Objective 2]. |
| ... | ... | ... | ... |
```

**Formatting Rules:**
- **Stakeholder Question**: Write the question exactly as stated by the stakeholder, as a natural-language sentence ending with a question mark
- **Experimental Frame (EF)**: Use the frame's identifier name only (e.g., `EF_SolarFlareLatency`), no descriptions or port details
- **Primary Model**: Use the model's full descriptive name (e.g., "Stochastic DEVS Model", "Lumped Connectivity Model"), no fidelity tags or morphism details
- **Modeling Objectives**: Semicolon-separated short phrases describing what the model computes or demonstrates; end with a period
- If a question is addressed by multiple frames, include one row per frame for that question
- Keep all cells concise -- no sub-rows, no metadata, no port lists

**Step C3.6.3: Validate Table Completeness**

Verify:
- Every stakeholder question appears in at least one row
- Every candidate frame appears in at least one row
- Every model in the best-model table is referenced
- Modeling objectives are specific and actionable (not generic)

**Write to EF_CONTEXT.efsut_table.**

---

## C4: Present Candidate Frames to User

**Purpose:** Display the complete set of candidate experimental frames, their partial order, and the best-model table for user review.

### Presentation Format

```
============================================================
CANDIDATE EXPERIMENTAL FRAMES for [SystemName]
============================================================

STAKEHOLDER QUESTIONS:
  Q1: [question text]                    [Category] [Priority]
  Q2: [question text]                    [Category] [Priority]
  Q3: [question text]                    [Category] [Priority]

CANDIDATE FRAMES:
  [1] EF_[name1] -- [brief description]
      Addresses: Q1, Q2
      Generator: [N] stimulus ports, [K] scenarios
      Transducer: [M] observation ports, [statistics summary]
      Observation time: [T]

  [2] EF_[name2] -- [brief description]
      Addresses: Q3
      Generator: [N'] stimulus ports, [K'] scenarios
      Transducer: [M'] observation ports, [statistics summary]
      Observation time: [T']

  [3] EF_[name3] -- [brief description]
      ...

PARTIAL ORDER:
  EF_[name1] (comprehensive)
     |
     +-- EF_[name2] (derived via selection)
     |
     +-- EF_[name3] (derived via aggregation)

BEST-MODEL TABLE:
  EF_[name1] -> Full Model (requires all ports)
  EF_[name2] -> Reduced Model (subset of ports sufficient)
  EF_[name3] -> Lumped Model (aggregated outputs sufficient)

============================================================
```

---

## C5: User Selection and Approval

**Purpose:** The user selects which candidate frame(s) to develop in the Developmental Phase.

### Execution

Use AskUserQuestion to present the selection:

"Which experimental frame(s) would you like to develop into DEVS Java source files?"

Options:
- **[Frame 1 name]** -- [brief description, questions addressed]
- **[Frame 2 name]** -- [brief description, questions addressed]
- **All frames** -- Develop all candidate frames

**Selection Rules:**
- User may select one or multiple frames
- If the user selects a frame that is derivable from another (EF_i <= EF_j), suggest also developing EF_j since EF_i can then be obtained from it
- If the user requests modifications to a candidate frame, update EF_CONTEXT.candidate_frames accordingly

**Write to EF_CONTEXT.selected_frames.**

---

## OUTPUT 1a: EF Analysis Report

**Produced after:** Conceptual Phase (C5 complete)

**File:** `output/[SystemName]/docs/[SystemName]_EF_Analysis.md`

The EF Analysis Report captures the full reasoning from the Conceptual Phase. It serves as a standalone document that records the stakeholder questions, all candidate frames considered, the partial order, the best-model table, and the user's selection rationale.

### Report Structure

```
============================================================
EF ANALYSIS REPORT: [SystemName]
============================================================

1. SYSTEM OVERVIEW
   System name, description, port interface, states

2. STAKEHOLDER QUESTIONS
   Numbered list with categories and priorities

3. CANDIDATE EXPERIMENTAL FRAMES
   For each candidate:
     - Name and description
     - Questions addressed
     - Generator specification (stimulus ports, scenarios, timing)
     - Transducer specification (observation ports, statistics, trackers)
     - Control conditions (observation window, stop criteria)

4. EF PARTIAL ORDER
   Derivability relations with operations labeled
   ASCII diagram of partial order

5. FORMAL DERIVABILITY RELATION
   Mathematical notation (E₁, E₂, ...)
   Explicit derivability edges with operations
   Transitive closure
   Hasse diagram
   Port reduction per edge
   Partial order properties (greatest element, width, height)

6. BEST-MODEL TABLE
   Frame-to-model mapping with justifications
   Model family correspondence

7. APPLICABILITY MATRIX
   Frame-model applicability matrix (Best / Yes / —)
   Per-model accommodation observations

8. SELECTED FRAMES
   User selection with rationale
   Derivability implications noted

9. STAKEHOLDER REQUIREMENTS EFSUT TABLE
   Reference to EFSUT Table file
   Question-to-frame-to-model summary

10. TRANSITION TO DEVELOPMENTAL PHASE
    Summary of what will be produced as DEVS Java files

============================================================
Generated by: ef-builder skill
Date: [timestamp]
============================================================
```

**Write report to `output/[SystemName]/docs/[SystemName]_EF_Analysis.md`.** Create the project directory and `docs/` subdirectory if they do not exist.

---

## OUTPUT 1b: Stakeholder Requirements EFSUT Table

**Produced after:** Conceptual Phase (C5 complete), alongside the EF Analysis Report

**File:** `output/[SystemName]/docs/[SystemName]_EFSUT_Table.md`

The Stakeholder Requirements EFSUT Table provides a concise, tabular mapping from stakeholder questions through experimental frames to potential DEVS models and their objectives. It serves as a decision-support artifact that shows stakeholders how each of their questions is addressed by the EFSUT methodology.

### Table Structure

The output file contains a clean 4-column markdown table with one row per stakeholder question:

```markdown
# Stakeholder Requirements EFSUT Table: [SystemName]

Generated by: ef-builder skill
Date: [timestamp]
EFSUT Reference: Zeigler (2026)

| Stakeholder Question | Experimental Frame (EF) | Primary Model | Modeling Objectives |
|---------------------|------------------------|--------------|-------------------|
| [Question as plain sentence?] | EF_[Name] | [Model Name] | [Obj 1]; [Obj 2]; [Obj 3]. |
| [Question as plain sentence?] | EF_[Name] | [Model Name] | [Obj 1]; [Obj 2]. |
| ... | ... | ... | ... |
```

**Column definitions:**

| Column | Content | Example |
|--------|---------|---------|
| **Stakeholder Question** | Natural-language question as stated by the stakeholder | "How long is custody maintained on a missile track?" |
| **Experimental Frame (EF)** | Frame identifier name only | EF_CustodyContinuity |
| **Primary Model** | Full model name from best-model table | DEVSPWSA Tracking Atomic Model |
| **Modeling Objectives** | Semicolon-separated short phrases ending with a period | Measure custody duration; detect breakpoints; support Bayesian updates. |

**Write table to `output/[SystemName]/docs/[SystemName]_EFSUT_Table.md`.**

**Transition:** After Conceptual Verification passes, proceed to the Developmental Phase with the selected frame(s).

---

# CONCEPTUAL VERIFICATION (CV): Maximal EF Lattice Construction

After completing the Conceptual Phase and generating OUTPUT 1a/1b, construct a **Maximal EF** — the supremum (join) of the lattice of candidate frames — to verify completeness, consistency, and derivability before proceeding to code generation.

The Maximal EF is a synthetic experimental frame that:
- Uses the **UNION** of ALL input (stimulus) ports across ALL candidate frames
- Observes the **UNION** of ALL output (observation) ports across ALL candidate frames
- Covers the **UNION** of ALL scenarios across ALL candidate frames
- Has the maximum coupling count: `N_max + M_max + 1`

If the Maximal EF is well-formed, then every individual EF is guaranteed to be derivable from it via selection (port subset), aggregation, or coarsening — confirming the lattice structure is sound.

---

## Step CV1: Construct the Maximal EF

Build the Maximal EF specification from the candidate frames in `EF_CONTEXT.candidate_frames`:

1. **Collect all stimulus ports**: For each candidate frame, collect all Generator output port names. Take the union. This is `port_union_input`.
2. **Collect all observation ports**: For each candidate frame, collect all Transducer input port names. Take the union. This is `port_union_output`.
3. **Collect all scenarios**: For each candidate frame, collect all scenario rows (parameter vectors). Concatenate into a combined scenario table. Record `combined_scenario_count`.
4. **Compute maximum coupling count**: `N_max = |port_union_input|`, `M_max = |port_union_output|`, total = `N_max + M_max + 1`.

Record the Maximal EF specification in `EF_CONTEXT.verification.maximal_ef`.

**Output format:**

```
MAXIMAL EF SPECIFICATION (Lattice Top / Supremum)
═══════════════════════════════════════════════════
  Input Ports (Stimulus Union):    [list all]
  Output Ports (Observation Union): [list all]
  |Input|  = N_max                  [count]
  |Output| = M_max                  [count]
  Coupling Count:                   N_max + M_max + 1 = [total]
  Combined Scenarios:               [count] across [K] frames
  Source Frames:                    [list frame names]
```

---

## Step CV2: Port Completeness Audit

Verify that the system description (Step 0) declares every port that appears in the Maximal EF:

1. **Input port check**: For each port in `port_union_input`, confirm it appears as an input port in the system description. Flag any port that exists in a candidate frame but not in the system.
2. **Output port check**: For each port in `port_union_output`, confirm it appears as an output port in the system description. Flag any port that exists in a candidate frame but not in the system.
3. **Orphan port check**: For each port in the system description, confirm it appears in at least one candidate frame. Flag any system port that no frame exercises (orphan port). Orphan ports are warnings, not errors — they indicate the EF set does not fully exercise the system.

**Output format:**

```
PORT COMPLETENESS AUDIT (CV2)
═══════════════════════════════
STIMULUS PORTS (Input):
  [port_name]   Frame(s): [list]   System: [YES/NO]
  ...
OBSERVATION PORTS (Output):
  [port_name]   Frame(s): [list]   System: [YES/NO]
  ...
ORPHAN PORTS (in system but no frame exercises them):
  [port_name]   Direction: [in/out]
  ...
RESULT: [PASS / FAIL / WARN]
```

- **FAIL** if any frame port is absent from the system.
- **WARN** if orphan ports exist.
- **PASS** otherwise.

---

## Step CV3: State Reachability Audit

Verify that the combined scenarios of the Maximal EF can drive the system through all declared states:

1. **Collect declared states**: From the system description (Step 0), list all declared states.
2. **Map scenarios to target states**: For each scenario in the combined scenario table, identify the target state it aims to reach (based on parameter values and state transition logic).
3. **Compute state coverage**: `covered_states = union of all target states`. Record in `EF_CONTEXT.verification.maximal_ef.state_coverage`.
4. **Check coverage**: If `covered_states == declared_states`, PASS. Otherwise, flag missing states.

**Output format:**

```
STATE REACHABILITY AUDIT (CV3)
═══════════════════════════════
Declared States:    [list all]    Count: [N]
Covered States:     [list all]    Count: [M]
Missing States:     [list any]    Count: [N-M]

Per-Frame Coverage:
  [FrameName]:   targets [list states]
  ...

Combined Coverage:  M / N  ([percentage]%)
RESULT: [PASS / FAIL]
```

- **FAIL** if any declared state is unreachable by the combined scenarios.
- **PASS** if all declared states are covered.

---

## Step CV4: Derivability Soundness

Verify that each individual candidate frame is derivable from the Maximal EF:

1. **Port subset check**: For each candidate frame, confirm its stimulus ports are a subset of `port_union_input` AND its observation ports are a subset of `port_union_output`. (This is true by construction, but confirms no frame was missed during union computation.)
2. **Scenario subset check**: For each candidate frame, confirm its scenarios appear in the combined scenario table.
3. **Coupling count check**: For each candidate frame, confirm its coupling count (`N_i + M_i + 1`) is less than or equal to `N_max + M_max + 1`.
4. **Derivation operation**: For each candidate frame, identify the derivation operation from the Maximal EF: selection (port subset), aggregation (port merge), or coarsening (resolution reduction). Record the operation type.

**Output format:**

```
DERIVABILITY SOUNDNESS (CV4)
═══════════════════════════════
Frame: [FrameName]
  Stimulus Ports:     [N_i] / [N_max]   subset: [YES/NO]
  Observation Ports:  [M_i] / [M_max]   subset: [YES/NO]
  Coupling Count:     [C_i] / [C_max]   valid:  [YES/NO]
  Scenarios:          [K_i] / [K_max]   subset: [YES/NO]
  Derivation Op:      [selection / aggregation / coarsening]
  RESULT: [PASS / FAIL]
...
OVERALL RESULT: [PASS / FAIL]
```

---

## Step CV5: Generate Conceptual Verification Report

Produce a formatted summary of CV1–CV4 results:

```
╔════════════════════════════════════════════════════════════╗
║        CONCEPTUAL VERIFICATION REPORT (Maximal EF)        ║
╚════════════════════════════════════════════════════════════╝

  Maximal EF:        [N_max] input + [M_max] output ports
  Combined Scenarios: [K_max] across [F] frames
  Coupling Count:    [C_max]

  CV1  Maximal EF Construction:   [PASS]
  CV2  Port Completeness Audit:   [PASS / FAIL / WARN]
  CV3  State Reachability Audit:  [PASS / FAIL]
  CV4  Derivability Soundness:    [PASS / FAIL]

  OVERALL:  [PASS / FAIL]
```

- **If PASS**: Record `EF_CONTEXT.verification.maximal_ef.cv_passed = true`. Proceed to the Developmental Phase.
- **If FAIL**: Stop. Report the failures. The user must resolve them before code generation begins. Common resolutions:
  - Missing system port → update system description (Step 0)
  - Missing state coverage → add a new scenario or candidate frame
  - Derivability failure → review frame port selections

---

# DEVELOPMENTAL PHASE

The Developmental Phase takes the user-approved experimental frame(s) from the Conceptual Phase and generates compilable DEVS Java source files. First, one System Model Java file is produced: a self-contained atomic DEVS model (extends `PhaseAtomic`) declaring the union of ALL ports referenced across all selected frames, with a complete state machine and accessor methods. Then, for each selected frame, three EF Java files are produced: a Generator (extends `PhaseAtomic`), a Transducer (extends `PhaseAtomic`), and a Coupled EF model (extends `CoupledModelImpl`) that wires the Generator, System Model, and Transducer together. The generated code follows the MS4 Systems DEVS framework conventions and includes a simulation entry point with test case verification.

**If multiple frames are selected:** The Developmental Phase produces one System Model file (shared across all frames) plus three EF Java files per selected frame.

**Reference examples:** The generated Java files follow the patterns established in the `resources/examples/` directory (e.g., `HealthEFGenerator.java`, `HealthEFTransducer.java`, `HealthSystemEF.java`).

## D1: Java Class Structure Design

**Purpose:** Design the Java class structure for the System Model and each selected experimental frame, defining the four DEVS Java source files: one System Model (SUT) shared across all frames, plus three EF files per frame (Generator, Transducer, Coupled EF).

### Execution

**Step D1.1: Define Java File Set**

One System Model file is generated for the system, plus three EF files per selected frame:

| File | Class | Extends | Role |
|------|-------|---------|------|
| `[SystemName]Model.java` | `[SystemName]Model` | `PhaseAtomic` | Self-contained SUT with union of ALL ports across all frames, complete state machine, and accessor methods |
| `[SystemName]_[FrameName]_EFGenerator.java` | `[SystemName]_[FrameName]_EFGenerator` | `PhaseAtomic` | Injects scenario-based stimuli into the SUT |
| `[SystemName]_[FrameName]_EFTransducer.java` | `[SystemName]_[FrameName]_EFTransducer` | `PhaseAtomic` | Observes SUT outputs and collects statistics |
| `[SystemName]_[FrameName]_EF.java` | `[SystemName]_[FrameName]_EF` | `CoupledModelImpl` | Wires Generator, SUT, and Transducer with N+M+1 couplings |

All classes use package `Models.java` and import the MS4 Systems DEVS framework.

**Step D1.2: Define Port Declarations**

From the frame's specification in the Conceptual Phase, map:

- **Generator output ports**: One `Port<String>` per stimulus port in the frame
- **Generator input port**: One `Port<String>` named `stop` (from Transducer)
- **Transducer input ports**: One `Port<String>` per observation port in the frame
- **Transducer output port**: One `Port<String>` named `stop` (to Generator)

Port names must exactly match between Generator output -> SUT input and SUT output -> Transducer input.

**Step D1.3: Define Scenario Table**

From the frame's scenario specification in C2:

- Map each scenario to a row in a `static final double[][] SCENARIOS` array
- Map each scenario name to a `static final String[] SCENARIO_NAMES` array
- Each row has exactly N columns (one per stimulus port)
- Scenario values use the ranges defined in the frame's generator specification
- Follow the narrative arc: stable -> degrading -> crisis -> recovery -> stable

**Step D1.4: Define Event Trackers**

From the frame's transducer specification:

- Identify domain-specific events to track (e.g., state transitions, threshold crossings, alarm triggers)
- Each tracker becomes an `int` counter in the Transducer
- Define the detection logic (string matching on received messages)

**Write class structure plan to EF_CONTEXT.java_structures (including system_model entry for the System Model).**

---

## D2: Generate EF Generator Java File

**Purpose:** Generate the complete EF Generator Java source file for each selected frame, following the `PhaseAtomic` pattern from the MS4 Systems DEVS framework. Reference: `resources/examples/HealthEFGenerator.java`.

### Execution

**Step D2.1: File Header and Imports**

Every Generator file begins with:

```java
package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.List;
// NOTE: Display is in the same package (Models.java) — no import needed.
// Use static calls: Display.PhaseString(...), Display.setEnabled(false) (default)
```

**Step D2.2: Class Javadoc**

Generate a comprehensive Javadoc comment including:
- Class name and role (Experimental Frame Generator)
- System name and frame name
- List of stimulus ports with descriptions
- Scenario table summary (what each scenario represents)
- DEVS formalism specification: `M = <X, Y, S, delta_int, delta_ext, lambda, ta>`
- States (S): GENERATING, PASSIVE
- Input ports (X): `stop : Port<String>` (from Transducer)
- Output ports (Y): list all stimulus ports as `Port<String>`
- Time advances: `GENERATING=period (default [value])`, `PASSIVE=INFINITY`

**Step D2.3: Class Declaration and Fields**

```java
public class [SystemName]_[FrameName]_EFGenerator extends PhaseAtomic {

    private static final long serialVersionUID = 1L;
    // Display is used via static methods — no instantiation needed.
    // Display.setEnabled(false) is called in initialize() — disabled by default.
    // Call Display.PhaseString(...) to show phase transitions.

    // -- Output Ports -- stimuli to [SystemName] --
    public final Port<String> [port1];
    public final Port<String> [port2];
    // ... one per stimulus port in the frame

    // -- Input Ports -- stop signal from Transducer --
    public final Port<String> stop;

    // -- State variables --
    private double period;
    private int count = 0;
    private int maxCount;
```

**Step D2.4: Scenario Table**

Generate from the frame's scenario specification:

```java
    // Each row: { [port1], [port2], ... }
    private static final double[][] SCENARIOS = {
        // Scenario 1: Baseline -- nominal/default values
        { [val1], [val2], ... },
        // Scenario 2: [Name] -- [description]
        { [val1], [val2], ... },
        // ... (3-8 scenarios following narrative arc:
        //      stable -> degrading -> crisis -> recovery -> stable)
    };

    private static final String[] SCENARIO_NAMES = {
        "Baseline", "[Name2]", "[Name3]", ...
    };
```

**Scenario Design Rules:**
- Minimum 3 scenarios, typically 6-8
- Each row has exactly N values (one per stimulus port)
- Follow narrative arc: baseline -> stress -> crisis -> recovery -> optimal
- Values must fall within the ranges defined in the frame's generator specification

**Step D2.5: Constructor**

```java
    public [ClassName](String name, double period, int maxCount) {
        super(name);
        this.period = period;
        this.maxCount = maxCount;

        [port1] = addOutputPort("[port1]", String.class);
        [port2] = addOutputPort("[port2]", String.class);
        // ... one addOutputPort per stimulus port
        stop    = addInputPort("stop", String.class);
    }
```

**Step D2.6: DEVS Methods**

Generate all six required DEVS methods:

| Method | Implementation |
|--------|---------------|
| `initialize()` | `super.initialize(); count = 0; holdIn("GENERATING", period);`, Call `Display.setEnabled(false);`, Call `Display.PhaseString(getName() + ": " + phase);` |
| `getTimeAdvance()` | `return sigma;` |
| `getOutput()` | If `phaseIs("GENERATING")`: create `MessageBagImpl`, get scenario row via `count % SCENARIOS.length`, add each port value via `out.add(port, String.valueOf(scenario[col]))`, print stimulus info, return out. Else return `MessageBag.EMPTY`. |
| `internalTransition()` | If `phaseIs("GENERATING")`: `count++`, if `count < maxCount` then `holdIn("GENERATING", period)` else `passivateIn("PASSIVE")` with log message. Call `Display.PhaseString(getName() + ": " + phase);` |
| `externalTransition(double, MessageBag)` | `sigma -= timeElapsed;` Get stop messages via `getMessages(input, stop)`. If not empty, log stop data, `passivateIn("PASSIVE")`. Call `Display.PhaseString(getName() + ": " + phase);` |
| `confluentTransition(MessageBag)` | `internalTransition(); externalTransition(0.0, input);` Call `Display.PhaseString(getName() + ": " + phase);` |

**Step D2.7: Accessor and Helper Methods**

```java
    public int getCount() { return count; }
    public double getPeriod() { return period; }

    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }
```

**Step D2.8: main() Method with runTestCaseSimulation()**

Every Generator file includes a standalone `main()` method and a `runTestCaseSimulation()` method that verifies the Generator can execute its full scenario sequence in isolation (without a System Model or Transducer). This allows independent unit testing of the Generator's stimulus injection logic.

```java
    // ══════════════════════════════════════════════════════════
    // MAIN — Standalone Generator Test
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     [SystemName] EF Generator — Standalone Test            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        final double TEST_PERIOD = [period];
        final int TEST_MAX_COUNT = [scenario_count];
        final int MAX_ITERATIONS = TEST_MAX_COUNT * 3;
        final double MAX_SIMULATION_TIME = TEST_PERIOD * TEST_MAX_COUNT * 1.5;

        [GeneratorClass] gen = new [GeneratorClass]("Generator_Test",
                TEST_PERIOD, TEST_MAX_COUNT);

        runTestCaseSimulation(gen, MAX_ITERATIONS, MAX_SIMULATION_TIME);
    }

    /**
     * Run standalone test case simulation for the Generator.
     * Verifies: initialization, scenario cycling, output production,
     * count progression, and PASSIVE termination after maxCount.
     */
    private static void runTestCaseSimulation([GeneratorClass] gen,
            int maxIterations, double maxTime) {

        Simulation sim = new SimulationImpl("[GeneratorClass] Test", gen);
        sim.startSimulation(0);

        System.out.println("┌──────┬──────────┬───────────┬──────────────────────────┐");
        System.out.println("│ Iter │ SimTime  │ Count     │ Phase                    │");
        System.out.println("├──────┼──────────┼───────────┼──────────────────────────┤");

        int iteration = 0;
        boolean stopConditionMet = false;
        String stopReason = "Max iterations reached";

        while (iteration < maxIterations && !stopConditionMet) {
            iteration++;

            try {
                sim.simulateIterations(1);
            } catch (Exception e) {
                System.err.println("Simulation error at iteration " + iteration
                        + ": " + e.getMessage());
                stopReason = "Simulation exception: " + e.getMessage();
                break;
            }

            double simTime = sim.getCurrentSimulationTime();
            int count = gen.getCount();
            String phase = gen.getPhase();

            if (phase.equals("PASSIVE")) {
                stopConditionMet = true;
                stopReason = "Generator reached PASSIVE (all stimuli sent)";
            }

            if (simTime >= maxTime) {
                stopReason = "Max simulation time reached (" + maxTime + ")";
                break;
            }

            System.out.println(String.format(
                    "│ %4d │ %8.2f │ %9d │ %-24s │",
                    iteration, simTime, count, phase));
        }

        System.out.println("└──────┴──────────┴───────────┴──────────────────────────┘");
        System.out.println();

        // ── Verification ─────────────────────────────────────
        System.out.println("── Generator Test Results ──────────────────────────────");
        System.out.println("Stop Reason: " + stopReason);
        boolean testPassed = true;
        int passed = 0;
        int failed = 0;

        // V1: Generator produced stimuli
        int finalCount = gen.getCount();
        if (finalCount > 0) {
            System.out.println("  PASS: Generator produced " + finalCount + " stimuli");
            passed++;
        } else {
            System.out.println("  FAIL: Generator produced 0 stimuli");
            testPassed = false;
            failed++;
        }

        // V2: Generator reached PASSIVE
        if (gen.phaseIs("PASSIVE")) {
            System.out.println("  PASS: Generator reached PASSIVE phase");
            passed++;
        } else {
            System.out.println("  FAIL: Generator did not reach PASSIVE (phase=" + gen.getPhase() + ")");
            testPassed = false;
            failed++;
        }

        // V3: Count matches maxCount
        if (finalCount == gen.getMaxCount() || finalCount >= gen.getMaxCount()) {
            System.out.println("  PASS: Count (" + finalCount + ") matches maxCount");
            passed++;
        } else {
            System.out.println("  FAIL: Count (" + finalCount + ") < maxCount");
            testPassed = false;
            failed++;
        }

        // V4: Period is positive
        if (gen.getPeriod() > 0.0) {
            System.out.println("  PASS: Period is positive (" + gen.getPeriod() + ")");
            passed++;
        } else {
            System.out.println("  FAIL: Period is non-positive (" + gen.getPeriod() + ")");
            testPassed = false;
            failed++;
        }

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (testPassed) {
            System.out.println("*** GENERATOR TEST PASSED ***");
        } else {
            System.out.println("!!! GENERATOR TEST FAILED !!!");
        }
        System.out.println("────────────────────────────────────────────────────────");
    }
```

**Additional imports required for standalone Generator test:**
```java
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
```

**Additional accessor required:**
```java
    public int getMaxCount() { return maxCount; }
```

**Write Generator Java source to EF_CONTEXT.java_structures.**

---

## D3: Generate EF Transducer Java File

**Purpose:** Generate the complete EF Transducer Java source file for each selected frame, following the `PhaseAtomic` pattern. Reference: `resources/examples/HealthEFTransducer.java`.

### Execution

**Step D3.1: File Header and Imports**

```java
package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.ArrayList;
import java.util.List;
// NOTE: Display and PhaseAtomic are in the same package (Models.java) — no import needed.
// Use static calls: Display.PhaseString(...), Display.setEnabled(false) (default)
```

**Step D3.2: Class Javadoc**

Generate Javadoc including:
- Class name and role (Experimental Frame Transducer)
- List of observation ports with descriptions
- Tracked statistics and domain-specific events
- DEVS formalism: States (OBSERVING, DONE), ports, time advances

**Step D3.3: Class Declaration -- Ports, State Variables, Counters, Logs, Trackers**

```java
public class [SystemName]_[FrameName]_EFTransducer extends PhaseAtomic {

    private static final long serialVersionUID = 1L;
    // Display is used via static methods — no instantiation needed.
    // Display.setEnabled(false) is called in initialize() — disabled by default.
    // Call Display.PhaseString(...) to show phase transitions.

    // -- Input Ports -- observations from [SystemName] --
    public final Port<String> [obs_port1];
    public final Port<String> [obs_port2];
    // ... one per observation port in the frame

    // -- Output Ports -- stop signal to Generator --
    public final Port<String> stop;

    // -- State variables --
    private double observationTime;
    private double clock = 0.0;

    // Report counters -- one per observation port
    private int [obs_port1]Reports = 0;
    private int [obs_port2]Reports = 0;
    // ...

    // Logs -- one per observation port
    private List<String> [obs_port1]Log = new ArrayList<String>();
    private List<String> [obs_port2]Log = new ArrayList<String>();
    // ...

    // Domain-specific event trackers
    private int [tracker1]Count = 0;
    private int [tracker2]Count = 0;
    // ...
```

**Step D3.4: Constructor**

```java
    public [ClassName](String name, double observationTime) {
        super(name);
        this.observationTime = observationTime;

        [obs_port1] = addInputPort("[obs_port1]", String.class);
        [obs_port2] = addInputPort("[obs_port2]", String.class);
        // ... one addInputPort per observation port
        stop = addOutputPort("stop", String.class);
    }
```

**Step D3.5: DEVS Methods**

| Method | Implementation |
|--------|---------------|
| `initialize()` | `super.initialize();` Reset clock=0.0, all counters=0, all logs=new ArrayList, all trackers=0. `holdIn("OBSERVING", observationTime);`, Call `Display.setEnabled(false);`, Call `Display.PhaseString(getName() + ": " + phase);` |
| `getTimeAdvance()` | `return sigma;` |
| `getOutput()` | If `phaseIs("OBSERVING")`: create `MessageBagImpl`, add stop message with summary: `"EF_STOP:observationTime=[X],totalReports=[Y],[port]Reports=[Z],..."`. Print observation window expired message. Return out. Else `MessageBag.EMPTY`. |
| `internalTransition()` | If `phaseIs("OBSERVING")`: `clock += sigma;` `passivateIn("DONE");` Compute throughput = totalReports / clock. Print formatted observation summary (see Step D3.6). Call `Display.PhaseString(getName() + ": " + phase);` |
| `externalTransition(double, MessageBag)` | `sigma -= timeElapsed; clock += timeElapsed;` For EACH observation port: get messages via `getMessages(input, port)`, if not empty: increment counter, append to log with timestamp, print receipt, check domain-specific event conditions and increment trackers. Call `Display.PhaseString(getName() + ": " + phase);` |
| `confluentTransition(MessageBag)` | `internalTransition(); externalTransition(0.0, input);` Call `Display.PhaseString(getName() + ": " + phase);` |

**Step D3.6: Observation Summary Format**

The `internalTransition()` method prints a formatted observation summary using box-drawing characters:

```
[Tag] ════════════════════════════════════════════════════════════════
[Tag]  [SYSTEM_NAME] TRANSDUCER -- OBSERVATION SUMMARY
[Tag] ════════════════════════════════════════════════════════════════
[Tag]  Observation Time:       [observationTime]
[Tag]  Final Clock:            [clock formatted %.2f]
[Tag]  ────────────────────────────────────────────────────
[Tag]  REPORT COUNTS:
[Tag]    [obs_port1]:           [count]
[Tag]    [obs_port2]:           [count]
[Tag]    TOTAL:                 [total]
[Tag]  ────────────────────────────────────────────────────
[Tag]  Throughput:             [throughput %.4f] reports/time-unit
[Tag]  ────────────────────────────────────────────────────
[Tag]  DOMAIN-SPECIFIC EVENTS:
[Tag]    [tracker1]:            [count]
[Tag]    [tracker2]:            [count]
[Tag]  ────────────────────────────────────────────────────
[Tag]  LAST [PORT] REPORT:
[Tag]    [last log entry from each port's log]
[Tag]  ────────────────────────────────────────────────────
[Tag]  Status:                 OBSERVATION COMPLETE
[Tag] ════════════════════════════════════════════════════════════════
```

Where `[Tag]` is `[ClassName]` (e.g., `[HealthEFTrans]`).

**Step D3.7: Accessor and Helper Methods**

Generate accessor methods for all counters, clock, observationTime, and event trackers:

```java
    public double getClock() { return clock; }
    public double getObservationTime() { return observationTime; }
    public int get[Port1]Reports() { return [port1]Reports; }
    public int get[Port2]Reports() { return [port2]Reports; }
    // ... one per observation port
    public int get[Tracker1]Count() { return [tracker1]Count; }
    // ... one per event tracker
```

Include the same `getMessages()` and `getFirstMessageValue()` helpers as the Generator.

**Step D3.8: main() Method with runTestCaseSimulation()**

Every Transducer file includes a standalone `main()` method and a `runTestCaseSimulation()` method that verifies the Transducer can initialize, hold in OBSERVING for its observation window, transition to DONE, produce a stop signal, and compute statistics — all without a Generator or System Model. This allows independent unit testing of the Transducer's observation and statistics logic.

```java
    // ══════════════════════════════════════════════════════════
    // MAIN — Standalone Transducer Test
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     [SystemName] EF Transducer — Standalone Test           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        final double TEST_OBS_TIME = [observation_time];
        final int MAX_ITERATIONS = 50;
        final double MAX_SIMULATION_TIME = TEST_OBS_TIME * 1.5;

        [TransducerClass] trans = new [TransducerClass]("Transducer_Test",
                TEST_OBS_TIME);

        runTestCaseSimulation(trans, MAX_ITERATIONS, MAX_SIMULATION_TIME);
    }

    /**
     * Run standalone test case simulation for the Transducer.
     * Verifies: initialization in OBSERVING, time advance equals
     * observationTime, transition to DONE after window expires,
     * stop signal production, and statistics computation.
     *
     * NOTE: Without a System Model providing input, the Transducer
     * will receive 0 reports. The test verifies structural correctness
     * (lifecycle, phases, timing) not data collection.
     */
    private static void runTestCaseSimulation([TransducerClass] trans,
            int maxIterations, double maxTime) {

        Simulation sim = new SimulationImpl("[TransducerClass] Test", trans);
        sim.startSimulation(0);

        System.out.println("┌──────┬──────────┬──────────────┬──────────┐");
        System.out.println("│ Iter │ SimTime  │ Phase        │ Clock    │");
        System.out.println("├──────┼──────────┼──────────────┼──────────┤");

        int iteration = 0;
        boolean stopConditionMet = false;
        String stopReason = "Max iterations reached";

        while (iteration < maxIterations && !stopConditionMet) {
            iteration++;

            try {
                sim.simulateIterations(1);
            } catch (Exception e) {
                System.err.println("Simulation error at iteration " + iteration
                        + ": " + e.getMessage());
                stopReason = "Simulation exception: " + e.getMessage();
                break;
            }

            double simTime = sim.getCurrentSimulationTime();
            String phase = trans.getPhase();
            double clock = trans.getClock();

            System.out.println(String.format(
                    "│ %4d │ %8.2f │ %-12s │ %8.2f │",
                    iteration, simTime, phase, clock));

            if (phase.equals("DONE")) {
                stopConditionMet = true;
                stopReason = "Transducer observation window expired (DONE)";
            }

            if (simTime >= maxTime) {
                stopReason = "Max simulation time reached (" + maxTime + ")";
                break;
            }
        }

        System.out.println("└──────┴──────────┴──────────────┴──────────┘");
        System.out.println();

        // ── Verification ─────────────────────────────────────
        System.out.println("── Transducer Test Results ─────────────────────────────");
        System.out.println("Stop Reason: " + stopReason);
        boolean testPassed = true;
        int passed = 0;
        int failed = 0;

        // V1: Transducer reached DONE
        if (trans.phaseIs("DONE")) {
            System.out.println("  PASS: Transducer reached DONE phase");
            passed++;
        } else {
            System.out.println("  FAIL: Transducer did not reach DONE (phase=" + trans.getPhase() + ")");
            testPassed = false;
            failed++;
        }

        // V2: Clock advanced to approximately observationTime
        double clockDiff = Math.abs(trans.getClock() - trans.getObservationTime());
        if (clockDiff < 0.01) {
            System.out.println("  PASS: Clock matches observationTime ("
                    + String.format("%.2f", trans.getClock()) + " ~ "
                    + String.format("%.2f", trans.getObservationTime()) + ")");
            passed++;
        } else {
            System.out.println("  FAIL: Clock mismatch (clock="
                    + String.format("%.2f", trans.getClock()) + ", obs="
                    + String.format("%.2f", trans.getObservationTime()) + ")");
            testPassed = false;
            failed++;
        }

        // V3: Observation time is positive
        if (trans.getObservationTime() > 0.0) {
            System.out.println("  PASS: Observation time is positive ("
                    + trans.getObservationTime() + ")");
            passed++;
        } else {
            System.out.println("  FAIL: Observation time is non-positive ("
                    + trans.getObservationTime() + ")");
            testPassed = false;
            failed++;
        }

        // V4: Report counts are non-negative (0 expected in standalone mode)
        // Check each observation port counter
        System.out.println("  PASS: Report counts are non-negative (standalone — 0 reports expected)");
        passed++;

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (testPassed) {
            System.out.println("*** TRANSDUCER TEST PASSED ***");
        } else {
            System.out.println("!!! TRANSDUCER TEST FAILED !!!");
        }
        System.out.println("────────────────────────────────────────────────────────");
    }
```

**Additional imports required for standalone Transducer test:**
```java
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
```

**Write Transducer Java source to EF_CONTEXT.java_structures.**

---

## D3B: Generate System Model Java File

**Purpose:** Generate one self-contained atomic DEVS model (extends `PhaseAtomic`) that serves as the System Under Test (SUT) for ALL selected experimental frames. This file declares the union of all ports referenced across all frames, implements a complete state machine, and exposes accessor methods used by EF Coupled models and Transducers. Generated ONCE per system (not per frame).

### Execution

**Step D3B.1: Collect Port Union**

Scan all selected frames' stimulus and observation ports to build the complete port sets:

- **Input ports (union):** Collect every unique stimulus port name across all selected frames' Generator output ports. Each becomes an input port on the System Model.
- **Output ports (union):** Collect every unique observation port name across all selected frames' Transducer input ports. Each becomes an output port on the System Model.

Record which frames use each port (for Javadoc annotation).

**Step D3B.2: File Header and Imports**

```java
package Models.java;

import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.impl.MessageBagImpl;
import com.ms4systems.devs.core.message.impl.MessageImpl;
import com.ms4systems.devs.core.message.Port;

import java.util.List;
// NOTE: Display and PhaseAtomic are in the same package (Models.java) — no import needed.
// Use static calls: Display.PhaseString(...), Display.setEnabled(false) (default)
```

**Step D3B.3: Class Javadoc**

Generate comprehensive Javadoc including:
- Class name and role (Atomic DEVS Model — System Under Test)
- System name and description
- List of ALL experimental frames this model serves, with question mappings
- Input ports with descriptions and which frames use each port
- Output ports with descriptions and which frames use each port
- States with descriptions and time advances
- DEVS formalism: `M = <X, Y, S, delta_int, delta_ext, lambda, ta>`
- EFSUT reference (Zeigler, 2026)

**Step D3B.4: Class Declaration and Fields**

```java
public class [SystemName]Model extends PhaseAtomic {

    private static final long serialVersionUID = 1L;
    // Display is used via static methods — no instantiation needed.
    // Display.setEnabled(false) is called in initialize() — disabled by default.
    // Call Display.PhaseString(...) to show phase transitions.

    // ── Input Ports ([N]) — stimulus from EF Generator ────────
    public final Port<String> [input_port1];
    public final Port<String> [input_port2];
    // ... one per unique input port across all frames

    // ── Output Ports ([M]) — observations to EF Transducer ────
    public final Port<String> [output_port1];
    public final Port<String> [output_port2];
    // ... one per unique output port across all frames

    // ── State Variables ─────────────────────────────────────
    // Domain-specific state variables derived from the system description
    // and the accessor methods needed by EF Coupled models and Transducers
    private double [stateVar1] = [default];
    private double [stateVar2] = [default];
    private int    [counter1]  = 0;
    // ...

    // ── Environment Variables ────────────────────────────────
    // Variables tracking external conditions (noise, adversary, etc.)
    private double [envVar1] = 0.0;
    // ...

    // ── Timing Variables ─────────────────────────────────────
    private double currentSimTime = 0.0;
    // Additional timing variables as needed for accessor computations

    // ── Thresholds ───────────────────────────────────────────
    // Constants governing state transitions
    private static final double [THRESHOLD_NAME] = [value];
    // ...

    // ── Time Advances ────────────────────────────────────────
    // Constants for holdIn() durations per state
    private static final double TA_[STATE1] = [value];
    private static final double TA_[STATE2] = [value];
    // ...

    // ── Pending Emit Mechanism ───────────────────────────────
    // Used to produce output on external transitions in DEVS
    // (output only fires before internal transitions)
    private boolean hasPendingEmit = false;
    private String  pendingPhase   = "";
    private double  pendingSigma   = Double.POSITIVE_INFINITY;
    private String  emit[Output1]  = null;
    private String  emit[Output2]  = null;
    // ... one emit variable per output port
```

**Step D3B.5: Constructor**

```java
    public [SystemName]Model(String name) {
        super(name);

        // Input ports
        [input_port1] = addInputPort("[input_port1]", String.class);
        [input_port2] = addInputPort("[input_port2]", String.class);
        // ... one addInputPort per unique input port

        // Output ports
        [output_port1] = addOutputPort("[output_port1]", String.class);
        [output_port2] = addOutputPort("[output_port2]", String.class);
        // ... one addOutputPort per unique output port
    }
```

**Constructor signature:** `(String name)` — single-argument, name only. The Coupled EF instantiates via `new [SystemName]Model("[SystemName]")`.

**Step D3B.6: DEVS Methods**

Generate all six required DEVS methods:

| Method | Implementation |
|--------|---------------|
| `initialize()` | `super.initialize();` Reset ALL state variables to defaults, clear pending emit, `passivateIn("[INITIAL_STATE]");`, Call `Display.setEnabled(false);`, Call `Display.PhaseString(getName() + ": " + phase);` |
| `getTimeAdvance()` | `return sigma;` |
| `getOutput()` | If `hasPendingEmit`: create `MessageBagImpl`, add non-null emit variables as messages on their respective output ports, return bag. Else call `emitForPhase()` which produces phase-specific output (e.g., emitting track_quality in TRACKING state). Return `MessageBag.EMPTY` if nothing to emit. |
| `internalTransition()` | If `hasPendingEmit`: save `pendingPhase` and `pendingSigma` to local variables, call `clearEmit()`, then `holdIn(savedPhase, savedSigma)`. Else: switch on current phase — each state defines its own next-state logic and `holdIn()`/`passivateIn()` call. Update `currentSimTime += sigma;` Call `Display.PhaseString(getName() + ": " + phase);` |
| `externalTransition(double, MessageBag)` | `sigma -= timeElapsed; currentSimTime += timeElapsed;` Parse ALL input port messages. Update environment variables. Switch on current phase — each state defines how it responds to each input. Use `scheduleEmit()` when output is needed (sets pending emit variables and calls `holdIn(currentPhase, 0.0)` to trigger output). Call `Display.PhaseString(getName() + ": " + phase);` |
| `confluentTransition(MessageBag)` | `internalTransition(); externalTransition(0.0, input);` Call `Display.PhaseString(getName() + ": " + phase);` |

**CRITICAL — Pending Emit Pattern:**

In DEVS, `getOutput()` only fires immediately before `internalTransition()`. To produce output in response to an external event:

1. `externalTransition()` calls `scheduleEmit(nextPhase, nextSigma, outputValues...)` which sets the emit variables and calls `holdIn(currentPhase, 0.0)`
2. The zero time advance triggers `getOutput()` which reads and emits the pending values
3. `internalTransition()` detects `hasPendingEmit`, saves `pendingPhase`/`pendingSigma` to local variables, clears the emit state via `clearEmit()`, then transitions to the saved phase with the saved sigma

**CRITICAL — Save Before Clear:** In `internalTransition()`, ALWAYS save `pendingPhase` and `pendingSigma` to local variables BEFORE calling `clearEmit()`, because `clearEmit()` resets them to `""`/`INFINITY`.

```java
    // CORRECT pattern:
    if (hasPendingEmit) {
        String nextPh = pendingPhase;
        double nextSg = pendingSigma;
        clearEmit();
        holdIn(nextPh, nextSg);
        return;
    }

    // INCORRECT — pendingPhase/pendingSigma are destroyed by clearEmit():
    // if (hasPendingEmit) {
    //     clearEmit();
    //     holdIn(pendingPhase, pendingSigma); // BUG: already cleared!
    // }
```

**Step D3B.7: State Machine Design**

Design the state machine based on the system description:

1. **Identify states:** Map system behaviors to discrete DEVS states. Each state needs:
   - A name (string constant, e.g., `"IDLE"`, `"TRACKING"`)
   - A time advance (how long the system stays in this state before autonomous transition)
   - An internal transition target (what state it moves to when time expires)
   - External transition handlers (how each input port affects this state)

2. **Initial state:** The state entered on `initialize()`, typically passive (ta=INFINITY) waiting for first input.

3. **State handlers in externalTransition:** For each state, create a handler method (e.g., `extIdle()`, `extTracking()`) that processes input messages and decides the next state. Keep `externalTransition()` as a clean `switch` dispatcher.

4. **Domain-specific logic:** Degradation checks, quality computation, threshold comparisons, and counter updates are implemented as private helper methods called from state handlers.

**Step D3B.8: Accessor Methods**

Generate accessor methods for ALL state variables that EF Coupled models or Transducers reference. Scan each Coupled EF's `runTestCaseSimulation()` and each Transducer's observation logic to identify required accessors:

```java
    // Accessor methods — called by EF test harnesses
    public double get[StateVar1]() { return [stateVar1]; }
    public double get[StateVar2]() { return [stateVar2]; }
    public int    get[Counter1]()  { return [counter1]; }
    // ...
    // getPhase() and phaseIs() are inherited from PhaseAtomic
```

**Step D3B.9: Helper Methods**

Generate standard helper methods:

```java
    // Schedule output emission on next internal transition
    private void scheduleEmit(String nextPhase, double nextSigma,
                              /* output values */) {
        hasPendingEmit = true;
        pendingPhase = nextPhase;
        pendingSigma = nextSigma;
        emit[Output1] = [value or null];
        emit[Output2] = [value or null];
        // ... set each emit variable (null = do not emit on that port)
        holdIn(getPhase(), 0.0);
    }

    // Clear all pending emit state
    private void clearEmit() {
        hasPendingEmit = false;
        pendingPhase = "";
        pendingSigma = Double.POSITIVE_INFINITY;
        emit[Output1] = null;
        emit[Output2] = null;
        // ... clear all emit variables
    }

    // Parse a string input to double (returns 0.0 on failure)
    private double parseInput(String val) {
        try { return Double.parseDouble(val.trim()); }
        catch (Exception e) { return 0.0; }
    }

    // Standard message helpers
    @SuppressWarnings("unchecked")
    private static List getMessages(MessageBag bag, Port<String> port) {
        return ((MessageBagImpl) bag).getMessages(port);
    }

    private static Object getFirstMessageValue(List ll) {
        MessageImpl m = (MessageImpl) ll.get(0);
        return m.getData();
    }

    // Format double to 4 decimal places
    private String fmt(double v) {
        return String.format("%.4f", v);
    }
```

**Step D3B.10: main() Method with runTestCaseSimulation()**

Every System Model file includes a standalone `main()` method and a `runTestCaseSimulation()` method that verifies the System Model can initialize, process a simulated sequence of external events, execute its state machine through multiple transitions, and produce output on its output ports — all without a Generator, Transducer, or Coupled EF. This allows independent unit testing of the SUT's state machine, transition logic, and accessor methods.

Because the System Model is an atomic model (no child components to send it messages), the standalone test uses a passive approach: it initializes the model and runs the simulation to verify the model starts in its initial state and responds correctly to its own time advances. For active testing of the state machine with injected stimuli, use the Coupled EF's `runTestCaseSimulation()`.

```java
    // ══════════════════════════════════════════════════════════
    // MAIN — Standalone System Model Test
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     [SystemName] Model — Standalone Test                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        final int MAX_ITERATIONS = 50;
        final double MAX_SIMULATION_TIME = 100.0;

        [SystemName]Model model = new [SystemName]Model("[SystemName]_Test");

        runTestCaseSimulation(model, MAX_ITERATIONS, MAX_SIMULATION_TIME);
    }

    /**
     * Run standalone test case simulation for the System Model.
     * Verifies: initialization in the correct initial state, port
     * declarations, accessor method availability, state variable
     * defaults, and basic structural integrity.
     *
     * NOTE: Without a Generator injecting stimuli, the System Model
     * will remain in its initial state (typically IDLE/passive).
     * The test verifies structural correctness and initialization,
     * not full behavioral coverage (which requires the Coupled EF).
     */
    private static void runTestCaseSimulation([SystemName]Model model,
            int maxIterations, double maxTime) {

        Simulation sim = new SimulationImpl("[SystemName]Model Test", model);
        sim.startSimulation(0);

        System.out.println("── System Model Initialization Check ──────────────────");
        System.out.println();

        boolean testPassed = true;
        int passed = 0;
        int failed = 0;

        // V1: Model starts in expected initial state
        String initialPhase = model.getPhase();
        if (initialPhase != null && !initialPhase.isEmpty()) {
            System.out.println("  PASS: Model initialized in phase: " + initialPhase);
            passed++;
        } else {
            System.out.println("  FAIL: Model has null or empty initial phase");
            testPassed = false;
            failed++;
        }

        // V2: Input port count matches expected
        // (Verify by checking that port fields are non-null)
        System.out.println("  PASS: Input ports declared and registered");
        passed++;

        // V3: Output port count matches expected
        System.out.println("  PASS: Output ports declared and registered");
        passed++;

        // V4: State variables initialized to valid defaults
        // (Domain-specific checks — generated per system)
        // For each state variable, verify its default is within valid range:
        //   if (model.get[StateVar]() >= [min] && model.get[StateVar]() <= [max]) {
        //       PASS
        //   } else { FAIL }
        System.out.println("  INFO: State variable defaults — verify via accessor methods:");
        // [Generated per-system: one check per state variable accessor]

        // V5: Run a few iterations to verify the model doesn't crash
        boolean simOk = true;
        int simIterations = 0;
        for (int i = 0; i < Math.min(10, maxIterations); i++) {
            try {
                sim.simulateIterations(1);
                simIterations++;
                double simTime = sim.getCurrentSimulationTime();
                if (simTime >= maxTime) break;
            } catch (Exception e) {
                System.out.println("  FAIL: Simulation error at iteration " + (i + 1)
                        + ": " + e.getMessage());
                simOk = false;
                testPassed = false;
                failed++;
                break;
            }
        }
        if (simOk) {
            System.out.println("  PASS: Model survived " + simIterations
                    + " simulation iterations without error");
            passed++;
        }

        // V6: Model phase after simulation is valid (non-null)
        String postPhase = model.getPhase();
        if (postPhase != null) {
            System.out.println("  PASS: Post-simulation phase is valid: " + postPhase);
            passed++;
        } else {
            System.out.println("  FAIL: Post-simulation phase is null");
            testPassed = false;
            failed++;
        }

        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (testPassed) {
            System.out.println("*** SYSTEM MODEL TEST PASSED ***");
        } else {
            System.out.println("!!! SYSTEM MODEL TEST FAILED !!!");
        }
        System.out.println("────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("NOTE: For full behavioral testing with stimulus injection,");
        System.out.println("      run the Coupled EF's main() method instead.");
    }
```

**Additional imports required for standalone System Model test:**
```java
import com.ms4systems.devs.core.simulation.Simulation;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
```

**Step D3B.11: Port Naming Rules**

- All port names use `snake_case` (e.g., `target_track`, `custody_report`)
- Port field names in the class match the port registration names exactly: `this.target_track = addInputPort("target_track", String.class);`
- All ports are `Port<String>` — values are passed as string representations
- Port names must exactly match between the System Model and the EF Generator/Transducer ports for coupling to work

**Write System Model Java source to EF_CONTEXT.java_structures.system_model.**

---

## D4: Generate Coupled EF Java File

**Purpose:** Generate the coupled DEVS model that wires the Generator, SUT, and Transducer together with N+M+1 internal couplings, and includes a `main()` method for simulation execution and test case verification. Reference: `resources/examples/HealthSystemEF.java`.

### Execution

**Step D4.1: File Header and Imports**

```java
package Models.java;

import com.ms4systems.devs.core.model.impl.CoupledModelImpl;
import com.ms4systems.devs.helpers.impl.SimulationOptionsImpl;
import com.ms4systems.devs.simviewer.standalone.SimViewer;
import com.ms4systems.devs.core.simulation.impl.SimulationImpl;
import com.ms4systems.devs.core.util.sesRelation;
import com.ms4systems.devs.core.util.sesRelationExtend;
```

**Step D4.2: Class Javadoc**

Generate comprehensive Javadoc including:
- Coupled model architecture diagram (ASCII art showing Generator -> SUT -> Transducer with port names)
- Complete list of internal couplings: N stimulus + M observation + 1 control = N+M+1 total
- Port names for each coupling group
- DEVS coupled formalism: `N = <X, Y, D, {M_d}, {I_d}, {Z_ij}, Select>`
- Statement: `"No External Ports (EIC/EOC): The EF is self-contained."`

**Step D4.3: Class Declaration -- Components and Defaults**

```java
public class [SystemName]_[FrameName]_EF extends CoupledModelImpl {

    private static final long serialVersionUID = 1L;
    protected SimulationOptionsImpl options = new SimulationOptionsImpl();

    // -- Component references for external monitoring --
    private [GeneratorClass] gen;
    private [SystemName]Model sys;   // Generated System Model (D3B)
    private [TransducerClass] trans;

    // -- Default parameters --
    private static final double DEFAULT_GEN_PERIOD = [period];
    private static final int DEFAULT_GEN_MAX_COUNT = [scenario_count];
    private static final double DEFAULT_OBS_TIME = [observation_time];
```

**Timing Rule:** `DEFAULT_OBS_TIME >= DEFAULT_GEN_PERIOD * DEFAULT_GEN_MAX_COUNT`

**Step D4.4: Constructors**

Generate three constructors:

```java
    // No-arg constructor
    public [ClassName]() {
        this("[ClassName]", DEFAULT_GEN_PERIOD, DEFAULT_GEN_MAX_COUNT,
                DEFAULT_OBS_TIME);
    }

    // Name-only constructor
    public [ClassName](String name) {
        this(name, DEFAULT_GEN_PERIOD, DEFAULT_GEN_MAX_COUNT,
                DEFAULT_OBS_TIME);
    }

    // Full constructor
    public [ClassName](String name, double genPeriod, int genMaxCount,
                       double observationTime) {
        super(name);

        // -- Component Instantiation --
        gen   = new [GeneratorClass]("Generator", genPeriod, genMaxCount);
        sys   = new [SystemName]Model("[SystemName]");  // Generated System Model (D3B)
        trans = new [TransducerClass]("Transducer", observationTime);

        addChildModel(gen);
        addChildModel(sys);
        addChildModel(trans);

        // -- Internal Couplings (IC) -- Generator -> System --
        // N stimulus couplings
        addCoupling(gen.[stim_port1], sys.[stim_port1]);
        addCoupling(gen.[stim_port2], sys.[stim_port2]);
        // ... one per stimulus port

        // -- Internal Couplings (IC) -- System -> Transducer --
        // M observation couplings
        addCoupling(sys.[obs_port1], trans.[obs_port1]);
        addCoupling(sys.[obs_port2], trans.[obs_port2]);
        // ... one per observation port

        // -- Internal Couplings (IC) -- Transducer -> Generator --
        // 1 stop coupling
        addCoupling(trans.stop, gen.stop);
    }
```

**Total IC = N + M + 1** (must match the frame's port specification exactly).

**Step D4.5: Component Accessors**

```java
    public [GeneratorClass] getGenerator() { return gen; }
    public [SystemName]Model getSystem() { return sys; }
    public [TransducerClass] getTransducer() { return trans; }
```

**Step D4.6: main() Method**

The `main()` method provides a complete simulation entry point:

```java
    public static void main(String[] args) throws ClassNotFoundException {
        // 1. Print test case header with box-drawing characters
        System.out.println("[banner]");

        // 2. Print test case parameters
        final double GEN_PERIOD = [period];
        final int GEN_MAX_COUNT = [scenario_count];
        final double OBSERVATION_TIME = [observation_time];
        final int MAX_ITERATIONS = 200;
        final double MAX_SIMULATION_TIME = [obs_time * 1.2];
        // Print parameter table and scenario list

        // 3. Build coupled model
        [ClassName] top = new [ClassName]("[ClassName]_TestCase",
                GEN_PERIOD, GEN_MAX_COUNT, OBSERVATION_TIME);
        top.initialize();

        // 4. SES mapping
        sesRelation ses = myDEVSToSES.mapDEVSModelToPlainSES(top);
        System.out.println(ses.printTreeString());
        System.out.println(myDEVSToSES.mapCoupledToSES(top));

        // 5. Simulation options
        SimulationOptionsImpl options = new SimulationOptionsImpl(args, true);
        top.options = options;
        options.setDisableViewer(True);

        // 6. Run simulation
        if (options.isDisableViewer()) {
            runTestCaseSimulation(top, MAX_ITERATIONS,
                    MAX_SIMULATION_TIME, options);
        } else {
            SimViewer viewer = new SimViewer();
            viewer.open(top, options);
        }
    }
```

**Step D4.7: runTestCaseSimulation() Method**

Generate the test case simulation method with these sections:

1. **Simulation setup**: Call `Display.setEnabled(false);` (mandatory default — prevents popup windows during headless/automated runs). Create `SimulationImpl`, call `sim.startSimulation(0)`.
2. **Iteration loop with monitoring table**:
   - Print table header with column names for key state variables from the SUT
   - Each iteration: `sim.simulateIterations(1)`
   - Read current simulation time via `double simTime = sim.getCurrentSimulationTime();` — this is the correct MS4 Systems method on `SimulationImpl`. Do NOT use `getSimulationTime()`, `getTime()`, or any other variant.
   - Read SUT state via accessor methods on `top.getSystem()` (e.g., `top.getSystem().getPhase()`, `top.getSystem().getCycleCount()`)
   - Check stop conditions: (a) transducer DONE + generator PASSIVE, (b) max time, (c) domain-specific termination
   - Print progress row every 5 iterations using `String.format()`
3. **Test case verification checks** (minimum 5 checks):
   - System received stimuli (cycle count or internal transition count > 0)
   - Transducer received reports on each observation port (count > 0)
   - Generator produced expected number of stimuli (count >= threshold)
   - State variables remained in valid ranges
   - System exited initial state
   - Domain-specific checks (e.g., expected event counts from trackers)
4. **Final results summary**: Print pass/fail counts, overall result

**MANDATORY: Stop Condition Enforcement**

Every `runTestCaseSimulation()` method MUST contain explicit stop conditions that are **guaranteed to be satisfiable** within the simulation's configured bounds. A simulation that cannot satisfy its stop condition will run infinitely through the iteration loop, consuming resources and producing no useful results.

**Requirements:**

1. **Every test case MUST have at least three independent stop conditions**, any one of which terminates the loop:
   - **(a) Logical completion**: `top.getTransducer().phaseIs("DONE") && top.getGenerator().phaseIs("PASSIVE")` — the Transducer's observation window has expired and the Generator has exhausted its stimuli or received the stop signal.
   - **(b) Max simulation time**: `simTime >= maxTime` — an absolute wall on simulated time. `maxTime` MUST be set to at least `1.2 * OBSERVATION_TIME` to allow the Transducer to reach DONE.
   - **(c) Max iteration count**: The `while` loop guard `iteration < maxIterations` — prevents infinite cycling even if the model deadlocks or enters a livelock. `maxIterations` MUST be >= `2 * GEN_MAX_COUNT * 10` to give enough headroom.

2. **Stop condition variables must be initialized before the loop:**
   ```java
   boolean stopConditionMet = false;
   String stopReason = "Max iterations reached";  // default fallback
   ```

3. **Every stop condition branch MUST set both `stopConditionMet = true` and a descriptive `stopReason` string** so the verification report clearly shows why the simulation terminated.

4. **Domain-specific stop conditions** (e.g., total health collapse, queue overflow, mission abort) are encouraged as additional termination criteria but MUST NOT be the only stop condition — the three mandatory conditions above must always be present.

**Stop Condition Verification Checklist (MUST be applied before writing the file):**

| # | Verification | How to Check |
|---|-------------|--------------|
| 1 | Loop has a finite upper bound | `while (iteration < maxIterations && !stopConditionMet)` — both guards present |
| 2 | `maxIterations` is set to a finite positive integer | Declared as `final int MAX_ITERATIONS = N;` where N > 0 |
| 3 | `maxTime` is set and checked inside the loop | `if (simTime >= maxTime)` with a `break` statement |
| 4 | Logical completion condition is present | `if (trans.phaseIs("DONE") && gen.phaseIs("PASSIVE"))` sets `stopConditionMet = true` |
| 5 | `stopConditionMet` is initialized to `false` | Declared before the loop |
| 6 | Every `stopConditionMet = true` has a `stopReason` assignment | No path sets the flag without explaining why |
| 7 | `maxTime >= 1.2 * OBSERVATION_TIME` | Prevents premature cutoff before Transducer completes |
| 8 | `maxIterations >= 2 * GEN_MAX_COUNT * 10` | Provides sufficient headroom for all stimulus-response cycles |
| 9 | No dead-code paths bypass the stop checks | All stop checks execute on every iteration (not gated behind `if` branches that may never fire) |
| 10 | The `stopReason` is printed in the results section | `System.out.println("Stop Reason: " + stopReason);` appears after the loop |

**If any row in the checklist above fails, the generated code MUST be corrected before the file is written.** Re-verify all 10 rows after any correction to prevent regressions.

**Verification Check Pattern:**

```java
        if ([condition]) {
            System.out.println("  PASS: [description]");
            passed++;
        } else {
            System.out.println("  FAIL: [description]");
            testPassed = false;
            failed++;
        }
```

**Step D4.8: printIterationRow() Helper**

```java
    private static void printIterationRow(int iter, double time,
            [state variable params]) {
        System.out.println(String.format(
                "| %4d | %8.2f | [format per variable] |",
                iter, time, [variables]));
    }
```

**Write Coupled EF Java source to EF_CONTEXT.java_structures.**

---

## D5: Verify and Write Java Files

**Purpose:** Verify the generated Java source files for structural correctness and EFSUT consistency, then write them to the output directory.

### Execution

**Step D5.1: Structural Verification**

Before writing, verify each generated Java file:

| Check | Verification |
|-------|-------------|
| **Package declaration** | `package Models.java;` present in all files |
| **Required imports** | All MS4 Systems imports present (MessageBag, MessageBagImpl, MessageImpl, Port for System Model/Generator/Transducer; CoupledModelImpl, SimulationOptionsImpl, SimViewer, SimulationImpl for Coupled EF) |
| **Class extends** | System Model, Generator, and Transducer extend `PhaseAtomic`; Coupled EF extends `CoupledModelImpl` |
| **serialVersionUID** | `private static final long serialVersionUID = 1L;` present in all files |
| **Port declarations** | All ports declared as `public final Port<String>` with correct direction (addOutputPort/addInputPort) |
| **Constructor signature** | System Model: `(String)`; Generator: `(String, double, int)`; Transducer: `(String, double)`; Coupled: `(String, double, int, double)` |
| **DEVS methods** | All 6 methods present in System Model, Generator, and Transducer: initialize, getTimeAdvance, getOutput, internalTransition, externalTransition, confluentTransition |
| **Helper methods** | `getMessages()` and `getFirstMessageValue()` present in System Model, Generator, and Transducer |
| **Scenario table** | `SCENARIOS` double[][] and `SCENARIO_NAMES` String[] present in Generator |
| **Standalone main() methods** | Present in Generator, Transducer, and System Model with standalone `runTestCaseSimulation()` |
| **Coupled EF main() method** | Present in Coupled EF with SimViewer, headless support, and full `runTestCaseSimulation()` |
| **System Model ports** | Union of ALL input/output ports across all selected frames present on the System Model |
| **Pending emit mechanism** | `scheduleEmit()` and `clearEmit()` methods present in System Model; `internalTransition()` saves pendingPhase/pendingSigma to locals before calling clearEmit() |
| **Display usage** | All `PhaseAtomic` subclasses (Generator, Transducer, System Model) call `Display.setEnabled(false);` in `initialize()` (mandatory default) and `Display.PhaseString(getName() + ": " + phase);` in all transition functions (static calls). No `new Display()` instantiation. No `import` for Display (same package `Models.java`). |
| **Simulation time method** | Coupled EF's `runTestCaseSimulation()` uses `sim.getCurrentSimulationTime()` — not `getSimulationTime()`, `getTime()`, or any other variant. The `sim` variable is of type `SimulationImpl`. |
| **Accessor methods** | System Model exposes all accessor methods called by any Coupled EF's `runTestCaseSimulation()` |

**Step D5.2: EFSUT Consistency Verification**

| Check | Verification |
|-------|-------------|
| **Question Coverage** | Every stakeholder question in `selected_frames` is addressed by at least one frame's Java files |
| **Port Union Completeness** | System Model declares the union of ALL input/output ports used across all selected frames |
| **Port Compatibility** | Generator output port names match System Model input port names; System Model output port names match Transducer input port names |
| **Coupling Completeness** | Exactly N stimulus + M observation + 1 stop = N+M+1 `addCoupling()` calls in Coupled EF constructor |
| **Scenario Completeness** | SCENARIOS array has >= 3 rows; each row has exactly N columns (one per stimulus port); SCENARIO_NAMES has same length as SCENARIOS |
| **Timing Constraints** | `DEFAULT_OBS_TIME >= DEFAULT_GEN_PERIOD * DEFAULT_GEN_MAX_COUNT` |
| **Accessor Coverage** | Coupled EF has getGenerator(), getSystem(), getTransducer(); System Model has all accessors called by any EF's runTestCaseSimulation(); Transducer has accessors for all counters and trackers |
| **Accessor Match** | Every `sys.getXxx()` call in any Coupled EF's `runTestCaseSimulation()` has a corresponding `public ... getXxx()` method on the System Model class |
| **Verification Checks** | runTestCaseSimulation() has >= 5 PASS/FAIL verification checks |
| **Stop Condition Completeness** | runTestCaseSimulation() has all 3 mandatory stop conditions: (a) logical completion (Transducer DONE + Generator PASSIVE), (b) max simulation time check with `break`, (c) max iterations in `while` guard. All 10 rows of the Stop Condition Verification Checklist (Step D4.7) pass. |
| **Stop Condition Satisfiability** | `MAX_SIMULATION_TIME >= 1.2 * OBSERVATION_TIME` and `MAX_ITERATIONS >= 2 * GEN_MAX_COUNT * 10`; no stop condition depends solely on a domain event that may never occur |

**Step D5.3: Generate Verification Report**

```
JAVA EF VERIFICATION REPORT for [SystemName]_[FrameName]
=====================================================

SYSTEM MODEL CHECKS:
  [PASS/FAIL] Package declaration
  [PASS/FAIL] Class extends PhaseAtomic
  [PASS/FAIL] serialVersionUID present
  [PASS/FAIL] All input ports declared ([N] total, union across all frames)
  [PASS/FAIL] All output ports declared ([M] total, union across all frames)
  [PASS/FAIL] Constructor signature: (String)
  [PASS/FAIL] DEVS methods (6/6): initialize, getTimeAdvance, getOutput,
              internalTransition, externalTransition, confluentTransition
  [PASS/FAIL] Pending emit mechanism (scheduleEmit, clearEmit)
  [PASS/FAIL] Save-before-clear in internalTransition
  [PASS/FAIL] Accessor methods match all EF references
  [PASS/FAIL] Helper methods (getMessages, getFirstMessageValue, parseInput)
  [PASS/FAIL] Display.setEnabled(false) in initialize() (mandatory default)
  [PASS/FAIL] Display uses static calls (Display.PhaseString, no `new Display()`)

STRUCTURAL CHECKS (per EF frame):
  [PASS/FAIL] Package declaration (3/3 EF files)
  [PASS/FAIL] Required imports
  [PASS/FAIL] Class inheritance (PhaseAtomic x2, CoupledModelImpl x1)
  [PASS/FAIL] serialVersionUID (3/3 EF files)
  [PASS/FAIL] Port declarations ([N] output + 1 stop in Generator,
              [M] input + 1 stop in Transducer)
  [PASS/FAIL] Constructor signatures
  [PASS/FAIL] DEVS methods (6/6 in Generator, 6/6 in Transducer)
  [PASS/FAIL] Helper methods (getMessages, getFirstMessageValue)
  [PASS/FAIL] Display.setEnabled(false) in initialize() (mandatory default, all 3 EF files)
  [PASS/FAIL] Display uses static calls (Display.PhaseString, no `new Display()`)
  [PASS/FAIL] Scenario table ([K] scenarios x [N] ports)
  [PASS/FAIL] Coupled EF main() method with SimViewer support
  [PASS/FAIL] Simulation time uses sim.getCurrentSimulationTime() (not getTime/getSimulationTime)

STANDALONE TEST METHODS:
  [PASS/FAIL] Generator has main() with standalone runTestCaseSimulation()
  [PASS/FAIL] Generator runTestCaseSimulation() validates: stimuli produced, PASSIVE reached, count matches, period positive
  [PASS/FAIL] Generator includes Simulation/SimulationImpl imports
  [PASS/FAIL] Generator has getMaxCount() accessor
  [PASS/FAIL] Transducer has main() with standalone runTestCaseSimulation()
  [PASS/FAIL] Transducer runTestCaseSimulation() validates: DONE reached, clock matches obs time, obs time positive
  [PASS/FAIL] Transducer includes Simulation/SimulationImpl imports
  [PASS/FAIL] System Model has main() with standalone runTestCaseSimulation()
  [PASS/FAIL] System Model runTestCaseSimulation() validates: initial phase, port registration, simulation stability
  [PASS/FAIL] System Model includes Simulation/SimulationImpl imports

EFSUT CONSISTENCY:
  [PASS/FAIL] Question Coverage
  [PASS/FAIL] Port Union Completeness (System Model covers all frames)
  [PASS/FAIL] Port Compatibility (Generator->SystemModel, SystemModel->Transducer)
  [PASS/FAIL] Coupling Completeness ([N+M+1] addCoupling calls)
  [PASS/FAIL] Scenario Completeness
  [PASS/FAIL] Timing Constraints (obs >= period * count)
  [PASS/FAIL] Accessor Coverage (System Model + Transducer)
  [PASS/FAIL] Accessor Match (every sys.getXxx() has corresponding method)
  [PASS/FAIL] Verification Checks (>= 5 in runTestCaseSimulation)
  [PASS/FAIL] Stop Condition Completeness (3 mandatory stops: logical, max time, max iterations)
  [PASS/FAIL] Stop Condition Satisfiability (MAX_SIM_TIME >= 1.2*OBS_TIME, MAX_ITER >= 2*COUNT*10)
  [PASS/FAIL] Stop Condition Verification Checklist (all 10 rows from D4.7 pass)

ERRORS: [count]
  [list if any]

WARNINGS: [count]
  [list if any]

RESULT: [VALID / INVALID]
=====================================================
```

**Step D5.4: Write Java Files**

Write the System Model and EF Java files to the output directory:

| File | Output Path |
|------|-------------|
| System Model | `output/[SystemName]/java/[SystemName]Model.java` |
| Generator | `output/[SystemName]/java/[SystemName]_[FrameName]_EFGenerator.java` |
| Transducer | `output/[SystemName]/java/[SystemName]_[FrameName]_EFTransducer.java` |
| Coupled EF | `output/[SystemName]/java/[SystemName]_[FrameName]_EF.java` |

### File Naming Convention

| Scenario | Files Generated |
|----------|----------------|
| Single frame selected | 4 files: `[SystemName]Model.java` (SUT), `[SystemName]_[FrameName]_EFGenerator.java`, `[SystemName]_[FrameName]_EFTransducer.java`, `[SystemName]_[FrameName]_EF.java` |
| Multiple frames | 1 System Model file (shared) + 3 EF files per frame (each frame gets its own complete, independent set) |

**Write Java file(s) to `output/[SystemName]/java/`.** Create the project directory and `java/` subdirectory if they do not exist.
**Write verification results to EF_CONTEXT.verification.**

---

# DEVELOPMENTAL VERIFICATION (DV): Maximal EF Cross-Check

After completing the Developmental Phase (D5) and writing all Java files, perform a comprehensive cross-check of the generated code against the Maximal EF specification from the Conceptual Verification. This catches port mismatches, missing accessors, orphan couplings, and state reachability gaps that may have been introduced during code generation.

---

## Step DV1: System Model vs Maximal EF Port Audit

Verify that the System Model Java file declares all ports from the Maximal EF port union:

1. **Scan System Model**: Extract all `addInputPort()` and `addOutputPort()` calls from the generated `[SystemName]Model.java`.
2. **Compare input ports**: Every port in `port_union_input` (from CV1) must appear as an `addInputPort()` in the System Model. Flag any missing input ports.
3. **Compare output ports**: Every port in `port_union_output` (from CV1) must appear as an `addOutputPort()` in the System Model. Flag any missing output ports.
4. **Check for extra ports**: Any port in the System Model that does NOT appear in the Maximal EF port union is an extra port. This is a warning (the System Model may declare utility ports), not an error.

**Output format:**

```
SYSTEM MODEL vs MAXIMAL EF PORT AUDIT (DV1)
═══════════════════════════════════════════════
System Model: [ClassName].java

INPUT PORTS:
  Port Name              Maximal EF    System Model    Match
  ─────────────────────  ──────────    ────────────    ─────
  [port_name]            YES           YES             [OK]
  [port_name]            YES           NO              [FAIL]
  ...

OUTPUT PORTS:
  Port Name              Maximal EF    System Model    Match
  ─────────────────────  ──────────    ────────────    ─────
  [port_name]            YES           YES             [OK]
  ...

EXTRA PORTS (in System Model but not in Maximal EF):
  [port_name]   Direction: [in/out]   [WARN]

RESULT: [PASS / FAIL]
```

---

## Step DV2: Accessor Coverage Matrix

Verify that every accessor method called across all Coupled EF `runTestCaseSimulation()` methods exists on the System Model and Transducer classes:

1. **Scan all Coupled EF files**: For each `[FrameName]EF.java`, extract all `top.getSystem().getXxx()` and `top.getTransducer().getXxx()` calls from `runTestCaseSimulation()`.
2. **Scan System Model**: Extract all `public ... getXxx()` method signatures.
3. **Scan each Transducer**: Extract all `public ... getXxx()` method signatures.
4. **Cross-reference**: For each accessor call, confirm a matching method exists. Flag any unresolved call.

**Output format:**

```
ACCESSOR COVERAGE MATRIX (DV2)
═══════════════════════════════
                        EF_1    EF_2    EF_3    ...    Declared
  sys.getLatency()       X       X       -             YES  [OK]
  sys.getSnr()           -       X       -             YES  [OK]
  sys.getPhase()         X       X       X             YES  [OK]  (inherited)
  trans.getXxx()         X       -       -             YES  [OK]
  sys.getMissing()       X       -       -             NO   [FAIL]
  ...

Total Calls:     [count]
Resolved:        [count]
Unresolved:      [count]

RESULT: [PASS / FAIL]
```

---

## Step DV3: Port Activity Matrix

Verify that every output port on the System Model is actually emitted (written to) in at least one state of the internal/external transition logic:

1. **Scan System Model**: For each output port declared via `addOutputPort()`, search the `getOutput()`, `internalTransition()`, and `externalTransition()` methods for code that emits a value on that port.
2. **Build activity matrix**: Map each output port to the states in which it is emitted.
3. **Flag inactive ports**: Any output port that is never emitted is a dead port — it will never produce output, so any Transducer wired to it will never receive data.

**Output format:**

```
PORT ACTIVITY MATRIX (DV3)
═══════════════════════════════
Output Port          NOMINAL  DEGRADED  REROUTING  DISRUPTED  FAILED  RECOVERING
─────────────────    ───────  ────────  ─────────  ─────────  ──────  ──────────
[port_name]            X        X          X          X         -        X
[port_name]            X        X          X          X         X        X
[port_name]            -        -          -          -         -        -    [DEAD]
...

Active Ports:    [count] / [total]
Dead Ports:      [count]

RESULT: [PASS / WARN]
```

- **WARN** if dead ports exist.
- **PASS** if all output ports are emitted in at least one state.

---

## Step DV4: Coupling Subsumption Check

Verify that each individual Coupled EF's couplings are a proper subset of what the Maximal EF would require:

1. **For each Coupled EF file**: Extract all `addCoupling(src.port, dst.port)` calls.
2. **Verify Generator → System couplings**: Each `gen.portX -> sys.portX` coupling must reference ports that exist on both the Generator's output ports and the System Model's input ports.
3. **Verify System → Transducer couplings**: Each `sys.portY -> trans.portY` coupling must reference ports that exist on both the System Model's output ports and the Transducer's input ports.
4. **Verify stop coupling**: `trans.stop -> gen.stop` must be present.
5. **Coupling count**: Verify `addCoupling()` count equals `N_i + M_i + 1` for each frame.
6. **Subsumption**: Verify each frame's coupling set is a subset of `{port_union_input} × {port_union_output} ∪ {stop}`.

**Output format:**

```
COUPLING SUBSUMPTION CHECK (DV4)
═══════════════════════════════════
Frame: [FrameName]EF
  Generator -> System:  [N_i] couplings   all valid: [YES/NO]
  System -> Transducer: [M_i] couplings   all valid: [YES/NO]
  Stop coupling:        [YES/NO]
  Total:                [N_i + M_i + 1]   expected: [N_i + M_i + 1]   [OK/MISMATCH]
  Subsumption:          subset of Maximal EF: [YES/NO]
  RESULT: [PASS / FAIL]
...
OVERALL RESULT: [PASS / FAIL]
```

---

## Step DV5: State Reachability Trace

Verify that each Generator's scenario table can drive the System Model through the target states identified in CV3:

1. **For each Generator file**: Extract the `SCENARIOS[][]` table and `TARGET_STATES[]` array.
2. **Map scenario parameters to System Model transitions**: Using the System Model's state transition thresholds (from `externalTransition()`), verify that each scenario's parameter vector would cause the expected state transition.
3. **Trace through the sequence**: Starting from the initial state (typically NOMINAL), apply each scenario in order and track the resulting state. Compare against `TARGET_STATES[]`.
4. **Flag mismatches**: If a scenario's parameters would NOT produce the target state (based on the System Model's threshold logic), flag it.

**Output format:**

```
STATE REACHABILITY TRACE (DV5)
═══════════════════════════════
Frame: [FrameName]EF
  Scenario    Parameters                      Target State    Achieved    Match
  ────────    ──────────────────────────────  ──────────────  ──────────  ─────
  1           noise=0.1, solar=0.0, ...       NOMINAL         NOMINAL     [OK]
  2           noise=0.5, solar=0.4, ...       DEGRADED        DEGRADED    [OK]
  3           noise=0.3, solar=0.2, adv=1.0   DISRUPTED       DISRUPTED   [OK]
  ...

  States Traced:    [count] / [declared]
  RESULT: [PASS / FAIL]
...
OVERALL RESULT: [PASS / FAIL]
```

---

## Step DV6: Generate Developmental Verification Report

Produce a formatted summary of DV1–DV5 results:

```
╔════════════════════════════════════════════════════════════╗
║      DEVELOPMENTAL VERIFICATION REPORT (Maximal EF)       ║
╚════════════════════════════════════════════════════════════╝

  System Model:       [ClassName].java
  Frames Verified:    [count]
  Files Checked:      [count]

  DV1  System Model Port Audit:      [PASS / FAIL]
  DV2  Accessor Coverage Matrix:     [PASS / FAIL]
  DV3  Port Activity Matrix:         [PASS / WARN]
  DV4  Coupling Subsumption Check:   [PASS / FAIL]
  DV5  State Reachability Trace:     [PASS / FAIL]

  OVERALL:  [PASS / FAIL]
```

- **If PASS**: Record `EF_CONTEXT.verification.maximal_ef.dv_passed = true`. Proceed to OUTPUT 2.
- **If FAIL**: Stop. Report failures. The user must resolve them before delivery. Common resolutions:
  - Missing accessor → add to System Model (D3B Step D3B.8)
  - Dead output port → add emit logic in appropriate state handler
  - Coupling mismatch → fix addCoupling() calls in Coupled EF (D4)
  - State trace mismatch → adjust scenario parameters or System Model thresholds

---

## OUTPUT 2: System Model and EF DEVS Java Files

**Produced after:** Developmental Phase (D5 complete)

**Files:** `output/[SystemName]/java/[SystemName]Model.java`, `output/[SystemName]/java/[SystemName]_[FrameName]_EFGenerator.java`, `output/[SystemName]/java/[SystemName]_[FrameName]_EFTransducer.java`, `output/[SystemName]/java/[SystemName]_[FrameName]_EF.java`

The System Model and EF DEVS Java Files are the formal deliverables of the Developmental Phase. One System Model is generated per system, plus three compilable EF Java source files per selected frame, all for the MS4 Systems DEVS framework:

| File | Class Extends | Contents |
|------|--------------|----------|
| **[SystemName]Model.java** | `PhaseAtomic` | Self-contained SUT with union of ALL ports across all frames, complete state machine, pending emit mechanism, accessor methods for EF test harnesses, standalone `main()` with `runTestCaseSimulation()` for initialization and structural verification |
| **EFGenerator.java** | `PhaseAtomic` | Scenario table (SCENARIOS[][]), stimulus output ports, GENERATING/PASSIVE states, count-based injection loop, stop port, standalone `main()` with `runTestCaseSimulation()` verifying scenario cycling, count progression, and PASSIVE termination |
| **EFTransducer.java** | `PhaseAtomic` | Observation input ports, per-port counters and logs, domain-specific event trackers, formatted observation summary, stop port, standalone `main()` with `runTestCaseSimulation()` verifying OBSERVING-to-DONE lifecycle and timing accuracy |
| **Coupled EF.java** | `CoupledModelImpl` | Component wiring (N+M+1 internal couplings), main() with SES mapping and SimViewer/headless, `runTestCaseSimulation()` with iteration monitoring, full system integration test, and PASS/FAIL verification |

### Test Execution Hierarchy

Each generated Java file can be executed independently via its `main()` method, forming a bottom-up test hierarchy:

```
Level 1 (Unit):       Generator.main()     Transducer.main()     SystemModel.main()
                      (scenario cycling)    (observation window)   (initialization)
                           |                      |                     |
Level 2 (Integration):    +----------------------+---------------------+
                                              |
                                    CoupledEF.main()
                                    (full EF simulation with
                                     Generator -> SUT -> Transducer)
```

- **Level 1 tests** verify each component in isolation — no dependencies on other components
- **Level 2 test** verifies the complete experimental frame with all components wired together

---

# SEMANTIC CONTINUITY SUMMARY

| Step | Reads From | Writes To | Key Checks |
|------|------------|-----------|------------|
| 0 | User input | `system` | Ports, states, variables identified |
| C1 | User input, system description | `stakeholder_questions` | Questions categorized and prioritized |
| C2 | `stakeholder_questions`, `system` | `candidate_frames` | Every question addressed by >= 1 frame |
| C3 | `candidate_frames` | `ef_partial_order`, `best_model_table` | Derivability correct, best models identified |
| C3.4 | `ef_partial_order` | `derivability_relation` | Formal edges, transitivity, Hasse diagram, properties computed |
| C3.5 | `best_model_table`, `candidate_frames`, `system` | `applicability_matrix` | Every frame-model pair classified as Best/Yes/— |
| C3.6 | `stakeholder_questions`, `candidate_frames`, `best_model_table` | `efsut_table` | Every question mapped to frame, model, and objectives |
| C4 | `candidate_frames`, `ef_partial_order`, `efsut_table` | (presentation only) | All frames presented with context |
| C5 | User selection | `selected_frames` | At least one frame selected |
| **OUTPUT 1a** | All Conceptual Phase data | `EF Analysis Report (.md)` | Complete reasoning captured |
| **OUTPUT 1b** | `efsut_table` | `EFSUT Table (.md)` | Question-Frame-Model-Objectives mapping complete |
| **CV1** | All `candidate_frames` | `maximal_ef` specification | Port union (input/output), combined scenarios, coupling count computed |
| **CV2** | `maximal_ef`, `system` | CV report | Every frame port exists in system; orphan ports flagged |
| **CV3** | `maximal_ef`, `system` | CV report | Combined scenarios cover all declared states |
| **CV4** | `maximal_ef`, `candidate_frames` | CV report | Each frame derivable from Maximal EF via selection/aggregation/coarsening |
| **CV5** | CV1–CV4 results | CV report | `cv_passed` set; gate to Developmental Phase |
| D1 | `selected_frames`, `system` | `java_structures` (class design) | 1 System Model + 3 EF files per frame designed; port union, scenarios, trackers planned |
| D2 | `candidate_frames`, `system`, `java_structures` | `java_structures` (Generator source) | Generator extends PhaseAtomic; SCENARIOS table complete; 6 DEVS methods present; standalone main() with runTestCaseSimulation() |
| D3 | `candidate_frames`, `system`, `java_structures` | `java_structures` (Transducer source) | Transducer extends PhaseAtomic; per-port counters/logs; event trackers; formatted summary; standalone main() with runTestCaseSimulation() |
| D3B | `selected_frames`, `system`, `java_structures` | `java_structures` (System Model source) | System Model extends PhaseAtomic; port union across all frames; state machine; pending emit mechanism; accessor methods; standalone main() with runTestCaseSimulation() |
| D4 | `candidate_frames`, `system`, `java_structures` | `java_structures` (Coupled EF source) | CoupledModelImpl with N+M+1 addCoupling calls; instantiates [SystemName]Model; main() with SimViewer; runTestCaseSimulation() (full integration test) |
| D5 | All previous context | `outputs.java_files`, `verification` | Structural + EFSUT consistency checks pass; System Model + 3 EF files per frame written |
| **DV1** | Generated Java files, `maximal_ef` | DV report | System Model declares all ports from Maximal EF port union |
| **DV2** | All Coupled EF files, System Model, Transducers | DV report | Every `sys.getXxx()` and `trans.getXxx()` call resolves to a real method |
| **DV3** | System Model source | DV report | Every output port emitted in at least one state; dead ports flagged |
| **DV4** | All Coupled EF files, `maximal_ef` | DV report | Each EF's couplings are subset of Maximal EF; coupling counts match N+M+1 |
| **DV5** | Generator SCENARIOS, System Model thresholds | DV report | Scenario parameters produce target states per System Model transition logic |
| **DV6** | DV1–DV5 results | DV report | `dv_passed` set; gate to OUTPUT 2 |
| **OUTPUT 2** | All Developmental Phase data | `System Model + EF DEVS Java Files (.java)` | 1 System Model + 3 compilable EF Java files per selected frame |

---

# TROUBLESHOOTING

### Issue: Candidate frame specifies ports not present on the system
**Cause:** Frame observation or stimulus ports reference ports the system does not have.
**Solution:** Re-check the system interface (Step 0). Either narrow the frame's port scope or update the system description to include the missing ports.

### Issue: Partial order has no derivability edges
**Cause:** All candidate frames are independent (no frame's data is derivable from another's).
**Solution:** This is valid. Independent frames address orthogonal questions. No action needed unless frames should be related.

### Issue: Best-model table maps all frames to the same model
**Cause:** All frames require the same level of model fidelity.
**Solution:** This can occur when frames differ only in scenarios, not in observed ports. Consider whether coarser models could serve some frames.

### Issue: Generator output ports do not match System Model input ports
**Cause:** Port names declared in the Generator's `addOutputPort()` calls do not match the corresponding `addInputPort()` names on the System Model.
**Solution:** Verify that every Generator output port name exactly matches a System Model input port name. Port names are case-sensitive. Re-check the system interface (Step 0), the frame's stimulus port list (C2), and the port union in D3B.

### Issue: System Model missing accessor method called by EF
**Cause:** A Coupled EF's `runTestCaseSimulation()` calls `sys.getXxx()` but the System Model class does not have a corresponding `getXxx()` method.
**Solution:** Add the missing accessor method to the System Model. Re-run D3B Step D3B.8 to scan all EF references and ensure every accessor is present.

### Issue: Pending emit values lost in internalTransition
**Cause:** `clearEmit()` is called before saving `pendingPhase`/`pendingSigma` to local variables in `internalTransition()`, destroying the values before they can be used.
**Solution:** Save `pendingPhase` and `pendingSigma` to local variables BEFORE calling `clearEmit()`. See D3B Step D3B.6 for the correct pattern.

### Issue: Observation window too short for scenario count
**Cause:** Constraint violated: `DEFAULT_OBS_TIME < DEFAULT_GEN_PERIOD * DEFAULT_GEN_MAX_COUNT`.
**Solution:** Increase `DEFAULT_OBS_TIME` or decrease `DEFAULT_GEN_PERIOD`/`DEFAULT_GEN_MAX_COUNT` in the Coupled EF defaults.

### Issue: Missing DEVS methods in Generator or Transducer
**Cause:** One or more of the 6 required DEVS methods (`initialize`, `getTimeAdvance`, `getOutput`, `internalTransition`, `externalTransition`, `confluentTransition`) is absent from the generated Java file.
**Solution:** Re-run D2 (Generator) or D3 (Transducer) generation. All 6 methods are mandatory for `PhaseAtomic` subclasses.

### Issue: SCENARIO_NAMES length does not match SCENARIOS row count
**Cause:** The `SCENARIO_NAMES` String array has a different length than the `SCENARIOS` double[][] array.
**Solution:** Ensure both arrays have exactly the same number of entries. Each scenario row must have a corresponding name.

### Issue: Coupling count mismatch in Coupled EF constructor
**Cause:** The number of `addCoupling()` calls does not equal N (stimulus) + M (observation) + 1 (stop).
**Solution:** Count the Generator output ports (N), Transducer input ports excluding stop (M), and verify exactly N + M + 1 `addCoupling()` calls exist in the Coupled EF constructor.

### Issue: runTestCaseSimulation() has fewer than 5 verification checks
**Cause:** Insufficient test coverage in the generated Coupled EF.
**Solution:** Add domain-specific verification checks. At minimum: (1) SUT received stimuli, (2) Transducer received reports on each port, (3) Generator produced expected count, (4) state variables in valid ranges, (5) system exited initial state.

### Issue: runTestCaseSimulation() runs infinitely / never terminates
**Cause:** The stop condition is either missing, unreachable, or depends on a domain event that never fires. Common sub-causes:
- The `while` loop lacks a `maxIterations` guard (loops on `while (true)` or `while (!stopConditionMet)` with no iteration cap).
- `maxTime` is checked but never reaches because the model enters a livelock (alternating states with zero-time advances).
- The logical completion check (`Transducer DONE && Generator PASSIVE`) is absent or uses the wrong phase string (e.g., `"Done"` vs `"DONE"`).
- `stopConditionMet` is never set to `true` because the only stop condition is a domain-specific event that the current scenario does not trigger.

**Solution:** Apply the full **Stop Condition Verification Checklist** from Step D4.7. Specifically:
1. Ensure the `while` loop has BOTH guards: `iteration < maxIterations` AND `!stopConditionMet`.
2. Ensure `maxTime` is checked inside the loop with a `break` statement, and that `MAX_SIMULATION_TIME >= 1.2 * OBSERVATION_TIME`.
3. Ensure `MAX_ITERATIONS >= 2 * GEN_MAX_COUNT * 10`.
4. Ensure the logical completion condition uses exact phase strings matching the Transducer and Generator implementations (case-sensitive).
5. Ensure at least one stop condition is unconditionally satisfiable (the iteration cap and max time bounds guarantee this).
6. After fixing, re-verify all 10 rows of the checklist before writing the file.

---

# REFERENCES

- Zeigler, B.P. (2026). "Experimental Frame-System Under Test (EFSUT): A Principled Foundation for Model Choice and Lifecycle Management in Digital Twins." *Digital Engineering and Digital Twin*, 4. DOI: 10.32604/dedt.2026.082492
- Zeigler, B.P., Muzy, A., & Kofman, E. (2019). *Theory of Modeling and Simulation*, 3rd ed. Academic Press.
- Zeigler, B.P. (2000). *Theory of Modeling and Simulation*, 2nd ed. Academic Press.

---

## Related Skills

- [devs-builder](../devs-builder/) -- Generate DEVS models from system descriptions
- [ses-builder](../ses-builder/) -- Build System Entity Structure (SES) hierarchies
- [pwsa-expert](../pwsa-expert/) -- Expert on PWSA mission engineering and DEVS-PWSA framework
