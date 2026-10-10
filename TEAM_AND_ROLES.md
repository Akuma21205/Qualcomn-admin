# Team and Roles

## Current status

The handoff mentions “Jev,” but their identity and role have **not been confirmed**. The proposed split below is only a starting point; do not assume it is agreed.

## Proposed ownership

| Workstream | Proposed owner | Responsibility |
|---|---|---|
| On-device runtime integration | Project lead / user | Qualcomm sample integration, STT and LLM runtime, acceleration verification |
| UI and interaction | Teammate, if confirmed | Compose form, review/edit flow, status/error display |
| TTS and evaluation support | Teammate, if confirmed | Offline voice output, test narrations, demo script, evaluation data |
| Schema and workflow | Shared, with one final decision-maker | Field types, mandatory fields, merge/correction semantics |

## First coordination decision

Freeze the schema and mandatory-field list before parallel implementation. That allows UI, inference, and evaluation work to proceed independently.

## Working agreement

- Every workstream should have a small, testable milestone.
- Integrate on the real board after each hardware/runtime milestone.
- Use official vendor docs and samples for Qualcomm APIs.
- Record unknowns and blockers explicitly rather than inventing answers.
- Keep the demo path focused on the required end-to-end workflow.
