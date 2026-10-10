package com.qualcomm.incident.adapter.decision

import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.contracts.DecisionEngine
import com.qualcomm.incident.core.contracts.DecisionResult

/**
 * Deterministic test fake for [DecisionEngine].
 *
 * NOTE: Prohibited as a silent demo fallback on the real board.
 */
class FakeDecisionEngine(
    var simulatedResult: DecisionResult = DecisionResult(
        incidentType = "Fire / Explosion",
        severityScore = 4,
        isUrgent = true,
        routingDepartment = "Emergency Fire Response",
        engineName = "FakeDecisionEngine"
    )
) : DecisionEngine {

    override suspend fun evaluate(context: DecisionContext): Result<DecisionResult> {
        return Result.success(simulatedResult)
    }
}
