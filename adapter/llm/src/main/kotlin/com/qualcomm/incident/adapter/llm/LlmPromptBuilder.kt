package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.domain.IncidentReport

/**
 * Builds optimized prompts for on-device LLM extraction on Snapdragon 8 Elite.
 * Supports ChatML (Qwen3.5-2B) and Llama-3 format templates.
 */
class LlmPromptBuilder(
    val customSystemPrompt: String? = null
) {

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """You are an On-Device Incident Extraction Assistant running on a Qualcomm Snapdragon 8 Elite NPU.
Your job is to extract industrial incident information from the user's speech transcript into a structured JSON patch.

OUTPUT JSON SCHEMA:
{
  "incident_type": "string or null",
  "severity_level": "string or null",
  "incident_location": "string or null",
  "date_time": "string or null",
  "persons_involved": ["string"] or null,
  "equipment_asset_involved": "string or null",
  "incident_description": "string or null",
  "immediate_actions_taken": "string or null",
  "witness_information": ["string"] or null,
  "recommended_follow_up_action": "string or null"
}

STRICT INSTRUCTIONS:
1. NO HALLUCINATION: Only extract facts explicitly stated in the transcript. NEVER guess or invent names, locations, severities, or dates.
2. OMIT UNMENTIONED FIELDS: If a field was not mentioned in the transcript, OMIT the key from the JSON entirely. Do not emit keys with guessed values.
3. EXPLICIT NEGATIONS: If the user explicitly states none (e.g., "no injuries", "no witnesses"), output an empty array [] or null for that field.
4. CORRECTIONS: If current report state is provided and the user corrects or updates a detail (e.g. "Actually it was in Warehouse B"), output the corrected field value.
5. JSON ONLY: Output ONLY the raw JSON object. Do not include markdown formatting, explanations, or conversational text."""
    }

    val systemPrompt: String
        get() = customSystemPrompt ?: DEFAULT_SYSTEM_PROMPT

    /**
     * Builds the complete model input prompt.
     */
    fun buildPrompt(
        transcript: String,
        currentReport: IncidentReport = IncidentReport(),
        modelFamily: ModelFamily = ModelFamily.QWEN
    ): String {
        val userContent = buildUserContent(transcript, currentReport)
        return when (modelFamily) {
            ModelFamily.QWEN -> formatQwen(systemPrompt, userContent)
            ModelFamily.LLAMA -> formatLlama(systemPrompt, userContent)
            ModelFamily.GENERIC -> formatGeneric(systemPrompt, userContent)
        }
    }

    private fun buildUserContent(transcript: String, currentReport: IncidentReport): String {
        val sb = StringBuilder()
        val populatedFields = getPopulatedFields(currentReport)

        if (populatedFields.isNotEmpty()) {
            sb.append("Current Incident Report State:\n")
            populatedFields.forEach { (field, value) ->
                sb.append("- ").append(field).append(": ").append(value).append("\n")
            }
            sb.append("\nNew User Utterance:\n\"")
            sb.append(transcript.trim())
            sb.append("\"\n\nExtract new or corrected fields as a JSON patch.")
        } else {
            sb.append("User Utterance:\n\"")
            sb.append(transcript.trim())
            sb.append("\"\n\nExtract incident details as a JSON patch.")
        }

        return sb.toString()
    }

    private fun getPopulatedFields(report: IncidentReport): Map<String, String> {
        val map = mutableMapOf<String, String>()
        report.incidentType?.let { map["incident_type"] = it }
        report.severityLevel?.let { map["severity_level"] = it }
        report.incidentLocation?.let { map["incident_location"] = it }
        report.dateTime?.let { map["date_time"] = it }
        report.personsInvolved?.takeIf { it.isNotEmpty() }?.let { map["persons_involved"] = it.joinToString(", ") }
        report.equipmentAssetInvolved?.let { map["equipment_asset_involved"] = it }
        report.incidentDescription?.let { map["incident_description"] = it }
        report.immediateActionsTaken?.let { map["immediate_actions_taken"] = it }
        report.witnessInformation?.takeIf { it.isNotEmpty() }?.let { map["witness_information"] = it.joinToString(", ") }
        report.recommendedFollowUpAction?.let { map["recommended_follow_up_action"] = it }
        return map
    }

    private fun formatQwen(system: String, user: String): String {
        return "<|im_start|>system\n$system<|im_end|>\n<|im_start|>user\n$user<|im_end|>\n<|im_start|>assistant\n"
    }

    private fun formatLlama(system: String, user: String): String {
        return "<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n$system<|eot_id|><|start_header_id|>user<|end_header_id|>\n$user<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n"
    }

    private fun formatGeneric(system: String, user: String): String {
        return "### System:\n$system\n\n### User:\n$user\n\n### Assistant:\n"
    }
}
