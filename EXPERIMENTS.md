# Experiment Log

Use this file to record measured results. Do not fill in results before running experiments.

## Environment snapshot

- Date:
- Device / build:
- Android version:
- Runtime / SDK version:
- STT model/version:
- LLM model/version:
- TTS engine/voice:
- App commit:
- Prompt/schema version:
- Offline mode verified:
- Snapdragon acceleration verified:

## Experiment template

### EXP-001 — Baseline STT sample

- **Question:** Can the vendor-provided sample transcribe a known clip on the target board?
- **Setup:** Record sample app, model, audio format, and device/runtime versions.
- **Input:** Clip ID and audio properties.
- **Expected:** Reference transcript.
- **Observed:** Actual transcript.
- **Latency:** 
- **Acceleration evidence:**
- **Result:** Not run
- **Notes / next action:**

### EXP-002 — Fixed-text LLM extraction

- **Question:** Can the installed on-device LLM map a fixed transcript into valid schema JSON?
- **Setup:** Record prompt, decoding settings, runtime, and model.
- **Input transcript:**
- **Expected fields:**
- **Actual output:**
- **Schema-valid:** 
- **Unsupported values invented:**
- **Latency:**
- **Result:** Not run
- **Notes / next action:**

### EXP-003 — End-to-end narration

- **Question:** Does mic → STT → extraction → form work on the board?
- **Setup:**
- **Input narration:**
- **Transcript:**
- **Field results:**
- **Latency by stage:**
- **Result:** Not run
- **Notes / next action:**

### EXP-004 — Missing-field follow-up

- **Question:** Does the app ask for a missing mandatory field and merge the response?
- **Initial narration:**
- **Missing field:**
- **Follow-up question:**
- **User answer:**
- **Final report:**
- **Result:** Not run
- **Notes / next action:**

### EXP-005 — Correction and addition

- **Question:** Can a user correct a value and add new information without restarting?
- **Initial report:**
- **Correction/addition:**
- **Expected state update:**
- **Actual state update:**
- **Unrelated fields preserved:**
- **Result:** Not run
- **Notes / next action:**

### EXP-006 — Airplane-mode run

- **Question:** Does the full core workflow operate without network connectivity?
- **Procedure:** Enable airplane mode after required assets are installed, then run the full flow.
- **STT passes:**
- **LLM passes:**
- **TTS passes:**
- **Unexpected network dependency:**
- **Result:** Not run
- **Evidence:**
