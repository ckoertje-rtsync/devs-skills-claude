# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

This repository builds Claude Code environment for implementing DEVS (Discrete Event Systems Specification) formalism for AI-driven modeling and simulation.

## Required Tooling for Development

When working in this repository, ALWAYS use AgentDB for learning, memory, and reasoning. This applies to task execution within the project -- not to the skills' own runtime dependencies.

Use the wrapper `./.claude/agentdb.sh`, which pins `AGENTDB_PATH` to `.agentdb/agentdb.db` and delegates to `npx agentdb@latest`. All commands below assume `cwd` is the repo root.

One-time setup (if the DB does not yet exist):

```
./.claude/agentdb.sh init --db .agentdb/agentdb.db
```

### AgentDB (pattern storage and retrieval)

- Search prior patterns before solving a problem:
  `./.claude/agentdb.sh query --query "<problem description>" --k 5 --synthesize-context`
- Persist a new reusable pattern after solving:
  `./.claude/agentdb.sh store-pattern --type experience --domain <domain> --pattern '<json>' --confidence 0.9`
- Record a user correction as a reflexion episode:
  `./.claude/agentdb.sh reflexion store <session-id> <task> <reward> <success> "<critique>"`
- Context-aware hierarchical recall with provenance:
  `./.claude/agentdb.sh recall with-certificate "<query>" 12`

### ReasoningBank (trajectory tracking and learning)

Trajectories are logged as reflexion episodes; each step is a `reflexion store` call tagged with the same `session-id`.

- Log a trajectory step (start or continuation):
  `./.claude/agentdb.sh reflexion store <session-id> <task> <reward> <success> "<critique>"`
- Retrieve relevant past trajectories:
  `./.claude/agentdb.sh reflexion retrieve "<task>" --k 5 --synthesize-context`
- Consolidate successful episodes into reusable skills:
  `./.claude/agentdb.sh skill consolidate`
- Discover causal edges from episode patterns:
  `./.claude/agentdb.sh learner run`
- Train / consolidate patterns after task completion:
  `./.claude/agentdb.sh train --domain <domain> --epochs 10 --batch-size 32`

## Skill Development Rules

- Each skill lives in its own top-level directory
- Skill outputs (generated models, simulation results, validation reports) go in `test/`
- Before solving a problem, search AgentDB for prior patterns; after solving, store the new pattern
- Use ReasoningBank trajectory tracking for multi-step model construction workflows
