package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport

/**
 * Deterministic policy evaluating mandatory field completion and selecting follow-up questions.
 */
object RequiredFieldsPolicy {

    enum class MandatoryField(val displayName: String, val promptQuestion: String) {
        INCIDENT_TYPE("Incident Type", "What type of incident occurred?"),
        SEVERITY_LEVEL("Severity Level", "What is the severity level of this incident?"),
        LOCATION("Incident Location", "Where did the incident occur?"),
        DATE_TIME("Date and Time", "When did this incident happen?"),
        DESCRIPTION("Description", "Please provide a brief description of what happened.")
    }

    /**
     * Identifies which mandatory fields are currently null or blank.
     */
    fun findMissingFields(report: IncidentReport): List<MandatoryField> {
        val missing = mutableListOf<MandatoryField>()
        if (report.incidentType.isNullOrBlank()) missing.add(MandatoryField.INCIDENT_TYPE)
        if (report.severityLevel.isNullOrBlank()) missing.add(MandatoryField.SEVERITY_LEVEL)
        if (report.incidentLocation.isNullOrBlank()) missing.add(MandatoryField.LOCATION)
        if (report.dateTime.isNullOrBlank()) missing.add(MandatoryField.DATE_TIME)
        if (report.incidentDescription.isNullOrBlank()) missing.add(MandatoryField.DESCRIPTION)
        return missing
    }

    /**
     * Determines whether the report has satisfied all required fields.
     */
    fun isComplete(report: IncidentReport): Boolean {
        return findMissingFields(report).isEmpty()
    }

    /**
     * Selects the next follow-up question to ask the user.
     */
    fun nextPrompt(report: IncidentReport): String? {
        return findMissingFields(report).firstOrNull()?.promptQuestion
    }
}
