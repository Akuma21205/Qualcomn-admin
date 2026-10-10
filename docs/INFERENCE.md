# On-Device Inference

## Constraint
Use Qualcomm AI Hub models (Snapdragon 8 Elite / SM8750-AC target) through verified runtimes on the Qualcomm QIDK board. All production inference runs on-device with Hexagon NPU acceleration; no cloud APIs.

---

## 1. Candidate Model Stack Evaluation

### A. LLM: `Qwen3.5-2B` (Primary Candidate)
* **Role:** Unstructured narrative extraction, structured JSON report patch generation, follow-up prompt formulation, and user correction handling.
* **Source & Checkpoint:** `Qwen/Qwen3.5-2B` optimized on **Qualcomm AI Hub** for Hexagon NPU (Apache 2.0).
* **Architecture & Quantization:** ~2.0 Billion parameters, quantized to **W4A16** (4-bit weights, 16-bit activations) for Hexagon Tensor Processor (HTP) execution via **GenieX / QAIRT**.
* **Memory Footprint:** ~1.4 GB RAM for model weights and KV cache.
* **Evaluation & Advantages:**
  - Modern architecture with superior multilingual tokenization and instruction following.
  - Generates strictly formatted JSON patches without formatting drift.
  - Supports `enable_think=False` or standard instruct template in GenieX to suppress verbose `<think>...</think>` reasoning tokens and emit immediate JSON.
* **Alternative & Fallback:**
  - `Llama-3.2-1B-Instruct`: Smaller (~700 MB RAM), faster token generation (~25+ tok/s), but weaker reasoning on ambiguous incident reports.
  - `Llama-3.2-3B-Instruct`: Stronger reasoning, but larger memory (~2.2 GB RAM) and slightly higher latency.
  - *Recommendation:* Begin integration with `Qwen3.5-2B`. If token generation latency on QIDK exceeds targets (>150ms per token), fall back to `Llama-3.2-1B-Instruct`.

---

### B. STT: `Whisper-Small` (Primary Candidate)
* **Role:** Audio-to-text transcription from 16kHz mono microphone streams.
* **Source & Checkpoint:** `openai/whisper-small` optimized on **Qualcomm AI Hub** (Apache 2.0).
* **Architecture:** 102M encoder, 139M decoder (~241M parameters total).
  - Qualcomm AI Hub optimization replaces standard Multi-Head Attention (MHA) with Single-Head Attention (SHA) and linear layers with convolutional (Conv) layers for Hexagon NPU acceleration.
* **Model Size & Memory:**
  - Storage: ~391 MB (encoder) + ~533 MB (decoder) = ~924 MB total.
  - Runtime RAM: ~1.1 GB during inference.
  - Input: 80x3000 log-Mel spectrogram (representing up to 30s of 16kHz audio).
  - Max Decoded Tokens: 200 tokens.
* **Evaluation & Advantages:**
  - Significantly lower Word Error Rate (WER) than `Whisper-Tiny` and `Whisper-Base`, particularly in noisy industrial environments, with heavy accents, or with technical jargon ("breaker panel exploded", "chemical scrubber leak").
* **Alternative & Fallback:**
  - `Whisper-Base` (74M params, ~290 MB storage): Lower latency on autoregressive token decoding (~40ms vs ~120ms), lower RAM (~400 MB).
  - *Recommendation:* Benchmark `Whisper-Small` on QIDK. If decoding latency exceeds 2.0 seconds for typical 10-second incident utterances, swap in `Whisper-Base`.

---

### C. TTS: Android Offline `TextToSpeech` (Primary Candidate)
* **Role:** Voice prompts for missing mandatory fields and verbal report readback.
* **Implementation:** Android native `android.speech.tts.TextToSpeech`.
* **Offline Verification Procedure:**
  1. Verify engine installation: `TextToSpeech.Engine.ACTION_CHECK_TTS_DATA`.
  2. Verify offline voice availability: Call `tts.isLanguageAvailable(Locale.US)` and ensure it returns `LANG_AVAILABLE` with no internet access (Airplane Mode).
  3. Inspect voice attributes: `voice.isNetworkConnectionRequired == false`.
* **Fallback Option:**
  - If the QIDK Android OS image does not include an offline voice pack, embed **`PiperTTS-EN`** (ONNX / Sherpa-ONNX), an open-source, ultra-lightweight (~50 MB) offline neural speech synthesizer.
  - Visual prompts on Jetpack Compose must **always** remain functional regardless of TTS status.

---

### D. Decision Layer: `Jev by TypeSafe AI` (Laptop / Eval Only)
* **Role:** High-level categorization, severity scoring rubric, and routing triage.
* **Compatibility Verdict:** **INCOMPATIBLE WITH ON-DEVICE APP.**
  - Jev is exclusively a cloud-hosted proprietary REST API (`POST https://api.typesafe.ai/v1/systemone`).
  - No on-device weights, ONNX models, or Hexagon NPU binaries exist.
  - Placing Jev in the mandatory path would violate the core offline Airplane Mode constraint.
* **Permitted Role:** Maintained strictly in `evaluation/scripts/jev_benchmark.py` on the laptop to serve as a benchmark oracle comparing on-device decisions against ground truth.
* **On-Device Production Path:** Classification and severity scoring on QIDK are handled locally by either:
  1. `Qwen3.5-2B` outputting constrained category and severity fields, or
  2. Deterministic Kotlin heuristic rules in `:core:workflow`.

---

## 2. Resource & Memory Budget on Snapdragon 8 Elite

| Component | Storage Size | Runtime RAM | Compute Engine | Expected Latency |
| :--- | :--- | :--- | :--- | :--- |
| **`Whisper-Small` (STT)** | ~924 MB | ~1.1 GB | Hexagon NPU (QAIRT) | ~300–800 ms per utterance |
| **`Qwen3.5-2B` (LLM)** | ~1.4 GB | ~1.5 GB | Hexagon NPU (GenieX) | ~20–35 tokens/sec |
| **Android OS + Compose App** | ~80 MB | ~1.2 GB | Kryo CPU + Adreno GPU | UI at 60–120 FPS |
| **Total Stack Footprint** | **~2.4 GB** | **~3.8 GB** | **Snapdragon 8 Elite** | **End-to-end: < 2.5s** |

> **Note:** The Qualcomm QIDK board is equipped with **12 GB to 16 GB LPDDR5X RAM**, meaning the entire model stack consumes < 30% of total physical memory, leaving ample headroom and avoiding Out-Of-Memory (OOM) kills.

---

## 3. Deployment and Integration Guide

### Step 1: Exporting Models from Qualcomm AI Hub
Using the official Qualcomm AI Hub Workbench / Python API:

```bash
# 1. Install Qualcomm AI Hub Python CLI
pip install qai-hub

# 2. Configure credentials
qai-hub configure --api_token <YOUR_QUALCOMM_AI_HUB_TOKEN>

# 3. Export Whisper-Small for Snapdragon 8 Elite
qai-hub compile \
  --model whisper_small \
  --device "Snapdragon 8 Elite" \
  --target_runtime qnn_lib \
  --output_dir ./vendor-samples/whisper_small/

# 4. Export Qwen3.5-2B for GenieX
# Note: Can be exported directly via GenieX CLI or AI Hub Workbench
geniex export \
  --model Qwen/Qwen3.5-2B \
  --backend qairt \
  --chipset qualcomm-snapdragon-8-elite-for-galaxy \
  --quantization w4a16 \
  --output_dir ./vendor-samples/qwen_3_5_2b/
```

### Step 2: Deploying to QIDK Board via ADB
```bash
# Push model context binaries to board's app-accessible storage
adb push ./vendor-samples/whisper_small/ /data/local/tmp/models/whisper/
adb push ./vendor-samples/qwen_3_5_2b/ /data/local/tmp/models/qwen/
```

### Step 3: Application Adapter Wiring
- `:adapter:stt` loads the pre-compiled Whisper QNN binary from app internal storage or asset directory.
- `:adapter:llm` initializes the GenieX Android SDK (`libGenie.so`) pointing to `/data/local/tmp/models/qwen/` (or app files directory).

---

## 4. Layer Responsibilities Summary

| Component | Nature | Primary Responsibility | Deployment Location |
| :--- | :--- | :--- | :--- |
| **STT (`Whisper-Small`)** | Audio Transformer | Transcribe 16kHz audio stream into raw English text | On-Device (Hexagon NPU) |
| **LLM (`Qwen3.5-2B`)** | Generative Text | Extract structured entity patches (names, locations, actions) from raw text | On-Device (Hexagon NPU) |
| **Deterministic Workflow** | Pure Kotlin Code | Validate types, null checks, mandatory fields check, patch merge, follow-up prompt scheduling | On-Device (Android JVM) |
| **Decision Engine (Local)** | Kotlin Rules / Qwen | Categorize incident type and assign severity level offline | On-Device (Android JVM / NPU) |
| **Jev (TypeSafe AI)** | System One (Cloud) | Benchmark oracle for classification, severity rubric, and routing evaluation | Laptop Evaluation (Python) |
| **TTS (`Android / Piper`)**| Speech Synthesis | Read back missing field prompts and final report offline | On-Device (Audio track) |

---

## 5. Offline & Hardware Guardrails
1. **Airplane Mode Test:** All core workflows (audio capture -> STT -> LLM -> follow-up -> review -> TTS) must pass with Wi-Fi and Cellular disabled.
2. **Acceleration Verification in Logcat:** Monitor for Qualcomm NPU runtime tags (`QnnHtp`, `Genie Backend: HTP`, `Perf: Hexagon NPU`). If CPU fallback occurs, flag immediately as a performance defect.
3. **Graceful Degradation:** If `Whisper-Small` causes memory or thermal pressure during long recording sessions, switch to `Whisper-Base` without changing application code.
