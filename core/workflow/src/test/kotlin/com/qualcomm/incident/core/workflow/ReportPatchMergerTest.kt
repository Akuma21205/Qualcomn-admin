package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.Provenance
import com.qualcomm.incident.core.domain.ReportPatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReportPatchMergerTest {

    @Test
    fun omittedFieldsDoNotEraseExistingData() {
        val initial = IncidentReport(
            incidentType = "Chemical",
            incidentLocation = "Building 4, Lab 2",
            severityLevel = "High"
        )

        // Patch only supplies dateTime; everything else is NotPresent
        val patch = ReportPatch(
            dateTime = OptionalField.Present("2026-10-10 09:15"),
            provenanceMap = mapOf("dateTime" to Provenance.STATED)
        )

        val merged = ReportPatchMerger.merge(initial, patch)

        assertEquals("Chemical", merged.incidentType)
        assertEquals("Building 4, Lab 2", merged.incidentLocation)
        assertEquals("High", merged.severityLevel)
        assertEquals("2026-10-10 09:15", merged.dateTime)
        assertEquals(Provenance.STATED, merged.fieldProvenance["dateTime"])
    }

    @Test
    fun presentFieldReplacesExistingValue() {
        val initial = IncidentReport(
            incidentLocation = "Floor 1",
            severityLevel = "Low"
        )

        // Patch updates location to Floor 2
        val patch = ReportPatch(
            incidentLocation = OptionalField.Present("Floor 2"),
            provenanceMap = mapOf("incidentLocation" to Provenance.STATED)
        )

        val merged = ReportPatchMerger.merge(initial, patch)

        assertEquals("Floor 2", merged.incidentLocation)
        assertEquals("Low", merged.severityLevel)
    }

    @Test
    fun explicitClearSetsFieldToNull() {
        val initial = IncidentReport(
            equipmentAssetInvolved = "Conveyor Belt A",
            incidentLocation = "Dock 3"
        )

        // Patch explicitly clears equipmentAssetInvolved
        val patch = ReportPatch(
            equipmentAssetInvolved = OptionalField.Present(null)
        )

        val merged = ReportPatchMerger.merge(initial, patch)

        assertNull(merged.equipmentAssetInvolved)
        assertEquals("Dock 3", merged.incidentLocation)
    }

    @Test
    fun sequentialMultiTurnPatchesAccumulateAccurately() {
        var report = IncidentReport()

        // Turn 1: user reports location and type
        val turn1 = ReportPatch(
            incidentType = OptionalField.Present("Electrical"),
            incidentLocation = OptionalField.Present("Warehouse 5")
        )
        report = ReportPatchMerger.merge(report, turn1)

        // Turn 2: user follow-up supplies severity
        val turn2 = ReportPatch(
            severityLevel = OptionalField.Present("Medium")
        )
        report = ReportPatchMerger.merge(report, turn2)

        // Turn 3: user follow-up supplies description
        val turn3 = ReportPatch(
            incidentDescription = OptionalField.Present("Sparks observed from breaker panel.")
        )
        report = ReportPatchMerger.merge(report, turn3)

        assertEquals("Electrical", report.incidentType)
        assertEquals("Warehouse 5", report.incidentLocation)
        assertEquals("Medium", report.severityLevel)
        assertEquals("Sparks observed from breaker panel.", report.incidentDescription)
    }
}
