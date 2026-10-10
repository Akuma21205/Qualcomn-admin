package com.qualcomm.incident.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.qualcomm.incident.adapter.decision.LocalRuleDecisionEngine
import com.qualcomm.incident.adapter.llm.GenieLlmAdapter
import com.qualcomm.incident.adapter.llm.ModelFamily
import com.qualcomm.incident.adapter.stt.AudioFixtures
import com.qualcomm.incident.adapter.stt.WhisperSttAdapter
import com.qualcomm.incident.adapter.tts.AndroidSpeechOutput
import com.qualcomm.incident.feature.audio.AudioCaptureManager
import com.qualcomm.incident.feature.report.IncidentReportViewModel
import com.qualcomm.incident.feature.report.ReportScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: IncidentReportViewModel
    private val audioCaptureManager = AudioCaptureManager()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled; audio capture will operate if granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check/Request RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Initialize production on-device adapters
        val sttEngine = WhisperSttAdapter()
        val extractor = GenieLlmAdapter(modelFamily = ModelFamily.QWEN)
        val speechOutput = AndroidSpeechOutput(context = this)
        val decisionEngine = LocalRuleDecisionEngine()

        viewModel = IncidentReportViewModel(
            sttEngine = sttEngine,
            extractor = extractor,
            speechOutput = speechOutput,
            decisionEngine = decisionEngine
        )

        setContent {
            MaterialTheme {
                ReportScreen(
                    viewModel = viewModel,
                    onSimulateVoiceMicClick = {
                        // Launch audio capture or test fixture
                        lifecycleScope.launch {
                            val audio = if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                audioCaptureManager.captureUtterance(maxDurationMs = 5000L)
                            } else {
                                AudioFixtures.createActiveSpeechToneAudio(durationMs = 1500)
                            }
                            viewModel.processAudio(audio)
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioCaptureManager.stopCapture()
    }
}
