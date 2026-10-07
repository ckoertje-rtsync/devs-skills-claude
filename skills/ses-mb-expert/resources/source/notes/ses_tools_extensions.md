# SES/MB Software Tools, Representations, and Third-Party Extensions

> **Source copy.** Research notes produced on 2026-10-06 for the SES/MB deep-research report (repo path `research_notes/SES and model base methodology/`), copied into the skill for verbatim lookup and lightly edited to remove personnel details. They are working notes: quotes and page locators are the useful part, and the curated files under `../../reference/` take precedence. References to "the report writer" or "other researchers" are from the original research task.

Scope: how Zeigler's System Entity Structure / Model Base (SES/MB) has been implemented and extended in software (late 1990s to 2026-10-06). Formal axioms, the core prune-and-synthesize workflow, and application case studies are covered by other researchers and are only touched on where a tool changes them.

Conventions used below:
- "[V: Crossref]", "[V: publisher page]", "[V: PDF title page]", "[V: repo]" = how the bibliographic record was verified in this session.
- "UNVERIFIED" = the record or claim could not be checked against a primary record in this session.
- Local MS4 Me guide = `MS4 Me User's Guide`, MS4 Systems, Inc. (the cover gives no date or version; PDF metadata creation date is 2021-10-19). Page numbers are the guide's printed page numbers.

---

## 1. MS4 Me (MS4 Systems / RTSync): natural-language SES, PES/pruning language, couplings, SES-to-DEVS generation, naming constraints

### Takeaway
MS4 Me puts SES and PES into a controlled English language. Each statement ends with `!`. Aspects are written as "From the X perspective, Y is made of ...", multi-aspects as "... made of more than one ...", and specializations as "... can be A or B in LABEL". Couplings are verb phrases ("A sends M to B [as N]") with `all`/`each` quantifiers over replicas. Pruning goes into a separate `.pes` file of imperative directives, and `write class files!` generates Java DEVS classes. The tool adds conventions that are not in Zeigler's formalism. A multi-aspect is expanded through an "index" specialization plus PES `restructure`/`set multiplicity` statements, and the expansion flattens the multi-aspect away. Port names are derived from message names (`outX`/`inX`). Choices left unresolved are filled in at random. Behavior from a selected specialization needs an explicit `inherit from X!`. Several naming rules also apply.

### Cited Findings

#### SES language constructs (guide Appendix 2, "System Entity Structure (SES) Language Specification")
- The guide lists the elements of an SES as aspects, specializations, couplings, "Similarities" (e.g., "a truck is like a car"), and variables with optional ranges. Appendix 2 gives syntax sections for every element **except** Similarities — [MS4 Me User's Guide, App. 2 §A, p. 173](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Identifier rule: a single word that starts with a letter or `_` and contains letters, digits, dashes, or underscores. `3strikes`, `Initial State`, and `Initial.State` are invalid. The guide notes: "Unlike the Atomic DEVS language, it is permissible to use dashes in an identifier." — [MS4 Me guide, App. 2 §B, pp. 173–174](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Readability particles: "a", "an", and "the" are accepted before names, and "is" can be replaced with "are" or "is (are)" — [MS4 Me guide, App. 2 §C, p. 174](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Aspect syntax: `from the ASPECT perspective, SYSTEM is made of SUBSYSTEM_1, SUBSYSTEM_2 and SUBSYSTEM_3 !`. A single-subsystem form (`... is made of SUBSYSTEM !`) is also defined. The rule says no comma before "and" when listing more than 2 subsystems, yet the same page shows "an engine, the transmission, and a chassis" as an acceptable variant — [MS4 Me guide, App. 2 §D, pp. 174–175](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Multi-aspect syntax: `from the ASPECT perspective, SYSTEM is made of more than one SUBSYSTEM !` (e.g., "a book is made of more than one page") — [MS4 Me guide, App. 2 §E, p. 175](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Specialization syntax: `SYSTEM can be SUB_1, SUB_2 or SUB_3 in SPECIALIZATION!` (e.g., `Transmission can be Automatic or Manual in gearShift !`). The specialization label names "the part of the system that can be performed by the subsystems" — [MS4 Me guide, App. 2 §F, pp. 175–176](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Coupling syntax: `from the ASPECT perspective, SENDING_SYSTEM sends MESSAGE to RECEIVING_SYSTEM !`, with an optional rename: `... sends SENT_MESSAGE to RECEIVING_SYSTEM as RECEIVED_MESSAGE !`. Scope rule: "Messages can only be passed between a subsystem and its parent system or subsystems within the same aspect ... The only exception ... the subsystem of a multi-aspect can send messages to siblings of its parent system." — [MS4 Me guide, App. 2 §§G–H, p. 176](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Multi-aspect couplings: `each MULTIASPECT_SYSTEM sends MESSAGE to RECEIVING_SYSTEM !`, `SYSTEM sends MESSAGE to each MULTIASPECT_SYSTEM !`, and `each X sends M to each Y [as N]!`. "The word 'all' can also be used for readability." — [MS4 Me guide, App. 2 §I, pp. 176–177](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Variables and ranges: `SYSTEM has VAR_1, VAR_2 and VAR_3 !` and `the range of SYSTEM's VARIABLE is TYPE [with values V1, V2 and V3 | [-6.3,6] | (-6.3,6)] !`. Brackets mark inclusive bounds and parentheses mark exclusive bounds — [MS4 Me guide, App. 2 §§J–K, pp. 177–178](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Comments: the guide's own SES examples use `//` comment lines (e.g., `//sharing of updates among enclaves`) — [MS4 Me guide, §II.I, pp. 125–126](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Minimal SES from the guide's Getting Started section: `from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!` / `... HelloWorld sends Hello to BackAtYou!` / `... BackAtYou sends Hi to HelloWorld!` — [MS4 Me guide, pp. 16–17](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### Multi-aspect restructuring, pruning, and uniform coupling (guide §II.I "Multi-Aspect With Specialization")
- The guide credits the multi-aspect concept to Zeigler & Hammonds (2007): "a multi-aspect is a special case of an aspect except that it has only one child called its multi-entity – which can generate a specified number of copies" — [MS4 Me guide, pp. 120–121 and fn. 11](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- To expand a multi-entity, "you add a specialization to it" (`Person can be id in index!`) and then put `restructure multiaspects using index!` and `set multiplicity of index as [3] for Person!` in the pruning file. This produces `id0_Person`, `id1_Person`, `id2_Person`. "These new entities are added to the (ordinary) aspect under the grand-parent entity and the multi-aspect is removed from the SES" — [MS4 Me guide, p. 121](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Each copy inherits the multi-entity's sub-SES and can be pruned independently (`select medium from height for id1_Person!`). "As general is the case, selections not made explicitly in the pruning file are made randomly." — [MS4 Me guide, p. 122](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Port derivation: `From the top perspective, id0_Person sends Bye to id1_Person!` "creates a coupling from outBye of id0_Person to inBye of id1_Person". `all Person sends Hello to all Person!` couples every replica to every replica "(the exception that there is no self-coupling ...)" — [MS4 Me guide, pp. 122–123](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- `each Boy sends Invitation to each Girl!` matches replicas "on the basis of having the same prefix in the pruned name of an entity (e.g., tall in tall_boy). Couplings are not added for matches that cannot be made". The guide also warns that the DatingGame example works "provided you don't use height as the basis for identifying multi-entity copies (in other words, 'set multiplicity of height …' does not appear in the pruning file)" — [MS4 Me guide, pp. 123–124](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- One-to-all and all-to-one forms (`MatchMaker sends MakeInvitation to all Boy!`, `all Boy sends Preference to MatchMaker!`) work "provided that the entity to be coupled is in the same coupled model as the multi-entities". The containing coupled model may also be the source — [MS4 Me guide, pp. 124–125](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Hierarchical multi-aspects (MultiEnclaveNet: Enclaves → Enclave → Clients → Client) can reuse one `index` specialization with separate multiplicities. If the index names differ, "a bottom-up order must be given": `restructure multiaspects using clientIndex then enclaveIndex!`. Stated limitation: "the number of clients in each enclave is the same since we can't specify them individually. This is a limitation on the expressiveness of the current version of MS4 Me which can be removed by supporting specification of multiplicity using context." — [MS4 Me guide, pp. 125–127](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### PES / pruning language and model generation
- PES directives shown in the guide include:
  - `set name of model as X !`, `pruned from X.ses !`, `restructure multiaspects using index !`, `set multiplicity of index as [5] for Processor !`, `write class files !`
  - `select FiveKGWeight from ObjectType for PhysicalObject !` (form: `select <child> from <specialization> for <entity>`)
  - `mergeAll from Try.ses !` and `don't select CC under AA!` ("10 instances of AAs do not select CC entities")
  — [MS4 Me guide, pp. 128–129, 157–158](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Behavior inheritance from a selected specialization is not automatic in generated code. "We need to put 'inherit from x!' sentences into the pes file to create a specialization model with parent's behaviors ... without 'inherit from BetaChimpTwo! and inherit from AlphaChimpTwo!', the selected models have empty behaviors." The Physics example adds `inherit from FiveKGWeight!` and similar lines after each `select` — [MS4 Me guide, pp. 26, 158](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Interactive pruning: an entity with more than one aspect needs an aspect choice, and a specialization needs a child choice. Choices can be undone. "Finish Pruning" can save a *partially* pruned PES, and "When running a simulation from a partial PES, any unmade choices will be randomly selected". "Prune SES starting from these choices" resumes from an existing PES, and "Run PES file in SimViewer" executes it — [MS4 Me guide, §VI.G, pp. 168–169](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- SES-to-DEVS division of labor: the guide recommends defining behavior in the Atomic DEVS natural language (`.dnl`) and relationships with SES ("A very powerful technique is to define model behavior with Atomic DEVS and then to define the relationships between models with SES"). The `.dnl` files compile to "MS4DEVSJava" Java classes — [MS4 Me guide, pp. 133–144, 165](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- A graphical front end, the Sequence Diagram editor, has an "Entity Configuration Dialog" with entity types "Default, Specialization, Multi-Aspect, and Decomposition", and its diagrams are saved as `.ses` files — [MS4 Me guide, §II.D, pp. 35–59](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Runtime structure change is done in Java code embedded in `.dnl` atomic models (e.g., `parent.addChildModel(...)`, `parent.addCoupling(...)`, `parent.removeChildModel(...)`), not in the SES — [MS4 Me guide, §II.H "Dynamic Structure", pp. 115–117](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### Naming / file constraints imposed by the tool
- "You must place all SES files in the ses directory." SES *file* names "must start with a letter, contain only letters, numbers, or the '_' character, and be valid Java identifiers and cannot contain spaces, periods, or be Java reserved words" — [MS4 Me guide, §VI.F.1, p. 165](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- "You must add an aspect to the SES before any other elements can be added. This first aspect defines the 'root system'." — [MS4 Me guide, §VI.F.2, p. 166](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Pruned-entity names are synthesized with prefixes (e.g., `id0_Person`, `tall_boy`), and the `each`-to-`each` coupling semantics depend on these prefixes — [MS4 Me guide, pp. 121, 124](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Practitioner observation, not documented in the guide: this repo's skill notes say MS4 Me's parser "intermittently singularizes a trailing `s`" in collective names, and they advise `UAVGroup` over `UAVs` — [identifier-rules.md (local skill reference)](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/identifier-rules.md). The guide's own examples use plural collective names freely (`Processors`, `People`, `Boys`, `Enclaves`, `Clients`) — [MS4 Me guide, pp. 120–128](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### Discrepancies between this repo's secondary skill references and the guide (for the report writer)
- PES selection form: the skill reference documents `select {OPT} for {ENTITY}!` and admits it is unverified — [pes-grammar.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/pes-grammar.md). The guide's examples always use `select {OPT} from {SPEC_LABEL} for {ENTITY} !` — [MS4 Me guide, pp. 122, 157](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- The skill's DatingGame PES example uses `restructure multiaspects using height!` with `set multiplicity of height as [3] for Boy!` — [pes-grammar.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/pes-grammar.md). The guide advises against exactly this (see the p. 124 warning quoted above).
- The skill says "Single-child aspects are flattened by MS4 Me; author at least 2 children" — [ses-grammar.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/ses-grammar.md). The guide defines a single-subsystem aspect form and uses one (`AgentAndActor is made of Agent!`) — [MS4 Me guide, pp. 115, 174](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- The skill places the no-comma rule at "exactly two children". The guide states it for more than 2 — [ses-grammar.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/ses-grammar.md) vs [MS4 Me guide, p. 174](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- The skill omits documented PES directives: `inherit from`, `mergeAll from`, `don't select X under Y`, and the `restructure ... using A then B` ordering — [pes-grammar.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/pes-grammar.md) vs [MS4 Me guide, pp. 26, 126, 129, 158](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- Consistent with the guide: the identifier regex, dashes allowed in SES, Java-identifier file names, the `all`/`each` coupling forms, and `write class files!` — [identifier-rules.md](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/reference/identifier-rules.md); [HelloWorld.pes / SimpleWorkFlow.pes examples](file:///home/chris/DEVS/devs-skills-claude/skills/ses-designer/resources/examples/SimpleWorkFlow.pes)

#### Status / lineage
- The current MS4 Systems site (footer "Copyright © 2024 MS4 Systems") promotes an "MS4ME web version" that is "Integrated with GPT3.0 generative AI capabilities ... generate activity diagrams with simple prompts and run the DEVS simulation". The home page does not mention SES — [MS4 Systems home page](https://ms4systems.com/)
- Zeigler & Sarjoughian's *Guide to Modeling and Simulation of Systems of Systems* has a chapter "System Entity Structure Basics" (Springer, 2013, and a 2017 edition). A companion SpringerBriefs volume has a chapter "Pruning SES List" (Zeigler, 2012) — [V: Crossref] [doi:10.1007/978-0-85729-865-2_3](https://doi.org/10.1007/978-0-85729-865-2_3); [doi:10.1007/978-3-319-64134-8_3](https://doi.org/10.1007/978-3-319-64134-8_3); [doi:10.1007/978-1-4471-4570-7_3](https://doi.org/10.1007/978-1-4471-4570-7_3)
- Earlier natural-language-style SES work exists. Example: Simard, Zeigler & Couretas, "Verb phrase model specification via system entity structures", 5th Annual Conf. on AI, Simulation and Planning in High Autonomy Systems (1994) — [V: Crossref, title/authors/venue only] [doi:10.1109/AIHAS.1994.390473](https://doi.org/10.1109/AIHAS.1994.390473)

### Inferences
- MS4 Me attaches couplings to an *aspect label* ("from the X perspective"), which follows Zeigler's attachment of couplings to aspects. It differs in deriving port names from message names (`out<Msg>`/`in<Msg>`) rather than declaring ports, so a message name is in effect a port pair.
- The multi-aspect idiom uses a *specialization* (`Person can be id in index!`) as a replication index, not as a taxonomy. Expansion then *restructures* the tree: the multi-aspect disappears and the copies move up to the grandparent's aspect. The resulting PES is therefore not simply a sub-tree of the SES, which departs from the textbook "selection-only" view of pruning.
- Two rules make an MS4 Me PES under-determined: unresolved choices are filled at random, and specialization behavior needs an explicit `inherit from`. A PES alone therefore does not fully define a model unless every choice and inheritance is written out. Tools that generate PES files (including this repo's `ses-designer` skill) should emit both explicitly.
- SES identifiers may contain dashes, but SES file names and atomic `.dnl` identifiers may not. Entities map to Java classes and models, so a dashed entity name is likely to fail at code generation. The guide does not say this; it is inferred from the two identifier rules.

### Gaps
- No syntax is documented for "Similarities" (truck "like" car), although Appendix 2 lists them as an SES element. Whether MS4 Me supports them was not determined.
- No formal grammar, BNF, or version history for the `.ses`/`.pes` languages was found. The guide is undated, and the web product's current SES support could not be determined from the public site.
- The trailing-`s` singularization issue appears only in this project's notes. No MS4 Systems documentation confirms it.
- The exact semantics of `mergeAll from X.ses !` (merging multiple SES files?) are not explained in the guide.

---

## 2. XML-based SES representations (SES as XML Schema, PES as XML instance; Zeigler & Hammonds; Mittal & Martín; later XSDs)

### Takeaway
Two different XML mappings coexist. In the first, from Zeigler & Hammonds (2007) and reused by Mittal/Risco-Martín, an SES is a compact generator of a *domain* XML Schema, and SESs are transformed "back and forth to XML". In the second, from the DLR/TU Clausthal SESTools and the Wismar tools, the SES itself is an XML *instance* of a generic SES meta-schema (`ses.xsd`). That meta-schema enforces several axioms with XML Schema 1.1 assertions and expresses selection constraints in XPath, but it has no coupling construct. Both mappings drop behavior. Couplings and procedural extensions are also lost unless carried in extra attributes or side files.

### Cited Findings
- Zeigler, B. P. & Hammonds, P. E., *Modeling & Simulation-Based Data Engineering: Introducing Pragmatics into Ontologies for Net-Centric Information Exchange*, Elsevier/Academic Press, published Aug 7, 2007. The table of contents includes Ch. 2 "Basics of System Entity Structuring", Ch. 3 "Representing the SES in XML", Ch. 4 "Using the SES-Java Tool Set", Ch. 6 "SES Families for Dynamic Data Processes", and Ch. 7 "Harmonization: Comparing SESs and Finding Common Cores". The publisher blurb says the SES has "all of the functionality of UML and few of the drawbacks" and aims at automating "the development of XML dtds and schemas" — [V: publisher page] [Elsevier book page](https://shop.elsevier.com/books/modeling-and-simulation-based-data-engineering/zeigler/978-0-12-372515-8); ISBN 9780123725158 and 448 pp. per [library catalog record](https://www.biblio.com/0123725151)
- Risco-Martín, de la Cruz, Mittal & Zeigler (2009) describe SES/XML tooling: "Tools have been developed to transform SESs back and forth to XML allowing many operations to be specified in either SES directly or in its XML guise ... it makes development of XML Schema transparent to the modeler ... SES structures are compact relative to equivalent Schema." They use the SES "to develop metamodels of UML, DEVS and eUDEVS". Behavior is carried separately in XFD-DEVS, "a W3C XML Schema" for Finite Deterministic DEVS. They also note that a model synthesized from a PES "doesn't contain the behavior specifications" — [V: Crossref] "eUDEVS: Executable UML with DEVS Theory of Modeling and Simulation", *SIMULATION* 85(11–12):750–777 (2009) [doi:10.1177/0037549709104727](https://doi.org/10.1177/0037549709104727); full text via [arXiv:2407.08281](https://arxiv.org/abs/2407.08281)
- Mittal & Risco Martín, *Netcentric System of Systems Engineering with DEVS Unified Process*, CRC Press, 2013, 712 pp. Part II includes a chapter "System Entity Structures and Contingency-Based Systems", alongside chapters on "DEVS Modeling Language", "DEVS Unified Process", "Model-Driven Engineering ...", and "Executable UML" — [V: publisher page] [Routledge/CRC page](https://www.routledge.com/Netcentric-System-of-Systems-Engineering-with-DEVS-Unified-Process/Mittal-Martin/p/book/9781439827062); [V: Crossref] [doi:10.1201/b13971](https://doi.org/10.1201/b13971)
- Cheon, Kim & Zeigler, "DEVS model composition by system entity structure", IEEE Int. Conf. on Information Reuse and Integration (IRI 2008), pp. 479–484 — [V: Crossref, title/authors/venue/pages] [doi:10.1109/IRI.2008.4583078](https://doi.org/10.1109/IRI.2008.4583078). The abstract was not retrieved.
- Karmokar, Durak, Hartmann & Zeigler, "Towards a standard computational representation for System Entity Structures", SummerSim/SCSC 2019, Berlin, July 22–24, 2019. Keywords: "System Entity Structure (SES), XML Schema, XPath". The full text is restricted — [V: DLR elib record] [elib.dlr.de/130764](https://elib.dlr.de/130764)
- The SESTools repo (DLR / TU Clausthal "aeronautical-informatics") ships `ses.xsd`, an XML Schema **1.1** (`vc:minVersion="1.1"`). It defines elements `entity`, `aspect`, `multiAspect`, `specialization`, and `var`. `var` has `name/type/default/lower/upper` attributes, `multiAspect` has an optional `constraint` attribute, and `entity` has an optional `ref`. Three `xs:assert` rules check (i) that no descendant repeats an entity's name, (ii) that siblings have distinct names, and (iii) that variable names are distinct — [V: repo] [SESTools ses.xsd](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/schema/ses.xsd)
- Example instance (`Scenario.xml`, validated against `ses.xsd`): `<entity name="Scenario"><aspect name="scenarioDec"><entity name="Environment"><var name="rho" default="5.5" lower="1.1" upper="10.1"/>...<entity name="Entities"><multiAspect name="entityMAsp"><entity name="Entity"><specialization name="entitySpec"><entity name="Aircraft">...` — [V: repo] [SESTools Scenario.xml](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/xslt/Scenario.xml)
- The SESTools README says "selection constraints can be added to the aspect node to restrict the choices of entities. These constraints are specified using XPath". It lists pruning as (1) assigning variable values, (2) choosing among aspects, (3) selecting a specialization entity, and (4) "Specifying cardinality in Multi-Aspect node" — [V: repo] [SESTools README](https://github.com/aeronautical-informatics/SESTools)
- Wismar tools also use XML as the exchange format. A 2017 workshop paper says the Python/PyQt5 SES editor uses "den XML Standard" for storing SES and exchanging them with simulation environments (title page: "Ein Software-Werkzeug zum Variantenmanagement unter Verwendung des SES/MB Ansatzes", Folkerts & Pawletta, HS Wismar) — [V: PDF title page; venue (AUTSYM 2017) inferred from URL only, UNVERIFIED] [PDF](https://fiw.hs-wismar.de/storages/hs-wismar/_FIW/_Spezialseiten/AUTSYM/Paper_AUTSYM_2017/Folkerts_HS_Wismar.pdf). SESViewEl receives "a System Entity Structure (SES) coded as XML ... (e.g. from SESToPy)" over a socket and renders it as SVG — [V: repo] [cea-wismar/sesviewel](https://github.com/cea-wismar/sesviewel)
- UNVERIFIED: an ACIMS paper, "Automated Link 16 Testing using DEVS and XML", reportedly says that PES are mapped directly into XML with SES node modes becoming XML elements. The PDF returned HTTP 403 and only a search snippet was seen — [ACIMS PDF (not opened)](https://acims.asu.edu/wp-content/uploads/sites/18/2012/02/Automated-Link-16-Testing-using-DEVS-and-XML.pdf)

### Inferences
- **What the meta-schema mapping (SESTools `ses.xsd`) preserves:**
  - Tree shape.
  - Alternating mode, structurally: entities contain only aspect/specialization/multiAspect/var, and descriptive nodes contain only entities.
  - Strict hierarchy, valid brothers, and attached-variable uniqueness, via assertions.
  - Variables with bounds.
- **What it loses:**
  - Couplings: there is no coupling element at all.
  - Uniformity: no assertion checks isomorphic subtrees for same-named nodes.
  - Message/port information.
  - Any procedural knowledge.
  - Also, `minOccurs="0"` allows empty aspects and specializations, which Zeigler's definitions would not normally treat as meaningful.
- **What the Zeigler–Hammonds/Mittal mapping (SES → domain XML Schema) preserves and loses:** it serves *data* engineering, so the SES generates schema structure and a PES-derived instance is a data document. It preserves composition and taxonomy. Dynamics (DEVS behavior) live in a separate schema (XFD-DEVS), and eUDEVS states explicitly that a PES-synthesized model lacks behavior.
- The two XML traditions therefore treat "SES = XML Schema" differently. In Zeigler–Hammonds the SES *is* (generates) the schema for a domain. In SESTools the SES is *an instance* of a fixed meta-schema. A report should not conflate them.

### Gaps
- The full text of Zeigler & Hammonds Ch. 3 was not accessible, so the exact SES→XSD mapping rules (how specializations map to `xs:choice`, multi-aspects to `maxOccurs`, couplings) were **not verified**. The user's premise "SES as XML Schema, PES as XML instance" is consistent with the TOC and eUDEVS but not quoted from the book.
- The full text of Karmokar et al. (SummerSim 2019) was not accessible (DLR-internal PDF). Its proposed standard, and whether it covers couplings, is unknown. The DOI and pages were not found in Crossref.
- No standalone document titled "DEVS/XML" by Mittal & Martín that defines an SES XML mapping was located. The available evidence is eUDEVS (2009) and the 2013 book TOC.
- The XML format used by MS4 Me's predecessor tools ("SES-Java Tool Set" / SESBuilder) was not inspected.

---

## 3. Pawletta and colleagues (Hochschule Wismar, CEA group; with DLR / TU Clausthal and Zeigler): SES Toolbox for MATLAB/Simulink, extended SES (eSES), automatic model generation, reconfigurable systems, experimentation infrastructure

### Takeaway
The Wismar group turned SES/MB into an "extended SES/MB" (eSES/MB) infrastructure. It adds:
- **SES variables** (a global input interface) restricted by **semantic conditions**.
- **Selection rules** at specializations, **aspect rules**, and cross-tree **selection constraints**.
- **numRep** at multi-aspects, **priorities**, an `mb` link attribute at leaves, and a `couplings` attribute.
- **SES functions**: procedural code evaluated during pruning to compute couplings, attributes, and iterators, which the group frames as extending "the declarative specification ... by procedural knowledge".
- A **NONE** element for optional features, **merge**, and **flattening** to an FPES.
- An outer **Experiment Control + Execution Unit** loop for reactive, automated generation and execution of model variants.

Implementations include a MATLAB toolbox (Simulink, plus earlier Dymola/OpenModelica builders) and a Python suite (SESToPy/SESMoPy/SESEuPy/SESEcPy) that targets Simulink, Dymola, OpenModelica, FMI, and DEVS. Development appears dormant since about 2022–2023.

### Cited Findings

#### Core publications (bibliography verified)
- Pawletta, T., Pascheka, D., Schmidt, A., Pawletta, S., "Ontology-Assisted System Modeling and Simulation within MATLAB/Simulink", *SNE* 24(2):59–68, Aug 2014. The article introduces "a new SES toolbox for ontological modeling within the MATLAB/Simulink environment" — [V: Crossref + journal page] [doi:10.11128/sne.24.tn.10241](https://doi.org/10.11128/sne.24.tn.10241); [SNE page](https://sne-journal.org/sne-volumes/volume-24/sne-242-articles/ontology-assisted-system-modeling-and-simulation-within-matlab-simulink). Note: the toolbox README misprints the DOI as `...tn.102241` — [SES_Tbx_Matlab README](https://github.com/cea-wismar/SES_Tbx_Matlab)
- Pawletta, T., Pascheka, D., Schmidt, A., "System Entity Structure Ontology Toolbox for MATLAB/Simulink: Used for Variant Modelling", *IFAC-PapersOnLine* 48(1):685–686 (MATHMOD 2015) — [V: Crossref] [doi:10.1016/j.ifacol.2015.05.188](https://doi.org/10.1016/j.ifacol.2015.05.188). The README instead cites the same paper as ARGESIM Report 44, pp. 369–370 — [README](https://github.com/cea-wismar/SES_Tbx_Matlab)
- Pawletta, T., Schmidt, A., Zeigler, B. P., Durak, U., "Extended Variability Modeling Using System Entity Structure Ontology within MATLAB/Simulink", SpringSim-ANSS 2016, Pasadena, Apr 3–6, 2016, pp. 62–69. The paper proposes "general extensions of the baseline SES approach with a focus on a new concept called SESFunctions" — [V: DLR elib + Crossref DOI] [elib.dlr.de/103961](https://elib.dlr.de/103961); [doi:10.22360/springsim.2016.anss.061](https://doi.org/10.22360/springsim.2016.anss.061)
- Schmidt, A., Durak, U., Pawletta, T., "Model-based testing methodology using system entity structures for MATLAB/Simulink models", *SIMULATION* 92(8):729–746 (2016) — [V: Crossref] [doi:10.1177/0037549716656791](https://doi.org/10.1177/0037549716656791)
- Durak, U., Pawletta, T., Oğuztüzün, H., Zeigler, B. P., "System Entity Structure and Model Base Framework in Model Based Engineering of Simulations for Technical Systems", SpringSim-Mod4Sim 2017, Virginia Beach. The paper compares SES/MB with "comparable software engineering methodologies" (metamodeling, model transformation) — [V: DLR elib + Crossref DOI] [elib.dlr.de/112672](https://elib.dlr.de/112672/); [doi:10.22360/springsim.2017.mod4sim.001](https://doi.org/10.22360/springsim.2017.mod4sim.001)
- Pawletta, T., Schmidt, A., Durak, U., Zeigler, B. P., "A Framework for the Metamodeling of Multi-variant Systems and Reactive Simulation Model Generation and Execution", *SNE* 28(1):11–18 (2018) — [V: PDF title page + Crossref] [doi:10.11128/sne.28.tn.10402](https://doi.org/10.11128/sne.28.tn.10402)
- Pawletta, T., Durak, U., Schmidt, A., "Modeling and Simulation of Versatile Technical Systems Using an Extended System Entity Structure/Model Base Infrastructure", in *Model Engineering for Simulation* (Academic Press, 2019), pp. 393–418. The chapter "extends the classical SES/MB framework by an experiment control unit and an execution unit" and extends "classical SES theory ... by some new concepts" (industrial robot control example) — [V: Crossref; abstract via Semantic Scholar] [doi:10.1016/B978-0-12-813543-3.00018-4](https://doi.org/10.1016/B978-0-12-813543-3.00018-4)
- Folkerts, H., Pawletta, T., Deatcu, C., Durak, U., "Variability Modeling for Engineering Applications", *SNE* 27(4):167–176 (2017) — [V: PDF title page + Crossref] [doi:10.11128/sne.27.tn.10391](https://doi.org/10.11128/sne.27.tn.10391)
- Deatcu, C., Folkerts, H., Pawletta, T., Durak, U., "How to Define SES Trees for Variability Modeling", *SNE* 29(3):117–126 (2019) — [V: Crossref] [doi:10.11128/sne.29.tn.10482](https://doi.org/10.11128/sne.29.tn.10482)
- "Design Patterns for Variability Modeling using SES Ontology", Mod4Sim 2018 — [V: Crossref title/venue only; authors (Deatcu, Folkerts, Pawletta, Durak) per SESTools README, UNVERIFIED against publisher] [doi:10.22360/springsim.2018.mod4sim.004](https://doi.org/10.22360/springsim.2018.mod4sim.004)
- Folkerts, H., Pawletta, T., Durak, U., "An Extension for the Specification and Automated Selection of System Variants Based on the System Entity Structure Using a Problem from Process Industry", *SNE* 33(1):17–25 (2023) — [V: PDF title page + Crossref] [doi:10.11128/sne.33.tn.10633](https://doi.org/10.11128/sne.33.tn.10633)
- Schwatinski, T., Pawletta, T., Pawletta, S., "Flexible Task Oriented Robot Controls Using the System Entity Structure and Model Base Approach", *SNE* 22 (2012) — [V: Crossref] [doi:10.11128/sne.22.tn.10135](https://doi.org/10.11128/sne.22.tn.10135)

#### eSES constructs (what is added), with syntax
- SES variables: "SESVariables have a global scope and are written in uppercase letters in the tree". They are "the input interface of an SES". Example: `SESVariables = {SPEC_CTRL,NSL} with SPEC_CTRL ∈ {'lc','nc'} ...; auxiliarySESVariable = {NUM} with NUM = numel(NSL)` — [Pawletta et al. 2018, SNE 28(1), p. 13](https://doi.org/10.11128/sne.28.tn.10402)
- Couplings as an aspect attribute: `{cplg=...}` with the structure `{'SrcEntity','FromPrt','SinkEntity','ToPrt'}`, covering IC, EIC, and EOC — [SNE 28(1), p. 14](https://doi.org/10.11128/sne.28.tn.10402)
- SESFunctions: "like ordinary functions. They extend the declarative specification defined by the baseline SES by procedural knowledge descriptions. SESFunctions are calculated during ... pruning". Example: `cplg = c_mus(Children,NUM)` returns the coupling set, and "Children and Parent are implicit attributes of each tree node". A MATLAB body loops `for i=1:num` to build `cplg(i,1:4)={children{1},num2str(i),children{2},num2str(i)}` — [SNE 28(1), p. 14](https://doi.org/10.11128/sne.28.tn.10402)
- Selection rule at a specialization: `srule_ctrl = { SPEC_CTRL=='lc' → dtfcn | SPEC_CTRL=='nc' → ltable }`. A per-replica rule such as `srule_s = { Parent.type=='sine[x]' → sine[x] | ... }` uses the parent's attribute `type`, which an iterator SESFunction `s_fcn(NSL)` sets — [SNE 28(1), pp. 14–15](https://doi.org/10.11128/sne.28.tn.10402)
- Leaf `mb` attribute links to the model base (e.g., `{mb='scope'}`). Attributes can be multisets indexed at selection (`amp=#{1,2.5,3}` with `sine[x]`) — [SNE 28(1), pp. 13, 15](https://doi.org/10.11128/sne.28.tn.10402)
- Further attributes listed in SNE 27(4): specialization *selection rules*, *aspect rules* ("associated with aspect or multi-aspect nodes on the same hierarchy level determine which of the siblings is to be chosen"), *selection constraints* ("to select a certain entity based on decisions taken anywhere else in the SES tree"), `numRep` at multi-aspects, `mb` ("permitted just for leaf nodes"), and a `priority` attribute among descriptive nodes on the same level. Also: "All values of attributes can be defined by constants or set via SES variables or SES Functions", and semantic conditions "are checked before pruning to exclude certain system configurations". Couplings may be "causal or acausal" — [Folkerts et al. 2017, SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391)
- Optional components: "an extension of the SES is used, the NONE element ... if the NONE branch is selected, the entity is not included at all" — [SNE 27(4), p. 170](https://doi.org/10.11128/sne.27.tn.10391)
- Merge: "a merge operation is defined allowing two or more SES to be combined ... A merge point is characterized by the same entity node name in both SES ... both SES are defined completely and fulfill all axioms" — [SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391)
- Toolbox methods: "(i) merging ...; (ii) pruning ...; (iii) flattening for the hierarchy reduction of a PES; (iv) validity checking of an SES and PES; and (v) translation scripts or templates to build EM for Simulink or MATLAB/DEVS" — [SNE 28(1), p. 17](https://doi.org/10.11128/sne.28.tn.10402)
- Multi-aspects in later work: "Number of Replications (numRep) is an additional attribute ... to define a variable number of entities". "For automated pruning specialization nodes have to define a selection rule as attribute". The 2023 paper presents "hierarchically arranged multi-aspects" and "a method for an automated derivation of system variants". Its example SES variable `NumPartialPlants` is bounded by a Semantic Condition — [Folkerts, Pawletta, Durak 2023, SNE 33(1)](https://doi.org/10.11128/sne.33.tn.10633)

#### Changes relative to Zeigler's axioms (as stated by the authors)
- Strict hierarchy: "In former work [Rozenblit & Zeigler 1993] ... a first idea for solving this specific problem was discussed under the constraint of relaxing the SES strict hierarchy axiom. ... we will suggest a complete solution without violation of the strict hierarchy axiom." The solution is a multi-aspect whose replication list comes from an SES variable, plus per-replica specialization — [SNE 28(1), pp. 14–15](https://doi.org/10.11128/sne.28.tn.10402)
- Valid brothers: during multi-aspect pruning "replicas of s are renamed to comply with the valid brothers axiom" (`s1, s2, s3`) — [SNE 28(1), p. 16](https://doi.org/10.11128/sne.28.tn.10402)
- Inheritance axiom applied at pruning: "the parent and child entity of each specialization relation is combined according to the inheritance axiom", which yields names like `sine_s1` — [SNE 28(1), p. 16](https://doi.org/10.11128/sne.28.tn.10402)
- The authors place the eSES as "based on the baseline SES definitions in [Zeigler & Hammonds 2007], but it uses some modifications based on former works ... and introduces some new concepts, such as the SESFunctions" — [SNE 28(1), p. 13](https://doi.org/10.11128/sne.28.tn.10402)

#### Experimentation infrastructure (reactive model generation)
- The infrastructure adds an **Execution Unit (EU)**, which links an executable model with a simulation engine, runs it, and collects results, and an **Experiment Control (EC)**, which "computes new settings for SESVariables as current input for the SES/MB framework to start the next cycle". Executable models are tuples (MUS_i, EF_j), meaning model under study plus experimental frame. The loop supports "structural changes of a variable structure system or experiments with several system configurations ... in a reactive manner" — [SNE 28(1), p. 17](https://doi.org/10.11128/sne.28.tn.10402)
- The toolbox README describes "the 'new' model builder moBuild and the execution unit exUnit", in directories `./ExecutionUnit` and `./ExperimentControl`, for "fully automated simulation experiments" — [SES_Tbx_Matlab README](https://github.com/cea-wismar/SES_Tbx_Matlab)
- The DFG-funded (PA 631/2) work targets "reactive and structure variable controls of interacting industrial robots and ... objective fidelity evaluation of flight and research simulators" — [SNE 28(1), pp. 17–18](https://doi.org/10.11128/sne.28.tn.10402)

#### Model-generation targets and software status
- MATLAB SES Editor: builds for MATLAB/Simulink and also Dymola and OpenModelica, with user-supplied MBs. Its "starke Versionsabhängigkeit" (strong dependence on MATLAB/Simulink versions) motivated a simulator-independent Python/PyQt5 editor — [Folkerts & Pawletta workshop paper (venue UNVERIFIED)](https://fiw.hs-wismar.de/storages/hs-wismar/_FIW/_Spezialseiten/AUTSYM/Paper_AUTSYM_2017/Folkerts_HS_Wismar.pdf)
- SES_Tbx_Matlab README:
  - "tested with Matlab R2018a and Matlab R2021a" and GPLv3. Website "not up-to-date currently".
  - Known limitation: "Siblings of multi-aspect nodes and siblings of aspect and multi-aspect nodes on the second layer of the SES cannot be pruned".
  - "current release allows to define subtrees under multi-aspect nodes".
  - "Currently it is possible to create only Simulink models".
  — [README](https://github.com/cea-wismar/SES_Tbx_Matlab)
  - Repo created 2020-09-15, last push 2022-06-02 — [GitHub API metadata for cea-wismar/SES_Tbx_Matlab](https://api.github.com/repos/cea-wismar/SES_Tbx_Matlab)
  - The Wismar page lists "Latest Version: 1.7" — [HS Wismar SES Toolbox page](https://fiw.hs-wismar.de/fakultaet/ausstattung-forschung/forschungsgruppen/research-group-cea/software-projects/ses-toolbox/)
  - MATLAB File Exchange entry 68531 — linked from the [README](https://github.com/cea-wismar/SES_Tbx_Matlab); not opened.
- Python framework: Folkerts, Pawletta, Deatcu, Hartmann, "Python-based eSES/MB Framework: Model Specification and Automatic Model Generation for Multiple Simulators", *SNE* 29(4):207–215 (2019) — [V: Crossref] [doi:10.11128/sne.29.tn.10497](https://doi.org/10.11128/sne.29.tn.10497). A same-titled precursor with the same four authors appears in Crossref as 2019 IIPhDW, pp. 121–126 — [V: Crossref] [doi:10.1109/iiphdw.2019.8755423](https://doi.org/10.1109/iiphdw.2019.8755423). The SESToPy README instead cites it as ASIM Workshop 2019, ARGESIM Report 57, pp. 69–75 — [README](https://github.com/hendrikfolkerts/sestopy). Both venues may exist; this is flagged as a record inconsistency.
- FMI: Folkerts, Pawletta, Deatcu, "Model Generation for Multiple Simulators Using SES/MB and FMI", *SNE* 31(1):25–32 (2021), with an ASIM SST 2020 proceedings version on pp. 13–20 — [V: Crossref] [doi:10.11128/sne.31.tn.10554](https://doi.org/10.11128/sne.31.tn.10554); [doi:10.11128/arep.59.a59003](https://doi.org/10.11128/arep.59.a59003)
- Automated, reactive pruning (SpringSim 2020): Crossref title "Automated Generation of Simulation Models Using Automated, Reactive Pruning of System Entity Structures" (no authors in the record; ACM DL landing returns HTTP 403) — [V: Crossref title only] [doi:10.22360/springsim.2020.mod4sim.001](https://doi.org/10.22360/springsim.2020.mod4sim.001). The SESToPy README gives the title as "Automated, Reactive Pruning of System Entity Structures for Simulation Engineering" (Folkerts, Pawletta, Deatcu, Zeigler) — [README](https://github.com/hendrikfolkerts/sestopy). Authors are UNVERIFIED against the publisher.
- Comparative evaluation: Deatcu, Pawletta, Folkerts, "MATLAB/Simulink's Variant Manager vs SESToPy", *SNE* 29(1):39–43 (2019). Findings: Simulink variants form a "150%-model" with static submodel interfaces, "while the eSES/MB approach allows to define variable interfaces and couplings". Variant Manager modeling is "bottom-up" and eSES/MB "top-down". The authors concede "a final comparison is not possible on the basis of just one example" — [V: PDF title page + Crossref] [doi:10.11128/sne.29.sw.10466](https://doi.org/10.11128/sne.29.sw.10466)

### Inferences
- Relative to Zeigler, eSES moves the SES from a purely declarative family specification to a **parameterized, partly procedural metamodel**. Pruning becomes function evaluation driven by SES variables and rules rather than human selection. This makes pruning automatable and reactive (closing the loop with EC/EU), but the knowledge in the SES is then only as analyzable as arbitrary MATLAB or Python code.
- eSES keeps the classical axioms as validity criteria and works hard to avoid breaking strict hierarchy and valid brothers (renaming replicas, combining names under inheritance). The new attributes are layered on top of the axioms rather than replacing them.
- The `couplings` attribute (4-tuples, optionally computed) is more general than MS4 Me's message-named couplings. It allows variable port counts and names, which the authors cite as the key advantage over Simulink Variant Manager.
- Maintenance: Wismar SES repos show no pushes after mid-2022 (SES_Tbx) or 2021 (SESToPy/SESMoPy/SESViewEl). The most recent SES paper found is 2023. Meanwhile the same GitHub org keeps updating DEVS-for-MATLAB repos into 2025–2026, which suggests the group's tooling focus shifted.

### Gaps
- The 2016 SpringSim-ANSS full text (the first SESFunction paper) and the 2019 Model Engineering chapter were not read. Exact definitions of semantic conditions and selection constraints are taken from SNE 27(4)/28(1)/33(1) summaries.
- A. Schmidt's PhD thesis ("Variant Management in Modeling and Simulation Using the SES/MB Framework", Rostock, submitted 10/2018) is cited in SNE 29(1) but was not verified — UNVERIFIED.
- No independent (non-Wismar) evaluation of SES Toolbox usability or performance was found.

---

## 4. Python and other open-source / academic implementations; DEVS environments with SES support

### Takeaway
Verifiable open-source SES implementations are few, mostly academic, and mostly dormant:
- Wismar's GPLv3 Python suite (SESToPy editor/pruner, SESMoPy model builder, SESEuPy execution unit, SESEcPy experiment control, SESViewEl viewer).
- The Java SESEditor/PESEditor from DLR/TU Clausthal (XSD 1.1 plus XPath constraints).
- The MATLAB SES Toolbox.

In the DEVS tool world, ASU's SESM was replaced by CoSMoS, which supports "families" of models for DEVS-Suite. DEVSimPy (Corsica) hosted an SES extension for abstraction hierarchies and time granularity. No packages named "pyses", or a "SES" PyPI package, were found. No evidence was found of SES support in VLE.

### Cited Findings
- **SESToPy** ("SES editor for Python"): Python 3 / PyQt5. Its changelog lists "couplings, pruning, flattening", "Ports have a type", and "Socket client connection". Known bugs include "selections constraints in pruning ignored" and "error in semantic conditions function ... program crashes from time to time". It runs in Docker and is GPLv3. The repo was created 2019-02-15 with last push 2021-07-31 — [README](https://github.com/hendrikfolkerts/sestopy); [GitHub API metadata](https://api.github.com/repos/hendrikfolkerts/sestopy)
- SESToPy file format: `.jsonsestree`, headed "SES JSON Version 1 - DO NOT EDIT THIS FILE MANUALLY!", is a list of node records. Examples:
  - `["22","Aspect Node","ctrlSysDEC","21",...,[["","","","","","","cplfcn(feedforward,CHILDREN)",""]],...]` (a coupling *function call* stored as a string)
  - `["24","Spec Node","feedforwardCtrlSPEC",...,[["fc","25","feedforward==1","T",""],["NONE","29","feedforward==0","F",""]],...]` (selection rules plus a NONE child)
  - Leaf attributes such as `["mb","'MB/TransferFunction'"]`
  — [Feedback.jsonsestree](https://github.com/hendrikfolkerts/sestopy/blob/master/Examples/FeedbackControl/Feedback.jsonsestree)
- The companion coupling function is ordinary Python: `def cplfcn(feedforward, children): ... if feedforward==0: cplg.append([children[3],"y / SPR",children[4],"u / SPR",""])` — [cplfcn.py](https://github.com/hendrikfolkerts/sestopy/blob/master/Examples/FeedbackControl/cplfcn.py)
- **SESMoPy** ("Modelbuilder for Python"): created 2019-04-01, last push 2021-07-31 — [GitHub API metadata](https://api.github.com/repos/hendrikfolkerts/sesmopy). The bundled infrastructure repo contains SESToPy, SESMoPy (with `functionsDymola.py`, `functionsOpenModelica.py`, `functionsSimulink.py`, and an FMU compliance checker), SESEuPy (execution unit), and SESEcPy (experiment control). It was created and last pushed 2022-03-11 — [cea-wismar/SESMB_Inf_Python](https://github.com/cea-wismar/SESMB_Inf_Python)
- SESToPy and SESMoPy are described as "platform-independent and open source variability modeling tools". Flattening produces an FPES, from which SESMoPy generates executable models — [Deatcu et al. 2019, SNE 29(1)](https://doi.org/10.11128/sne.29.sw.10466)
- SES/MB + DEVS in Python: "An Integrated Modeling, Simulation and Experimentation Environment in Python Based on SES/MB and DEVS" (Folkerts, Pawletta, Deatcu, Santucci, Capocchi), SummerSim 2019, Berlin — cited in the [SESToPy README](https://github.com/hendrikfolkerts/sestopy). UNVERIFIED (no Crossref or ACM record retrieved).
- **SESEditor / PESEditor** (Java). The README says SESEditor lets a user "draw SES graphs on the screen almost as one would on paper", with variables and XPath selection constraints on aspect nodes. PESEditor is an "interactive pruning tool" that "supports all the design patterns proposed in Deatcu et al." and cannot edit the SES. Repo created 2019-06-19, last push 2023-06-14 — [aeronautical-informatics/SESTools](https://github.com/aeronautical-informatics/SESTools); earlier personal repos from 2018: [bikashkarmokar/SESEditor](https://github.com/bikashkarmokar/SESEditor), [bikashkarmokar/PESEditor](https://github.com/bikashkarmokar/PESEditor)
- Tool paper: Karmokar, Durak, Jafer, Chhaya, Hartmann, "Tools for Scenario Development Using System Entity Structures", AIAA SciTech 2019 Forum — [V: Crossref] [doi:10.2514/6.2019-1712](https://doi.org/10.2514/6.2019-1712)
- **SESViewEl**: Electron/Node.js 10.15.3 viewer. It "starts a socket server on port 54545" and renders SES sent as XML. GPLv3. Last push 2021-04-12 — [cea-wismar/sesviewel](https://github.com/cea-wismar/sesviewel)
- **ASU tools.** "CoSMoS replaces SESM (Scalable Entity Structure Modeler)". It supports "managed, standalone creation of families of component-based models" covering parallel DEVS, Cellular Automata, and XML Schema models. It generates "full Java code for the DEVS-Suite simulator" for coupled models but only "partial code generation ... for atomic parallel DEVS models". v2.1.0 requires JRE 1.5–1.7 and v3.0.0 is an Eclipse product requiring JRE 1.7 — [ACIMS CoSMoS page](https://acims.asu.edu/software/cosmos)
  - Sarjoughian & Elamvazhuthi, "CoSMoS: a visual environment for component-based modeling, experimental design, and simulation", SIMUTools 2009 — [V: Crossref] [doi:10.4108/icst.simutools2009.5744](https://doi.org/10.4108/icst.simutools2009.5744)
- **DEVSimPy / Corsica.** Santucci, Capocchi & Zeigler, "System entity structure extension to integrate abstraction hierarchies and time granularity into DEVS modeling and simulation", *SIMULATION* 92(8):747–769 (2016) — [V: Crossref] [doi:10.1177/0037549716657168](https://doi.org/10.1177/0037549716657168). Per its abstract as surfaced by search (the HAL page returned an access-denied page), it extends the SES with abstraction-hierarchy and time-granularity concepts and uses DEVSimPy to implement their management — [HAL record (blocked)](https://hal.archives-ouvertes.fr/hal-01326253)
- Capocchi, Santucci, Pawletta, Folkerts & Zeigler, "Discrete-Event Simulation Model Generation based on Activity Metrics", *Simulation Modelling Practice and Theory* (2020), article 102122. The paper uses "model level activity metrics" to "select the most practical model" from "a set of system entity structure family models" — [V: Crossref; abstract via Semantic Scholar] [doi:10.1016/j.simpat.2020.102122](https://doi.org/10.1016/j.simpat.2020.102122)
- Small or experimental repos:
  - `ji-flores/ses-natural-language-tool` ("Developing a natural language-based tool to support modeling and simulation processes that use the System Entity Structure ontological framework"; Java; created Nov 2023; README is only a title) — [repo](https://github.com/ji-flores/ses-natural-language-tool)
  - `akgunkaan/Autonomous-ODD-Extraction` (2025; SES for automotive ODDs from NuScenes, stored as Protobuf/YAML) — [repo](https://github.com/akgunkaan/Autonomous-ODD-Extraction)
  - Both were found in a GitHub search for "system entity structure" — [GitHub search API](https://api.github.com/search/repositories?q=%22system+entity+structure%22)

### Inferences
- The open-source SES ecosystem is fragmented. Each tool family has its own representation: MS4 Me controlled English, SESToPy positional JSON plus Python side files, SESTools XSD-validated XML, and MATLAB toolbox internals. There is no shared exchange format in practice, despite the 2019 "standard computational representation" proposal.
- "Python SES" in the literature effectively means the Wismar SESToPy/SESMoPy suite. Its last code activity was 2021–2022, and it lists unresolved bugs in constraint handling.

### Gaps
- Searches found no package or tool named "pyses", nor any VLE (Virtual Laboratory Environment) SES support. Absence is not proven.
- Whether current DEVS-Suite releases include any SES editor or pruner was not checked; the DEVS-Suite SourceForge page was not opened. The SESM primary paper (I/ITSEC 2005 PDF on ACIMS) was not opened (ACIMS PDFs return 403), so SESM details come only from search snippets and the CoSMoS page.
- The DEVSimPy GitHub repo was not inspected for an SES plugin.

---

## 5. Other extensions: variability / feature models, ontologies (OWL), SysML/MBSE, ML/AI pipelines, meta-level abstraction

### Takeaway
The best-documented "other" use is **variability modeling**. The Wismar group explicitly maps SES patterns onto feature-model relations and needs a NONE extension for optional features and an aspect-plus-specializations idiom for OR. **MBSE integration** appears in SES-X (SES philosophy plus the X language, with two pruning levels). **AI/ML pipelines** appear in DLR/TU Clausthal work, where SES is a meta-level ontology for an Operational Domain Entity Structure (ODES) that drives ODD definition, scenario generation, and simulation-based synthetic training data. Zeigler's **MetaSES** (2022) abstracts the SES itself. No verified primary source mapping SES to **OWL** was found.

### Cited Findings
- **Feature models.** Folkerts et al. map feature-model relations onto SES design patterns:
  - "(i) mandatory features (logical AND), (ii) alternative features (logical XOR), (iii) optional features and, (iv) OR-features (logical OR). In an SES mandatory and alternative features can be expressed in several ways."
  - Optional features need "the NONE element".
  - "OR features can only be expressed by the combination of an aspect node with two or more specialization nodes. Each of the specializations needs to have one NONE element as child."
  - The paper's goal is "to show that the whole SES can be constructed using elementary design patterns. These in turn can be classified into the categories of the feature relations known from feature models".
  — [Folkerts, Pawletta, Deatcu, Durak 2017, SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391)
- **Product lines.** Pawletta et al. frame SES/MB against Software Product Lines and variation points ("SPL define variation points where different variants of products can be derived"). They contrast "a big model with variation points" (150% model) with "a metamodel that is processed with appropriate transformation methods" — [Pawletta et al. 2018, SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402)
- **SysML/MBSE.** Xie, Zhang, Li, Gu, Chen, "SES-X: A MBSE Methodology Based on SES/MB and X Language", *Information* 14(1):23 (2023). It "integrates system modeling (following SES philosophy) with system simulation" (via the X language), covers analysis, architecture decomposition, physical modeling, and simulation, and introduces "two levels of model pruning for model verification and simulation efficiency" (automobile case) — [V: publisher-affiliated record + Crossref] [doi:10.3390/info14010023](https://doi.org/10.3390/info14010023); [PolyU record](https://research.polyu.edu.hk/en/publications/ses-x-a-mbse-methodology-based-on-sesmb-and-x-language/)
- **UML metamodeling via SES.** eUDEVS uses "SES ontology ... to develop metamodels of UML, DEVS and eUDEVS" — [doi:10.1177/0037549709104727](https://doi.org/10.1177/0037549709104727); [arXiv](https://arxiv.org/abs/2407.08281)
- **Meta-level abstraction (MetaSES).** Zeigler, "Extending the Hierarchy of System Specifications and Morphisms with SES Abstraction", *Information* 14(1):22 (2022/2023). It proposes "an abstraction of the SES called the MetaSES that supports the construction of complex systems of systems with multiple components belonging to specified classes" within "a computational framework" for hierarchical DEVS models — [V: Crossref; abstract via Semantic Scholar] [doi:10.3390/info14010022](https://doi.org/10.3390/info14010022)
- **AI/ML data pipelines.** Gupta, Durak, Ellis, Torens, "From Operational Scenarios to Synthetic Data: Simulation-Based Data Generation for AI-Based Airborne Systems", AIAA SciTech 2022 — [V: Crossref + PDF title page] [doi:10.2514/6.2022-2103](https://doi.org/10.2514/6.2022-2103); [open PDF](https://cps4eu.eu/wp-content/uploads/2022/11/From-Operational-Scenarios-to-Synthetic-Data-Simulation-Based-Data-Generation-for-AI-Based-Airborne-Systems.pdf). Key points:
  - "SES is proposed at the meta-level as the high-level ontology. At the domain level, the operational scenarios given in ConOps are formalized with domain modelling into the Operational Domain Entity Structure (ODES)."
  - A PES specifies a "conceptual scenario" that is converted for Unity 3D to produce labeled images for a CNN drogue detector (97% IoU-based accuracy reported).
  - The authors plan to extend "SES at the meta-level further towards supporting intelligent system entity structures".
- Related titles from the same group, with SES content not verified: Durak et al., "Using System Entity Structures to Model the Elements of a Scenario in a Research Flight Simulator", AIAA MST 2017 — [V: Crossref] [doi:10.2514/6.2017-1076](https://doi.org/10.2514/6.2017-1076). Sprockhoff, Gupta, Durak, Krueger, "Scenario-Based Synthetic Data Generation for an AI-based System Using a Flight Simulator", AIAA SciTech 2024 — [V: Crossref, title only] [doi:10.2514/6.2024-1462](https://doi.org/10.2514/6.2024-1462). Gupta, Rezayat, Burmester, Durak, Ma, Hartmann, "From ConOps to an Operational Domain Model: Harnessing LLMs for Conceptual Model Design", *Advances in Conceptual Modeling* (LNCS), 2025, pp. 137–156 — [V: Crossref, title/authors only] [doi:10.1007/978-3-032-08620-4_9](https://doi.org/10.1007/978-3-032-08620-4_9). Whether the LLM paper generates SES/ODES is UNVERIFIED (search summary only).
- **Reinforcement learning.** The Wismar CEA GitHub org hosts `EFforRL_MATLAB_CaseStudy` (2022) and `MATLAB-RL-Agent_with_SimEvents` (2023). Whether these use SES was not verified — [cea-wismar repos](https://github.com/cea-wismar)
- **Ontology framing (non-OWL).** Wismar papers call SES an "ontology" ("Ontology-Assisted System Modeling ..."), and Zeigler & Hammonds frame it as "introducing pragmatics into ontologies" — [doi:10.11128/sne.24.tn.10241](https://doi.org/10.11128/sne.24.tn.10241); [Elsevier](https://shop.elsevier.com/books/modeling-and-simulation-based-data-engineering/zeigler/978-0-12-372515-8)
- **False friend to avoid.** arXiv:1602.00242, "Technical Report: Representing SES Cases Using Ontology" (Miao Chen), uses OWL/RDF, but its "SES" is "Socio-ecological System", not System Entity Structure — [arXiv abstract](https://arxiv.org/abs/1602.00242)

### Inferences
- In feature-model terms, classical SES natively covers AND (aspect) and XOR (specialization) but lacks first-class optional and OR. Wismar's NONE leaf is a pragmatic extension, and OR remains an idiom. A report can state that SES and feature models are close but not isomorphic.
- The AI/ML use is a *data-pipeline* use (ODD → scenarios → synthetic data), not ML-model architecture search. It reuses SES pruning as the "scenario sampler" and is an emerging line, centered on DLR/TU Clausthal, from 2022 onward.

### Gaps
- **OWL:** no verified paper mapping SES constructs to OWL classes or properties, or round-tripping SES through Protégé, was found in this session. This should be reported as an evidence gap, not as non-existence.
- **SysML:** no verified tool that imports or exports SysML block diagrams to or from SES was found, apart from SES-X (X language, not SysML). A 2014 NDIA presentation surfaced in search but was not opened.
- No SES-based MLOps or hyperparameter / neural-architecture configuration work was found. Searches for "SES" plus "machine learning pipeline" returned only unrelated AutoML results.

---

## 6. Where tools disagree with each other or with Zeigler's original definitions

### Takeaway
The tools agree on the four node types and the general idea of pruning. They diverge on:
- **(a) where couplings live and how ports are named;**
- **(b) how multi-aspect multiplicity is specified and expanded;**
- **(c) whether pruning is human, random, or rule- and function-driven;**
- **(d) how constraints are expressed;**
- **(e) whether procedural knowledge is allowed inside the SES;**
- **(f) the serialization format.**

Several departures are deliberate tool conventions rather than theory: MS4 Me's index-specialization idiom and random completion, eSES SESFunctions and NONE, and the SESTools XSD without couplings.

### Cited Findings

#### Couplings
- **MS4 Me:** statements scoped to an aspect label, with ports derived as `out<Msg>`/`in<Msg>`, optional renaming, `all`/`each` quantifiers, and no self-coupling in all-to-all — [MS4 Me guide, pp. 122–124, 176–177](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **eSES:** a `couplings`/`cplg` attribute of 4-tuples `{'SrcEntity','FromPrt','SinkEntity','ToPrt'}` on aspect and multi-aspect nodes, optionally computed by a SESFunction, which allows variable port numbers — [SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402); [SNE 29(1)](https://doi.org/10.11128/sne.29.sw.10466)
- **SESTools XSD:** no coupling element — [ses.xsd](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/schema/ses.xsd)

#### Multi-aspect semantics
- **MS4 Me:** multiplicity is set in the PES via an index specialization. Restructuring removes the multi-aspect and moves copies to the grandparent aspect. All copies of a nested multi-aspect get the same count ("limitation ... of the current version") — [MS4 Me guide, pp. 121, 127](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **eSES:** a `numRep` attribute at the multi-aspect node, which can come from SES variables. Replicas are renamed for valid brothers, and per-replica specialization is done via iterator SESFunctions. Hierarchical multi-aspects support automated derivation — [SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402); [SNE 33(1)](https://doi.org/10.11128/sne.33.tn.10633)
- **SESTools:** cardinality is specified during pruning, and a `multiAspect` element may carry an XPath `constraint` — [SESTools README](https://github.com/aeronautical-informatics/SESTools); [ses.xsd](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/schema/ses.xsd)
- **Hobby-level misuse example:** the ODD-extraction repo lists "Multi-Aspects: e.g., Sensors on the vehicle (Lidar, Radar, Camera)". These are heterogeneous types, whereas the classical multi-aspect (per the MS4 Me guide citing Zeigler & Hammonds) has a single multi-entity type — [Autonomous-ODD-Extraction README](https://github.com/akgunkaan/Autonomous-ODD-Extraction) vs [MS4 Me guide, pp. 120–121](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### Pruning control
- **MS4 Me:** interactive or textual pruning, with unmade choices "randomly selected" at simulation time — [MS4 Me guide, pp. 122, 168](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **eSES:** "For automated pruning specialization nodes have to define a selection rule as attribute". Pruning is driven by SES variables, and semantic conditions are checked before pruning — [SNE 33(1)](https://doi.org/10.11128/sne.33.tn.10633); [SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391)

#### Constraints
- **MS4 Me (as documented):** variable type/range declarations and PES `don't select X under Y!` — [MS4 Me guide, pp. 129, 177–178](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **eSES:** semantic conditions, selection rules, aspect rules, cross-tree selection constraints, and priorities — [SNE 27(4)](https://doi.org/10.11128/sne.27.tn.10391). SESToPy's README admits "selections constraints in pruning ignored" — [README](https://github.com/hendrikfolkerts/sestopy)
- **SESTools:** XPath constraints on aspect and multi-aspect nodes — [README](https://github.com/aeronautical-informatics/SESTools)

#### Declarative vs procedural
- **eSES:** SESFunctions "extend the declarative specification defined by the baseline SES by procedural knowledge descriptions" — [SNE 28(1), p. 14](https://doi.org/10.11128/sne.28.tn.10402)
- **MS4 Me:** keeps the SES declarative. Procedural structure change happens in Java inside atomic models (dynamic structure) — [MS4 Me guide, pp. 115–117](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)

#### Specialization and inheritance naming
- **MS4 Me:** pruned names carry the selected specialization as a prefix (`tall_boy`), and behavior inheritance needs `inherit from X!` — [MS4 Me guide, pp. 26, 124](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **eSES:** the parent and selected child are "combined according to the inheritance axiom" into names like `sine_s1` — [SNE 28(1), p. 16](https://doi.org/10.11128/sne.28.tn.10402)

#### Strict hierarchy
- **Rozenblit & Zeigler (1993):** earlier work relaxed the strict-hierarchy axiom for variable decomposition, as reported in [SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402).
- **eSES (2018):** explicitly avoids violating it — [SNE 28(1)](https://doi.org/10.11128/sne.28.tn.10402)
- **SESTools:** enforces it with an XSD assertion — [ses.xsd](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/schema/ses.xsd)

#### Serialization
- **MS4 Me:** English `.ses`/`.pes` text — [guide](file:///home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **SESToPy:** positional JSON "DO NOT EDIT" plus Python files — [Feedback.jsonsestree](https://github.com/hendrikfolkerts/sestopy/blob/master/Examples/FeedbackControl/Feedback.jsonsestree)
- **SESTools:** XML instance of `ses.xsd` — [ses.xsd](https://github.com/aeronautical-informatics/SESTools/blob/HEAD/SESEditor/src/main/java/dlr/xml/schema/ses.xsd)
- **Zeigler–Hammonds:** SES to and from XML Schema for data engineering — [Elsevier TOC](https://shop.elsevier.com/books/modeling-and-simulation-based-data-engineering/zeigler/978-0-12-372515-8); [eUDEVS](https://doi.org/10.1177/0037549709104727)

### Inferences
- **Coupling semantics are the least standardized part of SES tooling.** The three representations are not mutually translatable without information loss: MS4 Me's message-name ports, eSES's computed port tuples, and SESTools' absent couplings. Any exchange standard would need to settle ports versus messages and static versus computed couplings first.
- **Multi-aspect handling is the second major split.** It differs in where multiplicity lives (PES vs. node attribute vs. SES variable), in whether expansion restructures the tree, and in whether replicas can be pruned independently (yes in both MS4 Me and eSES, by different mechanisms).
- **Determinism differs.** MS4 Me tolerates incomplete PES files and fills them randomly, while eSES and SESTools aim for fully determined, rule-driven pruning. For reproducible experimentation (e.g., EFSUT-style studies), MS4 Me PES files should resolve every choice explicitly.

### Gaps
- No direct comparison paper across MS4 Me, the SES Toolbox, SESToPy, and SESTools was found. The disagreements above are synthesized from each tool's own documentation.
- How Zeigler's own current tooling (MS4 Me web version, the MetaSES framework) treats couplings and constraints after 2022 was not determined.

#### Citation verification ledger (supplementary)
- Verified against Crossref, a publisher record, or a PDF title page:
  - Zeigler & Hammonds 2007 (publisher page; ISBN via catalog).
  - Mittal & Risco Martín 2013 (publisher page + Crossref).
  - eUDEVS 2009.
  - Cheon/Kim/Zeigler 2008.
  - Pawletta et al. 2014 SNE and 2015 IFAC.
  - Pawletta et al. 2016 ANSS (DLR elib).
  - Schmidt et al. 2016.
  - Durak et al. 2017 Mod4Sim (DLR elib) and AIAA 2017.
  - Pawletta et al. 2018 SNE 28(1) (read in full).
  - Folkerts et al. 2017 SNE 27(4) (read).
  - Deatcu et al. 2019 SNE 29(1) (read) and SNE 29(3).
  - Folkerts et al. 2019 SNE 29(4) and IIPhDW 2019.
  - Folkerts et al. 2021 SNE 31(1) and ASIM 2020.
  - Folkerts et al. 2023 SNE 33(1) (read).
  - Pawletta/Durak/Schmidt 2019 chapter.
  - Schwatinski et al. 2012.
  - Santucci/Capocchi/Zeigler 2016.
  - Capocchi et al. 2020.
  - Zeigler 2022 MetaSES.
  - Xie et al. 2023 SES-X.
  - Karmokar et al. AIAA 2019 and SummerSim 2019 (DLR elib, no DOI).
  - Gupta et al. 2022 (read).
  - Sarjoughian & Elamvazhuthi 2009.
  - Zeigler & Sarjoughian chapters 2013/2017; Zeigler 2012 SpringerBriefs chapter.
  - Simard/Zeigler/Couretas 1994.
- UNVERIFIED:
  - Folkerts et al. SummerSim 2019 (README only).
  - SpringSim 2020 authors and title discrepancy.
  - Mod4Sim 2018 authors.
  - The Wismar AUTSYM 2017 venue.
  - Schmidt PhD thesis 2018.
  - ACIMS "Automated Link 16 Testing using DEVS and XML" (authors, venue, year, content).
  - SESM I/ITSEC 2005 paper.
  - The SES content of the 2024 and 2025 Durak-group papers.
