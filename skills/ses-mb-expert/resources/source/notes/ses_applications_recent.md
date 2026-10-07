# SES/MB Applications, Zeigler's Recent Work, Comparisons, and Critiques (about 2010 to 2026)

> **Source copy.** Research notes produced on 2026-10-06 for the SES/MB deep-research report (repo path `research_notes/SES and model base methodology/`), copied into the skill for verbatim lookup and lightly edited to remove personnel details. They are working notes: quotes and page locators are the useful part, and the curated files under `../../reference/` take precedence. References to "the report writer" or "other researchers" are from the original research task.

Scope and how things were checked. This note covers applications, Zeigler's recent work, comparisons with other approaches, and critiques. It leaves out formal SES axioms, the core prune-and-synthesize workflow, and tool internals, because other researchers cover those. Bibliographic details (title, authors, year, venue) come from three kinds of source:
- Crossref DOI records (`api.crossref.org/works/<doi>`).
- OpenAlex records, which are built from Crossref and publisher metadata.
- The title or cover page of PDFs I actually opened.

Where OpenAlex disagreed with Crossref, I used Crossref. OpenAlex labels DOIs `10.1142/S17939623…` as "Advances in Complex Systems", but Crossref says they belong to the *International Journal of Modeling, Simulation, and Scientific Computing* (IJMSSC). Works I could only see in reference lists or index records are marked UNVERIFIED. MDPI full texts (Information, Systems, Computers) returned HTTP 403. HAL and the University of Arizona repository returned bot-check pages. So for those papers, only the publisher abstract (via OpenAlex or Crossref) was read, and the claims below go no further than those abstracts.

## Q1. Application domains and concrete case studies (problem, how SES was used, outcomes)

### Takeaway
Since 2010, SES/MB has been applied mostly in four places:
- Defense and net-centric C2 work in the US and Korea.
- Aerospace and autonomous-system scenario engineering, led by the DLR / TU Clausthal / Embry-Riddle group.
- Manufacturing and process-industry variant and structure optimization, led by the Wismar group.
- Networks and IoT.

There are also smaller footholds in healthcare, energy-efficient production, robotics, climate, disaster response and illicit supply chains. In nearly every case, the SES serves as a design-space or configuration-space representation that is pruned to generate model or scenario variants. Quantified outcomes are rare: the clearest is up to 8% time savings and 23% cost savings in wireless sensor network (WSN) model construction.

### Cited Findings

**Defense, C2, net-centric and systems of systems (SoS)**
- **Link-16 conformance testing.** The Automated Test Case Generator (ATC-Gen), funded by the Joint Interoperability Test Command (JITC), turns MIL-STD-6016C into rules, then into DEVS test cases. Test models are written "in an XML-SES format" and C++ DEVS code is generated from them. The aim was to separate models, simulator and distributed simulation for reuse — [Mak, Mittal, Hwang, Nutaro, JDMS 2010](https://doi.org/10.1177/1548512909358585)
- **C2 information exchange.** SES is used as an ontology framework, with Battle Management Language (BML) specifying what a consumer wants. The authors define "novel pruning and transformation processes of the SES to extract and fuse data into higher-level representations". This is demonstrated in an air-defense scenario simulated in DEVS, where "the information requested by a commander is delivered in the right way at the right time" — [Lee & Zeigler, JDMS 7(4):206–225, 2010](https://doi.org/10.1177/1548512910365063)
- **Network analysis.** A web-based distributed network analyzer is built from an SES over a service-oriented architecture — [Kim, Seo, Zeigler, SIMULATION 86(3):155–180 (online 2009)](https://doi.org/10.1177/0037549709354112)
- **SoS on HLA/RTI.** The authors state that "SES/MB is limited in simulating the system of systems using HLA/RTI". In response they propose a System of Systems Entity Structure / Federate Base (SoSES/FB) with a federation-synthesis process, demonstrated on a warship anti-missile defense simulation — [B.S. Kim, C.B. Choi, T.G. Kim, SpringSim CNS 2013](https://doi.org/10.5555/2499986.2499990)
- **Armored fighting vehicle vulnerability.** SES/MB gives a hierarchical representation of the vehicle's components. A simulation program then weighs penetrated or damaged components by importance to compute vulnerability, both overall and by function — [H.K. Kim, Hwang, J.S. Lee, J. Korea Soc. Simulation 24(4), 2015](https://doi.org/10.9709/jkss.2015.24.4.059)
- **Other Korean and Chinese defense uses** found in SES-filtered index results (abstracts not read in full):
  - HEAP-based defense M&S methodology — [You, Chi, J.I. Kim, IEICE Trans. Inf. Syst., 2013](https://doi.org/10.1587/transinf.e96.d.655)
  - Model composition for engagement-level underwater warfare — [Zhang & Kang, ICMII 2015](https://doi.org/10.2991/icmii-15.2015.129)
  - A test-case base for dynamic verification of defense simulation models based on SES/MB — Choi et al. 2017 (UNVERIFIED venue, no DOI; seen in the OpenAlex index only)
- **UAV-based service systems of systems.** Multiresolution modeling builds "a collection of partial models, each oriented to one or more objectives" (payload, on-board energy, depot locations). The paper presents this as "a practical approach to multiresolution model-based system design of UAV-based service systems", based on DEVS and SES as implemented in MS4 Me — [Zeigler & D. Kim, SpringSim 2019](https://doi.org/10.23919/springsim.2019.8732861)
- **Netcentric SoS engineering with the DEVS Unified Process.** This line of work appears in the SES-filtered results — [Mittal & Risco-Martín, WSC 2013](https://doi.org/10.1109/wsc.2013.6721503); book: [Mittal & Risco-Martín, CRC Press (Crossref record 2018)](https://doi.org/10.1201/b13971)

**Manufacturing, production, process industry and energy-efficient production**
- **Structure plus parameter optimization.** Admissible model structures and parameter sets are specified in an SES, and basic components sit in a model base. New algorithms map SES knowledge to numerical structure parameters and back, so that an evolutionary algorithm can optimize structure and parameters together in MATLAB/Simulink — [Hagendorf, Pawletta, Larek, SIMULATION 89(9):1115–1127, 2013](https://doi.org/10.1177/0037549713500066)
- **Resource-efficient process chains.** Model-based planning of resource-efficient process chains uses SES — [Larek, Brinksmeier, Pawletta, Hagendorf, Springer 2012](https://doi.org/10.1007/978-3-642-24491-9_36)
- **Energy- and resource-efficient manufacturing chains.** Two alternative chains for producing countershafts (heating, hot-rolling, forging, turning) were defined and structured "in so-called system entity structures". A genetic algorithm then found the chain design and process parameters with the lowest energy and resource consumption — [Rentsch, Heinzel, Brinksmeier, Procedia CIRP 33:139–144, 2015](https://doi.org/10.1016/j.procir.2015.06.026)
- **Process industry.** Hierarchically arranged multi-aspects specify certain classes of system variants "very efficiently", and the paper gives a method for automatically deriving variants for automated simulation studies — [Folkerts, Pawletta, Durak, SNE 33, 2023](https://doi.org/10.11128/sne.33.tn.10633)
- **Valve test bench.** A test bench for valves is decomposed into generalized SES design patterns — [Folkerts, Pawletta, Deatcu, Durak, SNE 27, 2017](https://doi.org/10.11128/sne.27.tn.10391)
- **Floating LNG plant design.** A generic relational SES of liquefaction plant elements, built from rules extracted from the C3MR and DMR cycles, is pruned to automatically generate alternative liquefaction cycles as XML. Process simulation and reliability analysis then evaluate the alternatives — [Ha & Lee, Int. J. Naval Arch. Ocean Eng. 6(3):598–625, 2014](https://doi.org/10.2478/ijnaoe-2013-0200)
- **Semiconductors.** SES represents wafer failure patterns, and a pruned SES (PES) holding only the relevant entities is extracted for each pattern "for better computational performance" — [Han, S. Kim, T.K. Kim, Jung, IEICE Trans. Inf. Syst., 2010](https://doi.org/10.1587/transinf.e93.d.2001)

**Communication networks, IoT and cyber**
- **Wireless sensor networks.** The WSN-SES/MB framework synthesizes DEVS structures and models for large-scale WSNs. It reports "up to 8% time savings and up to 23% cost savings as compared to the manual extra effort", and says pruning "increases the diversity of simulation models" — [Nam & H.J. Kim, Sensors 21(2):430, 2021](https://doi.org/10.3390/s21020430) (full text read via [Europe PMC PMC7827288](http://europepmc.org/pmc/articles/PMC7827288))
- **IoT ecosystems.** An SES-based "IoT-Inclusive-Systems model" and risk-evaluation framework is built for converged IT/OT systems, with a MIRAI botnet case study — [Mittal, Cane, Schmidt, Harris, Tufarolo, in *Complexity Challenges in Cyber Physical Systems*, Wiley 2019, pp. 163–190](https://doi.org/10.1002/9781119552482.ch7)
- **Internet of Cyber-Things.** Case studies cover home automation, solar energy monitoring, conflict management and swarm robotics. Per the abstract, "the role of the SES is also discussed, highlighting its contribution in organizing models and generating alternative system configurations" — [Capocchi, Zeigler, Santucci, Computers 14(7):258, 2025](https://doi.org/10.3390/computers14070258)
- **Information security for autonomous vehicles.** An SES-based information-security framework for an autonomous-vehicle control environment (title-level evidence only; abstract not available) — [H.J. Kim & Park, CCIS 2022](https://doi.org/10.1007/978-981-19-6857-0_7)

**Robotics**
- **Flexible task-oriented robot controls.** Atomic tasks are composed at run time from process states and task-sequence constraints. SES/MB specifies, generates and executes these controls for cooperating robots — [Schwatinski, T. Pawletta, S. Pawletta, SNE 22, 2012](https://doi.org/10.11128/sne.22.tn.10135)
- **Drones.** Drone perception algorithms in an urban environment are validated in Gazebo using SES plus behaviour trees — [Gupta & Durak, AIAA SciTech 2023](https://doi.org/10.2514/6.2023-2589) (see Q3)

**Healthcare**
- **Health-care model suites.** SES merging and multiple aspects are used to build "suites of models", with a health-care suite as the example, hosted on the cloud-based Model Store — [Zeigler, Seo, D. Kim, IJMSSC 4(3):1340006, 2013](https://doi.org/10.1142/S1793962313400060) (venue checked against Crossref)
- **Personal wellness planning.** A web application for wellness planning and self-management embeds the MS4 Me SES/PES kernels and pruning libraries inside IT systems — [Seo, Kang, Zeigler, D. Kim, ACM DL 10.5555/2665008.2665015, 2014](https://doi.org/10.5555/2665008.2665015) (venue name UNVERIFIED, probably SpringSim 2014)
- **Healthcare ontology.** A healthcare-systems simulation ontology is "formally expressed using system entity structure language", linked to a model base. Entities are mapped to OWL classes for Protégé and SPARQL, with a cholera-outbreak case study — [Djitog & Kah, IJPHIM, 2020](https://doi.org/10.4018/ijphim.2020010105)
- **Zeigler's main healthcare system-of-systems (SoS) work.** The abstracts of these do not mention SES:
  - [Zeigler, IEEE Systems Journal 12(1):196–207, 2018](https://doi.org/10.1109/jsyst.2016.2514414)
  - [Traoré, Zacharewicz, Duboz, Zeigler, SIMULATION 95(6):481–497](https://doi.org/10.1177/0037549718776765)
  - IET book *Value-based Learning Healthcare Systems* — [IET 2018](https://doi.org/10.1049/pbhe015e)

**Other domains**
- **Climate.** A global-warming "suite of models" is built by SES merging, with families of models "generated and tested via pruning and transforming their SESs in bottom-up manner" — [Zeigler, Seo, Coop, D. Kim, 2013, ACM DL 10.5555/2499634.2499666](https://doi.org/10.5555/2499634.2499666) (venue UNVERIFIED)
- **Disaster response.** "Simulating Spontaneous Volunteers: A System Entity Structure for Defining Disaster Scenarios" (abstract not available) — [Lindner, Sackmann, Betke, ISCRAM 2019 (dblp)](https://dblp.uni-trier.de/db/conf/iscram/iscram2019.html#LindnerSB19)
- **Illicit supply chains (law enforcement).** "We parameterize structural uncertainty using System Entity Structure". The study compares calibration methods (Powell, ABC, Bayesian Optimization, Genetic Algorithms) for recovering the structure of a counterfeit-PPE supply chain from sparse data. Bayesian Optimization and GAs proved suitable — [van Schilt, Kwakkel, Mense, Verbraeck, Advanced Engineering Informatics 62:102926, 2024](https://doi.org/10.1016/j.aei.2024.102926)
- **Reinforcement learning design.** Q-learning is implemented within DEVS and SES to handle temporal, hierarchical and multi-agent aspects — [Capocchi & Santucci, Information 13(3):121, 2022](https://doi.org/10.3390/info13030121)
- **Automotive MBSE.** SES-X combines SES/MB with X language and performs "two levels of model pruning for model verification and simulation efficiency", with a car-model case study — [Xie, Zhang, Li, Gu, Chen, Information 14(1):23, 2023](https://doi.org/10.3390/info14010023)

**Earlier landmarks (brief)**
- *Multifacetted Modelling and Discrete Event Simulation* (1984) and "Structuring the organization of partial models" (IJGS 1978) are the conceptual origins. Zeigler himself cites both in a 2026 EFSUT manuscript ([local draft](/home/chris/DEVS/devs-skills-claude/docs/efsut/Three%20Complementary%20Uses%20of%20the%20EFSUT%20Methodology.docx)) — details UNVERIFIED beyond that reference list.
- *Modeling & Simulation-Based Data Engineering: Introducing Pragmatics into Ontologies for Net-Centric Information Exchange* (2007) positioned SES as an ontology for net-centric information exchange — [Zeigler & Hammonds, 2007 (CiNii record)](https://ci.nii.ac.jp/ncid/BB00585506). The publisher (Academic Press/Elsevier) is taken from citing reference lists only, so it is UNVERIFIED.
- SES used for composing DEVS models and for XML metadata in climate normals — [Cheon, D. Kim, Zeigler, IEEE IRI 2008](https://doi.org/10.1109/iri.2008.4583078)

### Inferences
- The application base is clustered around a few research organizations rather than spread across industry:
  - University of Arizona / RTSync.
  - Hochschule Wismar with DLR and TU Clausthal.
  - Korean defense M&S groups.
  - University of Corsica.
  - Beihang University (X-language line).
  - Most domain papers outside these groups (FLNG, illicit supply chains, disaster volunteers) are one-off uses.
- Across all domains the SES is used for three things, in falling order of frequency:
  1. A design or configuration space to prune into variants (manufacturing, FLNG, WSN, scenarios).
  2. An ontology for information exchange (C2, healthcare, IoT).
  3. Organizing a model library or suite (climate, healthcare, UAV multiresolution families).
- Energy appears mainly as an objective inside manufacturing optimization (Rentsch; Larek), plus one IoCT case study (solar monitoring). I found no dedicated power-grid or energy-system SES study.

### Gaps
- I found no public paper that applies SES specifically to "mission engineering" in the DoD Mission Engineering Guide sense, such as mission threads or mission engineering threads. The closest are UAV and ambulance SoS design (Zeigler & Kim 2019; Zeigler, Mittal, Traoré 2018) and Korean naval air-defense SoS work. Any RTSync or government mission-engineering SES work appears to be unpublished or in reports I could not reach.
- Full texts of the MDPI papers (MetaSES, SES-X, IoCT, MBSE with/out Simulation) were blocked (HTTP 403), so their case-study details and outcomes are limited to the abstracts.
- Quantitative outcome reporting is rare. Apart from WSN-SES/MB (8% time, 23% cost), most papers show feasibility on one example without measured benefits.
- The SES content of these works is UNVERIFIED (abstracts unavailable or silent on SES): Jafer, Zeigler & Kim, "A Framework for Rapid Configuration of Collaborative Aviation SoS Simulations" ([LNCS 2018](https://doi.org/10.1007/978-3-319-76072-8_7)); Capocchi et al., "Discrete-Event Simulation Model Generation based on Activity Metrics" ([SIMPAT 103:102122, 2020](https://doi.org/10.1016/j.simpat.2020.102122)); and Seddari, Bouras, Zeigler et al. on complex industrial manufacturing ([SSRN 2026](https://doi.org/10.2139/ssrn.7273337)).

## Q2. Zeigler's own recent publications that use or extend SES, and the current state of his thinking

### Takeaway
Zeigler's thinking has moved through four stages:
1. 2013–2017: the SES as the structural backbone of model families and "suites of models" for systems of systems (MS4 Me, the SoS Guide).
2. 2016–2019: SES extensions (abstraction hierarchies, multiresolution families, reactive pruning).
3. 2023: a unifying theoretical step, the MetaSES. It ties the SES to the hierarchy of system specifications and morphisms, for complex SoS with many classed components.
4. 2025–2026: SES as a supporting element in IoT/CPS design and in the EFSUT (Experimental Frame–System Under Test) methodology. In a draft companion manuscript, pruning an SES over EF and Model aspects yields EF/Model pairings.

His recent AI work (LLMs for DEVS model comprehension, generative "model seeds" completed by SAT synthesis) does not yet explicitly involve SES in any abstract I could read.

### Cited Findings

**Books and chapters**
- **Guide to Modeling and Simulation of Systems of Systems.** Zeigler & Sarjoughian, Springer, Simulation Foundations, Methods and Applications series: 1st ed. 2013 ([10.1007/978-0-85729-865-2](https://doi.org/10.1007/978-0-85729-865-2)); 2nd ed. 2017 ([10.1007/978-3-319-64134-8](https://doi.org/10.1007/978-3-319-64134-8)). Both checked via Crossref.
  - 2nd-edition chapters (from chapter DOIs): "System Entity Structure Basics", "Specialization and Pruning", "Aspects and Multi-aspects", "Managing Inheritance in Pruning", "Automated and Rule-Based Pruning and Experimental Execution", "Interest-Based Information Exchange: Mappings and Models", "Modeling and Simulation of Living Systems as Systems of Systems", "Cloud System Simulation Modeling", "Activity-Based Implementations of Systems of Systems" — [e.g., ch. 3](https://doi.org/10.1007/978-3-319-64134-8_3), [ch. 8](https://doi.org/10.1007/978-3-319-64134-8_8)
  - A companion SpringerBrief user's reference (2012) has chapters "System Entity Structure List" and "Pruning SES List" — [10.1007/978-1-4471-4570-7_3](https://doi.org/10.1007/978-1-4471-4570-7_3)
- **Theory of Modeling and Simulation, 3rd ed.** Zeigler, Muzy & Kofman, Elsevier/Academic Press, 2018. Chapter DOIs include "Abstraction: Constructing Model Families" and "Hierarchy of System Morphisms" — [10.1016/b978-0-12-813370-5.00026-2](https://doi.org/10.1016/b978-0-12-813370-5.00026-2). No chapter title names the SES.
- **Model Engineering for Simulation.** Elsevier 2019, ISBN 9780128135433.
  - Introduction by Lin Zhang, Bernard P. Zeigler, Yuanjun Laili (pp. 1–23) — [10.1016/b978-0-12-813543-3.00001-9](https://doi.org/10.1016/b978-0-12-813543-3.00001-9)
  - SES chapter: Pawletta, Durak, Schmidt, "Modeling and Simulation of Versatile Technical Systems Using an Extended System Entity Structure/Model Base Infrastructure" (pp. 393–418) — [10.1016/b978-0-12-813543-3.00018-4](https://doi.org/10.1016/b978-0-12-813543-3.00018-4)
  - Zeigler's chapter: "Simulation-Based Evaluation of Morphisms for Model Library Organization" (pp. 25–42) — [10.1016/b978-0-12-813543-3.00002-0](https://doi.org/10.1016/b978-0-12-813543-3.00002-0)

**Journal and conference papers (chronological)**
- **2016, SES extension for levels of detail.** SES is called "a fundamental representation of DEVS hierarchical modular model structures… [representing] a design space". The extension adds abstraction hierarchies and time granularity, implemented in DEVSimPy — [Santucci, Capocchi, Zeigler, SIMULATION 2016](https://doi.org/10.1177/0037549716657168)
- **2016, extended variability modeling** with the SES ontology inside MATLAB/Simulink, framed against software product-line variability — [Pawletta, Schmidt, Zeigler, Durak, SpringSim ANSS 2016](https://doi.org/10.22360/springsim.2016.anss.061)
- **2017, SES/MB as metamodeling.** SES/MB is positioned as metamodeling and model transformation for model-based engineering of technical-system simulations, "after reviewing comparable approaches in software engineering" — [Durak, Pawletta, Oğuztüzün, Zeigler, SpringSim Mod4Sim 2017](https://doi.org/10.22360/springsim.2017.mod4sim.001)
- **2018, reactive model generation.** A framework for metamodeling multivariant systems and generating and executing simulation models reactively — [Pawletta, Schmidt, Durak, Zeigler, SNE 28, 2018](https://doi.org/10.11128/sne.28.tn.10402)
- **2018, MBSE with/out Simulation.** The paper argues that MBSE lacks "capability to develop multifaceted models" and robust simulation, and that the resulting knowledge gaps "appear as epistemological emergent behaviors". It examines an unmanned-vehicle emergency-ambulance fleet as an example of the "multifaceted M&S methodology" required — [Zeigler, Mittal, Traoré, Systems 6(4):40, 2018](https://doi.org/10.3390/systems6040040)
- **2020, automatic pruning of complex SESs.** "Especially for some types of nodes… automatic pruning is a big challenge". The paper analyzes hierarchies of multi-aspect and specialization nodes and proposes a solution — [Folkerts, Pawletta, Deatcu, Zeigler, SpringSim Mod4Sim 2020](https://doi.org/10.22360/springsim.2020.mod4sim.001)
- **2023 (online 29 Dec 2022), MetaSES.** "This article works toward a unification of… the hierarchy of system specifications and morphisms and the System Entity Structure (SES)."
  - It proposes "an abstraction of the SES called the MetaSES that supports the construction of complex systems of systems with multiple components belonging to specified classes".
  - The goal is to "make it easier to design and build complex hierarchical DEVS models and to communicate their structures and intended behaviors to foster continued reuse".
  - It claims to "push the boundaries of complexity management"
  - Source: [Zeigler, Information 14(1):22](https://doi.org/10.3390/info14010022)
- **2022, DEVS and MBSE review.** "DEVS and MBSE: A review" (abstract not available, so SES content UNVERIFIED) — [Zeigler, IJMSSC 13(2):2230001, 2022](https://doi.org/10.1142/S1793962322300011) (venue checked via Crossref)
- **2025, IoT and IoCT design.** "We will demonstrate the added value of the System Entity Structure (SES) to enhance expressiveness, scalability, and flexibility in IoT system design" — Zeigler, Capocchi, Santucci, "DEVS as a Language for Design of Adaptive IoT Systems", HAL 2025 (UNVERIFIED venue; seen via the OpenAlex index). The journal version reviews the SES's role in "organizing models and generating alternative system configurations" — [Capocchi, Zeigler, Santucci, Computers 14(7):258, 2025](https://doi.org/10.3390/computers14070258)
- **2026, EFSUT published.** The methodology is organized around "questions, experimental conditions, models, and results" with "formal relations that clarify how tests are derived and which models can meaningfully be evaluated". It is aimed at "multi-model and system-of-systems contexts". Future work includes "automated derivation of Experimental Frames, integration with digital-thread toolchains". The abstract does not mention SES; full text was blocked — [Zeigler, Systems 14(6):691, 2026](https://doi.org/10.3390/systems14060691)
- **2026, EFSUT draft manuscript (local, unpublished).** File: "Three Complementary Uses of the EFSUT Methodology: Model Development, System Testing, and Decision Support in ParaDEVS", Zeigler, marked "Submitted to: Digital Engineering and Digital Twin (DEDT)" ([local file](/home/chris/DEVS/devs-skills-claude/docs/efsut/Three%20Complementary%20Uses%20of%20the%20EFSUT%20Methodology.docx)).
  - The main paper never mentions SES. It speaks of model families aligned with an EF partial order and an "applicability relation".
  - An appended second draft in the same file, "EFSUT-Driven DEVS Modeling for Processor–Device Digital Twins", does use SES. "A pruned Entity Structure (SES) formalizes these relationships, yielding ten Entity–Function/Model (EF/Model) pairings".
  - That SES has two aspects, EF ∈ {EF_MMIOWrite, EF_IRQGeneration, …} and Model ∈ {M_RouterMemory, M_InterruptFabric, …}. Pruning rules "enforce a one-to-one mapping", and the conclusion says "SES pruning ensures minimality… Pruned SES ensures no over-modeling".
  - Caution: this appended draft expands EFSUT as "Entities, Functions, Structure, Usage, Time", which contradicts the main paper and the published Systems 2026 paper ("Experimental Frame–System Under Test"). It looks like an unreviewed or AI-assisted draft.
- **2024, homomorphism and model families.** "Building families of models from base-lumped model pairs" addresses model-family construction through homomorphisms; SES is not mentioned in the abstract — [Zeigler, Koertje, Zanni, SIMULATION 100(12):1181–1196, 2024](https://doi.org/10.1177/00375497241264834)
- **Zeigler-group AI/LLM work (no SES in the abstracts):**
  - GEM DEVS Chat, an LLM assistant for DEVS Natural Language (DNL) models. In an 8-student experiment, students using the tool "understood DEVS models more quickly and accurately" — [Graciano Neto, Keller, D. Kim, Seo, Carbo, Zeigler, MSSiS 2024, pp. 41–50](https://doi.org/10.5753/mssis.2024.3714)
  - Generative AI with activity and flow-based diagrams — [Alshareef, Keller, Carbo, Zeigler, LNICST 2024, pp. 95–109](https://doi.org/10.1007/978-3-031-57523-5_8)
  - "Model seeds—potentially produced by large language models" are restricted to "partial specifications, which are then completed through formal synthesis" (SAT/SMT), then turned into DEVS — [Alshareef & Zeigler, Logics 4(3):7, 2026](https://doi.org/10.3390/logics4030007)
  - Multimodal AI for semantic fusion of data silos — [Alshareef & Zeigler, Systems 13(11):987, 2025](https://doi.org/10.3390/systems13110987)

**Local ParaDEVS papers (title pages checked)**
- [Zeigler, "Discrete Event Systems Theory for Fast Stochastic Simulation via Tree Expansion", Systems 12(3):80, 2024](https://doi.org/10.3390/systems12030080) (local: `/home/chris/DEVS/devs-skills-claude/docs/paradevs/systems-12-00080-v2.pdf`). It does not discuss SES. Its Related Work cites two SES/MB works among DEVS uses for MDP and optimization models: ref. 47, Folkerts, Pawletta, Deatcu, Santucci, Capocchi, "An Integrated Modeling, Simulation and Experimentation Environment in Python Based on SES/MB and DEVS", SummerSim 2019; and ref. 52, Xie et al., "SES-X", Information 2023.
- [Zeigler, Koertje, Zanni, Yoon, Dutan, "Advanced Tutorial on Paratemporal Simulation Using Tree Expansion", WSC 2024](https://doi.org/10.1109/wsc63780.2024.10838748) (local: `/home/chris/DEVS/devs-skills-claude/docs/paradevs/2024-zeigler-koertje-zanni-yoon-dutan.pdf`). It contains no mention of SES or entity structure (text search found zero hits).

### Inferences
- Zeigler's current position appears to be:
  - The SES remains the constructive ontology for composition.
  - The MetaSES is his most recent theoretical extension, tying SES to specification levels and morphisms.
  - The EF side is being formalized separately in EFSUT, with SES pruning proposed in draft form only as the mechanism that pairs EFs with minimally sufficient models.
- The idea of a model family runs through the 2013 suites, the 2019 multiresolution UAV work, the 2023 MetaSES, the 2024 homomorphism families and the 2026 EFSUT. This suggests SES is now one of several tools Zeigler uses to organize model families, alongside morphisms and EF partial orders, rather than the central focus.
- No Zeigler publication I found yet joins SES with LLMs. The architecture of the 2026 Logics paper (LLM produces partial structure; formal synthesis completes it) maps naturally onto "LLM proposes SES, pruning and validation enforce correctness". That is an inference, not something published. This repository's own `skills/ses-designer` skill (LLM-driven generation of MS4 Me SES/PES files) is one unpublished instance of that pattern.

### Gaps
- I could not confirm whether, or how much, TMS 3rd ed. (2018) covers SES inside its chapters. No chapter title names it.
- I could not read whether the published EFSUT paper (Systems 2026) mentions SES, because MDPI blocked the full text.
- No public Zeigler paper on "SES for mission engineering" or "SES for digital engineering" specifically was found (see Q1 Gaps).
- The SES content of Zeigler's "DEVS and MBSE: A review" (IJMSSC 2022 / World Scientific 2023 chapter) and of "How Can Modeling and Simulation Help Engineering of System of Systems?" ([Elsevier/ISTE 2017](https://doi.org/10.1016/b978-1-78548-256-4.50001-6)) was not checked.

## Q3. SES for scenario-based testing and simulation-based verification

### Takeaway
The most active post-2015 application line is the DLR / TU Clausthal / Embry-Riddle program.
- It started with SES-based model-based testing of Simulink models with experimental frames (2015–2016).
- It moved to SES metamodels of flight-simulator scenarios and an Aviation Scenario Definition Language (2017–2019).
- It then built an SES-based Operational Domain Modeling Environment (ODME) for ODD-driven scenario generation and coverage for AI-based aviation and automotive systems (2023–2026).

The program itself reports scalability, tool-fragmentation and manual-effort limits.

### Cited Findings
- **Model-based testing for simulation fidelity.** Test models are represented as experimental frames (EFs), and executable EFs for a model under test are generated with SES — [Schmidt, Durak, Pawletta, "Model-based testing methodology using system entity structures for MATLAB/Simulink models", SIMULATION 92(8):729–746, 2016](https://doi.org/10.1177/0037549716656791). The earlier conference version: [Schmidt, Durak, Rasch, Pawletta, SpringSim 2015](https://doi.org/10.5555/2872965.2872975)
- **Flight-simulator scenarios (DLR AVES).** "Scenario development is usually conducted in an unstructured and ad hoc manner". SES is "exploited for metamodeling in order to capture all possible elements of a scenario that can be simulated in AVES", and a scenario modeling method is built on that metamodel — [Durak, Pruter, Gerlach, Jafer, Pawletta, Hartmann, AIAA MST 2017-1076](https://doi.org/10.2514/6.2017-1076)
- **Computational representation of a simulation scenario definition language** — [Durak, Jafer, Wittman, Mittal, Hartmann, Zeigler, AIAA MST 2018-1398](https://doi.org/10.2514/6.2018-1398)
- **ASDL with SES and Ecore.** Both were used to metamodel ASDL; "they follow a similar structure… each metamodel can be easily converted into the other". The approach supports a scenario-driven training tool for air traffic controllers — [Jafer, Chhaya, Zeigler, Durak, Int. J. Aviation Aeronautics Aerospace, 2018](https://doi.org/10.58940/2374-6793.1248)
- **SES/PES tools.** Scenario development tools (SES Editor and PES Editor) — [Karmokar, Durak, Jafer, Chhaya, Hartmann, AIAA SciTech 2019-1712](https://doi.org/10.2514/6.2019-1712). An application-agnostic computational representation is proposed "in search for wider acceptance and eventual standardization" — [Karmokar, Durak, Hartmann, Zeigler 2019 (DLR elib)](https://elib.dlr.de/130764/) (venue UNVERIFIED)
- **ODD-driven testing for AI in aviation.** "Operational Domain Driven Testing" uses SES for "variable structural modelling of the scenario's entities and environment" and behaviour trees for dynamic events. It is demonstrated by validating a drone's perception algorithms in an urban environment in Gazebo — [Gupta & Durak, AIAA SciTech 2023-2589](https://doi.org/10.2514/6.2023-2589)
- **ODME toolkit for autonomous systems (automotive and aviation).** This paper links SES to the automotive scenario standards OpenSCENARIO and ASAM and to the SAE J3016 ODD. It describes ODME as "a successor to the SES tools" — [Naja, Gupta, Durak, Hartmann, "Towards a Scenario Toolkit for Autonomous Systems", SNE 34(1):13–21, 2024 (PDF read)](https://doi.org/10.11128/sne.34.tn.10672). Its workflow:
  1. Expert knowledge goes into an SES domain model.
  2. Pruning yields individual scenarios.
  3. A scenario manager labels and prioritizes them.
  4. Scenarios are executed in MATLAB or Gazebo.
  5. Assessment feeds back into the domain model.
- **Scenario coverage metric.** A quantifiable coverage metric for scenarios against an ODD, based on the formal scenario/ODD structure and discretized parameters, implemented in ODME — [Gupta, Sprockhoff, Durak, AIAA SciTech 2025-2326](https://doi.org/10.2514/6.2025-2326)
- **Automated generation for coverage.** "A significant limitation was the manual effort required to model scenarios to achieve high coverage levels". The paper automates scenario generation and adds ODD dependencies and distributions — [Gupta, Nana Mbouendeu, Kamdem Kountchou, Durak, AIAA SciTech 2026-0208](https://doi.org/10.2514/6.2026-0208). The abstract does not name SES; the link is through ODME.
- **Automotive-oriented examples:**
  - The SES-X car model with two-level pruning — [Xie et al., Information 14(1):23, 2023](https://doi.org/10.3390/info14010023)
  - An SES-based information-security framework for autonomous-vehicle control — [H.J. Kim & Park, CCIS 2022](https://doi.org/10.1007/978-981-19-6857-0_7)
- **Communications test generation.** Test models for Link-16 conformance testing are generated as XML-SES — [Mak et al., JDMS 2010](https://doi.org/10.1177/1548512909358585)

### Inferences
- For scenario-based testing, the SES fills the "logical scenario" (parameter space) role that OpenSCENARIO and ODD standards leave open. Pruning plays the role of concretizing a scenario. The newer work treats coverage of the pruning space against the ODD as the key metric.
- I found no SES-based scenario-generation paper from a mainstream automotive OEM or supplier. Automotive uses in the SES literature come from academic and research-lab groups that adapt aviation work to automotive standards.

### Gaps
- Whether these papers use SES directly is unconfirmed: [Stefani et al., CEAS Aeronautical Journal 16(1):197–212, 2024](https://doi.org/10.1007/s13272-024-00772-4) ("Automated scenario generation from Operational Design Domain model…"); [Lukić et al., SNE 2023](https://doi.org/10.11128/sne.33.tn.10666); and [Sprockhoff et al., AIAA 2024-1462](https://doi.org/10.2514/6.2024-1462). Their abstracts do not mention SES.
- No published numbers on test-effort savings or fault-detection gains from SES-based scenario generation were found.

## Q4. Comparison with feature models, SysML variant modeling, ontologies (OWL), and design-space exploration tools

### Takeaway
Real head-to-head comparisons are few and qualitative, each based on a single case:
- SES (Python eSES/MB) vs MATLAB/Simulink Variant Manager.
- SES/XML vs OWL/Protégé/EMF, and SES vs Ecore, for a scenario language.

Both found broadly equivalent expressiveness for their cases. SES's distinctive advantages were variable interfaces and couplings, a top-down whole-structure view, and framework independence. I found no formal or empirical comparison of SES with feature models (FODA/cardinality-based), SysML v1/v2 variant modeling, or design-space-exploration tools.

### Cited Findings
- **Simulink Variant Manager vs SESToPy/SESMoPy** (head-to-head on the MathWorks power-window example). Findings:
  - "with the Variant Manager interfaces of submodels are static, while the eSES/MB approach allows to define variable interfaces and couplings".
  - "Regarding the modeling effort the approaches do not differ considerably".
  - Variant Manager modeling "can be seen as a bottom-up procedure while modeling with the eSES/MB approach is rather top-down".
  - eSES/MB "gives more clarity… because the overall structure of the model can be captured with one sight".
  - "A survey among our students came to the result, that eSES/MB is a lot easier to get started with".
  - Caveat from the authors: "a final comparison is not possible on the basis of just one example".
  - Source: [Deatcu, Pawletta, Folkerts, SNE 29(1):39–43, 2019 (PDF read)](https://doi.org/10.11128/sne.29.sw.10466)
- **OWL/EMF vs SES/XML for ASDL.** The sample en-route scenario produced by both methods "found to be identical". Table 2 of the paper compares them: required attributes (SES adds "quantities"), ontology format (.owl vs graphical), and "framework independence" (OWL/EMF: no, dependent on EMF; SES/XML: yes). The authors also note:
  - SES "follows more rigid guidelines and restrictions in the definition of entities, aspects and specializations".
  - "an understanding of SES is required to comprehend and edit these files".
  - SES "did not require the additional step of transforming the ontological details into an EMF model".
  - Source: [Jafer, Chhaya, Updegrove, Durak, "Schema-based Ontological Representations of a Domain-Specific Scenario Modeling Language", Journal of Simulation Engineering vol. 1, 2018 (PDF read; Zenodo DOI)](https://doi.org/10.5281/zenodo.11114155)
- **SES vs Ecore.** "they follow a similar structure in the hierarchical definition of modeled elements despite there being different toolsets… each metamodel can be easily converted into the other type using transformations" — [Jafer, Chhaya, Zeigler, Durak, IJAAA 2018](https://doi.org/10.58940/2374-6793.1248)
- **SES and OWL used together, not in competition.** SES entities "are mapped into web ontology language (OWL) classes and can be visualized in Protégé and queried with SPARQL" — [Djitog & Kah, IJPHIM 2020](https://doi.org/10.4018/ijphim.2020010105)
- **Product lines.** The 2016 SpringSim paper frames SES variability modeling against software product lines, which "define variation points where different solutions can be derived for different products" at levels "ranging from requirements specification to source code" (abstract only) — [Pawletta, Schmidt, Zeigler, Durak, SpringSim ANSS 2016](https://doi.org/10.22360/springsim.2016.anss.061)
- **Software-engineering approaches.** The 2017 Mod4Sim paper reviews "comparable approaches in software engineering" before positioning SES/MB for metamodeling and model transformation (abstract only) — [Durak, Pawletta, Oğuztüzün, Zeigler, 2017](https://doi.org/10.22360/springsim.2017.mod4sim.001)
- **SysML/MBSE.** "MBSE with/out Simulation" contrasts MBSE "taken as a modeling activity without the support of full strength integrated simulation capability" with integrated multifaceted M&S — [Zeigler, Mittal, Traoré, Systems 6(4):40, 2018](https://doi.org/10.3390/systems6040040). SES-X couples SES philosophy with X language for MBSE system analysis, architecture decomposition, physical modeling and simulation — [Xie et al., Information 14(1):23, 2023](https://doi.org/10.3390/info14010023)
- **Design-space exploration and optimization.** Instead of separate DSE tools, the SES literature couples SES with optimizers:
  - Evolutionary algorithms over SES-derived structure parameters — [Hagendorf, Pawletta, Larek, SIMULATION 2013](https://doi.org/10.1177/0037549713500066)
  - Genetic algorithms over SES-structured manufacturing chains — [Rentsch et al., Procedia CIRP 2015](https://doi.org/10.1016/j.procir.2015.06.026)
  - Bayesian Optimization and GA calibration over an SES-parameterized structure space — [van Schilt et al., AEI 2024](https://doi.org/10.1016/j.aei.2024.102926)

### Inferences
- The available evidence puts SES roughly on par with OWL/Ecore for hierarchical domain metamodels. SES's differentiators are built-in couplings and multi-aspects, and pruning semantics tied directly to model generation. Its main cost is a specialized learning curve.
- The "150% model" vocabulary in the Variant Manager paper shows SES positioned as a variability-modeling language. Its natural peer is therefore the feature model, yet I found no published direct mapping or benchmark of SES against feature models.

### Gaps
- No head-to-head study of SES vs feature models (FODA, cardinality-based, or tools such as FeatureIDE or pure::variants) was found.
- No study of SES vs SysML v1 variant profiles or SysML v2 variability, and none against DSE tools (for example OpenMDAO, ModelCenter or DESERT), was found.
- The full texts of Pawletta et al. 2016 and Durak et al. 2017 (which review SPL and MDE approaches) could not be retrieved; the DLR elib copy returned a non-PDF page.

## Q5. Criticisms, limitations, and open problems

### Takeaway
Most criticism comes from SES practitioners themselves, not outside critics. The recurring issues are:
- Automatic pruning of nested multi-aspects and specializations.
- The explosion of variants or scenarios after pruning, which then need management, prioritization and coverage analysis.
- The lack of a standard computational representation, with fragmented tools.
- A specialized learning curve.
- Limits when scaling to distributed SoS federations.
- Little quantitative evidence of benefit.
- Weak treatment of validating the models that are generated.

### Cited Findings
- **Automatic pruning.** "For the process of automated model generation, it is necessary to provide automatic pruning mechanisms. Especially for some types of nodes comprised in an SES, automatic pruning is a big challenge… pruning SES containing hierarchies of multi-aspect and specialization nodes" — [Folkerts, Pawletta, Deatcu, Zeigler, SpringSim 2020](https://doi.org/10.22360/springsim.2020.mod4sim.001)
- **Standardization and interoperability.** "SES has been applied for various purposes by modeling and simulation community, however, there still exists a lack of standardized computational representation. This hinders the shareability of SES artifacts and interoperability of SES tools" — [Karmokar, Durak, Hartmann, Zeigler, 2019 (DLR elib)](https://elib.dlr.de/130764/)
- **Tool scalability and usability (from SES developers):**
  - "it is currently not scalable and requires several enhancements. Using two tools and managing the pruned models was a critical problem that made the project complicated and hard to use".
  - "The number of scenarios generated can extend to a large number, as the variability factors are many. It requires a mechanism to organize scenarios by labelling and prioritizing them". Scenario metrics are still assigned "manually".
  - Future work includes "a test data generator using machine learning algorithms".
  - Source: [Naja, Gupta, Durak, Hartmann, SNE 34(1):13–21, 2024](https://doi.org/10.11128/sne.34.tn.10672)
- **Manual effort in the SES/ODME scenario approach.** "A significant limitation was the manual effort required to model scenarios to achieve high coverage levels" — [Gupta et al., AIAA SciTech 2026-0208](https://doi.org/10.2514/6.2026-0208)
- **Tool completeness.** As their MATLAB SES toolbox matured, "the necessity to have some generalized examples for testing and verification came more and more into awareness". The authors propose design patterns which, "if completely representable and computable by a certain tool, support all aspects of SES theory" — [Deatcu, Folkerts, Pawletta, Durak, SNE 29, 2019](https://doi.org/10.11128/sne.29.tn.10482)
- **Learning curve and rigidity.** SES "follows more rigid guidelines and restrictions"; "an understanding of SES is required to comprehend and edit these files" — [Jafer et al., JSimE vol. 1, 2018](https://doi.org/10.5281/zenodo.11114155). Against that, students found eSES/MB "a lot easier to get started with" than Simulink Variant Manager — [Deatcu et al., SNE 29(1), 2019](https://doi.org/10.11128/sne.29.sw.10466)
- **Distributed SoS.** "SES/MB is limited in simulating the system of systems using HLA/RTI", which led to the SoSES/FB extension — [B.S. Kim, Choi, T.G. Kim, 2013](https://doi.org/10.5555/2499986.2499990)
- **Expressiveness for large SoS.** The MetaSES was introduced to handle "complex systems of systems with multiple components belonging to specified classes" and to "push the boundaries of complexity management". This implicitly admits limits in the classic SES for class-based, many-component SoS — [Zeigler, Information 14(1):22, 2023](https://doi.org/10.3390/info14010022)
- **Levels of abstraction and time granularity.** Classic SES did not capture these, which needed an extension — [Santucci, Capocchi, Zeigler, SIMULATION 2016](https://doi.org/10.1177/0037549716657168)
- **Multi-simulator semantic gap.** Extending SES/MB to generate models for multiple simulators needed FMI-based infrastructure — [Folkerts, Pawletta, Deatcu, SNE 31, 2021](https://doi.org/10.11128/sne.31.tn.10554). A template extension was needed for automatic model generation — [Martens, Bock, Simanski, Hagendorf, IIPhDW 2019](https://doi.org/10.1109/iiphdw.2019.8755406)
- **Modest measured gains.** The best quantified result found is "up to 8% time savings and up to 23% cost savings as compared to the manual extra effort" — [Nam & Kim, Sensors 2021](https://doi.org/10.3390/s21020430)
- **Combinatorial structure spaces.** Practitioners have handled large structure spaces by coupling SES with metaheuristics (evolutionary or genetic algorithms) rather than enumerating every pruning — [Hagendorf et al. 2013](https://doi.org/10.1177/0037549713500066); [Rentsch et al. 2015](https://doi.org/10.1016/j.procir.2015.06.026)
- **Validating generated models.** SES-generated experimental frames have been used for fidelity evaluation — [Schmidt, Durak, Pawletta 2016](https://doi.org/10.1177/0037549716656791). SES-X uses one of its two pruning levels "for model verification" — [Xie et al. 2023](https://doi.org/10.3390/info14010023). Zeigler's EFSUT draft asserts "SES pruning ensures minimality" and "no over-modeling" without a formal proof or evaluation ([local draft](/home/chris/DEVS/devs-skills-claude/docs/efsut/Three%20Complementary%20Uses%20of%20the%20EFSUT%20Methodology.docx)).

### Inferences
- Combinatorial explosion is acknowledged indirectly: through scenario-management needs, coverage metrics, metaheuristic search and automatic pruning research. I found no paper that quantifies pruning-space growth, or the complexity of constraint (selection-rule) satisfaction in SES, in the open literature. Treating constrained pruning as a SAT/CSP problem looks like an open, unexploited bridge. Zeigler's own 2026 SAT-based synthesis work is adjacent but not applied to SES.
- The semantic gap between the SES (structure) and the model base (behavior) is mostly handled by tool-specific conventions: templates, FMI wrappers, MS4 Me DNL files. The lack of a standard representation (Karmokar 2019) and the need for multi-simulator generation (Folkerts 2021) both point to this gap still being open.
- Adoption seems limited by tooling rather than theory. Several groups keep rebuilding SES editors and pruners (MS4 Me, the MATLAB SES Toolbox, SESToPy, SES/PES Editors, ODME), which matches the standardization critique.

### Gaps
- No independent, external critique of SES/MB (from outside the DEVS community) was found.
- No published benchmark of pruning or constraint-solving scalability (for example, the number of PESs against SES size, or the cost of checking selection constraints) was found.
- No systematic evaluation of how valid, or how close to the referent, SES-generated models are was found, beyond single-case demonstrations.

## Q6. Surveys, reviews, and retrospectives on SES

### Takeaway
I found no dedicated systematic literature review of SES or SES/MB, and no SES-specific anniversary retrospective. The nearest substitutes are:
- Tutorial and review papers from the method's developers (Durak et al. 2017; Pawletta et al. 2019 chapter).
- The book chapters of Zeigler & Sarjoughian's SoS Guide.
- Zeigler's 2023 MetaSES paper.
- Review-style DEVS papers that include an SES section (Capocchi, Zeigler, Santucci 2025).

### Cited Findings
- **Durak et al. 2017.** Reviews SES history, "directed labelled graphs that were proposed in the 1980s for specifying a family of system configurations", and comparable software-engineering approaches, then positions SES/MB for model-based engineering — [Durak, Pawletta, Oğuztüzün, Zeigler, SpringSim Mod4Sim 2017](https://doi.org/10.22360/springsim.2017.mod4sim.001)
- **Pawletta, Durak, Schmidt 2019.** A book-chapter overview of the extended SES/MB infrastructure for versatile technical systems — [in *Model Engineering for Simulation*, Elsevier 2019, pp. 393–418](https://doi.org/10.1016/b978-0-12-813543-3.00018-4)
- **Zeigler & Sarjoughian.** SES basics, pruning, multi-aspects and inheritance chapters — [*Guide to M&S of SoS*, Springer 2013/2017](https://doi.org/10.1007/978-3-319-64134-8)
- **Zeigler 2023.** Unifies SES with the specification hierarchy, discusses "several examples of applications" and suggests research directions — [Zeigler, Information 14(1):22, 2023](https://doi.org/10.3390/info14010022)
- **Capocchi, Zeigler, Santucci 2025.** A DEVS review for IoT/CPS that includes the SES role — [Computers 14(7):258, 2025](https://doi.org/10.3390/computers14070258)
- **Deatcu et al. 2019.** SES design patterns meant as a reference test suite for SES tools — [SNE 29, 2019](https://doi.org/10.11128/sne.29.tn.10482)
- **Broader retrospectives without an SES focus:** [Balci, Fujimoto, Goldsman, Nance, Zeigler, "The state of innovation in modeling and simulation: The last 50 years", WSC 2017](https://doi.org/10.1109/wsc.2017.8247835); [Ören, Zeigler, Tolk (eds.), *Body of Knowledge for Modeling and Simulation*, Springer 2023](https://doi.org/10.1007/978-3-031-11085-6). The SES coverage in either was not checked.

### Inferences
- A systematic review of SES applications would be a new contribution. This note is roughly the raw material for one. Its OpenAlex sweep for "system entity structure" since 2008 returned 258 hits. Of those, 99 distinct titles mention "entity structure" or "SES" in the title or abstract, and a few of those are off-topic noise.

### Gaps
- I could not confirm whether the SCS 2023 *Body of Knowledge for M&S* or the WSC 2017 retrospective contains an SES-specific section.
- Korean- and Chinese-language SES literature (for example, Korea Society for Simulation papers on defense SES/MB, and Beihang's X-language work) was only partly reachable, so coverage of those communities is incomplete.
