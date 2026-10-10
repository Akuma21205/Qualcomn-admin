package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import kotlin.math.PI
import kotlin.math.sin

/**
 * Deterministic audio fixtures for testing speech recognition, VAD, and format handling.
 */
object AudioFixtures {

    const val SAMPLE_RATE_HZ = 16000

    /**
     * Creates a pure silence PCM audio input buffer (all zeros).
     */
    fun createSilenceAudio(durationMs: Int = 1000): AudioInput {
        val sampleCount = (durationMs * SAMPLE_RATE_HZ) / 1000
        val pcm = ByteArray(sampleCount * 2) // 16-bit PCM = 2 bytes per sample
        return AudioInput(pcmBytes = pcm, sampleRateHz = SAMPLE_RATE_HZ)
    }

    /**
     * Creates background noise below the speech detection threshold.
     */
    fun createSubThresholdNoiseAudio(durationMs: Int = 1000, amplitude: Float = 0.005f): AudioInput {
        val sampleCount = (durationMs * SAMPLE_RATE_HZ) / 1000
        val floats = FloatArray(sampleCount) { i ->
            (sin(2.0 * PI * 100.0 * i / SAMPLE_RATE_HZ) * amplitude).toFloat()
        }
        val pcm = AudioPreprocessor.floatToPcm16(floats)
        return AudioInput(pcmBytes = pcm, sampleRateHz = SAMPLE_RATE_HZ)
    }

    /**
     * Creates an active speech-like modulated tone buffer (exceeds VAD threshold).
     */
    fun createActiveSpeechToneAudio(
        durationMs: Int = 1500,
        frequencyHz: Double = 440.0,
        amplitude: Float = 0.4f
    ): AudioInput {
        val sampleCount = (durationMs * SAMPLE_RATE_HZ) / 1000
        val floats = FloatArray(sampleCount) { i ->
            // Modulated tone to simulate speech formant variation
            val carrier = sin(2.0 * PI * frequencyHz * i / SAMPLE_RATE_HZ)
            val modulator = 0.5 + 0.5 * sin(2.0 * PI * 4.0 * i / SAMPLE_RATE_HZ) // 4Hz syllable rhythm
            (carrier * modulator * amplitude).toFloat()
        }
        val pcm = AudioPreprocessor.floatToPcm16(floats)
        return AudioInput(pcmBytes = pcm, sampleRateHz = SAMPLE_RATE_HZ)
    }

    /**
     * Creates audio with leading silence, speech content, and trailing silence.
     */
    fun createSpeechWithPaddedSilence(
        silenceBeforeMs: Int = 500,
        speechMs: Int = 1000,
        silenceAfterMs: Int = 500
    ): AudioInput {
        val totalMs = silenceBeforeMs + speechMs + silenceAfterMs
        val totalSamples = (totalMs * SAMPLE_RATE_HZ) / 1000
        val floats = FloatArray(totalSamples)

        val speechStart = (silenceBeforeMs * SAMPLE_RATE_HZ) / 1000
        val speechEnd = speechStart + (speechMs * SAMPLE_RATE_HZ) / 1000

        for (i in speechStart until speechEnd) {
            floats[i] = (sin(2.0 * PI * 350.0 * (i - speechStart) / SAMPLE_RATE_HZ) * 0.35f).toFloat()
        }

        val pcm = AudioPreprocessor.floatToPcm16(floats)
        return AudioInput(pcmBytes = pcm, sampleRateHz = SAMPLE_RATE_HZ)
    }

    /**
     * Standard test utterances for incident reporting verification.
     */
    val INCIDENT_TEST_SCRIPTS = listOf(
        "There is a severe chemical leak from the reactor tank in Building 4. Ammonia vapor detected.",
        "Small electrical fire on server rack 12 in the main data center. Fire extinguisher deployed.",
        "Forklift collision in Warehouse B. One worker injured with leg fracture. First aid team requested.",
        "Hydraulic press pressure valve blew in manufacturing shop floor. Equipment shut down immediately."
    )
}
