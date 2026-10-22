# {{name}} ({{id}}) — Coupled Model Specification

> Self-contained DEVS specification, coupling-flow diagram, and interaction-sequence diagram for the **{{name}}** coupled model.

{{#if source_file}}
**Source:** `{{source_file}}`
{{/if}}

## Formal Specification

**N = \<X, Y, D, {M_d}, {I_d}, {Z_ij}, Select\>**

### External Input Ports (X)

| Port Name | Event Type | Description |
|-----------|------------|-------------|
{{#each formal_spec.X}}
| `{{port_name}}` | `{{event_type}}` | {{description}} |
{{/each}}

### External Output Ports (Y)

| Port Name | Event Type | Description |
|-----------|------------|-------------|
{{#each formal_spec.Y}}
| `{{port_name}}` | `{{event_type}}` | {{description}} |
{{/each}}

### Component Set (D)

| Component ID | Model Name | Type |
|--------------|------------|------|
{{#each formal_spec.D}}
| `{{component_id}}` | {{model_name}} | {{model_type}} |
{{/each}}

### External Input Couplings (EIC)

> Routes external inputs to internal component ports.

| Source Port (N.X) | Destination Component | Destination Port |
|-------------------|-----------------------|------------------|
{{#each formal_spec.Z_ij.EIC}}
| `{{source_port}}` | `{{dest_component}}` | `{{dest_port}}` |
{{/each}}

### Internal Couplings (IC)

> Routes outputs from one component to inputs of another.

| Source Component | Source Port | Destination Component | Destination Port |
|------------------|-------------|-----------------------|------------------|
{{#each formal_spec.Z_ij.IC}}
| `{{source_component}}` | `{{source_port}}` | `{{dest_component}}` | `{{dest_port}}` |
{{/each}}

### External Output Couplings (EOC)

> Routes internal component outputs to the coupled model's external output ports.

| Source Component | Source Port | Destination Port (N.Y) |
|------------------|-------------|------------------------|
{{#each formal_spec.Z_ij.EOC}}
| `{{source_component}}` | `{{source_port}}` | `{{dest_port}}` |
{{/each}}

### Coupling Summary

| Coupling Type | Count |
|---------------|-------|
| **EIC** (External Input) | {{eic_count}} |
| **IC** (Internal) | {{ic_count}} |
| **EOC** (External Output) | {{eoc_count}} |
| **Total** | {{total_coupling_count}} |

### Tie-Breaking / Select Function

{{formal_spec.Select}}

## Coupling & Data Flow Diagram

Mermaid `flowchart TD`. Solid arrows are internal couplings (IC); dashed arrows are external input/output couplings (EIC/EOC).

```mermaid
flowchart TD
    subgraph {{id}}["{{name}}"]
        direction LR

        {{#each formal_spec.X}}
        EXT_IN_{{port_name}}(["⬇ {{port_name}}<br/><i>{{event_type}}</i>"])
        {{/each}}

        {{#each components}}
        {{component_id}}["{{component_name}}<br/>‹Atomic›<br/>{{state_count}} states"]
        {{/each}}

        {{#each formal_spec.Y}}
        EXT_OUT_{{port_name}}(["⬆ {{port_name}}<br/><i>{{event_type}}</i>"])
        {{/each}}

        {{#each formal_spec.Z_ij.EIC}}
        EXT_IN_{{source_port}} -.->|"EIC"| {{dest_component}}
        {{/each}}

        {{#each formal_spec.Z_ij.IC}}
        {{source_component}} -->|"{{source_port}} → {{dest_port}}"| {{dest_component}}
        {{/each}}

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

## Interaction Sequence

Mermaid `sequenceDiagram` tracing a representative scenario through the coupling structure. Per the house style, sequence-diagram notes use ASCII (`delta_ext`, `delta_int`, `lambda`, `->`) and `<br/>` for line breaks.

### Scenario: {{scenario_name}}

{{scenario_description}}

```mermaid
sequenceDiagram
    autonumber
    participant ENV as Environment
    {{#each participants}}
    participant {{component_id}} as {{component_name}}
    {{/each}}

    Note over ENV,{{last_participant}}: {{scenario_name}}

    {{#each sequence_steps}}
    {{#if is_external_input}}
    ENV->>{{target}}: {{event}} via {{port}}
    {{/if}}
    {{#if is_delta_ext}}
    Note over {{target}}: delta_ext: {{from_state}} -> {{to_state}}<br/>ta = {{sigma}}
    activate {{target}}
    {{/if}}
    {{#if is_lambda}}
    Note over {{source}}: lambda: {{port}} -> {{value}}
    {{/if}}
    {{#if is_internal_coupling}}
    {{source}}-->>{{target}}: {{value}} via {{source_port}} -> {{dest_port}}
    {{/if}}
    {{#if is_delta_int}}
    Note over {{target}}: delta_int: {{from_state}} -> {{to_state}}
    deactivate {{target}}
    {{/if}}
    {{#if is_external_output}}
    {{source}}-->>ENV: {{value}} via {{port}}
    {{/if}}
    {{/each}}
```

### Sequence Walkthrough

| Step | Time | Component | Event | Description |
|------|------|-----------|-------|-------------|
{{#each sequence_steps}}
| {{step_number}} | t={{time}} | {{component}} | {{event_type}} | {{description}} |
{{/each}}
