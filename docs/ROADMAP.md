# Roadmap
 
## M0 — Device discovery [COMPLETED]
- Device `b9622c87` connected & authorized via ADB.
- Hardware verified: Qualcomm `SM8650` (Pineapple HDK), Android 14 (API 34), 12GB RAM.
- Runtime & models confirmed: Qualcomm Genie SDK v1.11.0 + QNN HTP V75 NPU acceleration verified live with on-device `Llama-3.2-3B-Instruct` in `/data/local/tmp/genie_bundle`.
- Vendor sample APK pulled to `vendor-samples/com.quicinc.chatapp.apk`.
- Inventory documented in `docs/HARDWARE_NOTES.md`. **Exit criteria met.**

## M1 — Freeze contracts [COMPLETED]
Agree on report schema, mandatory fields, presence-aware patch semantics, `SttEngine`, `IncidentExtractor`, `SpeechOutput`, and `DecisionEngine`. Verified with unit test suites and test fakes across `:core:domain`, `:core:contracts`, and `:core:workflow`. See [`docs/M1_CHECKLIST.md`](M1_CHECKLIST.md). **Exit:** shared contracts frozen and committed.

## M2 — Parallel module development
- **Laptop A [COMPLETED]**: STT/audio adapter (`WhisperSttAdapter`, fallback `Whisper-Base`), `AudioPreprocessor` (PCM16, VAD, 80-channel log-Mel spectrogram), `WavAudioUtil` (RIFF/WAVE encoder/decoder), `AudioCaptureManager` (Flow-based recording & VAD utterance capture), `AudioFixtures`, and full unit test suite. See [`docs/LAPTOP_A_STT.md`](LAPTOP_A_STT.md).
- **Laptop B [IN PROGRESS]**: LLM extraction (`Qwen3.5-2B` via GenieX, fallback `Llama-3.2`) + offline TTS verification.
- **Laptop C/integration owner**: domain, workflow state machine, local decision rules, Compose UI with fake adapters.
- Python evaluation: local test suite and optional Jev benchmark harness.
**Exit:** each workstream has tests, setup notes and stable interfaces.

## M3 — Board vertical slice
Build/install Android shell; integrate real STT (`Whisper-Small`), then LLM (`Qwen3.5-2B`), then TTS on QIDK; verify Hexagon NPU acceleration in Logcat (`QnnHtp`, `Genie Backend: HTP`). Benchmark combined RAM and thermal profile. **Exit:** real board runs microphone → STT → LLM → validated report.

## M4 — Interaction completion
Implement missing-field follow-ups, multi-turn additions, corrections, incident categorization, and visual fallback when TTS fails. **Exit:** updates preserve unrelated fields; offline decision flow operates without network.

## M5 — Evaluation & Benchmarking
Build ~40 labeled cases; measure WER (`Whisper-Small` vs `Base`), field extraction accuracy (`Qwen3.5-2B` vs `Llama-3.2`), null correctness and latency; run `jev_benchmark.py` on laptop to compare cloud System One decision quality against on-device LLM/rules. **Exit:** reproducible quantitative results.

## M6 — Offline demo
Airplane-mode end-to-end run on the Qualcomm QIDK board; verify Jev/cloud calls are disconnected and zero network packets are sent; prepare demo and recovery path. **Exit:** core workflow works 100% offline on Snapdragon 8 Elite.

## Priority under time pressure
1. Vendor sample/runtime (Qualcomm AI Hub + QIDK).
2. Shared contracts (Completed in M1).
3. Real STT (`Whisper-Small` / `Base`) + LLM (`Qwen3.5-2B` / `Llama-3.2`) on QIDK board.
4. Follow-up/correction logic and local triage.
5. Offline proof and metrics.
6. UI polish and optional Jev benchmark.
