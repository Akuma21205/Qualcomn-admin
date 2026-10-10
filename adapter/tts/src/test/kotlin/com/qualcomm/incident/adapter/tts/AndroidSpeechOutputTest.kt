package com.qualcomm.incident.adapter.tts

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidSpeechOutputTest {

    private val bridge = SimulatedTtsBridge()
    private val speechOutput = AndroidSpeechOutput(bridge = bridge)

    @Test
    fun `speak synthesizes utterance successfully`() = runBlocking {
        val result = speechOutput.speak("Please specify the incident location.")

        assertTrue(result.isSuccess)
        assertEquals(1, bridge.spokenHistory.size)
        assertEquals("Please specify the incident location.", bridge.spokenHistory[0])
    }

    @Test
    fun `speak returns success immediately for blank text`() = runBlocking {
        val result = speechOutput.speak("   ")

        assertTrue(result.isSuccess)
        assertEquals(0, bridge.spokenHistory.size)
    }

    @Test
    fun `speak fails gracefully when offline voice data is missing`() = runBlocking {
        bridge.offlineAvailable = false

        val result = speechOutput.speak("Please state your name.")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is OfflineTtsUnavailableException)
        assertEquals(0, bridge.spokenHistory.size)
    }

    @Test
    fun `speak returns failure when utterance encounters playback error`() = runBlocking {
        bridge.shouldFail = true
        bridge.simulatedErrorCode = -2

        val result = speechOutput.speak("Warning: High temperature detected.")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TtsPlaybackException)
        val ex = result.exceptionOrNull() as TtsPlaybackException
        assertEquals(-2, ex.errorCode)
    }

    @Test
    fun `stop halts playback on underlying bridge`() {
        speechOutput.stop()
        assertTrue(bridge.isStopped)
    }
}
