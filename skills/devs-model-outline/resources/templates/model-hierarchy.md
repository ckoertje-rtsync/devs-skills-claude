# Model Structure & Hierarchy — {{system_name}}

> Mermaid `classDiagram` for model structure and `graph BT` for composition hierarchy.

## Class Diagram

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

    {{#each atomic_models}}
    class {{name}} {
        {{#each formal_spec.X}}
        +Port~{{event_type}}~ {{port_name}}
        {{/each}}
        {{#each formal_spec.Y}}
        +Port~{{event_type}}~ {{port_name}}
        {{/each}}
        -String phase
        -double sigma
        {{#each state_variables}}
        -{{type}} {{name}}
        {{/each}}
        +initialize()
        +internalTransition()
        +externalTransition(double, MessageBag)
        +getOutput() MessageBag
        +getTimeAdvance() double
    }
    DEVSModel <|-- {{name}}
    {{/each}}

    {{#each coupled_models}}
    class {{name}} {
        {{#each components}}
        +{{component_type}} {{component_name}}
        {{/each}}
        +initialize()
    }
    DEVSModel <|-- {{name}}
    {{#each components}}
    {{../name}} *-- {{component_type}} : contains
    {{/each}}
    {{/each}}
```

## Composition Hierarchy

```mermaid
graph BT
    {{#each hierarchy_atomic_nodes}}
    {{id}}["{{name}}<br/>‹Atomic›"]
    {{/each}}

    {{#each hierarchy_coupled_nodes}}
    {{id}}{{"{{name}}<br/>‹Coupled›"}}
    {{/each}}

    {{#each hierarchy_edges}}
    {{child_id}} --> {{parent_id}}
    {{/each}}

    style {{root_id}} fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
```

## Hierarchy Summary

| Property | Value |
|----------|-------|
| **Root Model** | {{hierarchy_root}} |
| **Hierarchy Depth** | {{hierarchy_depth}} |
| **Total Atomic Models** | {{atomic_model_count}} |
| **Total Coupled Models** | {{coupled_model_count}} |
| **Total Components** | {{total_component_count}} |

## Component Cross-Reference

| Component | Parent Model | Type | States | Ports (In/Out) |
|-----------|-------------|------|--------|----------------|
{{#each component_cross_ref}}
| {{component_name}} | {{parent_model}} | {{model_type}} | {{state_count}} | {{input_ports}}/{{output_ports}} |
{{/each}}
