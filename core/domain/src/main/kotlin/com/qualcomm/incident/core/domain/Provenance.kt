package com.qualcomm.incident.core.domain

import kotlinx.serialization.Serializable

/**
 * Tracks the evidence source/confidence for individual report fields.
 */
@Serializable
enum class Provenance {
    STATED,     // Directly spoken by the user
    NORMALIZED, // Standardized date/time or enumerated type
    INFERRED,   // Inferred with high certainty from transcript context
    UNKNOWN     // Not specified or unconfirmed
}
