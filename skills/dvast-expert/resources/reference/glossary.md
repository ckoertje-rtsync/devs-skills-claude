# DVAST Glossary

Terms and acronyms for the DVAST project (MDA20-T002 second Phase II). Every entry has a provenance tag:

| Tag | Meaning |
|-----|---------|
| **[G]** | Verbatim from the proposal's own glossary, on its first page. |
| **[T]** | Defined, or made clear by usage, in the proposal body. The section, figure or appendix is given. |
| **[B]** | Standard background meaning. The proposal uses the term without defining it, so it is **not** a proposal claim. Label it as background if you repeat it. |
| **[S]** | From the post-award quarterly status reports (QSR #1–#3, see `revised-plan.md` §0). These are the team's reports, not the proposal. |

Task IDs such as "Task 1" or "Subtask 1.3" in [G]/[T] entries are **proposal** IDs. The revised plan renumbers them (crosswalk in `revised-plan.md` §3). Section references use the proposal's own numbering: (1) Significance, (2) Technical Objectives, (3) Work Plan, (4) Related Work, (5) Future R&D, (6) Key Personnel, (10) Commercialization, (12) References, and Appendices 1–2.

---

## A. The proposal's glossary (verbatim, 14 terms)

| Term | Expansion [G] | Notes |
|------|---------------|-------|
| **BOA** | Ballistic Missile Defense System Overhead Persistent Infrared Architecture | The MDA software system that proposal Task 1 (revised T1) virtualizes, producing "BOA/DVAST". Written "BMDS OPIR Architecture (BOA)" in (10). |
| **DEVS** | Discrete Event System Specification | |
| **DSP** | DEVS Simulation Protocol | The body spells it out ("DEVS Simulation Protocol") and never uses the acronym. |
| **DVAST** | DEVS-based Virtual AS IS Simulation Test Platform | The title spells it "DEVS-based Virtual As Is Simulation Test Platform". |
| **EDISS** | End to end Digital Integrated System-level Simulation | Named in (10) as "an ideal target program for the DVAST support". TESLA was applied to BOA to build a representation for EDISS. |
| **DMBSE** | DEVS-based Model Based System Engineering | In the glossary only. The body never uses it. |
| **KVM** | Kernel based Virtual Machine | The Linux hypervisor studied in Phase I and rejected as the base for modification (see `history.md`). |
| **MBSE** | Model Based System Engineering | |
| **MDA** | Missile Defense Agency | The sponsor. |
| **M&S** | Modeling & Simulation | |
| **MS4 Me** | RTSync's DEVS-based M&S Development environment | Used for a proof-of-concept implementation of the sequential protocol design (the proposal does not say which phase) and for the Phase I architectural framework in Appendix 1. |
| **TCIS** | Time Controlled Instruction Stream | In the glossary only. The body never uses it. |
| **TESLA** | Tactical Emulation for Software-in-the-Loop Assessment | MDA's QEMU-based framework, developed at ORNL. DVAST is intended to replace it. |
| **VCPU** | Virtual/clock CPU | The body writes "vCPU" in lower case and uses it for the *virtual* CPU (for example "KVM virtual CPUs (vCPUs)", App. 2). |

---

## B. DVAST core concepts

| Term | Meaning | Tag / where |
|------|---------|-------------|
| **"As is" software** | Unmodified, operationally relevant tactical software, executed on a *model of hardware* so that it can take part in a discrete event simulation. | [T] (1) |
| **Non-real-time simulation** | Simulation whose time does not track wall-clock time. It may run faster or slower. "Time in non-real-time simulation does not flow naturally but requires a simulation coordinator to determine the advance of time." | [T] (1), App. 2 |
| **Sequential simulation protocol** | The DVAST form of the DEVS simulation protocol. The coordinator first gets (ta, out) from the simulator of the other models, then sends (ta, in) to the as-is simulator, which runs until it produces an output or reaches the time bound. Its main advantage is that the as-is simulator "does not have to remember the state of its model as it marches the model state forward". See `architecture.md`. | [T] (1), Fig. 1 |
| **Simulator for As Is Model** | "Simulator 2" in Fig. 1. It wraps the virtualized software and marches it forward under a time-advance bound. | [T] Fig. 1 |
| **DEVS Simulator for Other Models** | "Simulator 1" in Fig. 1. It covers every other DEVS model in the simulation. | [T] Fig. 1 |
| **DEVS Coordinator / Simulation Time Manager (STM)** | The component that translates outputs to inputs through the coupling and controls the advance of simulation time. Fig. 2 labels it "DEVS Coordinator Simulation Time Manager (STM)". | [T] Fig. 1, Fig. 2 |
| **ADEVS As-Is CPU Simulator** | The ADEVS-based simulator that hosts the as-is software's virtual core in Fig. 2. It is to be *modified for the new replacement API*. | [T] Fig. 2 |
| **DVAST kernel module** | The custom, simulation-specific Linux kernel module. It has (1) a minimal virtual hardware model and (2) a custom scheduling mechanism. It replaces KVM (the "vCPU Accelerator" in Fig. 2), exposes the TSC with simulation time, and evicts the virtual core precisely. | [T] (1), (3) |
| **User space timer solution** | The first proof of concept from the First Phase II. A high-precision user-space timer evicts the virtual core from the physical core. It needs no kernel module or administrative permissions, but it gives no access to the TSC. | [T] (1) |
| **Minimal virtual hardware model** | The smallest set of simulated hardware needed to run unmodified software. It is one of the two components of the DVAST kernel module. | [T] (1) |
| **Ideal model (of the software)** | A model that "fully reflects the manner in which software behaves when executes on a fielded tactical system". It is the reference that the as-is execution must reproduce. | [T] App. 1, Fig. 7 |
| **BCM (Base Coupled Model)** | The coupled model of *Other Models* plus the *Ideal Model of Software*. It runs on the BCM time base, which is simulation time. | [T] App. 1, Fig. 7 |
| **VCM (Virtualized Coupled Model)** | The coupled model of *Other Models* plus the *As is Software Model*. The as-is part runs on the host (KVM) time base, which is real time. | [T] App. 1, Fig. 7 |
| **Homomorphism (BCM → VCM)** | The formal mapping used to state the requirement that event timing and I/O exchange in the VCM match those of the BCM. From it the team derived conditions on the time-base mapping and on event occurrence. | [T] App. 1 |
| **DEVS-like system** | A system whose input and output segments are discrete event streams. It responds to an external event by changing state immediately, then follows an input-free trajectory until the next event. This view lets any DEVS-like system be translated into DEVS elements. | [T] App. 1 |
| **Tactical interface solution** | Proposal subtask 1.3 (revised 1.1, "TCP and UDP interface solution for BOA"). DEVS implementations of the TCP/IP protocols used by BOA and MDST, plus translation between simulation data and tactical message formats. | [T] (3) |
| **Interposed simulated protocol stack** | ORNL's earlier fix for the TCP retransmit-timer problem. It accepts data from the real-time stack immediately and handles every interaction with the non-real-time stack in simulated time. | [T] (3) Subtask 1.3 |
| **BOA/DVAST** | BOA virtualized with DVAST. The Task 1 end product, verified in MDST scenarios. | [T] (3) |
| **BOA/TESLA** | BOA virtualized with the QEMU-based TESLA. Validated in 2019. Task 1 replicates that validation with DVAST. | [T] (3) |
| **High performance DVAST** | Proposal Task 2 (revised T4, "HPC support DVAST"). A DVAST that can (a) simulate computers more capable than the physical host and (b) simulate smaller or similar computers at or faster than real time on average. | [T] (3) Task 2 |
| **Overprovisioning** | Operational systems are sized for workloads up to 100% of the expected load, so up to 50% of cores sit idle under the anticipated load. This is the basis for the claimed 2x speedup. | [T] (3) Subtask 2.1 |

## C. DEVS formalism terms

| Term | Meaning | Tag / where |
|------|---------|-------------|
| **Atomic model** | A basic DEVS component that specifies the dynamics of a system component. In ADEVS it is "essentially an event driven state machine". Device data sheets are translated into atomic models. | [T] Background, App. 1 |
| **Coupled model** | A composition of component models (atomic or coupled) plus the couplings between them. | [T] Background |
| **Closure under coupling** | A coupled DEVS model behaves like an atomic DEVS model. This is what makes hierarchical construction and the BCM/VCM argument valid. | [T] Background, App. 1 |
| **Abstract simulator** | The technology-independent algorithm that executes a DEVS model. It guarantees the same behavior on desktop and HPC platforms. | [T] Background |
| **Time advance (ta)** | Fig. 1 legend: "ta – time advance". In DVAST it is also the time bound the coordinator sends to the as-is simulator. [B] In DEVS, how long a model stays in its current state before an internal event. | [T] Fig. 1 / [B] |
| **tn** | Time of next event. | [T] Fig. 1 legend |
| **Elapsed time (e)** | The component simulators must calculate and report it correctly. [B] In DEVS, the time since the last event. | [T] (1) Background / [B] |
| **in / out** | Input and output event values. | [T] Fig. 1 legend |
| **DoDeltaInt / DoDeltaExt** | Invoke the internal transition function (δint) or the external transition function (δext). | [T] Fig. 1 legend |
| **translateByCoupling** | The coordinator operation that maps one component's output to another component's input using the coupled model's couplings. | [T] Fig. 1 |
| **GetTnAndOut / SendTaAndIn** | Protocol calls in Fig. 1. The first asks Simulator 1 for (ta, out). The second sends (ta, in) to Simulator 2 and returns a Result, which is "Done" when the time bound is reached. | [T] Fig. 1 |
| **Parallel DEVS (standard algorithm)** | Parallelizes the computation of component outputs and of state transitions. Subtask 2.2.1, citing ref. [16]. | [T] (3) Subtask 2.2 |
| **Conservative parallel DEVS** | A parallel algorithm that uses lookahead, giving "nearly linear speedup" under ideal conditions. Subtask 2.2.2, citing refs. [16] and [17]. | [T] (3) Subtask 2.2 |
| **Lookahead** | The speedup of the conservative algorithm depends on identifying and exploiting it, and it is used to assign atomic models to logical processes. [B] How far ahead a logical process can promise it will produce no output. | [T] (3) Subtask 2.2 / [B] |
| **Logical process** | The atomic models are assigned to "the logical processes of the parallel algorithm" by graph algorithms. [B] A unit of parallel execution in a parallel discrete event simulation. | [T] (3) Subtask 2.2 / [B] |
| **Infinite time advance** | How the processor-core model represents a halted core. The core model waits passively until it receives an interrupt. | [T] (3) Subtask 2.1 |
| **Experimental Frame (EF)** | A Fig. 7 callout: the BCM is "Valid for real system in desired EF". [B] In DEVS theory, the specification of the conditions under which a model is observed and experimented with. | [T] Fig. 7 / [B] |
| **Federate** | A participant in a federated simulation. Non-real-time execution must stay consistent with the other federates. | [T] (1), App. 1 |

## D. Virtualization and x86 hardware terms

| Term | Meaning | Tag / where |
|------|---------|-------------|
| **Hypervisor / VMM** | "Also known as virtual machine monitors", hypervisors create and run guest VMs by sharing host resources. | [T] (1) |
| **Native hypervisor** | A hypervisor that runs directly on the hardware (Fig. 4). | [T] App. 1 |
| **Hosted hypervisor** | A hypervisor that runs as a process on the host OS (Fig. 5). | [T] App. 1 |
| **Guest / host** | The guest is the virtualized OS and software. The host is the physical machine and its OS. | [T] |
| **VM** | Virtual machine. | [T] (1) |
| **vCPU / virtual core** | The virtual processor that executes guest instructions. It is "evicted" from the physical core to bound execution. | [T] (1), (3) |
| **TSC / TSCR** | Time Stamp Counter (Register), "the preferred time source for modern operating systems". Under KVM it is keyed to wall-clock time. DVAST virtualizes it to report *simulation* time. | [T] (1), App. 2 |
| **VMX** | The Intel virtualization instruction set. The second proof-of-concept module uses "the Intel time out instruction from the Intel VMX instruction set". [B] This probably means the VMX-preemption timer. That is an interpretation, not stated in the proposal. | [T] (1) / [B] |
| **HLT** | The halt instruction. A core issues it when it has nothing left to execute and is waiting for an interrupt. Linux uses it to put the core in a low-power state. | [T] (3) Subtask 2.1 |
| **Extended page tables (EPT)** | Map the virtualized memory addresses used by the guest to physical memory addresses on the host, simulating guest RAM. The proposal says "extended page tables" and never uses the acronym "EPT". | [T] (3) Subtask 1.1 |
| **MMIO** | Memory-mapped I/O. Fig. 2 key: "Memory Mapped Input/Output". | [T] Fig. 2, (3) |
| **Port I/O** | I/O through processor I/O ports. Together with MMIO, it connects the virtual core to simulated peripherals. | [T] (3), App. 1 |
| **Simulated hardware interrupts** | Interrupts delivered to the virtual core so that as-is software can work with NICs, disks and other peripherals. Subtask 1.1.1. | [T] (3) |
| **Traps/IO / Execute** | The two arcs of the Fig. 2 cycle. *Traps/IO* runs from the As Is Operational Software to KVM (vCPU Accelerator). *Execute* runs from KVM back to the software. | [T] Fig. 2 |
| **Bus** | In the computer-system model, routes events and acknowledgements between the processor and devices (Fig. 6). | [T] App. 1 |
| **PIT** | Programmable interval timer, "a counter that generates an output signal when it reaches a programmed count". It may trigger an interrupt. | [T] App. 1 |
| **LAPIC** | Local Advanced Programmable Interrupt Controller. | [T] App. 1 |
| **IOAPIC** | Input/Output Advanced Programmable Interrupt Controller. | [T] App. 1 |
| **AHCI** | The proposal writes "Advanced Host Control Interface disk controller". [B] The standard name is Advanced Host *Controller* Interface. | [T] App. 1 |
| **PCI** | The proposal writes both "Peripheral Component Interface devices" and "Peripheral Component Interconnect (PIC)". [B] The standard expansion is Peripheral Component Interconnect, and "PIC" normally means Programmable Interrupt Controller. See `source-notes.md`. | [T] App. 1 |
| **Message signaled interrupts** | Interrupts produced by a device connected to PCI and routed by the bus. | [T] App. 1 |
| **BogoMIPS** | "Number of iterations of a loop per second". Estimated with the simulated TSC to show consistent, tunable timing precision (Subtask 1.2.3). | [T] (3) |
| **QEMU** | Quick EMUlator. The base of today's TESLA. | [T] (2), ref. [12] |
| **DEADLINE scheduler** | A Linux real-time scheduling policy investigated in Phase I to limit vCPU run time. It may not tell the scheduled thread that its budget is exhausted. | [T] App. 2 |
| **SIGXCPU** | A Linux signal considered as a way to notify the DEVS controller that the vCPU budget was exceeded ("it may not work"). | [T] App. 2 |
| **TCP transmit retry timer problem** | In QEMU-based TESLA, the gap between the non-real-time protocol stack (in virtual BOA) and the real-time stack (in MDST and the Linux kernel) limited non-real-time runs. | [T] (3) Subtask 1.3 |
| **Upstreaming** | Getting changes accepted into the mainline Linux kernel. Phase I judged that KVM changes for simulation "would likely be difficult to upstream". | [T] (1) |

## E. Programs, organizations and tools

| Term | Meaning | Tag / where |
|------|---------|-------------|
| **MDA20-T002** | The STTR topic: *Non-Real-Time Hardware Assisted Computer System Simulation*. | [T] (1) |
| **B2S-0089** | The proposal number, from the page header. | [T] header |
| **STTR** | Small Business Technology Transfer. The project is an MDA STTR. [B] STTR requires a research-institution partner, here ORNL. | [T] (1) / [B] |
| **RTSync Corp** | The prime. A 2004 spin-off of ACIMS (University of Arizona and Arizona State University). | [T] (10) |
| **ORNL** | Oak Ridge National Laboratory, the Research Institution partner. It developed TESLA and performs more than 30% of the second Phase II work. | [T] (6), (8) |
| **ACIMS** | Arizona Center for Integrative Modeling and Simulation. | [T] (6), (10) |
| **MDST** | The MDA simulation that provides the engagement scenarios in which BOA/DVAST is verified. It exchanges data with BOA over TCP/IP messaging. The proposal never expands the acronym. [S] QSR #2 and #3 expand it as **Missile Defense Space Tool**, "MDA's high-fidelity modeling and simulation environment". | [T] (2), (3) / [S] |
| **ADEVS** | ORNL's DEVS simulation package (https://web.ornl.gov/~nutarojj/adevs/). It implements the computer-system model and underlies the "ADEVS As-Is CPU Simulator". The proposal calls it an "object oriented general purpose programming language". [B] ADEVS is a C++ DEVS library. | [T] App. 1 / [B] |
| **CAMLI** | RTSync's MBSE and Digital Engineering environment from MDA18-012. DVAST is to be integrated with CAMLI and the integration demonstrated. The proposal never expands the acronym. [S] The status-report decks' roadmap slide expands it as **Collaborative Development Architecture for Model Level Integration**. | [T] (4), (10) / [S] |
| **DSFA** | DEVS Streaming Framework for Adaptive Architectures (MDA18-012 related work). | [T] (4) |
| **ParaDEVS** | Paratemporal Simulation with Uncertainty Quantification (MDA STTR MDA22-T001). A related RTSync program. | [T] (6) |
| **ATC-Gen** | Automated Test Case Generator. DEVS-based Link 16 (MIL-STD-6016C) conformance testing for JITC. | [T] (4) |
| **JITC** | Joint Interoperability Test Command. | [T] (4) |
| **BMDS** | Ballistic Missile Defense System. | [T] (10) |
| **OPIR** | Overhead Persistent Infrared (in "BMDS OPIR Architecture"). | [T] (10) |
| **DES** | MDA's M&S Directorate, "responsible for developing the current TESLA program". | [T] (10) |
| **SN / THAAD / GMD / AB** | MDA element program offices named as potential users: Sensors Directorate, Terminal High Altitude Area Defense, Ground-based Midcourse Defense, and Aegis BMD. | [T] (10) |
| **TRL** | Technology Readiness Level. The proposal's targets are TRL 8 (BOA/DVAST, proposal Task 1) and TRL 4 (high performance DVAST, proposal Task 2). The revised plan restates no per-task TRL. | [T] (2) |
| **CA** | Contract award. Deliverable schedules are given in months after CA. | [T] Table 2 |
| **GFP / CAP** | Government Furnished Property and Contractor Acquired Property, listed in the final inventory report. | [T] Table 2 |
| **SoS** | System of Systems. | [T] (1) |
| **SISO / IEEE / SCS** | Standards and professional bodies named in (6) and (10). | [T] |
| **SDR** | Software Defined Radio (ORNL related work). | [T] (4) |
| **ORNL/TM-2021/1994** | The ORNL Technical Report cited for the claimed 10x cut in long-term maintenance cost. It is not in the reference list. | [T] (2) |

## F. Terms from the status reports

| Term | Meaning | Tag / where |
|------|---------|-------------|
| **QSR** | Quarterly Status Report, CDRL A004. Seven are due, at +3 to +21 months. | [S] |
| **CDRL** | [B] Contract Data Requirements List, the list that defines each contract deliverable. | [B] |
| **Productization** | The reports' name for revised **T2 Verification and Documentation**: end-user API documentation and a robust regression test suite. | [S] |
| **Demonstration target** | The environment in which BOA/DVAST is to be demonstrated (revised 1.2 and 1.4). Originally MDST, pending a POC at Northrop Grumman. From June 2026 the team is looking for a new one. | [S] |
| **SWIC** | A candidate environment for demonstrating DVAST. The team hopes to support "the transition of the SWIC to a simulation-based capability". **Never expanded in the reports**, so do not guess. | [S] QSR #3 deck |
| **lwIP (LWIP)** | LightWeight IP. [B] A small open-source TCP/IP stack. [S] The basis of DVAST's simulated TCP/IP stack (revised 1.1), which runs on simulation time and no longer depends on Linux kernel real-time retransmit timers. | [S] / [B] |
| **NetworkProxy** | The DVAST device model that bridges a simulated network interface (lwIP, simulation time) to a host interface (real time). Its proxy classes **InternalListener** (outgoing connections from the simulator) and **ExternalListener** (incoming connections from the host) create one proxy per TCP connection. Address and port mappings are fixed when the device is created. | [S] QSR #2, #3 |
| **PCI passthrough** | Giving the simulated computer direct access to a host PCI device, so that BOA can use a host GPU (revised 1.3). | [S] |
| **VFIO / PCI_VFIO** | VFIO is the Linux service that exposes physical PCI devices to a virtual machine. The decks expand it as "Virtual Function Input/Output". **PCI_VFIO** is the DVAST device model built on the VFIO ioctl interface. It supports type 0x00 PCI devices: configuration space, MMIO to the BARs, DMA, and MSI and INTx interrupts. | [S] / [B] |
| **IOMMU** | [B] Input-output memory management unit. It maps device DMA addresses and protects them, which passthrough relies on. | [B] |
| **INTx / IRQ** | [B] INTx: legacy pin-based PCI interrupts. IRQ: interrupt request. Revised 3.3 is "MSI and IRQ interrupts". | [B] |
| **Exit conditions** | Revised 3.2, "Complete set of exit conditions". [B] VM exits are events that return control from the guest to the hypervisor or kernel module (I/O access, HLT, interrupts). HLT is exposed to DVAST "via a KVM exit condition". | [S] / [B] |
| **NUMA** | Non-Uniform Memory Access: how long memory access takes depends on the address. DVAST mirrors the host's NUMA topology in the guest (vCPU pinning, memory ranges, distance table) so that guest I/O latency matches the host's. | [S] |
| **SRAT / SLIT / PPTT** | [B] ACPI tables for resource affinity, locality distances and processor topology (including caches). [S] DVAST reads SRAT from the host (via `numactl -H` and `lsmem`) and fills these tables in for the guest. | [S] / [B] |
| **Small-to-large mapping** | Choosing which subset of host NUMA hardware hosts a guest smaller than the host. The default fills the first host node and spills into the next. The best policy is still open. | [S] QSR #2, #3 |
| **Idle Processor Optimization** | The completed HLT feature. The guest idle loop's HLT reaches DVAST as a KVM exit, and the simulation clock then advances faster than real time. | [S] |
| **idle=halt** | Linux boot flag. [S] Newer guest kernels need it so that they use HLT when idle. The team expects it to become unnecessary once the DVAST kernel module is done. | [S] / [B] |
| **UWAIT** | As written in the decks: the instruction that "the newest Linux kernels use in place of HLT", with no KVM exit for it in stock KVM. [B] This is probably Intel's **UMWAIT** (WAITPKG). That is an interpretation, not stated. | [S] / [B] |
| **Headless BOA** | A BOA configuration without a windowing system. GUIs keep cores busy and remove HLT idle time, so faster than real time depends on it. It was an open question to the BOA side. | [S] QSR #2 deck |
| **adevs engine modernization** | Rewriting the late-1990s adevs C++ DEVS engine, which was optimized for memory, for speed on modern hardware, and refactoring DVAST onto it (revised 4.1 and 4.2). | [S] |
| **DEVS model specification** | AI-assisted extraction of DEVS atomic specifications (7-tuple, state diagrams) and coupled specifications (couplings, flow charts, sequence diagrams) from DVAST's hardware-model sources, for example Core, PIT, APIC, Processor and Computer. | [S] QSR #3 |
| **EFSUT** | Experimental Frame / System Under Test methodology, the basis of the regression test framework. | [S] QSR #3 |
| **Inverse DEVS model** | The dual of a device model. It produces an output where the device expects an input and waits where the device produces an output, so it can act as the device's experimental frame. The team generates them with an LLM from a reference model and inverse-tester pair, then verifies them in adevs. | [S] QSR #2, #3 |
| **Replay regression testing** | Recording known-good input and output trajectories per device, then replaying the inputs after changes and comparing the outputs. | [S] QSR #3 |
| **Doxygen** | [B] A documentation generator. [S] It produces DVAST's HTML API documentation from source comments. | [S] / [B] |
