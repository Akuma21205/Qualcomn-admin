# Questions for Qualcomm Mentors

Resolve these early because runtime integration is the main schedule risk.

## Models and runtime

1. Which STT, LLM, and TTS models are preinstalled?
2. What are their exact model names, versions, sizes, quantization formats, and licenses?
3. Which runtime/SDK should the team use? Please provide its exact version and Android integration documentation.
4. Is there a supported sample app that runs each model on the HDK8750?
5. Where are the model assets located, and what are the supported ways to access them?
6. How can we verify that inference is using Snapdragon acceleration rather than falling back to CPU?
7. Are there known latency, memory, context-length, or audio-format constraints?

## Android and audio

8. Which Android version/build is installed on the board?
9. Which microphone input on this board is known to work with Android `AudioRecord`?
10. What sample rate, channel count, and PCM format should we use?
11. Is offline Android TTS available, or is a bundled local TTS engine recommended?
12. Is there a known-good Android Studio/Gradle/JDK configuration for the sample app?

## Requirements and judging

13. Which incident fields are mandatory?
14. Is there a prescribed schema or submission API, or may the team define the report schema?
15. Are pre-recorded audio clips allowed during the demo, in addition to live speech?
16. How will on-device execution / Snapdragon acceleration be verified by judges?
17. Is airplane-mode demonstration recommended or required?
18. Are there constraints on storing reports locally or on the expected submit action?

## Team

19. Who is “Jev” in the project context, and are they a teammate? If yes, what is their preferred ownership area?
20. Are there other team members and existing responsibilities?
