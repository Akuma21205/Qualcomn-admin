package com.qualcomm.incident.core.contracts

import com.qualcomm.incident.core.domain.ReportPatch

/**
 * Validated patch containing structured updates extracted by the LLM.
 */
data class ValidatedReportPatch(
    val patch: ReportPatch,
    val rawJson: String? = null,
    val latencyMs: Long? = null
)
