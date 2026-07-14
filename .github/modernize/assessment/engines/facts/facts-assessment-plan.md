> **Important:** Do NOT call `#appmod-run-assessment-action` or `#appmod-run-assessment-report` — follow the instructions below to dispatch the deep-analysis skills as parallel subagents.

# Task
Produce 6 AI deep-analysis markdown documents for this codebase by dispatching each fact skill as an independent parallel subagent.

# Context
- Workspace path: "c:/projects/github hackaton/work/operation-springfield"
- Each skill writes ONE markdown file at "c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/<skill-id>.md"
- Skill IDs (registered as chat skills): architecture-diagram, dependency-map, api-service-contracts, data-architecture, configuration-inventory, business-workflows

# Instructions
Execute the following steps in order.

## Step 1: Dispatch ALL 6 Fact Skills in PARALLEL (Single Message)

**[CRITICAL] Issue ALL 6 `task(...)` calls below in ONE assistant turn — do NOT call them sequentially, do NOT wait for one before issuing the next.** Parallel dispatch is the whole point; serializing them defeats the entire workflow.

```
task(subagent_type: "general-purpose", description: "Run architecture-diagram", prompt: "Invoke the `architecture-diagram` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/architecture-diagram.md — do NOT write to any other location.")
task(subagent_type: "general-purpose", description: "Run dependency-map", prompt: "Invoke the `dependency-map` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/dependency-map.md — do NOT write to any other location.")
task(subagent_type: "general-purpose", description: "Run api-service-contracts", prompt: "Invoke the `api-service-contracts` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/api-service-contracts.md — do NOT write to any other location.")
task(subagent_type: "general-purpose", description: "Run data-architecture", prompt: "Invoke the `data-architecture` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/data-architecture.md — do NOT write to any other location.")
task(subagent_type: "general-purpose", description: "Run configuration-inventory", prompt: "Invoke the `configuration-inventory` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/configuration-inventory.md — do NOT write to any other location.")
task(subagent_type: "general-purpose", description: "Run business-workflows", prompt: "Invoke the `business-workflows` skill against the workspace at c:/projects/github hackaton/work/operation-springfield with workspace-path=c:/projects/github hackaton/work/operation-springfield and assessment_dir=c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts. The skill writes its output to c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/business-workflows.md — do NOT write to any other location.")
```

## Step 2: Wait for All Subagents

After dispatching all 6, wait for every subagent to finish. Do NOT touch any output file yourself — each subagent owns its own file.

## Step 3: Verify Completion

Confirm all 6 files exist:
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/architecture-diagram.md`
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/dependency-map.md`
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/api-service-contracts.md`
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/data-architecture.md`
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/configuration-inventory.md`
- `c:/projects/github hackaton/work/operation-springfield/.github/modernize/assessment/engines/facts/business-workflows.md`

If any file is missing, re-dispatch ONLY the missing skill(s) — again in a single parallel batch. Do NOT finish until all 6 files exist.