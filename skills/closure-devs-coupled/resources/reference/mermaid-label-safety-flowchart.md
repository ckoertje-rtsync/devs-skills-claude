# Mermaid label safety (flowchart)

Block diagrams for coupled DEVS use Mermaid's `flowchart` (typically `flowchart LR`), not `stateDiagram-v2`. The portability rules overlap with the stateDiagram rules shipped in `closure-devs-atomic`, but flowchart has its own parser quirks. Follow these to produce diagrams that render identically in `mmdc`, GitHub, and VS Code preview.

## Prefer ASCII in edge labels

Greek letters (`σ`, `τ`, `λ`) and math symbols (`≤`, `≥`, `×`) render in recent Mermaid but frequently corrupt under VS Code's bundled preview and in dark-mode theme fallbacks. Use ASCII tokens (`sigma`, `tau`, `lambda`, `<=`, `>=`, `x`). Reserve Greek glyphs for prose, not the diagram.

## Avoid `:=` in labels

Flowchart labels are delimited by `|...|` (`A --> |label| B`). A `:` inside the label is legal but `:=` looks to some renderers like a broken label terminator from the state-diagram grammar. Use `=` (`x = 1`).

## Avoid `;` in labels

`;` is a statement separator in flowchart. Putting it inside a `|label|` block works, but multi-renderer behavior is inconsistent. Use `,` or `/`.

## Use `-> ` inside `|label|` for port wiring

Keep coupling labels terse and directional. The idiom that reads cleanest:

```
genr --> |out -> ariv| transd
```

i.e. `<fromPort> -> <toPort>`. When the ports happen to share a name on both sides, one port name is fine:

```
ef --> |in| proc
```

For EIC / EOC where one side is the external port (already a labeled node), label the edge with just the child-side port:

```
in_ext((in)) --> |solved| transd
genr --> |out| out_ext((out))
```

## Node shapes — keep them stable

Pick one shape per role and don't mix:

- **External ports** — round: `portname((portname))`
- **Children (atomic)** — rectangle: `varname["varname : ClassName"]`
- **Children (coupled)** — subroutine rectangle: `varname[["varname : ClassName"]]` (double brackets; indicates a nested subsystem)

## Subgraph wrapping

Always wrap the coupled's children + external ports inside one `subgraph` whose label is the coupled's class name. This is the visual analogue of the DEVS boundary:

```
subgraph MyCoupled[" MyCoupled : Coupled "]
    direction LR
    ...
end
```

The leading/trailing space inside the label quotes matters — `MyCoupled[" ... "]` renders with whitespace padding that makes the title legible.

## Keep labels on one line

`<br/>` works in current Mermaid but is parser-flaky in older versions. If a coupling needs explanation, put it in the README table or the block-diagram Markdown body underneath the fenced block, not inside the label.

## Keep parentheses simple

`|out -> ariv|` is fine. `|out -> (ariv : Job)|` risks the inner parens tripping some parsers — put the type in the Markdown table instead.

## Canonical shape for a coupled

```
flowchart LR
    subgraph MyCoupled[" MyCoupled : Coupled "]
        direction LR
        in_ext((in))
        out_ext((out))
        a["a : ChildA"]
        b["b : ChildB"]

        in_ext --> |in|   a
        a      --> |out -> in| b
        b      --> |out| out_ext
    end
```

## Validate before shipping

Always run `mmdc` over the Mermaid block before writing the final file. The canonical command lives in `resources/scripts/validate-mermaid.sh`; the SKILL.md VALIDATE stage invokes it.
