package com.qualcomm.incident.adapter.stt

/**
 * Supported Whisper Automatic Speech Recognition model configurations on Qualcomm Hexagon NPU.
 */
enum class WhisperModelVariant(
    val modelName: String,
    val encoderParams: String,
    val decoderParams: String,
    val approxStorageMb: Int,
    val approxRuntimeRamMb: Int,
    val targetPlatform: String = "SM8650 / Hexagon HTP V75"
) {
    /**
     * Primary candidate for high accuracy in noisy industrial environments.
     */
    WHISPER_SMALL(
        modelName = "openai/whisper-small",
        encoderParams = "102M",
        decoderParams = "139M",
        approxStorageMb = 924,
        approxRuntimeRamMb = 1100
    ),

    /**
     * Recommended low-latency fallback on QIDK.
     */
    WHISPER_BASE(
        modelName = "openai/whisper-base",
        encoderParams = "39M",
        decoderParams = "35M",
        approxStorageMb = 290,
        approxRuntimeRamMb = 400
    ),

    /**
     * Ultra-lightweight fallback for minimal memory footprint.
     */
    WHISPER_TINY(
        modelName = "openai/whisper-tiny",
        encoderParams = "9M",
        decoderParams = "30M",
        approxStorageMb = 150,
        approxRuntimeRamMb = 250
    );

    val sampleRateHz: Int = 16000
    val maxAudioDurationSeconds: Double = 30.0
    val nMels: Int = 80
    val nFft: Int = 400
    val hopLength: Int = 160
    val maxDecodedTokens: Int = 200
}
