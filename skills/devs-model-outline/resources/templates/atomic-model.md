# {{name}} ({{id}}) — Atomic Model Specification

> Self-contained DEVS specification and state diagram for the **{{name}}** atomic model.

{{#if source_file}}
**Source:** `{{source_file}}`
{{/if}}

## Formal Specification

**M = \<X, Y, S, δ_int, δ_ext, λ, ta\>**

### Input Set (X)

| Port Name | Event Type | Description |
|-----------|------------|-------------|
{{#each formal_spec.X}}
| `{{port_name}}` | `{{event_type}}` | {{description}} |
{{/each}}

### Output Set (Y)

| Port Name | Event Type | Description |
|-----------|------------|-------------|
{{#each formal_spec.Y}}
| `{{port_name}}` | `{{event_type}}` | {{description}} |
{{/each}}

### State Set (S)

| State | Description | σ (Time Advance) | Passive? |
|-------|-------------|-------------------|----------|
{{#each formal_spec.S}}
| `{{name}}` | {{description}} | {{sigma}} | {{is_passive}} |
{{/each}}

> **Phase variable:** phase ∈ { {{state_names_list}} }
>
> **Sigma variable:** σ ∈ ℝ⁺ ∪ {∞}

### Internal Transition Function (δ_int)

| From State | To State | Description |
|------------|----------|-------------|
{{#each formal_spec.delta_int}}
| `{{from_state}}` | `{{to_state}}` | {{description}} |
{{/each}}

### External Transition Function (δ_ext)

| Current State | Input Port | Event | To State | Description |
|---------------|------------|-------|----------|-------------|
{{#each formal_spec.delta_ext}}
| `{{current_state}}` | `{{input_port}}` | `{{event}}` | `{{to_state}}` | {{description}} |
{{/each}}

### Output Function (λ)

> Fires immediately before δ_int executes.

| State | Output Port | Value | Description |
|-------|-------------|-------|-------------|
{{#each formal_spec.lambda}}
| `{{state}}` | `{{output_port}}` | `{{value}}` | {{description}} |
{{/each}}

### Time Advance Function (ta)

| State | Duration | Description |
|-------|----------|-------------|
{{#each formal_spec.ta}}
| `{{state}}` | {{duration}} | {{description}} |
{{/each}}

{{#if confluent_transition}}
### Confluent Transition

{{confluent_transition}}
{{/if}}

{{#if state_variables}}
### Additional State Variables

| Variable | Type | Initial Value | Description |
|----------|------|---------------|-------------|
{{#each state_variables}}
| `{{name}}` | `{{type}}` | `{{initial}}` | {{description}} |
{{/each}}
{{/if}}

## State Diagram

Mermaid `stateDiagram-v2`. States show their time advance (`ta`); passive states (`ta = INFINITY`) wait indefinitely for external input. Greek `δ`/`λ` are used here, where `stateDiagram-v2` renders them reliably.

```mermaid
stateDiagram-v2
    [*] --> {{initial_state}}

    {{#each formal_spec.S}}
    {{name}} : {{name}} (ta = {{sigma}}{{#if is_passive}}, passive{{/if}})
    {{/each}}

    {{#each formal_spec.delta_int}}
    {{from_state}} --> {{to_state}} : δ_int{{#if description}}, {{description}}{{/if}}
    {{/each}}

    {{#each formal_spec.delta_ext}}
    {{current_state}} --> {{to_state}} : {{input_port}} (δ_ext)
    {{/each}}

    {{#each formal_spec.lambda}}
    note right of {{state}} : λ {{output_port}} {{value}}
    {{/each}}
```

### Transition Summary

| Type | Trigger | From | To | Description |
|------|---------|------|----|-------------|
{{#each formal_spec.delta_int}}
| δ_int | Time advance expired | `{{from_state}}` | `{{to_state}}` | {{description}} |
{{/each}}
{{#each formal_spec.delta_ext}}
| δ_ext | `{{input_port}}` event | `{{current_state}}` | `{{to_state}}` | {{description}} |
{{/each}}
