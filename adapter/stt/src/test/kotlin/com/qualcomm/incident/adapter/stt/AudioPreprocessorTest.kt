package com.qualcomm.incident.adapter.stt

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPreprocessorTest {

    @Test
    fun testPcm16ToFloat_emptyArray() {
        val floats = AudioPreprocessor.pcm16ToNormalizedFloat(ByteArray(0))
        assertEquals(0, floats.size)
    }

    @Test
    fun testPcm16ToFloatAndBack_roundTripAccuracy() {
        // Test values: 0, 16384 (~0.5), -16384 (~-0.5), 32767 (~1.0), -32768 (-1.0)
        val pcm = byteArrayOf(
            0x00, 0x00,
            0x00, 0x40, // 16384
            0x00, 0xC0.toByte(), // -16384
            0xFF.toByte(), 0x7F, // 32767
            0x00, 0x80.toByte() // -32768
        )

        val floats = AudioPreprocessor.pcm16ToNormalizedFloat(pcm)
        assertEquals(5, floats.size)
        assertEquals(0.0f, floats[0], 0.001f)
        assertEquals(0.5f, floats[1], 0.001f)
        assertEquals(-0.5f, floats[2], 0.001f)
        assertEquals(1.0f, floats[3], 0.001f)
        assertEquals(-1.0f, floats[4], 0.001f)

        // Round trip back to PCM
        val encodedBytes = AudioPreprocessor.floatToPcm16(floats)
        assertEquals(pcm.size, encodedBytes.size)
        // Allow at most 1 unit difference due to rounding
        for (i in pcm.indices) {
            assertEquals(pcm[i].toDouble(), encodedBytes[i].toDouble(), 1.0)
        }
    }

    @Test
    fun testCalculateRms_silenceVsActiveTone() {
        val silence = AudioFixtures.createSilenceAudio(durationMs = 500)
        val silenceFloats = AudioPreprocessor.pcm16ToNormalizedFloat(silence.pcmBytes)
        val silenceRms = AudioPreprocessor.calculateRms(silenceFloats)
        assertEquals(0.0f, silenceRms, 0.0001f)
        assertTrue(AudioPreprocessor.isSilence(silenceFloats))

        val active = AudioFixtures.createActiveSpeechToneAudio(durationMs = 500, amplitude = 0.5f)
        val activeFloats = AudioPreprocessor.pcm16ToNormalizedFloat(active.pcmBytes)
        val activeRms = AudioPreprocessor.calculateRms(activeFloats)
        assertTrue(activeRms > 0.1f)
        assertFalse(AudioPreprocessor.isSilence(activeFloats))
    }

    @Test
    fun testTrimSilence_preservesSpeechSection() {
        val paddedAudio = AudioFixtures.createSpeechWithPaddedSilence(
            silenceBeforeMs = 500,
            speechMs = 1000,
            silenceAfterMs = 500
        )
        val rawFloats = AudioPreprocessor.pcm16ToNormalizedFloat(paddedAudio.pcmBytes)
        val trimmed = AudioPreprocessor.trimSilence(rawFloats)

        // Trimmed size should be significantly smaller than raw (silence stripped)
        assertTrue(trimmed.isNotEmpty())
        assertTrue(trimmed.size < rawFloats.size)
    }

    @Test
    fun testLogMelSpectrogram_computes80Channels() {
        val active = AudioFixtures.createActiveSpeechToneAudio(durationMs = 100)
        val floats = AudioPreprocessor.pcm16ToNormalizedFloat(active.pcmBytes)
        val mel = AudioPreprocessor.computeLogMelSpectrogram(floats, nMels = 80)

        assertEquals(80, mel.size)
        assertTrue(mel[0].isNotEmpty())
    }
}
