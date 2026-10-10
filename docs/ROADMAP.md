# Roadmap
 
## M0 — Device discovery
Connect QIDK board with ADB; record Android version/build; verify Snapdragon 8 Elite SoC (`SM8750-AC`); obtain Qualcomm AI Hub models (`Whisper`, `Llama-3.2`) and runtime samples (`GenieX`, QNN). **Exit:** device recognized and inventory recorded.

## M1 — Freeze contracts [COMPLETED]
Agree on report schema, mandatory fields, presence-aware patch semantics, `SttEngine`, `IncidentExtractor`, `SpeechOutput`, and `DecisionEngine`. Verified with unit test suites and test fakes across `:core:domain`, `:core:contracts`, and `:core:workflow`. See [`docs/M1_CHECKLIST.md`](M1_CHECKLIST.md). **Exit:** shared contracts frozen and committed.

## M2 — Parallel module development
- Laptop A: STT/audio adapter (Whisper) and fixtures.
- Laptop B: LLM extraction (`Llama-3.2`) + offline TTS in one environment.
- Laptop C/integration owner: domain, workflow state machine, local decision rules, Compose UI with fake adapters.
- Python evaluation: local test suite and optional Jev benchmark harness.
**Exit:** each workstream has tests, setup notes and stable interfaces.

## M3 — Board vertical slice
Build/install Android shell; integrate real STT, then LLM, then TTS on QIDK; verify Hexagon NPU acceleration in logs. **Exit:** real board runs microphone → STT → LLM → validated report.

## M4 — Interaction completion
Implement missing-field follow-ups, multi-turn additions, corrections, incident categorization, and visual fallback when TTS fails. **Exit:** updates preserve unrelated fields; offline decision flow operates without network.

## M5 — Evaluation & Benchmarking
Build ~40 labeled cases; measure WER, field accuracy, null correctness and latency; run `jev_benchmark.py` on laptop to compare cloud System One decision quality against on-device LLM/rules. **Exit:** reproducible quantitative results.

## M6 — Offline demo
Airplane-mode end-to-end run on the Qualcomm QIDK board; verify Jev/cloud calls are disconnected and zero network packets are sent; prepare demo and recovery path. **Exit:** core workflow works 100% offline on Snapdragon 8 Elite.

## Priority under time pressure
1. Vendor sample/runtime (Qualcomm AI Hub + QIDK).
2. Shared contracts.
3. Real STT (Whisper) + LLM (Llama-3.2) on QIDK board.
4. Follow-up/correction logic and local triage.
5. Offline proof and metrics.
6. UI polish and optional Jev benchmark.
