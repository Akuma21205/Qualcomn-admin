# On-Device Inference

## Constraint
Use supplied open-source model(s) through the verified runtime. All inference runs on the board with Snapdragon acceleration; no cloud APIs.

## Confirm before integration
Exact model names/versions/licenses, runtime SDK, sample apps, supported audio formats, tokenizer/chat template, context limits, model paths, offline TTS availability, and how to prove acceleration.

Do not assume QAIRT/QNN/Genie or any API name without official documentation.

## Workstreams
- **STT/audio — Laptop A:** validate the vendor STT sample, audio format and microphone; implement `SttEngine`; provide fixtures and latency notes.
- **LLM + TTS — Laptop B:** implement `IncidentExtractor` and `SpeechOutput` in one environment; test structured patches, malformed JSON, null behavior and offline voice output.
- **App/workflow — Laptop C/integration owner:** schema, merge rules, required-field state machine and Compose UI; use fake adapters until the real adapters are ready.

## LLM guardrails
- Return evidence-supported updates only.
- Keep unknown values null.
- Validate JSON and types in deterministic code.
- Bounded retry for malformed output; preserve current state on failure.
- The LLM does not decide whether required fields are complete.

## TTS
Use Android `TextToSpeech` only if the selected voice is verified offline. Otherwise package a compatible local engine. Speech failure must not block visual prompts.

Measure STT, LLM, validation and TTS latency separately; record versions and acceleration evidence.
