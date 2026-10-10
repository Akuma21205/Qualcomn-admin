package com.qualcomm.incident.adapter.decision

import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.contracts.DecisionEngine
import com.qualcomm.incident.core.contracts.DecisionResult
import com.qualcomm.incident.core.domain.IncidentCategory
import com.qualcomm.incident.core.domain.SeverityLevel

/**
 * Production on-device decision engine using comprehensive deterministic triage heuristics.
 * Evaluates incident narrative across all standard taxonomies, calculates rubric severity scores (1-5),
 * flags critical urgencies, and assigns dedicated emergency routing departments offline.
 */
class LocalRuleDecisionEngine : DecisionEngine {

    override suspend fun evaluate(context: DecisionContext): Result<DecisionResult> {
        val transcriptLower = context.transcript.lowercase()

        // 1. Evaluate Incident Category
        val category = classifyCategory(transcriptLower)

        // 2. Evaluate Severity Rubric and Urgency
        val (severityScore, isUrgent) = evaluateSeverityRubric(transcriptLower, category)

        // 3. Assign Emergency Response Department
        val department = resolveRoutingDepartment(category, isUrgent)

        return Result.success(
            DecisionResult(
                incidentType = category.displayName,
                severityScore = severityScore,
                isUrgent = isUrgent,
                routingDepartment = department,
                engineName = "LocalRuleDecisionEngine"
            )
        )
    }

    private fun classifyCategory(text: String): IncidentCategory {
        return when {
            // Near Miss indicators
            containsAny(text, "near miss", "close call", "almost hit", "narrowly avoided", "near hit", "almost injured", "avoided accident") ->
                IncidentCategory.NEAR_MISS

            // Fire / Explosion indicators
            containsAny(text, "fire", "smoke", "flame", "burn", "explosion", "blaze", "ignite", "detonation", "combustion") ->
                IncidentCategory.FIRE

            // Chemical indicators
            containsAny(text, "chemical", "acid", "toxic", "leak", "spill", "fume", "vapor", "hazmat", "corrosive", "chlorine", "ammonia", "gas leak") ->
                IncidentCategory.CHEMICAL

            // Electrical indicators
            containsAny(text, "electric", "voltage", "wire", "breaker", "arc", "spark", "transformer", "short circuit", "electrocution", "blackout") ->
                IncidentCategory.ELECTRICAL

            // Medical indicators
            containsAny(text, "hurt", "injury", "injured", "medical", "bleeding", "fracture", "unconscious", "first aid", "ambulance", "paramedic", "hospital", "cut") ->
                IncidentCategory.MEDICAL

            // Structural / Facilities indicators
            containsAny(text, "collapse", "structural", "roof", "scaffold", "cracked wall", "ceiling", "beam", "foundation", "sinkhole") ->
                IncidentCategory.STRUCTURAL

            // Mechanical indicators
            containsAny(text, "broken", "motor", "gear", "conveyor", "hydraulic", "crane", "forklift", "jammed", "machine failure", "press", "bearing", "pump") ->
                IncidentCategory.MECHANICAL

            else -> IncidentCategory.OTHER
        }
    }

    private fun evaluateSeverityRubric(text: String, category: IncidentCategory): Pair<Int, Boolean> {
        // Critical Level 5 indicators
        val criticalTriggers = listOf(
            "evacuate", "evacuation", "explosion", "fatal", "fatality", "unconscious",
            "toxic cloud", "severe acid", "trapped", "life threatening", "catastrophic",
            "critical emergency", "mayday"
        )
        if (containsAny(text, *criticalTriggers.toTypedArray())) {
            return Pair(5, true)
        }

        // High Level 4 indicators
        val highTriggers = listOf(
            "fire spreading", "large spill", "fracture", "heavy smoke", "active leak",
            "arcing", "high voltage", "structural crack", "severe", "major", "hospitalized"
        )
        if (containsAny(text, *highTriggers.toTypedArray())) {
            return Pair(4, true)
        }

        // Moderate Level 3 indicators
        val moderateTriggers = listOf(
            "smoke", "sparks", "contained leak", "first aid required", "motor seized",
            "power cut", "urgent", "emergency", "shut down"
        )
        if (containsAny(text, *moderateTriggers.toTypedArray())) {
            return Pair(3, false)
        }

        // Low Level 1 indicators
        val lowTriggers = listOf(
            "minor", "small drip", "inspection", "false alarm", "scratch", "trivial", "near miss", "no damage"
        )
        if (containsAny(text, *lowTriggers.toTypedArray())) {
            return Pair(1, false)
        }

        // Default Level 2 (Medium) for general incidents
        return Pair(2, false)
    }

    private fun resolveRoutingDepartment(category: IncidentCategory, isUrgent: Boolean): String {
        val prefix = if (isUrgent) "[EMERGENCY DISPATCH] " else ""
        val dept = when (category) {
            IncidentCategory.CHEMICAL -> "Hazmat Emergency Response Team"
            IncidentCategory.FIRE -> "Fire Safety & Suppression Unit"
            IncidentCategory.ELECTRICAL -> "High-Voltage Electrical Safety"
            IncidentCategory.MEDICAL -> "Occupational Health & First Aid"
            IncidentCategory.STRUCTURAL -> "Facilities & Structural Safety Division"
            IncidentCategory.MECHANICAL -> "Plant Maintenance & Machinery Safety"
            IncidentCategory.NEAR_MISS -> "EHS Incident Prevention & Safety Investigation"
            IncidentCategory.OTHER -> "General Safety & Operations Desk"
        }
        return prefix + dept
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }
}
