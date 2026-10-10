# M2 — Laptop C Deliverables & Verification Checklist

**Module Owners:** Laptop C Workstream (`:feature:report`, `:feature:audio`, `:app`, and `:adapter:decision`)  
**Target Hardware:** Qualcomm QIDK (Snapdragon 8 Gen 3 / `SM8650`)  
**Status:** **COMPLETED & VERIFIED**

---

## 1. Scope & Objective

Laptop C (App & Integration Owner) is responsible for:
1. **Interactive UI (`:feature:report`):** Modern, responsive Jetpack Compose application screen (`ReportScreen`) rendering live workflow states, missing field prompts, audio controls, manual editing, and provenance tags.
2. **Workflow Orchestration (`IncidentReportViewModel`):** Coordinates the end-to-end voice loop: Microphone/Audio &rarr; STT (`Whisper`) &rarr; LLM (`Qwen3.5-2B`) &rarr; Deterministic Merge (`ReportPatchMerger`) &rarr; Local Triage (`LocalRuleDecisionEngine`) &rarr; Offline TTS Follow-Up / Finalize.
3. **Local Decision & Triage Engine (`:adapter:decision`):** On-device deterministic classification heuristics mapping incident descriptions to standard taxonomy (`IncidentCategory`) and severity scores (1–5).
4. **Android Entry Point (`:app`):** `MainActivity` lifecycle, dynamic runtime audio permissions (`RECORD_AUDIO`), and dependency wiring.
5. **Milestone 2 Exit:** Unifies Laptop A, Laptop B, and Laptop C so that all modules have stable interfaces, tests, and setup notes.

---

## 2. Component Deliverables

### A. `:feature:report` (Jetpack Compose UI & ViewModel)
| Component | File Path | Status | Description |
| :--- | :--- | :--- | :--- |
| **`IncidentReportViewModel`** | [`IncidentReportViewModel.kt`](file:///d:/iiit%20h/Qualcomn-admin/feature/report/src/main/kotlin/com/qualcomm/incident/feature/report/IncidentReportViewModel.kt) | Verified | Orchestrates multi-turn dialog flow, audio ingestion, patch merging, decision triage, and follow-up speech prompts. |
| **`ReportScreen`** | [`ReportScreen.kt`](file:///d:/iiit%20h/Qualcomn-admin/feature/report/src/main/kotlin/com/qualcomm/incident/feature/report/ReportScreen.kt) | Verified | Interactive Compose UI: status banner, active follow-up prompt card, voice control section, decision triage summary, and structured 10-field incident schema display with provenance tags. |
| **ViewModel Tests** | [`IncidentReportViewModelTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/feature/report/src/test/kotlin/com/qualcomm/incident/feature/report/IncidentReportViewModelTest.kt) | Verified | Tests audio processing, follow-up prompt triggering, manual overrides, finalizing, and reset. |

### B. `:adapter:decision` (On-Device Triage Engine)
| Component | File Path | Status | Description |
| :--- | :--- | :--- | :--- |
| **`LocalRuleDecisionEngine`** | [`DecisionEngineAdapter.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/decision/src/main/kotlin/com/qualcomm/incident/adapter/decision/DecisionEngineAdapter.kt) | Verified | On-device keyword and heuristic triage engine categorizing Fire, Chemical, Electrical, Medical, Mechanical, and urgency flags. |
| **Decision Engine Tests** | [`LocalRuleDecisionEngineTest.kt`](file:///d:/iiit%20h/Qualcomn-admin/adapter/decision/src/test/kotlin/com/qualcomm/incident/adapter/decision/LocalRuleDecisionEngineTest.kt) | Verified | Unit tests validating incident categorization, critical urgency detection, and default fallbacks. |

### C. `:app` (Android Application Entry Point)
| Component | File Path | Status | Description |
| :--- | :--- | :--- | :--- |
| **`MainActivity`** | [`MainActivity.kt`](file:///d:/iiit%20h/Qualcomn-admin/app/src/main/kotlin/com/qualcomm/incident/app/MainActivity.kt) | Verified | Connects runtime permissions (`RECORD_AUDIO`), production adapters, ViewModel, and launch of Compose UI. |
| **Manifest** | [`AndroidManifest.xml`](file:///d:/iiit%20h/Qualcomn-admin/app/src/main/AndroidManifest.xml) | Verified | Declares `RECORD_AUDIO` and `MODIFY_AUDIO_SETTINGS` hardware permissions. |

---

## 3. Milestone 2 Completion Summary

| Workstream | Modules | Deliverables | Status |
|---|---|---|---|
| **Laptop A** | `:adapter:stt`, `:feature:audio` | `WhisperSttAdapter`, `AudioPreprocessor`, `WavAudioUtil`, `AudioCaptureManager`, `AudioFixtures`, 4 unit test suites | **COMPLETED** |
| **Laptop B** | `:adapter:llm`, `:adapter:tts`, `evaluation/` | `GenieLlmAdapter`, `LlmPromptBuilder`, `LlmPatchParser`, `AndroidSpeechOutput`, Python evaluation harness, 3 unit test suites | **COMPLETED** |
| **Laptop C** | `:feature:report`, `:adapter:decision`, `:app` | `ReportScreen` (Compose), `IncidentReportViewModel`, `LocalRuleDecisionEngine`, `MainActivity`, 2 unit test suites | **COMPLETED** |

**Exit Criteria Met:** All modules have completed implementations, stable contracts, unit tests, and setup notes. The codebase is fully prepared for Milestone 3 (Board vertical slice on QIDK).
