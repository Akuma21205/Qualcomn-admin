# Module Breakdown and Parallel Development

## Workstreams

| Workstream | Laptop | Owns | Deliverables |
|---|---|---|---|
| STT + audio | A | `AudioRecord`, STT adapter, audio format tests | `SttEngine`, transcript fixtures, setup and latency notes |
| LLM + TTS | B (same laptop) | LLM extraction and offline speech output | `IncidentExtractor`, `SpeechOutput`, extraction/TTS tests |
| App + domain/workflow | C or integration laptop | Kotlin schema, merge logic, follow-ups, Compose UI | Buildable app using fake adapters |
| Evaluation | Any laptop | Python dataset, WER, field metrics | Repeatable JSON/CSV results |
| Board integration | Designated integration laptop + Qualcomm board | Runtime integration, acceleration proof, offline demo | End-to-end verified build |

These are workstreams, not assumptions about which named person owns them.

## Repository layout

```text
qualcomm-incident-assistant/
├── AGENTS.md
├── docs/
├── app/
├── core/
│   ├── domain/
│   ├── contracts/
│   └── workflow/
├── feature/
│   ├── audio/
│   └── report/
├── adapter/
│   ├── stt/
│   ├── llm/
│   └── tts/
├── evaluation/
│   ├── dataset/
│   ├── tests/
│   └── scripts/
└── vendor-samples/   # authorized samples/docs only
```

## How contributors work independently
1. Freeze `IncidentReport`, presence-aware `ReportPatch`, and adapter interfaces before parallel coding.
2. Each contributor checks out the same repo and works on a feature branch.
3. Use fake adapters and local fixtures when another module or the board is unavailable.
4. Include tests and a short setup note with each adapter.
5. Do not commit model binaries unless licensing and repo policy allow it.
6. Merge via PR; integrate real STT, then LLM, then TTS on the board.

## Important patch detail
A nullable data class alone cannot distinguish “field omitted” from “explicitly clear field.” Patch types must preserve field presence. Never replace the entire report with an incomplete LLM response.
