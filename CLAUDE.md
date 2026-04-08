# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

This repository builds Claude Code environment for implementing DEVS (Discrete Event Systems Specification) formalism for AI-driven modeling and simulation.

## Required Tooling for Development

When working in this repository, ALWAYS use claude-flow/ruflo MCP tools and AgentDB for learning, memory, and reasoning. This applies to all task execution within the project -- not needed for the skills' own runtime dependencies.

### AgentDB (pattern storage and retrieval)

- `mcp__claude-flow__agentdb_pattern-search` -- search for previously learned patterns before solving a problem
- `mcp__claude-flow__agentdb_pattern-store` -- persist new reusable patterns after solving a problem
- `mcp__claude-flow__agentdb_feedback` -- record user corrections for future improvement
- `mcp__claude-flow__agentdb_hierarchical-recall` -- context-aware retrieval during complex tasks

### ReasoningBank (trajectory tracking and learning)

- `mcp__claude-flow__hooks_intelligence_trajectory-start` -- begin trajectory for multi-step reasoning
- `mcp__claude-flow__hooks_intelligence_trajectory-step` -- log intermediate reasoning steps
- `mcp__claude-flow__hooks_intelligence_pattern-store` -- persist successful solution patterns
- `mcp__claude-flow__hooks_intelligence_pattern-search` -- retrieve relevant past trajectories
- `mcp__claude-flow__hooks_intelligence_learn` -- train the reasoning bank after task completion

### Swarm Coordination (multi-agent skill execution)

- `mcp__claude-flow__swarm_init` -- initialize agent swarms for complex skill tasks
- `mcp__claude-flow__agent_spawn` -- spawn specialized sub-agents
- `mcp__claude-flow__task_orchestrate` -- coordinate multi-step skill workflows

## Skill Development Rules

- Each skill lives in its own top-level directory
- Skill outputs (generated models, simulation results, validation reports) go in `test/`
- Before solving a problem, search AgentDB for prior patterns; after solving, store the new pattern
- Use ReasoningBank trajectory tracking for multi-step model construction workflows
