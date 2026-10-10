# Questions for Qualcomm Mentors

## Runtime/models
1. Are pre-exported Qualcomm AI Hub model binaries for **`Qwen3.5-2B`** (W4A16 via GenieX) or **`Whisper-Small`** (SHA edge-optimized) pre-loaded on the QIDK board filesystem (e.g. `/data/local/tmp/`), or should we export them via Qualcomm AI Hub Workbench?
2. Which specific runtime version of **GenieX** or **QAIRT** is installed on the QIDK Android build, and where are the matching JNI `.so` libraries located?
3. How do we verify Hexagon NPU hardware acceleration in Logcat (e.g., verifying `QnnHtp` backend execution and ruling out CPU fallback)?
4. What is the verified audio capture route and sample format for the QIDK onboard microphones (MIC1/3 vs MIC4/5 vs 3.5mm jack)?
5. Does the QIDK Android image provide preloaded offline English voice data for `TextToSpeech` (`LANG_AVAILABLE`), or should we bundle a local neural TTS library like `PiperTTS-EN`?

## Board/problem statement
6. Android build/SDK and Gradle/JDK versions?
7. Which microphone route is known to work?
8. Which report fields are mandatory? Is there a required schema/submission API?
9. Are prerecorded clips allowed?
10. How will offline execution and acceleration be checked?

## Architecture / Hybrid evaluation
11. Can modules be prototyped on separate laptops and integrated on the board?
12. **Resolved:** "Jev" in the initial handoff refers to TypeSafe AI's Jev (System One decision model), evaluated for fast classification/triage. Because Jev is cloud-only, our mandatory path uses on-device models on QIDK. Question: Are teams permitted to showcase a laptop-side hybrid comparison (on-device vs Jev cloud benchmark), or must 100% of all demo materials focus strictly on on-device execution?
