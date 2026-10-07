# SES/MB Open Problems

The limitations of the System Entity Structure / Model Base (SES/MB) methodology: what its originators admitted, what practitioners report, which expressiveness gaps produced extensions, where the evidence is thin, and which research directions look open. Almost all criticism comes from SES developers themselves; no independent critique from outside the DEVS community was found. Current as of 2026-10-06.

Provenance tags: [Z] Zeigler or coauthors, read directly; [T] third-party literature; [G] MS4 Me User's Guide (page); [F] field observation from this repo's practice; [I] inference no source states.

## 1. Limits the originators admitted

| Limit | Quote | Source |
|---|---|---|
| Modularity has a cost | "some work must be put into preparing model descriptions appropriately" | [Z] [Zeigler 1986, WSC, p. 131](https://informs-sim.org/wsc86papers/1986_0019.pdf) |
| The general design environment is far off | "Implementation of such a package, in all its generality, may be a long way off. However, specific parts of it, have already been implemented." | [Z] [Rozenblit & Zeigler 1985, WSC, p. 230](https://informs-sim.org/wsc85papers/1985_0048.pdf) |
| Large model bases lose coherence | "context-sensitive pruning and partitioned entity structure bases ... to promote model base coherence and evolvability" (the remedy implies the problem) | [Z] [Zeigler, Luh & Kim 1990, AIHAS](https://doi.org/10.1109/aihas.1990.93938) |
| Strict hierarchy blocks recursion | "we must relax the strict hierarchy SES axiom as in Cho (1993) in order to facilitate such a process" ("Cho (1993)" is UNVERIFIED) | [Z] [Rozenblit & Zeigler 1993, WSC, p. 607](https://informs-sim.org/wsc93papers/1993_0084.pdf) |
| Nested transforms needed isolation | Transforming nested PES files required a separate Scheme environment "so as not to interfere with the parent environment" | [Z] [Rozenblit, Kim & Zeigler 1988, WSC, p. 228](https://informs-sim.org/wsc88papers/1988_0033.pdf) |
| Adoption | The 2013 suites paper closes on "future research and developments needed to bring the marketplace into common use" | [Z] [Zeigler, Seo & D. Kim 2013, IJMSSC](https://doi.org/10.1142/S1793962313400060) |

The MS4 Me User's Guide records tool-level limits [G]:

- Multi-aspects exist "to address the limitation of having to explicitly declare decompositions having different numbers of entities" (p. 120).
- Without `inherit from`, "the selected models have empty behaviors" (p. 27).
- "Couplings are not added for matches that cannot be made" in `each`/`each` couplings (p. 124).
- Nested multi-aspects get the same count in every parent copy, "a limitation on the expressiveness of the current version of MS4 Me" (p. 127).
- Unmade choices in a partial PES "will be randomly selected" (p. 168), and no text directive for choosing among several aspects is documented.

No originator retrospective that assesses these limits with evidence was found [Z]. Such material, if it exists, is likely in the paywalled 1991 TOMACS paper, the Guide chapters, or the TMS editions.

## 2. Practitioner complaints

| Complaint | Quote | Source |
|---|---|---|
| Automatic pruning of nested structures | "Especially for some types of nodes ... automatic pruning is a big challenge ... pruning SES containing hierarchies of multi-aspect and specialization nodes" | [T] [SpringSim Mod4Sim 2020](https://doi.org/10.22360/springsim.2020.mod4sim.001) (author list UNVERIFIED; see `source-notes.md` §3) |
| Variant explosion | "The number of scenarios generated can extend to a large number, as the variability factors are many. It requires a mechanism to organize scenarios by labelling and prioritizing them"; scenario metrics still assigned "manually" | [T] [Naja, Gupta, Durak & Hartmann 2024, SNE 34(1)](https://doi.org/10.11128/sne.34.tn.10672) |
| Tool scalability | "it is currently not scalable and requires several enhancements. Using two tools and managing the pruned models was a critical problem" | [T] Naja et al. 2024 |
| No standard representation | "there still exists a lack of standardized computational representation. This hinders the shareability of SES artifacts and interoperability of SES tools" | [Z] [Karmokar, Durak, Hartmann & Zeigler 2019, DLR elib](https://elib.dlr.de/130764) (venue details UNVERIFIED) |
| Structure without behavior | A model synthesized from a PES "doesn't contain the behavior specifications" | [Z] [Risco-Martín, de la Cruz, Mittal & Zeigler 2009, eUDEVS, SIMULATION 85(11–12)](https://doi.org/10.1177/0037549709104727) |
| Manual effort for coverage | "A significant limitation was the manual effort required to model scenarios to achieve high coverage levels" | [T] [Gupta et al. 2026, AIAA 2026-0208](https://doi.org/10.2514/6.2026-0208) |
| Distributed federations | "SES/MB is limited in simulating the system of systems using HLA/RTI" | [T] [B.S. Kim, Choi & T.G. Kim 2013](https://doi.org/10.5555/2499986.2499990) |
| Learning curve | SES "follows more rigid guidelines and restrictions"; "an understanding of SES is required to comprehend and edit these files" (but students found eSES/MB "a lot easier to get started with" than Simulink Variant Manager) | [T] [Jafer et al. 2018, JSimE](https://doi.org/10.5281/zenodo.11114155); [Deatcu et al. 2019, SNE 29(1)](https://doi.org/10.11128/sne.29.sw.10466) |
| Tool completeness | As the MATLAB toolbox matured, "the necessity to have some generalized examples for testing and verification came more and more into awareness"; design patterns proposed as a reference test suite | [T] [Deatcu, Folkerts, Pawletta & Durak 2019, SNE 29(3)](https://doi.org/10.11128/sne.29.tn.10482) |
| Dormant, fragile tools | MATLAB toolbox README: "Siblings of multi-aspect nodes and siblings of aspect and multi-aspect nodes on the second layer of the SES cannot be pruned"; SESToPy README lists "selections constraints in pruning ignored"; no pushes after 2021–2022 | [T] [SES_Tbx_Matlab](https://github.com/cea-wismar/SES_Tbx_Matlab); [SESToPy](https://github.com/hendrikfolkerts/sestopy) |

In this repo [F]: MS4 Me has intermittently failed on collective entity names ending in a trailing "s" (`UAVs`), although the guide's own examples use such names; `ses-designer` therefore defaults to `UAVGroup`-style names. See `lessons.md`.

## 3. Expressiveness gaps and the extensions they produced

| Gap in the classic SES | Extension | Source |
|---|---|---|
| Variable number of identical components | Multiple aspect / multi-aspect with a multiplicity | [Z] [Zeigler 1986, WSC, p. 136](https://informs-sim.org/wsc86papers/1986_0019.pdf); [G] pp. 120–121 |
| Recursive structures | Relaxed strict hierarchy | [Z] Rozenblit & Zeigler 1993, p. 607 |
| Model bases too large and incoherent | Context-sensitive pruning, partitioned entity structure bases | [Z] Zeigler, Luh & Kim 1990 |
| Data in relational stores | RASES, a relational-algebraic SES | [T] [Park, Lee & Kim 1994, WSC](https://doi.org/10.1109/wsc.1994.717403) |
| Families of model suites | SES merging | [Z] Zeigler, Seo & D. Kim 2013 |
| Levels of abstraction, time granularity | Abstraction-hierarchy extension in DEVSimPy | [Z] [Santucci, Capocchi & Zeigler 2016](https://doi.org/10.1177/0037549716657168) |
| Automated, parameter-driven pruning; computed couplings | eSES: SES variables, semantic conditions, selection rules, SESFunctions, NONE element, numRep | [T] [Pawletta et al. 2018, SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402); [Folkerts et al. 2017, SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391) |
| Reactive experimentation loops | Experiment control and execution unit around SES/MB | [T] Pawletta et al. 2018 |
| HLA/RTI federations | SoSES/FB | [T] B.S. Kim et al. 2013 |
| Generating for several simulators | FMI-based infrastructure; template extension | [T] [Folkerts, Pawletta & Deatcu 2021, SNE 31(1)](https://doi.org/10.11128/sne.31.tn.10554); [Martens et al. 2019, IIPhDW](https://doi.org/10.1109/iiphdw.2019.8755406) |
| Class-based systems of systems with many components | MetaSES | [Z] [Zeigler 2023, Information 14(1):22](https://doi.org/10.3390/info14010022) |
| Coupling to MBSE | SES-X with the X language | [T] [Xie et al. 2023](https://doi.org/10.3390/info14010023) |

Each extension was built tool by tool, which is one reason the tools now disagree on couplings, multiplicity, constraints, and file format [I] (see `tools.md`).

## 4. Evidence gaps

What the literature lacks matters as much as what it states [T]:

- **No independent critique.** Every limitation above comes from SES developers or their close collaborators.
- **No scalability benchmarks.** Nobody has published how the number of PESs grows with SES size, or the cost of checking selection constraints. Practitioners handle large structure spaces with metaheuristics (evolutionary and genetic algorithms) rather than by analysis ([Hagendorf et al. 2013](https://doi.org/10.1177/0037549713500066)).
- **No systematic validation** of SES-generated models against their referents, beyond single-case demonstrations. SES-generated experimental frames serve fidelity evaluation ([Schmidt, Durak & Pawletta 2016](https://doi.org/10.1177/0037549716656791)), and SES-X uses a pruning level "for model verification", but neither is a validation study. The EFSUT draft's "SES pruning ensures minimality" has no proof or evaluation.
- **No systematic literature review** and no SES anniversary retrospective.
- **One quantified benefit**: up to 8% time and 23% cost savings in WSN model construction ([Nam & H.J. Kim 2021](https://doi.org/10.3390/s21020430)).
- **Thin coverage of non-English literature.** Korean- and Chinese-language SES work was only partly reachable.
- **Unread core sources.** The 1984, 1990, and 2007 books, the Guide chapters, and the TMS editions were not read in full; see `source-notes.md` §6.

## 5. Research opportunities

All items here are inferences from the gaps above; no source proposes them in this form [I].

1. **Constrained pruning as SAT/CSP.** Encode specialization choices, multiplicities, selection rules, and semantic conditions as a constraint problem. This would give completeness, enumeration, and counting of admissible PESs, and would make scalability measurable. Zeigler's 2026 SAT/SMT synthesis of DEVS from partial "model seeds" ([Alshareef & Zeigler, Logics 4(3):7](https://doi.org/10.3390/logics4030007)) sits next to this without touching the SES.
2. **Coverage of the pruning space.** Generalize the ODME scenario-coverage metric ([Gupta, Sprockhoff & Durak 2025](https://doi.org/10.2514/6.2025-2326)) from ODD scenarios to any SES: what fraction of admissible PESs, or of pairwise choice combinations, a study has exercised.
3. **A standard representation that settles semantics first.** The tools split on whether couplings name ports or messages, whether couplings are static or computed, where multiplicity lives, and whether pruning may run code. A schema alone will not fix that; the coupling and multiplicity semantics need agreement first.
4. **LLM proposes, validator enforces.** An LLM drafts an SES from a description; axiom and coupling-scope validation and constrained pruning enforce correctness. This mirrors the Logics 2026 architecture, and this repo's `ses-designer` skill is an unpublished instance of it.
5. **SES-driven EFSUT pairing.** Make the EFSUT draft's idea testable: an SES with EF and Model aspects whose pruning yields EF/model pairings, with applicability and minimality checked formally rather than asserted.
6. **Measured benefits.** Controlled comparisons of SES-based generation against hand-built models or feature-model and SysML variant approaches, reporting effort, defects, and coverage.
