package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhisperSttAdapterTest {

    @Test
    fun testTranscribe_emptyAudioReturnsEmptyTextWithoutHallucination() = runBlocking {
        val adapter = WhisperSttAdapter()
        val result = adapter.transcribe(AudioInput(pcmBytes = ByteArray(0)))

        assertTrue(result.isSuccess)
        val transcript = result.getOrThrow()
        assertEquals("", transcript.text)
        assertNotNull(transcript.elapsedMs)
    }

    @Test
    fun testTranscribe_pureSilenceReturnsEmptyTextWithoutHallucination() = runBlocking {
        var backendCalled = false
        val adapter = WhisperSttAdapter(
            modelVariant = WhisperModelVariant.WHISPER_SMALL,
            inferenceBackend = {
                backendCalled = true
                "Hallucinated speech"
            }
        )

        val silenceAudio = AudioFixtures.createSilenceAudio(durationMs = 1000)
        val result = adapter.transcribe(silenceAudio)

        assertTrue(result.isSuccess)
        val transcript = result.getOrThrow()
        assertEquals("", transcript.text)
        // Backend must NEVER be called on silence
        assertEquals(false, backendCalled)
    }

    @Test
    fun testTranscribe_activeSpeechTriggersInferenceBackend() = runBlocking {
        val expectedTranscript = "Smoke and flames detected in electrical substation 3."
        var receivedMelFrames = 0

        val adapter = WhisperSttAdapter(
            modelVariant = WhisperModelVariant.WHISPER_SMALL,
            inferenceBackend = { mel ->
                receivedMelFrames = mel[0].size
                expectedTranscript
            }
        )

        val speechAudio = AudioFixtures.createActiveSpeechToneAudio(durationMs = 1200)
        val result = adapter.transcribe(speechAudio)

        assertTrue(result.isSuccess)
        val transcript = result.getOrThrow()
        assertEquals(expectedTranscript, transcript.text)
        assertTrue(receivedMelFrames > 0)
        assertTrue((transcript.elapsedMs ?: 0L) >= 0L)
    }

    @Test
    fun testModelVariants_haveAccurateSpecs() {
        assertEquals("openai/whisper-small", WhisperModelVariant.WHISPER_SMALL.modelName)
        assertEquals(924, WhisperModelVariant.WHISPER_SMALL.approxStorageMb)
        assertEquals(1100, WhisperModelVariant.WHISPER_SMALL.approxRuntimeRamMb)

        assertEquals("openai/whisper-base", WhisperModelVariant.WHISPER_BASE.modelName)
        assertEquals(290, WhisperModelVariant.WHISPER_BASE.approxStorageMb)

        assertEquals("openai/whisper-tiny", WhisperModelVariant.WHISPER_TINY.modelName)
        assertEquals(150, WhisperModelVariant.WHISPER_TINY.approxStorageMb)
    }
}
