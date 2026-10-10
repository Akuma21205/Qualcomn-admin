package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.ReportPatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State machine managing the voice incident reporting lifecycle.
 */
class IncidentWorkflowStateMachine(
    initialReport: IncidentReport = IncidentReport()
) {
    sealed interface State {
        data object Idle : State
        data object Listening : State
        data object Transcribing : State
        data object Extracting : State
        data class AskingFollowUp(val question: String, val missingField: RequiredFieldsPolicy.MandatoryField) : State
        data object Reviewing : State
        data object Finalized : State
        data class Error(val message: String) : State
    }

    private val _reportState = MutableStateFlow(initialReport)
    val reportState: StateFlow<IncidentReport> = _reportState.asStateFlow()

    private val _workflowState = MutableStateFlow<State>(State.Idle)
    val workflowState: StateFlow<State> = _workflowState.asStateFlow()

    fun onStartListening() {
        _workflowState.value = State.Listening
    }

    fun onAudioCaptured() {
        _workflowState.value = State.Transcribing
    }

    fun onTranscriptionComplete() {
        _workflowState.value = State.Extracting
    }

    fun onPatchExtracted(patch: ReportPatch) {
        val updated = ReportPatchMerger.merge(_reportState.value, patch)
        _reportState.value = updated

        val missing = RequiredFieldsPolicy.findMissingFields(updated)
        _workflowState.value = if (missing.isNotEmpty()) {
            val next = missing.first()
            State.AskingFollowUp(next.promptQuestion, next)
        } else {
            State.Reviewing
        }
    }

    fun onConfirmAndFinalize() {
        _workflowState.value = State.Finalized
    }

    fun onError(message: String) {
        _workflowState.value = State.Error(message)
    }

    fun reset() {
        _reportState.value = IncidentReport()
        _workflowState.value = State.Idle
    }
}
