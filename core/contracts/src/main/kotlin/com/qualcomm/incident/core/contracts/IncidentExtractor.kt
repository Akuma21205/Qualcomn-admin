package com.qualcomm.incident.core.contracts

import com.qualcomm.incident.core.domain.IncidentReport

/**
 * Interface for on-device Large Language Model extraction (e.g. Llama-3.2 via GenieX).
 */
interface IncidentExtractor {
    /**
     * Extracts an incremental report patch from a speech transcript grounded in current report state.
     */
    suspend fun extractPatch(
        transcript: String,
        currentReport: IncidentReport
    ): Result<ValidatedReportPatch>
}
