# Experiment Log

Do not fill results before running tests.

## Environment
- Date / board build:
- Android version:
- Runtime/SDK:
- STT model/version:
- LLM model/version:
- TTS engine/voice:
- App commit/schema version:
- Acceleration verified:
- Airplane-mode verified:

## EXP-001 — Device baseline
Commands: `adb devices`, `getprop ro.product.model`, `getprop ro.build.version.release`, `getprop ro.build.version.sdk`. Result: Not run.

## EXP-002 — STT: Whisper-Small vs Whisper-Base Profiling on QIDK
- **Input:** 10s and 30s test incident narration audio clips (16kHz PCM; clean, noisy background, accented).
- **Candidates:** `Whisper-Small` (SHA edge-optimized, ~241M params) vs `Whisper-Base` (74M params).
- **Recorded Metrics:** WER (Word Error Rate), Encoder latency (ms), Autoregressive Decoder latency (ms), End-to-end latency (ms), Peak RAM usage (MB), Hexagon NPU offload log confirmation (`QnnHtp`). Result: Not run.

## EXP-003 — LLM: Qwen3.5-2B Structured Patch Extraction on QIDK
- **Input:** Benchmark incident transcripts (clean narrations, missing required fields, follow-up corrections).
- **Candidates:** `Qwen3.5-2B` (W4A16 via GenieX; `enable_think=False`) vs `Llama-3.2-1B-Instruct`.
- **Recorded Metrics:** Schema validity rate (100% target), field extraction accuracy (%), Time-to-first-token (TTFT ms), Token generation rate (tokens/sec), Peak RAM (MB), Hexagon NPU log confirmation (`Genie Backend: HTP`). Result: Not run.

## EXP-004 — Offline TTS Verification on QIDK
- **Engine:** Android native `TextToSpeech` (with preloaded offline voice) vs packaged `PiperTTS-EN` fallback.
- **Condition:** 100% Airplane Mode (Wi-Fi and mobile data disabled).
- **Recorded Metrics:** Initialization latency (ms), playback start latency (ms), audio routing to board speaker/jack, audible clarity. Result: Not run.

## EXP-005 — End-to-end
Narration / transcript / report / follow-up-correction behavior / per-stage latency / airplane-mode result. Not run.

## EXP-006 — Patch merge
Initial state / update / expected changed fields / unrelated fields preserved / result. Not run.

## EXP-007 — Jev System One Decision Benchmark (Laptop / Cloud)
- **Input state:** Labeled incident transcript / partial report.
- **Jev Questions:**
  - `Choice`: Incident category (Electrical, Chemical, Mechanical, Fire, Medical, Other).
  - `Score`: Severity level (1–5 rubric).
  - `Noul`: Is immediate emergency escalation required? (Probability 0.0–1.0).
- **Metrics:** Jev latency, accuracy vs ground-truth labels, token cost ($0.042/1M input tokens).
- **Comparison:** Compare Jev decisions against on-device `Llama-3.2` constrained extraction and Kotlin rules.
- **Airplane-mode fallback check:** Disconnect network; verify Android app switches seamlessly to on-device decision engine without failing or crashing. Result: Not run.
