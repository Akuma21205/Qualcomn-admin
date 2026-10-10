package com.qualcomm.incident.adapter.stt

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Audio preprocessing and feature extraction pipeline for on-device STT (Whisper).
 * Formats 16kHz 16-bit mono PCM into normalized floating-point samples and log-Mel spectrograms.
 */
object AudioPreprocessor {

    const val SAMPLE_RATE_HZ = 16000
    const val BYTES_PER_SAMPLE = 2 // 16-bit PCM
    const val DEFAULT_SILENCE_THRESHOLD_RMS = 0.012f
    const val N_MELS = 80
    const val N_FFT = 400
    const val HOP_LENGTH = 160
    const val MAX_AUDIO_DURATION_SECONDS = 30.0

    /**
     * Converts raw 16-bit little-endian PCM bytes into normalized float samples in [-1.0, 1.0].
     */
    fun pcm16ToNormalizedFloat(pcmBytes: ByteArray): FloatArray {
        if (pcmBytes.isEmpty()) return FloatArray(0)
        val sampleCount = pcmBytes.size / BYTES_PER_SAMPLE
        val floats = FloatArray(sampleCount)
        for (i in 0 until sampleCount) {
            val byteIndex = i * 2
            val low = pcmBytes[byteIndex].toInt() and 0xFF
            val high = pcmBytes[byteIndex + 1].toInt()
            val sample = (high shl 8) or low
            floats[i] = sample / 32768.0f
        }
        return floats
    }

    /**
     * Encodes normalized float samples in [-1.0, 1.0] back to 16-bit little-endian PCM bytes.
     */
    fun floatToPcm16(samples: FloatArray): ByteArray {
        val bytes = ByteArray(samples.size * BYTES_PER_SAMPLE)
        for (i in samples.indices) {
            val clamped = samples[i].coerceIn(-1.0f, 1.0f)
            val intVal = (clamped * 32767.0f).toInt()
            val byteIndex = i * 2
            bytes[byteIndex] = (intVal and 0xFF).toByte()
            bytes[byteIndex + 1] = ((intVal shr 8) and 0xFF).toByte()
        }
        return bytes
    }

    /**
     * Computes the Root Mean Square (RMS) energy level of the audio signal.
     */
    fun calculateRms(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0.0f
        var sumSquares = 0.0
        for (sample in samples) {
            sumSquares += (sample * sample)
        }
        return sqrt(sumSquares / samples.size).toFloat()
    }

    /**
     * Returns true if the audio energy is below the silence threshold (e.g. background hiss / empty recording).
     */
    fun isSilence(samples: FloatArray, thresholdRms: Float = DEFAULT_SILENCE_THRESHOLD_RMS): Boolean {
        if (samples.isEmpty()) return true
        return calculateRms(samples) < thresholdRms
    }

    /**
     * Trims leading and trailing silent sections from audio samples while retaining a small padding window.
     * Prevents Whisper from generating hallucinated text over prolonged silence.
     */
    fun trimSilence(
        samples: FloatArray,
        thresholdRms: Float = DEFAULT_SILENCE_THRESHOLD_RMS,
        windowSize: Int = 320, // 20ms at 16kHz
        padMs: Int = 100,
        sampleRate: Int = SAMPLE_RATE_HZ
    ): FloatArray {
        if (samples.isEmpty() || isSilence(samples, thresholdRms)) return FloatArray(0)

        val padSamples = (padMs * sampleRate) / 1000
        var firstActiveIndex = 0
        var lastActiveIndex = samples.size - 1

        // Find start of speech
        for (i in 0 until samples.size step windowSize) {
            val end = minOf(i + windowSize, samples.size)
            val window = samples.copyOfRange(i, end)
            if (calculateRms(window) >= thresholdRms) {
                firstActiveIndex = maxOf(0, i - padSamples)
                break
            }
        }

        // Find end of speech
        for (i in (samples.size - windowSize) downTo 0 step windowSize) {
            val end = minOf(i + windowSize, samples.size)
            val window = samples.copyOfRange(i, end)
            if (calculateRms(window) >= thresholdRms) {
                lastActiveIndex = minOf(samples.size, end + padSamples)
                break
            }
        }

        if (firstActiveIndex >= lastActiveIndex) return FloatArray(0)
        return samples.copyOfRange(firstActiveIndex, lastActiveIndex)
    }

    /**
     * Computes an 80-channel log-Mel spectrogram matching Whisper's feature extractor.
     * Window size: 400 (25ms), Hop length: 160 (10ms), Hann window, 80 Mel filters.
     */
    fun computeLogMelSpectrogram(
        samples: FloatArray,
        nMels: Int = N_MELS,
        nFft: Int = N_FFT,
        hopLength: Int = HOP_LENGTH
    ): Array<FloatArray> {
        val numFrames = max(1, (samples.size - nFft) / hopLength + 1)
        val melSpectrogram = Array(nMels) { FloatArray(numFrames) }

        // Hann window function
        val hannWindow = FloatArray(nFft) { i ->
            (0.5 * (1.0 - cos(2.0 * PI * i / (nFft - 1)))).toFloat()
        }

        // Simple mel filterbank center frequencies approximation
        val melMin = 0.0
        val melMax = hzToMel(SAMPLE_RATE_HZ / 2.0)
        val melPoints = DoubleArray(nMels + 2) { i ->
            melMin + (melMax - melMin) * i / (nMels + 1)
        }
        val hzPoints = DoubleArray(melPoints.size) { i -> melToHz(melPoints[i]) }
        val binPoints = IntArray(hzPoints.size) { i ->
            ((nFft + 1) * hzPoints[i] / SAMPLE_RATE_HZ).toInt().coerceIn(0, nFft / 2)
        }

        for (frame in 0 until numFrames) {
            val offset = frame * hopLength
            val windowed = FloatArray(nFft)
            for (j in 0 until nFft) {
                val sampleIdx = offset + j
                if (sampleIdx < samples.size) {
                    windowed[j] = samples[sampleIdx] * hannWindow[j]
                }
            }

            // Power spectrum via DFT (for STT feature pipeline)
            val halfFft = nFft / 2 + 1
            val powerSpectrum = FloatArray(halfFft)
            for (k in 0 until halfFft) {
                var real = 0.0
                var imag = 0.0
                val angleFactor = 2.0 * PI * k / nFft
                for (n in 0 until nFft) {
                    val angle = angleFactor * n
                    real += windowed[n] * cos(angle)
                    imag -= windowed[n] * sin(angle)
                }
                powerSpectrum[k] = ((real * real + imag * imag) / nFft).toFloat()
            }

            // Apply triangular mel filterbank
            for (m in 0 until nMels) {
                var melEnergy = 0.0f
                val startBin = binPoints[m]
                val centerBin = binPoints[m + 1]
                val endBin = binPoints[m + 2]

                for (k in startBin until centerBin) {
                    if (centerBin > startBin) {
                        val weight = (k - startBin).toFloat() / (centerBin - startBin)
                        melEnergy += weight * powerSpectrum[k]
                    }
                }
                for (k in centerBin until endBin) {
                    if (endBin > centerBin) {
                        val weight = (endBin - k).toFloat() / (endBin - centerBin)
                        melEnergy += weight * powerSpectrum[k]
                    }
                }

                // Log-Mel scaling
                val logMel = ln(maxOf(melEnergy, 1e-10f))
                melSpectrogram[m][frame] = logMel
            }
        }

        return melSpectrogram
    }

    private fun hzToMel(hz: Double): Double = 2595.0 * ln(1.0 + hz / 700.0)
    private fun melToHz(mel: Double): Double = 700.0 * (Math.exp(mel / 2595.0) - 1.0)
}
