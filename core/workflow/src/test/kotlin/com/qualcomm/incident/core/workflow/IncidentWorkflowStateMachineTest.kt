package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentWorkflowStateMachineTest {

    @Test
    fun multiTurnVoiceFlowTransitionsCorrectly() {
        val sm = IncidentWorkflowStateMachine()
        assertEquals(IncidentWorkflowStateMachine.State.Idle, sm.workflowState.value)

        // Turn 1: User starts speaking
        sm.onStartListening()
        assertEquals(IncidentWorkflowStateMachine.State.Listening, sm.workflowState.value)

        sm.onAudioCaptured()
        assertEquals(IncidentWorkflowStateMachine.State.Transcribing, sm.workflowState.value)

        sm.onTranscriptionComplete()
        assertEquals(IncidentWorkflowStateMachine.State.Extracting, sm.workflowState.value)

        // Turn 1 extraction: missing date_time and description
        val initialPatch = ReportPatch(
            incidentType = OptionalField.Present("Electrical"),
            severityLevel = OptionalField.Present("High"),
            incidentLocation = OptionalField.Present("Panel B2")
        )
        sm.onPatchExtracted(initialPatch)

        val stateAfterTurn1 = sm.workflowState.value
        assertTrue(stateAfterTurn1 is IncidentWorkflowStateMachine.State.AskingFollowUp)
        assertEquals(
            RequiredFieldsPolicy.MandatoryField.DATE_TIME,
            (stateAfterTurn1 as IncidentWorkflowStateMachine.State.AskingFollowUp).missingField
        )

        // Turn 2: Follow-up response fills the remaining fields
        val followUpPatch = ReportPatch(
            dateTime = OptionalField.Present("2026-10-10 11:30 AM"),
            incidentDescription = OptionalField.Present("Breaker failed under heavy load.")
        )
        sm.onPatchExtracted(followUpPatch)

        // Now all required fields are complete -> transitions to Reviewing
        assertEquals(IncidentWorkflowStateMachine.State.Reviewing, sm.workflowState.value)
        assertEquals("Electrical", sm.reportState.value.incidentType)
        assertEquals("Panel B2", sm.reportState.value.incidentLocation)
        assertEquals("2026-10-10 11:30 AM", sm.reportState.value.dateTime)

        // User confirms report on review screen -> Finalized
        sm.onConfirmAndFinalize()
        assertEquals(IncidentWorkflowStateMachine.State.Finalized, sm.workflowState.value)
    }

    @Test
    fun errorStateHandlesGracefullyAndResets() {
        val sm = IncidentWorkflowStateMachine()
        sm.onError("Audio capture failed")

        val state = sm.workflowState.value
        assertTrue(state is IncidentWorkflowStateMachine.State.Error)
        assertEquals("Audio capture failed", (state as IncidentWorkflowStateMachine.State.Error).message)

        sm.reset()
        assertEquals(IncidentWorkflowStateMachine.State.Idle, sm.workflowState.value)
        assertTrue(sm.reportState.value.incidentType == null)
    }
}
