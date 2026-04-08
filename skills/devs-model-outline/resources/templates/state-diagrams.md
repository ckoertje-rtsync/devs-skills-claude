# State Diagrams — {{system_name}}

> Mermaid `stateDiagram-v2` for each atomic model showing states, transitions, and time advances.

{{#each atomic_models}}

---

## {{name}} ({{id}})

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

### State Descriptions

| State | Time Advance (ta) | Description |
|-------|-------------------|-------------|
{{#each formal_spec.S}}
| `{{name}}` | {{sigma}} | {{description}} |
{{/each}}

### Transition Summary

| Type | Trigger | From | To | Description |
|------|---------|------|----|-------------|
{{#each formal_spec.delta_int}}
| δ_int | Time advance expired | `{{from_state}}` | `{{to_state}}` | {{description}} |
{{/each}}
{{#each formal_spec.delta_ext}}
| δ_ext | `{{input_port}}` event | `{{current_state}}` | `{{to_state}}` | {{description}} |
{{/each}}

{{/each}}
