Helpers the renderer of `atomic-skeleton.dnl.tmpl` must provide:

- `join_and` — `"a, b and c"` for 3+ fields, `"a and b"` for 2, `"a"` for 1.
- `head` — the tag head for a tag entry:
  - `initialize` → `initialize variables`
  - `internal` → `internal event for {state}`
  - `output` → `output event for {state}`
  - `external` → `external event for {state} with {port}`
  - `external-any` → `external event with {port}`
  - `library` → `add library`
  - `additional` → `add additional code`
- `time` is written as given: a number literal (`10`, `0.5`, `0`) or a quoted expression (`"serviceTime"`), quotes included.

Drop a whole section (and its comment line) when its list is empty.
