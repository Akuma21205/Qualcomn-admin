package com.qualcomm.incident.core.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentReportTest {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun defaultReportHasAllFieldsNull() {
        val report = IncidentReport()
        assertNull(report.incidentType)
        assertNull(report.severityLevel)
        assertNull(report.incidentLocation)
        assertNull(report.dateTime)
        assertNull(report.personsInvolved)
        assertNull(report.equipmentAssetInvolved)
        assertNull(report.incidentDescription)
        assertNull(report.immediateActionsTaken)
        assertNull(report.witnessInformation)
        assertNull(report.recommendedFollowUpAction)
        assertTrue(report.fieldProvenance.isEmpty())
    }

    @Test
    fun serializationRoundTripPreservesNullsAndValues() {
        val original = IncidentReport(
            incidentType = "Electrical",
            severityLevel = "Medium",
            incidentLocation = "Main Substation",
            dateTime = "2026-10-10 14:30",
            personsInvolved = listOf("Alice", "Bob"),
            incidentDescription = "Transformer tripped",
            fieldProvenance = mapOf("incidentType" to Provenance.STATED)
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<IncidentReport>(serialized)

        assertEquals(original, deserialized)
        assertEquals(Provenance.STATED, deserialized.fieldProvenance["incidentType"])
        assertNull(deserialized.witnessInformation)
    }

    @Test
    fun incidentCategoryParsingWorksAccurately() {
        assertEquals(IncidentCategory.ELECTRICAL, IncidentCategory.fromString("electrical"))
        assertEquals(IncidentCategory.FIRE, IncidentCategory.fromString("Fire / Explosion"))
        assertEquals(IncidentCategory.NEAR_MISS, IncidentCategory.fromString("near miss"))
        assertNull(IncidentCategory.fromString(null))
    }

    @Test
    fun severityLevelParsingWorksAccurately() {
        assertEquals(SeverityLevel.LOW, SeverityLevel.fromString("low"))
        assertEquals(SeverityLevel.CRITICAL, SeverityLevel.fromString("CRITICAL"))
        assertEquals(SeverityLevel.HIGH, SeverityLevel.fromScore(4))
        assertEquals(SeverityLevel.CRITICAL, SeverityLevel.fromScore(5))
        assertNull(SeverityLevel.fromString(null))
    }
}
