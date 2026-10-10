package com.qualcomm.incident.core.contracts

/**
 * Interface for offline Text-to-Speech output.
 */
interface SpeechOutput {
    /**
     * Speaks the provided text using offline speech synthesis.
     */
    suspend fun speak(text: String): Result<Unit>

    /**
     * Halts ongoing speech playback.
     */
    fun stop()
}
