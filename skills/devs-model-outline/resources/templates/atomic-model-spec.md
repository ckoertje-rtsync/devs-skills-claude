# Atomic Model Specifications — {{system_name}}

> Formal DEVS specifications for all atomic models in the system.

{{#each atomic_models}}

---

## {{name}} ({{id}})

{{#if source_file}}
**Source:** `{{source_file}}`
{{/if}}

### Formal Specification

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

{{/each}}
