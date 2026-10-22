# Source Notes: Inconsistencies and Gaps in the Accepted Proposal

Known defects in `MDA20_T002_2ndPh2_TechVolume_RTSync.docx`. They are recorded so that answers and new documents do not copy them unknowingly, and so that nobody "corrects" them silently. When one of them matters to an answer, **say so explicitly**. The proposal is an accepted, fixed document, so quote it as written and flag the issue.

## A. Facts the document does not contain

| Missing | Consequence |
|---------|-------------|
| Contract number, award (CA) date, dollar value and period of performance of the **second** Phase II | Deliverable dates stay relative (+N months after CA). The gantt in `work-plan.md` uses a notional start. **The status reports now supply the contract number (HQ0860-25-C-E058) and the period of performance (Sep 29, 2025 – Sep 28, 2027).** See `program.md`. No source states the dollar value or the formal CA date. |
| Dates of Phase I | The lineage timeline shows Phase I as "dates not stated". |
| Expansion of **MDST** and **CAMLI** | Not in the proposal. **The status reports expand both**: Missile Defense Space Tool, and Collaborative Development Architecture for Model Level Integration. See the glossary. |
| The Phase I and First Phase II reports, and ORNL/TM-2021/1994 | The 5x-precision and 10x-maintenance claims cannot be traced further within this skill. |

## B. Numbering and structure

1. **Sections run (1)–(10) and then (12) References.** There is no section (11).
2. **Task 1 subtask labels disagree.** The prose uses "Subtask 1.1", "Subtask 2", "Subtask 3" and "Subtask 4", while Table 1 uses T1.1, T1.2, T1.3 and T1.4. Use the Table 1 IDs. The mapping is in `work-plan.md`.
3. **Table 1 skips 1.1.2.** It lists 1.1.1 (interrupts), 1.1.3 (device I/O) and 1.1.4 (extended page tables), while the prose lists the goals in the order interrupts, EPT, MMIO/port I/O.
4. **The Subtask 2.2 goals are numbered "1)" and "4)"**, because of a broken list in Word. Table 1 calls them 2.2.1 and 2.2.2.
5. **"Phase 2" and "Phase II" are mixed**, and the work plan uses past tense: "During second Phase 2, we proposed two distinct thrusts…".
6. **"We elaborated the design for the second alternative"** in the Background refers to alternatives that this document never introduces. The sentence was probably carried over from an earlier report.
7. **Appendix 1 ends with a stray sentence**: "Investigation into design and use cases for employing an accurate timing source with KVM." It is the title of Appendix 2 run into the text.
8. **The Research Institution's Related Work says** TESLA insights "will be leveraged to implement the proposed approach during Phase I". That is stale Phase I text.

## C. Typos and name variants

| As written | Likely intent | Where |
|------------|---------------|-------|
| "QEME-derived TESLA" | QEMU-derived | Subtask 1.3 |
| "our used space DVAST solution" | user space | Subtask 2.1 |
| "virtualized memory addressed" | addresses | Subtask 1.1 |
| "into the MDS and commercial customer organizations" | probably MDA or BMDS | (10c) |
| "HQ086022C7601" as against "HQ0860-22-C-7144" | inconsistent contract-number formatting | (4) |
| "Performing Organization/Client: RTSync (Sub), Oak Ridge National Laboratory (Sub)" for the First Phase II | no prime is named | (4) |

## D. Technical terminology to flag

| As written | Standard usage | Where |
|------------|----------------|-------|
| "a device connected to a Peripheral Component Interconnect (PIC)" | PCI = Peripheral Component Interconnect. **PIC** normally means Programmable Interrupt Controller. | Appendix 1 |
| "Peripheral Component Interface devices (PCI)" | Peripheral Component **Interconnect** | Appendix 1 |
| "Advanced Host Control Interface disk controller (AHCI)" | Advanced Host **Controller** Interface | Appendix 1 |
| "the ADEVS object oriented general purpose programming language" | ADEVS is a C++ DEVS simulation **library**, not a language | Appendix 1 |
| "the Intel time out instruction from the Intel VMX instruction set" | Probably the VMX-preemption timer. This is an **interpretation**, so do not state it as the proposal's claim. | (1) First Phase II results |
| Glossary "VCPU: Virtual/clock CPU" | The body uses "vCPU" for the *virtual* CPU. | Glossary |
| Glossary entries DSP, DMBSE, TCIS | Never used in the body | Glossary |

## E. Content that conflicts with the chosen approach, or looks carried over

1. **Commercialization (b) still sells a "customer designed KVM non-real-time simulation platform"** and "KVM non-real-time simulation platform technologies". That contradicts the Phase I decision to build a custom, non-KVM kernel module. When describing the product, use "DVAST" or "the DVAST kernel module".
2. **Commercialization (c) revenue pathways mention "fintech and defense clients", "hedge funds, banks", "predictive modeling solutions" and "AI-driven market analytics providers".** These are not DVAST use cases, and the text appears to come from a different proposal. Quote it only if asked about the proposal's wording.
3. **The 2x claim has two framings.** Objective benefit 4 says "up to 2x the scale and performance of the physical hardware", while Subtask 2.1 says "at least 2x reduction in simulation time". The two framings are related but not the same claim.

## F. Items that depend on time (today is after the proposal date)

- The .docx metadata gives creation and modification dates of **2025-06-16**.
- IP: "RTSync plans to file a new patent to safeguard DVAST's innovations by June, 2026". Whether it was filed is **unknown to this skill**. Ask the user and do not assume.
- The market figures (Table 3), the forecast (Table 4) and the $2.7T/9.4% CAGR context are dated to the proposal.

## G. Artifacts of the extraction (see `../source/tech-volume.md`)

- **The Table 1 schedule bars** were recovered from Word cell shading. Plain-text extraction loses them.
- **The Figure 2 callouts** ("Replace" and "Modify for new replacement API") are overlaid text boxes. Their targets (KVM, and the ADEVS As-Is CPU Simulator) were resolved from drawing-group coordinates.
- `image3.png` in the .docx is a byte-identical duplicate of Figure 2 and is not bundled.

## H. The status reports (QSR #1–#3)

Defects in the post-award quarterly status reports that `revised-plan.md` draws on. The same rule applies: flag them, and do not correct them silently.

**Numbering and structure**
1. **Two task numberings.** The deck slide "Schedule of major events" uses T1 Demonstrate BOA/DVAST, T2 Verification and Documentation, T3 Complete the DVAST kernel module, T4 HPC support. The report headings and the "Progress to date" slide use Goal/Task 1 Kernel module, 2 BOA/DVAST, 3 HPC, 4 Productization. This skill uses the **schedule IDs**. The crosswalk is in `revised-plan.md` §3.
2. **T2 has no subtask 2.1.** It starts at 2.2, and 2.3 ("Comprehensive API documentation and regression test suite") repeats the regression suite of 2.2.
3. **HLT has no line on the schedule slide**, but the reports track it under Goal 3 as complete.
4. **QSR #3's section 2 covers only "Task 4: Productization".** The other tasks appear in Appendix B, "Summary of Progress to Date".
5. **QSR #3 Appendix C contains the June 9, 2026 deck twice**, at pp. 14–46 and pp. 47–79.
6. **QSR #2 and QSR #3 each call their meeting "the third meeting"** (Feb 5 and Jun 9, 2026). Counting the kickoff (Oct 7, 2025) and Dec 11, 2025, they are the third and fourth.

**Stale or carried-over text**
1. **The QSR #1 (v2) cover and title page are stale:**
   - The dates covered read "23 December 2023 – 15 March 2024".
   - The report number is `FY2024_MDA-STTR20T002-PH2-07`.
   - The title page says "Phase II", not "2nd Phase II".
   - The abstract sells a "redesigned KVM non-real-time simulation platform". See also §E.1. The product is the DVAST kernel module that replaces KVM.
2. **QSR #3's Appendix B and its "Planning/Next Steps" repeat QSR #1 almost verbatim.** Examples:
   - "need POC at Northrop"
   - "Initiate the transition from architectural design to active code development for the core kernel module"
   - "Begin development on the MDST interface"
   - packet-loss debugging

   Those lines contradict the same report's June 9 deck, which says "looking for new demonstration target" and marks TCP/UDP "complete". **Prefer the deck status words** for progress, and say that the text differs.
3. **Status on the slide conflicts with the report text:**
   - QSR #2's slide says multi-threading is "ongoing" and the regression suite is "progressing". Its text says "no progress to date" for both.
   - QSR #3's slide says the hardware models (NUMA, GPU) are "complete". Its text says NVIDIA GPU access testing is still in progress and small-to-large NUMA mapping needs more work.
4. **The QSR #3 figure cross-references are stale.** The text says "see Figure 5" for HLT (it is Fig. 9), "Figure 2" for PCI passthrough (Fig. 6), and "Figures 3/4" for `numactl` and `lsmem` (Figs. 7 and 8).

**Deliverable records**
1. The QSR is **CDRL A004** on the covers and in the file names, but **data item A002** in the deck's deliverables table.
2. Its format is "Contractor: Word" in that table, but ".ppt" in the "Phase II products" table.
3. The kickoff row is labelled "Phase I Kickoff Slides". It was the second Phase II kickoff.

**Cover-form (SF 298) issues**
- Block 16, security classification, reads **"C"** on QSR #3 and **"U"** on QSR #1. This is almost certainly an error. Do not propagate it.
- The distribution-statement date differs between the SF 298 and the title page of QSR #1: [6/07/2022] against [10/02/2020].

**Typos and terminology**

| As written | Likely intent | Where |
|------------|---------------|-------|
| "Develop a TPC and UDP interface solution" | TCP | schedule slide, 1.1 |
| "integrating VAST to the … (BOA)" | DVAST | QSR #2 abstract |
| "VPCU" | VCPU | all three, intro item (2) |
| "NVIDIA General Processing Units (GPUs)" | Graphics Processing Units | QSR #2, #3, PCI passthrough |
| "192.196.0.1" in the NetworkProxy example | Probably 192.168.0.1. It is only an example address. | QSR #2, #3 |
| "collects system outputs in generators" | In EF terms, **transducers** collect outputs and generators produce inputs | QSR #3, QA framework |
| "UWAIT" | Probably Intel UMWAIT (WAITPKG). This is an interpretation. | QSR #2, #3 decks |
| "When the KVM kernel module replace it completed" | Probably "when the DVAST kernel module that replaces KVM is completed" | HLT slide, all decks |
| "Fully function DVAST kernel module" | fully functional | schedule slide, T3 product |
| "Virtual Function Input/Output (VFIO)" | Matches the Linux kernel's own expansion ("Virtual Function I/O"). Not a defect. It is listed so that nobody "corrects" it. | QSR #2 deck |

**KVM framing.** Appendix A of QSR #2 and #3 and the slide "Rework needed to enable KVM support of DEVS" describe *modifying KVM* (TSC redirected to DEVS time, externalized time control, rescheduled interrupts). The revised T3 product is a DVAST kernel module "that will replace KVM", consistent with the proposal's decision. Treat the KVM-rework material as background on the requirements, and keep describing the product as **the DVAST kernel module** (§E.1).
