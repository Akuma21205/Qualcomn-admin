# On-Device Inference Plan

## Constraint

The problem statement requires all AI models to execute on-device using Snapdragon acceleration and strictly prohibits cloud APIs. This applies to speech recognition, LLM extraction, and any AI-based speech generation.

## Model/runtime inventory first

Before writing vendor-specific integration, confirm with the mentor or inspect the official sample project:
- STT model name, version, input/output format, supported languages.
- LLM name, size, quantization, tokenizer/chat template, context length, runtime.
- TTS availability and whether it works offline.
- Supported Qualcomm runtime/SDK version and Android integration path.
- Sample code, build steps, licenses, and model asset locations.
- Whether acceleration is actually active (not silently falling back to CPU).

Do not assume QAIRT, QNN, Genie, or any API name until confirmed by actual docs/sample code.

## Recommended implementation order

1. Run a provided STT sample with one known audio clip.
2. Verify the returned transcript and latency.
3. Run a provided LLM sample with fixed text and request JSON extraction.
4. Implement schema validation and report merging without model dependencies.
5. Connect STT output to extraction.
6. Add live microphone capture after file/clip-based inference is known to work.
7. Add follow-up questions, corrections, and offline TTS.
8. Test all stages with airplane mode enabled.

## STT adapter contract

Input:
- Audio samples in the exact format supported by the installed model.
- Explicit sample rate/channel count and duration metadata.

Output:
- Transcript text.
- Status/error.
- Optional timing/confidence metadata if supported.

Handle:
- Empty audio.
- No speech detected.
- Runtime failure or timeout.
- Partial/low-quality transcription.
- User cancellation.

## LLM extraction contract

Input:
- Current schema and report state.
- Latest transcript.
- Rules that require evidence-grounded extraction, nulls for unknown values, and patch-only updates.

Output:
- Machine-readable patch.
- Evidence/provenance for populated fields if supported.
- Explicit unknown fields.

Guardrails:
- Treat model output as untrusted.
- Enforce a bounded retry for malformed JSON.
- Validate keys and value types in code.
- Reject unsupported keys and values.
- Never let the LLM decide whether all mandatory fields are complete; deterministic code does this.
- If parsing still fails, preserve the existing report and ask the user to repeat or clarify.

## TTS

First choice proposed in the handoff: Android `TextToSpeech`, after verifying that the selected voice works without network access. If it depends on a downloaded/network-backed voice, select a truly local option. Piper is an alternative only after packaging and runtime feasibility are verified.

## Latency

Measure each stage separately:
- Audio capture duration
- STT wall-clock latency
- LLM first-token and total generation latency, if available
- Validation/merge time
- TTS start latency

Optimize only after a working baseline exists. Prefer short extraction prompts and bounded output length; do not sacrifice field correctness for a flashy demo.
