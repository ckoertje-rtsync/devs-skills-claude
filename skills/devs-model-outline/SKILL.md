---
name: devs-model-outline
description: "Create a DEVS model specification from input/documentation or outline an existing DEVS model from project source files. Produces a suite of Markdown files with Mermaid UML diagrams including state diagrams for atomic models, flow and sequence diagrams for coupled models."
---

# DEVS Model Outline

## What This Skill Does

Produces comprehensive DEVS (Discrete Event System Specification) model documentation as a suite of Markdown files with embedded Mermaid UML diagrams. Operates in two modes:

- **CREATE mode**: Given a system description or requirements documentation, autonomously infers the complete DEVS model structure — states, ports, transitions, time advances, couplings — and generates formal specification documents with visual diagrams.
- **ANALYZE mode**: Given a project containing existing DEVS model source files (`.java` MS4Systems Basic-DEVS or `.pdm` PowerDEVS), parses the code to extract the model structure and generates equivalent documentation.

Both modes produce the same output: 9 interlinked Markdown files covering formal specifications, state diagrams, coupling topologies, interaction sequences, model hierarchy, and test scenarios.

## Workflow Overview

```mermaid
stateDiagram-v2
    [*] --> Phase0_SCAN

    state Phase0_SCAN {
        [*] --> scan_templates
        [*] --> scan_agentdb
        [*] --> scan_project
        scan_templates: Glob templates/*.md
        scan_agentdb: AgentDB pattern-search
        scan_project: Glob project files<br/>(ANALYZE mode only)
        scan_templates --> build_catalog
        scan_agentdb --> build_catalog
        scan_project --> build_catalog
        build_catalog: Build catalog → MODEL_CONTEXT
    }

    Phase0_SCAN --> Phase1_DISCOVER

    state Phase1_DISCOVER {
        state mode_fork <<choice>>
        [*] --> mode_fork
        mode_fork --> CREATE: description provided
        mode_fork --> ANALYZE: project files found
        mode_fork --> BOTH: description + files

        state CREATE {
            [*] --> infer_model_type
            [*] --> identify_states
            [*] --> map_ports
            [*] --> determine_transitions
            infer_model_type --> merge_create
            identify_states --> merge_create
            map_ports --> merge_create
            determine_transitions --> merge_create
            merge_create: Merge & present for confirmation
        }

        state ANALYZE {
            [*] --> parse_java
            [*] --> parse_pdm
            parse_java: Parse .java files<br/>(ports, states, transitions)
            parse_pdm: Parse .pdm files<br/>(atomics, lines, params)
            parse_java --> merge_analyze
            parse_pdm --> merge_analyze
            merge_analyze: Merge & present for confirmation
        }

        state BOTH {
            [*] --> parse_sources
            [*] --> process_description
            parse_sources --> merge_both
            process_description --> merge_both
            merge_both: Merge & present for confirmation
        }
    }

    Phase1_DISCOVER --> Phase2_FORMALIZE

    state Phase2_FORMALIZE {
        [*] --> validate_atomic
        [*] --> validate_coupled
        [*] --> gen_test_scenarios
        [*] --> build_hierarchy
        validate_atomic: Validate atomic specs<br/>(completeness, reachability)
        validate_coupled: Validate coupled specs<br/>(port consistency, coupling validity)
        gen_test_scenarios: Generate test scenarios
        build_hierarchy: Build hierarchy tree
        validate_atomic --> compile_issues
        validate_coupled --> compile_issues
        gen_test_scenarios --> compile_issues
        build_hierarchy --> compile_issues
        compile_issues: Compile validation summary
    }

    Phase2_FORMALIZE --> Phase3_DIAGRAM

    state Phase3_DIAGRAM {
        [*] --> gen_state_diagrams
        [*] --> gen_flow_diagrams
        [*] --> gen_sequence_diagrams
        [*] --> gen_class_diagram
        [*] --> gen_hierarchy_diagram
        gen_state_diagrams: stateDiagram-v2<br/>(per atomic model)
        gen_flow_diagrams: flowchart TD<br/>(per coupled model)
        gen_sequence_diagrams: sequenceDiagram<br/>(per coupled model)
        gen_class_diagram: classDiagram<br/>(overall structure)
        gen_hierarchy_diagram: graph BT<br/>(composition tree)
        gen_state_diagrams --> validate_mermaid
        gen_flow_diagrams --> validate_mermaid
        gen_sequence_diagrams --> validate_mermaid
        gen_class_diagram --> validate_mermaid
        gen_hierarchy_diagram --> validate_mermaid
        validate_mermaid: Compile-test with mmdc
    }

    Phase3_DIAGRAM --> Phase4_DEPLOY

    state Phase4_DEPLOY {
        [*] --> write_overview
        [*] --> write_atomic_specs
        [*] --> write_coupled_specs
        [*] --> write_state_diags
        [*] --> write_flow_diags
        [*] --> write_sequences
        [*] --> write_hierarchy
        [*] --> write_tests
        write_overview: 01-model-overview.md
        write_atomic_specs: 02-atomic-model-specs.md
        write_coupled_specs: 03-coupled-model-specs.md
        write_state_diags: 04-state-diagrams.md
        write_flow_diags: 05-coupling-flow-diagrams.md
        write_sequences: 06-interaction-sequences.md
        write_hierarchy: 07-model-hierarchy.md
        write_tests: 08-test-scenarios.md
        write_overview --> write_readme
        write_atomic_specs --> write_readme
        write_coupled_specs --> write_readme
        write_state_diags --> write_readme
        write_flow_diags --> write_readme
        write_sequences --> write_readme
        write_hierarchy --> write_readme
        write_tests --> write_readme
        write_readme: README.md (index)
    }

    Phase4_DEPLOY --> Phase5_SUMMARIZE

    state Phase5_SUMMARIZE {
        [*] --> present_summary
        present_summary: Present executive summary
        present_summary --> store_patterns
        store_patterns: AgentDB pattern-store
        store_patterns --> train_reasoning
        train_reasoning: ReasoningBank learn
    }

    Phase5_SUMMARIZE --> [*]
```

## Model Context Object

All phases read from and write to a shared **Model Context** that maintains semantic consistency:

```
MODEL_CONTEXT = {
  // ── Mode Selection ──
  mode: "CREATE" | "ANALYZE" | "BOTH",
  source: {
    type: "description" | "files" | "both",
    description: "",               // User-provided text (CREATE mode)
    project_path: "",              // Path to project with model files (ANALYZE mode)
    documentation_paths: [],       // Paths to ingested docs (CREATE mode)
    source_files: []               // Discovered source files (ANALYZE mode)
  },

  // ── From Phase 0 (Reference Scan) ──
  catalog: {
    templates: [                   // Output templates
      { name: "", path: "", placeholders: [] }
    ],
    prior_patterns: []             // From AgentDB search
  },

  // ── From Phase 1 (Discovery) ──
  system: {
    name: "",                      // System/model name
    description: "",               // Purpose statement
    goal: "",                      // Expected behavior / modeling objective
    formalism: "",                 // "Basic DEVS", "Parallel DEVS", etc.
    type: "atomic" | "coupled" | "hierarchical"
  },

  atomic_models: [                 // One entry per atomic model
    {
      id: "AM1",
      name: "",                    // Model class name
      source_file: "",             // Source path (ANALYZE mode only)
      formal_spec: {
        X: [                       // Input set
          { port_name: "", event_type: "", description: "" }
        ],
        Y: [                       // Output set
          { port_name: "", event_type: "", description: "" }
        ],
        S: [                       // State set
          { name: "", description: "", sigma: "", is_passive: false }
        ],
        delta_int: [               // Internal transition function
          { from_state: "", to_state: "", description: "" }
        ],
        delta_ext: [               // External transition function
          { current_state: "", input_port: "", event: "", to_state: "", description: "" }
        ],
        lambda: [                  // Output function
          { state: "", output_port: "", value: "", description: "" }
        ],
        ta: [                      // Time advance function
          { state: "", duration: "", description: "" }
        ]
      },
      confluent_transition: "",    // Confluent transition strategy
      state_variables: [           // Additional state variables beyond phase/sigma
        { name: "", type: "", initial: "", description: "" }
      ]
    }
  ],

  coupled_models: [                // One entry per coupled model
    {
      id: "CM1",
      name: "",
      source_file: "",
      formal_spec: {
        X: [                       // External input set
          { port_name: "", event_type: "", description: "" }
        ],
        Y: [                       // External output set
          { port_name: "", event_type: "", description: "" }
        ],
        D: [],                     // Component set (list of model IDs)
        M_d: {},                   // Component model references
        I_d: {},                   // Influencees for each component
        Z_ij: {                    // Coupling specifications
          EIC: [                   // External Input Couplings
            { source_port: "", dest_component: "", dest_port: "" }
          ],
          IC: [                    // Internal Couplings
            { source_component: "", source_port: "", dest_component: "", dest_port: "" }
          ],
          EOC: [                   // External Output Couplings
            { source_component: "", source_port: "", dest_port: "" }
          ]
        },
        Select: ""                 // Tie-breaking function description
      }
    }
  ],

  hierarchy: {                     // Model composition hierarchy
    root: "",                      // Root model ID
    depth: 0,                      // Hierarchy depth
    tree: {}                       // Nested tree { id, name, type, children: [] }
  },

  // ── From Phase 2 (Formalization) ──
  formalization: {
    atomic_specs_valid: [],        // Validated atomic spec IDs
    coupled_specs_valid: [],       // Validated coupled spec IDs
    issues: [],                    // Detected issues
    test_scenarios: [
      {
        id: "TS1",
        name: "",
        target_model: "",          // Model ID being tested
        initial_state: "",
        input_sequence: [
          { time: 0, port: "", event: "" }
        ],
        expected_outputs: [
          { time: 0, port: "", value: "" }
        ],
        expected_final_state: "",
        description: ""
      }
    ]
  },

  // ── From Phase 3 (Diagram Generation) ──
  diagrams: {
    state_diagrams: [              // One per atomic model
      { model_id: "", mermaid_code: "" }
    ],
    flow_diagrams: [               // One per coupled model
      { model_id: "", mermaid_code: "" }
    ],
    sequence_diagrams: [           // Interaction patterns
      { model_id: "", mermaid_code: "" }
    ],
    class_diagram: "",             // Single overall class/hierarchy diagram
    hierarchy_diagram: ""          // Model composition tree
  },

  // ── From Phase 4 (Deployment) ──
  deployment: {
    deployed: false,
    output_dir: "",                // docs/devs-outline/[system-name]/
    files: [],                     // List of written file paths
    timestamp: ""
  },

  // ── From Phase 5 (Summary) ──
  summary: {
    executive_summary: "",
    metrics: {
      atomic_model_count: 0,
      coupled_model_count: 0,
      total_states: 0,
      total_ports: 0,
      total_couplings: 0,
      total_transitions: 0,
      test_scenario_count: 0
    }
  }
}
```

## Prerequisites

- A system to document — either:
  - A description of the system to model (text, requirements, design docs) for **CREATE mode**
  - A project directory containing DEVS model source files (`.java` MS4Systems or `.pdm` PowerDEVS) for **ANALYZE mode**
- No compilation or runtime dependencies — this skill produces documentation only

## Reference Library

This skill includes output templates with Handlebars-style placeholders and embedded Mermaid diagram blocks.

### Resource Paths (Relative to Skill Directory)

```
devs-model-outline/
└── resources/
    └── templates/              # 9 Markdown output templates
        ├── model-overview.md          # System identification & inventory
        ├── atomic-model-spec.md       # Formal M = <X,Y,S,δ_int,δ_ext,λ,ta>
        ├── coupled-model-spec.md      # Formal N = <X,Y,D,{M_d},{I_d},{Z_ij},Select>
        ├── state-diagrams.md          # Mermaid stateDiagram-v2
        ├── coupling-flow-diagrams.md  # Mermaid flowchart TD
        ├── interaction-sequences.md   # Mermaid sequenceDiagram
        ├── model-hierarchy.md         # Mermaid classDiagram + graph BT
        ├── test-scenarios.md          # Test scenario specifications
        └── index-readme.md            # Index/README linking all outputs
```

---

## Phase 0: Reference Library Scan (Automatic)

**Purpose:** Catalog output templates and search for prior DEVS outline patterns.

**Executes:** Automatically on skill invocation, BEFORE user interaction.

### Concurrent Execution

```mermaid
sequenceDiagram
    participant S as Skill Engine
    participant FS as File System
    participant ADB as AgentDB
    participant RB as ReasoningBank

    par Phase 0 Concurrent Scan
        S->>FS: Glob templates/*.md
        Note over FS: Step 0.1
        S->>ADB: pattern-search("DEVS model outline")
        Note over ADB: Step 0.2
        S->>RB: trajectory-start(task="DEVS model outline")
        Note over RB: Step 0.3
    end

    FS-->>S: template_files[]
    ADB-->>S: prior_patterns[]
    RB-->>S: trajectory_id

    S->>S: Step 0.4: Build catalog
    Note over S: MODEL_CONTEXT.catalog = {...}
```

**Steps 0.1, 0.2, 0.3 (CONCURRENT):**

| Step | Tool | Action |
|------|------|--------|
| 0.1 | `Glob` | `resources/templates/*.md` → `template_files[]` |
| 0.2 | `mcp__claude-flow__agentdb_pattern-search` | Search for `"DEVS model outline"` patterns → `prior_patterns[]` |
| 0.3 | `mcp__claude-flow__hooks_intelligence_trajectory-start` | Begin trajectory: `task="DEVS model outline generation"` |

**Step 0.4 (Sequential, depends on 0.1-0.3):**

For each template file, extract:
- Template name (from filename)
- Handlebars placeholder names (scan for `{{...}}` patterns)
- Mermaid diagram types present

Build catalog:

```
MODEL_CONTEXT.catalog = {
  templates: [
    { name: "model-overview", path: "resources/templates/model-overview.md",
      placeholders: ["system_name", "system_description", ...] },
    { name: "atomic-model-spec", path: "resources/templates/atomic-model-spec.md",
      placeholders: ["model_name", "port_name", "event_type", ...] },
    ...
  ],
  prior_patterns: [...]  // From AgentDB
}
```

### Phase 0 → Phase 1 Handoff

```
HANDOFF_0_TO_1 = {
  catalog: MODEL_CONTEXT.catalog,
  trajectory_id: <from ReasoningBank>
}
```

---

## Phase 1: System Discovery (Mode-Dependent)

**Depends on:** `HANDOFF_0_TO_1`

### Mode Determination

Determine the operating mode from the user's input:

| Condition | Mode |
|-----------|------|
| User provides system description or requirements text | `CREATE` |
| User provides a project path containing `.java` or `.pdm` files | `ANALYZE` |
| User provides both description AND project path | `BOTH` |
| Neither provided | Ask the user via `AskUserQuestion` |

Set `MODEL_CONTEXT.mode` accordingly.

---

### Phase 1 — CREATE Mode

**Purpose:** Infer DEVS model structure from the user's system description.

**Step 1.1: Collect System Description**

If the user's initial input contains a system description, use it directly. Otherwise, ask:
- What system or process should be modeled?
- What is the modeling goal or expected behavior?

**Steps 1.2a-1.2f (CONCURRENT):**

Autonomously infer the DEVS structure from the description. Execute these analysis steps in parallel:

| Step | Analysis | Output |
|------|----------|--------|
| 1.2a | Determine model type (atomic vs coupled vs hierarchical) | `system.type` |
| 1.2b | Identify states and state variables — discrete phases the system passes through | `atomic_models[].formal_spec.S` |
| 1.2c | Map input and output ports — what events enter and leave the system | `atomic_models[].formal_spec.X`, `Y` |
| 1.2d | Determine transitions (δ_int, δ_ext), output function (λ), and time advances (ta) | `delta_int`, `delta_ext`, `lambda`, `ta` |
| 1.2e | Identify coupled model structure if applicable — components and couplings (EIC, IC, EOC) | `coupled_models[]` |
| 1.2f | Search AgentDB for matching domain patterns | `mcp__claude-flow__agentdb_pattern-search` |

**Inference Guidelines:**

For each atomic model, self-answer these questions:
1. What are the distinct behavioral phases? → States (S)
2. What external events can arrive? → Input ports (X)
3. What events does this model produce? → Output ports (Y)
4. When no external event occurs, what happens after the time advance expires? → Internal transition (δ_int)
5. When an external event arrives, how does the state change? → External transition (δ_ext)
6. What output is produced before an internal transition? → Output function (λ)
7. How long does the model stay in each state? → Time advance (ta)
8. Which states are passive (waiting indefinitely for input)? → ta = INFINITY

For coupled models, additionally determine:
9. What atomic components make up this system?
10. How do external inputs reach internal components? → EIC
11. How do components communicate with each other? → IC
12. How do internal outputs reach the system boundary? → EOC
13. When multiple components have simultaneous events, what is the priority? → Select

**Step 1.3: Present Analysis for Confirmation**

Present the inferred model structure to the user in a readable format:

```
INFERRED DEVS MODEL: [SystemName]
──────────────────────────────────
Type:        [Atomic / Coupled / Hierarchical]
Formalism:   Basic DEVS (Zeigler, 2000)

ATOMIC MODELS:
  [ModelName]:
    States:      [state1], [state2], ...
    Input Ports: [port1] (Type), [port2] (Type), ...
    Output Ports:[port1] (Type), ...
    Transitions: [count] internal, [count] external
    
COUPLED MODELS (if applicable):
  [SystemName]:
    Components:  [comp1], [comp2], ...
    Couplings:   [count] EIC, [count] IC, [count] EOC

Please confirm or provide corrections.
```

**Step 1.4: Apply User Corrections**

If the user provides corrections, update the MODEL_CONTEXT accordingly. Log corrections via `mcp__claude-flow__agentdb_feedback`.

---

### Phase 1 — ANALYZE Mode

**Purpose:** Parse existing DEVS model source files to extract the model structure.

**Step 1.1: Scan Project Files**

If not already done in Phase 0, scan the project directory:

```
// CONCURRENT:
Glob: [project_path]/**/*.java    → java_files[]
Glob: [project_path]/**/*.pdm     → pdm_files[]
```

**Step 1.2: Parse Java Files (CONCURRENT per file)**

For each `.java` file, apply these extraction patterns:

| Element | Pattern to Search | Extraction |
|---------|-------------------|------------|
| **Model name** | `class (\w+) extends` | Class name |
| **Model type** | `extends PhaseAtomic` / `extends CoupledModelImpl` | Atomic vs Coupled |
| **Input ports** | `addInputPort\("([^"]+)",\s*(\w+)\.class\)` | Port name, event type |
| **Output ports** | `addOutputPort\("([^"]+)",\s*(\w+)\.class\)` | Port name, event type |
| **States** | `holdIn\("([^"]+)",\s*(.+?)\)` | Phase name, sigma value |
| **States** | `passivateIn\("([^"]+)"\)` | Phase name (sigma = INFINITY) |
| **States** | `passivate\(\)` | PASSIVE state (sigma = INFINITY) |
| **State checks** | `phaseIs\("([^"]+)"\)` | Referenced phase names |
| **δ_int** | Method body of `internalTransition()` | Transition logic |
| **δ_ext** | Method body of `externalTransition(double, MessageBag)` | Transition logic |
| **λ** | Method body of `getOutput()` | Output mappings |
| **ta** | Method body of `getTimeAdvance()` | Time advance logic |
| **Coupled components** | `addChildModel\((.+?)\)` | Child model instances |
| **Couplings** | `addCoupling\((.+?),\s*"([^"]+)",\s*(.+?),\s*"([^"]+)"\)` | Source, srcPort, dest, destPort |

**Step 1.3: Parse PowerDEVS .pdm Files**

For each `.pdm` file:

| Element | Pattern | Extraction |
|---------|---------|------------|
| **Root model** | `Coupled\s*\{[^}]*Name\s*=\s*([^;]+)` | Coupled model name |
| **Atomic blocks** | `Atomic\s*\{[^}]*Name\s*=\s*([^;]+)` | Component name |
| **Ports** | `Ports\s*=\s*(\d+)\s*;\s*(\d+)` | Input count, output count |
| **Connections** | `Line\s*\{[^}]*\}` | Connection topology |
| **Parameters** | `Parameters\s*=\s*"([^"]*)"` | Model parameters |

**Step 1.4: Merge Parsed Data**

Combine all parsed elements into `MODEL_CONTEXT.atomic_models[]` and `MODEL_CONTEXT.coupled_models[]`.

**Step 1.5: Present Discovered Structure**

Display the discovered model structure to the user for confirmation (same format as CREATE mode Step 1.3).

**Step 1.6: Apply User Corrections**

Same as CREATE mode Step 1.4.

---

### Phase 1 — BOTH Mode

Combines ANALYZE (parse files first) then enriches with CREATE (augment from description). The parsed source files take precedence; the description fills gaps or provides semantic context not derivable from code.

---

### Phase 1 → Phase 2 Handoff

```
HANDOFF_1_TO_2 = {
  ...HANDOFF_0_TO_1,
  mode: MODEL_CONTEXT.mode,
  system: MODEL_CONTEXT.system,
  atomic_models: MODEL_CONTEXT.atomic_models,
  coupled_models: MODEL_CONTEXT.coupled_models,
  user_confirmed: true
}
```

Log: `mcp__claude-flow__hooks_intelligence_trajectory-step(action="Phase 1: Discovery complete", data=HANDOFF_1_TO_2)`

---

## Phase 2: Formalize and Validate

**Depends on:** `HANDOFF_1_TO_2`

**Purpose:** Validate the discovered/inferred DEVS specifications for completeness and consistency. Generate test scenarios. Build the model hierarchy tree.

### Concurrent Execution

```mermaid
sequenceDiagram
    participant V as Validator
    participant TS as Test Generator
    participant HB as Hierarchy Builder

    par Phase 2 Concurrent Formalization
        V->>V: Step 2.1: Validate atomic specs
        Note over V: Check completeness:<br/>All states reachable?<br/>All ports used?<br/>ta defined for all states?
        V->>V: Step 2.2: Validate coupled specs
        Note over V: Check consistency:<br/>Port types match across couplings?<br/>All components connected?<br/>No dangling ports?
        TS->>TS: Step 2.3: Generate test scenarios
        Note over TS: Create input sequences<br/>that exercise all transitions<br/>and verify expected outputs
        HB->>HB: Step 2.4: Build hierarchy tree
        Note over HB: Construct nested tree<br/>from coupled/atomic relationships
    end
```

**Steps 2.1, 2.2, 2.3, 2.4 (CONCURRENT):**

| Step | Input | Validation/Action | Output |
|------|-------|--------------------|--------|
| 2.1 | `atomic_models[]` | For each atomic: all states reachable via transitions? All ports referenced in δ_ext/λ? ta defined for every state? At least one passive state? | `formalization.atomic_specs_valid[]`, `issues[]` |
| 2.2 | `coupled_models[]`, `atomic_models[]` | For each coupled: all component ports connected? EIC source ports exist on coupled model? IC port types compatible? EOC dest ports exist on coupled model? No orphaned components? | `formalization.coupled_specs_valid[]`, `issues[]` |
| 2.3 | `atomic_models[]`, `coupled_models[]` | Generate at least 2 test scenarios per model: (1) nominal path exercising primary transitions, (2) edge case with passive state or timeout | `formalization.test_scenarios[]` |
| 2.4 | `coupled_models[]`, `atomic_models[]` | Build `hierarchy.tree` from parent-child relationships. Compute `hierarchy.depth`. Identify `hierarchy.root`. | `hierarchy` |

**Step 2.5 (Sequential, depends on 2.1-2.4): Compile Validation Summary**

Present any issues found. If critical issues exist (e.g., unreachable states, dangling ports), ask the user to resolve before proceeding.

### Phase 2 → Phase 3 Handoff

```
HANDOFF_2_TO_3 = {
  ...HANDOFF_1_TO_2,
  formalization: MODEL_CONTEXT.formalization,
  hierarchy: MODEL_CONTEXT.hierarchy
}
```

Log: `mcp__claude-flow__hooks_intelligence_trajectory-step(action="Phase 2: Formalization complete")`

---

## Phase 3: Mermaid Diagram Generation

**Depends on:** `HANDOFF_2_TO_3`

**Purpose:** Generate all Mermaid UML diagrams from the formalized model specifications.

### Concurrent Execution

Execute Steps 3.1 through 3.5 **in parallel**:

| Step | Diagram Type | Scope | Output Field |
|------|-------------|-------|--------------|
| 3.1 | `stateDiagram-v2` | One per atomic model | `diagrams.state_diagrams[]` |
| 3.2 | `flowchart TD` | One per coupled model | `diagrams.flow_diagrams[]` |
| 3.3 | `sequenceDiagram` | One per coupled model | `diagrams.sequence_diagrams[]` |
| 3.4 | `classDiagram` | One overall | `diagrams.class_diagram` |
| 3.5 | `graph BT` | One overall | `diagrams.hierarchy_diagram` |

---

### Step 3.1: State Diagrams (`stateDiagram-v2`)

Generate one state diagram per atomic model. For each atomic model in `MODEL_CONTEXT.atomic_models[]`:

```mermaid
stateDiagram-v2
    [*] --> STATE1

    STATE1 : STATE1 (ta = 5.0)
    STATE2 : STATE2 (ta = 10.0)
    PASSIVE : PASSIVE (ta = INFINITY, passive)

    STATE1 --> STATE2 : δ_int
    STATE2 --> PASSIVE : δ_int
    PASSIVE --> STATE1 : port_in (δ_ext)

    note right of STATE1 : λ port_out value
    note right of PASSIVE : Waits for port_in
```

**Construction rules:**

1. **Initial state**: `[*] --> {first state from S where is_passive=false, or first state listed}`
2. **State nodes**: For each state in `formal_spec.S`:
   - Label: `{name} : {name} (ta = {sigma})` — append `, passive` for passive states
   - **NEVER** use `\n` in state labels — it renders as literal text. Use the `: description` syntax instead.
3. **Internal transitions**: For each entry in `formal_spec.delta_int`:
   - Arrow: `{from_state} --> {to_state} : δ_int` or `{from_state} --> {to_state} : δ_int, {brief description}`
4. **External transitions**: For each entry in `formal_spec.delta_ext`:
   - Arrow: `{current_state} --> {to_state} : {input_port} (δ_ext)`
5. **Output function notes**: For each entry in `formal_spec.lambda`:
   - Use **inline** notes only: `note right of {state} : λ {output_port} {value}`
   - **NEVER** use multi-line `note ... end note` blocks — they fail in many renderers.
6. **Passive state indicators**: States with `is_passive=true` should NOT have outgoing δ_int arrows (they wait for external input)

**Label safety rules (apply to ALL diagram types):**

- **No `\n`**: Never use `\n` in any label or description — it renders as literal backslash-n, not a newline
- **No `[brackets]`**: Avoid `[text]` in transition labels — use `(parentheses)` or plain text instead
- **No multiple colons in transitions**: Use at most one `:` per transition label (the separator between arrow and label)
- **Inline notes only for stateDiagram**: Use `note right of STATE : text` not multi-line blocks
- **Use `<br/>` sparingly**: Only in `flowchart` node labels where it is reliably supported

---

### Step 3.2: Coupling Flow Diagrams (`flowchart TD`)

Generate one flow diagram per coupled model. For each coupled model in `MODEL_CONTEXT.coupled_models[]`:

```mermaid
flowchart TD
    subgraph CM["CoupledModelName"]
        direction LR

        EXT_IN_port1(["⬇ ext_input"])
        EXT_OUT_port2(["⬆ ext_output"])

        COMP_A["ComponentA\n‹Atomic›\nStates: 3"]
        COMP_B["ComponentB\n‹Atomic›\nStates: 2"]

        EXT_IN_port1 -.->|"EIC"| COMP_A
        COMP_A -->|"out → in"| COMP_B
        COMP_B -.->|"EOC"| EXT_OUT_port2
    end
```

**Construction rules:**

1. **Subgraph**: Wrap in `subgraph CM["{coupled_model_name}"]` with `direction LR`
2. **External input ports**: For each port in `formal_spec.X`:
   - Node: `EXT_IN_{port_name}(["⬇ {port_name}"])`
3. **External output ports**: For each port in `formal_spec.Y`:
   - Node: `EXT_OUT_{port_name}(["⬆ {port_name}"])`
4. **Components**: For each component ID in `formal_spec.D`:
   - Look up the atomic model by ID to get state count
   - Node: `{component_id}["{component_name}\n‹Atomic›\nStates: {state_count}"]`
5. **EIC arrows** (dashed): For each EIC entry:
   - `EXT_IN_{source_port} -.->|"EIC"| {dest_component}`
6. **IC arrows** (solid): For each IC entry:
   - `{source_component} -->|"{source_port} → {dest_port}"| {dest_component}`
7. **EOC arrows** (dashed): For each EOC entry:
   - `{source_component} -.->|"EOC"| EXT_OUT_{dest_port}`

---

### Step 3.3: Interaction Sequence Diagrams (`sequenceDiagram`)

Generate one sequence diagram per coupled model showing a representative message passing scenario. For each coupled model in `MODEL_CONTEXT.coupled_models[]`:

```mermaid
sequenceDiagram
    participant ENV as Environment
    participant A as ComponentA
    participant B as ComponentB

    Note over ENV,B: Scenario: Normal processing flow

    ENV->>A: event via ext_input
    Note over A: δ_ext: PASSIVE → BUSY\nta = 5.0
    activate A

    A->>A: Processing (ta = 5.0)
    Note over A: λ: out → result
    A-->>B: result via out→in
    deactivate A
    Note over A: δ_int: BUSY → PASSIVE

    activate B
    Note over B: δ_ext: IDLE → ACTIVE\nta = 3.0
    B->>B: Processing (ta = 3.0)
    Note over B: λ: out → output
    B-->>ENV: output via ext_output
    deactivate B
    Note over B: δ_int: ACTIVE → IDLE
```

**Construction rules:**

1. **Participants**: Environment first, then each component in coupling order
2. **Scenario**: Use the first test scenario from `formalization.test_scenarios[]` that targets this coupled model, or construct a nominal path scenario
3. **External input**: `ENV->>{first_component}: {event} via {port_name}`
4. **δ_ext notes**: `Note over {component}: δ_ext: {from} → {to}\nta = {sigma}`
5. **Activation bars**: `activate {component}` when entering a non-passive state, `deactivate` after δ_int returns to passive
6. **λ outputs**: Dashed arrows `{source}-->>{dest}: {value} via {port}→{port}`
7. **δ_int notes**: `Note over {component}: δ_int: {from} → {to}`
8. **Concurrent events**: Use `par` blocks when multiple components have simultaneous transitions

---

### Step 3.4: Class Diagram (`classDiagram`)

Generate one overall class diagram showing model structure:

```mermaid
classDiagram
    class DEVSModel {
        <<abstract>>
        +initialize()
        +getTimeAdvance() double
        +internalTransition()
        +externalTransition(double, MessageBag)
        +getOutput() MessageBag
    }

    class ComponentA {
        +Port~Entity~ in
        +Port~Entity~ out
        -String phase
        -double sigma
        -int counter
        +initialize()
        +internalTransition()
        +externalTransition(double, MessageBag)
        +getOutput() MessageBag
        +getTimeAdvance() double
    }

    class CoupledSystem {
        +ComponentA compA
        +ComponentB compB
        +initialize()
    }

    DEVSModel <|-- ComponentA
    DEVSModel <|-- ComponentB
    DEVSModel <|-- CoupledSystem
    CoupledSystem *-- ComponentA : contains
    CoupledSystem *-- ComponentB : contains
```

**Construction rules:**

1. **Abstract base**: Always include `DEVSModel` with the 5 core DEVS methods
2. **Atomic models**: For each `atomic_models[]`:
   - List ports with generic types: `+Port~{event_type}~ {port_name}`
   - List state variables: `-{type} {name}`
   - Include the 5 DEVS methods
3. **Coupled models**: For each `coupled_models[]`:
   - List component references: `+{ComponentType} {componentName}`
   - Include `+initialize()`
4. **Inheritance**: `DEVSModel <|-- {ModelName}` for all models
5. **Composition**: `{CoupledModel} *-- {AtomicModel} : contains` for each component relationship

---

### Step 3.5: Hierarchy Diagram (`graph BT`)

Generate one bottom-up hierarchy diagram:

```mermaid
graph BT
    AM1["ComponentA\n‹Atomic›"]
    AM2["ComponentB\n‹Atomic›"]
    CM1{{"CoupledSystem\n‹Coupled›"}}

    AM1 --> CM1
    AM2 --> CM1

    style CM1 fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
```

**Construction rules:**

1. **Atomic models**: Rectangle nodes `{id}["{name}\n‹Atomic›"]`
2. **Coupled models**: Hexagon nodes `{id}{{"{name}\n‹Coupled›"}}`
3. **Edges**: `{child_id} --> {parent_id}` from component to containing coupled model
4. **Root styling**: `style {root_id} fill:#e1f5fe,stroke:#0288d1,stroke-width:2px`
5. **Layout**: Bottom-to-top (`graph BT`) so atomics are at the bottom, root coupled at top

---

### Step 3.6 (Sequential): Validate Mermaid Compilation

**Compile-test every diagram** using `mermaid-cli` (`mmdc`) to catch rendering failures before deployment. This is mandatory — do not skip.

**Validation procedure:**

1. Extract every `` ```mermaid `` block from the generated content into a temporary `.mmd` file
2. For each `.mmd` file, run:
   ```bash
   npx --yes @mermaid-js/mermaid-cli -i {file}.mmd -o {file}.svg --quiet
   ```
3. Check the result:
   - **SVG file exists** → compilation succeeded
   - **SVG file missing** → parse/render error — fix and re-validate
4. For each generated SVG, verify:
   - No `class="error"` markers: `grep -c 'class="error"' {file}.svg` must be 0
   - No literal `\n` text: `grep -c '\\n' {file}.svg` must be 0 (indicates broken labels)
   - File size > 1000 bytes (very small SVGs may be error placeholders)

**If any diagram fails:**
- Read the mmdc error output to identify the exact syntax issue
- Apply the label safety rules from Step 3.1
- Re-generate and re-validate the failing diagram
- Repeat until all diagrams pass

**Common fixes:**
| Error Pattern | Cause | Fix |
|---------------|-------|-----|
| Literal `\n` in SVG text | `\n` in label | Remove `\n`, use inline descriptions |
| Parse error on `[text]` | Brackets in transition labels | Replace with parentheses or plain text |
| Duplicate state ID | Same alias used in two states | Use unique aliases |
| Missing `end` keyword | Unclosed `note` or `par` block | Use inline notes instead |

**Acceptance criteria:** ALL extracted mermaid blocks must produce valid SVGs with zero errors before proceeding to Phase 4.

### Phase 3 → Phase 4 Handoff

```
HANDOFF_3_TO_4 = {
  ...HANDOFF_2_TO_3,
  diagrams: MODEL_CONTEXT.diagrams
}
```

Log: `mcp__claude-flow__hooks_intelligence_trajectory-step(action="Phase 3: Diagrams generated")`

---

## Phase 4: Deploy Output Files

**Depends on:** `HANDOFF_3_TO_4`

**Purpose:** Write all Markdown output files using templates, populating placeholders with MODEL_CONTEXT data.

### Output Directory

```
docs/devs-outline/{system_name}/
├── README.md
├── 01-model-overview.md
├── 02-atomic-model-specs.md
├── 03-coupled-model-specs.md
├── 04-state-diagrams.md
├── 05-coupling-flow-diagrams.md
├── 06-interaction-sequences.md
├── 07-model-hierarchy.md
└── 08-test-scenarios.md
```

Where `{system_name}` is derived from `MODEL_CONTEXT.system.name` converted to kebab-case.

### Concurrent Execution

**Steps 4.1-4.8 (CONCURRENT):** Write all 8 content files using the corresponding templates from `resources/templates/`. For each file:

1. Read the template from `resources/templates/`
2. Replace all `{{placeholder}}` tokens with values from MODEL_CONTEXT
3. Expand `{{#each collection}}...{{/each}}` blocks by iterating over the corresponding arrays
4. Write the populated file to the output directory

| Step | Template | Output File | Primary Data Source |
|------|----------|-------------|---------------------|
| 4.1 | `model-overview.md` | `01-model-overview.md` | `system`, `atomic_models[]`, `coupled_models[]`, `hierarchy` |
| 4.2 | `atomic-model-spec.md` | `02-atomic-model-specs.md` | `atomic_models[].formal_spec` |
| 4.3 | `coupled-model-spec.md` | `03-coupled-model-specs.md` | `coupled_models[].formal_spec` |
| 4.4 | `state-diagrams.md` | `04-state-diagrams.md` | `diagrams.state_diagrams[]` |
| 4.5 | `coupling-flow-diagrams.md` | `05-coupling-flow-diagrams.md` | `diagrams.flow_diagrams[]` |
| 4.6 | `interaction-sequences.md` | `06-interaction-sequences.md` | `diagrams.sequence_diagrams[]` |
| 4.7 | `model-hierarchy.md` | `07-model-hierarchy.md` | `diagrams.class_diagram`, `diagrams.hierarchy_diagram` |
| 4.8 | `test-scenarios.md` | `08-test-scenarios.md` | `formalization.test_scenarios[]` |

**Step 4.9 (Sequential, depends on 4.1-4.8):** Write README.md

Use the `index-readme.md` template. This file links to all 8 content files and includes a quick stats summary.

**Step 4.10 (Sequential):** Verify all files exist

```
// Verify all 9 files were written:
Glob: docs/devs-outline/{system_name}/*.md → verify count = 9
```

Update deployment status:

```
MODEL_CONTEXT.deployment = {
  deployed: true,
  output_dir: "docs/devs-outline/{system_name}/",
  files: [list of 9 file paths],
  timestamp: <current ISO timestamp>
}
```

### Phase 4 → Phase 5 Handoff

```
HANDOFF_4_TO_5 = {
  ...HANDOFF_3_TO_4,
  deployment: MODEL_CONTEXT.deployment
}
```

Log: `mcp__claude-flow__hooks_intelligence_trajectory-step(action="Phase 4: Files deployed")`

---

## Phase 5: Summarize and Learn

**Depends on:** `HANDOFF_4_TO_5`

**Purpose:** Present results, compute metrics, store learned patterns.

### Sequential Execution

**Step 5.1: Compute Metrics**

```
MODEL_CONTEXT.summary.metrics = {
  atomic_model_count: atomic_models.length,
  coupled_model_count: coupled_models.length,
  total_states: sum(atomic_models[].formal_spec.S.length),
  total_ports: sum(atomic_models[].formal_spec.X.length + Y.length)
               + sum(coupled_models[].formal_spec.X.length + Y.length),
  total_couplings: sum(coupled_models[].formal_spec.Z_ij.EIC.length
                       + IC.length + EOC.length),
  total_transitions: sum(atomic_models[].formal_spec.delta_int.length
                         + delta_ext.length),
  test_scenario_count: formalization.test_scenarios.length
}
```

**Step 5.2: Present Executive Summary**

```
DEVS MODEL OUTLINE COMPLETE
════════════════════════════

System:      {system.name}
Mode:        {mode}
Formalism:   {system.formalism}
Output:      {deployment.output_dir}

╔══════════════════════════════════════════╗
║  METRICS                                 ║
╠══════════════════════════════════════════╣
║  Atomic Models:    {atomic_model_count}  ║
║  Coupled Models:   {coupled_model_count} ║
║  Total States:     {total_states}        ║
║  Total Ports:      {total_ports}         ║
║  Total Couplings:  {total_couplings}     ║
║  Total Transitions:{total_transitions}   ║
║  Test Scenarios:   {test_scenario_count} ║
╚══════════════════════════════════════════╝

FILES GENERATED:
  ✓ README.md
  ✓ 01-model-overview.md
  ✓ 02-atomic-model-specs.md
  ✓ 03-coupled-model-specs.md
  ✓ 04-state-diagrams.md
  ✓ 05-coupling-flow-diagrams.md
  ✓ 06-interaction-sequences.md
  ✓ 07-model-hierarchy.md
  ✓ 08-test-scenarios.md
```

**Step 5.3: Store Patterns in AgentDB**

```
mcp__claude-flow__agentdb_pattern-store({
  pattern_type: "devs-model-outline",
  domain: MODEL_CONTEXT.system.description,
  model_type: MODEL_CONTEXT.system.type,
  atomic_count: metrics.atomic_model_count,
  coupled_count: metrics.coupled_model_count,
  state_count: metrics.total_states,
  mode: MODEL_CONTEXT.mode,
  success: true
})
```

**Step 5.4: Train ReasoningBank**

```
mcp__claude-flow__hooks_intelligence_learn({
  verdict: "success",
  reward: 0.95,
  context: "DEVS model outline generation",
  outcome: {
    mode: MODEL_CONTEXT.mode,
    models_documented: metrics.atomic_model_count + metrics.coupled_model_count,
    diagrams_generated: diagrams.state_diagrams.length
                        + diagrams.flow_diagrams.length
                        + diagrams.sequence_diagrams.length + 2,
    files_written: deployment.files.length
  }
})
```

If the user provided corrections during Phase 1:

```
mcp__claude-flow__agentdb_feedback({
  feedback_type: "correction",
  context: "DEVS model outline - Phase 1 discovery",
  original: <original inference>,
  corrected: <user correction>,
  lesson: <what to infer differently next time>
})
```

---

## Swarm Coordination (Complex Models)

For systems with more than 5 atomic models or 3 coupled models, use swarm coordination to parallelize work:

**Phase 1 ANALYZE — File Parsing Swarm:**

```
mcp__claude-flow__swarm_init({
  name: "devs-outline-parse",
  topology: "fan-out",
  task_count: source_files.length
})

// Spawn one worker per source file:
for each source_file in source_files:
  mcp__claude-flow__agent_spawn({
    type: "worker",
    task: "Parse DEVS model from " + source_file,
    data: { file_path: source_file, patterns: JAVA_PARSE_PATTERNS }
  })
```

**Phase 3 — Diagram Generation Swarm:**

```
mcp__claude-flow__swarm_init({
  name: "devs-outline-diagrams",
  topology: "fan-out",
  task_count: atomic_models.length + coupled_models.length * 2 + 2
})

// Spawn workers for each diagram type:
// - One per atomic model (state diagrams)
// - Two per coupled model (flow + sequence diagrams)
// - One for class diagram
// - One for hierarchy diagram
```

---

## Conditional Sections

### Atomic-Only Systems

If `MODEL_CONTEXT.system.type === "atomic"` (no coupled models):
- Skip `03-coupled-model-specs.md` — write a placeholder noting "No coupled models"
- Skip `05-coupling-flow-diagrams.md` — write a placeholder
- Skip `06-interaction-sequences.md` — write a placeholder
- The class diagram and hierarchy diagram show only the atomic model(s)

### Coupled-Only Documentation

If the user only wants to document a single coupled model's topology (no deep atomic specs):
- Still document all atomic components, but with abbreviated specs
- Focus depth on coupling diagrams and interaction sequences

### Hierarchical Models

If `hierarchy.depth > 2` (nested coupled models):
- Generate flow diagrams at each level of the hierarchy
- The hierarchy diagram uses multiple layers in the `graph BT`
- Sequence diagrams may need `box` blocks to group components by their parent coupled model
