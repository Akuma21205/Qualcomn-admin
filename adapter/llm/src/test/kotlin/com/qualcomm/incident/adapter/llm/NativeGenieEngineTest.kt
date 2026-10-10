package com.qualcomm.incident.adapter.llm

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeGenieEngineTest {

    @Test
    fun `generate uses simulatedEngineFallback when running on host JVM without QIDK binary`() = runBlocking {
        val simulated = SimulatedGenieEngine(
            defaultResponse = """{"incident_type": "Fire / Explosion", "incident_location": "Generator 2"}"""
        )
        val engine = NativeGenieEngine(
            configPath = "/non/existent/path.json",
            binaryPath = "/non/existent/binary",
            simulatedEngineFallback = simulated
        )

        val result = engine.generate("Fire reported at Generator 2")
        assertTrue(result.isSuccess)
        val genResult = result.getOrThrow()
        assertTrue(genResult.text.contains("Generator 2"))
        assertEquals(1, simulated.executionHistory.size)
    }

    @Test
    fun `generate fails gracefully when fallback is null and binary is absent`() = runBlocking {
        val engine = NativeGenieEngine(
            configPath = "/non/existent/path.json",
            binaryPath = "/non/existent/binary",
            simulatedEngineFallback = null
        )

        val result = engine.generate("Emergency in warehouse")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
