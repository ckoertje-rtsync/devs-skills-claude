---
name: "pwsa-expert"
description: "Expert on Proliferated Warfighter Space Architecture (PWSA) mission objectives, outlines, and specifications. Draws from RTSync/VT-NSI Phase I Final Reports and Phase II Technical Volume for the DEVS-PWSA mission engineering engine. Use this skill when answering questions about PWSA mission threads, tranche evolution, DEVS-based performance modeling, morphism frameworks, Bayesian confidence propagation, SDA digital engineering, or any PWSA-related topic."
---

Base directory for this skill: C:\Users\carbo\OneDrive\Desktop\Claude\.claude\skills\pwsa-expert

## What This Skill Does

Serves as an authoritative expert on the Proliferated Warfighter Space Architecture (PWSA) and the DEVS-PWSA mission engineering engine. All answers are grounded in the three reference documents stored in the `resources/` directory. When invoked, this skill reads the relevant PDF(s) and provides accurate, document-sourced answers about PWSA mission objectives, system architecture, technical approach, ontology frameworks, morphism methods, and tranche-evolution reasoning.

## Reference Documents

The following documents are the **primary sources** for all PWSA information. They are stored in the skill's `resources/` directory and should be read using the Read tool when detailed answers are needed.

| Document | File | Pages | Description |
|----------|------|-------|-------------|
| Phase I Final Report (LI0002) | `resources/FA254125PB053_LI0002_FinalReport_RTSync_Oct2025.pdf` | 63 | Earlier Phase I deliverable with proof-of-concept results, feasibility demonstration, use case identification, and system design for DEVS-PWSA |
| Phase I Final Report (LI0003) | `resources/FA254125PB053_LI0003_FinalReport_RTSync_Oct2025.pdf` | 65 | Updated/definitive Phase I Final Report (Contract Line Item 0003). Covers reporting period October 3-31, 2025. Contains detailed morphism workflow, solar flare physics, graph database algorithms, ontology appendices, agentic AI exploration, and notional Phase II SOW. **Prefer this document for Phase I details.** |
| Phase II Technical Volume | `resources/SF24C_T003_Ph2_TechnicalVolume_RTSync.pdf` | 50 | Phase II proposal: 5 technical objectives, 5 tasks, 24-month work plan, Bayesian tranche-evolution engine, commercialization plan |

### How to Access the Documents

Since these are PDF files, use the Read tool with the `pages` parameter to access specific sections. Use Python with PyMuPDF (fitz) for full text extraction if the Read tool's PDF support is unavailable:

```python
C:/Python314/python.exe -c "
import sys, io, fitz
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
doc = fitz.open('[SKILL_BASE_DIR]/resources/[FILENAME].pdf')
for i, page in enumerate(doc):
    text = page.get_text()
    if text.strip():
        print(f'--- PAGE {i+1} ---')
        print(text)
doc.close()
"
```

Replace `[SKILL_BASE_DIR]` with the base directory path and `[FILENAME]` with the PDF filename.

---

## Program Overview

### Contract Information

| Field | Value |
|-------|-------|
| Contract Number | FA254125PB053 |
| Topic Number | SF24C-T003 |
| Title | Strengthening Space: Uniting Research and Innovation for PWSA |
| Performing Organization | RTSync Corp., 6909 W Ray Rd STE 15-107, Chandler, AZ 85226 |
| Phase I Research Partner | Virginia Tech National Security Institute (VT-NSI) |
| Phase II Research Partner | University of Arizona, Dept. of Systems and Industrial Engineering |
| Sponsor | Space Systems Command (SSC), Chantilly, VA |
| TPOC | Mr. Richard Hencke, Dr. Russell Teehan |
| Phase I Value | $304,986 |
| Phase I Period | August 1, 2025 - October 31, 2025 |
| Phase I Report Number | 2025_SF-STTR24C-T003-PH1-03 |
| Phase II Duration | 24 months (proposed) |
| Distribution | Authorized to U.S. Government Agencies only |

### Key Personnel

| Name | Role | Phase I Affiliation | Phase II Affiliation |
|------|------|---------------------|----------------------|
| Dr. Bernard P. Zeigler | Principal Investigator, Chief Scientist | RTSync Corp. | RTSync Corp. |
| Dr. Doohwan Kim | Senior System Engineer, President/CTO | RTSync Corp. | RTSync Corp. |
| Christian Koertje | Research Engineer | RTSync Corp. | RTSync Corp. |
| Dr. Paul Wach | Co-PI (Phase I), PI Arizona (Phase II) | Virginia Tech (VT-NSI) | University of Arizona |
| Dr. Alejandro Salado | Associate Professor, Director of SE | — | University of Arizona |

**Note**: During Phase I, the research partnership was with Virginia Tech National Security Institute (VT-NSI), contributing expertise in test and evaluation, cyberphysical systems, and mission engineering. For Phase II, the academic partnership transitioned to the University of Arizona.

---

## PWSA Mission Context

### What is PWSA?

The **Proliferated Warfighter Space Architecture** (PWSA) is the Space Development Agency's (SDA) plan for a resilient, proliferated constellation of satellites in low Earth orbit (LEO). PWSA evolves through fast, overlapping **tranche cycles**, with each tranche introducing:
- Updated payloads
- New waveforms
- Revised Concepts of Operations (CONOPS)
- Vendor-specific models

SDA must deliver timely, defensible assessments of architecture-level performance and resilience despite this rapid evolution.

### PWSA Layers

The architecture is organized into functional layers:

| Layer | Function | Phase I Coverage | Phase II Coverage |
|-------|----------|------------------|-------------------|
| **Transport Layer** | Intersatellite communications, message routing | Primary focus | Continued |
| **Tracking Layer** | Sensor pointing, custody handoff, missile warning | Not covered | Added in Task 2 |
| **Kill Chain** | End-to-end engagement sequence (tracking to transport to kill) | Referenced | Stretch goal |

### Core Challenge

Current SDA analysis tools are:
- Fragmented across performers
- Inconsistent in fidelity
- Slow to integrate
- Reliant on document-based practices that are static and siloed

A framework is lacking by which existing models can be rapidly integrated to address performance questions as they arise. The current methodology lacks sufficient flexibility to accommodate new models coming online and older models being upgraded or replaced. Scalability is required for questions ranging from individual payloads to full constellation performance. The wide range of fidelity levels and computational burdens requires justified means for tradeoff balances between accuracy and computation times.

The DEVS-PWSA project addresses this by providing a mathematically grounded, computationally scalable mission-engineering environment with four core requirements:

1. **Rapid Integration**: Quickly incorporate existing tools into the DEVS-PWSA framework, supporting iterative development
2. **Flexibility**: Easily accommodate new models and interactions as needs evolve, allowing updates for new tranches/launches
3. **Scalability**: From individual payloads to full constellation performance, supporting both capability-specific and constellation-level analyses
4. **Multi-fidelity Analysis**: Multiplicity of models at selectable scopes and resolution levels to balance computational efficiency with accuracy requirements

---

## DEVS-PWSA Technical Framework

### Architecture Pillars

The DEVS-PWSA mission engineering engine rests on four pillars:

1. **DEVS-Based State-Transition Engine** - Formal, composable representation of system behavior with strict semantics for state, events, and timing. Enables rapid tranche-to-tranche evolution by allowing models to be inserted, replaced, or updated without destabilizing the broader architecture.

2. **High-Fidelity Rare-Event and Resilience Analysis** - Stochastic simulation engine capturing low-probability, high-impact behaviors (link degradation, cyber disruptions, node failures, adversarial interference, multi-domain cascading effects).

3. **Digital-Engineering Ecosystem Integration** - Interfaces with Unified Data Library (UDL), NEBULA, SDA data fabrics. Consumes SysML, MBSE artifacts, and mission-thread definitions.

4. **Scalability** - Optimized for high-performance execution of hundreds to thousands of nodes across multiple domains. Supports real-time and faster-than-real-time simulation.

### Foundational Theories

| Theory | Originator | Purpose |
|--------|-----------|---------|
| DEVS (Discrete Event System Specification) | Zeigler, 1976 | Behavioral modeling formalism (10,000+ publications) |
| SES (System Entity Structure) | Zeigler et al. | Structural hierarchy and model family specification |
| EF (Experimental Frame) | Zeigler | Observation, stimulation, and measurement specification |
| T3SD (Tricotyledon Theory of System Design) | Wymore, 1993 | Coined MBSE; Function, Structure, Resource spaces |
| STOIC (Systems Theoretic Ontological Integration Core) | RTSync/UA team | Ontology family aligning T3SD with DEVS/SES |

### DEVS Background

The DEVS framework stands out for its hierarchical and modular architecture. A system or System-of-Systems (SoS) model consists of individual models, each representing a system component, classified as either **atomic** (indivisible) or **coupled** (comprising other models). DEVS treats agent-based and other simulation elements as specific atomic or coupled models with their state transitions, inputs, and outputs. The framework ensures the integrity of each simulator's fidelity while fostering collaboration between components.

Key capabilities:
- Support for varied M&S paradigms despite differences in state variables or timing mechanisms
- The overarching DEVS simulation protocol ensures correct interaction between diverse components
- Facilitates amalgamation of independently crafted models, promoting efficiency and reusability
- DEVS Markov models support agent-based modeling with reconfigurable transition probabilities and transition times during execution for dynamic structural change
- DEVS Markov models are full-fledged DEVS models that can be coupled with other components in hierarchical compositions

### Key Technologies

| Technology | Description | Role in DEVS-PWSA |
|-----------|-------------|-------------------|
| MS4 Me | RTSync simulation environment | DEVS model execution, visualization, and run-control |
| Neo4j | Graph database (most robust, largest market share) | Knowledge graph storage, model relationship querying, Cypher queries |
| ArcadeDB | Open-source graph database (no monetary cost) | Alternative/complement to Neo4j; can run embedded in JVM |
| Protege | Visual ontology modeling tool | DEVS ontology development and class/relationship definition |
| Rosetta | Textual-code ontology tool | Future: treat ontology as code, enabling Git repos and software analysis |
| OWLApi | Java-based ontology API | Programmatic reading of ontologies for graph database population |
| SpaceNet | VT-NSI distributed space networking software | Future: transport satellite network modeling (stretch goal) |
| ParaDEVS | Branching-time tree-expansion | Stochastic DEVS evaluation, 1000x speedups |
| CAMLI | Collaborative integration layer (MDA-sponsored) | Multi-performer model integration |
| DVAST | Virtual as-is simulation test platform | Software-in-the-loop testing |
| DSFA | DEVS Streaming Framework | Adaptive architecture orchestration |

### Additional Tools for Future Integration

| Tool | Domain | Integration Type |
|------|--------|-----------------|
| Cameo | MBSE/SysML | Model-based systems engineering artifacts |
| MATLAB | Simulation/Math | Script invocation, file/API exchange |
| Ansys STK | Space domain analysis | Orbital mechanics and mission planning |
| Innoslate | Requirements management | Requirements traceability |
| Tensor X | Emerging STTR/SBIR tool | Advanced analysis |
| Sedaro | Emerging STTR/SBIR tool | Satellite digital twin |

---

## Phase I Results Summary

### Primary Objectives

- Elaborate a concept for a prototype, emphasizing M&S to develop DEVS-PWSA for warfighter-relevant scenarios
- Enhance and utilize existing RTSync M&S infrastructure and software, leveraging SoS coupling capabilities
- Enhance and utilize existing VT-NSI infrastructure in conjunction with RTSync infrastructure
- Define/concretize use case(s)
- Conduct analysis of a network node loss scenario
- Demonstrate/illustrate technical feasibility for one use case

### Tasks Completed

| Task | Description | Lead | Status |
|------|-------------|------|--------|
| Task 1 | DEVS Modeling Framework for PWSA Performance Modeling Formulation | RTSync & VT-NSI | Completed |
| Task 2 | Algorithm Development | VT-NSI | Completed |
| Task 3 | Test Environment Development | RTSync & VT-NSI | Completed |
| Task 4 | Feasibility Demonstration | RTSync & VT-NSI | Completed |

### Task 1: DEVS-PWSA Framework

Developed a tranche-aware DEVS modeling family spanning multiple resolutions:

**Multi-Resolution Model Family** (see LI0003 Figure 1):
- **Base Model**: Conceptual representation encompassing all pertinent behaviors, physical principles, and parameters
- **Stochastic Model**: Intermediate fidelity with probabilistic elements
- **Lumped Connectivity Model**: Simplified representation retaining essential parameters
- **DEVS Markov Model**: Lowest resolution; condenses complex physics into simplified metrics (connection probabilities, mean latency values)
- **PWSA Operations Model**: Operational-level representation

Each model uses:
- DEVS atomic models representing individual satellites, ground stations, and disruptors
- Port-to-port coupling following message flow
- Family of models linked by morphisms

**Advantages of Multi-Resolution Models**: High-resolution models provide greater detail and accuracy but require longer computation times; low-resolution models, when correct, execute more swiftly and inform immediate operational decisions while higher-resolution simulations complete.

#### Morphism Automation Tools

Developed tools for automating morphism mappings between DEVS models:
- Model information and morphism mappings stored in **JSON file format**
- When mapping from high to low resolution, the mapping is many-to-one with an expression computing target parameter values from source values
- The expression can be analytic, algorithmic, or derived from simulation and data

**Morphism Applier Workflow** (see LI0003 Figures 2-4):
1. Define the experimental frame and assign parameters for the base model
2. Morphism applier within MS4 Me queries the model ontology
3. Ontology retrieves parameter values from existing dataset or computes them dynamically via expression engine
4. Returns equivalent parameter configuration for the lumped model
5. Lumped model is instantiated with morphically aligned parameter set
6. Results stored in structured JSON format

**Morphism Composition** (see LI0003 Figure 5):
- Morphisms can be composed to form higher-level mappings
- Example: "BaseToConnectivity" morphism + "ConnectivityToMarkov" morphism = "BaseToMarkov" morphism
- This compositional approach streamlines transformation and facilitates consistent parameter mapping across the entire model hierarchy

#### Markov Models for Solar Flare Disruption

To accommodate electronic disruptions such as solar flares, DEVS satellite models update their internal state to reflect disruptive effects (damage to electronic circuitry, interference with electromagnetic transmissions). In the simplified model, **latency degradation** represents the satellite taking longer to transmit messages. The effect is quantified with aggregated Base Model parameter values using the parameter morphism method.

### Task 2: Algorithm Development

Completed algorithms expressing relationships between physics-based deterministic models (base model) and probabilistic Markov models (lumped model). The work represents the convergence of three domains:

1. **Modeling & Simulation** (MS4 Me)
2. **Ontology** (Protege)
3. **Graph-based algorithmic analysis** (Neo4j, ArcadeDB)

Key accomplishments:
- Completed DEVS-based ontology in Protege
- Converted ontology to data structures underpinning the graph databases
- Ran native graphDB analytics demonstrating algorithmic determination of homomorphic equivalence between base and lumped models
- The ontology enables harmonization of data from models in MS4 Systems and the graphDBs
- Notional example: automated translation of TX power to probability of connection between satellites

**Current State**: Requires manual exchange between MS4 Systems and graphDBs (JSON file exchange). Conversion of ontology to graphDB data structures has an established workflow.

**Future Vision**: If probability of connection or mean latency is established as a KPP, the flow-down to the physics of the design will be automated. Working in reverse, changes to the design can be automatically connected to performance against KPPs. For example, transmitter (TX) power automatically connected to probability of connection between satellite nodes, and vice versa.

**Parameter Flow** (see LI0003 Figure 8):
- In MS4 Me, parameters declared as: "Use Frequency with type double..."
- Parameters accessed by ArcadeDB through JSON representation
- Expression in JSON written by ArcadeDB (and Neo4j) that, when executed by MS4 Systems, assigns values to drive simulation

**Neo4j Visualization** (see LI0003 Figure 9):
- Shows homomorphic relationship between Base and Markov models
- Two clusters: larger cluster (physics-based model) and smaller cluster (lumped model)
- Connected by "BaseToLumpedParameterMorphism" relation

### Task 3: Test Environment Development

Created the generic architecture model for the test environment (see LI0003 Figure 10):

**Component Selection**:
- Initially favored ArcadeDB; subsequently evaluated Neo4j benefits
- Concluded that incorporating both databases incurred minimal additional cost
- Both are self-hosted graphDBs enabling storage of models as knowledge graphs
- Both have native Java support through API packages
- Both utilize Cypher as the querying language, simplifying implementation and maintenance
- Neo4j requires an external instance (Neo4j Desktop application)
- ArcadeDB can run embedded within the Java Virtual Machine (JVM) or connect to external container

**Ontology Tooling**:
- Protege used for ontology modeling (sufficient for this phase)
- Straightforward to transfer ontology to graphDB data structures

**Stretch Goals Explored**:
- **Rosetta**: Textual-code ontology tool. Advantage: treating ontology as code enables Git repositories and software analysis capabilities. Deferred to future phases.
- **SpaceNet** (VT-NSI): Distributed Space and Terrestrial Networking Infrastructure for Multi-Constellation Coexistence. Focused on modeling networks of transport satellites (matching the Phase I use case focus). Deferred to future phases as not critical to Phase I outcome.

**Future Vision** (see LI0003 Figure 11):
- Growth from transport layer to transport + tracking + kill chain + acquisition
- Digital engineering ConOps with harmonious data flow through ecosystem
- Agentic AI playing a pivotal role intersecting with M&S, MBSE, and T&E
- Decision maker interfaces for human-machine teaming

### Task 4: Feasibility Demonstration

**Use Case**: Solar flare causing outage/latency in transport

#### Solar Flare Physics Background

Solar flares and associated solar radio bursts introduce significant variability in the near-Earth electromagnetic environment:
- Deposit large amounts of energy into the upper atmosphere, increasing ionization in ionosphere and thermosphere
- The resulting "swelling" amplifies scintillation effects and increases atmospheric drag on LEO satellites
- HF systems experience severe attenuation and even complete radio blackouts during major flares
- Higher-frequency systems (L- and S-bands) exhibit temporary reductions in carrier-to-noise ratio

**Intersatellite Link Impact**:
- For intersatellite links transmitting at higher frequencies (not propagating through atmosphere/ionosphere), effects are **significantly diminished**, especially above 400km altitude
- Solar radio and microwave emissions at spacecraft frequencies can act as **natural jamming**, but only under specific geometric alignments where the Sun lies within the antenna's main beam or strong sidelobes
- Solar irradiance in S-band (~3 GHz) varies significantly during increased solar activity
- During major events: flux can reach **1,000-10,000 SFU** at 3 GHz, producing solar flux density of **10^-19 to 10^-18 W/m^2/Hz**

**Modeling Approach**:
- Most solar radio bursts are short duration (seconds to ~15 minutes), broadband, and incoherent emissions
- Modeled as **noise spectral density** rather than coherent interference signal
- Represented as increase in system noise temperature scaling with solar flux intensity
- Mapped to an **environmental loss parameter within the link budget**, capturing transient increase in system noise temperature

#### Experimental Frame and Scenario

**Scenario Configuration** (see LI0003 Figure 12):
- t=0: Communication message generated, starts message flow
- t=1: Solar flare event impacts satellite along nominal path
- As solar intensity increases, SNR decreases, packet error rate increases, retransmissions increase
- Either makes connection too difficult to establish or adds significant latency

**MS4 Me Realization** (see LI0003 Figure 13):
- Each small box represents a DEVS atomic model
- Arrows indicate port-to-port coupling following message flow

**Test Configuration**:
- Probability of connection = 1.0 (solely examining end-to-end transmission time)
- Single hop transmission latency mapped from varying degrees of transmitter power
- Power consumed by single-hop transmissions loosely related to system cost

**Results** (from 1000 Monte Carlo samples per parameter setting, see LI0003 Figure 14):
- Orange line: no flare (baseline performance at each cost)
- Blue line: solar flare data
- Greater disparity between average end-to-end time for **low-cost systems**
- Three scenario comparison:

| Scenario | Noise | Power | Latency |
|----------|-------|-------|---------|
| Nominal | Low | Standard | Low |
| Solar Flare | High | Nominal | Increased |
| Mitigated | High | Small increase | Restored to nominal |

**Key Finding**: A relatively small increase in power restores nominal latency despite a high noise environment.

**Morphism Applier in MS4 Me** (see LI0003 Figure 15):
- Parameter values mapped from ontology ingested in JSON format
- Operating within run-control framework, applies values from single-hop analysis to instantiate lumped model ("SatCTM")
- X-band base model scenario settings generate corresponding simulation of lumped Markov model
- Enables execution of individual Monte Carlo samples for performance evaluation

### Morphism Modeling Application

A computational tool developed during Phase I to identify and exploit structural morphisms between satellite constellation system models (see LI0003 Section 6.1).

**Purpose**: Establish formal mappings between model representations varying in fidelity, configuration, or environmental conditions while preserving essential behavioral and performance characteristics.

**Core Capabilities**:

1. **Model Management**: Populate and interact with Neo4j and ArcadeDB through ontology-driven graph representations. Retrieve parameter lists, assign/modify parameter values, identify models sharing specific characteristics.

2. **Existing Morphism Detection**: When an existing morphism is identified, directly maps attributes from base to target model. Physical and system parameters mapped include:
   - Constants: speed of light, Boltzmann's constant
   - Operational: frequency, bandwidth, antenna characteristics, system noise temperature, environmental losses, spectral efficiency

3. **Performance Metrics Computed**:
   - Wavelength
   - Free-space path loss (FSPL)
   - Aggregate losses
   - Received power
   - Signal-to-noise ratio (SNR)
   - Bit rate
   - Energy-per-bit-to-noise ratio (Eb/N0)
   - Bit error rate (BER)
   - Packet error rate (PER)
   - Success rate
   - Transmit latency

4. **Definable Morphism Discovery**: When an existing morphism cannot be established, searches for a **definable morphism** by examining structural proximity in graph space. Missing attributes in the target model are inferred from the closest related model. Results interpreted with awareness of approximation uncertainty.

5. **Output**: All results exported in structured, machine-readable **JSON format** including attributes and computed values for both models, morphism type (existing or definable), and mapping details. Designed for integration with external analysis tools, simulation environments, or data visualization pipelines.

**Applications**:
- Nominal vs. degraded condition transitions (e.g., solar flare activity)
- Changing network configurations
- Orbital geometry variations
- Antenna alignment impact assessment
- Cross-model performance and reliability quantification

### PWSA Loss Scenario Ontology Mapping

| PWSA Concept | Wymore SDL | DEVS Formalisms |
|-------------|------------|-----------------|
| 8x satellite NTN | Set of FSD, coupled into BSD | Set of IO Systems, coupled into Network |
| 2x ground station | FSD, may be coupled into BSD | IO System, coupled into Network |
| 1x unfriendly disruptor | FSD, may be coupled into BSD | IO System, coupled into Network |
| Link16 comms | Parameterize IO | Parameterized IO |
| Extension laser mediated communication | Parameterize IO | Parameterized IO |
| Nominal communication | BSD of satellites + ground stations | Network of coupled systems |
| Off-nominal communication | BSD + unfriendly disruptor | Network + disruptor |
| Rerouting | Homomorphism between nominal/off-nominal | Homomorphism mapping |

---

## Homomorphism and Parameter Morphisms

### Homomorphism Concept

A homomorphism is centered on a correspondence between base and lumped model states. The correspondence is defined by a mapping from base model states to lumped model states. The mapping becomes a homomorphism if it is **preserved under transitions**: for every base state and its corresponding lumped state, the respective states to which they transit also correspond under the mapping.

### Domain-Specific Meanings When Homomorphism EXISTS

| Meaning | Description |
|---------|-------------|
| **Functional Preservation** | Satellite tasking flows, inter-satellite links, and control paths appear equivalently in morphically related models |
| **Design Reuse** | A common system pattern (e.g., resilient communications mesh, tasking topology) can be applied across multiple constellation configurations |
| **Scaling Validation** | A smaller, validated model can be faithfully mapped into a larger or future PWSA model, supporting design upgrades and phase transitions |
| **Architecture Validation** | Confirms that proposed updates or variations continue to support the original warfighting mission and architectural intent |
| **Traceability and Compliance** | Shows that new system variants remain consistent with baseline DEVS artifacts |
| **Interoperability Assurance** | Demonstrates that coalition partner models and PWSA designs align to validate joint mission readiness |

### Domain-Specific Meanings When Homomorphism CANNOT Be Established

| Indicator | Description |
|-----------|-------------|
| **Structural Gaps** | Missing communications links or control paths that break mission-critical connectivity |
| **Behavioral Mismatches** | A satellite in one model performs functions (data relay, threat detection) with no equivalent counterpart in the other |
| **Broken Orbital Logic** | Ground-to-space tasking chains or orbital adjacency assumptions fail to map, undermining coverage or coordination |
| **Verification Failure** | New architecture version does not meet mission objectives captured in reference model |
| **Design Divergence** | Differences between design intent and implementation reflect architecture drift or unmet requirements |
| **Loss of Simulation Prediction Validity** | Transformations from high-resolution models to fast-running DEVS Markov models lose accuracy, producing incorrect behavior within the experimental frame |

### Parameter Morphisms

Simulation and analytic models are generally structured with parameters whose values determine their specific behaviors. **Parameter morphisms** are homomorphisms that map parameter values from one model to another such that the models structured with the corresponding parameter values are homomorphically related.

### VV&A Application

The concept of homomorphism is key to the proposed approach to Verification, Validation, and Accreditation (VV&A). Two design spaces are considered:
1. **Space of System Requirements** (functional system design)
2. **Space of Buildable System Designs** (constrained by available technology)

A homomorphism from the buildable space to the requirements space establishes an implementable system design used as a basis for verification. Morphisms also propagate information gathered from test exercises on one model to others morphically related to it, building confidence in the family as a whole.

---

## DEVS Ontology Details

### Ontology Structure (Phase I)

The DEVS ontology was developed in Protege, focused on defining appropriate levels of detail for the DEVS IO System (Base) model and DEVS Markov (Lumped) model. The ontology is based on Zeigler's Theory of Modeling and Simulation (3rd ed., 2018).

**Components** (see LI0003 Appendix C, Figures C1-C5):
- **Class Structure (Taxonomy)**: Defines DEVS classes hierarchically
- **Object Properties**: Defines possible relationships between classes
- **Individuals**: Instantiated from the PWSA use case with SDA-relevant context
- **IO System Detail**: Captures specific algebraic structures and instances of IO System models
- **Markov Model Detail**: Captures specific algebraic structures and instances of Markov models

### Graph Database Integration

The ontology-to-graph-database pipeline works as follows (see LI0003 Appendix D):

1. **Ontology Creation**: DEVS classes, subclasses, and components defined in Protege
2. **OWLApi Processing**: Java application reads the ontology using OWLApi
3. **Graph Population**: Each DEVS class stored as a node; subclass and component relationships stored as edges
4. **Individual Storage**: Ontology individuals (model instances) stored with all relationships
5. **Querying**: Cypher queries retrieve model parameters, homomorphic relationships, and mappings
6. **Dynamic Morphism Loading**: Java reflection dynamically loads morphism classes based on graph edge labels (e.g., "BaseToLumpedMorphism")
7. **Conversion**: Loaded morphism class computes target model parameters from source model parameters
8. **Persistence**: Input and target model instances stored in database for later retrieval

**Approximation for Incomplete Models**: When input models are incomplete, similarity search on previously inserted model instances populates missing input fields.

---

## Phase I Notional Phase II SOW

The Phase I Final Report (LI0003) includes a notional Statement of Work for Phase II, structured as:

### Task 1: Use Case Expansion
- Extend PWSA focus from transport layer to include tracking layer
- Assess feasibility of incorporating kill chain (tracking -> transport -> kill) in later phases
- Elaborate physics-based model for cost-informed mitigation strategies
- Increase model fidelity from 2D abstractions to multidimensional networks

### Task 2: Software and Toolset Expansion
- Extend DEVS-PWSA framework with additional PWSA elements
- Enhance algorithms with additional algebraic structures and improved mapping
- Upgrade morphism modeling tool for discovery and evaluation of definable morphisms
- Streamline workflows (reduce manual JSON file exchanges between MS4 Me and graphDB)
- Expand architecture to integrate additional tools (Cameo, MATLAB, Ansys STK, Innoslate, Rosetta, Tensor X, Sedaro)
- Develop notional agentic AI framework with orchestrator model

### Proposed Deliverables
- Updated PWSA use case documentation including tracking layer and kill chain roadmap
- Enhanced physics-based model supporting cost-informed mitigation strategies
- Multidimensional network models demonstrating SDA realism
- Extended DEVS-PWSA framework and upgraded algorithms
- Enhanced morphism modeling application with validation results
- Streamlined workflow processes and tool integrations
- Conceptual architecture and design documentation for agentic AI orchestrator

---

## Phase II Technical Objectives

### Objective 1 - Establish Formal Substrate for Tranche-Evolution Reasoning

**Lead**: University of Arizona

**Scope**:
- Extend STOIC ontology family with PWSA-specific mission concepts
- Instantiate Homomorphism Library for representative subsystems
- Define Bayesian morphisms quantifying structural/behavioral similarity
- Create evidence-type provenance ontology aligned with PROV-O

**Key Outputs**:
1. PWSA-extended STOIC ontology family
2. Homomorphism Library instantiation with degradation measures
3. Bayesian morphism framework specification
4. Evidence type provenance ontology (PROV-O aligned)
5. Draft journal manuscript

### Objective 2 - Expand DEVS-PWSA and Build Ingestion Backbone

**Lead**: RTSync

**Scope**:
- Extend DEVS-PWSA to Tracking Layer (sensor pointing, custody handoff, missile-warning)
- Implement multi-tool digital-engineering ingestion pipeline
- Automate bidirectional model-to-graph-database bridge
- Conduct mid-program capability demonstration

**Key Outputs**:
1. Extended DEVS-PWSA framework (Transport + Tracking)
2. Validated multi-tool ingestion pipeline
3. Automated bidirectional model-graph-database bridge
4. Mid-program capability demonstration package

### Objective 3 - Deliver Automated KPP Reverse-Flow and V&V Traceability

**Lead**: RTSync

**Scope**:
- Implement agentic-AI orchestrator baseline
- Automate bidirectional KPP-to-design-parameter mapping
- Apply digital-thread methodology using Mission Thread Ontology
- Surface traceability gaps, test coverage, and morphism-based degradation

**Governance**: Propose-then-validate discipline - no proposal committed until passing two-tier validation gate (SHACL conformance, dual-reasoner consistency, competency-query coverage)

**Key Outputs**:
1. Agentic-AI orchestrator with KPP reasoning
2. Automated KPP-design parameter mapping
3. V&V traceability engine
4. Gap-analysis reports

### Objective 4 - Integrate Scenario Execution and Deploy Digital-Engineering Environment

**Lead**: RTSync

**Scope**:
- Integrate scenario-execution and visualization layer
- Deploy ontology-maintenance pipeline as continuous-validation gate
- Host integrated capability on UA Digital Engineering Factory (DEF)
- Conduct cross-domain harmonization pilot

**Key Outputs**:
1. Integrated DEVS-PWSA on UA DEF
2. Scenario configurations for Tranche 2 and Tranche 3
3. Operational ontology-maintenance and validation pipeline
4. Cross-domain harmonization pilot report

### Objective 5 - Deliver Bayesian Tranche-Evolution Engine and Flagship Demonstration

**Lead**: Joint UA + RTSync

**Scope**:
- Operationalize Bayesian tranche-evolution engine
- Apply digital-thread methodology to tranche-evolution sensitivity analysis
- Execute flagship Tranche-1 to Tranche-2 demonstration
- Produce SDA transition package

**Key Innovation**: When new tranche data updates only part of the architecture, the Bayesian engine updates confidence only where evidence warrants it, preventing unintended belief shifts across unrelated subsystems.

**Key Outputs**:
1. PWSA Bayesian tranche-evolution engine
2. Tranche-evolution sensitivity-analysis results
3. Flagship demonstration package
4. Transition package and final technical report

---

## Phase II Program Schedule

| Milestone | Month | Deliverable |
|-----------|-------|-------------|
| Task 1 Kickoff: Formal Substrate Initialization | +2 | Requirements analysis, ontology extension plan |
| STOIC Extensions | +4 | STOIC extensions report |
| Task 1 Completion: Formal Substrate Established | +6 | Homomorphism Library, Bayesian morphism definitions, provenance ontology |
| Task 2 Kickoff: Tracking Layer + Ingestion Pipeline | +8 | Tracking Layer SES structure, ingestion pipeline architecture |
| Task 2 Midpoint: Multi-Tool Ingestion Backbone Operational | +10 | Working ingestion adapters, model-graph database bridge, Tracking Layer DEVS models |
| Task 2 Completion: Integrated Tracking Layer | +12 | Fully integrated Tracking Layer DEVS-PWSA environment |
| Task 3 Kickoff: Agentic AI Orchestrator + KPP Reverse-Flow | +14 | Initial orchestrator integration with MBSE repositories and graph store |
| Task 3 Completion: Automated KPP Reverse-Flow + V&V Traceability | +16 | Automated KPP-design mapping, V&V traceability engine, gap-analysis reports |
| Task 4 Kickoff: Scenario Engine + Ontology Maintenance | +18 | Unified scenario-execution environment architecture |
| Task 4 Completion: Digital Engineering Factory Deployment | +20 | Fully integrated scenario engine, ontology pipeline, visualization tools |
| Task 5 Kickoff: Bayesian Tranche-Evolution Engine | +22 | Bayesian update engine prototype, tranche-evolution sensitivity analysis |
| Flagship Demonstration: Tranche-1 to Tranche-2 | +23 | End-to-end demonstration package |
| Final Delivery: Transition Package + Documentation | +24 | Final Technical Report, software, training materials |

---

## Morphism Quality Framework

### Two-Axis Distance Metric

Morphism quality is captured on two orthogonal axes:

- **StructuralDistance (x-axis)**: How many distinct real-world states or inputs are lumped together by the lower-fidelity model
- **BehavioralDistance (y-axis)**: Worst-case output discrepancy between linked specifications

### Quadrant Interpretation

| Quadrant | Structural | Behavioral | Interpretation |
|----------|-----------|------------|----------------|
| Ideal | Low | Low | Near-isomorphic: both structure and behavior preserved |
| Coarse but Safe | High | Low | Safe simplification (lumped-parameter model) |
| Precise but Wrong | Low | High | Numerical-error buildup (discretization failure) |
| Divergent | High | High | Operationally concerning: no structural or behavioral correspondence |

### ParaDEVS Integration

Phase II incorporates the ParaDEVS branching-time tree-expansion method to ensure morphism-quality metrics remain scalable for stochastic DEVS models. The merging step prunes behaviorally equivalent branches, and worst-case output discrepancy is estimated over inputs sampled from the experimental-frame distribution.

---

## Bayesian Tranche-Evolution Framework

### Mathematical Foundation

Rests on the Mathematical Systems Engineering Metamodel and its hierarchy of system specifications. Each node in the Bayesian network represents a stage of PWSA system evolution:
- Analytical models
- Lower-fidelity simulations
- Higher-fidelity simulations
- Digital twins
- Fielded operational systems

Edge probabilities are parameterized by morphism quality (the formal measure of equivalence between linked specifications).

### Evidence Processing

New test data, vendor model updates, and tranche scope changes are absorbed as evidence at the appropriate node. The framework uses d-separation-based information-flow rules to propagate updates only through relevant paths, preventing unintended belief shifts.

### Evidence Types

| Type | Description |
|------|-------------|
| Established | Direct test/observation data |
| Inferred | Derived from morphism-based reasoning |
| Synthetic | Generated from simulation models |

---

## Envisioned Operational Use Cases

### 1. Rapid Vendor Model Update Integration

During tranche cycles, vendors deliver late-cycle updates. The morphism engine quantifies differences from prior versions, and the Bayesian engine updates confidence only for affected subsystems. Result: hours-not-weeks assessment of KPP shifts and new risks.

### 2. Mission-Thread Impact Analysis Under Node Loss or Cyber Disruption

Analysts configure degraded-mode scenarios, execute multi-domain DEVS models, and observe mission-thread impacts on latency, coverage, and continuity. The Bayesian engine highlights which subsystems drive the degradation.

### 3. Automated KPP Reverse-Flow for Requirements Coverage

The reverse-flow engine traces dependencies through the Mission Thread Ontology, identifying untested requirements, missing evidence, and design parameters with outsized impact on specific KPPs (e.g., detection latency, custody duration).

### 4. Cross-Vendor Comparison and Architecture-Level Trades

The morphism engine provides quantitative comparison of structural and behavioral fidelity across competing vendor models. The scenario engine evaluates mission-thread performance under each vendor's assumptions.

---

## MBSE and Science-Based VV&A

### Digital Transformation Context

The defense industry began its digital transformation after the release of the digital engineering strategy in 2018. True transformation is still emerging. Even within the same set of tools, different organizations have varied implementations, leading to a lack of reuse and interoperability. This is particularly pronounced in mission engineering, where interdependent organizations must collaborate to develop, operate, and maintain systems.

**Key Problem**: Models created in MBSE tools (SysML, Cameo) often adhere to organization-specific "style guides." MBSE lacks a common underlying mathematical theory (unlike CFD and FEA). SysMLv2 has ontological underpinnings but lacks a mathematical foundation.

**Solution**: Two mathematical frameworks proposed for integration:
1. **T3SD** (Tricotyledon Theory of System Design) - coined the term MBSE
2. **DEVS** (Theory of Modeling & Simulation) - widely recognized with 10,000+ publications

The DEVS-PWSA project applies research toward integrating T3SD and DEVS in the space domain to achieve digital test and evaluation (dT&E) for SDA. Our methods advance V&V and T&E, including proving sufficient conditions for selection of verification models that are inherently valid due to their mathematical underpinning.

### Three Phases of dT&E

The project leverages past DOT&E-associated work proposing phases of digital transformation of T&E, with emphasis on phase three where data and models are underpinned by mathematical theory.

### Expected Contributions

Once applied, these frameworks will contribute toward:
- Standardized data and models
- Wargaming
- Advanced threat modeling and analysis
- Loss of capability assessment and mitigation
- Documentation of performance
- Identification of patterns

---

## Agentic AI Integration

### Generative AI vs. Agentic AI

The 2025 transition from passive generative AI to active agentic AI is central to DEVS-PWSA's future direction:
- **Generative AI**: Only recalls information within knowledge base; may hallucinate predictions
- **Agentic AI**: Leverages tools in its orchestration toolbox to compute results; uses LLMs as interface to humans and orchestrator for software platform ecosystems

Agentic AI is the desired technological path for pairing with DEVS-PWSA technology.

### Phase I Exploration

Research centered on LangGraph orchestration framework with Model Context Protocol (MCP) for tool integration. Envisioned tools include:

| Domain | Tools | Nature | Integration |
|--------|-------|--------|-------------|
| Signal/Comms | GNUradio | Stream-oriented | Real-time dataflow, RPC bridge |
| Simulation/Math | MATLAB, MS4Me | Batch/iterative | Script invocation, file/API exchange |
| Design/Geometry | CAD | Visual, parametric | API or model-file exchange |
| Semantic/Ontological | Protege (OWL) | Knowledge reasoning | RDF/SPARQL, semantic API |
| Graph/Database | Neo4j, ArcadeDB | Persistent, structured | REST or Bolt queries |

### MCP Type Evaluation

| MCP Type | Core Idea | Fit Assessment |
|----------|-----------|----------------|
| Local (Monolithic) | All tools in one environment via local APIs | Not enough; too limited for multi-domain workflows |
| Service-oriented (SOA) | Each tool as network service (REST/gRPC) | Good baseline; extend with schema registry |
| Schema-driven (Semantic) | Ontology-based schemas for capabilities | Excellent fit, especially with Protege/Neo4j |
| Event-driven/Async | Message queues or event bus for long-running tasks | Needed for GNUradio and MATLAB integration |
| Federated/Distributed | Multiple MCP nodes each managing their own domain | Best overall structure for the use case |

### Recommended MCP Architecture

**Federated Semantic MCP** combining the latter three types to maximize capabilities:
- Service-oriented MCP for tool exposure as network services
- Schema-driven (Semantic) MCP for ontology-based cross-domain reasoning
- Event-driven/Async MCP for real-time simulation workflows

### Tool-MCP Adapter Pairings

| Tool | MCP Adapter Type | Example Interface |
|------|-----------------|-------------------|
| GNUradio | Streaming adapter | gRPC or ZMQ endpoint; MCP node subscribes to data topics |
| MATLAB | Batch adapter | Python-MATLAB Engine API wrapper |
| CAD | File or API adapter | Parametric CAD API or neutral file export (STEP, IGES) |
| Protege | Semantic adapter | OWL API or SPARQL endpoint integration |
| Neo4j/ArcadeDB | Data adapter | Bolt or REST API query endpoint |
| MS4Me | Multiscale simulation adapter | SOAP/REST or co-simulation bridge via FMI/FMU standard |

### Additional Integration Options
- **FMI/FMU** (Functional Mock-up Interface): Unify MATLAB and MS4Me simulations
- **RDF-based context serialization**: Makes every MCP exchange machine-semantic

### Phase II Agentic AI Role

Implements a propose-then-validate discipline:
- Coordinates deterministic tools
- Proposes model and mapping modifications
- No proposal committed until passing two-tier validation gate
- SHACL conformance, dual-reasoner consistency, competency-query coverage required

---

## Mission Thread Ontology

Developed and validated in recent STIDS Conference work:
- 17 classes and 18 object properties
- Aligned with BFO and Common Core Ontologies
- 10-shape SHACL graph for closed-world validation
- 12-question competency-question catalog
- Independently validated against authentic third-party mission-engineering model

Phase II extends the Mission Thread Ontology with PWSA-specific concepts for Tasks 3 and 5.

---

## Risk Register (Phase II)

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| External tool dependencies in ingestion pipeline | Medium | Medium | Modular adapter interfaces; stable version targets locked at kickoff |
| Scope creep in cross-domain harmonization pilot | Medium | Medium | Pilot scoped to PWSA + one partner domain; quarterly scope review |
| Agentic-AI standards evolution | Medium | Medium | Lock to specific protocol version; modular adapter pattern |
| Autonomous agent modifications need semantic constraints | Medium | Medium | Semantic validation gates auto-correct to ensure formal validity |
| Large network simulations cause lag | Medium | Medium | Morphism-based simplification/abstraction chain |
| Schedule pressure on flagship demonstration | Medium | High | Baselined into Year 2; Quarter 5 integrated demo serves as rehearsal |
| Multi-institutional coordination (UA + RTSync) | Low | Medium | Established collaboration with clear lead-and-support roles per Task |

---

## Competitive Landscape

### RTSync Advantages Over Competitors

| Advantage | Detail |
|-----------|--------|
| Compute Cost | 70-90% lower (CPU-only, no GPU training pipelines) |
| Analysis Speed | 50-80% faster via automated KPP reverse-flow |
| Model Ingestion | Hours-not-weeks via multi-tool backbone |
| Reasoning | Mathematically grounded (morphisms + Bayesian updates) |
| Transparency | Full auditability, unlike opaque ML-based approaches |

### Competitors

AGI/Ansys STK, AFSIM-based integrators, LinQuest, Parsons, BlueHalo, Raytheon digital-engineering toolchains

### Market Size (Initial Estimates)

| Market | Potential Size | 1% Penetration |
|--------|---------------|----------------|
| Defense | $600M | $6M |
| Engineering | $200M | $2M |
| Cybersecurity | $32M | $320K |
| Manufacturing | $37M | $370K |
| **Total** | **$869M** | **$8.7M** |

---

## Intellectual Property

RTSync holds two granted patents:
1. **US Patent 12,124,778 B1**: "Method for Improving the Simulation of Complex Stochastic Systems"
2. **US Patent 11,922,102 B1**: "Method to Integrate Diverse Components for Simulation of Complex Systems"

Additional patent filing planned by November 2026 for DEVS-PWSA innovations.

---

## Related Programs

| Program | Sponsor | Value | Relevance |
|---------|---------|-------|-----------|
| SF24B-T009 Phase I | SDA | $179,992 | Digital life-cycle integration, PT&E for PWSA |
| SF24C-T003 Phase I | SDA | $304,986 | DEVS-PWSA framework foundation (this project) |
| MDA22-T001 Phase II (ParaDEVS) | MDA | $1,509,996 | Paratemporal simulation, 1000x speedups |
| AF211-CSO1 Phase II | AFRL | $749,996 | DEVS-MBSE predictive analytics |
| MDA18-012 2nd Phase II (CAMLI) | MDA | $1,809,990 | Integrated simulation architecture |
| MDA20-T002 2nd Phase II (DVAST) | MDA | $2,087,996 | Virtual as-is simulation testing |
| ATC-Gen (Link 16) | JITC/DISA | $4,663,041 | Automated test case generation |

---

## Phase I References

The Phase I Final Report (LI0003) includes 46 references. Key references include:

| # | Citation | Relevance |
|---|---------|-----------|
| 1 | Abdurrahman & Sarjoughian, Model-Driven Time-Accurate DEVS-Based Approaches, 2018 | DEVS methodology |
| 6 | Ceylan et al., STAMP approach to analyse socio-technical systems, 2021 | Systems-theoretic analysis |
| 8 | De Saqui-Sannes et al., Taxonomy of MBSE approaches, IEEE Access, 2022 | MBSE landscape |
| 13 | Imbert et al., Combining DEVS simulation and ontological modeling, Simulation, 2023 | DEVS-ontology integration |
| 17 | GAO-25-106838, Laser Communications for SDA, Feb 2025 | SDA laser comms assessment |
| 22 | SDA, PWSA Link-16 Mission Thread for USINDOPACOM | PWSA reference architecture |
| 27 | Seo, Zeigler & Kim, DEVS Markov Modeling and Simulation, 2018 | DEVS Markov formalism |
| 34 | Wach et al., Conjoining Wymore's Framework and DEVS, Applied Sciences, 2021 | T3SD-DEVS integration |
| 35 | Wach et al., Pairing Bayesian Methods and Systems Theory, INSIGHT, 2022 | Bayesian V&V |
| 37 | Wymore, Model-Based Systems Engineering, CRC Press, 1993 | T3SD foundation |
| 39 | Zeigler, Muzy & Kofman, Theory of Modeling and Simulation (3rd ed.), 2018 | Core DEVS reference |
| 45 | Zeigler, DEVS and MBSE: A Review, 2022 | DEVS-MBSE integration |

For the complete reference list, see the LI0003 PDF pages 23-25.

---

## Glossary

| Acronym | Definition |
|---------|-----------|
| AFSIM | Advanced Framework for Simulation, Integration, and Modeling |
| AI | Artificial Intelligence |
| BER | Bit Error Rate |
| BFO | Basic Formal Ontology |
| CAMLI | Collaborative Development Architecture for Model Level Integration |
| CONOPS | Concept of Operations |
| DE | Digital Engineering |
| DEF | Digital Engineering Factory |
| DEVS | Discrete Event Systems Specification |
| DoD | Department of Defense |
| DOT&E | Department of Operational Test and Evaluation |
| dT&E | Digital Test & Evaluation |
| dTEMP | Digital Test and Evaluation Master Plan |
| DVAST | DEVS Virtual As-Is Simulation Test Platform |
| Eb/N0 | Energy-per-bit-to-noise ratio |
| EF | Experimental Frame |
| FMI/FMU | Functional Mock-up Interface / Functional Mock-up Unit |
| FSPL | Free-Space Path Loss |
| GAO | Government Accountability Office |
| graphDB | Graph Database |
| IDSK | Integrated Decision Support Key |
| KPI | Key Performance Indicator |
| KPP | Key Performance Parameters |
| LEO | Low-Earth Orbit |
| LLM | Large Language Model |
| MBSE | Model-Based Systems Engineering |
| MCP | Model Context Protocol |
| MDA | Missile Defense Agency |
| ME | Mission Engineering |
| MoP | Measures of Performance |
| M&S | Modeling and Simulation |
| MS&A | Modeling, Simulation, and Analysis |
| NTN | Non-Terrestrial Network |
| OCT | Optical Communication Terminals |
| OWL | Web Ontology Language |
| PER | Packet Error Rate |
| PROV-O | Provenance Ontology |
| PT&E | Phased Test and Evaluation |
| PWSA | Proliferated Warfighter Space Architecture |
| RAG | Retrieval Augmented Generation |
| RDF | Resource Description Framework |
| RF | Radio Frequency |
| SDA | Space Development Agency |
| SE | Systems Engineering |
| SES | System Entity Structure |
| SFU | Solar Flux Unit |
| SHACL | Shapes Constraint Language |
| SLM | Small Language Model |
| SNR | Signal-to-Noise Ratio |
| SoS | System of Systems |
| SSC | Space Systems Command |
| STOIC | Systems Theoretic Ontological Integration Core |
| SysML | Systems Modeling Language |
| T3SD | Tricotyledon Theory of System Design |
| T&E | Test & Evaluation |
| V&V | Verification and Validation |
| VT-NSI | Virginia Tech National Security Institute |
| VV&A | Verification, Validation, and Accreditation |

---

## How to Use This Skill

### When Invoked

1. Read this SKILL.md first for the comprehensive knowledge base
2. If the user's question requires specific details not covered here, read the relevant PDF from `resources/` using the extraction method described above
3. Always cite the source document when providing information
4. For Phase I details, prefer the LI0003 report as the definitive source

### Example Questions This Skill Can Answer

- "What are the PWSA mission objectives?"
- "Describe the DEVS-PWSA architecture"
- "What is the Bayesian tranche-evolution engine?"
- "How does the morphism quality framework work?"
- "What use cases were demonstrated in Phase I?"
- "What are the Phase II technical objectives?"
- "How does the agentic AI orchestrator work?"
- "What is the STOIC ontology?"
- "What are the risks identified for Phase II?"
- "How does DEVS-PWSA compare to competitors?"
- "What is the Mission Thread Ontology?"
- "What tools are used in the test environment?"
- "Explain the solar flare feasibility demonstration"
- "What is the Phase II program schedule?"
- "What commercial applications does DEVS-PWSA support?"
- "What is a homomorphism and when does it fail?"
- "What performance metrics does the morphism modeling application compute?"
- "How do graph databases integrate with the DEVS ontology?"
- "What is the difference between existing and definable morphisms?"
- "How does DEVS-PWSA support VV&A?"
- "What is the morphism composition approach?"
- "What MCP architecture is recommended for agentic AI?"

### When to Refer to the PDFs Directly

- For exact figures, diagrams, or visual content (LI0003 has 17 figures in main body + appendix figures)
- For specific numerical results from Monte Carlo simulations
- For detailed ontology class structures and object properties (LI0003 Appendix C, Figures C1-C5)
- For graph database query examples and visualizations (LI0003 Appendix D, Figures D1-D4)
- For exact contract terms, deliverables schedules, or budget details
- For personnel publication lists
- For detailed algorithm descriptions or mathematical formulations
- For the complete 46-reference bibliography (LI0003 pages 23-25)
- For presentation slides from the October 30, 2025 final meeting (LI0003 Appendix F)
