# Roadmap

## M0 — Device discovery
Connect with ADB; record Android version/build; obtain model/runtime/sample docs and acceleration-verification method from Qualcomm. **Exit:** device recognized and inventory recorded.

## M1 — Freeze contracts
Agree on report schema, mandatory fields, presence-aware patch semantics, `SttEngine`, `IncidentExtractor`, and `SpeechOutput`. **Exit:** shared contracts committed before parallel coding.

## M2 — Parallel module development
- Laptop A: STT/audio adapter and fixtures.
- Laptop B: LLM extraction + offline TTS in one environment.
- Laptop C/integration owner: domain, workflow, Compose UI with fake adapters.
- Python evaluation can be developed independently.
**Exit:** each workstream has tests, setup notes and stable interfaces.

## M3 — Board vertical slice
Build/install Android shell; integrate real STT, then LLM, then TTS; verify acceleration. **Exit:** real board runs microphone → STT → LLM → validated report.

## M4 — Interaction completion
Implement missing-field follow-ups, multi-turn additions, corrections and visual fallback when TTS fails. **Exit:** updates preserve unrelated fields.

## M5 — Evaluation
Build ~40 labeled cases; measure WER, field accuracy, null correctness and latency; test error paths. **Exit:** reproducible results.

## M6 — Offline demo
Airplane-mode end-to-end run; confirm no cloud dependency; prepare demo and recovery path. **Exit:** core workflow works offline on the Qualcomm board.

## Priority under time pressure
1. Vendor sample/runtime.
2. Shared contracts.
3. Real STT + LLM on board.
4. Follow-up/correction logic.
5. Offline proof and metrics.
6. UI polish and extras.
