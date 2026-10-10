package com.qualcomm.incident.adapter.tts

import com.qualcomm.incident.core.contracts.SpeechOutput

/**
 * On-device offline Text-to-Speech adapter.
 * Uses Android TextToSpeech engine with preloaded offline voice assets.
 */
class AndroidSpeechOutput : SpeechOutput {

    override suspend fun speak(text: String): Result<Unit> {
        // Concrete offline TTS playback
        return Result.failure(NotImplementedError("Offline TTS engine integration pending M3"))
    }

    override fun stop() {
        // Stop audio playback
    }
}
