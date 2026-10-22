# Doxygen .dox cheat sheet for DEVS specification pages

Everything the templates use, the rules that keep Doxygen warning-free, and
the fixes for the warnings you will actually see. Verified against Doxygen
1.13 with Graphviz installed.

## 1. File anatomy

A `.dox` file holds one or more `/** ... */` comment blocks and nothing else.
Each block documents exactly one page:

```
/**
\page devs_jobprocessor JobProcessor DEVS atomic model
...
*/
```

- `\mainpage Title` makes the block the landing page (`html/index.html`).
- `\page id Title` makes a related page rendered as `html/<id>.html`.
- Never put two `\page` commands in one block; never put code outside a block.
- A `.dox` file is picked up by the default `FILE_PATTERNS`; the templates
  list `*.dox` explicitly anyway.

## 2. Commands the templates rely on

| Command | Purpose |
|---|---|
| `\mainpage`, `\page id title` | page header (see above) |
| `\tableofcontents` | in-page TOC from `\section`/`\subsection` |
| `\section label Title`, `\subsection label Title` | headings; labels must be unique project-wide |
| `\subpage id` | link **and** parent/child nesting in the tree view |
| `\ref target "text"` | link to a page, section, class, member or namespace |
| `\dot ... \enddot` | inline Graphviz graph (needs `HAVE_DOT = YES`) |
| `\msc ... \endmsc` | inline message sequence chart (mscgen is built into Doxygen, no extra tool) |
| `\code{.py} ... \endcode` | syntax-highlighted code block |
| `\note`, `\warning`, `\see` | callouts |
| Markdown pipe tables, lists, `` `code` `` | body content |

## 3. Ids, labels and slugs

- `slug` = model class name lower-cased, every non-alphanumeric character
  replaced by `_` (`JobProcessor` -> `jobprocessor`, `Gen-Proc` -> `gen_proc`).
- Page id: `devs_<slug>`. Section labels: `<slug>_<topic>`. The index page
  uses the `index_` prefix. Labels are global: two models with the same class
  name in different packages need distinct slugs (append the package).
- Allowed characters in ids and labels: letters, digits, `_`, `-`.

## 4. `\ref` targets per language (verified)

| Language | Class | Member | Namespace / module |
|---|---|---|---|
| Java | `\ref JobProcessor` (or `pkg.JobProcessor` when packaged) | `\ref JobProcessor::internalTransition` | n/a |
| C++ | `\ref JobProcessor`, `\ref ns::JobProcessor` | `\ref JobProcessor::delta_int` | `\ref ns` |
| Python | `\ref gen_proc.JobProcessor` (module = file stem, **required**) | `\ref gen_proc.JobProcessor.intTransition`, `\ref gen_proc.JobProcessor.inport` | `\ref gen_proc` |
| Julia (filtered) | `\ref JobProcessor.JobProcessor` (namespace = file stem) | `\ref JobProcessor.delta_int!` (functions are namespace members; `!` is fine) | `\ref JobProcessor` |

Add display text to keep tables readable: `\ref gen_proc.JobProcessor.intTransition "JobProcessor.intTransition()"`.
Only `\ref` things that are in `INPUT`; framework base classes are not, so
write them as plain `` `AtomicDEVS` ``.

## 5. `\subpage` has one parent

A page may be a `\subpage` of exactly one other page, otherwise Doxygen warns
and the tree view breaks. Rule used by the templates:

- The index page `\subpage`s the root model(s): every model not contained by
  another model in the bundle.
- A coupled page `\subpage`s its components, but only the first time a model
  appears; a model reused in several coupled models is `\subpage` of the first
  and `\ref` everywhere else.
- Everything else links with `\ref devs_<slug>`.
- A single-model bundle has no index page: that model's page is the `\mainpage`.

## 6. Tables

- Blank line before and after every table.
- Header row, then the `|---|` row with the same number of cells.
- A cell may hold `` `code` ``, `\ref ...`, `\subpage ...`.
- An empty `{{#each}}` block must still leave a valid table: emit one row
  such as `| (none) | | |` rather than no rows.
- No `|` inside a cell (write "or" instead).

## 7. Diagrams

Doxygen does not render Mermaid. The templates use Graphviz (`\dot`) for state
and coupling diagrams and mscgen (`\msc`) for the interaction sketch because
both render offline inside Doxygen and are checked by `run-doxygen.sh`.

### `\dot` rules

- Node ids: `[A-Za-z_][A-Za-z0-9_]*`; otherwise double-quote the id. Phase
  names such as `:busy` or `BUSY` become ids `busy`; keep the original text in
  the `label`.
- Always quote labels. `\n` inside a label is a line break and is safe inside
  the comment block.
- ASCII only in labels: `sigma`, `inf`, `->`, not Greek letters or arrows.
- Edge label grammar: `int / action` for internal, `ext: port / action` for
  external (dashed), `port -> port` for couplings. Keep each label under ~40
  characters; move detail into the tables.
- Do not use HTML-like labels (`<...>`); the comment parser interferes.
- Every `\dot` needs its `\enddot`; graphs are counted by the validator.
- Passive phases (ta = inf) are plain ellipses labelled `(ta = inf)`.

### `\msc` rules

- Participants on one line, comma-separated, terminated by `;`:
  `env [label="environment"], gen [label="gen : Generator"];`
- Arrows: `a->b` message, `a=>b` call, `a>>b` return, `a->a` self-message.
  Each arrow line ends with `;`.
- `hscale = "1.6";` widens the chart; labels follow the dot ASCII rule.

## 8. Text pitfalls inside `/** */`

| Pitfall | Why | Do this |
|---|---|---|
| `*/` anywhere in the text (e.g. a glob `src/*/`) | ends the comment block | write `src/ * /` or describe in words |
| A word starting with `\` or `@` | parsed as a command | `\\` or `\@` (e.g. `\@Override`) |
| `<Type>` outside code spans | treated as HTML | wrap in backticks: `` `Map<String, Job>` `` |
| A word identical to a class name | auto-linked | prefix with `%` to suppress: `%JobProcessor` |
| `\f$ ... \f$` formulas | need LaTeX or MathJax | use plain text / indented code for the tuple |
| Line starting with `#` | Markdown heading | indent it or use `\#` |
| `-` or `1.` at line start after a table | starts a list | leave a blank line |
| Greek letters | fine in prose, bad in diagrams | prose only; diagrams ASCII |

## 9. DEVS element -> page section

| DEVS element | Atomic page section | Coupled page section |
|---|---|---|
| Overview, source, parameters | `<slug>_overview` | `<slug>_overview` |
| Tuple | `<slug>_formal` | `<slug>_formal` |
| X, Y | `<slug>_ports` | `<slug>_ports` (external ports) |
| S | `<slug>_state` (phases + variables) | n/a |
| ta | `<slug>_ta` | n/a |
| delta_int / delta_ext / delta_con | `<slug>_dint`, `<slug>_dext`, `<slug>_dcon` | n/a |
| lambda | `<slug>_lambda` | n/a |
| D, M_d | n/a | `<slug>_components` |
| EIC, IC, EOC | n/a | `<slug>_couplings` (`_eic`, `_ic`, `_eoc`) |
| Diagram | `<slug>_diagram` (state graph) | `<slug>_diagram` (coupling graph) + `<slug>_sequence` |
| Mapping to code | `<slug>_source` | `<slug>_source` |

## 10. Doxyfile settings per language

Fill `{{LANGUAGE_SETTINGS}}` and `{{FILE_PATTERNS}}` in `templates/Doxyfile.tmpl`:

| Language | `FILE_PATTERNS` | `LANGUAGE_SETTINGS` |
|---|---|---|
| Java | `*.java *.dox` | `OPTIMIZE_OUTPUT_JAVA = YES` |
| C++ | `*.h *.hpp *.hh *.cpp *.cc *.cxx *.dox` | `BUILTIN_STL_SUPPORT = YES` |
| Python | `*.py *.dox` | `OPTIMIZE_OUTPUT_JAVA = YES` and `PYTHON_DOCSTRING = YES` |
| Julia | `*.jl *.dox` | `OPTIMIZE_OUTPUT_JAVA = YES`, `EXTENSION_MAPPING = jl=Python`, `FILTER_PATTERNS = *.jl="python3 ./julia-doxygen-filter.py"` (copy the filter next to the Doxyfile) |

Mixed-language bundles take the union of patterns and settings.
`INPUT = . <source paths relative to the Doxyfile>`: the `.` picks up the
`.dox` files; `RECURSIVE = YES` only when the source directory has subdirectories to include.
`HAVE_DOT` is `YES` when `command -v dot` succeeds, else `NO` (graphs then show
as their dot source and the validator skips the graph count).
`FULL_PATH_NAMES = NO` and `DIRECTORY_GRAPH = NO` keep directory pages out of
the output.

## 11. Warnings and their one-pass fixes

| Warning (from `doxygen-warnings.log`) | Fix |
|---|---|
| `unable to resolve reference to 'X' for \ref command` | qualify per section 4 (Python/Julia need the module prefix); if the target is not in INPUT, replace `\ref X` with `` `X` `` |
| `multiple use of section label 'x'` | two models share a slug; append a package/file suffix to one slug |
| `page X is already a subpage of Y` or `page X is a subpage of itself` | apply the single-parent rule (section 5): change the extra `\subpage` to `\ref` |
| `Problems running dot` / `syntax error in line N` from dot | unquoted label, bad id, missing `;`, stray `>` in a label. Quote it, sanitise the id |
| `Illegal command \foo` | an unescaped `\` or `@` in prose; escape it |
| `end of comment block while expecting command \enddot` | the graph lost its `\enddot`, or a `*/` sequence inside the block |
| `found </th> tag` / stray table text | table has unequal cell counts or no blank line before it |
| `Member X (function) ... is not documented` | informational from the user's sources (`WARN_IF_UNDOCUMENTED = NO` should silence it); never edit the user's source to fix it |
| `warning: Tag 'X' at line N of file 'Doxyfile' has become obsolete` | remove that line from the Doxyfile; the template only uses current tags |
