package com.qualcomm.incident.adapter.decision

import com.qualcomm.incident.core.contracts.DecisionContext
import com.qualcomm.incident.core.domain.IncidentCategory
import com.qualcomm.incident.core.domain.IncidentReport
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRuleDecisionEngineTest {

    private val engine = LocalRuleDecisionEngine()

    @Test
    fun testEvaluate_classifiesNearMiss() = runBlocking {
        val context = DecisionContext(
            transcript = "Forklift narrowly avoided hitting a pedestrian worker in aisle 4, close call.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.NEAR_MISS.displayName, result.incidentType)
        assertEquals(1, result.severityScore)
        assertFalse(result.isUrgent)
        assertTrue(result.routingDepartment.contains("Prevention"))
    }

    @Test
    fun testEvaluate_classifiesFireCategory() = runBlocking {
        val context = DecisionContext(
            transcript = "Visible smoke and flames from battery testing bench.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.FIRE.displayName, result.incidentType)
        assertTrue(result.routingDepartment.contains("Fire Safety"))
    }

    @Test
    fun testEvaluate_classifiesUrgentChemicalSpill() = runBlocking {
        val context = DecisionContext(
            transcript = "Critical emergency! Evacuate now! Toxic chemical acid leak in reactor room.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.CHEMICAL.displayName, result.incidentType)
        assertTrue(result.isUrgent)
        assertEquals(5, result.severityScore)
        assertTrue(result.routingDepartment.contains("Hazmat"))
    }

    @Test
    fun testEvaluate_classifiesElectricalIncident() = runBlocking {
        val context = DecisionContext(
            transcript = "High voltage circuit breaker arced and threw sparks.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.ELECTRICAL.displayName, result.incidentType)
        assertTrue(result.routingDepartment.contains("Electrical"))
    }

    @Test
    fun testEvaluate_classifiesMedicalInjury() = runBlocking {
        val context = DecisionContext(
            transcript = "Worker suffered a fall injury with bone fracture, first aid required.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.MEDICAL.displayName, result.incidentType)
        assertTrue(result.routingDepartment.contains("Medical") || result.routingDepartment.contains("First Aid"))
    }

    @Test
    fun testEvaluate_classifiesStructuralIncident() = runBlocking {
        val context = DecisionContext(
            transcript = "Scaffold cracked and ceiling beam collapse observed in hall.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.STRUCTURAL.displayName, result.incidentType)
        assertTrue(result.routingDepartment.contains("Structural"))
    }

    @Test
    fun testEvaluate_classifiesMechanicalFailure() = runBlocking {
        val context = DecisionContext(
            transcript = "Conveyor belt gear is broken and motor seized on line 3.",
            currentReport = IncidentReport()
        )
        val result = engine.evaluate(context).getOrThrow()
        assertEquals(IncidentCategory.MECHANICAL.displayName, result.incidentType)
        assertTrue(result.routingDepartment.contains("Maintenance"))
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
