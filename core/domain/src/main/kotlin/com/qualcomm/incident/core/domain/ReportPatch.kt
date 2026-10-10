package com.qualcomm.incident.core.domain

import kotlinx.serialization.Serializable

/**
 * Presence-aware field container to distinguish omitted fields from explicit clears.
 */
@Serializable
sealed interface OptionalField<out T> {
    data object NotPresent : OptionalField<Nothing>
    data class Present<T>(val value: T?) : OptionalField<T>
}

/**
 * Represents an incremental patch emitted by an extractor or UI correction.
 * Only fields with [OptionalField.Present] will be merged into the existing [IncidentReport].
 */
data class ReportPatch(
    val incidentType: OptionalField<String> = OptionalField.NotPresent,
    val severityLevel: OptionalField<String> = OptionalField.NotPresent,
    val incidentLocation: OptionalField<String> = OptionalField.NotPresent,
    val dateTime: OptionalField<String> = OptionalField.NotPresent,
    val personsInvolved: OptionalField<List<String>> = OptionalField.NotPresent,
    val equipmentAssetInvolved: OptionalField<String> = OptionalField.NotPresent,
    val incidentDescription: OptionalField<String> = OptionalField.NotPresent,
    val immediateActionsTaken: OptionalField<String> = OptionalField.NotPresent,
    val witnessInformation: OptionalField<List<String>> = OptionalField.NotPresent,
    val recommendedFollowUpAction: OptionalField<String> = OptionalField.NotPresent,
    val provenanceMap: Map<String, Provenance> = emptyMap()
)
