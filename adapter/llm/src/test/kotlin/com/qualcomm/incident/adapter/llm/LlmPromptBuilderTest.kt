package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.domain.IncidentReport
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmPromptBuilderTest {

    private val builder = LlmPromptBuilder()

    @Test
    fun `buildPrompt formats with Qwen ChatML tokens by default`() {
        val prompt = builder.buildPrompt(
            transcript = "Chemical leak in processing bay 3",
            modelFamily = ModelFamily.QWEN
        )

        assertTrue(prompt.contains("<|im_start|>system"))
        assertTrue(prompt.contains("<|im_end|>"))
        assertTrue(prompt.contains("<|im_start|>user"))
        assertTrue(prompt.contains("<|im_start|>assistant"))
        assertTrue(prompt.contains("Chemical leak in processing bay 3"))
    }

    @Test
    fun `buildPrompt formats with Llama tokens when requested`() {
        val prompt = builder.buildPrompt(
            transcript = "Forklift damaged rack in Warehouse A",
            modelFamily = ModelFamily.LLAMA
        )

        assertTrue(prompt.contains("<|begin_of_text|>"))
        assertTrue(prompt.contains("<|start_header_id|>system<|end_header_id|>"))
        assertTrue(prompt.contains("<|start_header_id|>user<|end_header_id|>"))
        assertTrue(prompt.contains("<|start_header_id|>assistant<|end_header_id|>"))
        assertTrue(prompt.contains("Forklift damaged rack in Warehouse A"))
    }

    @Test
    fun `prompt includes strict anti-hallucination and schema instructions`() {
        val prompt = builder.buildPrompt("Fire in electrical closet")

        assertTrue(prompt.contains("NO HALLUCINATION"))
        assertTrue(prompt.contains("OMIT UNMENTIONED FIELDS"))
        assertTrue(prompt.contains("JSON SCHEMA"))
        assertTrue(prompt.contains("incident_location"))
        assertTrue(prompt.contains("severity_level"))
    }

    @Test
    fun `prompt includes current report state when fields are populated`() {
        val currentReport = IncidentReport(
            incidentType = "Chemical Spill",
            incidentLocation = "Building 4"
        )

        val prompt = builder.buildPrompt(
            transcript = "Actually it happened in Building 5 at 2 PM",
            currentReport = currentReport
        )

        assertTrue(prompt.contains("Current Incident Report State:"))
        assertTrue(prompt.contains("- incident_type: Chemical Spill"))
        assertTrue(prompt.contains("- incident_location: Building 4"))
        assertTrue(prompt.contains("New User Utterance:"))
        assertTrue(prompt.contains("Actually it happened in Building 5 at 2 PM"))
    }

    @Test
    fun `prompt omits current report header when report is completely empty`() {
        val prompt = builder.buildPrompt(
            transcript = "Small fire extinguished in break room",
            currentReport = IncidentReport()
        )

        assertFalse(prompt.contains("Current Incident Report State:"))
        assertTrue(prompt.contains("User Utterance:"))
        assertTrue(prompt.contains("Small fire extinguished in break room"))
    }
}
