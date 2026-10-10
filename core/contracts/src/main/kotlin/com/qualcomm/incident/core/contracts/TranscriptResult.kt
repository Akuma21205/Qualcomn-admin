package com.qualcomm.incident.core.contracts

/**
 * Result returned by an on-device Speech-to-Text engine.
 */
data class TranscriptResult(
    val text: String,
    val elapsedMs: Long? = null,
    val isFinal: Boolean = true
)
