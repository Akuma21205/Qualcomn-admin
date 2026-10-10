# On-Device Inference

## Constraint
Use Qualcomm AI Hub models (Snapdragon 8 Elite / SM8750-AC target) through verified runtimes on the Qualcomm QIDK board. All production inference runs on-device with Hexagon NPU acceleration; no cloud APIs.

## Target model selection (Qualcomm AI Hub)
- **STT:** `Whisper-Base-En` (or `Whisper-Tiny-En` for ultra-low latency) compiled for TFLite (QNN delegate) or QNN context binary.
- **LLM:** `Llama-3.2-1B-Instruct` (or `3B-Instruct`) compiled for W4A16 execution via the **Genie / GenieX** Android runtime on Hexagon NPU.
- **TTS:** Android `TextToSpeech` (with offline voice) as primary; fallback to packaged local engine (e.g. `PiperTTS-EN`).

## Evaluation of Jev by TypeSafe AI (System One model)

### What is Jev?
Jev is the first "System One" model created by TypeSafe AI (released September 2026). Unlike generative LLMs that generate token-by-token natural language text, Jev is a non-autoregressive decision model designed exclusively for structured decision-making. It takes an arbitrary state (text or JSON up to 32k tokens) and evaluates typed questions in parallel:
- **`Choice`**: Selects exactly one option from an enumerated list (ideal for incident category taxonomy).
- **`Score`**: Rates state against a rubric (ideal for severity levels 1–5).
- **`Noul`**: Computes boolean / probability judgments (ideal for emergency escalation flags).

### Technical profile
- **API Endpoint:** `POST https://api.typesafe.ai/v1/systemone`
- **Authentication:** Bearer token (`Authorization: Bearer <API_KEY>`)
- **Latency:** 70–500 ms
- **Pricing:** $0.042 per million input tokens; output tokens free ("too cheap to meter").
- **SDKs:** Official Python and JavaScript/TypeScript SDKs.

### Compatibility verdict: Incompatible with mandatory on-device path
- **Verified Fact:** Jev is a closed-source, proprietary cloud REST API hosted on TypeSafe AI's infrastructure.
- **Constraint Check:** There are no downloadable weights, ONNX models, or Hexagon NPU binaries available for Jev on Qualcomm QIDK or Android.
- **Conclusion:** Jev **cannot satisfy** our mandatory offline, no-cloud-inference requirement. Making Jev a hard dependency in the Android app would break the offline Airplane-Mode demonstration.

### Permitted role & architecture placement
1. **Benchmark Oracle (Laptop Evaluation):** Jev runs in the `evaluation/` Python harness to establish a high-accuracy baseline for incident classification, severity scoring, and next-action routing against labeled test cases.
2. **Optional Cloud Hybrid Mode:** Accessible only when explicitly enabled in debug/laptop builds, providing shadow scoring alongside on-device inference.
3. **Strict On-Device Fallback (Production Path):**
   - Incident categorization and severity scoring on the QIDK board are handled by **either**:
     a. The on-device `Llama-3.2` model via constrained JSON fields, or
     b. Deterministic Kotlin heuristic rules in `:core:workflow`.
   - The app must function completely when disconnected from the internet.

## Layer responsibilities: STT vs LLM vs Jev vs Workflow

| Component | Nature | Primary Responsibility | Deployment Location |
| :--- | :--- | :--- | :--- |
| **STT (`Whisper`)** | Generative Audio | Transcribe 16kHz audio stream into raw English text | On-Device (Hexagon NPU) |
| **LLM (`Llama-3.2`)** | Generative Text | Extract structured entity patches (names, locations, actions) from raw text | On-Device (Hexagon NPU) |
| **Deterministic Workflow** | Pure Kotlin Code | Validate types, null checks, mandatory fields check, patch merge, follow-up prompt scheduling | On-Device (Android JVM) |
| **Decision Engine (Local)** | Kotlin Rules / LLM | Categorize incident type and assign severity level offline | On-Device (Android JVM / NPU) |
| **Jev (TypeSafe AI)** | System One (Cloud) | Benchmark oracle for classification, severity rubric, and routing evaluation | Laptop Evaluation (Python) |
| **TTS (`Android / Piper`)**| Speech Synthesis | Read back missing field prompts and final report offline | On-Device (Audio track) |

## Workstreams
- **STT/audio — Laptop A:** Validate Whisper on QIDK; implement `SttEngine`; audio preprocessing fixtures and latency notes.
- **LLM + TTS — Laptop B:** Implement `IncidentExtractor` and `SpeechOutput`; test structured patches, null behavior, prompt formatting, and offline speech output.
- **App/workflow — Laptop C/integration owner:** Kotlin schema, merge rules, required-field state machine, local decision rules, and Compose UI; use fake adapters until real adapters are wired.
- **Evaluation:** Python harness comparing on-device extraction against ground truth, plus Jev shadow decision benchmarks.

## TTS guardrail
Use Android `TextToSpeech` only if the selected voice is verified offline. Otherwise package a compatible local engine. Speech failure must not block visual prompts.
