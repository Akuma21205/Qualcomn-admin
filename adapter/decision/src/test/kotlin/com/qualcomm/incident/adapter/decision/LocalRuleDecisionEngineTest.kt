package com.qualcomm.incident.adapter.decision

import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.domain.IncidentCategory
import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.SeverityLevel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRuleDecisionEngineTest {

    private val engine = LocalRuleDecisionEngine()

    @Test
    fun testEvaluate_classifiesFireCategory() = runBlocking {
        val context = DecisionContext(
            transcript = "Visible smoke and fire from battery testing bench.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.FIRE.displayName, result.incidentType)
        assertFalse(result.isUrgent)
    }

    @Test
    fun testEvaluate_classifiesUrgentChemicalSpill() = runBlocking {
        val context = DecisionContext(
            transcript = "Critical emergency! Toxic chemical acid leak in reactor room.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.CHEMICAL.displayName, result.incidentType)
        assertTrue(result.isUrgent)
        assertEquals(5, result.severityScore)
    }

    @Test
    fun testEvaluate_classifiesElectricalIncident() = runBlocking {
        val context = DecisionContext(
            transcript = "High voltage circuit breaker arced and threw sparks.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.ELECTRICAL.displayName, result.incidentType)
    }

    @Test
    fun testEvaluate_classifiesMedicalInjury() = runBlocking {
        val context = DecisionContext(
            transcript = "Worker suffered a fall injury and needs medical attention.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.MEDICAL.displayName, result.incidentType)
    }

    @Test
    fun testEvaluate_classifiesMechanicalFailure() = runBlocking {
        val context = DecisionContext(
            transcript = "Conveyor belt gear is broken and motor seized.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.MECHANICAL.displayName, result.incidentType)
    }

    @Test
    fun testEvaluate_defaultsToOtherForUnspecified() = runBlocking {
        val context = DecisionContext(
            transcript = "Something strange happened near loading dock 2.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.OTHER.displayName, result.incidentType)
    }
}
