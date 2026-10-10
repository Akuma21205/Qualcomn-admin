# Technology Stack

| Layer | Decision | Notes |
|---|---|---|
| App language | Kotlin on Android/JVM | Type-safe app and domain logic |
| UI | Jetpack Compose | Native report form, review and correction |
| Build | Gradle + Android Gradle Plugin | Pin versions to verified Qualcomm sample |
| IDE | Antigravity + Android Studio | Coding plus SDK/build/device debugging |
| Device access | ADB | Install, launch and inspect logs |
| Audio capture | Android `AudioRecord` | Confirm mic route and PCM format on board |
| STT | Supplied open-source on-device model | Use verified Qualcomm runtime/sample; model name unknown |
| LLM | Supplied open-source on-device model | Extract evidence-grounded report patches |
| TTS | Android `TextToSpeech` only if voice works offline; otherwise packaged local TTS | No network-backed speech |
| Serialization | `kotlinx.serialization` | Typed JSON boundary |
| Domain/workflow | Kotlin data classes, sealed types, deterministic state machine | Null handling, validation, follow-ups and corrections |
| Tests | JUnit + Kotlin test tooling | Domain, patch merge and workflow tests |
| Evaluation | Python, `pytest`, `jiwer`, pandas | WER, field accuracy and latency analysis |
| Version control | Git + GitHub | Branches and code review |

## Runtime rule
The exact Qualcomm runtime is not confirmed. Use only the runtime/SDK and APIs shown in official docs or supplied samples. Do not assume QAIRT, QNN, Genie, or any other runtime without evidence.

## Where code runs
- **Qualcomm board:** Android app, STT, LLM, deterministic workflow, UI and offline TTS.
- **Laptops:** development, local fixtures, unit tests, Python evaluation and builds.
- **Forbidden in final path:** cloud model APIs, remote inference endpoints, or a laptop-hosted inference server.

## Deliberate exclusions
No React/FastAPI, no LangChain/LangGraph for this small workflow, no separate database until persistence is required, and no new model selection until the supplied model is tested.
