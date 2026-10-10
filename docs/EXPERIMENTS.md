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

## EXP-002 — STT sample
Input clip / reference / actual transcript / format / latency / acceleration evidence / result. Not run.

## EXP-003 — LLM extraction
Input transcript / expected patch / actual output / schema validity / unsupported values / latency. Not run.

## EXP-004 — Offline TTS
Engine/voice / text / airplane mode / playback / latency. Not run.

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
