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
| Decision / Triage (Experimental) | Jev by TypeSafe AI (System One) — Laptop/Eval only | Cloud REST API for structured classification/routing benchmark; NOT used in mandatory on-device path |
| Tests | JUnit + Kotlin test tooling | Domain, patch merge and workflow tests |
| Evaluation | Python, `pytest`, `jiwer`, pandas, TypeSafe AI SDK | WER, field accuracy, decision accuracy, latency analysis |
| Version control | Git + GitHub | Branches and code review |

## Runtime rule
The Qualcomm board runs the verified QIDK on-device stack (Snapdragon 8 Elite / SM8750-AC). Use only runtime/SDK APIs verified through official Qualcomm AI Hub and QIDK samples. Cloud APIs (including Jev) are forbidden in the mandatory on-device demo path.

## Where code runs
- **Qualcomm board (QIDK):** Android app, STT (Whisper via AI Hub), LLM (Llama-3.2 via GenieX), deterministic workflow/state machine, UI, and offline TTS.
- **Laptops:** development, local fixtures, unit tests, Python evaluation harness, and experimental cloud comparisons (e.g. Jev benchmark).
- **Forbidden in final path:** cloud model APIs (including Jev in production path), remote inference endpoints, or a laptop-hosted inference server.

## Deliberate exclusions
No React/FastAPI, no LangChain/LangGraph for this small workflow, no separate database until persistence is required, no cloud APIs in the offline APK, and no new model selection until the supplied QIDK / AI Hub models are tested.
