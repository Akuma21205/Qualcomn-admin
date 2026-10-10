package com.qualcomm.incident.core.contracts

/**
 * Encapsulates raw PCM audio recorded from the microphone.
 */
data class AudioInput(
    val pcmBytes: ByteArray,
    val sampleRateHz: Int = 16000,
    val channelCount: Int = 1,
    val bitsPerSample: Int = 16
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioInput
        return pcmBytes.contentEquals(other.pcmBytes) &&
                sampleRateHz == other.sampleRateHz &&
                channelCount == other.channelCount &&
                bitsPerSample == other.bitsPerSample
    }

    override fun hashCode(): Int {
        var result = pcmBytes.contentHashCode()
        result = 31 * result + sampleRateHz
        result = 31 * result + channelCount
        result = 31 * result + bitsPerSample
        return result
    }
}
