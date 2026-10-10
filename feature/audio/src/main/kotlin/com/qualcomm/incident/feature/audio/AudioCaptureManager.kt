package com.qualcomm.incident.feature.audio

import com.qualcomm.incident.core.contracts.AudioInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Manages audio recording via Android AudioRecord on the Qualcomm QIDK board.
 * Captures 16kHz 16-bit mono PCM streams suitable for on-device STT.
 */
class AudioCaptureManager {

    /**
     * Starts continuous audio capture and emits raw PCM chunks.
     */
    fun startCapture(): Flow<ByteArray> {
        // Skeleton: In full implementation, configure AudioRecord with AudioSource.MIC or VOICE_RECOGNITION
        return emptyFlow()
    }

    /**
     * Captures a single utterance until silence is detected or manually stopped.
     */
    suspend fun captureUtterance(): AudioInput {
        // Skeleton: Returns placeholder audio input buffer
        return AudioInput(pcmBytes = ByteArray(0))
    }

    /**
     * Stops current recording session and releases hardware microphone resources.
     */
    fun stopCapture() {
        // Skeleton: stop and release AudioRecord
    }
}
