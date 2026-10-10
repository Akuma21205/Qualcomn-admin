package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages multi-turn conversation dialogue, turn history, corrections,
 * explicit negations, and undo/rollback capabilities for incident intake.
 */
class MultiTurnConversationManager(
    initialReport: IncidentReport = IncidentReport()
) {

    enum class TurnType {
        INITIAL_NARRATIVE,
        FOLLOW_UP_ANSWER,
        CORRECTION_OVERRIDE,
        NEGATION_REMOVAL,
        MANUAL_EDIT
    }

    data class ConversationTurn(
        val turnIndex: Int,
        val userUtterance: String,
        val turnType: TurnType,
        val appliedPatch: ReportPatch,
        val reportStateBefore: IncidentReport,
        val reportStateAfter: IncidentReport,
        val timestampMs: Long = System.currentTimeMillis()
    )

    private val _turns = MutableStateFlow<List<ConversationTurn>>(emptyList())
    val turns: StateFlow<List<ConversationTurn>> = _turns.asStateFlow()

    private val _currentReport = MutableStateFlow(initialReport)
    val currentReport: StateFlow<IncidentReport> = _currentReport.asStateFlow()

    /**
     * Applies a new conversation turn to the report, categorizing the interaction type
     * and preserving all unmentioned fields.
     */
    fun applyTurn(utterance: String, patch: ReportPatch): ConversationTurn {
        val before = _currentReport.value
        val turnType = detectTurnType(utterance, before, patch)
        val after = ReportPatchMerger.merge(before, patch)

        val newTurn = ConversationTurn(
            turnIndex = _turns.value.size + 1,
            userUtterance = utterance,
            turnType = turnType,
            appliedPatch = patch,
            reportStateBefore = before,
            reportStateAfter = after
        )

        _currentReport.value = after
        _turns.value = _turns.value + newTurn
        return newTurn
    }

    /**
     * Rolls back the last turn, restoring the report to its prior state.
     * Returns true if a turn was undone, or false if already at initial turn.
     */
    fun undoLastTurn(): Boolean {
        val history = _turns.value
        if (history.isEmpty()) return false

        val lastTurn = history.last()
        _currentReport.value = lastTurn.reportStateBefore
        _turns.value = history.dropLast(1)
        return true
    }

    /**
     * Resets the entire conversation back to an empty report.
     */
    fun reset() {
        _currentReport.value = IncidentReport()
        _turns.value = emptyList()
    }

    private fun detectTurnType(
        utterance: String,
        before: IncidentReport,
        patch: ReportPatch
    ): TurnType {
        val lower = utterance.lowercase().trim()

        // 1. Initial narrative if history is empty
        if (_turns.value.isEmpty()) {
            return TurnType.INITIAL_NARRATIVE
        }

        // 2. Explicit negation / clear keywords
        val negationKeywords = listOf("no one", "nobody", "no injuries", "no witness", "none", "cancel", "clear", "nothing")
        if (negationKeywords.any { lower.contains(it) }) {
            return TurnType.NEGATION_REMOVAL
        }

        // 3. Explicit correction keywords
        val correctionKeywords = listOf("correction", "actually", "scratch that", "instead of", "change to", "not ", "rather")
        if (correctionKeywords.any { lower.contains(it) }) {
            return TurnType.CORRECTION_OVERRIDE
        }

        // 4. Overwrite check: If a field was already populated and patch presents a new value
        if (isOverwritingExistingField(before, patch)) {
            return TurnType.CORRECTION_OVERRIDE
        }

        return TurnType.FOLLOW_UP_ANSWER
    }

    private fun isOverwritingExistingField(before: IncidentReport, patch: ReportPatch): Boolean {
        if (patch.incidentType is OptionalField.Present && before.incidentType != null) return true
        if (patch.severityLevel is OptionalField.Present && before.severityLevel != null) return true
        if (patch.incidentLocation is OptionalField.Present && before.incidentLocation != null) return true
        if (patch.dateTime is OptionalField.Present && before.dateTime != null) return true
        if (patch.incidentDescription is OptionalField.Present && before.incidentDescription != null) return true
        if (patch.equipmentAssetInvolved is OptionalField.Present && before.equipmentAssetInvolved != null) return true
        return false
    }
}
