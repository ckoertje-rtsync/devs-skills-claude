# Pattern — Multi-Aspect Idioms

Multi-aspects let the SES express *"N of the same kind"* without fixing N at design time. The `.pes` picks N during pruning. Three recurring idioms cover almost every use case.

## Idiom 1 — Plain replication (no per-instance distinction)

**When.** You need N replicas and the replicas do not need to address each other by index (e.g. a bank of independent queues).

```
from the multiQueue perspective, Queues are made of more than one Queue!
```

`.pes`:

```
restructure multiaspects using default!
set multiplicity of default as [3] for Queue!
```

Caveat: without a specialization, MS4 Me defaults the restructure label to `default`. Some deployments require an explicit tagging specialization; add `Queue can be id in default!` if the PES rejects the plain form.

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

The restructure expands the single `Processor` declaration into `id_0_Processor`, `id_1_Processor`, … `id_4_Processor`. Couplings use `all Processor` or `each Processor` to refer to the group.

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
set multiplicity of height as [3] for Boy!
set multiplicity of height as [3] for Girl!
```

The `each ... each` coupling with a shared specialization label pairs replicas by tag: `shortish_Boy ↔ shortish_Girl`, `medium_Boy ↔ medium_Girl`, `tall_Boy ↔ tall_Girl`.

## Choosing between `all` and `each`

| Intent | Use | Semantics |
|---|---|---|
| Fan-out to every replica | `... sends M to all {UNIT}!` | One sender, N receivers (N copies of M fire). |
| Fan-in from every replica | `all {UNIT} sends M to ...!` | N senders, one receiver. |
| Full broadcast matrix | `all {X} sends M to all {Y}!` | NxM couplings; MS4 Me drops self-couplings automatically when X = Y. |
| Pairwise match | `each {X} sends M to each {Y}!` | Requires matching specialization labels on both sides; N couplings. |

## Common mistakes

- **Forgetting the tagging specialization.** Without `{UNIT} can be id in index!`, you can still declare a multi-aspect, but pruning has no label to restructure against unless you accept the `default` fallback. Prefer explicit tagging.
- **Setting multiplicity without restructure.** `set multiplicity of index as [5] for Processor!` alone is rejected — you must precede it with `restructure multiaspects using index!`.
- **Using `all` or `each` on a non-multi-aspect endpoint.** `all` / `each` are legal only on entities that are multi-aspect units. Applying them to a plain aspect child is a cross-reference error.
- **Mixing labels in `each`/`each` couplings.** `each Boy sends M to each Girl!` requires both sides to share the specialization label (`height` in the example). Different labels fall back to all-to-all or fail.
