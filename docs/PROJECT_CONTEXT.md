# Project Context

## Goal
Build a native Android, voice-driven incident-report assistant for Megathon X using the open-source model(s) supplied for the Qualcomm HDK8750 (SM8750P). All AI inference must run on-device with Snapdragon acceleration; cloud APIs are prohibited.

## Pipeline
Microphone → STT → LLM extraction → deterministic validation and required-field check → follow-up loop → review/correction → finalize → screen and offline TTS.

## Confirmed in the handoff
- Board: Qualcomm HDK8750 (SM8750P), observed running Android.
- User reports Qualcomm supplies an open-source model already installed.
- Development via Antigravity/AI IDE, with Android Studio/Gradle and ADB.
- Contributors need independent laptop workstreams; LLM + TTS are intended to share one laptop/workstream.

## Unknown — do not guess
- Exact STT/LLM/TTS models, versions, licenses, sizes and formats.
- Runtime/SDK and APIs, model paths and official samples.
- Android version, working microphone path and audio format.
- Organizer-defined mandatory fields and report submission behavior.
- Whether “Jev” names a teammate or was intended to mean a technical component; workstream split here follows the explicit LLM + TTS wording.

## Success criteria
On-board end-to-end flow, evidence of Snapdragon acceleration, no cloud dependency, accurate structured fields, follow-ups for missing required fields, corrections without losing other fields, and successful airplane-mode demo.
