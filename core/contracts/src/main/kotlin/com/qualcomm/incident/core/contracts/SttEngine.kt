package com.qualcomm.incident.core.contracts

/**
 * Interface for on-device Automatic Speech Recognition (e.g. Whisper on Hexagon NPU).
 */
interface SttEngine {
    /**
     * Transcribes input audio into text.
     * Must not hallucinate text on empty or unintelligible audio.
     */
    suspend fun transcribe(audio: AudioInput): Result<TranscriptResult>
}
