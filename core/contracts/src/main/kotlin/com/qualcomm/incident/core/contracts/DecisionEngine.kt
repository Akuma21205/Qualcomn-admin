package com.qualcomm.incident.core.contracts

import com.qualcomm.incident.core.domain.IncidentReport

/**
 * Context input for decision, categorization, or triage evaluation.
 */
data class DecisionContext(
    val transcript: String,
    val currentReport: IncidentReport
)

/**
 * Structured result containing category, severity triage, and escalation flags.
 */
data class DecisionResult(
    val incidentType: String? = null,
    val severityScore: Int? = null, // e.g. 1 to 5 scale
    val isUrgent: Boolean = false,
    val routingDepartment: String? = null,
    val engineName: String // "LocalRules", "OnDeviceLlama", or "JevSystemOne"
)

/**
 * Interface for evaluating high-level categorization, urgency, and triage decisions.
 * Production implementation runs on-device; optional Jev adapter runs for laptop evaluation.
 */
interface DecisionEngine {
    suspend fun evaluate(context: DecisionContext): Result<DecisionResult>
}
