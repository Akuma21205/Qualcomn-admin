package com.qualcomm.incident.feature.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.qualcomm.incident.core.contracts.AudioInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

/**
 * Manages live audio capture via Android AudioRecord on the Qualcomm QIDK board.
 * Captures standard 16kHz 16-bit mono PCM streams suitable for on-device Whisper STT.
 */
class AudioCaptureManager(
    val sampleRateHz: Int = 16000,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    val audioSource: Int = MediaRecorder.AudioSource.VOICE_RECOGNITION
) {

    private val isRecording = AtomicBoolean(false)
    private var activeRecord: AudioRecord? = null

    val bufferSize: Int = maxOf(
        AudioRecord.getMinBufferSize(sampleRateHz, channelConfig, audioFormat),
        sampleRateHz * 2 / 10 // 100ms buffer chunk
    )

    /**
     * Streams live PCM audio chunks via a cold [Flow].
     */
    @SuppressLint("MissingPermission")
    fun startCapture(): Flow<ByteArray> = callbackFlow {
        if (!isRecording.compareAndSet(false, true)) {
            close(IllegalStateException("Audio capture is already in progress"))
            return@callbackFlow
        }

        val record = try {
            AudioRecord(audioSource, sampleRateHz, channelConfig, audioFormat, bufferSize).also {
                activeRecord = it
            }
        } catch (e: Exception) {
            isRecording.set(false)
            close(e)
            return@callbackFlow
        }

        try {
            record.startRecording()
            val chunk = ByteArray(bufferSize)

            while (isRecording.get()) {
                val bytesRead = record.read(chunk, 0, chunk.size)
                if (bytesRead > 0) {
                    val bufferCopy = chunk.copyOf(bytesRead)
                    trySend(bufferCopy)
                } else if (bytesRead < 0) {
                    break
                }
            }
        } finally {
            try {
                record.stop()
                record.release()
            } catch (_: Exception) { }
            activeRecord = null
            isRecording.set(false)
        }

        awaitClose {
            stopCapture()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Captures a complete user utterance until speech silence is detected or max duration expires.
     */
    @SuppressLint("MissingPermission")
    suspend fun captureUtterance(
        maxDurationMs: Long = 15000L,
        silenceTimeoutMs: Long = 1800L,
        silenceThresholdRms: Float = 0.015f
    ): AudioInput = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        val chunk = ByteArray(bufferSize)
        val startTime = System.currentTimeMillis()
        var lastSpeechTime = System.currentTimeMillis()
        var hasDetectedSpeech = false

        val record = try {
            AudioRecord(audioSource, sampleRateHz, channelConfig, audioFormat, bufferSize).also {
                activeRecord = it
            }
        } catch (e: Exception) {
            return@withContext AudioInput(pcmBytes = ByteArray(0), sampleRateHz = sampleRateHz)
        }

        try {
            isRecording.set(true)
            record.startRecording()

            while (isRecording.get()) {
                val now = System.currentTimeMillis()
                if (now - startTime >= maxDurationMs) {
                    break // Max duration reached
                }

                val bytesRead = record.read(chunk, 0, chunk.size)
                if (bytesRead > 0) {
                    outputStream.write(chunk, 0, bytesRead)

                    val rms = computeChunkRms(chunk, bytesRead)
                    if (rms >= silenceThresholdRms) {
                        hasDetectedSpeech = true
                        lastSpeechTime = now
                    } else if (hasDetectedSpeech && (now - lastSpeechTime >= silenceTimeoutMs)) {
                        break // End of speech detected
                    }
                } else if (bytesRead < 0) {
                    break
                }
            }
        } finally {
            try {
                record.stop()
                record.release()
            } catch (_: Exception) { }
            activeRecord = null
            isRecording.set(false)
        }

        AudioInput(
            pcmBytes = outputStream.toByteArray(),
            sampleRateHz = sampleRateHz,
            channelCount = 1,
            bitsPerSample = 16
        )
    }

    /**
     * Halts an active audio recording session.
     */
    fun stopCapture() {
        isRecording.set(false)
        try {
            activeRecord?.stop()
            activeRecord?.release()
        } catch (_: Exception) { }
        activeRecord = null
    }

    fun isCurrentlyRecording(): Boolean = isRecording.get()

    private fun computeChunkRms(chunk: ByteArray, bytesRead: Int): Float {
        val sampleCount = bytesRead / 2
        if (sampleCount == 0) return 0.0f
        var sumSquares = 0.0
        for (i in 0 until sampleCount) {
            val low = chunk[i * 2].toInt() and 0xFF
            val high = chunk[i * 2 + 1].toInt()
            val sample = ((high shl 8) or low) / 32768.0
            sumSquares += (sample * sample)
        }
        return sqrt(sumSquares / sampleCount).toFloat()
    }
}
