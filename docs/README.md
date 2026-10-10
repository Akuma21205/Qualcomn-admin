# Qualcomm AI Incident Report Assistant

Megathon X project: fully on-device voice-to-incident-report Android app for the Qualcomm HDK8750 (SM8750P).

## Constraints
- Use the open-source model(s) supplied for the Qualcomm board, through verified vendor runtime/sample code.
- All inference runs on-device with Snapdragon acceleration; no cloud APIs.
- Unknown report values remain null.
- Use Kotlin/JVM type-safe contracts between modules.
- Team members may develop modules on separate laptops; LLM + TTS share one laptop/workstream.
- Laptop tests do not prove board/NPU integration.

## Documentation
- `PROJECT_CONTEXT.md` — confirmed context and unknowns
- `TECH_STACK.md` — chosen stack
- `ARCHITECTURE.md` — system and module architecture
- `MODULES.md` — parallel workstreams and repo layout
- `API_CONTRACTS.md` — typed interfaces
- `AGENTS.md` — AI coding-agent instructions
- `SETUP.md` — Android/ADB setup
- `HARDWARE_NOTES.md` — board notes
- `DATA.md` — schema and patch rules
- `INFERENCE.md` — STT/LLM/TTS integration
- `EVALUATION.md` — metrics and test cases
- `EXPERIMENTS.md` — experiment log
- `ROADMAP.md` — milestones
- `MENTOR_QUESTIONS.md` — questions to resolve
- `TEAM_AND_ROLES.md` — ownership proposal

## Status
No end-to-end code/runtime has been verified on the board yet. Exact models, runtime, Android build, microphone route, and mandatory fields remain unknown.
