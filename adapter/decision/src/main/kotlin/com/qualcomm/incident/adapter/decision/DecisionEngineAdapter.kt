package com.qualcomm.incident.adapter.decision

import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.contracts.DecisionEngine
import com.qualcomm.incident.core.contracts.DecisionResult
import com.qualcomm.incident.core.domain.IncidentCategory
import com.qualcomm.incident.core.domain.SeverityLevel

/**
 * Production on-device decision engine using deterministic classification heuristics.
 */
class LocalRuleDecisionEngine : DecisionEngine {

    override suspend fun evaluate(context: DecisionContext): Result<DecisionResult> {
        val transcriptLower = context.transcript.lowercase()

        val inferredCategory = when {
            transcriptLower.contains("fire") || transcriptLower.contains("smoke") || transcriptLower.contains("burn") ->
                IncidentCategory.FIRE.displayName
            transcriptLower.contains("electric") || transcriptLower.contains("wire") || transcriptLower.contains("breaker") ->
                IncidentCategory.ELECTRICAL.displayName
            transcriptLower.contains("chemical") || transcriptLower.contains("leak") || transcriptLower.contains("spill") ->
                IncidentCategory.CHEMICAL.displayName
            transcriptLower.contains("hurt") || transcriptLower.contains("injury") || transcriptLower.contains("medical") ->
                IncidentCategory.MEDICAL.displayName
            transcriptLower.contains("broken") || transcriptLower.contains("motor") || transcriptLower.contains("gear") ->
                IncidentCategory.MECHANICAL.displayName
            else -> IncidentCategory.OTHER.displayName
        }

        val isUrgent = transcriptLower.contains("urgent") ||
                transcriptLower.contains("emergency") ||
                transcriptLower.contains("critical") ||
                transcriptLower.contains("explosion")

        val severity = if (isUrgent) SeverityLevel.CRITICAL.displayName else SeverityLevel.MEDIUM.displayName

        return Result.success(
            DecisionResult(
                incidentType = inferredCategory,
                severityScore = if (isUrgent) 5 else 2,
                isUrgent = isUrgent,
                routingDepartment = "Safety & Emergency Response",
                engineName = "LocalRuleDecisionEngine"
            )
        )
    }
}
