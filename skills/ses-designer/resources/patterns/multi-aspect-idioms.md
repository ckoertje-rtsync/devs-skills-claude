# Pattern — Multi-Aspect Idioms

Multi-aspects let the SES express *"N of the same kind"* without fixing N at design time. The `.pes` picks N during pruning. Three recurring idioms cover almost every use case.

## Naming the collective parent — avoid trailing-`s` plurals

A multi-aspect has two entities: the **collective parent** (host of the `... is made of more than one ...` statement) and the **replicated unit**. The MS4 Me User's Guide names the parent as the plural of the unit (`Processors` → `Processor`, `Queues` → `Queue`), and the examples below follow the guide verbatim. **However, MS4 Me's natural-language parser intermittently mis-handles a trailing-`s` plural** — it tries to singularize the name and can reject the declaration or collide it with the unit.

**Default to an explicit collective suffix instead of pluralizing:** `UAVGroup`, `ProcessorPool`, `QueueBank`, `TargetSet`, `SensorArray`. The replicated unit keeps the singular name, so the multi-aspect reads cleanly and `is_or_are` resolves to `is`:

```
from the fleet perspective, UAVGroup is made of more than one UAV!
```

Reserve the plural-of-unit form (`Processors are made of more than one Processor!`) for cases that must match an existing guide-style model. When in doubt during DISCOVER, propose the `...Group`/`...Pool`/`...Set` name.

## Idiom 1 — Plain replication (no per-instance distinction)

**When.** You need N replicas and nothing addresses an individual replica (e.g. a bank of independent queues). Couplings use only `all`.

```
from the multiQueue perspective, Queues are made of more than one Queue!
Queue can be id in index!
```

`.pes`:

```
restructure multiaspects using index!
set multiplicity of index as [3] for Queue!
```

The tagging specialization is still required: the guide makes copies by adding a specialization to the multi-entity and restructuring on its label (p. 121). The guide documents no restructure without one.

## Idiom 2 — Tagged replication (index-based matching)

**When.** You need the replicas to be nameable (e.g. for logging) or wired pairwise against another multi-aspect.

```
from the multiProcessor perspective, Processors are made of more than one Processor!
Processor can be id in index!
```

`.pes`:

```
restructure multiaspects using index!
set multiplicity of index as [5] for Processor!
```

The restructure expands the single `Processor` declaration into `id0_Processor`, `id1_Processor`, … `id4_Processor` (guide p. 121). Couplings use `all Processor` or `each Processor` to refer to the group, or a copy name such as `id0_Processor` to refer to one copy (p. 123). To pick a specialization for one copy, select on the copy: `select medium from height for id1_Person!` (p. 122).

## Idiom 3 — Labeled replication with matching pairs

**When.** Two multi-aspects must be paired one-to-one (e.g. `Boy` replicas dating `Girl` replicas of the same height).

```
from the dating perspective, DatingGame is made of Boys and Girls!
from the boy perspective, Boys are made of more than one Boy!
from the girl perspective, Girls are made of more than one Girl!
Boy can be shortish, medium or tall in height!
Girl can be shortish, medium or tall in height!
from the dating perspective, each Boy sends Invitation to each Girl!
from the dating perspective, each Girl sends NiceToMeetYou to each Boy!
```

`.pes`:

```
restructure multiaspects using height!
```

The guide restructures this example on `height` and warns that it works only "provided you don't use height as the basis for identifying multi-entity copies (in other words, 'set multiplicity of height …' does not appear in the pruning file)" (p. 124). So there is no `set multiplicity of height` line. The guide does not print `DatingGame.pes`; the single line above follows that description.

The `each ... each` coupling pairs copies by the prefix of their pruned names (p. 124): `shortish_Boy ↔ shortish_Girl`, `medium_Boy ↔ medium_Girl`, `tall_Boy ↔ tall_Girl`.

## Choosing between `all` and `each`

| Intent | Use | Semantics |
|---|---|---|
| Fan-out to every replica | `... sends M to all {UNIT}!` | One sender, N receivers (N copies of M fire). |
| Fan-in from every replica | `all {UNIT} sends M to ...!` | N senders, one receiver. |
| Full broadcast matrix | `all {X} sends M to all {Y}!` | NxM couplings; MS4 Me drops self-couplings automatically when X = Y. |
| Pairwise match | `each {X} sends M to each {Y}!` | Matches copies by pruned-name prefix; copies with no match get no coupling (p. 124). |

## Common mistakes

- **Naming the collective parent with a trailing-`s` plural.** `UAVs are made of more than one UAV!` can trip MS4 Me's singularizer. Prefer an explicit collective suffix (`UAVGroup`, `ProcessorPool`, `TargetSet`) — see "Naming the collective parent" above.
- **Forgetting the tagging specialization.** Without a specialization such as `{UNIT} can be id in index!`, pruning has no label to restructure against. The guide makes copies only through a specialization on the unit (p. 121).
- **Setting multiplicity without restructure.** `set multiplicity of index as [5] for Processor!` alone is rejected — you must precede it with `restructure multiaspects using index!`.
- **Using `all` or `each` on a non-multi-aspect endpoint.** `all` / `each` are legal only on entities that are multi-aspect units. Applying them to a plain aspect child is a cross-reference error.
- **Mixing labels in `each`/`each` couplings.** `each Boy sends M to each Girl!` pairs copies whose pruned names share a prefix (`tall_Boy` with `tall_Girl`). If the prefixes or the counts differ, the unmatched copies get no coupling (p. 124); MS4 Me does not fall back to all-to-all.
- **Setting the multiplicity of the matching label.** Restructure on `height` to match by height, but do not write `set multiplicity of height ...` (p. 124).
- **Selecting on the unit instead of a copy.** After restructuring, write `select {OPT} from {LABEL} for id1_{UNIT}!`, not `... for {UNIT}!` (p. 122).
