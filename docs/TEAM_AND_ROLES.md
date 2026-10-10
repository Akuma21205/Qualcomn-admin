# Team and Roles

| Workstream | Setup | Responsibility |
|---|---|---|
| STT + audio | Laptop A | Audio capture, STT adapter, format tests and latency |
| LLM + TTS | Laptop B (same laptop) | Extraction adapter, structured patch tests, offline speech |
| App + workflow | Laptop C or integration owner | Kotlin schema, state machine, Compose UI, fake adapters |
| Evaluation | Any laptop | Python dataset, WER, field metrics and regression tests |
| Board integration | Designated laptop + Qualcomm board | Runtime integration, acceleration proof and offline demo |

## Collaboration rules
- Freeze schema/interfaces before parallel coding.
- Use one shared Git repo and feature branches.
- Include tests and setup notes with each adapter.
- Use fakes/fixtures to unblock local work, never as silent demo fallbacks.
- Do not commit model binaries unless licensing/repository policy allows it.
- Integrate one real adapter at a time on the board.

The handoff mentions “Jev” but does not confirm who this is or their role. Assign named ownership only after confirming with the team.
