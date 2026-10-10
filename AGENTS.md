# Instructions for AI Coding Agents

Please review the complete guidelines in [`docs/AGENTS.md`](docs/AGENTS.md).

## Core Rules
1. All final AI inference runs **100% on-device** on the Qualcomm QIDK (Snapdragon 8 Elite / SM8750-AC).
2. Cloud APIs are strictly prohibited in the mandatory runtime path.
3. Unstated incident fields must remain `null` — never hallucinate or invent data.
4. Preserve modular boundaries and shared Kotlin contracts defined in `:core:contracts`.
5. Fakes are allowed for local unit tests, but must never be used as silent demo substitutes.
