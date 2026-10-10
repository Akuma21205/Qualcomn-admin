# Qualcomm AI Incident Report Assistant

On-device voice-driven incident reporting assistant for the Qualcomm® Innovators Development Kit (QIDK) targeting Snapdragon 8 Elite for Galaxy (`SM8750-AC`).

## Documentation
All architecture, contracts, tech stack, and roadmap documents are located in [`docs/`](docs/):
- [`docs/README.md`](docs/README.md) — Project Overview
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — System Architecture and Logical Modules
- [`docs/TECH_STACK.md`](docs/TECH_STACK.md) — Technology Stack and Decisions
- [`docs/MODULES.md`](docs/MODULES.md) — Module Breakdown and Workstreams
- [`docs/API_CONTRACTS.md`](docs/API_CONTRACTS.md) — Kotlin API Contracts
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — Development Roadmap and Milestones
- [`docs/INFERENCE.md`](docs/INFERENCE.md) — On-Device Inference and Jev Evaluation
- [`docs/DATA.md`](docs/DATA.md) — Incident Report Schema and Merge Rules
- [`docs/SETUP.md`](docs/SETUP.md) — Android Studio, ADB, and Board Setup

## Repository Structure
- `:app` — Android Application entry point
- `:core:domain` — Schema and provenance models
- `:core:contracts` — Shared type-safe interfaces (`SttEngine`, `IncidentExtractor`, `SpeechOutput`, `DecisionEngine`)
- `:core:workflow` — Deterministic state machine and patch merge engine
- `:feature:audio` — AudioRecord capture
- `:feature:report` — Jetpack Compose UI
- `:adapter:stt` — Qualcomm AI Hub Whisper STT adapter
- `:adapter:llm` — Qualcomm AI Hub GenieX LLM adapter
- `:adapter:tts` — Offline Text-to-Speech adapter
- `:adapter:decision` — Local triage heuristics and optional Jev cloud adapter for evaluation
- `evaluation/` — Python test dataset and evaluation scripts
