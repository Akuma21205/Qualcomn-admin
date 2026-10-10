package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.contracts.IncidentExtractor
import com.qualcomm.incident.core.contracts.ValidatedReportPatch
import com.qualcomm.incident.core.domain.IncidentReport

/**
 * On-device LLM extractor adapter targeting Qualcomm GenieX / Hexagon NPU.
 * Implements [IncidentExtractor] via Qualcomm AI Hub compiled Qwen3.5-2B W4A16 graph (fallback Llama-3.2).
 */
class GenieLlmAdapter : IncidentExtractor {

    override suspend fun extractPatch(
        transcript: String,
        currentReport: IncidentReport
    ): Result<ValidatedReportPatch> {
        // Concrete on-device GenieX inference on QIDK
        // To be integrated in M3 once vendor runtime sample is verified
        return Result.failure(NotImplementedError("GenieX LLM NPU runtime integration pending M3"))
    }
}
