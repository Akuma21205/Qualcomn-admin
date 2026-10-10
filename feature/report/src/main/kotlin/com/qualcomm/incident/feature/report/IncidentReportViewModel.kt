package com.qualcomm.incident.feature.report

import com.qualcomm.incident.core.contracts.AudioInput
import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.contracts.DecisionEngine
import com.qualcomm.incident.core.contracts.DecisionResult
import com.qualcomm.incident.core.contracts.IncidentExtractor
import com.qualcomm.incident.core.contracts.SpeechOutput
import com.qualcomm.incident.core.contracts.SttEngine
import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.Provenance
import com.qualcomm.incident.core.domain.ReportPatch
import com.qualcomm.incident.core.workflow.IncidentWorkflowStateMachine
import com.qualcomm.incident.core.workflow.ReportPatchMerger
import com.qualcomm.incident.core.workflow.RequiredFieldsPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates on-device voice reporting interaction.
 * Coordinates STT, LLM entity extraction, local triage decision engine,
 * deterministic patch merging, and offline TTS prompts.
 */
class IncidentReportViewModel(
    val sttEngine: SttEngine,
    val extractor: IncidentExtractor,
    val speechOutput: SpeechOutput,
    val decisionEngine: DecisionEngine,
    val stateMachine: IncidentWorkflowStateMachine = IncidentWorkflowStateMachine(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    val reportState: StateFlow<IncidentReport> = stateMachine.reportState
    val workflowState: StateFlow<IncidentWorkflowStateMachine.State> = stateMachine.workflowState

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    private val _lastDecision = MutableStateFlow<DecisionResult?>(null)
    val lastDecision: StateFlow<DecisionResult?> = _lastDecision.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    /**
     * Ingests audio input and runs the multi-stage pipeline:
     * Audio -> STT -> LLM -> Deterministic Merge -> Decision Engine -> Follow-up/Review.
     */
    fun processAudio(audio: AudioInput) {
        scope.launch {
            stateMachine.onAudioCaptured()

            val transcriptResult = sttEngine.transcribe(audio).getOrElse { error ->
                stateMachine.onError("Transcription error: ${error.message}")
                return@launch
            }

            val text = transcriptResult.text.trim()
            _lastTranscript.value = text

            if (text.isEmpty()) {
                // Audio was silence / empty, return to prior state without hallucination
                val missing = RequiredFieldsPolicy.findMissingFields(reportState.value)
                if (missing.isNotEmpty()) {
                    val next = missing.first()
                    // Re-prompt missing field
                    speakPrompt(next.promptQuestion)
                }
                return@launch
            }

            processNarrativeText(text)
        }
    }

    /**
     * Processes narrative text (from ASR or manual text field entry).
     */
    fun processNarrativeText(text: String) {
        scope.launch {
            stateMachine.onTranscriptionComplete()

            val patchResult = extractor.extractPatch(text, reportState.value).getOrElse { error ->
                stateMachine.onError("Extraction error: ${error.message}")
                return@launch
            }

            var mergedReport = ReportPatchMerger.merge(reportState.value, patchResult.patch)

            // Evaluate triage/classification if incidentType or severityLevel not explicitly stated
            if (mergedReport.incidentType == null || mergedReport.severityLevel == null) {
                val decisionRes = decisionEngine.evaluate(
                    DecisionContext(
                        transcript = text,
                        currentReport = mergedReport
                    )
                ).getOrNull()

                if (decisionRes != null) {
                    _lastDecision.value = decisionRes
                    val decisionPatch = ReportPatch(
                        incidentType = if (mergedReport.incidentType == null) OptionalField.Present(decisionRes.incidentType) else OptionalField.NotPresent,
                        severityLevel = if (mergedReport.severityLevel == null) OptionalField.Present(
                            if (decisionRes.isUrgent) "Critical" else "Medium"
                        ) else OptionalField.NotPresent
                    )
                    mergedReport = ReportPatchMerger.merge(mergedReport, decisionPatch)
                }
            }

            stateMachine.onPatchExtracted(patchResult.patch)

            // If missing fields exist, trigger verbal follow-up question
            val missing = RequiredFieldsPolicy.findMissingFields(mergedReport)
            if (missing.isNotEmpty()) {
                speakPrompt(missing.first().promptQuestion)
            }
        }
    }

    /**
     * Directly updates a specific field (manual review override).
     */
    fun updateField(field: RequiredFieldsPolicy.MandatoryField, value: String) {
        val patch = when (field) {
            RequiredFieldsPolicy.MandatoryField.INCIDENT_TYPE ->
                ReportPatch(incidentType = OptionalField.Present(value.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.SEVERITY_LEVEL ->
                ReportPatch(severityLevel = OptionalField.Present(value.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.INCIDENT_LOCATION ->
                ReportPatch(incidentLocation = OptionalField.Present(value.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.DATE_TIME ->
                ReportPatch(dateTime = OptionalField.Present(value.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.INCIDENT_DESCRIPTION ->
                ReportPatch(incidentDescription = OptionalField.Present(value.ifBlank { null }))
        }
        stateMachine.onPatchExtracted(patch)
    }

    /**
     * Confirms the report and transitions to Finalized state.
     */
    fun confirmAndFinalize() {
        scope.launch {
            stateMachine.onConfirmAndFinalize()
            speakPrompt("Incident report confirmed and submitted offline.")
        }
    }

    fun startListening() {
        stateMachine.onStartListening()
    }

    fun reset() {
        stateMachine.reset()
        _lastTranscript.value = ""
        _lastDecision.value = null
    }

    private suspend fun speakPrompt(prompt: String) {
        _isSpeaking.value = true
        try {
            speechOutput.speak(prompt)
        } catch (_: Exception) {
            // Visual prompt remains functional even if audio speech output fails
        } finally {
            _isSpeaking.value = false
        }
    }
}
