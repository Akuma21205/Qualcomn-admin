# Roadmap and Milestones

Prioritize a working vertical slice over feature count. Integration with the actual board/runtime is the largest known risk.

## M0 — Device connection

**Tasks**
- Install Platform-Tools.
- Enable USB debugging and authorize the laptop.
- Capture model, Android version, and SDK level.

**Exit criteria**
- `adb devices` shows the board as `device`.
- Device properties are recorded.
- No board configuration changes were required.

## M1 — Android hello world

**Tasks**
- Create or adapt a minimal Android app.
- Build and install it using the project's Gradle wrapper.
- Display a simple confirmation screen.

**Exit criteria**
- App installs and launches on the board.
- Build/deploy steps are documented.

## M2 — Run vendor samples

**Tasks**
- Obtain the official sample app and runtime docs.
- Run STT on one known clip.
- Run the installed LLM on a fixed prompt.
- Confirm whether acceleration is active.

**Exit criteria**
- STT and LLM samples run on the board.
- Model/runtime versions and latency are recorded.
- No vendor API has been guessed or fabricated.

## M3 — Schema and deterministic core

**Tasks**
- Agree on exact JSON schema and mandatory fields.
- Implement JSON parsing, type validation, null policy, and merge rules.
- Implement missing-field selection and follow-up generation.
- Add unit tests for corrections and additions.

**Exit criteria**
- Test cases cover valid, invalid, incomplete, and correction patches.
- Unknown fields remain null.
- Existing fields are not accidentally erased.

## M4 — Fixed-text extraction

**Tasks**
- Connect the LLM adapter to a fixed transcript.
- Extract a patch matching the schema.
- Validate output and implement bounded recovery.

**Exit criteria**
- Representative transcripts produce schema-valid state or a safe recoverable error.
- Unsupported fields are not silently populated.

## M5 — End-to-end voice workflow

**Tasks**
- Integrate audio capture and STT.
- Connect transcript to extraction.
- Render the editable review form.
- Support missing-field follow-up and corrections.
- Add offline TTS.

**Exit criteria**
- Mic → STT → LLM → form works on the actual board.
- Corrections and later additions update the existing report.
- TTS works with network disabled.

## M6 — Evaluation and demo hardening

**Tasks**
- Build the ~40-case evaluation set and laptop-side Python harness.
- Measure WER, field-level accuracy, null correctness, and latency.
- Test accents/noise where feasible.
- Run the entire flow in airplane mode.
- Prepare a reliable demo script and fallback plan.

**Exit criteria**
- Results are reproducible and honestly reported.
- Core workflow passes airplane-mode testing.
- Demo does not depend on a laptop-hosted model or cloud API.

## Priority rule

If time becomes tight, preserve this order:
1. On-device compliance.
2. Reliable extraction and schema validation.
3. End-to-end path on the real board.
4. Missing-field questions and corrections.
5. Evaluation evidence.
6. UI polish and optional extras.
