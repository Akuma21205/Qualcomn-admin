package com.qualcomm.incident.adapter.llm

import com.qualcomm.incident.core.contracts.ValidatedReportPatch
import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.Provenance
import com.qualcomm.incident.core.domain.ReportPatch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Robust JSON patch parser for on-device LLM output.
 * Extracts presence-aware [ReportPatch] guaranteeing unstated fields remain [OptionalField.NotPresent].
 */
class LlmPatchParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {

    companion object {
        private val THINKING_REGEX = Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE)
        private val CODE_FENCE_REGEX = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""", RegexOption.IGNORE_CASE)
    }

    /**
     * Parses the LLM's raw output string into a [ValidatedReportPatch].
     */
    fun parse(
        rawResponse: String,
        latencyMs: Long? = null,
        provenanceSource: String = "on_device_llm:qwen3.5-2b"
    ): Result<ValidatedReportPatch> = runCatching {
        val sanitized = sanitizeLlmOutput(rawResponse)
        val jsonString = extractJsonString(sanitized)
        val jsonElement = json.parseToJsonElement(jsonString)

        require(jsonElement is JsonObject) {
            "Expected root JSON object in LLM response, got ${jsonElement::class.simpleName}"
        }

        val jsonObject = jsonElement.jsonObject
        val provenanceMap = mutableMapOf<String, Provenance>()
        val timestamp = System.currentTimeMillis()

        fun recordProvenance(fieldKey: String) {
            provenanceMap[fieldKey] = Provenance(
                source = provenanceSource,
                timestamp = timestamp,
                confidence = 0.95f
            )
        }

        val incidentType = parseStringField(jsonObject, "incident_type", "incidentType")
        if (incidentType is OptionalField.Present && !incidentType.value.isNullOrBlank()) {
            recordProvenance("incidentType")
        }

        val severityLevel = parseStringField(jsonObject, "severity_level", "severityLevel")
        if (severityLevel is OptionalField.Present && !severityLevel.value.isNullOrBlank()) {
            recordProvenance("severityLevel")
        }

        val incidentLocation = parseStringField(jsonObject, "incident_location", "incidentLocation")
        if (incidentLocation is OptionalField.Present && !incidentLocation.value.isNullOrBlank()) {
            recordProvenance("incidentLocation")
        }

        val dateTime = parseStringField(jsonObject, "date_time", "dateTime")
        if (dateTime is OptionalField.Present && !dateTime.value.isNullOrBlank()) {
            recordProvenance("dateTime")
        }

        val personsInvolved = parseListField(jsonObject, "persons_involved", "personsInvolved")
        if (personsInvolved is OptionalField.Present && !personsInvolved.value.isNullOrEmpty()) {
            recordProvenance("personsInvolved")
        }

        val equipmentAssetInvolved = parseStringField(jsonObject, "equipment_asset_involved", "equipmentAssetInvolved")
        if (equipmentAssetInvolved is OptionalField.Present && !equipmentAssetInvolved.value.isNullOrBlank()) {
            recordProvenance("equipmentAssetInvolved")
        }

        val incidentDescription = parseStringField(jsonObject, "incident_description", "incidentDescription")
        if (incidentDescription is OptionalField.Present && !incidentDescription.value.isNullOrBlank()) {
            recordProvenance("incidentDescription")
        }

        val immediateActionsTaken = parseStringField(jsonObject, "immediate_actions_taken", "immediateActionsTaken")
        if (immediateActionsTaken is OptionalField.Present && !immediateActionsTaken.value.isNullOrBlank()) {
            recordProvenance("immediateActionsTaken")
        }

        val witnessInformation = parseListField(jsonObject, "witness_information", "witnessInformation")
        if (witnessInformation is OptionalField.Present && !witnessInformation.value.isNullOrEmpty()) {
            recordProvenance("witnessInformation")
        }

        val recommendedFollowUpAction = parseStringField(jsonObject, "recommended_follow_up_action", "recommendedFollowUpAction")
        if (recommendedFollowUpAction is OptionalField.Present && !recommendedFollowUpAction.value.isNullOrBlank()) {
            recordProvenance("recommendedFollowUpAction")
        }

        val patch = ReportPatch(
            incidentType = incidentType,
            severityLevel = severityLevel,
            incidentLocation = incidentLocation,
            dateTime = dateTime,
            personsInvolved = personsInvolved,
            equipmentAssetInvolved = equipmentAssetInvolved,
            incidentDescription = incidentDescription,
            immediateActionsTaken = immediateActionsTaken,
            witnessInformation = witnessInformation,
            recommendedFollowUpAction = recommendedFollowUpAction,
            provenanceMap = provenanceMap
        )

        ValidatedReportPatch(
            patch = patch,
            rawJson = jsonString,
            latencyMs = latencyMs
        )
    }

    private fun sanitizeLlmOutput(raw: String): String {
        return raw.replace(THINKING_REGEX, "").trim()
    }

    private fun extractJsonString(text: String): String {
        val fenceMatch = CODE_FENCE_REGEX.find(text)
        if (fenceMatch != null) {
            val content = fenceMatch.groupValues[1].trim()
            if (content.startsWith("{") && content.endsWith("}")) {
                return content
            }
        }

        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1).trim()
        }

        throw IllegalArgumentException("No valid JSON object delimiters '{ ... }' found in LLM output: $text")
    }

    private fun parseStringField(
        obj: JsonObject,
        snakeKey: String,
        camelKey: String
    ): OptionalField<String> {
        val element = findElement(obj, snakeKey, camelKey) ?: return OptionalField.NotPresent
        return when (element) {
            is JsonNull -> OptionalField.Present(null)
            is JsonPrimitive -> {
                val str = element.contentOrNull?.trim()
                if (str == null || str.equals("null", ignoreCase = true) || str.equals("none", ignoreCase = true)) {
                    OptionalField.Present(null)
                } else {
                    OptionalField.Present(str)
                }
            }
            else -> OptionalField.Present(element.toString().trim())
        }
    }

    private fun parseListField(
        obj: JsonObject,
        snakeKey: String,
        camelKey: String
    ): OptionalField<List<String>> {
        val element = findElement(obj, snakeKey, camelKey) ?: return OptionalField.NotPresent
        return when (element) {
            is JsonNull -> OptionalField.Present(null)
            is JsonArray -> {
                val items = element.mapNotNull { it.jsonPrimitive.contentOrNull?.trim() }
                    .filter { it.isNotBlank() && !it.equals("none", ignoreCase = true) }
                OptionalField.Present(items)
            }
            is JsonPrimitive -> {
                val str = element.contentOrNull?.trim()
                if (str == null || str.equals("null", ignoreCase = true) || str.equals("none", ignoreCase = true)) {
                    OptionalField.Present(emptyList())
                } else {
                    val split = str.split(",", ";").map { it.trim() }.filter { it.isNotBlank() }
                    OptionalField.Present(split)
                }
            }
            else -> OptionalField.Present(null)
        }
    }

    private fun findElement(obj: JsonObject, snakeKey: String, camelKey: String): JsonElement? {
        return obj[snakeKey] ?: obj[camelKey]
    }
}
