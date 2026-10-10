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
import com.qualcomm.incident.core.domain.ReportPatch
import com.qualcomm.incident.core.domain.SeverityLevel
import com.qualcomm.incident.core.workflow.IncidentWorkflowStateMachine
import com.qualcomm.incident.core.workflow.MultiTurnConversationManager
import com.qualcomm.incident.core.workflow.ReportPatchMerger
import com.qualcomm.incident.core.workflow.RequiredFieldsPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates on-device voice reporting interaction with multi-turn support.
 * Coordinates STT, LLM entity extraction, local triage decision engine,
 * multi-turn turn tracking, corrections, undo rollback, and offline TTS prompts with visual fallback.
 */
class IncidentReportViewModel(
    val sttEngine: SttEngine,
    val extractor: IncidentExtractor,
    val speechOutput: SpeechOutput,
    val decisionEngine: DecisionEngine,
    val stateMachine: IncidentWorkflowStateMachine = IncidentWorkflowStateMachine(),
    val conversationManager: MultiTurnConversationManager = MultiTurnConversationManager(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    sealed interface TtsStatus {
        data object Ready : TtsStatus
        data object Speaking : TtsStatus
        data class Unavailable(val reason: String) : TtsStatus
    }

    val reportState: StateFlow<IncidentReport> = conversationManager.currentReport
    val workflowState: StateFlow<IncidentWorkflowStateMachine.State> = stateMachine.workflowState
    val conversationTurns: StateFlow<List<MultiTurnConversationManager.ConversationTurn>> = conversationManager.turns

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript.asStateFlow()

    private val _lastDecision = MutableStateFlow<DecisionResult?>(null)
    val lastDecision: StateFlow<DecisionResult?> = _lastDecision.asStateFlow()

    private val _ttsStatus = MutableStateFlow<TtsStatus>(TtsStatus.Ready)
    val ttsStatus: StateFlow<TtsStatus> = _ttsStatus.asStateFlow()

    /**
     * Ingests audio input and runs the multi-stage pipeline:
     * Audio -> STT -> LLM -> Multi-Turn Merge -> Decision Engine -> Follow-up/Review.
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
                    speakPrompt(missing.first().promptQuestion)
                }
                return@launch
            }

            processNarrativeText(text)
        }
    }

    /**
     * Processes narrative text (from ASR or manual text field entry).
     * Handles multi-turn incremental additions, explicit corrections, and negations.
     */
    fun processNarrativeText(text: String) {
        scope.launch {
            stateMachine.onTranscriptionComplete()

            val patchResult = extractor.extractPatch(text, reportState.value).getOrElse { error ->
                stateMachine.onError("Extraction error: ${error.message}")
                return@launch
            }

            // Apply turn to conversation manager (records turn history and updates report)
            conversationManager.applyTurn(text, patchResult.patch)
            var currentReport = conversationManager.currentReport.value

            // Evaluate triage/classification if incidentType or severityLevel not explicitly stated
            if (currentReport.incidentType == null || currentReport.severityLevel == null) {
                val decisionRes = decisionEngine.evaluate(
                    DecisionContext(
                        transcript = text,
                        currentReport = currentReport
                    )
                ).getOrNull()

                if (decisionRes != null) {
                    _lastDecision.value = decisionRes
                    val severityStr = SeverityLevel.fromScore(decisionRes.severityScore)?.displayName ?: "Medium"
                    val decisionPatch = ReportPatch(
                        incidentType = if (currentReport.incidentType == null) OptionalField.Present(decisionRes.incidentType) else OptionalField.NotPresent,
                        severityLevel = if (currentReport.severityLevel == null) OptionalField.Present(severityStr) else OptionalField.NotPresent
                    )
                    conversationManager.applyTurn("[Auto Triage: ${decisionRes.incidentType}]", decisionPatch)
                    currentReport = conversationManager.currentReport.value
                }
            }

            // Update state machine
            stateMachine.onPatchExtracted(patchResult.patch)

            // If missing fields exist, trigger verbal follow-up question
            val missing = RequiredFieldsPolicy.findMissingFields(currentReport)
            if (missing.isNotEmpty()) {
                speakPrompt(missing.first().promptQuestion)
            }
        }
    }

    /**
     * Applies an explicit manual or verbal correction to a specific mandatory field.
     */
    fun applyCorrection(field: RequiredFieldsPolicy.MandatoryField, newValue: String?) {
        val patch = when (field) {
            RequiredFieldsPolicy.MandatoryField.INCIDENT_TYPE ->
                ReportPatch(incidentType = OptionalField.Present(newValue?.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.SEVERITY_LEVEL ->
                ReportPatch(severityLevel = OptionalField.Present(newValue?.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.LOCATION ->
                ReportPatch(incidentLocation = OptionalField.Present(newValue?.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.DATE_TIME ->
                ReportPatch(dateTime = OptionalField.Present(newValue?.ifBlank { null }))
            RequiredFieldsPolicy.MandatoryField.DESCRIPTION ->
                ReportPatch(incidentDescription = OptionalField.Present(newValue?.ifBlank { null }))
        }

        conversationManager.applyTurn("Manual correction for ${field.displayName}", patch)
        stateMachine.onPatchExtracted(patch)
    }

    /**
     * Undoes the last conversation turn, rolling back the report state to the prior turn.
     */
    fun undoLastTurn(): Boolean {
        val undone = conversationManager.undoLastTurn()
        if (undone) {
            val restoredReport = conversationManager.currentReport.value
            val missing = RequiredFieldsPolicy.findMissingFields(restoredReport)
            if (missing.isNotEmpty()) {
                val next = missing.first()
                stateMachine.onPatchExtracted(ReportPatch()) // Re-evaluates state machine
                scope.launch { speakPrompt(next.promptQuestion) }
            }
        }
        return undone
    }

    /**
     * Directly updates a specific field (backward compatibility).
     */
    fun updateField(field: RequiredFieldsPolicy.MandatoryField, value: String) {
        applyCorrection(field, value)
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
        conversationManager.reset()
        stateMachine.reset()
        _lastTranscript.value = ""
        _lastDecision.value = null
        _ttsStatus.value = TtsStatus.Ready
    }

    /**
     * Speaks a verbal prompt offline with non-blocking visual fallback.
     */
    private suspend fun speakPrompt(prompt: String) {
        _ttsStatus.value = TtsStatus.Speaking
        try {
            speechOutput.speak(prompt)
            _ttsStatus.value = TtsStatus.Ready
        } catch (e: Exception) {
            // Visual prompt remains functional even if audio speech output fails
            _ttsStatus.value = TtsStatus.Unavailable(e.message ?: "TTS service offline")
        }
    }
}
