package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.contracts.IncidentExtractor
import com.qualcomm.incident.core.contracts.ValidatedReportPatch
import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch

/**
 * Deterministic test fake for [IncidentExtractor].
 * Used for local laptop development and unit testing without Qualcomm GenieX NPU runtime.
 *
 * NOTE: Prohibited as a silent demo fallback on the real board.
 */
class FakeIncidentExtractor(
    var simulatedPatch: ReportPatch = ReportPatch(
        incidentType = OptionalField.Present("Fire / Explosion"),
        severityLevel = OptionalField.Present("High"),
        incidentLocation = OptionalField.Present("Server Room B"),
        dateTime = OptionalField.Present("10:00 AM"),
        incidentDescription = OptionalField.Present("Server rack overheated and caught fire.")
    )
) : IncidentExtractor {

    override suspend fun extractPatch(
        transcript: String,
        currentReport: IncidentReport
    ): Result<ValidatedReportPatch> {
        return Result.success(
            ValidatedReportPatch(
                patch = simulatedPatch,
                rawJson = "{\"fake\": true}",
                latencyMs = 120L
            )
        )
    }
}
