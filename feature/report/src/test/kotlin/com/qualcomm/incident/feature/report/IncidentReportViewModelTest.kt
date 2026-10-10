package com.qualcomm.incident.feature.report

import com.qualcomm.incident.adapter.decision.FakeDecisionEngine
import com.qualcomm.incident.adapter.llm.FakeIncidentExtractor
import com.qualcomm.incident.adapter.stt.AudioFixtures
import com.qualcomm.incident.adapter.stt.FakeSttEngine
import com.qualcomm.incident.adapter.tts.FakeSpeechOutput
import com.qualcomm.incident.core.contracts.SpeechOutput
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch
import com.qualcomm.incident.core.workflow.IncidentWorkflowStateMachine
import com.qualcomm.incident.core.workflow.RequiredFieldsPolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncidentReportViewModelTest {

    private lateinit var fakeStt: FakeSttEngine
    private lateinit var fakeLlm: FakeIncidentExtractor
    private lateinit var fakeTts: FakeSpeechOutput
    private lateinit var fakeDecision: FakeDecisionEngine
    private lateinit var viewModel: IncidentReportViewModel

    @Before
    fun setUp() {
        fakeStt = FakeSttEngine(simulatedTranscript = "Fire in server room B at 10 AM, server rack overheated.")
        fakeLlm = FakeIncidentExtractor()
        fakeTts = FakeSpeechOutput()
        fakeDecision = FakeDecisionEngine()

        viewModel = IncidentReportViewModel(
            sttEngine = fakeStt,
            extractor = fakeLlm,
            speechOutput = fakeTts,
            decisionEngine = fakeDecision
        )
    }

    @Test
    fun testProcessAudio_extractsPatchAndTransitionsState() = runBlocking {
        fakeLlm.nextPatch = ReportPatch(
            incidentType = OptionalField.Present("Fire"),
            severityLevel = OptionalField.Present("High"),
            incidentLocation = OptionalField.Present("Server Room B"),
            dateTime = OptionalField.Present("10:00 AM"),
            incidentDescription = OptionalField.Present("Server rack overheated")
        )

        val audio = AudioFixtures.createActiveSpeechToneAudio(durationMs = 500)
        viewModel.processAudio(audio)

        kotlinx.coroutines.delay(200)

        val report = viewModel.reportState.value
        assertEquals("Fire", report.incidentType)
        assertEquals("High", report.severityLevel)
        assertEquals("Server Room B", report.incidentLocation)
        assertEquals("10:00 AM", report.dateTime)
        assertEquals("Server rack overheated", report.incidentDescription)

        // All mandatory fields present -> state should be Reviewing
        assertEquals(IncidentWorkflowStateMachine.State.Reviewing, viewModel.workflowState.value)
    }

    @Test
    fun testMultiTurn_followUpAndCorrectionSequence() = runBlocking {
        // Turn 1: Partial report missing location and dateTime
        fakeLlm.nextPatch = ReportPatch(
            incidentType = OptionalField.Present("Chemical"),
            severityLevel = OptionalField.Present("Critical"),
            incidentDescription = OptionalField.Present("Acid pipe ruptured")
        )
        viewModel.processNarrativeText("Chemical spill in acid pipe.")
        kotlinx.coroutines.delay(200)

        assertTrue(viewModel.workflowState.value is IncidentWorkflowStateMachine.State.AskingFollowUp)
        assertEquals("Chemical", viewModel.reportState.value.incidentType)

        // Turn 2: Follow-up response answering location
        fakeLlm.nextPatch = ReportPatch(
            incidentLocation = OptionalField.Present("Reactor Room 3")
        )
        viewModel.processNarrativeText("It happened in Reactor Room 3.")
        kotlinx.coroutines.delay(200)

        // Turn 1 fields MUST remain preserved
        assertEquals("Chemical", viewModel.reportState.value.incidentType)
        assertEquals("Critical", viewModel.reportState.value.severityLevel)
        assertEquals("Acid pipe ruptured", viewModel.reportState.value.incidentDescription)
        assertEquals("Reactor Room 3", viewModel.reportState.value.incidentLocation)

        // Turn 3: User correction of location
        viewModel.applyCorrection(RequiredFieldsPolicy.MandatoryField.LOCATION, "Reactor Room 4")
        assertEquals("Reactor Room 4", viewModel.reportState.value.incidentLocation)
        assertEquals("Chemical", viewModel.reportState.value.incidentType)
    }

    @Test
    fun testTtsFailure_fallsBackVisuallyWithoutBlockingWorkflow() = runBlocking {
        val failingTts = object : SpeechOutput {
            override suspend fun speak(text: String) {
                throw IllegalStateException("Offline TTS voice pack not installed")
            }
            override fun stop() {}
            override fun isAvailable(): Boolean = false
        }

        val vmWithFailingTts = IncidentReportViewModel(
            sttEngine = fakeStt,
            extractor = fakeLlm,
            speechOutput = failingTts,
            decisionEngine = fakeDecision
        )

        fakeLlm.nextPatch = ReportPatch(
            incidentType = OptionalField.Present("Chemical"),
            severityLevel = OptionalField.Present("Critical"),
            incidentDescription = OptionalField.Present("Acid leak")
        )

        // Process narrative - TTS will fail, but state machine should still move to AskingFollowUp!
        vmWithFailingTts.processNarrativeText("Acid leak occurred.")
        kotlinx.coroutines.delay(200)

        // State machine must NOT be in error, must be in AskingFollowUp
        assertTrue(vmWithFailingTts.workflowState.value is IncidentWorkflowStateMachine.State.AskingFollowUp)
        // TTS status reflects graceful failure
        assertTrue(vmWithFailingTts.ttsStatus.value is IncidentReportViewModel.TtsStatus.Unavailable)
    }

    @Test
    fun testUndoLastTurn_rollsBackReportState() = runBlocking {
        viewModel.applyCorrection(RequiredFieldsPolicy.MandatoryField.LOCATION, "Initial Location A")
        assertEquals("Initial Location A", viewModel.reportState.value.incidentLocation)

        viewModel.applyCorrection(RequiredFieldsPolicy.MandatoryField.LOCATION, "Updated Location B")
        assertEquals("Updated Location B", viewModel.reportState.value.incidentLocation)

        val undone = viewModel.undoLastTurn()
        assertTrue(undone)
        assertEquals("Initial Location A", viewModel.reportState.value.incidentLocation)
    }

    @Test
    fun testConfirmAndFinalize_transitionsToFinalized() = runBlocking {
        viewModel.confirmAndFinalize()
        kotlinx.coroutines.delay(200)

        assertEquals(IncidentWorkflowStateMachine.State.Finalized, viewModel.workflowState.value)
        assertTrue(fakeTts.spokenTexts.any { it.contains("confirmed and submitted") })
    }

    @Test
    fun testReset_clearsAllState() = runBlocking {
        viewModel.applyCorrection(RequiredFieldsPolicy.MandatoryField.LOCATION, "Zone 3")
        viewModel.reset()

        assertEquals(IncidentWorkflowStateMachine.State.Idle, viewModel.workflowState.value)
        assertEquals(null, viewModel.reportState.value.incidentLocation)
        assertEquals(0, viewModel.conversationTurns.value.size)
    }
}
