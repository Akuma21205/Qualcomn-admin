# Module Breakdown and Parallel Development

## Workstreams

| Workstream | Laptop | Owns | Deliverables |
|---|---|---|---|
| STT + audio | A | `AudioRecord`, STT adapter (Whisper), audio format tests | `SttEngine`, transcript fixtures, setup and latency notes |
| LLM + TTS | B (same laptop) | LLM extraction (Llama-3.2) and offline speech output | `IncidentExtractor`, `SpeechOutput`, extraction/TTS tests |
| App + domain/workflow | C or integration laptop | Kotlin schema, merge logic, state machine, local decision rules, Compose UI | Buildable app using fake adapters |
| Evaluation & Benchmarking | Any laptop | Python dataset, WER, field metrics, and Jev System One benchmark | Repeatable JSON/CSV results |
| Board integration | Designated integration laptop + Qualcomm QIDK board | Runtime integration, acceleration proof, offline demo | End-to-end verified build on Snapdragon 8 Elite |

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
│   ├── tts/
│   └── decision/      # local rules on-device; optional Jev cloud adapter for eval
├── evaluation/
│   ├── dataset/
│   ├── tests/
│   └── scripts/       # evaluate_wer.py, evaluate_extraction.py, jev_benchmark.py
└── vendor-samples/    # authorized samples/docs only
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
