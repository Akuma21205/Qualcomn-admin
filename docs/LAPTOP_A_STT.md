# Laptop A — Speech-to-Text (STT) & Audio Workstream

**Module Scope:** `:adapter:stt` and `:feature:audio`  
**Target Device:** Qualcomm Innovators Development Kit (QIDK) / Snapdragon 8 Gen 3 (`SM8650`)  
**Status:** Completed & Tested (Milestone 2)

---

## 1. Overview & Architecture

Laptop A provides the audio ingestion and Automatic Speech Recognition (ASR) subsystem for the on-device Incident Assistant. It bridges hardware microphone capture, signal conditioning, silence/VAD segmentation, and Whisper neural transcription.

```
+-------------------------------------------------------------+
|                     Audio Pipeline                          |
+-------------------------------------------------------------+
|                                                             |
|   Hardware Microphones (QIDK Quad Mic / 3.5mm Jack)         |
|                              │                              |
|                              ▼                              |
|      AudioCaptureManager (:feature:audio)                   |
|      • 16kHz, 16-bit, Mono PCM stream                       |
|      • Dynamic buffer chunking (100ms windows)              |
|      • VAD speech boundary detection                        |
|                              │                              |
|                              ▼                              |
|      AudioPreprocessor (:adapter:stt)                       |
|      • PCM16 -> Float[-1.0, 1.0] normalization             |
|      • RMS energy calculation & Silence rejection           |
|      • Anti-hallucination silence trimming                  |
|      • 80-channel log-Mel spectrogram feature extractor     |
|                              │                              |
|                              ▼                              |
|      WhisperSttAdapter (:adapter:stt)                       |
|      • Whisper-Small / Whisper-Base configuration           |
|      • Hexagon NPU / QNN delegate inference                 |
|      • Latency benchmarking & zero-hallucination guardrail  |
|                              │                              |
|                              ▼                              |
|                   TranscriptResult (Text)                   |
+-------------------------------------------------------------+
```

---

## 2. Audio Specifications

| Parameter | Value | Rationale |
|---|---|---|
| **Sampling Rate** | `16,000 Hz` (16 kHz) | Native input rate expected by OpenAI Whisper |
| **Bit Depth** | `16-bit` Little-Endian | High dynamic range (~96 dB) with standard Android support |
| **Channels** | `1` (Mono) | Whisper expects single-channel audio |
| **Window Length** | `400 samples` (25 ms) | FFT window for spectral decomposition |
| **Hop Length** | `160 samples` (10 ms) | Frame shift matching Whisper acoustic encoder |
| **Mel Filterbanks** | `80 channels` | Standard Whisper log-Mel filter dimension |
| **Max Utterance** | `30.0 seconds` | Maximum input window for standard Whisper context |

---

## 3. Candidate Model Selection

| Model | Checkpoint | Encoder / Decoder | Storage | Runtime RAM | Trade-off & Decision |
|---|---|---|---|---|---|
| **`Whisper-Small`** *(Primary)* | `openai/whisper-small` | 102M / 139M (241M total) | ~924 MB | ~1.1 GB | **Selected Primary**: Lowest Word Error Rate (WER) on technical terms, numbers, and noisy industrial environments. |
| **`Whisper-Base`** *(Fallback)* | `openai/whisper-base` | 39M / 35M (74M total) | ~290 MB | ~400 MB | **Fallback**: 3x faster decoding; swap in if board thermal/latency exceeds 2s threshold. |
| **`Whisper-Tiny`** | `openai/whisper-tiny` | 9M / 30M (39M total) | ~150 MB | ~250 MB | Extreme resource constraint option. |

---

## 4. Anti-Hallucination Guardrails

A notorious failure mode of autoregressive speech decoders like Whisper is generating repetitive hallucinations (e.g. *"Thank you for watching"*, *"Subtitle by..."*) when fed prolonged silence or low-level background noise.

To prevent this:
1. **Empty Audio Rejection**: Instant return of `TranscriptResult(text = "")` if input byte array is empty.
2. **RMS Energy VAD Threshold**: Input audio is analyzed with `AudioPreprocessor.calculateRms()`. If RMS energy is below `0.012f` (default threshold), the engine emits an empty string without invoking neural model inference.
3. **Silence Trimming**: Leading and trailing silent chunks are stripped via `AudioPreprocessor.trimSilence()`, preserving only active speech plus a 100ms safety pad.

---

## 5. Artifacts & Deliverables

### In `:adapter:stt`:
- [`WhisperSttAdapter.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/WhisperSttAdapter.kt): Concrete implementation of `SttEngine` with NPU inference integration hooks and latency tracking.
- [`AudioPreprocessor.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/AudioPreprocessor.kt): Signal processing, normalization, VAD, silence trimming, and 80-channel log-Mel spectrogram extraction.
- [`WavAudioUtil.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/WavAudioUtil.kt): Full RIFF/WAVE header encoder and decoder for file-based testing.
- [`WhisperModelVariant.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/WhisperModelVariant.kt): Hyperparameters and memory footprints for Whisper model family.
- [`AudioFixtures.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/AudioFixtures.kt): Deterministic test buffers (silence, sub-threshold noise, active tones, speech bursts).
- [`FakeSttEngine.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/FakeSttEngine.kt): Deterministic fake double for local unit testing.

### In `:feature:audio`:
- [`AudioCaptureManager.kt`](file:///d:/iiit%20h/Qualcomn-admin/feature/audio/src/main/kotlin/com/qualcomm/incident/feature/audio/AudioCaptureManager.kt): Production Android `AudioRecord` wrapper supporting continuous chunk streaming via Kotlin `Flow<ByteArray>` and VAD-terminated utterance capture.

### Unit Tests in `:adapter:stt`:
- [`AudioPreprocessorTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/test/kotlin/com/qualcomm/incident/adapter/stt/AudioPreprocessorTest.kt): Validates round-trip PCM16 conversion, RMS energy calculation, VAD trimming, and Mel spectrogram dimensions.
- [`WavAudioUtilTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/test/kotlin/com/qualcomm/incident/adapter/stt/WavAudioUtilTest.kt): Validates 44-byte WAV header integrity, round-trip serialization, and corrupted file rejection.
- [`WhisperSttAdapterTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/test/kotlin/com/qualcomm/incident/adapter/stt/WhisperSttAdapterTest.kt): Verifies anti-hallucination behavior, inference dispatch, and latency tracking.
- [`FakeSttEngineTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/stt/src/test/kotlin/com/qualcomm/incident/adapter/stt/FakeSttEngineTest.kt): Verifies test double behavior.
