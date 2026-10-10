package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenieLlmAdapterTest {

    private val engine = SimulatedGenieEngine()
    private val adapter = GenieLlmAdapter(engine = engine)

    @Test
    fun `extractPatch returns empty patch immediately on blank transcript`() = runBlocking {
        val result = adapter.extractPatch("   ", IncidentReport())

        assertTrue(result.isSuccess)
        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.NotPresent, patch.incidentLocation)
        assertEquals(OptionalField.NotPresent, patch.incidentType)
        assertEquals(0, engine.executionHistory.size)
    }

    @Test
    fun `extractPatch coordinates prompt generation and parsing successfully`() = runBlocking {
        engine.registerResponse(
            promptSnippet = "Generator 4 caught fire",
            responseJson = """
            {
              "incident_type": "Fire / Explosion",
              "severity_level": "High",
              "equipment_asset_involved": "Generator 4",
              "incident_location": "Utility Room A"
            }
            """.trimIndent()
        )

        val result = adapter.extractPatch(
            transcript = "Generator 4 caught fire in Utility Room A, high severity!",
            currentReport = IncidentReport()
        )

        assertTrue(result.isSuccess)
        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Fire / Explosion"), patch.incidentType)
        assertEquals(OptionalField.Present("High"), patch.severityLevel)
        assertEquals(OptionalField.Present("Generator 4"), patch.equipmentAssetInvolved)
        assertEquals(OptionalField.Present("Utility Room A"), patch.incidentLocation)
        assertEquals(OptionalField.NotPresent, patch.personsInvolved)
        assertEquals(1, engine.executionHistory.size)
    }

    @Test
    fun `extractPatch handles multi-turn correction`() = runBlocking {
        val currentReport = IncidentReport(
            incidentType = "Chemical Spill",
            incidentLocation = "Building 1",
            incidentDescription = "Acid drum leak"
        )

        engine.registerResponse(
            promptSnippet = "Actually it happened in Building 2",
            responseJson = """
            {
              "incident_location": "Building 2",
              "persons_involved": ["David K."]
            }
            """.trimIndent()
        )

        val result = adapter.extractPatch(
            transcript = "Actually it happened in Building 2, and David K. was exposed.",
            currentReport = currentReport
        )

        assertTrue(result.isSuccess)
        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Building 2"), patch.incidentLocation)
        assertEquals(OptionalField.Present(listOf("David K.")), patch.personsInvolved)
        assertEquals(OptionalField.NotPresent, patch.incidentType) // Preserves existing Chemical Spill
        assertEquals(OptionalField.NotPresent, patch.incidentDescription)
    }

    @Test
    fun `extractPatch fails cleanly when engine fails`() = runBlocking {
        engine.shouldFail = true

        val result = adapter.extractPatch("Emergency in plant 1", IncidentReport())

        assertTrue(result.isFailure)
        assertEquals("Simulated GenieX NPU error", result.exceptionOrNull()?.message)
    }
}
