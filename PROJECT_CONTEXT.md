# Project Context

## Goal

Build an **AI Incident Report Assistant** for the Qualcomm problem statement at Megathon X. The product accepts conversational speech and produces a structured incident report using on-device generative AI and function/tool-calling-style orchestration.

## Problem statement pipeline

Microphone → Speech-to-Text (STT) → LLM information extraction → mandatory-field check → tool/function call or deterministic action → populate form → user review and submit → screen output and Text-to-Speech (TTS).

If required fields are missing, the app should ask a focused follow-up question and merge the answer into the existing report.

## Example

Input narration:

> There was a water leak near Conference Room 2 around 10 AM. Maintenance has already isolated the area.

Expected extraction includes:
- Incident type: water leak
- Location: Conference Room 2
- Time: 10 AM
- Immediate action: area isolated

Other fields remain unknown unless explicitly supplied or safely normalized from context.

## Evaluation priorities

1. All models execute on-device using Snapdragon acceleration; no cloud APIs.
2. Understand natural conversational speech rather than only predefined commands.
3. Accurate speech recognition and transcription.
4. Accurate extraction of incident information.
5. Correct mapping to report fields.

## Known facts

- Target hardware: Qualcomm HDK8750 (SM8750P) development kit.
- The device appears to run Android; the observed display showed Android with an app open.
- Qualcomm provides the device with an open-source model already installed, according to the user.
- The user is new to Android Studio and ADB; explain Android-specific steps explicitly.
- Planned development environment: Antigravity or another AI IDE, with Android Studio installed for SDK/Gradle support.

## Unknowns — do not silently assume

- Which STT, LLM, and TTS models are installed.
- Runtime and supported APIs (QAIRT/QNN/Genie are possibilities, not confirmed facts).
- Model locations and whether sample apps are preloaded.
- Android version.
- Which microphone input works reliably.
- Which fields the organizers consider mandatory.
- Whether pre-recorded audio is allowed during the demo.
- Whether “Jev” is a teammate and, if so, their role.
- Laptop operating system.

## Scope discipline

The first objective is a reliable offline core workflow. Avoid extra features until the microphone → STT → extraction → form loop works on the actual board.
