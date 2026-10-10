package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.domain.OptionalField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmPatchParserTest {

    private val parser = LlmPatchParser()

    @Test
    fun `parses complete valid json with snake_case keys`() {
        val jsonOutput = """
        {
          "incident_type": "Fire / Explosion",
          "severity_level": "High",
          "incident_location": "Server Room B",
          "date_time": "10:15 AM",
          "persons_involved": ["Alex Smith", "Dana Lee"],
          "equipment_asset_involved": "Rack UPS-3",
          "incident_description": "Battery pack ruptured and sparked flame.",
          "immediate_actions_taken": "CO2 extinguisher deployed and room isolated.",
          "witness_information": ["Marcus Vance"],
          "recommended_follow_up_action": "Replace UPS capacitor and inspect thermal sensor."
        }
        """.trimIndent()

        val result = parser.parse(jsonOutput, latencyMs = 210L)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Fire / Explosion"), patch.incidentType)
        assertEquals(OptionalField.Present("High"), patch.severityLevel)
        assertEquals(OptionalField.Present("Server Room B"), patch.incidentLocation)
        assertEquals(OptionalField.Present("10:15 AM"), patch.dateTime)
        assertEquals(OptionalField.Present(listOf("Alex Smith", "Dana Lee")), patch.personsInvolved)
        assertEquals(OptionalField.Present("Rack UPS-3"), patch.equipmentAssetInvolved)
        assertEquals(OptionalField.Present("Battery pack ruptured and sparked flame."), patch.incidentDescription)
        assertEquals(OptionalField.Present("CO2 extinguisher deployed and room isolated."), patch.immediateActionsTaken)
        assertEquals(OptionalField.Present(listOf("Marcus Vance")), patch.witnessInformation)
        assertEquals(OptionalField.Present("Replace UPS capacitor and inspect thermal sensor."), patch.recommendedFollowUpAction)

        assertEquals(210L, result.getOrThrow().latencyMs)
        assertEquals(10, patch.provenanceMap.size)
    }

    @Test
    fun `partial json extraction maps omitted keys to NotPresent`() {
        val jsonOutput = """
        {
          "incident_location": "Warehouse C Bay 4",
          "incident_description": "Pallet collapsed onto walkway"
        }
        """.trimIndent()

        val result = parser.parse(jsonOutput)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Warehouse C Bay 4"), patch.incidentLocation)
        assertEquals(OptionalField.Present("Pallet collapsed onto walkway"), patch.incidentDescription)

        // All unmentioned fields MUST be NotPresent
        assertEquals(OptionalField.NotPresent, patch.incidentType)
        assertEquals(OptionalField.NotPresent, patch.severityLevel)
        assertEquals(OptionalField.NotPresent, patch.dateTime)
        assertEquals(OptionalField.NotPresent, patch.personsInvolved)
        assertEquals(OptionalField.NotPresent, patch.equipmentAssetInvolved)
        assertEquals(OptionalField.NotPresent, patch.immediateActionsTaken)
        assertEquals(OptionalField.NotPresent, patch.witnessInformation)
        assertEquals(OptionalField.NotPresent, patch.recommendedFollowUpAction)

        assertEquals(2, patch.provenanceMap.size)
        assertNotNull(patch.provenanceMap["incidentLocation"])
        assertNotNull(patch.provenanceMap["incidentDescription"])
    }

    @Test
    fun `parses json wrapped in markdown code fence`() {
        val llmOutputWithFence = """
        Here is the extracted information:
        ```json
        {
          "incident_type": "Chemical Spill",
          "severity_level": "Medium",
          "incident_location": "Laboratory 2"
        }
        ```
        Hope this helps!
        """.trimIndent()

        val result = parser.parse(llmOutputWithFence)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Chemical Spill"), patch.incidentType)
        assertEquals(OptionalField.Present("Medium"), patch.severityLevel)
        assertEquals(OptionalField.Present("Laboratory 2"), patch.incidentLocation)
        assertEquals(OptionalField.NotPresent, patch.personsInvolved)
    }

    @Test
    fun `strips think tags emitted by Qwen or reasoning models`() {
        val outputWithThinking = """
        <think>
        The user mentions a hydraulic oil leak near machine press 9. No injuries mentioned.
        Extract incident_description and equipment_asset_involved.
        </think>
        {
          "equipment_asset_involved": "Press 9",
          "incident_description": "Hydraulic oil leak"
        }
        """.trimIndent()

        val result = parser.parse(outputWithThinking)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Press 9"), patch.equipmentAssetInvolved)
        assertEquals(OptionalField.Present("Hydraulic oil leak"), patch.incidentDescription)
    }

    @Test
    fun `supports camelCase keys gracefully`() {
        val jsonOutput = """
        {
          "incidentType": "Equipment Failure",
          "incidentLocation": "Substation 1",
          "personsInvolved": ["Sam Green"]
        }
        """.trimIndent()

        val result = parser.parse(jsonOutput)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Equipment Failure"), patch.incidentType)
        assertEquals(OptionalField.Present("Substation 1"), patch.incidentLocation)
        assertEquals(OptionalField.Present(listOf("Sam Green")), patch.personsInvolved)
    }

    @Test
    fun `handles explicit empty list and explicit null`() {
        val jsonOutput = """
        {
          "incident_location": "Bay 12",
          "persons_involved": [],
          "witness_information": null
        }
        """.trimIndent()

        val result = parser.parse(jsonOutput)
        assertTrue(result.isSuccess)

        val patch = result.getOrThrow().patch
        assertEquals(OptionalField.Present("Bay 12"), patch.incidentLocation)
        assertEquals(OptionalField.Present(emptyList<String>()), patch.personsInvolved)
        assertEquals(OptionalField.Present(null), patch.witnessInformation)
        assertEquals(OptionalField.NotPresent, patch.severityLevel)
    }

    @Test
    fun `fails gracefully when no json object is present`() {
        val garbageOutput = "Sorry, I am not sure what you mean. Could you repeat?"

        val result = parser.parse(garbageOutput)
        assertTrue(result.isFailure)
    }

    @Test
    fun `fails gracefully on malformed json`() {
        val brokenJson = "{\"incident_type\": \"Fire\", broken_json"

        val result = parser.parse(brokenJson)
        assertTrue(result.isFailure)
    }
}
