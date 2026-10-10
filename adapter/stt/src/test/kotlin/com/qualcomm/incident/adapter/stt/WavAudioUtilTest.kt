package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WavAudioUtilTest {

    @Test
    fun testPcmToWavAndBack_roundTripIntegrity() {
        val original = AudioFixtures.createActiveSpeechToneAudio(durationMs = 500)
        val wavBytes = WavAudioUtil.pcmToWav(original)

        // Standard 44-byte WAV header + PCM audio bytes
        assertEquals(44 + original.pcmBytes.size, wavBytes.size)

        // Check RIFF and WAVE magic headers
        assertEquals("RIFF", String(wavBytes, 0, 4))
        assertEquals("WAVE", String(wavBytes, 8, 4))
        assertEquals("fmt ", String(wavBytes, 12, 4))

        // Parse back to AudioInput
        val parsedResult = WavAudioUtil.wavToPcm(wavBytes)
        assertTrue(parsedResult.isSuccess)

        val parsed = parsedResult.getOrThrow()
        assertEquals(original.sampleRateHz, parsed.sampleRateHz)
        assertEquals(original.channelCount, parsed.channelCount)
        assertEquals(original.bitsPerSample, parsed.bitsPerSample)
        assertArrayEquals(original.pcmBytes, parsed.pcmBytes)
    }

    @Test
    fun testWavToPcm_rejectsCorruptedHeader() {
        val badBytes = "NOT_A_VALID_WAV_FILE_HEADER".toByteArray()
        val result = WavAudioUtil.wavToPcm(badBytes)
        assertTrue(result.isFailure)
    }

    @Test
    fun testWavToPcm_rejectsEmptyInput() {
        val result = WavAudioUtil.wavToPcm(ByteArray(0))
        assertTrue(result.isFailure)
    }
}
