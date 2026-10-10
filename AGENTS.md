# Instructions for AI Coding Agents

These instructions apply to Antigravity and any other AI coding agent working in this repository.

## Mission

Implement the Qualcomm AI Incident Report Assistant for Megathon X: a fully on-device voice-to-structured-incident-report application running on the Qualcomm development board.

## Hard constraints

1. **No cloud inference and no cloud APIs.** Do not add remote LLM/STT/TTS calls, cloud SDKs, telemetry, or hidden network dependencies.
2. **All AI inference must run on the device with Snapdragon acceleration**, as required by the problem statement.
3. **Never invent report values.** If narration does not support a value, leave it `null`.
4. Track whether a value was stated, normalized, or inferred. Do not present inferred values as user-stated facts.
5. Do not guess Qualcomm, QAIRT, QNN, Genie, or other vendor APIs. Use only the real documentation and sample code placed in `/docs` or otherwise supplied by the team.
6. Prefer adapting an official/sample application over building runtime integration from scratch.
7. Keep the final app capable of running offline. The laptop is a development tool only.
8. Do not change board switches, power configuration, camera connectors, or firmware mode unless a Qualcomm mentor explicitly directs it.

## Engineering rules

- Before implementing vendor integration, inspect `/docs` and the sample app source.
- If a required API is not documented, stop and report the missing evidence instead of fabricating a method or class.
- Keep model-runtime code behind a small adapter interface.
- Keep mandatory-field logic, schema validation, merging, and date/time normalization deterministic.
- Validate all model-produced JSON before applying it.
- Add tests for parsing, validation, merging, missing-field handling, and corrections.
- Keep changes small and build/test after each milestone.
- Do not add features outside the roadmap until the core path works on the actual device.
- Avoid dependencies that require internet access at runtime.
- Do not commit secrets, device dumps containing sensitive information, or confidential hardware-manual images.

## Definition of done for a feature

- The code builds.
- Relevant unit or integration tests pass.
- Failure paths are handled without crashing or silently corrupting the report.
- The feature works on the real board when it involves hardware/runtime integration.
- Offline behavior is verified for any inference or speech feature.
- Documentation is updated when setup, schema, runtime, or behavior changes.

## Before asking the user questions

Inspect the repository and documentation first. Ask only for information that blocks implementation. Clearly distinguish confirmed facts from assumptions.

## Initial task order

1. Verify ADB connection and device properties.
2. Inspect available models, runtimes, and sample apps with mentor-provided guidance.
3. Run a provided sample end to end.
4. Agree on report schema and mandatory fields.
5. Implement deterministic schema/state logic and tests.
6. Integrate STT, then LLM extraction, then UI/follow-up/TTS.
7. Run the evaluation set and offline demonstration.
