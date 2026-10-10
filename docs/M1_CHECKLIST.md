# Milestone 1 (M1) Checklist & Verification

**Project:** Qualcomm AI Incident Report Assistant  
**Team:** LoRA Farm (Megathon X at IIIT Hyderabad)  
**Status:** Completed & Frozen

---

## 📋 M1 Scope & Deliverables Checklist

### 1. Domain Models (`:core:domain`)
- [x] **`IncidentReport`**: 10 fields matching abstract and specification ([IncidentReport.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/main/kotlin/com/qualcomm/incident/core/domain/IncidentReport.kt)).
  - `incidentType: String? = null`
  - `severityLevel: String? = null`
  - `incidentLocation: String? = null`
  - `dateTime: String? = null`
  - `personsInvolved: List<String>? = null`
  - `equipmentAssetInvolved: String? = null`
  - `incidentDescription: String? = null`
  - `immediateActionsTaken: String? = null`
  - `witnessInformation: List<String>? = null`
  - `recommendedFollowUpAction: String? = null`
  - `fieldProvenance: Map<String, Provenance>`
- [x] **`ReportPatch` & `OptionalField`**: Presence-aware patch container ([ReportPatch.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/main/kotlin/com/qualcomm/incident/core/domain/ReportPatch.kt)).
  - Distinguishes `OptionalField.NotPresent` (omitted -> do not erase) from `OptionalField.Present(value)` (update or explicit clear).
- [x] **`Provenance`**: Tracks evidence confidence ([Provenance.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/main/kotlin/com/qualcomm/incident/core/domain/Provenance.kt)).
  - `STATED`, `NORMALIZED`, `INFERRED`, `UNKNOWN`.
- [x] **Standard Taxonomies**:
  - `IncidentCategory` ([IncidentCategory.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/main/kotlin/com/qualcomm/incident/core/domain/IncidentCategory.kt)): Electrical, Chemical, Mechanical, Fire/Explosion, Medical, Structural, Near Miss, Other.
  - `SeverityLevel` ([SeverityLevel.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/main/kotlin/com/qualcomm/incident/core/domain/SeverityLevel.kt)): Low, Medium, High, Critical.
- [x] **Serialization**: Full `kotlinx.serialization` compatibility verified with round-trip unit tests.

### 2. Core API Contracts (`:core:contracts`)
- [x] **`AudioInput`**: 16kHz mono PCM buffer with duration calculation ([AudioInput.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/AudioInput.kt)).
- [x] **`TranscriptResult`**: Text transcript, latency, finality ([TranscriptResult.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/TranscriptResult.kt)).
- [x] **`ValidatedReportPatch`**: Strongly typed patch, raw JSON, latency ([ValidatedReportPatch.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/ValidatedReportPatch.kt)).
- [x] **`SttEngine`**: Suspended ASR interface ([SttEngine.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/SttEngine.kt)).
- [x] **`IncidentExtractor`**: Suspended LLM extraction interface ([IncidentExtractor.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/IncidentExtractor.kt)).
- [x] **`SpeechOutput`**: Offline TTS playback interface ([SpeechOutput.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/SpeechOutput.kt)).
- [x] **`DecisionEngine`**: High-level triage & categorization interface ([DecisionEngine.kt](file:///c:/Users/shash/Qualcomn-admin/core/contracts/src/main/kotlin/com/qualcomm/incident/core/contracts/DecisionEngine.kt)).

### 3. Core Deterministic Workflow (`:core:workflow`)
- [x] **`ReportPatchMerger`**: Pure functional patch merge engine ([ReportPatchMerger.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/main/kotlin/com/qualcomm/incident/core/workflow/ReportPatchMerger.kt)).
  - Guarantees: Omitted fields are preserved; target fields updated; explicit null clears; provenance updated.
- [x] **`RequiredFieldsPolicy`**: Deterministic mandatory field evaluator ([RequiredFieldsPolicy.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/main/kotlin/com/qualcomm/incident/core/workflow/RequiredFieldsPolicy.kt)).
  - Checks: `incidentType`, `severityLevel`, `incidentLocation`, `dateTime`, `incidentDescription`.
  - Computes `isComplete(report)` and `nextPrompt(report)`.
- [x] **`IncidentWorkflowStateMachine`**: Reactive state machine ([IncidentWorkflowStateMachine.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/main/kotlin/com/qualcomm/incident/core/workflow/IncidentWorkflowStateMachine.kt)).
  - States: `Idle`, `Listening`, `Transcribing`, `Extracting`, `AskingFollowUp`, `Reviewing`, `Finalized`, `Error`.

### 4. Test Fakes (Unblocks M2 Parallel Workstreams)
- [x] **`FakeSttEngine`**: Deterministic speech recognition double ([FakeSttEngine.kt](file:///c:/Users/shash/Qualcomn-admin/adapter/stt/src/main/kotlin/com/qualcomm/incident/adapter/stt/FakeSttEngine.kt)).
- [x] **`FakeIncidentExtractor`**: Deterministic patch extraction double ([FakeIncidentExtractor.kt](file:///c:/Users/shash/Qualcomn-admin/adapter/llm/src/main/kotlin/com/qualcomm/incident/adapter/llm/FakeIncidentExtractor.kt)).
- [x] **`FakeSpeechOutput`**: In-memory speech recording double ([FakeSpeechOutput.kt](file:///c:/Users/shash/Qualcomn-admin/adapter/tts/src/main/kotlin/com/qualcomm/incident/adapter/tts/FakeSpeechOutput.kt)).
- [x] **`FakeDecisionEngine`**: Deterministic triage double ([FakeDecisionEngine.kt](file:///c:/Users/shash/Qualcomn-admin/adapter/decision/src/main/kotlin/com/qualcomm/incident/adapter/decision/FakeDecisionEngine.kt)).

### 5. Unit Test Suites
- [x] **`IncidentReportTest`**: Verifies null defaults, serialization round-trip, and taxonomy parsing ([IncidentReportTest.kt](file:///c:/Users/shash/Qualcomn-admin/core/domain/src/test/kotlin/com/qualcomm/incident/core/domain/IncidentReportTest.kt)).
- [x] **`ReportPatchMergerTest`**: Verifies preservation of unmentioned fields, targeted overwrites, explicit clears, and multi-turn accumulation ([ReportPatchMergerTest.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/test/kotlin/com/qualcomm/incident/core/workflow/ReportPatchMergerTest.kt)).
- [x] **`RequiredFieldsPolicyTest`**: Verifies detection of missing mandatory fields and prompt selection ([RequiredFieldsPolicyTest.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/test/kotlin/com/qualcomm/incident/core/workflow/RequiredFieldsPolicyTest.kt)).
- [x] **`IncidentWorkflowStateMachineTest`**: Verifies multi-turn dialog flow, follow-up state, review transition, and error handling ([IncidentWorkflowStateMachineTest.kt](file:///c:/Users/shash/Qualcomn-admin/core/workflow/src/test/kotlin/com/qualcomm/incident/core/workflow/IncidentWorkflowStateMachineTest.kt)).

---

## 🚀 Exit Verification
All contracts and workflow foundations are frozen and committed.
Team members on Laptop A (STT), Laptop B (LLM + TTS), and Laptop C (Compose UI & Workflow) can now develop their respective modules in parallel against `:core:contracts` without merge conflicts.
