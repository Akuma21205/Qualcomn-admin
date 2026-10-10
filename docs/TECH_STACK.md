# Technology Stack

| Layer | Decision | Notes |
|---|---|---|
| App language | Kotlin on Android/JVM | Type-safe app and domain logic |
| UI | Jetpack Compose | Native report form, review and correction |
| Build | Gradle + Android Gradle Plugin | Pin versions to verified Qualcomm sample |
| IDE | Antigravity + Android Studio | Coding plus SDK/build/device debugging |
| Device access | ADB | Install, launch and inspect logs |
| Audio capture | Android `AudioRecord` | 16kHz 16-bit mono PCM stream; mic route confirmed on QIDK |
| STT (Primary Candidate) | `Whisper-Small` (Qualcomm AI Hub) | ~241M params; edge-optimized (SHA + Conv); QAIRT/NPU accelerated. Fallback: `Whisper-Base` (~74M params) |
| LLM (Primary Candidate) | `Qwen3.5-2B` (Qualcomm AI Hub / GenieX) | ~2B params; W4A16 quantized on Hexagon NPU; structured JSON patch extraction. Fallback: `Llama-3.2-1B/3B` |
| TTS (Primary Candidate) | Android `TextToSpeech` (Offline Voice) | Built-in Android system service; subject to offline voice verification on QIDK. Fallback: `PiperTTS-EN` |
| Serialization | `kotlinx.serialization` | Strongly typed boundary for JSON patches and schemas |
| Domain/workflow | Kotlin data classes, sealed types, deterministic state machine | Null preservation, patch merge, mandatory field checks, and follow-up loops |
| Decision / Triage (Experimental) | Jev by TypeSafe AI (System One) — Laptop/Eval only | Cloud REST API for structured classification/routing benchmark; NOT used in mandatory on-device path |
| Tests | JUnit + Kotlin test tooling | Domain, patch merge, and state machine tests |
| Evaluation | Python, `pytest`, `jiwer`, pandas, TypeSafe AI SDK | WER, field accuracy, decision accuracy, and latency benchmarks |
| Version control | Git + GitHub | Branches and code review |

## Model Candidate Evaluation & Resource Specifications

| Role | Candidate Model | Source / Runtime | Parameters | On-Device Memory | Target Device Runtime | Fallback Candidate |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **STT** | `Whisper-Small` | Qualcomm AI Hub (Apache 2.0) | 102M Enc / 139M Dec (~241M total) | ~924 MB storage; ~1.1 GB RAM | QAIRT / QNN on Hexagon NPU | `Whisper-Base` (~74M params, ~290 MB) |
| **LLM** | `Qwen3.5-2B` | Qualcomm AI Hub (Apache 2.0) | ~2.0 Billion | ~1.4 GB (W4A16 quantized) | GenieX / QAIRT on Hexagon NPU | `Llama-3.2-1B-Instruct` or `3B` |
| **TTS** | Android `TextToSpeech` | Android System (Offline) | N/A (System voice pack) | Minimal (<50 MB) | Android OS Audio Track | `PiperTTS-EN` (ONNX on CPU/NPU) |
| **Decision** | Local Rules / On-Device LLM | Pure Kotlin / `Qwen3.5-2B` | N/A | Included in App / LLM memory | Android JVM / Hexagon NPU | Heuristic rule table |
| **Decision (Eval)** | Jev by TypeSafe AI | Cloud REST API (`api.typesafe.ai`) | Proprietary System One | 0 MB on board (Cloud only) | Laptop Python harness only | N/A (Cloud prohibited on board) |

## Runtime rule
The Qualcomm board runs the verified QIDK on-device stack (Snapdragon 8 Elite / SM8750-AC). Use only runtime/SDK APIs verified through official Qualcomm AI Hub and QIDK samples (QAIRT, QNN, GenieX). Cloud APIs (including Jev in production) are strictly forbidden in the mandatory on-device demo path.

## Where code runs
- **Qualcomm board (QIDK):** Android app, STT (`Whisper-Small` via AI Hub), LLM (`Qwen3.5-2B` via GenieX), deterministic workflow/state machine, UI, and offline TTS.
- **Laptops:** development, local fixtures, unit tests, Python evaluation harness, and experimental cloud comparisons (e.g. Jev benchmark).
- **Forbidden in final path:** cloud model APIs (including Jev in production path), remote inference endpoints, or a laptop-hosted inference server.

## Deliberate exclusions
No React/FastAPI, no LangChain/LangGraph for this small workflow, no separate database until persistence is required, no cloud APIs in the offline APK, and no unverified model weights outside Qualcomm AI Hub.
