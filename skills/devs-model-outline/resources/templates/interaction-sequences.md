# Interaction Sequence Diagrams — {{system_name}}

> Mermaid `sequenceDiagram` for each coupled model showing message passing between components.

{{#each coupled_models}}

---

## {{name}} ({{id}})

### Scenario: {{scenario_name}}

{{scenario_description}}

```mermaid
sequenceDiagram
    participant ENV as Environment
    {{#each participants}}
    participant {{component_id}} as {{component_name}}
    {{/each}}

    Note over ENV,{{last_participant}}: Scenario: {{scenario_name}}

    {{#each sequence_steps}}
    {{#if is_external_input}}
    ENV->>{{target}}: {{event}} via {{port}}
    {{/if}}
    {{#if is_delta_ext}}
    Note over {{target}}: δ_ext: {{from_state}} → {{to_state}}\nta = {{sigma}}
    activate {{target}}
    {{/if}}
    {{#if is_processing}}
    {{target}}->>{{target}}: Processing (ta = {{sigma}})
    {{/if}}
    {{#if is_lambda}}
    Note over {{source}}: λ: {{port}} → {{value}}
    {{/if}}
    {{#if is_internal_coupling}}
    {{source}}-->>{{target}}: {{value}} via {{source_port}}→{{dest_port}}
    {{/if}}
    {{#if is_delta_int}}
    Note over {{target}}: δ_int: {{from_state}} → {{to_state}}
    deactivate {{target}}
    {{/if}}
    {{#if is_external_output}}
    {{source}}-->>ENV: {{value}} via {{port}}
    {{/if}}
    {{#if is_concurrent}}
    par Concurrent transitions
        {{#each concurrent_steps}}
        {{source}}->>{{target}}: {{event}}
        Note over {{target}}: {{transition_note}}
        {{/each}}
    end
    {{/if}}
    {{/each}}
```

### Sequence Walkthrough

| Step | Time | Component | Event | Description |
|------|------|-----------|-------|-------------|
{{#each sequence_steps}}
| {{step_number}} | t={{time}} | {{component}} | {{event_type}} | {{description}} |
{{/each}}

{{/each}}
