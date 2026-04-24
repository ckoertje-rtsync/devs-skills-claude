# Mermaid label safety (stateDiagram-v2)

Mermaid's stateDiagram-v2 grammar parses transition labels leniently, so most CLI renderers (`@mermaid-js/mermaid-cli` / `mmdc`) accept labels that will nonetheless *render poorly* or *parse-reject* in GitHub, VS Code preview, or older Mermaid versions. Follow these rules to produce diagrams that render identically everywhere.

## Prefer ASCII in labels

Greek letters (`σ`, `τ`) and math symbols (`≤`, `≥`, `×`) render in the latest Mermaid but frequently corrupt under VS Code's bundled preview and in dark-mode themes where fonts fall back. Use ASCII tokens (`sigma`, `tau`, `<=`, `>=`, `x`). Reserve Greek glyphs for table cells and prose, not the diagram.

## Avoid `:=` in transition labels

The label separator is `:`. Some renderers get confused when a second `:` appears shortly after it (e.g., `x := 1`). Use `=` instead (`x = 1`).

## Avoid `;` in transition labels

`;` is a statement-terminator candidate in several Mermaid grammars. It usually passes the parser but can render with odd whitespace. Use `,` or `/` as separators.

## Use `/` as the trigger-action separator

UML convention: `trigger / action`. This keeps labels scannable and matches standard state-chart notation:

```
running --> paused : tau_ext inPause / passivate
```

## Consolidate no-op transitions into `note` blocks

Parameter updates that do not change phase (e.g., `inTemperature / T = msg` on a model that stays in `running`) produce self-loops that pile on top of each other when rendered. The fourth self-loop on a state is usually invisible. Collapse these into a single `note` block:

```
note right of running
    In-place parameter updates (no phase change):
    inTemperature  -> T  = msg
    inField        -> h  = msg, recompute energy
    inReset        -> reinit lattice
end note
```

Only structural transitions (those that actually change phase) belong on edges.

## Keep labels on one line

`<br/>` is accepted in the latest Mermaid but not universally. If a label feels too long, split it across multiple transitions or move detail into a `note`.

## Keep parentheses, but keep them simple

`tau_int (sigma = dt)` is fine. `foo(bar(baz))` is not — nested parens trip some parsers.

## Canonical shape for a DEVS atomic

```
stateDiagram-v2
    [*] --> {initial_phase} : initialize / holdIn({initial_phase}, {sigma})

    {phase_a} --> {phase_a} : tau_int (sigma = {dt}) / {summary of internal action}
    {phase_a} --> {phase_b} : tau_ext {port} / {phase-changing action}
    {phase_b} --> {phase_a} : tau_ext {port} / {phase-changing action}

    note right of {phase_a}
        In-place updates (no phase change):
        {port1}  -> {effect}
        {port2}  -> {effect}
    end note
```

## Validate before shipping

Always run `mmdc` over the Mermaid block before writing the final file. The canonical command lives in `resources/scripts/validate-mermaid.sh`; the SKILL.md VALIDATE stage invokes it.
