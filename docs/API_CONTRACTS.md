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
```

Define `IncidentReport`, `AudioInput`, and `ValidatedReportPatch` in shared domain/contracts code after the schema and supported audio format are confirmed. Use Kotlin data classes, sealed result/error types, and `kotlinx.serialization` where compatible.

## Behavioral contract
- STT returns an explicit empty/error result; it must not invent a transcript.
- LLM returns only evidence-supported field updates.
- Validate JSON, field names and types before merging.
- Omitted patch fields do not erase current values.
- Explicit user corrections update only the target field.
- Workflow—not the LLM—decides whether required fields are missing.
- TTS must work offline; UI prompts remain usable if TTS fails.
- Fake adapters are for local tests only, never silent demo fallbacks.
