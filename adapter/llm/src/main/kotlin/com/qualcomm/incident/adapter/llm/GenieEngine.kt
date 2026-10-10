package com.qualcomm.incident.adapter.llm

/**
 * Configuration for Qualcomm GenieX / QAIRT W4A16 inference on Snapdragon 8 Elite Hexagon NPU.
 */
data class GenieConfig(
    val modelPath: String = "/data/local/tmp/models/qwen/",
    val maxTokens: Int = 512,
    val temperature: Float = 0.0f, // 0.0 for deterministic structured JSON output
    val topP: Float = 0.9f,
    val stopTokens: List<String> = listOf("<|im_end|>", "<|endoftext|>", "<|eot_id|>")
)

/**
 * Result returned from GenieX token generation.
 */
data class GenieGenerationResult(
    val text: String,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val latencyMs: Long
)

/**
 * Interface abstraction for on-device Qualcomm GenieX inference engine.
 */
interface GenieEngine {
    /**
     * Executes generation on the Hexagon NPU.
     */
    suspend fun generate(
        prompt: String,
        config: GenieConfig = GenieConfig()
    ): Result<GenieGenerationResult>
}

/**
 * In-memory test engine for unit testing and offline development on Laptop B.
 */
class SimulatedGenieEngine(
    private val cannedResponses: MutableMap<String, String> = mutableMapOf(),
    var defaultResponse: String = "{}",
    var simulatedLatencyMs: Long = 150L,
    var shouldFail: Boolean = false,
    var failureThrowable: Throwable = IllegalStateException("Simulated GenieX NPU error")
) : GenieEngine {

    val executionHistory = mutableListOf<String>()

    fun registerResponse(promptSnippet: String, responseJson: String) {
        cannedResponses[promptSnippet] = responseJson
    }

    override suspend fun generate(
        prompt: String,
        config: GenieConfig
    ): Result<GenieGenerationResult> {
        executionHistory.add(prompt)

        if (shouldFail) {
            return Result.failure(failureThrowable)
        }

        val matched = cannedResponses.entries.firstOrNull { (snippet, _) ->
            prompt.contains(snippet, ignoreCase = true)
        }?.value ?: defaultResponse

        return Result.success(
            GenieGenerationResult(
                text = matched,
                promptTokens = prompt.length / 4,
                completionTokens = matched.length / 4,
                latencyMs = simulatedLatencyMs
            )
        )
    }

    fun clear() {
        executionHistory.clear()
        cannedResponses.clear()
        shouldFail = false
    }
}

/**
 * JNI declarations and documentation for Qualcomm AI Hub GenieX C/C++ native runtime (`libGenie.so`).
 * Used during M3 board deployment on QIDK.
 */
object GenieNativeBridge {
    const val LIBRARY_NAME = "Genie"

    /**
     * Documentation for native methods exported by Qualcomm GenieX SDK:
     *
     * JNIEXPORT jlong JNICALL
     * Java_com_qualcomm_incident_adapter_llm_GenieNativeBridge_nativeInit(
     *     JNIEnv *env, jobject thiz, jstring modelPath, jstring backend);
     *
     * JNIEXPORT jstring JNICALL
     * Java_com_qualcomm_incident_adapter_llm_GenieNativeBridge_nativeGenerate(
     *     JNIEnv *env, jobject thiz, jlong handle, jstring prompt, jfloat temperature, jint maxTokens);
     *
     * JNIEXPORT void JNICALL
     * Java_com_qualcomm_incident_adapter_llm_GenieNativeBridge_nativeRelease(
     *     JNIEnv *env, jobject thiz, jlong handle);
     */
}
