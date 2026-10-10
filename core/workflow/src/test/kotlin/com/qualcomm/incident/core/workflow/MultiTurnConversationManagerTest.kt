package com.qualcomm.incident.core.workflow

import com.qualcomm.incident.core.domain.OptionalField
import com.qualcomm.incident.core.domain.ReportPatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiTurnConversationManagerTest {

    @Test
    fun testMultiTurn_incrementalAdditionsPreserveUnrelatedFields() {
        val manager = MultiTurnConversationManager()

        // Turn 1: Initial report (type & description only)
        val turn1Patch = ReportPatch(
            incidentType = OptionalField.Present("Chemical"),
            incidentDescription = OptionalField.Present("Acid valve leaked")
        )
        val t1 = manager.applyTurn("Chemical spill in acid valve", turn1Patch)

        assertEquals(1, t1.turnIndex)
        assertEquals(MultiTurnConversationManager.TurnType.INITIAL_NARRATIVE, t1.turnType)
        assertEquals("Chemical", manager.currentReport.value.incidentType)
        assertEquals("Acid valve leaked", manager.currentReport.value.incidentDescription)
        assertNull(manager.currentReport.value.incidentLocation)

        // Turn 2: Follow-up response (location)
        val turn2Patch = ReportPatch(
            incidentLocation = OptionalField.Present("Reactor Building 4")
        )
        val t2 = manager.applyTurn("It happened in Reactor Building 4", turn2Patch)

        assertEquals(2, t2.turnIndex)
        assertEquals(MultiTurnConversationManager.TurnType.FOLLOW_UP_ANSWER, t2.turnType)
        // Crucial M4 constraint: Turn 1 fields MUST remain preserved!
        assertEquals("Chemical", manager.currentReport.value.incidentType)
        assertEquals("Acid valve leaked", manager.currentReport.value.incidentDescription)
        assertEquals("Reactor Building 4", manager.currentReport.value.incidentLocation)

        // Turn 3: Follow-up response (date/time & severity)
        val turn3Patch = ReportPatch(
            dateTime = OptionalField.Present("10:15 AM"),
            severityLevel = OptionalField.Present("Critical")
        )
        val t3 = manager.applyTurn("At 10:15 AM, critical severity", turn3Patch)

        assertEquals(3, t3.turnIndex)
        assertEquals("Chemical", manager.currentReport.value.incidentType)
        assertEquals("Acid valve leaked", manager.currentReport.value.incidentDescription)
        assertEquals("Reactor Building 4", manager.currentReport.value.incidentLocation)
        assertEquals("10:15 AM", manager.currentReport.value.dateTime)
        assertEquals("Critical", manager.currentReport.value.severityLevel)
    }

    @Test
    fun testMultiTurn_explicitCorrectionOverwritesTargetAndPreservesOthers() {
        val manager = MultiTurnConversationManager()

        // Turn 1
        manager.applyTurn(
            "Fire in Server Room B",
            ReportPatch(
                incidentType = OptionalField.Present("Fire"),
                incidentLocation = OptionalField.Present("Server Room B"),
                incidentDescription = OptionalField.Present("Rack overheating")
            )
        )

        // Turn 2: Correction
        val correctionTurn = manager.applyTurn(
            "Actually correction, it is in Server Room C, not B",
            ReportPatch(
                incidentLocation = OptionalField.Present("Server Room C")
            )
        )

        assertEquals(MultiTurnConversationManager.TurnType.CORRECTION_OVERRIDE, correctionTurn.turnType)
        // Target field updated
        assertEquals("Server Room C", manager.currentReport.value.incidentLocation)
        // Unrelated fields preserved
        assertEquals("Fire", manager.currentReport.value.incidentType)
        assertEquals("Rack overheating", manager.currentReport.value.incidentDescription)
    }

    @Test
    fun testMultiTurn_explicitNegationClearsField() {
        val manager = MultiTurnConversationManager()

        // Turn 1: with persons involved
        manager.applyTurn(
            "Worker John Doe was injured",
            ReportPatch(
                incidentDescription = OptionalField.Present("Slip and fall"),
                personsInvolved = OptionalField.Present(listOf("John Doe"))
            )
        )
        assertEquals(listOf("John Doe"), manager.currentReport.value.personsInvolved)

        // Turn 2: Negation
        val negationTurn = manager.applyTurn(
            "Actually no one was injured, cancel persons",
            ReportPatch(
                personsInvolved = OptionalField.Present(emptyList())
            )
        )

        assertEquals(MultiTurnConversationManager.TurnType.NEGATION_REMOVAL, negationTurn.turnType)
        assertEquals(emptyList<String>(), manager.currentReport.value.personsInvolved)
        assertEquals("Slip and fall", manager.currentReport.value.incidentDescription)
    }

    @Test
    fun testMultiTurn_undoRollsBackToPriorReportState() {
        val manager = MultiTurnConversationManager()

        // Turn 1
        manager.applyTurn("Incident in Warehouse 1", ReportPatch(incidentLocation = OptionalField.Present("Warehouse 1")))
        // Turn 2
        manager.applyTurn("Correction to Warehouse 2", ReportPatch(incidentLocation = OptionalField.Present("Warehouse 2")))

        assertEquals("Warehouse 2", manager.currentReport.value.incidentLocation)
        assertEquals(2, manager.turns.value.size)

        // Undo Turn 2
        val undone = manager.undoLastTurn()
        assertTrue(undone)
        assertEquals(1, manager.turns.value.size)
        // Restored to Turn 1 state
        assertEquals("Warehouse 1", manager.currentReport.value.incidentLocation)
    }

    @Test
    fun testMultiTurn_undoOnEmptyReturnsFalse() {
        val manager = MultiTurnConversationManager()
        assertFalse(manager.undoLastTurn())
    }
}
