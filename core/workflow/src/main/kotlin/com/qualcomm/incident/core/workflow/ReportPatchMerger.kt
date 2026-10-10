package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.Provenance
import com.qualcomm.incident.core.domain.ReportPatch

/**
 * Deterministically merges an incremental [ReportPatch] into an existing [IncidentReport].
 *
 * Rules:
 * - Omitted fields ([OptionalField.NotPresent]) never overwrite existing data.
 * - Explicit values replace target fields.
 * - Provenance entries are updated alongside changed fields.
 */
object ReportPatchMerger {

    fun merge(current: IncidentReport, patch: ReportPatch): IncidentReport {
        val updatedProvenance = current.fieldProvenance.toMutableMap()

        fun <T> resolve(
            fieldName: String,
            currentVal: T?,
            optionalField: OptionalField<T>
        ): T? {
            return when (optionalField) {
                is OptionalField.NotPresent -> currentVal
                is OptionalField.Present -> {
                    patch.provenanceMap[fieldName]?.let { updatedProvenance[fieldName] = it }
                        ?: run { updatedProvenance[fieldName] = Provenance.STATED }
                    optionalField.value
                }
            }
        }

        return current.copy(
            incidentType = resolve("incidentType", current.incidentType, patch.incidentType),
            severityLevel = resolve("severityLevel", current.severityLevel, patch.severityLevel),
            incidentLocation = resolve("incidentLocation", current.incidentLocation, patch.incidentLocation),
            dateTime = resolve("dateTime", current.dateTime, patch.dateTime),
            personsInvolved = resolve("personsInvolved", current.personsInvolved, patch.personsInvolved),
            equipmentAssetInvolved = resolve("equipmentAssetInvolved", current.equipmentAssetInvolved, patch.equipmentAssetInvolved),
            incidentDescription = resolve("incidentDescription", current.incidentDescription, patch.incidentDescription),
            immediateActionsTaken = resolve("immediateActionsTaken", current.immediateActionsTaken, patch.immediateActionsTaken),
            witnessInformation = resolve("witnessInformation", current.witnessInformation, patch.witnessInformation),
            recommendedFollowUpAction = resolve("recommendedFollowUpAction", current.recommendedFollowUpAction, patch.recommendedFollowUpAction),
            fieldProvenance = updatedProvenance
        )
    }
}
