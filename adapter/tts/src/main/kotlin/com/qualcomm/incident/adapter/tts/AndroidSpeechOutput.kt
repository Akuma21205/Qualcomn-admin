package com.qualcomm.incident.adapter.tts

import com.qualcomm.incident.core.contracts.SpeechOutput
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

/**
 * On-device offline Text-to-Speech adapter.
 * Uses Android TextToSpeech engine with preloaded offline voice assets or native fallback.
 *
 * Guarantees:
 * - 100% offline verification prior to utterance synthesis.
 * - Non-blocking coroutine completion via [suspendCancellableCoroutine].
 * - Immediate cancellation / stop support.
 * - Graceful failure fallback so visual UI prompts are never blocked.
 */
class AndroidSpeechOutput(
    val bridge: TtsEngineBridge
) : SpeechOutput {

    constructor(context: Any? = null) : this(
        bridge = if (context != null) AndroidSystemTtsBridge(context) else SimulatedTtsBridge()
    )

    override suspend fun speak(text: String): Result<Unit> {
        if (text.isBlank()) {
            return Result.success(Unit)
        }

        if (!bridge.isOfflineAvailable()) {
            return Result.failure(
                OfflineTtsUnavailableException(
                    "Offline TTS voice pack unavailable. Ensure voice data is installed and networkConnectionRequired == false."
                )
            )
        }

        val utteranceId = UUID.randomUUID().toString()

        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                bridge.stopPlayback()
            }

            val callback = object : TtsUtteranceCallback {
                override fun onStart(utteranceId: String) {
                    // Speech playback started
                }

                override fun onDone(utteranceId: String) {
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                    }
                }

                override fun onError(utteranceId: String, errorCode: Int, message: String?) {
                    if (continuation.isActive) {
                        val errMsg = message ?: "TTS playback error with code $errorCode"
                        continuation.resume(Result.failure(TtsPlaybackException(errorCode, errMsg)))
                    }
                }
            }

            val dispatched = bridge.speakUtterance(utteranceId, text, callback)
            if (!dispatched && continuation.isActive) {
                continuation.resume(
                    Result.failure(TtsPlaybackException(-1, "Failed to dispatch speech utterance"))
                )
            }
        }
    }

    override fun stop() {
        bridge.stopPlayback()
    }

    fun shutdown() {
        bridge.shutdown()
    }
}
