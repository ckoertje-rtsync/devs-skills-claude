# Mermaid Label Safety for SES Hierarchy Diagrams

The `ses-designer` skill emits two Mermaid diagrams per SES: a `graph TD` hierarchy tree and a `graph LR` coupling map. `mmdc` (via `@mermaid-js/mermaid-cli`) accepts more than GitHub and VS Code preview render cleanly, so follow these rules to keep diagrams portable.

## Use ASCII in labels

Stick to ASCII letters, digits, underscores, and spaces. Greek glyphs, math symbols, and emoji render in the latest Mermaid but frequently corrupt under VS Code's bundled preview or dark-mode themes with font fallbacks.

## Node IDs must not contain dashes

Mermaid's ID grammar rejects `-`. The skill substitutes `_` for `-` in node IDs while keeping the display label human-readable:

```
%% Entity identifier: electric-engine
electric_engine[electric-engine]
```

Generator rule: `id = identifier.replace('-', '_')`; `label = identifier`.

## Avoid `;` in labels

`;` is a statement terminator in some Mermaid grammars. It parses in `mmdc` but renders with extra whitespace in other tools. Use `,` or `/` instead.

## Avoid `:=` in labels

`:` is the edge-label separator. A second `:` in close proximity (`x := 1`) confuses some renderers. Use `=` (`x = 1`).

## Keep edge labels on one line

`<br/>` is accepted in the latest Mermaid but not universally. If a label feels too long, split it across multiple edges or move detail into a separate `graph` block.

## Shape encoding

The skill encodes SES constructs via Mermaid node shapes. Use these consistently:

| SES construct | Mermaid shape | Template |
|---|---|---|
| Root or coupled entity (aspect host) | Rectangle | `Foo[Foo]` |
| Aspect (decomposition hub) | Rounded rectangle | `asp_bar(bar)` |
| Multi-aspect (replicated group) | Stadium | `ma_baz([baz])` |
| Specialization choice-point | Diamond | `sp_qux{qux}` |
| Specialization option | Rectangle | `opt_ElectricEngine[ElectricEngine]` |
| Atomic leaf entity | Rectangle | `Processor[Processor]` |

Prefix node IDs with `asp_`, `ma_`, `sp_`, `opt_` to avoid collisions when an aspect and an entity share a base name.

## Edges

| Relationship | Edge style |
|---|---|
| Entity → its aspect | `Foo --> asp_bar` (unlabeled) |
| Aspect → child entity | `asp_bar --> Child` (unlabeled) |
| Parent → multi-aspect | `Processors --> ma_multiProcessor` (unlabeled) |
| Multi-aspect → unit entity | `ma_multiProcessor -->\|more than one\| Processor` |
| Entity → specialization | `Processor --> sp_index` (unlabeled) |
| Specialization → option | `sp_index -->\|id\| opt_id` |

Couplings go in a separate `graph LR` block to keep the hierarchy tree legible. Coupling edge labels take the form `{message}` or `{message} as {recv_message}` when renamed.

## Validate before shipping

Always run `mmdc` over every diagram before writing the final file. The canonical command lives in `resources/scripts/validate-mermaid.sh`; the SKILL.md VALIDATE stage invokes it.
