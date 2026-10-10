package com.qualcomm.incident.core.domain

import kotlinx.serialization.Serializable

/**
 * Standard severity classification.
 */
@Serializable
enum class SeverityLevel(val level: Int, val displayName: String) {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");

    companion object {
        fun fromString(value: String?): SeverityLevel? {
            if (value == null) return null
            val normalized = value.trim().uppercase()
            return entries.firstOrNull { 
                it.name == normalized || it.displayName.equals(value.trim(), ignoreCase = true) 
            }
        }

        fun fromScore(score: Int?): SeverityLevel? {
            return when (score) {
                1 -> LOW
                2 -> MEDIUM
                3, 4 -> HIGH
                5 -> CRITICAL
                else -> null
            }
        }
    }
}
