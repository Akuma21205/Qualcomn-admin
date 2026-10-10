# Qualcomm AI Incident Report Assistant

Megathon X project: fully on-device voice-to-incident-report Android app for the Qualcomm® Innovators Development Kit (QIDK) targeting Snapdragon 8 Elite for Galaxy (`SM8750-AC`).

## Constraints
- Use open-source models optimized from **Qualcomm AI Hub** (`Whisper` for STT, `Llama-3.2` via GenieX for LLM).
- All production inference runs on-device with Hexagon NPU acceleration; no cloud APIs in mandatory path.
- Unknown report values remain null; unstated fields are never fabricated.
- Jev by TypeSafe AI (System One model) is evaluated as a laptop-side decision benchmark only; it is not a production runtime dependency.
- Use Kotlin/JVM type-safe contracts between modules.
- Team members develop modules in parallel using feature branches and local fixtures.
- Final demonstration must function completely offline in Airplane Mode.

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
