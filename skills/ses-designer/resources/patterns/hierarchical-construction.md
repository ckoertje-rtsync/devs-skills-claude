# Pattern — Hierarchical SES Construction (Root-First)

Building an SES bottom-up (enumerating atomics first) almost always produces a flat structure where a single aspect lists dozens of children. Building top-down produces a strict tree that respects the alternating-mode axiom and surfaces natural decomposition seams.

## The recipe

1. **Pick a root system.** It is almost always the name of the system-of-interest (the thing you are modeling). One root per SES file.
2. **Enumerate top-level perspectives.** Ask: *"What are the 1–3 distinct viewpoints from which this system decomposes?"* Typical perspectives: `structural`, `functional`, `behavioral`, `top`, `Dialog`, `logistics`. Perspectives name aspects; each aspect decomposes the root.
3. **For each perspective, list its direct children.** Children are entities, not further aspects.
4. **For each child, decide one of four next moves:**
   - **Atomic leaf** — the entity is indivisible at this level. Stop recursing.
   - **Decompose** — the child has its own perspective(s); recurse from step 2 with the child as the new root.
   - **Replicate** — the child represents a group of identical units → declare a *multi-aspect* (`from the {SUBASPECT} perspective, {CHILD}s are made of more than one {UNIT}!`). Usually pair with a tagging specialization (`{UNIT} can be id in index!`) so replicas can be addressed in couplings.
   - **Specialize** — the child has mutually exclusive variant implementations → declare a specialization (`{CHILD} can be A, B or C in {LABEL}!`). Each variant is itself an entity that may further decompose.
5. **Wire couplings only after the tree is complete.** Per the scope rule (see `coupling-scope.md`), couplings are constrained by the hierarchy you just built — sender and receiver must be legal siblings or a parent/child pair in a shared aspect.
6. **Attach variables last.** Variables are parameters of an entity, not structural decomposition; they never change the tree.

## Why root-first

- The alternating-mode axiom requires aspects and specializations to alternate. Asking *"decompose or specialize?"* at each entity forces a legal alternation; asking *"list all your atomics"* at the top sets you up for axiom violations later.
- A strict tree makes `.pes` pruning tractable: every multi-aspect and every specialization becomes an explicit choice point, and the user knows how many choices they need to make before running `write class files!`.
- Aspects are the durable hierarchical seams. Changing the number of `Processor` replicas in a `.pes` should not require rewriting the `.ses`; the skill's prompting order makes this split obvious.

## Anti-patterns the skill avoids

- **Flattening.** Everything lives under one aspect of the root. Specializations and multi-aspects sit at the root too. This parses but throws away the benefits of SES.
- **Hidden atomics.** Declaring a coupling endpoint that was never named in any aspect, specialization, or multi-aspect. The validator flags this as an undeclared entity.
- **Aspect stuffing.** Using one aspect to mean multiple perspectives ("topAndFunctional"). Split into two aspects, each with its own children list; they may overlap in membership.
- **Premature specialization.** Treating every entity with configurable attributes as a specialization. If the difference is a parameter value, use a variable + range; save specialization for behaviorally distinct implementations.
- **Implicit multiplicity.** Listing identical children by hand (`is made of Processor1, Processor2, Processor3 and Processor4!`). Use a multi-aspect so pruning can vary the count.

## The three reference tree shapes

**Flat system (no decomposition beyond the root):**

```
root
  └─ aspect: top
       ├─ entityA (atomic)
       ├─ entityB (atomic)
       └─ entityC (atomic)
```

Applicable to: small demos (HelloWorld), quick experiments.

**Hierarchical system with nested aspects:**

```
root
  └─ aspect: top
       ├─ subsystemA
       │    └─ aspect: internal
       │         ├─ atomicA1
       │         └─ atomicA2
       └─ subsystemB
            └─ aspect: internal
                 ├─ atomicB1
                 └─ atomicB2
```

Applicable to: any system with a natural physical or functional nesting.

**Parameterized system with replicas and variants:**

```
root
  └─ aspect: top
       ├─ generator (atomic)
       ├─ processorsGroup
       │    └─ multi-aspect: replicated
       │         └─ processor (atomic)
       │              └─ specialization: index
       │                   └─ id
       └─ variantConsumer
            └─ specialization: consumerKind
                 ├─ LossyConsumer
                 └─ StrictConsumer
```

Applicable to: scalable simulations (SimpleWorkFlow, DatingGame-style matching).
