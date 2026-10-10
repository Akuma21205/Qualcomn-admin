package com.qualcomm.incident.core.domain

import kotlinx.serialization.Serializable

/**
 * Domain entity representing an incident report.
 *
 * Rules:
 * - Unstated values remain null.
 * - Never invent severity, names, witnesses, assets, actions, or dates.
 */
@Serializable
data class IncidentReport(
    val incidentType: String? = null,
    val severityLevel: String? = null,
    val incidentLocation: String? = null,
    val dateTime: String? = null,
    val personsInvolved: List<String>? = null,
    val equipmentAssetInvolved: String? = null,
    val incidentDescription: String? = null,
    val immediateActionsTaken: String? = null,
    val witnessInformation: List<String>? = null,
    val recommendedFollowUpAction: String? = null,
    val fieldProvenance: Map<String, Provenance> = emptyMap()
)
