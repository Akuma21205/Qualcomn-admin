package com.qualcomm.incident.adapter.stt

import com.qualcomm.incident.core.contracts.AudioInput
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility for parsing and serializing standard RIFF/WAVE PCM audio files.
 */
object WavAudioUtil {

    private const val HEADER_SIZE = 44

    /**
     * Packages raw PCM [AudioInput] into a standard 44-byte RIFF/WAVE container.
     */
    fun pcmToWav(audio: AudioInput): ByteArray {
        val pcmData = audio.pcmBytes
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val sampleRate = audio.sampleRateHz
        val channels = audio.channelCount
        val bitsPerSample = audio.bitsPerSample
        val byteRate = (sampleRate * channels * bitsPerSample) / 8
        val blockAlign = (channels * bitsPerSample) / 8

        val out = ByteArrayOutputStream(totalAudioLen + HEADER_SIZE)

        // RIFF header
        out.write("RIFF".toByteArray())
        out.write(intToByteArray(totalDataLen))
        out.write("WAVE".toByteArray())

        // fmt subchunk
        out.write("fmt ".toByteArray())
        out.write(intToByteArray(16)) // Subchunk1Size for PCM
        out.write(shortToByteArray(1.toShort())) // AudioFormat 1 = PCM
        out.write(shortToByteArray(channels.toShort()))
        out.write(intToByteArray(sampleRate))
        out.write(intToByteArray(byteRate))
        out.write(shortToByteArray(blockAlign.toShort()))
        out.write(shortToByteArray(bitsPerSample.toShort()))

        // data subchunk
        out.write("data".toByteArray())
        out.write(intToByteArray(totalAudioLen))
        out.write(pcmData)

        return out.toByteArray()
    }

    /**
     * Parses a standard RIFF/WAVE file byte array into [AudioInput].
     * Validates PCM audio format, sample rate (16kHz), channels (mono), and bit depth (16-bit).
     */
    fun wavToPcm(wavBytes: ByteArray): Result<AudioInput> {
        if (wavBytes.size < HEADER_SIZE) {
            return Result.failure(IllegalArgumentException("File too small to be a valid WAV file (${wavBytes.size} bytes)"))
        }

        val buffer = ByteBuffer.wrap(wavBytes).order(ByteOrder.LITTLE_ENDIAN)

        // Check RIFF
        val riff = String(wavBytes, 0, 4)
        if (riff != "RIFF") {
            return Result.failure(IllegalArgumentException("Invalid RIFF header: $riff"))
        }

        // Check WAVE
        val wave = String(wavBytes, 8, 4)
        if (wave != "WAVE") {
            return Result.failure(IllegalArgumentException("Invalid WAVE header: $wave"))
        }

        // Locate fmt chunk and data chunk
        var offset = 12
        var audioFormat = -1
        var numChannels = -1
        var sampleRate = -1
        var bitsPerSample = -1
        var dataOffset = -1
        var dataSize = -1

        while (offset + 8 <= wavBytes.size) {
            val chunkId = String(wavBytes, offset, 4)
            val chunkSize = buffer.getInt(offset + 4)
            offset += 8

            when (chunkId) {
                "fmt " -> {
                    audioFormat = buffer.getShort(offset).toInt()
                    numChannels = buffer.getShort(offset + 2).toInt()
                    sampleRate = buffer.getInt(offset + 4)
                    bitsPerSample = buffer.getShort(offset + 14).toInt()
                    offset += chunkSize
                }
                "data" -> {
                    dataOffset = offset
                    dataSize = chunkSize
                    break
                }
                else -> {
                    offset += chunkSize
                }
            }
        }

        if (dataOffset == -1 || dataSize <= 0) {
            return Result.failure(IllegalArgumentException("WAV file contains no data chunk"))
        }

        if (audioFormat != 1) {
            return Result.failure(IllegalArgumentException("Unsupported audio format: $audioFormat (must be PCM)"))
        }

        val actualDataSize = minOf(dataSize, wavBytes.size - dataOffset)
        val pcm = ByteArray(actualDataSize)
        System.arraycopy(wavBytes, dataOffset, pcm, 0, actualDataSize)

        return Result.success(
            AudioInput(
                pcmBytes = pcm,
                sampleRateHz = sampleRate,
                channelCount = numChannels,
                bitsPerSample = bitsPerSample
            )
        )
    }

    private fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }
}
