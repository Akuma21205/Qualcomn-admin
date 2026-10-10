package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequiredFieldsPolicyTest {

    @Test
    fun emptyReportIdentifiesAllMandatoryFieldsMissing() {
        val empty = IncidentReport()
        val missing = RequiredFieldsPolicy.findMissingFields(empty)

        assertEquals(5, missing.size)
        assertFalse(RequiredFieldsPolicy.isComplete(empty))
        assertEquals(RequiredFieldsPolicy.MandatoryField.INCIDENT_TYPE.promptQuestion, RequiredFieldsPolicy.nextPrompt(empty))
    }

    @Test
    fun partiallyCompletedReportIdentifiesRemainingMissing() {
        val partial = IncidentReport(
            incidentType = "Mechanical",
            severityLevel = "Low"
        )
        val missing = RequiredFieldsPolicy.findMissingFields(partial)

        assertEquals(3, missing.size)
        assertTrue(missing.contains(RequiredFieldsPolicy.MandatoryField.LOCATION))
        assertTrue(missing.contains(RequiredFieldsPolicy.MandatoryField.DATE_TIME))
        assertTrue(missing.contains(RequiredFieldsPolicy.MandatoryField.DESCRIPTION))
        assertFalse(RequiredFieldsPolicy.isComplete(partial))
        assertEquals(RequiredFieldsPolicy.MandatoryField.LOCATION.promptQuestion, RequiredFieldsPolicy.nextPrompt(partial))
    }

    @Test
    fun fullyCompletedReportReturnsCompleteAndNullPrompt() {
        val complete = IncidentReport(
            incidentType = "Fire",
            severityLevel = "Critical",
            incidentLocation = "Main Refinery Unit 3",
            dateTime = "2026-10-10 12:00",
            incidentDescription = "Flare line rupture."
        )

        assertTrue(RequiredFieldsPolicy.isComplete(complete))
        assertTrue(RequiredFieldsPolicy.findMissingFields(complete).isEmpty())
        assertNull(RequiredFieldsPolicy.nextPrompt(complete))
    }
}
