package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import com.qualcomm.incident.core.contracts.SttEngine
import com.qualcomm.incident.core.contracts.TranscriptResult

/**
 * Deterministic test fake for [SttEngine].
 * Used for local laptop development and unit testing without Qualcomm NPU hardware.
 *
 * NOTE: Prohibited as a silent demo fallback on the real board.
 */
class FakeSttEngine(
    var simulatedTranscript: String = "Fire in server room B at 10 AM, server rack overheated."
) : SttEngine {

    override suspend fun transcribe(audio: AudioInput): Result<TranscriptResult> {
        if (audio.pcmBytes.isEmpty()) {
            return Result.success(TranscriptResult(text = "", elapsedMs = 0))
        }
        return Result.success(
            TranscriptResult(
                text = simulatedTranscript,
                elapsedMs = 45L,
                isFinal = true
            )
        )
    }
}
