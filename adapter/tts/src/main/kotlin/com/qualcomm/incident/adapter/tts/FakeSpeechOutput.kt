package com.qualcomm.incident.adapter.tts

import com.qualcomm.incident.core.contracts.SpeechOutput

/**
 * In-memory test fake for [SpeechOutput].
 * Records spoken utterances for verification in unit tests.
 *
 * NOTE: Prohibited as a silent demo fallback on the real board.
 */
class FakeSpeechOutput : SpeechOutput {

    val spokenUtterances = mutableListOf<String>()
    var isStopped = false

    override suspend fun speak(text: String): Result<Unit> {
        isStopped = false
        spokenUtterances.add(text)
        return Result.success(Unit)
    }

    override fun stop() {
        isStopped = true
    }

    fun clear() {
        spokenUtterances.clear()
        isStopped = false
    }
}
