package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import com.qualcomm.incident.core.contracts.SttEngine
import com.qualcomm.incident.core.contracts.TranscriptResult

/**
 * On-device Whisper STT adapter targeting Qualcomm Hexagon NPU.
 * Implements [SttEngine] via Qualcomm AI Hub compiled Whisper-Small assets (fallback Whisper-Base).
 */
class WhisperSttAdapter : SttEngine {

    override suspend fun transcribe(audio: AudioInput): Result<TranscriptResult> {
        // Concrete on-device Whisper inference on QIDK
        // To be integrated in M3 once vendor runtime sample is verified
        return Result.failure(NotImplementedError("Whisper NPU runtime integration pending M3"))
    }
}
