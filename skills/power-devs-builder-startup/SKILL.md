---
name: "PowerDEVS Builder Startup"
description: "Create PowerDEVS models (.pdm files) for continuous/hybrid system simulation using QSS methods. Takes a brief description, autonomously infers the complete model structure including integrators, summers, multipliers, and signal routing, then generates a valid .pdm file. Use when modeling differential equations, control systems, population dynamics, or any continuous system with PowerDEVS. General-purpose version: prompts for user-specific directories at runtime."
---

# PowerDEVS Builder Startup

## What This Skill Does

Builds complete PowerDEVS models (.pdm files) through a structured, concurrent workflow with semantic continuity across all phases.

**This is the general-purpose distribution version.** Unlike the personalized `power-devs-builder`, this skill prompts the user for their specific directory paths at runtime rather than using hardcoded paths.

## Workflow Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        MODEL CONTEXT (Persists Across Phases)           │
│  ┌──────────────┬──────────────┬──────────────┬──────────────────────┐  │
│  │ model_name   │ equations    │ components   │ connections          │  │
│  │ description  │ state_vars   │ parameters   │ layout               │  │
│  │ deployment   │ summary      │ validation   │ catalog              │  │
│  │ user_config  │              │              │                      │  │
│  └──────────────┴──────────────┴──────────────┴──────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
     ┌──────────────────────────────┼──────────────────────────────┐
     │                              │                              │
     ▼                              ▼                              ▼
┌─────────┐  ─────────────►  ┌─────────┐  ─────────────►  ┌─────────┐
│ Phase 0 │   HANDOFF: ctx   │ Phase 1 │   HANDOFF: ctx   │ Phase 2 │
│  SCAN   │   + catalog      │ DISCOVER│   + analysis     │ GENERATE│
└─────────┘                  └─────────┘                  └─────────┘
     │                              │                              │
     │ CONCURRENT:                  │ CONCURRENT:                  │ CONCURRENT:
     │ • models scan                │ • equation parse             │ • atomics gen
     │ • atomics scan               │ • component map              │ • points gen
     │                              │ • routing calc               │ • lines gen
     │                              │                              │
     └──────────────────────────────┼──────────────────────────────┘
                                    │
                                    ▼
                              ┌─────────┐  ─────────────►  ┌─────────┐
                              │ Phase 3 │   HANDOFF: ctx   │ Phase 4 │
                              │ VALIDATE│   + artifacts    │ DEPLOY  │
                              └─────────┘                  └─────────┘
                                    │                            │
                                    │ CONCURRENT:                │
                                    │ • structure check          ▼
                                    │ • routing verify     ┌─────────┐
                                    │ • param validate     │ Phase 5 │
                                    │                      │SUMMARIZE│
                                    │                      └─────────┘
                                    │                            │
                                    └────────────────────────────┘
                                                 │
                                                 ▼
                                      docs/models/[name].md
```

## User Configuration (Runtime Prompts)

This skill requires user-specific directory paths for validation (Phase 3), deployment (Phase 4), and summary (Phase 5). These paths are collected at runtime using `AskUserQuestion` prompts.

### Required User Paths

| Variable | Description | Example |
|----------|-------------|---------|
| `POWERDEVS_WSL_PATH` | WSL path to PowerDEVS installation root | `~/powerdevs-CERN` |
| `WINDOWS_PROJECT_DIR` | Windows path to the project working directory | `C:\Users\john\Projects\MyModel` |
| `WSL_PROJECT_DIR` | WSL mount path of the project directory | `/mnt/c/Users/john/Projects/MyModel` |
| `DEPLOY_SUBDIR` | Subdirectory within PowerDEVS for deployed models | `examples/openaiGenModel` |

### When Paths Are Collected

Paths are prompted **once** at the start of Phase 3 (the first phase that requires them). The collected values are stored in `MODEL_CONTEXT.user_config` and reused in Phases 4 and 5.

### Path Derivation

If the user provides a Windows path like `C:\Users\john\Projects\MyModel`, the WSL mount path can be derived automatically:
- Replace `C:\` with `/mnt/c/`
- Replace all `\` with `/`
- Example: `C:\Users\john\Projects\MyModel` → `/mnt/c/Users/john/Projects/MyModel`

### User Config in MODEL_CONTEXT

```
MODEL_CONTEXT.user_config = {
  powerdevs_wsl_path: "",       // e.g., "~/powerdevs-CERN"
  windows_project_dir: "",      // e.g., "C:\Users\john\Projects\MyModel"
  wsl_project_dir: "",          // e.g., "/mnt/c/Users/john/Projects/MyModel"
  deploy_subdir: "",            // e.g., "examples/openaiGenModel"
  collected: false              // Set to true after prompts are answered
}
```

## Model Context Object

All phases read from and write to a shared **Model Context** that maintains semantic consistency:

```
MODEL_CONTEXT = {
  // From User Configuration (collected at Phase 3 start)
  user_config: {
    powerdevs_wsl_path: "",      // WSL path to PowerDEVS root
    windows_project_dir: "",     // Windows project directory
    wsl_project_dir: "",         // WSL mount of project directory
    deploy_subdir: "",           // Deploy subdirectory in PowerDEVS
    collected: false
  },

  // From Phase 0 (Reference Scan)
  catalog: {
    models: [...],           // Available reference models
    atomics: {...},          // Component definitions by category
    patterns: [...]          // Extracted connection patterns
  },

  // From Phase 1 (System Discovery)
  model: {
    name: "",                // Model identifier
    description: "",         // Purpose statement
    goal: ""                 // Expected behavior
  },
  equations: {
    differential: [...],     // dx/dt = f(x) forms
    algebraic: [...]         // Supporting equations
  },
  state_variables: [
    { name, initial, units, meaning }
  ],
  parameters: [
    { name, value, description }
  ],
  qss_config: {
    method: "",              // QSS/QSS2/QSS3/LIQSS...
    dqmin: "",
    dqrel: ""
  },

  // From Phase 2 (Generation) - Built using Phase 0 & 1 data
  components: [
    { type, name, path, ports, params, position }
  ],
  connections: [
    { source: {cmp, port}, sink: {cmp, port}, points: [...] }
  ],
  junctions: [
    { lines: [...], position }
  ],

  // From Phase 3 (Validation)
  validation: {
    passed: bool,
    checks: [...],
    warnings: [...]
  },

  // From Phase 4 (Deployment)
  deployment: {
    deployed: bool,
    source_path: "",           // Windows source path
    wsl_path: "",              // WSL target path
    timestamp: ""              // ISO timestamp
  },

  // From Phase 5 (Summary)
  summary: {
    generated: bool,
    path: "",                  // docs/models/[name].md
    timestamp: ""              // ISO timestamp
  }
}
```

---

## Prerequisites

- PowerDEVS simulator installed (optional, for running the generated model)
- A continuous or hybrid system to model (differential equations, control systems, population dynamics, etc.)

---

## Reference Library

This skill includes a comprehensive reference library with 44 models and 306 atomic block headers.

### Resource Paths (Relative to Skill Directory)

```
power-devs-builder-startup/
└── resources/
    ├── models/          # 44 reference .pdm files
    ├── atomics/         # 306 atomic block headers (.h)
    │   ├── qss/         # 48 files - QSS integration methods
    │   ├── continuous/  # 33 files - Continuous blocks
    │   ├── hybrid/      # 44 files - Hybrid blocks
    │   ├── sinks/       # 28 files - Output blocks
    │   ├── sources/     # 32 files - Signal generators
    │   └── ... (16 more categories)
    └── templates/       # Reusable model templates
```

---

## Phase 0: Reference Library Scan (Automatic)

**Purpose:** Build the component catalog that Phase 1 and Phase 2 will reference.

**Executes:** Automatically on skill invocation, BEFORE user interaction.

### Concurrent Execution

Execute Steps 0.1 and 0.2 **in parallel** using concurrent tool calls:

```
┌─────────────────────────────────────────────────────────┐
│                    PHASE 0: SCAN                        │
│                                                         │
│  ┌─────────────────┐       ┌─────────────────┐         │
│  │ Step 0.1        │       │ Step 0.2        │         │
│  │ Glob: models/   │  ││   │ Glob: atomics/  │         │
│  │ *.pdm           │  ││   │ **/*.h          │         │
│  └────────┬────────┘       └────────┬────────┘         │
│           │                         │                   │
│           └────────────┬────────────┘                   │
│                        ▼                                │
│              ┌─────────────────┐                        │
│              │ Step 0.3        │                        │
│              │ Parse & Index   │                        │
│              │ (sequential)    │                        │
│              └────────┬────────┘                        │
│                       ▼                                 │
│              ┌─────────────────┐                        │
│              │ Step 0.4        │                        │
│              │ Build Catalog   │                        │
│              └────────┬────────┘                        │
│                       ▼                                 │
│              MODEL_CONTEXT.catalog = {...}              │
└─────────────────────────────────────────────────────────┘
```

**Step 0.1 & 0.2 (CONCURRENT):**
```
// Execute these two Glob calls in a single message with parallel tool calls:
Glob: resources/models/*.pdm      → model_files[]
Glob: resources/atomics/**/*.h    → atomic_files[]
```

**Step 0.3 (Sequential, depends on 0.1 & 0.2):**
For each .pdm file, extract:
- All `Path = ` values (atomic block paths)
- Parameter structures and default values
- Layout conventions (Position coordinates)
- Connection patterns

**Step 0.4: Build Catalog → MODEL_CONTEXT.catalog**
```
MODEL_CONTEXT.catalog = {
  models: [
    { name: "lotka_volterra", path: "resources/models/lotka_volterra.pdm",
      components: ["qss/qss_integrator.h", "qss/qss_wsum.h", ...] }
  ],
  atomics: {
    "qss/qss_integrator.h": { ports: "1;1", params: ["Method", "dqmin", "dqrel", "x0"] },
    "qss/qss_wsum.h": { ports: "N;1", params: ["K[0-7]", "Inputs"] },
    "qss/qss_multiplier.h": { ports: "2;1", params: ["Advance method", "dQmin", "dQrel"] },
    "sinks/gnuplot.h": { ports: "N;0", params: ["Inputs", "Format", "Format1-5"] },
    "sink/toLogger.h": { ports: "1;0", params: [] }
  },
  patterns: [
    { type: "feedback_loop", components: ["integrator", "wsum"], example: "lotka_volterra" },
    { type: "nonlinear_product", components: ["multiplier", "wsum"], example: "lotka_volterra" }
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

## Phase 1: System Discovery (Autonomous Inference)

**Purpose:** Analyze user description and build the complete model specification.

**Depends on:** Phase 0 catalog (for component matching and pattern recognition)

### Concurrent Execution

After collecting user description, execute Steps 1.2a-1.2c **in parallel**:

```
┌─────────────────────────────────────────────────────────┐
│                  PHASE 1: DISCOVER                      │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Step 1.1: Collect user description              │   │
│  └────────────────────────┬────────────────────────┘   │
│                           ▼                             │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │
│  │ Step 1.2a    │ │ Step 1.2b    │ │ Step 1.2c    │ │
│  │ Parse        │ │ Map to       │ │ Calculate    │ │
│  │ equations    │ │ components   │ │ routing      │ │
│  │              │ │ (use catalog)│ │              │ │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ │
│         │                │                │          │
│         └────────────────┼────────────────┘          │
│                          ▼                            │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Step 1.3: Merge & present for confirmation      │ │
│  └────────────────────────┬────────────────────────┘ │
│                           ▼                           │
│            MODEL_CONTEXT.{model, equations,           │
│                          state_variables, parameters, │
│                          qss_config} = {...}          │
└─────────────────────────────────────────────────────────┘
```

**Step 1.1: Collect user description**
If not provided, ask using `AskUserQuestion`:
- "Briefly describe the continuous system you want to model."

**Steps 1.2a, 1.2b, 1.2c (CONCURRENT):**

| Step | Input | Process | Output |
|------|-------|---------|--------|
| 1.2a | User description | Extract differential equations | `equations[]` |
| 1.2b | User description + `catalog.atomics` | Map terms to components | `component_map{}` |
| 1.2c | Equations + component_map | Calculate signal routing | `routing[]` |

**Step 1.3: Merge results → MODEL_CONTEXT**
```
MODEL_CONTEXT.model = { name, description, goal }
MODEL_CONTEXT.equations = { differential: [...], algebraic: [...] }
MODEL_CONTEXT.state_variables = [...]
MODEL_CONTEXT.parameters = [...]
MODEL_CONTEXT.qss_config = { method, dqmin, dqrel }
```

### Self-Answered Questions (Using Phase 0 Catalog)

When mapping to components, reference `MODEL_CONTEXT.catalog.atomics`:

1. **Model name and description** → `MODEL_CONTEXT.model`
2. **Modeling goal** → `MODEL_CONTEXT.model.goal`
3. **System equations** → `MODEL_CONTEXT.equations`
4. **State variables** → `MODEL_CONTEXT.state_variables`
5. **Parameters** → `MODEL_CONTEXT.parameters`
6. **QSS Method** (match from `catalog.atomics["qss/qss_integrator.h"].params`) → `MODEL_CONTEXT.qss_config`
7. **Component inventory** (select from `catalog.atomics`) → preliminary `components[]`
8. **Signal routing** → preliminary `connections[]`
9. **Quantum parameters** → `MODEL_CONTEXT.qss_config.{dqmin, dqrel}`
10. **Output configuration** (select from `catalog.atomics` sinks) → sink components
11. **Simulation parameters** → `MODEL_CONTEXT.parameters`
12. **Test scenarios** → documentation only

### Present Analysis for Confirmation

```
Based on your description, here is the PowerDEVS model analysis:

MODEL: [MODEL_CONTEXT.model.name]
PURPOSE: [MODEL_CONTEXT.model.goal]

SYSTEM EQUATIONS (from MODEL_CONTEXT.equations):
  dx1/dt = [equation]
  dx2/dt = [equation]

STATE VARIABLES (from MODEL_CONTEXT.state_variables):
  x1: [name] (initial: [value], units: [units])

PARAMETERS (from MODEL_CONTEXT.parameters):
  [param]: [value] ([description])

COMPONENT INVENTORY (matched from MODEL_CONTEXT.catalog.atomics):
  - [N] QSS Integrators (qss/qss_integrator.h, method: [QSS3])
  - [N] WSums (qss/qss_wsum.h)
  - [N] Multipliers (qss/qss_multiplier.h)
  - [N] Sinks (sinks/gnuplot.h, sink/toLogger.h)

SIGNAL ROUTING:
  [component1].out -> [component2].in[N]

QUANTUM SETTINGS (from MODEL_CONTEXT.qss_config):
  dqmin: [value], dqrel: [value]
```

### Phase 1 → Phase 2 Handoff

```
HANDOFF_1_TO_2 = {
  // All of MODEL_CONTEXT from Phase 0 & 1
  catalog: MODEL_CONTEXT.catalog,
  model: MODEL_CONTEXT.model,
  equations: MODEL_CONTEXT.equations,
  state_variables: MODEL_CONTEXT.state_variables,
  parameters: MODEL_CONTEXT.parameters,
  qss_config: MODEL_CONTEXT.qss_config,

  // Pre-computed for Phase 2
  component_list: [...],    // Types and counts
  routing_matrix: [...],    // Connection pairs
  layout_hints: {...}       // Suggested positions based on catalog.patterns
}
```

---

## Phase 2: PDM File Generation

**Purpose:** Generate the complete .pdm file using all context from Phase 0 and Phase 1.

**Depends on:** Full MODEL_CONTEXT from Phases 0 and 1

### Concurrent Execution

Generate atomic blocks, junction points, and connection lines **in parallel**:

```
┌─────────────────────────────────────────────────────────┐
│                  PHASE 2: GENERATE                      │
│                                                         │
│  Input: HANDOFF_1_TO_2 (full MODEL_CONTEXT)            │
│                                                         │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │
│  │ Step 2.1     │ │ Step 2.2     │ │ Step 2.3     │ │
│  │ Generate     │ │ Generate     │ │ Generate     │ │
│  │ Atomics      │ │ Points       │ │ Lines        │ │
│  │ (use catalog)│ │ (use routing)│ │ (use routing)│ │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ │
│         │                │                │          │
│         └────────────────┼────────────────┘          │
│                          ▼                            │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Step 2.4: Assemble PDM file                     │ │
│  │ (sequential - must follow structure order)      │ │
│  └────────────────────────┬────────────────────────┘ │
│                           ▼                           │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Step 2.5: Write to src/[model_name].pdm         │ │
│  └─────────────────────────────────────────────────┘ │
│                           │                           │
│            MODEL_CONTEXT.{components,                 │
│                          connections,                 │
│                          junctions} = {...}           │
└─────────────────────────────────────────────────────────┘
```

**Steps 2.1, 2.2, 2.3 (CONCURRENT):**

| Step | Input | Process | Output |
|------|-------|---------|--------|
| 2.1 | `component_list` + `catalog.atomics` | Generate Atomic blocks | `atomic_blocks[]` |
| 2.2 | `routing_matrix` + `layout_hints` | Generate Point junctions | `points[]` |
| 2.3 | `routing_matrix` + `component_indices` | Generate Line connections | `lines[]` |

**Step 2.1: Generate Atomics (Reference catalog for exact syntax)**
For each component in `HANDOFF_1_TO_2.component_list`:
```
// Look up exact format from MODEL_CONTEXT.catalog.atomics[path]
Atomic
    {
    Name = [from MODEL_CONTEXT.state_variables or generated]
    Ports = [from catalog.atomics[path].ports]
    Path = [path from catalog]
    Description = [from catalog or generated]
    Graphic
        {
        Position = [from layout_hints or calculated]
        Dimension = 675 ; 675
        Direction = Right
        Color = 15
        Icon = [from catalog pattern]
        }
    Parameters
        {
        [for each param in catalog.atomics[path].params]
        [param] = [value from MODEL_CONTEXT.parameters or qss_config]
        }
    }
```

**Step 2.4: Assemble PDM (Sequential - structure order matters)**
```
Coupled
    {
    Type = Root
    Name = [MODEL_CONTEXT.model.name]
    Ports = 0; 0
    Description = [MODEL_CONTEXT.model.description]
    Graphic { ... }
    Parameters { }
    System
        {
        [atomic_blocks from Step 2.1 - in declaration order]
        [points from Step 2.2 - after all atomics]
        [lines from Step 2.3 - after all points]
        }
    }
```

**Step 2.5: Write file**
Save to `src/[MODEL_CONTEXT.model.name].pdm`

### Phase 2 → Phase 3 Handoff

```
HANDOFF_2_TO_3 = {
  // Full MODEL_CONTEXT
  ...MODEL_CONTEXT,

  // Generated artifacts
  pdm_file_path: "src/[name].pdm",
  components: MODEL_CONTEXT.components,      // With final indices
  connections: MODEL_CONTEXT.connections,    // With final routing
  junctions: MODEL_CONTEXT.junctions,

  // For validation
  component_count: N,
  connection_count: M,
  expected_ports: {...}
}
```

---

## Phase 3: Validation and Auto-Correction

**Purpose:** Verify the generated model against PowerDEVS requirements, validate all atomic paths and parameters, ensure GnuPlot captures all outputs, and **automatically fix any errors found**.

**Depends on:** Full MODEL_CONTEXT from all previous phases

**Critical:** This phase performs runtime validation against the actual PowerDEVS installation and auto-corrects the PDM file if errors are detected.

### Step 3.0: Collect User Configuration (REQUIRED - First Time Only)

**Before any validation can proceed, collect the user's directory paths.**

If `MODEL_CONTEXT.user_config.collected == false`, use `AskUserQuestion` to prompt:

**Prompt 1: PowerDEVS Installation Path**
```
Question: "What is the WSL path to your PowerDEVS installation directory?"
Header: "PowerDEVS"
Options:
  - "~/powerdevs-CERN" (Default CERN installation)
  - "~/PowerDEVS" (Standard installation)
  - Other (user provides custom path)
```

**Prompt 2: Project Working Directory**
```
Question: "What is the Windows path to your project working directory (where src/ and docs/ are located)?"
Header: "Project Dir"
Options:
  - Other (user must provide their path, e.g., "C:\Users\myname\Projects\MyModel")
```

**After collecting responses:**
1. Store `POWERDEVS_WSL_PATH` from Prompt 1 response
2. Store `WINDOWS_PROJECT_DIR` from Prompt 2 response
3. Derive `WSL_PROJECT_DIR` by converting Windows path:
   - Replace drive letter `X:\` with `/mnt/x/` (lowercase)
   - Replace all `\` with `/`
4. Set `DEPLOY_SUBDIR` = `examples/openaiGenModel` (default)
5. Set `MODEL_CONTEXT.user_config.collected = true`

**Example derivation:**
```
User provides: C:\Users\john\Projects\MyModel
Derived WSL:   /mnt/c/Users/john/Projects/MyModel
```

**Store in MODEL_CONTEXT:**
```
MODEL_CONTEXT.user_config = {
  powerdevs_wsl_path: "~/powerdevs-CERN",
  windows_project_dir: "C:\\Users\\john\\Projects\\MyModel",
  wsl_project_dir: "/mnt/c/Users/john/Projects/MyModel",
  deploy_subdir: "examples/openaiGenModel",
  collected: true
}
```

### Validation Workflow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    PHASE 3: VALIDATE & AUTO-CORRECT                     │
│                                                                         │
│  Input: HANDOFF_2_TO_3 (full MODEL_CONTEXT + pdm_file_path)            │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │              STEP 3.0: COLLECT USER CONFIGURATION               │   │
│  │  (AskUserQuestion: PowerDEVS path, project directory)           │   │
│  │  → MODEL_CONTEXT.user_config                                    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    STEP 3.1: PATH VERIFICATION                   │   │
│  │  For each atomic block in PDM:                                   │   │
│  │  • Check path exists: [POWERDEVS_WSL_PATH]/atomics/              │   │
│  │  • If NOT found → search for alternative path                    │   │
│  │  • Log: { path, exists, alternative }                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                  STEP 3.2: PARAMETER VERIFICATION                │   │
│  │  For each atomic block:                                          │   │
│  │  • Read actual .cpp implementation from PowerDEVS                │   │
│  │  • Extract expected parameter names from va_arg() calls          │   │
│  │  • Compare with PDM parameters                                   │   │
│  │  • Log: { block, expected_params, actual_params, mismatches }    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                  STEP 3.3: GNUPLOT VERIFICATION                  │   │
│  │  • Count total output signals in model                           │   │
│  │  • Verify GnuPlot Inputs parameter matches signal count          │   │
│  │  • Verify all saturation/integrator outputs route to GnuPlot     │   │
│  │  • Verify Format1-N parameters exist for each input              │   │
│  │  • Log: { expected_inputs, actual_inputs, missing_routes }       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                  STEP 3.4: AUTO-CORRECTION                       │   │
│  │  IF errors found:                                                │   │
│  │  • Fix invalid paths → use verified alternatives                 │   │
│  │  • Fix parameter names → use names from .cpp implementation      │   │
│  │  • Fix GnuPlot config → update Inputs and Format parameters      │   │
│  │  • Rewrite corrected PDM file                                    │   │
│  │  • Re-run validation to confirm fixes                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                              │                                          │
│                              ▼                                          │
│            MODEL_CONTEXT.validation = {                                 │
│              passed: bool,                                              │
│              checks: [...],                                             │
│              corrections: [...],                                        │
│              warnings: [...]                                            │
│            }                                                            │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step 3.1: Atomic Path Verification

**Execute via Bash to check actual PowerDEVS installation:**

```bash
# For each Path in the PDM file, verify it exists:
# Replace [POWERDEVS_WSL_PATH] with value from MODEL_CONTEXT.user_config.powerdevs_wsl_path
MSYS_NO_PATHCONV=1 wsl bash -c 'ls [POWERDEVS_WSL_PATH]/atomics/[path].h 2>/dev/null && echo "EXISTS" || echo "NOT_FOUND"'
```

**Path Verification Rules:**

| PDM Path | WSL Check Path | If Not Found → Alternative |
|----------|----------------|---------------------------|
| `sources/constant.h` | `[POWERDEVS_WSL_PATH]/atomics/sources/constant.h` | (usually exists) |
| `sources/random.h` | `[POWERDEVS_WSL_PATH]/atomics/sources/random.h` | → `random/normal_gen.h` |
| `qss/qss_multiplier.h` | `[POWERDEVS_WSL_PATH]/atomics/qss/qss_multiplier.h` | → `continuous/multiplier.h` |
| `qss/qss_saturation.h` | `[POWERDEVS_WSL_PATH]/atomics/qss/qss_saturation.h` | → `hybrid/hsaturation.h` |
| `sink/toLogger.h` | `[POWERDEVS_WSL_PATH]/atomics/sink/toLogger.h` | → `sinks/toLogger.h` |
| `sinks/gnuplot.h` | `[POWERDEVS_WSL_PATH]/atomics/sinks/gnuplot.h` | (usually exists) |

**Auto-Correction Action:** If path not found, replace with verified alternative path.

### Step 3.2: Parameter Verification

**Extract expected parameters from PowerDEVS .cpp files:**

```bash
# Read the atomic implementation to find parameter names:
# Replace [POWERDEVS_WSL_PATH] with value from MODEL_CONTEXT.user_config.powerdevs_wsl_path
MSYS_NO_PATHCONV=1 wsl bash -c 'cat [POWERDEVS_WSL_PATH]/atomics/[path].cpp | grep -A 5 "va_arg\|readDefaultParameterValue"'
```

**Known Parameter Mappings (from PowerDEVS source):**

| Atomic Block | Correct Parameters | Common Mistakes |
|--------------|-------------------|-----------------|
| `sources/constant.h` | `Constant`, `Sample Time` | (usually correct) |
| `random/normal_gen.h` | `m`, `std`, `T`, `seed` | `Mean`, `Std`, `Sample Time` |
| `continuous/multiplier.h` | (none) | `Advance method`, `dQmin`, `dQrel` |
| `hybrid/hsaturation.h` | `xl` (double), `xu` (double) | `MinVal`, `MaxVal`, `Method` |
| `qss/qss_integrator.h` | `Method`, `dqmin`, `dqrel`, `x0` | (usually correct) |
| `qss/qss_wsum.h` | `K[0-7]`, `Inputs` | (usually correct) |
| `sinks/gnuplot.h` | `Inputs`, `Format`, `Format1-5` | `%Inputs` instead of number |

**Parameter Type Rules:**

| Type | Format | Example |
|------|--------|---------|
| `Str` | String value | `Constant = Str; 1.37 ; description` |
| `Val` | Double value (no quotes) | `xl = Val; 0.0 ; description` |
| `Lst` | List selection | `Method = Lst; 2%QSS%QSS2%QSS3% ;` |

**Auto-Correction Action:** Replace incorrect parameter names with correct ones from .cpp source.

### Step 3.3: GnuPlot Output Verification

**Verification Checklist:**

```
□ GnuPlot.Ports matches expected input count (e.g., "3 ; 0" for 3 inputs)
□ GnuPlot.Parameters.Inputs = explicit number (NOT "%Inputs")
□ Format parameter includes: set xrange, set yrange, set grid, set title
□ Format1 through FormatN defined for each input
□ All output-producing blocks (saturation, integrator) have routes to GnuPlot
□ Line connections use correct component indices
```

**GnuPlot Parameter Template:**

```
Parameters
    {
    Inputs = Str; [N] ; Number of inputs (explicit number, not %Inputs)
    Format = Str; set xrange [0:%tf] @ set yrange [min:max] @ set grid @ set title '[title]' @ set ylabel '[ylabel]' @ set xlabel 'Time' @ set key top right ; General Formatting
    Format1 = Str; with lines lw 2 lc rgb '[color1]' title '[label1]' ; Input 1
    Format2 = Str; with lines lw 2 lc rgb '[color2]' title '[label2]' ; Input 2
    Format3 = Str; with lines lw 2 lc rgb '[color3]' title '[label3]' ; Input 3
    Format4 = Str;  ; Unused
    Format5 = Str;  ; Unused
    }
```

**Auto-Correction Action:** Update GnuPlot parameters to match actual input count and add missing Format entries.

### Step 3.4: Auto-Correction Process

**When errors are detected, execute corrections in order:**

```
1. PATH CORRECTIONS
   - Read current PDM file
   - For each invalid path:
     - Determine correct alternative from verification table
     - Update Path = line in PDM
   - Write corrected PDM

2. PARAMETER CORRECTIONS
   - For each block with parameter mismatches:
     - Read correct parameter names from .cpp source
     - Rebuild Parameters block with correct names
     - Preserve values, update names only
   - Write corrected PDM

3. GNUPLOT CORRECTIONS
   - Count actual inputs connected to GnuPlot
   - Update Inputs parameter to explicit count
   - Ensure Format1-N exist for each input
   - Update Format with proper ranges and labels
   - Write corrected PDM

4. RE-VALIDATION
   - Run Steps 3.1-3.3 again on corrected PDM
   - If still errors → report and suggest manual review
   - If passed → proceed to Phase 4
```

### Validation Commands Reference

**Verify all paths in PDM exist:**
```bash
# Replace [POWERDEVS_WSL_PATH] with value from MODEL_CONTEXT.user_config.powerdevs_wsl_path
MSYS_NO_PATHCONV=1 wsl bash -c '
for path in sources/constant random/normal_gen continuous/multiplier hybrid/hsaturation sinks/gnuplot sinks/toLogger; do
  if [ -f [POWERDEVS_WSL_PATH]/atomics/${path}.h ]; then
    echo "✓ ${path}.h"
  else
    echo "✗ ${path}.h NOT FOUND"
  fi
done
'
```

**Extract parameters from atomic implementation:**
```bash
# Replace [POWERDEVS_WSL_PATH] with value from MODEL_CONTEXT.user_config.powerdevs_wsl_path
MSYS_NO_PATHCONV=1 wsl bash -c 'cat [POWERDEVS_WSL_PATH]/atomics/[category]/[block].cpp | head -40'
```

**Verify GnuPlot connections in PDM:**
```bash
# Count lines going to GnuPlot (component 13 in example)
grep -c "Sink = Cmp ; 13" src/[model].pdm
```

### Validation Output

```
MODEL_CONTEXT.validation = {
  passed: true/false,

  path_checks: [
    { path: "sources/constant.h", exists: true },
    { path: "random/normal_gen.h", exists: true },
    { path: "continuous/multiplier.h", exists: true },
    { path: "hybrid/hsaturation.h", exists: true },
    { path: "sinks/gnuplot.h", exists: true },
    { path: "sinks/toLogger.h", exists: true }
  ],

  parameter_checks: [
    { block: "Noise F0", expected: ["m","std","T","seed"], actual: ["m","std","T","seed"], valid: true },
    { block: "Sat F0", expected: ["xl","xu"], actual: ["xl","xu"], valid: true },
    { block: "GnuPlot", expected: ["Inputs","Format","Format1-5"], actual: ["Inputs=3","Format","Format1-3"], valid: true }
  ],

  gnuplot_check: {
    expected_inputs: 3,
    actual_inputs: 3,
    inputs_param: "3",
    format_entries: ["Format1","Format2","Format3"],
    all_outputs_routed: true
  },

  corrections: [
    { type: "path", block: "Noise F0", old: "sources/random.h", new: "random/normal_gen.h" },
    { type: "parameter", block: "Noise F0", old: "Mean", new: "m" },
    { type: "gnuplot", field: "Inputs", old: "%Inputs", new: "3" }
  ],

  warnings: [
    { type: "suggestion", message: "Consider using different random seeds for each noise block" }
  ]
}
```

### Phase 3 → Phase 4 Handoff

```
HANDOFF_3_TO_4 = {
  ...MODEL_CONTEXT,

  validation: MODEL_CONTEXT.validation,
  pdm_corrected: true/false,
  correction_count: N,
  ready_for_deployment: MODEL_CONTEXT.validation.passed
}
```

**Proceed to Phase 4 only if `validation.passed == true`**

---

## Semantic Continuity Summary

| Phase | Reads From | Writes To | Key Consistency Checks |
|-------|------------|-----------|------------------------|
| 0 | resources/ | `catalog` | N/A |
| 1 | `catalog`, user input | `model`, `equations`, `state_variables`, `parameters`, `qss_config` | Components exist in `catalog.atomics` |
| 2 | All Phase 0 & 1 context | `components`, `connections`, `junctions` | Paths match `catalog.atomics` keys |
| 3 | All previous context + `user_config` | `validation` | Counts match, indices valid, user paths verified |
| 4 | `validation.passed`, `pdm_file_path`, `user_config` | `deployment` | Validation passed, WSL path exists |
| 5 | Full MODEL_CONTEXT + `user_config` | `summary` | All phases completed, markdown generated |

**Cross-Phase References:**
- Phase 1 uses `catalog.atomics` to validate component selection
- Phase 2 uses `catalog.atomics[path].params` for exact parameter syntax
- Phase 2 uses `equations` to verify all terms have components
- Phase 3 uses `user_config.powerdevs_wsl_path` for path verification
- Phase 3 uses `state_variables` to verify integrator coverage
- Phase 3 uses `connections` to verify routing completeness
- Phase 4 uses `user_config` for deployment paths
- Phase 5 uses `user_config` for file location documentation

---

## PowerDEVS Atomic Block Reference

### QSS Integrator
```
Path: qss/qss_integrator.h (from catalog.atomics)
Ports: 1 ; 1
Parameters:
  Method = Lst; [MODEL_CONTEXT.qss_config.method index]%QSS%QSS2%QSS3%...
  dqmin = Str; [MODEL_CONTEXT.qss_config.dqmin]
  dqrel = Str; [MODEL_CONTEXT.qss_config.dqrel]
  x0 = Str; [MODEL_CONTEXT.state_variables[i].initial]
```

### WSum
```
Path: qss/qss_wsum.h (from catalog.atomics)
Ports: [N from routing] ; 1
Parameters:
  K[0-7] = Str; [coefficients from MODEL_CONTEXT.equations]
  Inputs = Str; %Inputs
```

### Multiplier (PREFERRED: continuous/multiplier.h)
```
Path: continuous/multiplier.h
Ports: 2 ; 1
Parameters: NONE (no parameters required)
Description: Simple 2-input multiplier, no configuration needed
```

**Alternative (may have parsing issues):**
```
Path: qss/qss_multiplier.h
Ports: 2 ; 1
Parameters:
  Advance method = Lst; 1%Purely static%Estimate step%
  dQmin = Str; 1e-6
  dQrel = Str; 1e-3
```

### Saturation (PREFERRED: hybrid/hsaturation.h)
```
Path: hybrid/hsaturation.h
Ports: 1 ; 1
Parameters:
  xl = Val; [lower_bound] ; Lower saturation limit (double)
  xu = Val; [upper_bound] ; Upper saturation limit (double)
```

**Alternative (may have parsing issues):**
```
Path: qss/qss_saturation.h
Ports: 1 ; 1
Parameters:
  Method = Lst; 1%Purely static%Estimate step%
  dQmin = Str; 1e-6
  dQrel = Str; 1e-3
  MinVal = Str; [lower_bound]
  MaxVal = Str; [upper_bound]
```

### Normal Random Generator
```
Path: random/normal_gen.h
Ports: 0 ; 1
Parameters:
  m = Str; [mean] ; Mean value
  std = Str; [standard_deviation] ; Standard deviation
  T = Str; [sample_period] ; Sampling period
  seed = Str;  ; Random seed (empty string for random)
```

### Constant Source
```
Path: sources/constant.h
Ports: 0 ; 1
Parameters:
  Constant = Str; [value] ; Constant output value
  Sample Time = Str; 0 ; 0 for continuous
```

### GnuPlot Sink
```
Path: sinks/gnuplot.h
Ports: [N] ; 0
Parameters:
  Inputs = Str; [N] ; EXPLICIT NUMBER (not %Inputs)
  Format = Str; set xrange [0:%tf] @ set yrange [min:max] @ set grid @ set title '[title]' @ set ylabel '[ylabel]' @ set xlabel 'Time' @ set key top right ; General formatting (@ = newline)
  Format1 = Str; with lines lw 2 lc rgb '[color]' title '[label]' ; Input 1
  Format2 = Str; with lines lw 2 lc rgb '[color]' title '[label]' ; Input 2
  Format3 = Str; with lines lw 2 lc rgb '[color]' title '[label]' ; Input 3
  Format4 = Str;  ; Unused
  Format5 = Str;  ; Unused
```

### Logger Sink
```
Path: sinks/toLogger.h
Ports: 1 ; 0
Parameters: NONE (no parameters required)
```

---

## Validated Atomic Block Reference

**IMPORTANT:** Use these validated paths and parameters to avoid runtime errors.

| Block Type | Recommended Path | Parameters | Notes |
|------------|------------------|------------|-------|
| Constant | `sources/constant.h` | `Constant`, `Sample Time` | Sample Time=0 for continuous |
| Random Normal | `random/normal_gen.h` | `m`, `std`, `T`, `seed` | seed="" for random |
| Multiplier | `continuous/multiplier.h` | (none) | Simpler than qss version |
| Saturation | `hybrid/hsaturation.h` | `xl`, `xu` (Val type) | Use Val not Str for doubles |
| Integrator | `qss/qss_integrator.h` | `Method`, `dqmin`, `dqrel`, `x0` | Standard QSS integrator |
| WSum | `qss/qss_wsum.h` | `K[0-7]`, `Inputs` | Up to 8 inputs |
| GnuPlot | `sinks/gnuplot.h` | `Inputs`, `Format`, `Format1-5` | Inputs must be explicit number |
| Logger | `sinks/toLogger.h` | (none) | Just logs to file |

---

## QSS Method Selection Guide

| System Type | Recommended Method | dqrel | Method Code |
|-------------|-------------------|-------|-------------|
| Smooth, non-stiff | QSS2 or QSS3 | 1e-3 | 1 or 2 |
| Stiff systems | LIQSS2 or LIQSS3 | 1e-4 | 7 or 8 |
| Discontinuous | BQSS or CQSS | 1e-3 | 4 or 5 |
| High accuracy | QSS4 | 1e-5 | 3 |
| Fast prototyping | QSS | 1e-2 | 0 |

---

## Common System Templates

### Lotka-Volterra (Predator-Prey)
```
Equations: dx/dt = ax - bxy, dy/dt = cxy - dy
Components: 2 Integrators, 1 Multiplier, 2 WSums, 1 GnuPlot, 2 ToLoggers
Reference: resources/models/lotka_volterra.pdm
```

### Mass-Spring-Damper
```
Equations: dx1/dt = x2, dx2/dt = (F - cx2 - kx1)/m
Components: 2 Integrators, 1 WSum, 1 Source, 1 GnuPlot
Reference: resources/models/inverted_pendulum.pdm (similar structure)
```

### Lorenz Attractor
```
Equations: dx/dt = σ(y-x), dy/dt = x(ρ-z)-y, dz/dt = xy - βz
Components: 3 Integrators, 3 Multipliers, 3 WSums, 1 GnuPlot
Reference: Similar to resources/models/vanderpol_osc.pdm
```

---

## Troubleshooting

### Common Runtime Errors

| Error Message | Cause | Solution |
|---------------|-------|----------|
| `parameter 'X' NOT FOUND` | Wrong parameter names | Use exact names from .cpp source (e.g., `m` not `Mean`) |
| `parameter 'Purely static' NOT FOUND` | qss_multiplier parsing issue | Use `continuous/multiplier.h` instead (no params) |
| `parameter 'MinVal' NOT FOUND` | qss_saturation parsing issue | Use `hybrid/hsaturation.h` with `xl`, `xu` params |
| `file not found` for atomic | Invalid path | Verify path exists: `wsl ls [POWERDEVS_WSL_PATH]/atomics/[path].h` |
| GnuPlot shows no data | Missing connections | Verify Line entries connect outputs to GnuPlot inputs |
| GnuPlot wrong number of traces | Inputs param wrong | Set `Inputs = Str; N` (explicit number, not %Inputs) |

### Path Resolution Errors

| Invalid Path | Correct Path | Notes |
|--------------|--------------|-------|
| `sources/random.h` | `random/normal_gen.h` | Different directory |
| `sink/toLogger.h` | `sinks/toLogger.h` | Missing 's' |
| `qss/qss_multiplier.h` | `continuous/multiplier.h` | Simpler alternative |
| `qss/qss_saturation.h` | `hybrid/hsaturation.h` | Simpler alternative |

### Parameter Name Errors

| Block | Wrong Parameters | Correct Parameters |
|-------|------------------|-------------------|
| `random/normal_gen.h` | `Mean`, `Std`, `Sample Time`, `Seed` | `m`, `std`, `T`, `seed` |
| `hybrid/hsaturation.h` | `MinVal`, `MaxVal`, `Method` | `xl`, `xu` (Val type) |
| `continuous/multiplier.h` | `Advance method`, `dQmin`, `dQrel` | (none required) |
| `sinks/gnuplot.h` | `Inputs = %Inputs` | `Inputs = 3` (explicit) |

### General Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Model doesn't run | Syntax error | Check `components` indices match declaration order |
| Wrong results | Incorrect routing | Verify `connections` against `equations` |
| Very slow | Quantum too small | Increase `qss_config.dqrel` |
| Oscillations | Quantum too large | Decrease `qss_config.dqrel` or use higher QSS method |

---

## Phase 4: Deploy to WSL PowerDEVS

**Purpose:** Copy the generated .pdm file to the WSL PowerDEVS installation for simulation.

**Depends on:** Phase 3 validation passing

**Target Directory:** `[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/` (from `MODEL_CONTEXT.user_config`)

### Deployment Execution

```
┌─────────────────────────────────────────────────────────┐
│                  PHASE 4: DEPLOY                        │
│                                                         │
│  Input: HANDOFF_2_TO_3.pdm_file_path                   │
│         MODEL_CONTEXT.validation.passed == true        │
│         MODEL_CONTEXT.user_config (from Phase 3)       │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Step 4.1: Copy to WSL                           │   │
│  │                                                 │   │
│  │ Source: src/[model_name].pdm                    │   │
│  │ Target: [POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/   │   │
│  │         [model_name].pdm                        │   │
│  └────────────────────────┬────────────────────────┘   │
│                           ▼                             │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Step 4.2: Verify deployment                     │   │
│  └────────────────────────┬────────────────────────┘   │
│                           ▼                             │
│            MODEL_CONTEXT.deployment = {                 │
│              deployed: true,                            │
│              wsl_path: "[POWERDEVS_WSL_PATH]/..."      │
│            }                                            │
└─────────────────────────────────────────────────────────┘
```

### Step 4.1: Copy to WSL

Execute the following Bash command to copy the generated model to WSL:

```bash
# Replace placeholders with values from MODEL_CONTEXT.user_config:
#   [WSL_PROJECT_DIR]    = user_config.wsl_project_dir
#   [POWERDEVS_WSL_PATH] = user_config.powerdevs_wsl_path
#   [DEPLOY_SUBDIR]      = user_config.deploy_subdir
MSYS_NO_PATHCONV=1 wsl bash -c 'cp [WSL_PROJECT_DIR]/src/[model_name].pdm [POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/'
```

**Path Mapping:**
| Windows Path | WSL Path |
|--------------|----------|
| `[WINDOWS_PROJECT_DIR]\src\` | `[WSL_PROJECT_DIR]/src/` |
| WSL `[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/` | Target directory |

**Important:** The `MSYS_NO_PATHCONV=1` environment variable prevents Git Bash from converting Linux-style paths.

### Step 4.2: Verify Deployment

```bash
# Replace placeholders with values from MODEL_CONTEXT.user_config
MSYS_NO_PATHCONV=1 wsl bash -c 'ls -la [POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/[model_name].pdm'
```

### Phase 4 Output

```
MODEL_CONTEXT.deployment = {
  deployed: true,
  source_path: "src/[model_name].pdm",
  wsl_path: "[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/[model_name].pdm",
  timestamp: "[ISO timestamp]"
}
```

### Running the Model in PowerDEVS

After deployment, the model can be opened in PowerDEVS running in WSL:

1. Launch PowerDEVS in WSL
2. Open `[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/[model_name].pdm`
3. Configure simulation parameters (final time, etc.)
4. Run simulation and view GnuPlot output

## Phase 5: Generate Model Summary

**Purpose:** Create a comprehensive markdown summary document for the generated model.

**Depends on:** All previous phases completed successfully

**Output Directory:** `docs/models/`

### Summary Generation

```
┌─────────────────────────────────────────────────────────┐
│                  PHASE 5: SUMMARIZE                     │
│                                                         │
│  Input: Full MODEL_CONTEXT from all phases             │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Step 5.1: Generate markdown summary             │   │
│  │                                                 │   │
│  │ Content:                                        │   │
│  │ • Model metadata (name, description, source)   │   │
│  │ • System equations                             │   │
│  │ • Parameters table                             │   │
│  │ • Component inventory                          │   │
│  │ • Signal routing diagram                       │   │
│  │ • File locations                               │   │
│  │ • Usage instructions                           │   │
│  └────────────────────────┬────────────────────────┘   │
│                           ▼                             │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Step 5.2: Write to docs/models/[model_name].md │   │
│  └────────────────────────┬────────────────────────┘   │
│                           ▼                             │
│            MODEL_CONTEXT.summary = {                    │
│              generated: true,                           │
│              path: "docs/models/[model_name].md"        │
│            }                                            │
└─────────────────────────────────────────────────────────┘
```

### Summary Template

Write the following markdown structure to `docs/models/[model_name].md`:

```markdown
# PowerDEVS Model: [MODEL_CONTEXT.model.name]

## Overview

| Property | Value |
|----------|-------|
| **Model Name** | [model_name] |
| **Description** | [MODEL_CONTEXT.model.description] |
| **Generated** | [timestamp] |
| **Source** | [reference if applicable] |

## System Equations

[List all equations from MODEL_CONTEXT.equations]

```
dx/dt = [equation]
```

## Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| [name] | [value] | [description] |

## State Variables

| Variable | Initial Value | Units | Description |
|----------|---------------|-------|-------------|
| [name] | [x0] | [units] | [meaning] |

## QSS Configuration

| Setting | Value |
|---------|-------|
| Method | [QSS/QSS2/QSS3/etc.] |
| dqmin | [value] |
| dqrel | [value] |

## Component Inventory

| # | Component | Type | Path | Description |
|---|-----------|------|------|-------------|
| 1 | [name] | [type] | [path] | [description] |

**Total Components:** [N]

## Signal Routing

```
[component1].out → [component2].in
[component2].out → [component3].in
...
```

## File Locations

| Location | Path |
|----------|------|
| **Windows** | `[WINDOWS_PROJECT_DIR]\src\[model_name].pdm` |
| **WSL** | `[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/[model_name].pdm` |
| **Summary** | `docs/models/[model_name].md` |

## Usage

### Running in PowerDEVS

1. Open PowerDEVS in WSL
2. File → Open → `[POWERDEVS_WSL_PATH]/[DEPLOY_SUBDIR]/[model_name].pdm`
3. Set simulation final time (e.g., 100 seconds)
4. Click Run to execute simulation
5. View results in GnuPlot window

### Modifying Parameters

To adjust model behavior, edit the following in PowerDEVS:
- [List key parameters that can be modified]

## Validation

| Check | Status |
|-------|--------|
| Structure | ✓ Passed |
| Routing | ✓ Passed |
| Parameters | ✓ Passed |

---

*Generated by PowerDEVS Builder Startup Skill*
```

### Phase 5 Output

```
MODEL_CONTEXT.summary = {
  generated: true,
  path: "docs/models/[model_name].md",
  timestamp: "[ISO timestamp]"
}
```

---

## Related Skills
- [DEVS Builder](../devs-builder/) - Discrete event DEVS models in Java
- [PowerDEVS Builder](../power-devs-builder/) - Personalized version with hardcoded paths
- [SPARC Methodology](../sparc-methodology/) - Structured development workflow
