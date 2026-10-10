# Instructions for AI Coding Agents

## Mission
Build the Qualcomm AI Incident Report Assistant as a native Kotlin/Compose Android app with supplied open-source on-device models and modular, type-safe interfaces.

## Non-negotiable
1. No cloud inference, remote model APIs, analytics or runtime network dependency.
2. Final inference runs on the Qualcomm board with Snapdragon acceleration.
3. Use only verified vendor docs/sample apps for Qualcomm integration; never fabricate runtime APIs.
4. Unstated values remain null. Do not invent people, severity, witnesses, assets or actions.
5. Preserve module boundaries and shared Kotlin contracts.
6. STT/audio is one workstream; LLM + TTS share a laptop/workstream.
7. Fakes are allowed in tests, not as silent demo substitutes.
8. Do not alter board switches, power settings, camera connectors or firmware mode for normal app development.

## Workflow
- Read `TECH_STACK.md`, `ARCHITECTURE.md`, `MODULES.md`, `API_CONTRACTS.md` and `DATA.md`.
- Inspect actual vendor docs and samples before implementing adapters.
- If an API is undocumented, stop and report the missing evidence.
- Validate all LLM JSON before applying it.
- Preserve patch field presence: omitted does not mean clear.
- Add tests for parsing, merging, follow-ups, corrections and failures.
- Keep Python evaluation separate from Android runtime.
- Build/test after each small milestone.
