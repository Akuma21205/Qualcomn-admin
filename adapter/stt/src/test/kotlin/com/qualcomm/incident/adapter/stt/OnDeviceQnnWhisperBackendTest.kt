package com.qualcomm.incident.adapter.stt

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnDeviceQnnWhisperBackendTest {

    @Test
    fun `runInference uses mock fallback when provided`() = runBlocking {
        val backend = OnDeviceQnnWhisperBackend(
            mockFallbackResponse = "Fire in server room"
        )
        val dummySpectrogram = Array(80) { FloatArray(100) }
        val result = backend.runInference(dummySpectrogram)

        assertEquals("Fire in server room", result)
    }

    @Test
    fun `runInference returns descriptive transcription on host environment`() = runBlocking {
        val backend = OnDeviceQnnWhisperBackend(
            modelDir = "/non/existent/whisper"
        )
        val dummySpectrogram = Array(80) { FloatArray(50) }
        val result = backend.runInference(dummySpectrogram)

        assertTrue(result.contains("Hexagon NPU"))
        assertTrue(result.contains("50 frames"))
    }
}
