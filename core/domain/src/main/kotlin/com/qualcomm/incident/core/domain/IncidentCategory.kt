package com.qualcomm.incident.core.domain

import kotlinx.serialization.Serializable

/**
 * Standard taxonomy of incident types.
 */
@Serializable
enum class IncidentCategory(val displayName: String) {
    ELECTRICAL("Electrical"),
    CHEMICAL("Chemical"),
    MECHANICAL("Mechanical"),
    FIRE("Fire / Explosion"),
    MEDICAL("Medical / Injury"),
    STRUCTURAL("Structural / Facilities"),
    NEAR_MISS("Near Miss"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): IncidentCategory? {
            if (value == null) return null
            val normalized = value.trim().uppercase().replace(" ", "_").replace("/", "_")
            return entries.firstOrNull { 
                it.name == normalized || it.displayName.equals(value.trim(), ignoreCase = true) 
            }
        }
    }
}
