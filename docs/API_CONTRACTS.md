# API Contracts

These are application-owned Kotlin interfaces, **not Qualcomm SDK APIs**. Adapt them to the verified toolchain.

```kotlin
data class TranscriptResult(
    val text: String,
    val elapsedMs: Long? = null
)

interface SttEngine {
    suspend fun transcribe(audio: AudioInput): Result<TranscriptResult>
}

interface IncidentExtractor {
    suspend fun extractPatch(
        transcript: String,
        currentReport: IncidentReport
    ): Result<ValidatedReportPatch>
}

interface SpeechOutput {
    suspend fun speak(text: String): Result<Unit>
    fun stop()
}

data class DecisionContext(
    val transcript: String,
    val currentReport: IncidentReport
)

data class DecisionResult(
    val incidentType: String? = null,
    val severityScore: Int? = null, // e.g., 1-5 scale
    val isUrgent: Boolean = false,
    val routingDepartment: String? = null,
    val engineName: String // "LocalRules", "OnDeviceLlama", or "JevSystemOne"
)

interface DecisionEngine {
    suspend fun evaluate(context: DecisionContext): Result<DecisionResult>
}
```

Define `IncidentReport`, `AudioInput`, `ValidatedReportPatch`, and `DecisionContext` in shared domain/contracts code. Use Kotlin data classes, sealed result/error types, and `kotlinx.serialization` where compatible.

## Behavioral contract
- **STT:** Returns an explicit empty/error result; it must not invent a transcript.
- **LLM Extraction:** Returns only evidence-supported field updates.
- **Validation:** Validate JSON, field names and types before merging.
- **Presence-Aware Patch:** Omitted patch fields do not erase current values. Explicit corrections update only the target field.
- **Workflow Authority:** The deterministic workflow state machine—not the LLM—decides whether required fields are missing.
- **Decision Engine (Incident Classification & Triage):**
  - Default production implementation (`LocalDecisionEngine`) executes completely on-device using deterministic rules or constrained on-device LLM prompts.
  - Optional `JevDecisionAdapter` accesses TypeSafe AI's `POST /v1/systemone` API only during laptop evaluation or hybrid test builds. If network is unavailable, it must fail safely to the local engine without throwing or crashing.
- **TTS:** Must work offline; UI prompts remain usable if TTS fails.
- **Fake Adapters:** For local unit testing only, never silent demo fallbacks.
