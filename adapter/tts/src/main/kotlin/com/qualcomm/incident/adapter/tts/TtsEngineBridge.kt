package com.qualcomm.incident.adapter.tts

/**
 * Callback for speech synthesis utterance progress.
 */
interface TtsUtteranceCallback {
    fun onStart(utteranceId: String)
    fun onDone(utteranceId: String)
    fun onError(utteranceId: String, errorCode: Int, message: String? = null)
}

/**
 * Bridge interface decoupling Android / native TTS synthesis from the core coroutine logic.
 */
interface TtsEngineBridge {
    /**
     * Returns true if offline voice pack is installed and available without network.
     */
    fun isOfflineAvailable(): Boolean

    /**
     * Enqueues or plays an utterance.
     */
    fun speakUtterance(
        utteranceId: String,
        text: String,
        callback: TtsUtteranceCallback
    ): Boolean

    /**
     * Halts active playback immediately.
     */
    fun stopPlayback()

    /**
     * Releases system TTS resources.
     */
    fun shutdown()
}

/**
 * Exception thrown when offline voice data is absent or network is required.
 */
class OfflineTtsUnavailableException(message: String) : Exception(message)

/**
 * Exception thrown when TTS utterance fails during playback.
 */
class TtsPlaybackException(val errorCode: Int, message: String) : Exception(message)

/**
 * In-memory simulated bridge for testing and development on Laptop B.
 */
class SimulatedTtsBridge(
    var offlineAvailable: Boolean = true,
    var shouldFail: Boolean = false,
    var simulatedErrorCode: Int = -1
) : TtsEngineBridge {

    val spokenHistory = mutableListOf<String>()
    var isStopped = false

    override fun isOfflineAvailable(): Boolean = offlineAvailable

    override fun speakUtterance(
        utteranceId: String,
        text: String,
        callback: TtsUtteranceCallback
    ): Boolean {
        isStopped = false
        spokenHistory.add(text)

        if (shouldFail) {
            callback.onError(utteranceId, simulatedErrorCode, "Simulated playback failure")
            return false
        }

        callback.onStart(utteranceId)
        callback.onDone(utteranceId)
        return true
    }

    override fun stopPlayback() {
        isStopped = true
    }

    override fun shutdown() {
        isStopped = true
    }

    fun clear() {
        spokenHistory.clear()
        isStopped = false
        shouldFail = false
    }
}
