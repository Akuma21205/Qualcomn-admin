package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSttEngineTest {

    @Test
    fun testFakeSttEngine_returnsSimulatedTranscript() = runBlocking {
        val customTranscript = "Overheating transformer in bay 4, power cut initiated."
        val fake = FakeSttEngine(simulatedTranscript = customTranscript)

        val activeAudio = AudioFixtures.createActiveSpeechToneAudio(durationMs = 500)
        val result = fake.transcribe(activeAudio)

        assertTrue(result.isSuccess)
        val transcript = result.getOrThrow()
        assertEquals(customTranscript, transcript.text)
        assertEquals(45L, transcript.elapsedMs)
        assertTrue(transcript.isFinal)
    }

    @Test
    fun testFakeSttEngine_returnsEmptyOnEmptyAudio() = runBlocking {
        val fake = FakeSttEngine()
        val result = fake.transcribe(AudioInput(pcmBytes = ByteArray(0)))

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrThrow().text)
    }
}
