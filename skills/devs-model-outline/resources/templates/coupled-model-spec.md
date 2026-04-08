# Coupled Model Specifications — {{system_name}}

> Formal DEVS specifications for all coupled models in the system.

{{#each coupled_models}}

---

## {{name}} ({{id}})

{{#if source_file}}
**Source:** `{{source_file}}`
{{/if}}

### Formal Specification

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

{{/each}}
