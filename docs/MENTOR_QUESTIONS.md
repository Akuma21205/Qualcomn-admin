# Questions for Qualcomm Mentors

## Runtime/models
1. Which open-source STT/LLM models are installed? Exact names, versions, licenses and formats?
2. Which runtime/SDK version and official Android sample should we use?
3. How do we verify Snapdragon acceleration and detect CPU fallback?
4. Supported audio formats, model constraints and model asset locations?
5. Is a local/offline TTS engine supplied or recommended?

## Board/problem statement
6. Android build/SDK and Gradle/JDK versions?
7. Which microphone route is known to work?
8. Which report fields are mandatory? Is there a required schema/submission API?
9. Are prerecorded clips allowed?
10. How will offline execution and acceleration be checked?

## Architecture / Hybrid evaluation
11. Can modules be prototyped on separate laptops and integrated on the board?
12. **Resolved:** "Jev" in the initial handoff refers to TypeSafe AI's Jev (System One decision model), evaluated for fast classification/triage. Because Jev is cloud-only, our mandatory path uses on-device models on QIDK. Question: Are teams permitted to showcase a laptop-side hybrid comparison (on-device vs Jev cloud benchmark), or must 100% of all demo materials focus strictly on on-device execution?
