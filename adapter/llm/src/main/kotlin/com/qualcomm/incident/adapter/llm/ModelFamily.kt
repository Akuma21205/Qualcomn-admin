package com.qualcomm.incident.adapter.llm

/**
 * Supported LLM model families running on Snapdragon 8 Elite via Qualcomm AI Hub.
 */
enum class ModelFamily {
    /**
     * Qwen3.5-2B using ChatML prompt tokens (<|im_start|>, <|im_end|>).
     * Primary on-device extraction model.
     */
    QWEN,

    /**
     * Llama-3.2-1B / 3B using Llama-3 instruct header tokens.
     * Fallback on-device model.
     */
    LLAMA,

    /**
     * Generic prompt template for standard instruction testing.
     */
    GENERIC
}
