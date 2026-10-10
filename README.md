# Qualcomm AI Incident Report Assistant

On-device, voice-driven incident reporting for **Megathon X at IIIT Hyderabad**.

The app turns natural spoken narration into a structured incident report, asks for missing mandatory information, lets the user review and correct the report, and presents the result on screen and through text-to-speech.

## Non-negotiable constraints

- **All AI inference must run on the Qualcomm device using Snapdragon acceleration.**
- **No cloud APIs, cloud inference, or network dependency.**
- Do not invent missing report fields. Keep unknown values `null`.
- Treat Qualcomm runtime/API details as unknown until verified from the supplied documentation and sample apps.
- The final demo must run independently on the development board.

## Documentation index

| File | Purpose |
|---|---|
| [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) | Problem, scope, known facts, and assumptions |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Components, data flow, and state machine |
| [`AGENTS.md`](AGENTS.md) | Instructions for Antigravity/AI coding agents |
| [`SETUP.md`](SETUP.md) | Laptop, Android SDK, ADB, build, and deployment |
| [`HARDWARE_NOTES.md`](HARDWARE_NOTES.md) | Board setup and hardware facts/unknowns |
| [`DATA.md`](DATA.md) | Report schema, null policy, provenance, and examples |
| [`INFERENCE.md`](INFERENCE.md) | On-device STT, extraction, validation, and TTS plan |
| [`EVALUATION.md`](EVALUATION.md) | Test methodology and metrics |
| [`EXPERIMENTS.md`](EXPERIMENTS.md) | Experiment log template and baseline plan |
| [`ROADMAP.md`](ROADMAP.md) | Milestones, acceptance criteria, and priorities |
| [`MENTOR_QUESTIONS.md`](MENTOR_QUESTIONS.md) | Questions to resolve with Qualcomm mentors |
| [`TEAM_AND_ROLES.md`](TEAM_AND_ROLES.md) | Proposed ownership; unconfirmed assumptions |

## Current status

At handoff, the problem and hardware had been reviewed, but **no project code had been written and nothing had been run on the board**. Runtime, model inventory, Android version, microphone path, and schema requirements still need confirmation.

## Suggested initial workflow

1. Get `adb devices` to recognize the board.
2. Record device model and Android version.
3. Ask mentors about the installed models, runtime, sample apps, and demo constraints.
4. Run a Qualcomm-provided sample end to end before integrating it into a custom app.
5. Agree on and freeze the JSON schema.
6. Build the smallest end-to-end vertical slice, then add missing-field follow-ups and corrections.
7. Demonstrate in airplane mode and run the evaluation set.
