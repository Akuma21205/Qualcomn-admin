# Hardware Notes

## Target device

- Development kit: Qualcomm HDK8750 (SM8750P).
- The handoff identifies the platform as corresponding to Snapdragon 8 Elite; confirm platform/runtime details with Qualcomm.
- The observed setup showed Android running on the display, so the planned delivery is an Android app installed through ADB.
- The board includes microphones, a speaker connection, USB-C, Micro USB, HDMI output, SD socket, and M.2 connector.

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

## Unconfirmed software inventory

Record these after confirmation:

| Item | Status |
|---|---|
| Android version / SDK level | Unknown |
| Installed STT model and version | Unknown |
| Installed LLM and parameter count | Unknown |
| Installed TTS option | Unknown |
| Runtime / SDK and version | Unknown |
| Sample app path and build instructions | Unknown |
| Model asset locations | Unknown |
| Snapdragon acceleration confirmed in runtime logs | Unknown |
| Microphone input selected and tested | Unknown |

## Confidential material

The hardware quick-start manual was marked confidential/no public disclosure in the handoff. Do not publish its photos or redistribute the manual. Keep any sensitive hardware documentation in the team's authorized workspace.
