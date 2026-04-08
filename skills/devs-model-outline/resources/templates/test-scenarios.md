# Test Scenarios — {{system_name}}

> Test scenario specifications with input sequences, expected outputs, and verification criteria.

{{#each test_scenarios}}

---

## Scenario {{id}}: {{name}}

**Target Model:** {{target_model}}

{{description}}

### Initial State

| Model | State | σ (Time Advance) |
|-------|-------|-------------------|
{{#each initial_states}}
| {{model_name}} | `{{state}}` | {{sigma}} |
{{/each}}

### Input Sequence

| Time | Port | Event | Description |
|------|------|-------|-------------|
{{#each input_sequence}}
| t = {{time}} | `{{port}}` | `{{event}}` | {{description}} |
{{/each}}

### Expected Outputs

| Time | Port | Value | Description |
|------|------|-------|-------------|
{{#each expected_outputs}}
| t = {{time}} | `{{port}}` | `{{value}}` | {{description}} |
{{/each}}

### Expected Final State

| Model | State | σ (Time Advance) |
|-------|-------|-------------------|
{{#each expected_final_states}}
| {{model_name}} | `{{state}}` | {{sigma}} |
{{/each}}

### Invariants to Verify

{{#each invariants}}
- {{invariant}}
{{/each}}

{{/each}}

---

## Test Coverage Summary

| Metric | Value |
|--------|-------|
| **Total Scenarios** | {{scenario_count}} |
| **Models Covered** | {{models_covered}} |
| **Transitions Exercised** | {{transitions_exercised}} |
| **States Visited** | {{states_visited}} |
| **Coverage** | {{coverage_percentage}}% |
