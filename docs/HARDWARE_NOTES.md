# Hardware Notes

## Target device

- Platform: Qualcomm® Innovators Development Kit (QIDK).
- SoC: Snapdragon 8 Elite for Galaxy (`SM8750-AC` / `SM8750`).
- Target Compute: Hexagon NPU for high-efficiency on-device neural acceleration.
- OS: Android running on the display; development workflow via ADB over USB-C.
- Interfaces: Onboard microphones, speaker header, USB-C, Micro USB, HDMI output, SD socket, and M.2 connector.

## Audio-relevant observations

- The quick-start manual identifies MIC1/3 at the bottom of the main board and MIC4/5 at the top.
- A 3.5 mm headset jack is also present.
- The usable microphone route and capture configuration have **not** been verified.
- Validate audio capture early. Do not assume a microphone works merely because it is physically present.

## Setup and safety

- Use the supplied 12V DC adapter and leave board power configuration at its known working/default setup.
- Do not change DIP switches as part of normal app development.
- Do not use Emergency Download / forced USB boot mode unless a Qualcomm mentor explicitly instructs it for firmware recovery.
- The rear camera connector is not fool-proofed and is unnecessary for this project; leave it alone.
- ADB is sufficient for the initial Android app workflow.

## Confirmed software & hardware inventory (M0 Discovery Verified)

Recorded on board connection (`b9622c87`):

| Item | Status / Details |
|---|---|
| Platform / Board | Qualcomm Pineapple HDK (`ro.board.platform=pineapple`) |
| SoC Model | Qualcomm `SM8650` (Snapdragon 8 Gen 3, `ro.soc.model=SM8650`) |
| Android version / SDK level | Android 14 (API 34, userdebug build `qti/pineapple/pineapple:14/UKQ1.230917.001`) |
| Installed LLM & parameter count | `Llama-3.2-3B-Instruct` (3 binary context slices: `llama_v3_2_3b_instruct_part_[1..3]_of_3.bin`) |
| Alternate LLM | `qwen2.5-3b-instruct-q4_k_m.gguf` (in `/data/local/tmp`) |
| Installed STT option | Whisper-Base / Tiny models ready for deployment; `/system/bin/tinycap` ready for 16kHz audio capture |
| Installed TTS option | Android `TextToSpeech` (offline voice verified on API 34) |
| Runtime / SDK and version | Qualcomm Genie SDK (`libGenie.so` v1.11.0) + QNN HTP V75 runtime (`libQnnHtpV75.so`) |
| Sample app path | `com.quicinc.chatapp` (Qualcomm Innovation Center Chat App, pulled to `vendor-samples/`) |
| Model asset locations | `/data/local/tmp/genie_bundle/` |
| Snapdragon acceleration confirmed | **Yes!** Verified via `./genie-t2t-run -c genie_config.json` running on Hexagon NPU via `QnnHtp` backend |
| Microphone input configuration | ALSA card 0 `pineapple-qrd-snd-card`, Quad Mic mix ports via `sku_pineapple_qssi` audio policy |
| Memory & Storage | 12 GB LPDDR5X RAM (11.6 GB visible), ~7.6 GB available on `/data` |

## Confidential material

The hardware quick-start manual was marked confidential/no public disclosure in the handoff. Do not publish its photos or redistribute the manual. Keep any sensitive hardware documentation in the team's authorized workspace.
