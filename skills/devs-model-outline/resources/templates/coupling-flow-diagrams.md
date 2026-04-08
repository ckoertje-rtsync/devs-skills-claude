# Coupling & Data Flow Diagrams — {{system_name}}

> Mermaid `flowchart TD` for each coupled model showing component topology and data flow.

{{#each coupled_models}}

---

## {{name}} ({{id}})

```mermaid
flowchart TD
    subgraph {{id}}["{{name}}"]
        direction LR

        %% External input ports
        {{#each formal_spec.X}}
        EXT_IN_{{port_name}}(["⬇ {{port_name}}"])
        {{/each}}

        %% Component models
        {{#each components}}
        {{component_id}}["{{component_name}}\n‹Atomic›\nStates: {{state_count}}"]
        {{/each}}

        %% External output ports
        {{#each formal_spec.Y}}
        EXT_OUT_{{port_name}}(["⬆ {{port_name}}"])
        {{/each}}

        %% EIC (External Input Couplings) — dashed arrows
        {{#each formal_spec.Z_ij.EIC}}
        EXT_IN_{{source_port}} -.->|"EIC"| {{dest_component}}
        {{/each}}

        %% IC (Internal Couplings) — solid arrows
        {{#each formal_spec.Z_ij.IC}}
        {{source_component}} -->|"{{source_port}} → {{dest_port}}"| {{dest_component}}
        {{/each}}

        %% EOC (External Output Couplings) — dashed arrows
        {{#each formal_spec.Z_ij.EOC}}
        {{source_component}} -.->|"EOC"| EXT_OUT_{{dest_port}}
        {{/each}}
    end
```

### Coupling Legend

| Arrow Style | Coupling Type | Description |
|-------------|---------------|-------------|
| `-.->` dashed | **EIC** (External Input Coupling) | External port → component port |
| `-->` solid | **IC** (Internal Coupling) | Component port → component port |
| `-.->` dashed | **EOC** (External Output Coupling) | Component port → external port |

### Data Flow Analysis

| Coupling | Source | Port | Destination | Port | Data Type |
|----------|--------|------|-------------|------|-----------|
{{#each formal_spec.Z_ij.EIC}}
| EIC | {{name}} (ext) | `{{source_port}}` | {{dest_component}} | `{{dest_port}}` | `{{data_type}}` |
{{/each}}
{{#each formal_spec.Z_ij.IC}}
| IC | {{source_component}} | `{{source_port}}` | {{dest_component}} | `{{dest_port}}` | `{{data_type}}` |
{{/each}}
{{#each formal_spec.Z_ij.EOC}}
| EOC | {{source_component}} | `{{source_port}}` | {{name}} (ext) | `{{dest_port}}` | `{{data_type}}` |
{{/each}}

{{/each}}
