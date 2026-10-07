# The SES/Model Base (SES/MB) Framework and Methodology (Zeigler and collaborators, 1980s to present)

> **Source copy.** Research notes produced on 2026-10-06 for the SES/MB deep-research report (repo path `research_notes/SES and model base methodology/`), copied into the skill for verbatim lookup and lightly edited to remove personnel details. They are working notes: quotes and page locators are the useful part, and the curated files under `../../reference/` take precedence. References to "the report writer" or "other researchers" are from the original research task.

Scope: the SES/MB workflow, the model base, pruning, and synthesis ("transform"). Formal SES axioms, third-party tools (Pawletta SES Toolbox, XML/Python SES) and application case studies belong to other researchers and appear here only where needed.

How sources were checked: each work cited below had its title, authors, year and venue checked against a Crossref DOI record (api.crossref.org), an Open Library ISBN or catalog record, or the scanned cover page of the paper itself. The scanned WSC papers come from the INFORMS-SIM WSC archive (informs-sim.org), and each one's printed proceedings header and page numbers were read directly. Anything I could not check this way is marked **UNVERIFIED**. Statements are credited to the person who made them. "(Zeigler et al. state)" means the claim comes from Zeigler or a coauthor. Claims from later authors are labelled as such. Where possible, quotations are given verbatim from the scanned pages.

---

## 1. What is the "model base" (MB)? What goes in it, and how is it organized and managed?

### Takeaway
In Zeigler's own early papers, the model base is a repository of *modular* models. Each model has explicit input and output ports, so it can be retrieved and coupled to other models. It holds atomic models, coupled models, and models of experimental-frame components (generators, transducers, acceptors). The MB is the *procedural/behavioral* half of a knowledge base whose *declarative/structural* half is the entity structure base (SES plus pruned entity structures). In DEVS-Scheme the MB was a set of model-definition files plus state files, managed through ESP-Scheme operations (save, load, retrieve, store, prune, transform). Pruned entity structures were saved in the base as catalogue entries for alternative models.

### Cited Findings
- **Model base and modularity (Zeigler 1986).** Zeigler introduces the MB through modularity. If models A and B are "in the model base" in proper modular form, they can be coupled into AB, "a coupled model, is once again in modular form ... Once placed into the model base, AB can itself be employed to construct yet larger models in the same manner used with A and B. This property, called closure under coupling enables hierarchical construction of models." — Zeigler, B.P. (1986), "Hierarchical Modular Modeling/Knowledge Representation," *Proc. 1986 Winter Simulation Conference* (J. Wilson, J. Henriksen, S. Roberts, eds.), pp. 129–137, DOI 10.1145/318242.318280 — [scanned paper, p. 129](https://www.informs-sim.org/wsc86papers/1986_0019.pdf); [DOI](https://doi.org/10.1145/318242.318280)
- **The file-based MB in the 1986 paper.** An atomic model has three parts, kept as files: `.io` (input/output ports and ranges), `.stat` (static structure) and `.dyn` (dynamic structure, i.e., external and internal transitions). A coupled model has `.io`, `.comp` (component names) and `.coup` (coupling spec). "To construct a coupled model, the files associated with its components are retrieved from the model base and merged with the files describing the coupled model." Model *classes* (multiple models) add `.gen` files that generate instances. — [Zeigler 1986, pp. 133–135](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **SES is declarative; MB is procedural (Zeigler 1986, §4.1 "The System Entity Structure/Model Knowledge Base").** "The decomposition, taxonomic, and coupling relationships are combined in the system entity structure, a declarative scheme related to frame-theoretic and object-based representations. The model base contains models which are procedural in character, expressed in classical and AI-derived formalisms such as production rules and logic programming." In 1986 the entity-structure side was supported by "ESP-4 (entity structurer and pruner), a PASCAL system available for VAX under VMS and being ported to MS-DOS." — [Zeigler 1986, p. 137](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **Two-part knowledge base (Kim 1988 dissertation).** "The knowledge base created using the framework is composed of a structural knowledge base called entity structure base and a behavioral knowledge base called model base." DEVS-Scheme was extended for behavioral knowledge, especially kernel models for massively parallel architectures. ESP-Scheme, "a realization of entity structure formalism in a frame-theoretic representation," was extended to manage structural knowledge. — Kim, Tag Gon (1988), *A Knowledge-Based Environment for Hierarchical Modelling and Simulation*, Ph.D. dissertation, University of Arizona — [UA repository OAI record](https://repository.arizona.edu/server/oai/request?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:repository.arizona.edu:10150/184380); [handle](http://hdl.handle.net/10150/184380)
- **Concrete layout of the DEVS-Scheme knowledge base (Rozenblit, Kim & Zeigler 1988, Fig. 3).** The KNOWLEDGE BASE has two stores:
  - **ENBASE** holds "entity structure" and "pruned entity structure" files.
  - **MBASE** holds "models defn files" and "state vars files".

  The operations shown are:
  - ESP-Scheme side: `make-entstr`, `set-current-item`, `add-item`, `prune`, `save-entstr`, `load-entstr`.
  - Bridge between the two: `transform` and `inv-transform`.
  - DEVS-Scheme side: `retrieve`, `store`, `save-state`, `resume-state`.
  - Working-memory "working models": atomic models, digraph models, and kernel models (`make-pair`, `make-broadcast`, `make-hypercube`, `make-cellular`).

  "ESP-Scheme, implements system entity structure to synthesize and organize a family of models called the model base." — Rozenblit, J.W., Kim, T.G., Zeigler, B.P. (1988), "Towards an Implementation of a Knowledge-Based System Design and Simulation Environment," *Proc. 1988 WSC* (M. Abrams, P. Haigh, J. Comfort, eds.), pp. 226–230, DOI 10.1109/WSC.1988.716149 — [scanned paper, p. 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf); [DOI](https://doi.org/10.1109/wsc.1988.716149)
- **Archiving and cataloguing in the MB (Zeigler, Hu & Rozenblit 1989, "Layer 2: System Entity Structure/Model Base" of DEVS-Scheme):**
  - "Archivability: Models can be saved on disk in the form of model definition files or as pruned entity structure files. The latter form is preferred both for the convenience of model construction ... as well as for taking advantage of the automatic cataloguing provided by the environment."
  - "Model-base Cataloguing: Pruned entity structures are given the name of their root entity together with a user supplied suffix so that they can be identified as representing alternative models of the root entity."
  - "Reusability: Models developed for studying a particular real system, archived in the model base and managed by the system entity structure, are retrievable for use as components in new models. Due to pruned entity naming, hierarchical models expressed in the form of pruned entity structures are just as reusable as those in the model base. This fosters accumulative growth of 'off-the-shelf' reusable components."

  — Zeigler, B.P., Hu, J., Rozenblit, J.W. (1989), "Hierarchical, Modular Modelling in DEVS-Scheme," *Proc. 1989 WSC* (E.A. MacNair, K.J. Musselman, P. Heidelberger, eds.), pp. 84–89, DOI 10.1145/76738.76749 — [scanned paper, p. 86](https://www.informs-sim.org/wsc89papers/1989_0012.pdf); [DOI](https://doi.org/10.1145/76738.76749)
- **Experimental frames are model-base residents too (Zeigler et al. 1989).** "Experimental Frame/Model Separation: Experimental frames are independently realized as models of special kinds: generators, transducers, acceptors. Having input/output ports, they can be coupled to models to which they are applicable." — [Zeigler, Hu & Rozenblit 1989, p. 85](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Separate frame bases (Rozenblit's design methodology).** Fig. 1 of Rozenblit & Zeigler (1985) shows an "Entity Str. Base" feeding Design Model Construction and a separate "Exp. Frame Base" feeding Experimentation via Simulation. Rozenblit, Kim & Zeigler (1988) report that "Rozenblit and Hu (1988) are developing procedures for automatic experimental frame generation from a repository of basic frame components, called frame base." — [Rozenblit & Zeigler 1985, p. 224](https://www.informs-sim.org/wsc85papers/1985_0048.pdf); [Rozenblit, Kim & Zeigler 1988, p. 229](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **ESP-Scheme as a model base management system (Kim et al. 1990).** "ESP-Scheme acts as a model base management system in DEVS-Scheme, a knowledge-based simulation environment. It supports specification of the structure of a family of models, pruning the structure to a reduced version, and transforming the latter to a simulation model by synthesizing component models in the model base." The SES is "a labeled tree with attached variable types that satisfy certain axioms." Examples in the paper: a parallel processor model and a university phone registration system. — Kim, T.G., Lee, C., Christensen, E.R., Zeigler, B.P. (1990), "System Entity Structuring and Model Base Management," *IEEE Trans. Systems, Man, and Cybernetics* 20(5):1013–1024, DOI 10.1109/21.59966 (abstract via OpenAlex; metadata via Crossref) — [DOI](https://doi.org/10.1109/21.59966)
- **Model base management for multifaceted systems (Zeigler, Luh & Kim 1990/1991).** "Building on the basis of the system entity structure/model base framework as implemented in the DEVS-Scheme, the authors have extended the ability of their knowledge/model base tools to support multifaceted systems design through the ability to generate a family of design alternatives, reusability of models, and model base coherence and evolvability. It is shown how the pruning process supports reuse of previously pruned structures. Concepts of context-sensitive pruning and partitioned entity structure bases are introduced to promote model base coherence and evolvability." The approach was demonstrated on a robot-managed space-borne laboratory simulation.
  - Conference version: Zeigler, Luh, Kim, "Model base management for multifaceted systems," *Proc. [1990] AI, Simulation and Planning in High Autonomy Systems*, IEEE CS Press, pp. 25–31, DOI 10.1109/AIHAS.1990.93938 ([DOI](https://doi.org/10.1109/aihas.1990.93938)).
  - Journal version: Zeigler, B.P., Luh, C.-J., Kim, T.G. (1991), "Model base management for multifacetted systems," *ACM TOMACS* 1(3):195–218, DOI 10.1145/140765.140791 ([DOI](https://doi.org/10.1145/140765.140791)). Metadata verified via Crossref. The journal abstract was elided by the publisher in Semantic Scholar, so the quoted abstract above is from the IEEE conference version.
- **Book-length treatment (titles only; content not read).** Zeigler (1990), *Object-Oriented Simulation with Hierarchical, Modular Models: Intelligent Agents and Endomorphic Systems* (Academic Press, Boston), has chapters titled "Digraph-Models and Experimental Frames" (pp. 89–116), "A Model Base for Simple Multi-Computer Architectures" (pp. 117–143), "System Entity Structures" (pp. 145–168) and "Model Base Management and Endomorphic Systems" (pp. 299–323). — [Crossref chapter list, ISBN 9780127784526](https://api.crossref.org/works?filter=isbn:9780127784526); [Open Library record](https://openlibrary.org/isbn/9780127784526)
- **MS4 Me's file-based MB.** Project content lives in `src/Models`, with subdirectories `dnl` ("Atomic DEVS (finite discrete DEVS) natural language atomic model descriptions"), `java` ("Java source code generated for models and systems"), `pes` ("Pruned entity structures"), `ses` ("System entity structures") and `xml` (p. 160). "A very powerful technique is to define model behavior with Atomic DEVS and then to define the relationships between models with SES" (p. 165). — MS4 Systems, Inc., *MS4 Me User's Guide* (local PDF `/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf`; no year printed on the cover page; PDF metadata creation date 2021-10-19)
- **Cloud repository era (Zeigler, Seo & Kim 2013).** A suite of models is built in MS4 Me by "merging of System Entity Structures." Once built, "the suite of models can be hosted on Model Store, the cloud-based repository of models provided by MS4 systems as a basis for further collaborative model development". The paper also discusses "a marketplace of models." — Zeigler, B.P., Seo, C., Kim, D. (2013), "System Entity Structures for Suites of Simulation Models," *Int. J. Modeling, Simulation, and Scientific Computing* 4(3):1340006, DOI 10.1142/S1793962313400060 — [DOI](https://doi.org/10.1142/s1793962313400060)
- **Later repository chapters (titles only; content paywalled, not read).** Zeigler & Sarjoughian, *Guide to Modeling and Simulation of Systems of Systems*: "Model Repositories for Systems of Systems" (2013, ch. 16, pp. 259–279) and "Model Development and Execution Process with Repositories, Validation, and Verification" (2nd ed. 2017, ch. 16, pp. 295–324). — [Crossref chapter list 2013](https://api.crossref.org/works?filter=isbn:9780857298652); [Crossref chapter list 2017](https://api.crossref.org/works?filter=isbn:9783319641348)

### Inferences
- In the original papers the "model base" is a store of *behavior*. Structure is held separately in the entity structure base. The pair (ENBASE, MBASE) is what "SES/MB" names. Zeigler, Luh & Kim (1990) use the phrase "system entity structure/model base framework" themselves, so the term is the originators' own and was not coined later.
- Because pruned entity structures are stored and named as alternatives of their root entity, the MB stores two kinds of *model*: (a) primitive behavior files (atomic models and hand-built coupled models), and (b) recipes (PES files) that regenerate composite models on demand. MS4 Me keeps the same two-part split as `.dnl`/`.java` (behavior) plus `.ses`/`.pes` (structure).

### Gaps
- I did not read the full text of Kim et al. (1990, IEEE SMC) or Zeigler, Luh & Kim (1991, TOMACS). The detailed mechanics of "context-sensitive pruning" and "partitioned entity structure bases" therefore come only from the conference abstract.
- I did not read the 1990 book chapters ("System Entity Structures"; "Model Base Management and Endomorphic Systems"). Their content is not reflected here beyond the chapter titles.

---

## 2. What are the end-to-end steps of the SES/MB methodology, and what are the operations called?

### Takeaway
Zeigler and colleagues describe a loop with six steps:
1. Specify (or retrieve) an SES for the domain.
2. **Prune** it, top-down, against objectives, requirements and constraints (expressed as generic experimental frames and selection/synthesis rules), producing a **pruned entity structure (PES)** or "composition tree".
3. **Transform** the PES: retrieve components from the model base and synthesize them bottom-up into a hierarchical, modular DEVS coupled model.
4. Simulate the model inside an experimental frame.
5. Evaluate and rank the alternatives.
6. Iterate.

The operation names used in the literature are *pruning* (`prune`), *transform* / *synthesis* (`transform`, with the inverse `inv-transform`), *retrieve*, *store*, *save-entstr*/*load-entstr*, and in later work *merging* SESs. In MS4 Me the corresponding user-level actions are "Prune SES into PES", the PES directives ("select ... from ... for ...!", "restructure multiaspects using ...!", "write class files!"), and "Run PES file in SimViewer".

### Cited Findings
- **Earliest statement of the workflow (Rozenblit & Zeigler 1985), verbatim from the paper's outline:**
  - "the system entity structure is a basic means of organizing a family of possible configurations of the system being designed."
  - "the objectives and requirements of the design project induce appropriate generic experimental frames."
  - "the design entity structure is pruned with respect to the generic frames. This results in a family of design configurations that conform to the design objectives."
  - "the pruned substructures serve as skeletons for generating rules for synthesis of design models."
  - "resulting models are evaluated in respective experimental frames and the best design models are chosen on the basis of such evaluations."

  Fig. 1 shows: objectives → Design Model Construction (Entity Str. Base) → Experimentation via Simulation (Exp. Frame Base) → Evaluation → feedback. — Rozenblit, J.W., Zeigler, B.P. (1985), "Concepts for Knowledge-Based System Design Environments," *Proc. 1985 WSC* (D. Gantz, G. Blais, S. Solomon, eds.), pp. 223–231, DOI 10.1145/21850.253157 — [scanned paper, pp. 223–224](https://www.informs-sim.org/wsc85papers/1985_0048.pdf); [DOI](https://doi.org/10.1145/21850.253157)
- **The prune algorithm and the expert-environment architecture (Rozenblit & Zeigler 1985).** The paper gives "Procedure Prune(E_j, CV_GOF, V_GOF)". It recursively walks aspects and entities, keeping the substructures that "accommodate the generic observation frame GOF," and its output is "a set of design model structures in the form of decomposition trees ... Each such structure ... constitutes a skeleton for a hierarchical model construction." Fig. 4 ("Architecture for Expert System Design Environment") is driven by Objectives:
  - Generic Observation Frames feed "Behavioral Pruning".
  - Synthesis Rules feed "Struct. Pruning".
  - Both act on the Design Entity Structure, which yields "Candidate Model Structures", then "Model Construc[tion]", then "Simulation".

  — [Rozenblit & Zeigler 1985, pp. 226–228](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **"Top-down pruning ... bottom-up synthesis" (Zeigler 1986).** In the flexible-processor (FP) example, the user starts at the root, chooses how many FPs (an attached variable such as `FPS.NUMBER`), and selects a variant for each specialization (F-COMPUTER, COORDINATOR). "Having done top-down pruning of the entity structure to select the desired components in the model base, we now follow bottom-up synthesis to construct the new model." The selected components are coupled per the coupling spec of each decomposition, up to the root. Section heading: "4.0 Entity Structure Pruning: Synthesis of Models." — [Zeigler 1986, p. 136](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **Named functions in DEVS-Scheme (Rozenblit, Kim & Zeigler 1988):**
  - "A user prunes the entity structure obtaining a reduced structure that specifies a hierarchical composition tree. Upon invoking the transform procedure, the system searches the model base for model components specified in the model composition tree and synthesizes the desired model by coupling them together in a hierarchical manner. The result is a discrete event simulation model expressed in DEVS-Scheme which is ready to be executed to perform simulation studies."
  - "The user, whether human or artificial, should be a goal-directed agent which can interrogate the knowledge base and synthesize a model using pruning operations that ultimately reduce the structure to a composition tree."

  — [Rozenblit, Kim & Zeigler 1988, pp. 226, 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **What `transform` does (Rozenblit, Kim & Zeigler 1988), verbatim:** "As it traverses the pruned entity structure, transform calls upon a retrieval process to search for a model of the current entity. If one is found, it is used and transformation of the entity subtree is aborted. Retrieve looks for a model first in working memory, then in model definition files, and finally, provided that the entity is a leaf, in pruned-entity structure files. The latter mode requires an invocation of transform which is executed in a separate Scheme environment so as not to interfere with the parent environment (see Kim, 1988 for greater detail)." Model objects "must reside in working memory in order to be simulated," and are reconstructed "from disk file definitions by direct evaluation (the only possibility for atomic-models) or by applying the transform function to a pruned entity structure in working memory." — [Rozenblit, Kim & Zeigler 1988, p. 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **Software organization (Rozenblit, Kim & Zeigler 1988, Fig. 1).** ESP-4 (Pascal: "Entity Structure & Pruner, Large Entity Structure Management, Model Integration") leads to ESP-SCHEME ("Structure, Prune") plus "Constraint-Driven Pruning", then TRANSFORM ("Synthesis"), then DEVS-SCHEME ("Hierarchical, Modular Discrete Event Modelling and Simulation Environment"), then "Distributed Simulation Architecture". Rule-based pruning ran in the MODSYN shell (Turbo Prolog, "generate and test"), which uses "aspect ordering" and "specialization-oriented pruning" to choose unique entities for model composition trees. — [Rozenblit, Kim & Zeigler 1988, p. 227](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **"Model synthesis via pruning" (Zeigler, Hu & Rozenblit 1989).** "Hierarchical models can [be] synthesized by creating and transforming pruned entity structures. This requires only that the lowest level atomic-models referenced by a pruned entity structure reside in the model base." — [Zeigler, Hu & Rozenblit 1989, p. 86](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Kim et al. (1990) list three operations:** "specification of the structure of a family of models, pruning the structure to a reduced version, and transforming the latter to a simulation model by synthesizing component models in the model base." — [DOI 10.1109/21.59966](https://doi.org/10.1109/21.59966)
- **Restatement in the Java/XML era (Cheon, Kim & Zeigler 2008).** "A pruning operation results in a reduced structure, pruned entity structure (PES) that the SES is pruned to meet design objectives. The PES is eventually synthesized into a simulation model by combining it with models in the model base." — Cheon, S., Kim, D., Zeigler, B.P. (2008), "DEVS Model Composition by System Entity Structure," *2008 IEEE Int. Conf. on Information Reuse and Integration*, pp. 479–484, DOI 10.1109/IRI.2008.4583078 — [DOI](https://doi.org/10.1109/iri.2008.4583078)
- **Pruning to a composition tree whose leaves are system specifications (Rozenblit & Zeigler 1993).** In Knowledge-Based Simulation Design (KBSD), "This selection and synthesis process is called pruning (Rozenblit and Huang 1991). Pruning results in a recommendation for a model composition tree, i.e., a set of hierarchically arranged design components whose models may reside in a design library. Performance of alternative design solutions is studied by associating system specifications (in the form of models) with the components of composition trees." "Pruning the application SES generates a composition tree for the system model specification ... whose leaf nodes are system specifications." The paper also prunes a *canonical SES of system-specification formalisms* (e.g., selecting DEVS as "Formalism-type"). — Rozenblit, J.W., Zeigler, B.P. (1993), "Representing and Constructing System Specifications Using the System Entity Structure Concepts," *Proc. 1993 WSC* (G.W. Evans et al., eds.), pp. 604–611, DOI 10.1145/256563.256742 — [scanned paper, pp. 606–607](https://www.informs-sim.org/wsc93papers/1993_0084.pdf); [DOI](https://doi.org/10.1145/256563.256742)
- **MS4 Me workflow (local User's Guide):**
  - The user writes a natural-language `.ses` file (e.g., "from the Dialog perspective, HelloWorldSES is made of HelloWorld and BackAtYou!" and "... HelloWorld sends Hello to BackAtYou!"), clicks "Prune SES to PES", then "Run PES file in SimViewer" (pp. 17–19). Footnote: "PES abbreviates Pruned Entity Structure. A PES is a pruned SES." (p. 17).
  - The reference section explains: "A SES describes the different aspect and specialization decompositions of a system into its components. Unless there is only one aspect for each entity and there are no specializations, the SES describes a number of possible configurations of the system. A PES specifies one of those configurations that will be used to run a simulation" (p. 168).
  - The process is called "the pruning and transformation process" (p. 132). For MultiEnclaveNet, "After pruning and transformation, MultiEnclaveNet will become a coupled model that contains one or more Enclaves" (p. 126).
  - PES directive vocabulary: `set name of model as X !`, `pruned from X.ses !`, `select FiveKGWeight from ObjectType for PhysicalObject !`, `inherit from FiveKGWeight!`, `restructure multiaspects using index !`, `set multiplicity of index as [5] for Processor !`, `mergeAll from Try.ses !`, `don't select CC under AA!`, `write class files !` (pp. 128–129, 157–158).
  - "View animation for PES" creates models from the PES and opens the coupled model in SimViewer (p. 44).

  — [MS4 Me User's Guide, local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **POSE environment (Rozenblit's group, 1993).** POSE splits construction into "1) Automatic Generation of Experimental Frame (AGEF), 2) Node Modeling, 3) System Modeling, and 4) Model Integration," and uses "the composition tree and system entity structure concepts" plus "the experimental frame formalism." — Chien, L.-P., Rozenblit, J.W. (1993), "An Environment for Automatic System Performance Evaluation," *Proc. 1993 WSC*, p. 582ff (cover page read; DOI not checked) — [scanned paper](https://www.informs-sim.org/wsc93papers/1993_0081.pdf)

### Inferences
- The sequence specify SES → prune (selection) → PES/composition tree → transform (retrieve + couple, bottom-up) → simulate in EF → evaluate/rank → re-prune is consistent across 1985–2008. MS4 Me's "write class files!" plus "Run PES" is the modern counterpart of DEVS-Scheme's `transform`.
- `transform` has a memoization/short-circuit property: if a model for an interior entity already exists in working memory or the MB, the subtree is *not* re-synthesized. This is what makes stored PESs and coupled models reusable as building blocks (see Q6).
- Pruning has two flavors in the 1985–1991 work. *Behavioral* pruning is driven by the generic observation frame. *Structural* pruning is driven by synthesis/selection rules over coupling and selection constraints.

### Gaps
- `inv-transform` (shown in the 1988 Fig. 3) is not described in the text I read. Its semantics (presumably model → PES) are **not confirmed**.
- I did not obtain the precise ESP-Scheme API documentation (Kim 1988 dissertation full text; Kim et al. 1990). The UA repository blocked direct PDF download (HTTP 403).

---

## 3. How do closure under coupling and hierarchical, modular DEVS make synthesis sound?

### Takeaway
Zeigler's argument is that modularity (all interaction goes through ports) plus closure under coupling (a coupled DEVS is again a DEVS with the same kind of interface) guarantees that whatever composition tree pruning produces can be built bottom-up into a valid model. The built model can itself go back into the model base as a component. Systems-isomorphism checks guarantee that replicated copies (e.g., multi-entity instances) behave identically to the original.

### Cited Findings
- **Zeigler (1986):** closure under coupling "enables hierarchical construction of models." "An important benefit of such modular construction is that a model in the model base can be readily, and independently, tested by coupling a test module to it. The ability to do such testing at each stage of a hierarchical construction facilitates reliable and efficient verification of large simulation models, not otherwise attainable. Test modules for models can be developed in a systematic manner using the concept of experimental frame." — [Zeigler 1986, pp. 129–130](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **"Layer 1: Systems Model Specification" of DEVS-Scheme (Zeigler, Hu & Rozenblit 1989):**
  - "Closure under Coupling: Models may be connected together by coupling of input and output ports to create larger, coupled models, having the same interface properties as the components. Hierarchical construction follows as a consequence of modularity and closure under coupling; successively more complex models can be built by using, as building blocks, the coupled models already constructed."
  - "Stand-alone and Bottom-up Testability: ... models are independently verifiable at every stage of hierarchical construction. This fosters secure and incremental bottom-up synthesis of complex models."
  - "Isomorphic Replicatability: Copies can easily be made of complex, hierarchical models ... Systems isomorphism concepts provide the formal basis for correctness of model replication."
  - "System Theoretic Formal Basis: The DEVS formalism ... provides a sound semantics for discrete event model representation."
  - Layer 2 depends on Layer 1: "Layer 2 relies on Layer 1 to provide the ability to specify models which populate the model base that Layer-2 organizes."

  — [Zeigler, Hu & Rozenblit 1989, pp. 84–85](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Isomorphism-checked copying (Rozenblit, Kim & Zeigler 1988).** "To test the methods for creating copies of models we employ a novel approach: we implemented a parallel set of methods for checking isomorphism between models ... For a copying method to be valid, a copy of a model must be isomorphic to the original as determined by the isomorphism test." The copying methods are `make-new` (recursive for coupled models) and `make-class`. — [Rozenblit, Kim & Zeigler 1988, pp. 228–229](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **Kim & Zeigler (1987)** describe DEVS-Scheme in SCOOPS (PC-Scheme): the taxonomy of classes with kernel models (broadcast, hypercube, cellular), and "implementation of isomorphism checks between pairs of models." — Kim, T.G., Zeigler, B.P. (1987), "The DEVS Formalism: Hierarchical, Modular Systems Specification in an Object Oriented Framework," *Proc. 1987 WSC*, pp. 559–566, DOI 10.1145/318371.318665 — [DOI](https://doi.org/10.1145/318371.318665); [archive PDF](https://www.informs-sim.org/wsc87papers/1987_0080.pdf) (archive index lists this file for the paper; I did not read the scan)
- **Coupled system structure (Rozenblit & Zeigler 1993).** They restate the coupled-system structure N = <D, {S_a}, {I_a}, {Z_a}> (from Zeigler 1984): component systems S_a, influencer sets I_a and interface maps Z_a. NET ("System Specification NET (Coupled System)") is "the basis for a hierarchical form of model construction." — [Rozenblit & Zeigler 1993, pp. 605, 607](https://www.informs-sim.org/wsc93papers/1993_0084.pdf)
- **Zeigler (1987, SIMULATION):** "Hierarchical, modular specification of discrete-event models offers a basis for reusable model bases and hence for enhanced simulation of truly varied design alternatives ... The environment is developed in an object-oriented manner which lends itself to model base organization using the entity structure knowledge representation." — Zeigler, B.P. (1987), "Hierarchical, Modular Discrete-Event Modelling in an Object-Oriented Environment," *SIMULATION* 49(5):219–230, DOI 10.1177/003754978704900506. Crossref also holds a duplicate deposit, 10.1177/003754978904900506, with identical metadata. — [DOI](https://doi.org/10.1177/003754978704900506)
- **A reported cost of modularity (Zeigler 1986).** "While the modularity property is a highly desirable one for the flexible construction of models, some work must be put into preparing model descriptions appropriately" (spring-coupling example, where positions and forces at both ends must be made explicit). — [Zeigler 1986, p. 131](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)

### Inferences
- The soundness argument is constructive, not a separate theorem about pruning. Every node of a PES composition tree is either a leaf (an atomic or stored model in the MB) or an aspect with a coupling spec. Closure under coupling makes each interior node a well-defined DEVS. So induction from the leaves upward yields a well-defined root model. Pruning only has to guarantee that the leaves exist in the MB, which is the "only requirement" noted in the 1989 paper.
- Multi-entity replication (copies of a multi-entity) is made trustworthy by isomorphism checks, not by closure alone.

### Gaps
- The formal closure-under-coupling proof (DEVS coupled → DEVS atomic) lives in Zeigler's *Theory of Modeling and Simulation* editions. I did not open those texts, so no page-level citation is given. Per the assignment, the formal-SES/DEVS-theory researcher covers this.

---

## 4. How do experimental frames (EFs) fit in? Are EFs pruned from the SES too? What roles do objectives, requirements and constraints play in pruning?

### Takeaway
In Rozenblit and Zeigler's knowledge-based system design (KBSD/KBDSE) methodology, design objectives and requirements are expressed as *generic experimental frames* (generic observation frames: sets of generic input/output variable types). These frames *drive* pruning: the SES is pruned "with respect to" them. Objectives, requirements and constraints are also encoded as production rules: *selection rules* attached to specializations and *synthesis rules* attached to aspects/decompositions. After pruning, the generic frame is instantiated into an actual EF (generator, transducer, acceptor models in the MB) and coupled to the synthesized model for evaluation. In DEVS-Scheme's Layer 3, EFs were generated automatically from performance-index expressions. In MS4 Me practice, EF components (generators, transducers) are simply entities inside the same SES and are pruned with the model. So the answer is "both": EFs guide pruning, and EF structures can themselves be organized and pruned via entity structures.

### Cited Findings
- **Generic EF as the representation of objectives (Rozenblit & Zeigler 1985).** "The generic experimental frame is a structure that represents a set of design objectives in the form of standard variable types. Such standard variable types express measures of input/output performance, utilization of resources, reliability assessments etc." The paper defines the generic observation frame "GOF={IG, OG}," where IG/OG are sets of generic input/output variable types. Benefits of pruning against generic frames:
  - "a generic frame extracts only those substructures which conform to the design objectives."
  - "partial models of the design can be formulated and evaluated."
  - Evaluation is performed in EFs "generated by instantiating the generic frames used to prune the system entity structure."
  - Generic frames "automatically determine: a) the aspects that are selected for each entity, b) the depth of the pruning process, c) the descriptive variables of components."

  — [Rozenblit & Zeigler 1985, pp. 223, 226](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **Constraint taxonomy for synthesis (Rozenblit & Zeigler 1985).** The paper distinguishes "objectives-derived constraints", "standards" (e.g., government or industry), "resource constraints", "natural constraints" (laws of nature) and "syntactic constraints". Constraints are either "convertable to active form" (rules with an operator NEXT_IN_Ci and applicability predicate A_i) or "inherently passive" (tested, with backtracking). "The canonical rule scheme for the synthesis problem" is given in Fig. 3.
  - Car-design example: pruning by the frame "Gasoline Consumption" removes the Service aspect and the Electrical/Steam engines, because their variables are not in the frame. A Department of Transportation *standard* prunes out 2-cycle engines. The resulting PES (Fig. 6) is then coupled with the refined EF "Gasoline Consumption" for simulation (Fig. 8).

  — [Rozenblit & Zeigler 1985, pp. 227–230](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **Objectives drive three processes (Rozenblit, Kim & Zeigler 1988).** "Design objectives (understood here in a broader context that includes requirements and constraints impinging the design process) drive three fundamental processes in the methodology."
  - Coupling constraints are attached to aspects. "Selection constraints are associated with specializations of an entity" ("constraints-driven model structure generation").
  - "The design objectives also serve as a basis for the specification of the generic observation frames and experimental frames ... Generic frames consist of input, output, and summary generic variable types ... Experimental frames are instantiated generic frames wherein variable types are associated with model components and execution run conditions are defined in experiment initialization, continuation, and termination sets."
  - Rule sets: a "Selection rule set: each selection rule stands for a choice of an entity in a specialization" and a "Synthesis rule set: after selection rules have been applied ... synthesis rules ensure proper configuration of the selected entities," with certainty factors.

  — [Rozenblit, Kim & Zeigler 1988, pp. 226–227](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **"Layer 3: Systems Design" of DEVS-Scheme (Zeigler, Hu & Rozenblit 1989):**
  - "Goal Driven Pruning: ... Selection rules attached to specializations specify entity choices in terms of local design criteria. Synthesis rules attached to decompositions constrain the plausible combinations of model components ... forward, backward, and weight-oriented chaining."
  - "Hierarchical Design Specification: Design requirements including constraints, objectives, and criteria preference can be specified and attached to each corresponding entity at different design abstraction levels."
  - "Automatic Performance Modeling: ... Layer-3 ... supports automatic generation of experimental frames. By providing the algebraic expression of a defined performance index, atomic frames are extracted automatically and aggregated into an experimental frame model which can be coupled to the design model."
  - "Automated Design Evaluation and Ranking: Design models synthesized via pruning are automatically subjected to simulation evaluation in performance-relevant experimental frames and ranked according to multi-criteria decision methods."
  - This layer is implemented via FRASES ("Frames and Rules Associated System Entity Structure"), with Knowledge Acquisition by Representation (KAR) dialogs that attach EIF slots to SES nodes.

  — [Zeigler, Hu & Rozenblit 1989, pp. 86–89](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Rule-based model-structure generation (Rozenblit & Huang 1991).** "A knowledge base of production rules is defined for a given design domain and is incorporated in an expert system shell which recommends a feasible configuration of design objects from the system entity structure, based on specific design objectives, constraints, and requirements. This configuration is a basis for constructing a model of the system." The case study is a robot design problem. — Rozenblit, J.W., Huang, Y.M. (1991), "Rule-Based Generation of Model Structures in Multifaceted Modeling and System Design," *ORSA J. on Computing* 3(4):330–344, DOI 10.1287/ijoc.3.4.330 — [DOI](https://doi.org/10.1287/ijoc.3.4.330)
- **Precursors.**
  - Rozenblit & Huang (1987), "Constraint-driven generation of model structures," *Proc. 1987 WSC*, pp. 604–611, DOI 10.1145/318371.318671: SES plus production rules in Turbo Prolog, with a LAN case ([DOI](https://doi.org/10.1145/318371.318671)).
  - Rozenblit, Sevinc & Zeigler (1986), "Knowledge-based design of LANs using system entity structure concepts," *Proc. 1986 WSC*, pp. 858–865, DOI 10.1145/318242.318532: "the LAN design domain is subsequently restricted by pruning the entity structure with respect to network design objectives. A LAN model is then synthesized using a production rule scheme" ([DOI](https://doi.org/10.1145/318242.318532)).
- **KBDSE journal paper (title and metadata verified; abstract not retrieved).** Rozenblit, J.W., Hu, J., Kim, T.G., Zeigler, B.P. (1990), "Knowledge-Based Design and Simulation Environment (KBDSE): Foundational Concepts and Implementation," *J. Operational Research Society* 41(6):475–489, DOI 10.2307/2583032 (JSTOR). It was reprinted in *Artificial Intelligence in Operational Research* (Macmillan, 1992), pp. 261–275, DOI 10.1007/978-1-349-12362-9_25. — [DOI](https://doi.org/10.2307/2583032)
- **EFs and frame applicability (Zeigler & Ören 1986).** "The concepts of (generic) experimental frame and applicability of frames to models have been introduced to link the objectives with the models." "The methodologically desirable decomposition into model and experimental frame segments is independent of model formalism." — Zeigler, B.P., Ören, T.I. (1986), "Multifacetted, Multiparadigm Modelling Perspectives: Tools for the 90's," *Proc. 1986 WSC*, pp. 708–712, DOI 10.1145/318242.318513 — [scanned paper, pp. 708, 710](https://www.informs-sim.org/wsc86papers/1986_0108.pdf); [DOI](https://doi.org/10.1145/318242.318513)
- **EF components as SES entities in MS4 Me.**
  - The "EFP Example" shows a "System Entities Structure for an EFP model" where "EFP consists of a coupled model (EF) and an atomic model (Proc)", and "The Proc model can be a special case between fast and slow processes" (pp. 53–54).
  - The EFPwithMultiAspect examples put GeneratorOfJobs, multiple Processors and Transducer in one SES. Couplings include "Transducer sends Stop to GeneratorOfJobs" and "all Processor sends outJob to Transducer" (pp. 126–128).
  - The Transducer "keeps track of jobs and computes turnaround time and throughput" and "can be coupled into the SimpleWorkFlow model" (§III.B, around pp. 144–148).

  — [MS4 Me User's Guide, local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **EF-aware pruning in later work (title only; content paywalled).** Zeigler & Sarjoughian (2017), ch. 8 "Automated and Rule-Based Pruning and Experimental Execution" (pp. 107–121). The 2013 edition's ch. 8 was titled "Automated and Rule-Based Pruning" (pp. 93–102). The 2017 retitling adds "Experimental Execution". — [Crossref 2017 chapter list](https://api.crossref.org/works?filter=isbn:9783319641348)
- **Cited by Zeigler's group but not seen by me (UNVERIFIED).** Rozenblit, J.W., Zeigler, B.P. (1986), "Entity-Based Structures for Model and Experimental Frame Construction," in *Modelling and Simulation Methodology in the Artificial Intelligence Era* (M.S. Elzas et al., eds.), North-Holland. It appears in the reference list of Rozenblit, Kim & Zeigler 1988, p. 229 ([scan](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)). Its title indicates EF construction via entity structures. **UNVERIFIED**: not checked against a publisher or DOI record.
- **Local unpublished manuscript (treat with caution).** The `.docx` in `/home/chris/DEVS/devs-skills-claude/docs/efsut/` is headed "Submitted to: Digital Engineering and Digital Twin," by Bernard P. Zeigler (RTSync / University of Arizona). An appended section, "EFSUT-Driven DEVS Modeling for Processor–Device Digital Twins," describes an SES "contain[ing] two aspects: EF ∈ {EF_MMIOWrite, EF_IRQGeneration, …}, Model ∈ {M_RouterMemory, …}". Its "Pruning rules enforce a one-to-one mapping" (EF → Model) and yield "ten PES instances." It says "Pruned SES ensures no overmodeling."
  - Caveats: the manuscript is unpublished. This appended section reads like a draft. It expands EFSUT as "Entities, Functions, Structure, Usage, Time," which is inconsistent with the main manuscript's "Experimental Frame–System Under Test."
  - Status: **UNVERIFIED / not peer reviewed**. It is cited only to show that pruning EF/model pairs from one SES appears in current Zeigler-group drafts. — local file `/home/chris/DEVS/devs-skills-claude/docs/efsut/Three Complementary Uses of the EFSUT Methodology.docx`

### Inferences
- The EF plays two roles. Before pruning it is a filter: a generic observation frame selects which substructures matter. After pruning it is an executable model, coupled to the synthesized model and drawn from the MB or frame base.
- Both roles rest on the same EF/model separation and port-based coupling. That is why Zeigler et al. can treat EF construction with the same entity-structure machinery as models (1986 "entity-based structures for model and experimental frame construction"; 1989 automatic EF generation; MS4 Me EF-in-SES examples).
- Objectives enter pruning in two ways. Behavioral objectives enter through generic frames. Structural objectives and constraints (standards, resources, physical laws, syntax) enter through selection and synthesis rules.

### Gaps
- I did not read the Rozenblit (1985) dissertation, "A Conceptual Basis for Integrated, Simulation Based System Design" (Wayne State), or the 1986 "Entity-Based Structures ..." chapter. Both appear only as references cited in Zeigler-group papers.
- I could not confirm whether the 2017 Guide chapter 8 prunes EF configurations from the SES or only automates experiment execution over PES sets. The chapter is paywalled.

---

## 5. What is the "multifacetted modeling" methodology, and how does SES/MB implement it?

### Takeaway
Multifacetted modeling (Zeigler 1984) accepts that no single model captures a real system. It aims to build many partial models, each tied to particular objectives through experimental frames, at different aggregation levels and in different formalisms. These models are organized in a model base so that their partial perspectives can be integrated coherently. The SES is the organizing structure: decompositions, taxonomies and couplings over a family of models. The MB holds the model components. Pruning and transformation are the operations that pick out and assemble the model appropriate to a given objective or frame.

### Cited Findings
- **Definition by Zeigler and Ören (1986), verbatim.** "Multifacetted modelling methodology (Zeigler, 1984) signifies an approach to simulation modelling that recognizes the irreducible complexity of reality, while affirming that useful partial models can be constructed in the service of limited decision making objectives. Essential to the methodology is its support of an organized base of models whose partial perspectives can be integrated to achieve a coherent whole." The facets listed are multiple objectives, multiple levels of aggregation, multiple levels of behavior and structure, multiple formalisms, and multiple programming paradigms. — [Zeigler & Ören 1986, pp. 708–709](https://www.informs-sim.org/wsc86papers/1986_0108.pdf)
- **Environment support (Zeigler & Ören 1986).** "To gain full advantage of the knowledge in the model base, there must be provided a strong capability of representing the components of models, their variations and their interconnection. To facilitate synthesis, models must be readily dissembled into components, and these must be able to be easily assembled into new combinations, i.e., the environment should support modular, hierarchical model construction." They also write: "Object-oriented programming may be best to develop the knowledge representation data structures (system entity structures) for model organization. Logic programming may be most natural for model synthesis since such programs can readily generate the hierarchical compositions spanned by an entity structure, filtering out those that do not satisfy the coupling constraints." — [Zeigler & Ören 1986, pp. 709–710](https://www.informs-sim.org/wsc86papers/1986_0108.pdf)
- **Multifacetted design framework (Rozenblit & Zeigler 1985).** The goal is "to embed system design within the multifacetted modelling framework," and the paper lists what the framework provides: "mechanisms for model decomposition, hierarchical specification and aggregation of partial models," "a spectrum of performance evaluation methods," and representation schemes with "formalized operations that can exploit such structures." The paper credits Zeigler [18] with the "expert system design methodology" built on "the system entity structure and experimental frame." — [Rozenblit & Zeigler 1985, p. 223](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **The SES generalizes the composition tree (Zeigler 1986).** "By generalizing the composition tree concept so as to represent not just a single model, but a family of possible models, we arrive at the concept of system entity structure." "We use the term entity rather than model, since the entity may have several possible models to represent it. We also refer to a decomposition as an aspect since there may be several possible decompositions for a given entity." Multiple entities connected by "three vertical lines" form a "multiple aspect." — [Zeigler 1986, p. 135](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **The 1984 book (verified).** Zeigler, B.P. (1984), *Multifacetted Modelling and Discrete Event Simulation*, Academic Press, London and Orlando, ISBN 0-12-778450-0. Verified via the [Open Library ISBN record](https://openlibrary.org/isbn/0127784500). It was reviewed by J.R. Beaumont in *J. Operational Research Society* 36(6):545 (1985), DOI 10.2307/2582832 ([Crossref/JSTOR DOI](https://doi.org/10.2307/2582832)). I did not read the book itself.
- **Early implementation lineage (cited in Zeigler-group reference lists; UNVERIFIED by me):**
  - Zeigler, B.P., Belogus, D., Bolshoi, A. (1980), "ESP – An Interactive Tool for System Structuring," *Proc. 1980 European Meeting on Cybernetics and Systems Research*, Hemisphere Press.
  - Belogus, D. (1985), "Multifacetted Modelling and Simulation: A Software Engineering Implementation," doctoral dissertation, Weizmann Institute of Science.

  Both are listed in the reference lists of [Rozenblit & Zeigler 1985, pp. 230–231](https://www.informs-sim.org/wsc85papers/1985_0048.pdf) and [Rozenblit, Kim & Zeigler 1988, p. 230](https://www.informs-sim.org/wsc88papers/1988_0033.pdf). **UNVERIFIED**: not checked against publisher or DOI records.

### Inferences
- SES/MB is the concrete data architecture of multifacetted modeling:
  - "organized base of models" maps to the MB.
  - "representing the components of models, their variations and their interconnection" maps to the SES (aspects, specializations, couplings).
  - "objectives" map to (generic) EFs.
  - "synthesis" maps to pruning plus transform.
- The 1986 remark that logic programming suits synthesis "filtering out those that do not satisfy the coupling constraints" anticipates the Prolog-based rule-driven pruning (MODSYN, 1987–1988).

### Gaps
- The 1984 book was not accessible, so the original chapter-level definitions of SES operations and multifacetted methodology are not quoted from it directly.

---

## 6. How does the framework support model reuse, families of models, and design-space exploration?

### Takeaway
Reuse comes from modular MB components, plus stored PESs serving as named, retrievable alternatives, plus `transform` short-circuiting to existing models. Families come from the SES as a generative representation of all models synthesizable from MB components. Exploring alternatives uses *specialization* (choosing variants). Exploring numbers uses *multiple entities / multi-aspects* (choosing how many copies) and attached variables (e.g., `FPS.NUMBER`). Selection and synthesis rules prune infeasible combinations. Later, MS4 Me added partial PESs with random completion, resumable pruning, and SES merging for model suites.

### Cited Findings
- **The knowledge base as a generative family (Rozenblit, Kim & Zeigler 1988).** "The knowledge base framework ... is intended to be generative in nature, i.e., it should be a compact representation scheme which can be unfolded to generate the family of all possible models synthesizable from components in the model base." — [p. 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **The SES as compact family specification (Rozenblit & Zeigler 1993).** "The interaction of decomposition, coupling and taxonomic relations in an SES affords a compact specification of a family of models for a given domain. In a system entity structure, entities refer to conceptual components of reality for which models may reside in a library (model base)." "Typically, the SES is employed to specify families of design simulation models, generated by pruning a master SES, for a given application domain." — [pp. 606](https://www.informs-sim.org/wsc93papers/1993_0084.pdf)
- **Alternatives and numbers in one structure (Zeigler 1986, FP example).** The user picks how many Flexible Processors via a variable on the multiple entity ("FPS.NUMBER"). Each FP is decomposed into EXECUTIVE, SUPERVISORY, BUFFER and F-COMPUTER, with an F-COMP specialization (FULL, L-HALF, R-HALF, COMPILER). SUPERVISORY decomposes into SUPERVISOR plus COORDINATOR, with a COORD specialization (MS, D&C, PL). "A specialization indicates that a model class contains both a generic description, as well as specialized descriptions that can generate instances of several variants." — [Zeigler 1986, pp. 135–136](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **Synthesis constraints (Zeigler, Hu & Rozenblit 1989).** "The system entity structure can be augmented with rules that enforce constraints on selections from specializations. Such constraints can be of a global character where selections in various parts of the SES must be correlated. They also can be sensitive to the context in which the selection is being made. This supports coherence of the entity structure base." — [p. 86](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Reuse and design alternatives (Zeigler, Luh & Kim 1990).** "...generate a family of design alternatives, reusability of models, and model base coherence and evolvability ... the pruning process supports reuse of previously pruned structures." — [DOI 10.1109/aihas.1990.93938](https://doi.org/10.1109/aihas.1990.93938)
- **Variable family and variable structure (Kim 1988).** "Since structure descriptions in the environment are susceptible to run-time modification, the environment provides a convenient basis for developing variable family and variable structure simulation models such as adaptive computer architectures." — [UA OAI record](https://repository.arizona.edu/server/oai/request?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:repository.arizona.edu:10150/184380)
- **MS4 Me mechanisms for exploration:**
  - Multi-aspect: "The concept of multi-aspect is intended to address the limitation of having to explicitly declare decompositions having different numbers of entities". A multi-aspect "has only one child called its multi-entity – which can generate a specified number of copies (Zeigler and Hammonds)." Copies are generated via `restructure multiaspects using index!` and `set multiplicity of index as [3] for Person!`, giving id0_Person, id1_Person, ... (pp. 120–121).
  - "It is permissible, and desirable, to use the same specialization (here index) for different multi-aspects in the SES, where the multiplicity can be differentially specified in the pruning file" (p. 126).
  - Uniform coupling forms over copies: "sends ... to all", "all ... sends ...", "each Boy sends Invitation to each Girl" with name-prefix matching (pp. 123–124, 128).
  - Pruning: "Testing different system configurations with pruning". "When running a simulation from a partial PES, any unmade choices will be randomly selected". "Resume pruning from a previously created PES" (p. 168).
  - "Choices not made explicitly in the pruning file are made randomly" (p. 122).

  — [MS4 Me User's Guide, local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **Model suites by SES merging (Zeigler, Seo & Kim 2013).** "The approach employs the operation of merging of System Entity Structures ... We then consider the concept of multiple aspects that provides more advanced capabilities to construct and manipulate suites of models." The example is a health-care model suite. — [DOI 10.1142/s1793962313400060](https://doi.org/10.1142/s1793962313400060)
- **Model families in theory (title only).** Zeigler, Muzy & Kofman, *Theory of Modeling and Simulation*, 3rd ed. (Academic Press/Elsevier; Crossref dates the chapters 2019, while the edition is commonly dated 2018) has a chapter titled "Abstraction: Constructing Model Families" (pp. 405–443). I did not read it, so its relation to SES is **not verified**. — [Crossref chapter list, ISBN 9780128133705](https://api.crossref.org/works?filter=isbn:9780128133705)

### Inferences
- The design space is exactly the set of PESs obtainable from one SES, filtered by rules and frames. Specialization gives *qualitative* alternatives. Multi-aspect gives *cardinality* alternatives. Attached variables give *parametric* alternatives.
- Random completion of partial PESs (MS4 Me) is a simple design-space *sampling* mechanism. The DEVS-Scheme-era Layer 3 provided *ranking* via multi-criteria evaluation in EFs.

### Gaps
- No primary source that I read reports quantitative reuse metrics (e.g., the fraction of components reused across projects). Reuse benefits are asserted qualitatively.

---

## 7. Which historical implementations realized SES/MB, and what did each contribute?

### Takeaway
The lineage, based on what each source documents:
- **ESP / ESP-4** (1980; Pascal on VAX/VMS by 1986): entity structuring and pruning.
- **DEVS-Scheme** (Zeigler & Kim, 1986–1990; PC-Scheme/SCOOPS): hierarchical modular DEVS, the four-layer architecture, and transform from PES to executable models.
- **ESP-Scheme** (Kim et al., 1988–1990): the SES as model base management system, with prune/transform.
- **FRASES / MODSYN** (Hu; Huang/Rozenblit): rule-driven, goal-directed pruning and automatic EF generation.
- **Java/XML-era SESBuilder** (Cheon, Kim & Zeigler 2008): natural-language SES input, XML PES, couplings for composing DEVS models.
- **DEVS/SOA** (Mittal, Risco-Martín & Zeigler 2009): run-time composition of coupled DEVS services; SES used on top of it for distributed analysis (Kim, Seo & Zeigler 2009).
- **MS4 Me** (MS4 Systems): natural-language `.ses`/`.pes`, multi-aspect restructuring, inheritance management, merging, generated Java, SimViewer; plus the Model Store cloud repository.

For DEVS-C++ and DEVSJAVA I found no primary source describing SES/MB support specifically.

### Cited Findings
- **ESP-4 (Pascal).** "The entity structure methodology is supported by ESP-4 (entity structurer and pruner), a PASCAL system available for VAX under VMS and being ported to MS-DOS environments." — [Zeigler 1986, p. 137](https://www.informs-sim.org/wsc86papers/1986_0019.pdf). The 1988 Fig. 1 lists ESP-4's functions as "Entity Structure & Pruner, Large Entity Structure Management, Model Integration." — [Rozenblit, Kim & Zeigler 1988, p. 227](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **DEVS-Scheme: four layers (Zeigler, Hu & Rozenblit 1989).**
  - Layer 0: Lisp-based, object-oriented foundation.
  - Layer 1: System Model Specification (DEVS, modularity, closure, EF/model separation, isomorphic replication, hierarchical distributed simulation, multi-formalism, rule-based "forward-models").
  - Layer 2: System Entity Structure/Model Base (axiomatic specification, synthesis constraints, model synthesis via pruning, archivability, cataloguing, reusability).
  - Layer 3: Systems Design (FRASES, KAR, goal-driven pruning, automatic EF generation, automated ranking).

  "DEVS-Scheme was developed at the University of Arizona as a product of research funded by the National Science Foundation. It is currently being used to design autonomous robots for laboratory automation in a project sponsored by the AI Research Center, NASA Ames. The environment is available for academic and industrial use and is being commercialized by SIMEX." — [pp. 84–89](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **DEVS-Scheme origin (Kim & Zeigler 1987).** "This paper describes a realization of DEVS ... formalism in an object-oriented programming environment, SCOOPS of PC-Scheme. The realization, DEVS-Scheme ... supports hierarchical, modular specification of discrete event models." — [DOI 10.1145/318371.318665](https://doi.org/10.1145/318371.318665). Zeigler (1987, SIMULATION) gives the PC-Scheme environment and notes it "lends itself to model base organization using the entity structure knowledge representation." — [DOI 10.1177/003754978704900506](https://doi.org/10.1177/003754978704900506)
- **ESP-Scheme (Kim et al. 1990).** "a realization of the SES in Scheme (a Lisp dialect), that is called ESP-Scheme ... the computer representation of SES and main operations on SES are presented ... ESP-Scheme acts as a model base management system in DEVS-Scheme." — [DOI 10.1109/21.59966](https://doi.org/10.1109/21.59966). The 1988 paper adds that MODSYN was being ported from Turbo Prolog "to a Scheme environment" as a front end for DEVS-Scheme. — [p. 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **Dissertations behind the tools (cited in Zeigler, Hu & Rozenblit 1989, p. 89):**
  - Kim, T.G. (1988), DEVS-Scheme/ESP-Scheme knowledge-based environment. This one is verified via the [UA OAI record](https://repository.arizona.edu/server/oai/request?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:repository.arizona.edu:10150/184380).
  - Hu, J. (1989), "Towards a Knowledge-Based Design Support Environment for Design Automation and Performance Evaluation," Ph.D. dissertation, University of Arizona (FRASES). **UNVERIFIED** beyond the citation.
  - Huang, Y.M. (1987), "Building an Expert System Shell for Model Synthesis in Logic Programming," M.S. thesis, University of Arizona (MODSYN). Cited in [Rozenblit, Kim & Zeigler 1988, p. 229](https://www.informs-sim.org/wsc88papers/1988_0033.pdf); **UNVERIFIED** beyond the citation.
- **The 1990 book as the DEVS-Scheme compendium.** Zeigler (1990), *Object-Oriented Simulation with Hierarchical, Modular Models: Intelligent Agents and Endomorphic Systems*, Academic Press, Boston, verified via the [Open Library ISBN record](https://openlibrary.org/isbn/9780127784526). Chapter "DEVS Formalism and DEVS-Scheme," pp. 41–67, per the [Crossref chapter list](https://api.crossref.org/works?filter=isbn:9780127784526).
  - Discrepancy: Zeigler, Hu & Rozenblit (1989, p. 89) cite a forthcoming book as "Hierarchical Modular DEVS: Model Knowledge and Endomorphy in Object-Oriented Simulation," Academic Press, 1989. This appears to be the pre-publication title of the 1990 book. That identification is an **inference**.
- **DEVS-C++ (Zeigler, Moon, Kim & Kim 1996).** A high-performance environment for "large-scale, high-resolution landscape systems ... an implementation of discrete event system specification (DEVS) using container classes with C++." The abstract does not mention the SES or the model base. — Zeigler, B.P., Moon, Y., Kim, D., Kim, J.G. (1996), *Proc. HICSS-29*, vol. 1, pp. 350–359, DOI 10.1109/HICSS.1996.495481 — [DOI](https://doi.org/10.1109/hicss.1996.495481)
- **SESBuilder: Java/XML SES (Cheon, Kim & Zeigler 2008).** "SES is implemented in XML Metadata using Java language and Sun's Document Object Models (DOM) specification. The SESBuilder software supports natural language input for an SES definition and XML instance generation of its PES. The coupling information expressed in natural language with restricted syntax processed by SESBuilder is used to compose models." Example: compositions of DEVS generator models representing US Climate Normals. — [DOI 10.1109/iri.2008.4583078](https://doi.org/10.1109/iri.2008.4583078)
- **DEVS/SOA (Mittal, Risco-Martín & Zeigler 2009).** It implements DEVS over a service-oriented architecture and "supports a development and testing environment known as DEVS Unified Process that is built on a model continuity-based lifecycle methodology ... This framework also provides the crucial feature of run-time composability of coupled systems using SOA." The abstract does not mention the SES. — Mittal, S., Risco-Martín, J.L., Zeigler, B.P. (2009), *SIMULATION* 85(7):419–450, DOI 10.1177/0037549709340968 — [DOI](https://doi.org/10.1177/0037549709340968)
- **SES over DEVS/SOA (Kim, Seo & Zeigler 2009).** Network-traffic analysis "is achieved by applying System Entity Structure (SES) theory. To speed up evaluation time, a web-based distributed simulation approach over Service-oriented Architecture (SOA) is applied. Discrete Event System Specification/Service-oriented Architecture (DEVS/SOA) is used to deploy workloads into multi-servers." — Kim, T., Seo, C., Zeigler, B.P. (2009/2010), "Web-based Distributed Network Analyzer using a System Entity Structure over a Service-oriented Architecture," *SIMULATION* 86(3):155–180, DOI 10.1177/0037549709354112. Crossref's issued date is 2009-11-12, online-first; the issue is vol. 86 (2010). — [DOI](https://doi.org/10.1177/0037549709354112)
- **SES for data engineering (Zeigler & Hammonds 2007).** *Modeling & Simulation-Based Data Engineering: Introducing Pragmatics into Ontologies for Net-Centric Information Exchange*, Academic Press, 2007 (ISBN 978-0-12-372515-8). Title, subtitle, publisher and date (August 3, 2007) are from the [Open Library ISBN record](https://openlibrary.org/isbn/9780123725158). The authors (Zeigler, Hammonds) come from a library catalog search result ([USI library record](https://library.usi.edu/record/448195)) and the MS4 Me guide footnote 11 (p. 120), which cites it as introducing multi-aspects. I did not read its content. A search snippet suggesting the book covers SES-based XML schema generation and pruning-rule testing is **UNVERIFIED**.
- **MS4 Me (MS4 Systems, Inc.):**
  - Natural-language SES editor with templates for aspect, specialization, coupling (with renaming) and variable ranges, plus an SES tree view (pp. 164–168).
  - Interactive pruning GUI: select aspect/specialization, "Selections Made"/"Unselect", "Finish Pruning", partial PES, resume pruning (p. 168).
  - PES directives covering selection, inheritance, multi-aspect restructuring, merge and class-file generation (pp. 121–129, 157–158).
  - Generation of Java classes ("write class files!") and execution/animation in SimViewer (pp. 19, 44, 132).
  - Atomic DEVS natural language (`.dnl`) for MB behaviors.
  - Dynamic-structure examples that start from an SES (AgentAndActor, p. 118).

  — [MS4 Me User's Guide, local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **MS4 Me in the Guide (chapter titles; content paywalled).** Zeigler & Sarjoughian (2013; 2nd ed. 2017), *Guide to Modeling and Simulation of Systems of Systems* (Springer, Simulation Foundations, Methods and Applications) devotes chapters to "DEVS Integrated Development Environments," "System Entity Structure Basics," "DEVS Natural Language Models and Elaborations," "Specialization and Pruning," "Aspects and Multi-aspects," "Managing Inheritance in Pruning," and "Automated and Rule-Based Pruning [and Experimental Execution]." — [Crossref 2013 list](https://api.crossref.org/works?filter=isbn:9780857298652); [Crossref 2017 list](https://api.crossref.org/works?filter=isbn:9783319641348); [2017 book DOI](https://doi.org/10.1007/978-3-319-64134-8)
- **Model Store (Zeigler, Seo & Kim 2013).** A cloud repository "provided by MS4 systems" for sharing model suites built by SES merging. — [DOI 10.1142/s1793962313400060](https://doi.org/10.1142/s1793962313400060)

### Inferences
- The essential SES/MB division of labor has stayed stable from DEVS-Scheme to MS4 Me: an SES tool (ESP-Scheme → SESBuilder → MS4 Me SES editor) produces a PES, and a DEVS engine (DEVS-Scheme → DEVSJAVA/DEVS-Suite lineage → MS4 Me/MS4DEVSJava) synthesizes and simulates it. What changed is the surface: Lisp frames → XML → restricted natural language, plus added constructs (multi-aspect restructuring, uniform coupling forms, SES merging, inheritance directives).
- The rule-driven "Layer 3" design automation of the late 1980s (FRASES, MODSYN, automatic EF generation, multi-criteria ranking) has no fully documented counterpart in the MS4 Me guide I read. That guide documents interactive or file-based pruning with random completion. The 2017 chapter "Automated and Rule-Based Pruning and Experimental Execution" suggests a partial revival, but its content is unverified.

### Gaps
- **DEVSJAVA:** I found no primary Zeigler-group source in this session describing DEVSJAVA's own SES/pruning/transform facilities. The only Java-era SES evidence I verified is SESBuilder (2008) and MS4 Me. The report writer should not claim DEVSJAVA contributed specific SES/MB features without another source.
- **DEVS-C++:** the abstract I verified shows no SES/MB contribution. Its role appears to have been high-performance simulation, not SES/MB.
- **ESP-Scheme name variants:** the task brief mentions "ESP-Scheme" as a separate system. The sources confirm it was the SES component *of* DEVS-Scheme ("Layer 2"), not a standalone simulator.

---

## 8. What benefits and limitations did Zeigler and collaborators themselves report?

### Takeaway
The originators claim these benefits:
- reusable, independently testable model components that accumulate as an "off-the-shelf" library;
- compact generative representation of whole design families;
- automatic synthesis from pruned structures;
- objective-driven model scoping via experimental frames;
- automatic rule generation from well-designed structures;
- support for variable-structure models and distributed simulation.

Their own reported limitations and costs are:
- effort needed to make descriptions modular;
- generality of the knowledge-based design environment being "a long way off" (1985);
- model-base coherence and evolvability as open problems requiring context-sensitive pruning and partitioned entity structure bases (1990–91);
- the strict-hierarchy axiom needing relaxation for recursive representations (1993);
- in MS4 Me, explicit inheritance directives, random completion of partial PESs, coupling gaps when multi-aspect matches fail, and ordering constraints;
- continued development needed before model marketplaces see common use (2013).

### Cited Findings
- **Benefit: independent, bottom-up verification.** "The ability to do such testing at each stage of a hierarchical construction facilitates reliable and efficient verification of large simulation models, not otherwise attainable." — [Zeigler 1986, p. 130](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **Benefit: accumulating reusable components.** "This fosters accumulative growth of 'off-the-shelf' reusable components." — [Zeigler, Hu & Rozenblit 1989, p. 86](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Benefit: automatic synthesis.** "An advantage of the knowledge-based environment is that it is capable of automatically synthesizing hierarchical, modular models from model base resident components ... under the direction of structural knowledge." — [Kim 1988, UA OAI record](https://repository.arizona.edu/server/oai/request?verb=GetRecord&metadataPrefix=oai_dc&identifier=oai:repository.arizona.edu:10150/184380)
- **Benefit: rules from structure.** With good decomposition/taxonomy/coupling representations, "large segments of rules can be generated almost automatically from the knowledge structures: in the absence of such structures these rules would have to be generated one-by-one in an often ad hoc manner ... rule development should then reduce to the creative effort required to deal with the irreducible idiosyncracies of the problem domain." Also, structured representations "significantly reduce the effort of designing expert environments for a given problem domain." — [Rozenblit & Zeigler 1985, pp. 223, 225](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **Benefit: objective-scoped partial models.** Pruning against generic frames means "a number of design alternatives may be disregarded as not applicable," and "partial models of the design can be formulated and evaluated. This may significantly reduce the complexity which would arise if we had to deal with the overall design model." — [Rozenblit & Zeigler 1985, p. 226](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **Benefit: AI integration and distributed simulation.** "Since both the implementation and the underlying language are accessible to the user, the result is a capable medium for combining simulation modelling and artificial intelligence techniques ... It also serves as a medium for developing hierarchical distributed simulation models and architectures." — [Zeigler 1987, SIMULATION abstract, DOI](https://doi.org/10.1177/003754978704900506); [Zeigler, Hu & Rozenblit 1989, p. 84](https://www.informs-sim.org/wsc89papers/1989_0012.pdf)
- **Benefit (MS4 Me guide):** "A very powerful technique is to define model behavior with Atomic DEVS and then to define the relationships between models with SES" (p. 165). — [local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **Limitation: cost of modularity.** "...some work must be put into preparing model descriptions appropriately." — [Zeigler 1986, p. 131](https://www.informs-sim.org/wsc86papers/1986_0019.pdf)
- **Limitation: maturity, 1985.** "Implementation of such a package, in all its generality, may be a long way off. However, specific parts of it, have already been implemented." — [Rozenblit & Zeigler 1985, p. 230](https://www.informs-sim.org/wsc85papers/1985_0048.pdf)
- **Limitation: coherence and evolvability.** Zeigler, Luh & Kim introduce "context-sensitive pruning and partitioned entity structure bases ... to promote model base coherence and evolvability". This implies that unconstrained pruning and monolithic entity structure bases threatened coherence as model bases grew. — [DOI 10.1109/aihas.1990.93938](https://doi.org/10.1109/aihas.1990.93938)
- **Limitation: axiom tension.** For a recursive canonical SES of system specifications, "we must relax the strict hierarchy SES axiom as in Cho (1993) in order to facilitate such a process." — [Rozenblit & Zeigler 1993, p. 607](https://www.informs-sim.org/wsc93papers/1993_0084.pdf)
- **Limitation: implementation detail in DEVS-Scheme.** Transforming nested PES files required a separate Scheme environment "so as not to interfere with the parent environment." — [Rozenblit, Kim & Zeigler 1988, p. 228](https://www.informs-sim.org/wsc88papers/1988_0033.pdf)
- **Limitations noted in the MS4 Me guide:**
  - Multi-aspects were introduced "to address the limitation of having to explicitly declare decompositions having different numbers of entities" (p. 120).
  - "without 'inherit from BetaChimpTwo! and inherit from AlphaChimpTwo!', the selected models have empty behaviors" (pp. 26–27), and "To inherit actual atomic models, users need to modify the PhysicsModelPM1.pes file" (p. 158).
  - "Couplings are not added for matches that cannot be made – for example, if the numbers of boys and girls are not equal" (p. 124).
  - With different index names, "a bottom-up order must be given" (p. 126).
  - Unmade choices in a partial PES "will be randomly selected" (p. 168).
  - "To handle Multi-Aspect with specialization, PES file should be modified" (p. 128).

  — [local PDF](/home/chris/DEVS/devs-skills-claude/docs/ses/MS4_Me_Users_Guide.pdf)
- **Limitation: adoption.** The 2013 paper closes by discussing "future research and developments needed to bring the marketplace into common use by modeling and simulation practitioners." — [Zeigler, Seo & Kim 2013, DOI](https://doi.org/10.1142/s1793962313400060)
- **Self-assessment of the field (Zeigler & Ören 1986).** "The tendency among some simulation system makers has been to 'grope in the light' ... preferring to perfect the tools laid down decades ago, rather than step out with bold new ones into the unexplored ... The lesson is plain: take the next step!" This is framed as motivation for multifacetted/SES environments, not as a limitation of them. — [p. 711](https://www.informs-sim.org/wsc86papers/1986_0108.pdf)
- **How later authors characterize SES/MB (secondary).** The Wismar/Pawletta group and others describe an "SES/MB framework" for interactive or automatic generation of executable simulation models (e.g., the [Wismar SES Toolbox page](https://fiw.hs-wismar.de/fakultaet/ausstattung-forschung/forschungsgruppen/research-group-cea/software-projects/ses-toolbox/) and SNE journal papers). I saw this only in search-result summaries, not full text. It is included only to mark the boundary between originator statements (above) and later attribution. The other researchers cover this.

### Inferences
- The originators' benefits cluster around *reuse*, *verification* and *automation of synthesis*. Their limitations cluster around *managing large, evolving bases* (coherence, partitioning, context) and around *expressiveness gaps* (variable cardinality, which led to multi-aspects; inheritance of behavior into specialized entities; recursion versus strict hierarchy).
- Several limitations in the MS4 Me guide are usability-level (manual `.pes` edits) rather than conceptual.
- The late-1980s papers promised automated objective-driven design ranking. I found no later Zeigler-group evaluation, in the sources I read, reporting how well that automation worked in practice.

### Gaps
- I found no retrospective by Zeigler et al. that systematically assesses SES/MB limitations (e.g., scalability of pruning, combinatorial explosion, or MB versioning) with evidence. Such material, if it exists, is likely in the paywalled TOMACS 1991 paper, the 2013/2017 Guide chapters, or the TMS editions.
- I did not read the full text of the KBDSE JORS (1990) paper, so its discussion of limitations, if any, is not captured.

#### Bibliographic verification log (applies to all sections)
- **Verified against the scanned cover/header page** (INFORMS-SIM WSC archive):
  - Zeigler 1986 (WSC'86, pp. 129–137)
  - Zeigler & Ören 1986 (WSC'86, pp. 708–712)
  - Rozenblit & Zeigler 1985 (WSC'85, pp. 223–231)
  - Rozenblit, Kim & Zeigler 1988 (WSC'88, pp. 226–230)
  - Zeigler, Hu & Rozenblit 1989 (WSC'89, pp. 84–89)
  - Rozenblit & Zeigler 1993 (WSC'93, pp. 604–611)
  - Chien & Rozenblit 1993 (WSC'93, p. 582ff)
- **Archive URL caution:** the WSC'85 archive index links "Concepts for Knowledge-Based System Design Environments" to `1985_0047.pdf`, but that file actually contains Venkatraman & Wilson, "Using Path Control Variates in Activity Network Simulation" (pp. 217–222). The Rozenblit & Zeigler paper is in **`1985_0048.pdf`**, verified by reading the cover page.
- **Verified via Crossref DOI metadata:**
  - Kim et al. 1990 (IEEE SMC)
  - Zeigler, Luh & Kim 1991 (TOMACS) and the 1990 AIHAS conference version
  - Zeigler 1987 (SIMULATION; duplicate DOI deposit noted)
  - Kim & Zeigler 1987 (WSC)
  - Rozenblit & Huang 1987 and 1991
  - Rozenblit, Sevinc & Zeigler 1986
  - Rozenblit, Hu, Kim & Zeigler 1990 (JORS)
  - Cheon, Kim & Zeigler 2008
  - Mittal, Risco-Martín & Zeigler 2009
  - Kim, Seo & Zeigler 2009/2010
  - Zeigler, Moon, Kim & Kim 1996
  - Zeigler, Seo & Kim 2013
  - Zeigler & Sarjoughian 2013/2017 (chapter lists)
  - Zeigler 1990 book (chapter list)
  - Zeigler, Muzy & Kofman TMS 3rd ed. (chapter list)
- **Verified via Open Library ISBN records:**
  - Zeigler 1984 (Academic Press, London/Orlando)
  - Zeigler 1990 (Academic Press, Boston)
  - Zeigler, Praehofer & Kim 2000 (TMS 2nd ed., Academic Press, San Diego)
  - Zeigler & Hammonds 2007 (Academic Press; authors confirmed only via a library catalog search result and the MS4 Me footnote)
- **Verified via UA repository OAI-PMH record:** Kim 1988 dissertation.
- **UNVERIFIED** (cited only from Zeigler-group reference lists):
  - Zeigler, Belogus & Bolshoi 1980 (ESP)
  - Belogus 1985 dissertation
  - Rozenblit 1985 dissertation
  - Rozenblit & Zeigler 1986 "Entity-Based Structures for Model and Experimental Frame Construction"
  - Hu 1989 dissertation
  - Huang 1987 M.S. thesis
  - Rozenblit & Zeigler 1987/1988 "Design and Modelling Concepts," *Encyclopedia of Robotics*
- **MS4 Me User's Guide:** no publication year printed on the cover; the PDF metadata creation date is 2021-10-19. Page numbers cited are the printed page numbers.
- **Local EFSUT manuscript:** unpublished draft ("Submitted to: Digital Engineering and Digital Twin"); not peer reviewed.
