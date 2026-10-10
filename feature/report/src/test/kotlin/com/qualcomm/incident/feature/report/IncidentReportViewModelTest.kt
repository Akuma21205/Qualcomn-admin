package com.qualcomm.incident.feature.report

import com.qualcomm.incident.adapter.decision.FakeDecisionEngine
import com.qualcomm.incident.adapter.llm.FakeIncidentExtractor
import com.qualcomm.incident.adapter.stt.AudioFixtures
import com.qualcomm.incident.adapter.stt.FakeSttEngine
import com.qualcomm.incident.adapter.tts.FakeSpeechOutput
import com.qualcomm.incident.core.contracts.AudioInput
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch
import com.qualcomm.incident.core.workflow.IncidentWorkflowStateMachine
import com.qualcomm.incident.core.workflow.RequiredFieldsPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

        // Allow async launch to execute
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
    fun testProcessNarrative_incompleteReportAsksFollowUpAndSpeaks() = runBlocking {
        // Provide partial patch missing location and dateTime
        fakeLlm.nextPatch = ReportPatch(
            incidentType = OptionalField.Present("Chemical"),
            severityLevel = OptionalField.Present("Critical"),
            incidentDescription = OptionalField.Present("Acid pipe ruptured")
        )

        viewModel.processNarrativeText("Chemical spill in acid pipe.")
        kotlinx.coroutines.delay(200)

        val state = viewModel.workflowState.value
        assertTrue(state is IncidentWorkflowStateMachine.State.AskingFollowUp)

        val followUp = state as IncidentWorkflowStateMachine.State.AskingFollowUp
        assertEquals(RequiredFieldsPolicy.MandatoryField.INCIDENT_LOCATION, followUp.missingField)

        // TTS should have spoken the follow-up prompt
        assertTrue(fakeTts.spokenTexts.isNotEmpty())
        assertEquals(followUp.question, fakeTts.spokenTexts.first())
    }

    @Test
    fun testManualUpdateField_overridesValue() = runBlocking {
        viewModel.updateField(RequiredFieldsPolicy.MandatoryField.INCIDENT_LOCATION, "Building 9 Warehouse")

        val report = viewModel.reportState.value
        assertEquals("Building 9 Warehouse", report.incidentLocation)
    }

    @Test
    fun testConfirmAndFinalize_transitionsToFinalizedAndSpeaks() = runBlocking {
        viewModel.confirmAndFinalize()
        kotlinx.coroutines.delay(200)

        assertEquals(IncidentWorkflowStateMachine.State.Finalized, viewModel.workflowState.value)
        assertTrue(fakeTts.spokenTexts.any { it.contains("confirmed and submitted") })
    }

    @Test
    fun testReset_clearsStateAndReport() = runBlocking {
        viewModel.updateField(RequiredFieldsPolicy.MandatoryField.INCIDENT_LOCATION, "Zone 3")
        viewModel.reset()

        assertEquals(IncidentWorkflowStateMachine.State.Idle, viewModel.workflowState.value)
        assertEquals(null, viewModel.reportState.value.incidentLocation)
    }
}
