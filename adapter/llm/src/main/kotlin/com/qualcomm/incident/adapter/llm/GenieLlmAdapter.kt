package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.contracts.IncidentExtractor
import com.qualcomm.incident.core.contracts.ValidatedReportPatch
import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.ReportPatch

/**
 * On-device LLM extractor adapter targeting Qualcomm GenieX / Hexagon NPU.
 * Implements [IncidentExtractor] via Qualcomm AI Hub compiled Qwen3.5-2B W4A16 graph (fallback Llama-3.2).
 *
 * Coordinates prompt construction, on-device generation, and strict JSON patch parsing.
 */
class GenieLlmAdapter(
    val engine: GenieEngine = SimulatedGenieEngine(),
    val promptBuilder: LlmPromptBuilder = LlmPromptBuilder(),
    val parser: LlmPatchParser = LlmPatchParser(),
    val config: GenieConfig = GenieConfig(),
    val modelFamily: ModelFamily = ModelFamily.QWEN
) : IncidentExtractor {

    override suspend fun extractPatch(
        transcript: String,
        currentReport: IncidentReport
    ): Result<ValidatedReportPatch> {
        if (transcript.isBlank()) {
            return Result.success(
                ValidatedReportPatch(
                    patch = ReportPatch(),
                    rawJson = "{}",
                    latencyMs = 0L
                )
            )
        }

        val prompt = promptBuilder.buildPrompt(transcript, currentReport, modelFamily)
        val generationResult = engine.generate(prompt, config).getOrElse { error ->
            return Result.failure(error)
        }

        val provenanceTag = when (modelFamily) {
            ModelFamily.QWEN -> "on_device_llm:qwen3.5-2b"
            ModelFamily.LLAMA -> "on_device_llm:llama-3.2-1b"
            ModelFamily.GENERIC -> "on_device_llm:generic"
        }

        return parser.parse(
            rawResponse = generationResult.text,
            latencyMs = generationResult.latencyMs,
            provenanceSource = provenanceTag
        )
    }
}
