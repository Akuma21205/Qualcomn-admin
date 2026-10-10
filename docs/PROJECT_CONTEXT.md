# Project Context

## Goal
Build a native Android, voice-driven incident-report assistant for Megathon X using the open-source model(s) supplied for the Qualcomm HDK8750 (SM8750P). All AI inference must run on-device with Snapdragon acceleration; cloud APIs are prohibited.

## Pipeline
Microphone → STT → LLM extraction → deterministic validation and required-field check → follow-up loop → review/correction → finalize → screen and offline TTS.

## Confirmed in the handoff
- Target Hardware: Qualcomm Innovators Development Kit (QIDK) with Snapdragon 8 Elite for Galaxy (`SM8750-AC`).
- Model Sources: Qualcomm AI Hub models (`Whisper` for STT, `Llama-3.2` via GenieX for LLM).
- Development via Antigravity/AI IDE, with Android Studio/Gradle and ADB.
- Contributors need independent laptop workstreams; LLM + TTS share one laptop/workstream.
- Jev: TypeSafe AI's "System One" decision model (`POST api.typesafe.ai/v1/systemone`). Evaluated for laptop benchmarking; incompatible with on-device offline runtime.

## Unknown — do not guess
- Exact build version of Android on the QIDK unit and working microphone route.
- Specific QIDK sample app for GenieX/Whisper integration.
- Organizer-defined mandatory fields and report submission behavior.
- Whether a local offline TTS voice engine is installed on the QIDK Android build.

## Success criteria
On-board end-to-end flow, evidence of Snapdragon acceleration, no cloud dependency, accurate structured fields, follow-ups for missing required fields, corrections without losing other fields, and successful airplane-mode demo.
