package com.qualcomm.incident.feature.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qualcomm.incident.core.domain.IncidentReport
import com.qualcomm.incident.core.domain.Provenance
import com.qualcomm.incident.core.workflow.IncidentWorkflowStateMachine
import com.qualcomm.incident.core.workflow.MultiTurnConversationManager
import com.qualcomm.incident.core.workflow.RequiredFieldsPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: IncidentReportViewModel,
    modifier: Modifier = Modifier,
    onSimulateVoiceMicClick: () -> Unit = {}
) {
    val report by viewModel.reportState.collectAsState()
    val workflowState by viewModel.workflowState.collectAsState()
    val lastTranscript by viewModel.lastTranscript.collectAsState()
    val lastDecision by viewModel.lastDecision.collectAsState()
    val ttsStatus by viewModel.ttsStatus.collectAsState()
    val conversationTurns by viewModel.conversationTurns.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var activeCorrectionField by remember { mutableStateOf<RequiredFieldsPolicy.MandatoryField?>(null) }
    var correctionText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Qualcomm AI Incident Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Snapdragon 8 Gen 3 • Hexagon NPU Accelerated • Offline Ready",
                            fontSize = 11.sp,
                            color = Color(0xFF90CAF9)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1)
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Workflow Status Banner
            item {
                WorkflowStatusCard(
                    state = workflowState,
                    ttsStatus = ttsStatus
                )
            }

            // 2. Visual Fallback Warning for TTS Offline
            if (ttsStatus is IncidentReportViewModel.TtsStatus.Unavailable) {
                val unavailable = ttsStatus as IncidentReportViewModel.TtsStatus.Unavailable
                item {
                    TtsVisualFallbackBanner(reason = unavailable.reason)
                }
            }

            // 3. Follow-Up Question / Prompt Card (if missing fields exist)
            if (workflowState is IncidentWorkflowStateMachine.State.AskingFollowUp) {
                val followUp = workflowState as IncidentWorkflowStateMachine.State.AskingFollowUp
                item {
                    FollowUpCard(
                        question = followUp.question,
                        missingField = followUp.missingField,
                        onSubmitAnswer = { answer ->
                            viewModel.processNarrativeText(answer)
                        }
                    )
                }
            }

            // 4. Audio & Voice Control Section
            item {
                VoiceControlSection(
                    state = workflowState,
                    lastTranscript = lastTranscript,
                    onStartVoice = {
                        viewModel.startListening()
                        onSimulateVoiceMicClick()
                    },
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    onSubmitText = {
                        if (textInput.isNotBlank()) {
                            viewModel.processNarrativeText(textInput)
                            textInput = ""
                        }
                    }
                )
            }

            // 5. Decision Triage Banner (if evaluated)
            if (lastDecision != null) {
                item {
                    DecisionTriageCard(
                        incidentType = lastDecision!!.incidentType,
                        severityScore = lastDecision!!.severityScore,
                        isUrgent = lastDecision!!.isUrgent,
                        routingDepartment = lastDecision!!.routingDepartment
                    )
                }
            }

            // 6. Multi-Turn Conversation History Card
            if (conversationTurns.isNotEmpty()) {
                item {
                    ConversationHistoryCard(
                        turns = conversationTurns,
                        onUndo = { viewModel.undoLastTurn() }
                    )
                }
            }

            // 7. Incident Report Data Fields Card
            item {
                IncidentReportDataCard(
                    report = report,
                    workflowState = workflowState,
                    onConfirm = { viewModel.confirmAndFinalize() },
                    onReset = { viewModel.reset() },
                    onRequestCorrection = { field, currentValue ->
                        activeCorrectionField = field
                        correctionText = currentValue ?: ""
                    }
                )
            }
        }

        // Inline Field Correction Dialog
        if (activeCorrectionField != null) {
            AlertDialog(
                onDismissRequest = { activeCorrectionField = null },
                title = { Text("Correct ${activeCorrectionField!!.displayName}") },
                text = {
                    Column {
                        Text(
                            text = "Provide the corrected value or leave blank to clear:",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = correctionText,
                            onValueChange = { correctionText = it },
                            placeholder = { Text("Enter updated value...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.applyCorrection(activeCorrectionField!!, correctionText)
                            activeCorrectionField = null
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeCorrectionField = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun WorkflowStatusCard(
    state: IncidentWorkflowStateMachine.State,
    ttsStatus: IncidentReportViewModel.TtsStatus
) {
    val (statusText, statusColor, isProcessing) = when (state) {
        is IncidentWorkflowStateMachine.State.Idle -> Triple("Ready to Record", Color(0xFF455A64), false)
        is IncidentWorkflowStateMachine.State.Listening -> Triple("Listening via Microphone (16kHz PCM)...", Color(0xFF1976D2), true)
        is IncidentWorkflowStateMachine.State.Transcribing -> Triple("Transcribing (Whisper on Hexagon NPU)...", Color(0xFF7B1FA2), true)
        is IncidentWorkflowStateMachine.State.Extracting -> Triple("Extracting Fields (Llama-3.2 on NPU)...", Color(0xFFE65100), true)
        is IncidentWorkflowStateMachine.State.AskingFollowUp -> Triple("Awaiting Follow-up Information", Color(0xFFF57C00), false)
        is IncidentWorkflowStateMachine.State.Reviewing -> Triple("All Mandatory Fields Complete • Ready to Review", Color(0xFF2E7D32), false)
        is IncidentWorkflowStateMachine.State.Finalized -> Triple("Incident Report Finalized & Submitted", Color(0xFF1B5E20), false)
        is IncidentWorkflowStateMachine.State.Error -> Triple("Error: ${(state as IncidentWorkflowStateMachine.State.Error).message}", Color(0xFFC62828), false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                if (ttsStatus is IncidentReportViewModel.TtsStatus.Speaking) {
                    Text(
                        text = "Speaking verbal prompt offline...",
                        fontSize = 11.sp,
                        color = Color(0xFF00796B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun TtsVisualFallbackBanner(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ℹ️", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "TTS Audio Readback Offline",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
                Text(
                    text = "Speech output unavailable ($reason). All visual prompts remain fully operational.",
                    fontSize = 11.sp,
                    color = Color(0xFF795548)
                )
            }
        }
    }
}

@Composable
fun FollowUpCard(
    question: String,
    missingField: RequiredFieldsPolicy.MandatoryField,
    onSubmitAnswer: (String) -> Unit
) {
    var answerText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Missing Mandatory Field: ${missingField.name.replace("_", " ")}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = question,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    placeholder = { Text("Type reply...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (answerText.isNotBlank()) {
                            onSubmitAnswer(answerText)
                            answerText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text("Reply")
                }
            }
        }
    }
}

@Composable
fun VoiceControlSection(
    state: IncidentWorkflowStateMachine.State,
    lastTranscript: String,
    onStartVoice: () -> Unit,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    onSubmitText: () -> Unit
) {
    val isListening = state is IncidentWorkflowStateMachine.State.Listening
    val micBgColor by animateColorAsState(
        targetValue = if (isListening) Color(0xFFD32F2F) else Color(0xFF1976D2),
        label = "micBg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartVoice,
                    colors = ButtonDefaults.buttonColors(containerColor = micBgColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isListening) "Listening..." else "Record Utterance",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Push-to-Talk via QIDK Microphones",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (lastTranscript.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Latest Transcript: \"$lastTranscript\"",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Manual text entry fallback / corrections
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextInputChange,
                    placeholder = { Text("Or add detail / correction...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSubmitText,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                ) {
                    Text("Parse")
                }
            }
        }
    }
}

@Composable
fun ConversationHistoryCard(
    turns: List<MultiTurnConversationManager.ConversationTurn>,
    onUndo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conversation History (${turns.size} turns)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )
                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Undo Turn", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            turns.takeLast(3).forEach { turn ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "T${turn.turnIndex} [${turn.turnType.name.replace("_", " ")}]:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"${turn.userUtterance}\"",
                        fontSize = 11.sp,
                        color = Color(0xFF424242),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DecisionTriageCard(
    incidentType: String,
    severityScore: Int,
    isUrgent: Boolean,
    routingDepartment: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Automated Triage: $incidentType",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Routed to: $routingDepartment",
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C)
                )
            }
            Surface(
                color = if (isUrgent) Color(0xFFC62828) else Color(0xFF388E3C),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isUrgent) "URGENT (Score $severityScore/5)" else "NORMAL (Score $severityScore/5)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentReportDataCard(
    report: IncidentReport,
    workflowState: IncidentWorkflowStateMachine.State,
    onConfirm: () -> Unit,
    onReset: () -> Unit,
    onRequestCorrection: (RequiredFieldsPolicy.MandatoryField, String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Structured Incident Schema",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                OutlinedButton(onClick = onReset) {
                    Text("Reset", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReportFieldRow(
                label = "Incident Type",
                value = report.incidentType,
                provenance = report.fieldProvenance["incidentType"],
                isMandatory = true,
                onEdit = { onRequestCorrection(RequiredFieldsPolicy.MandatoryField.INCIDENT_TYPE, report.incidentType) }
            )
            ReportFieldRow(
                label = "Severity Level",
                value = report.severityLevel,
                provenance = report.fieldProvenance["severityLevel"],
                isMandatory = true,
                onEdit = { onRequestCorrection(RequiredFieldsPolicy.MandatoryField.SEVERITY_LEVEL, report.severityLevel) }
            )
            ReportFieldRow(
                label = "Location",
                value = report.incidentLocation,
                provenance = report.fieldProvenance["incidentLocation"],
                isMandatory = true,
                onEdit = { onRequestCorrection(RequiredFieldsPolicy.MandatoryField.LOCATION, report.incidentLocation) }
            )
            ReportFieldRow(
                label = "Date / Time",
                value = report.dateTime,
                provenance = report.fieldProvenance["dateTime"],
                isMandatory = true,
                onEdit = { onRequestCorrection(RequiredFieldsPolicy.MandatoryField.DATE_TIME, report.dateTime) }
            )
            ReportFieldRow(
                label = "Description",
                value = report.incidentDescription,
                provenance = report.fieldProvenance["incidentDescription"],
                isMandatory = true,
                onEdit = { onRequestCorrection(RequiredFieldsPolicy.MandatoryField.DESCRIPTION, report.incidentDescription) }
            )

            ReportFieldRow("Equipment / Asset", report.equipmentAssetInvolved, report.fieldProvenance["equipmentAssetInvolved"])
            ReportFieldRow("Immediate Actions", report.immediateActionsTaken, report.fieldProvenance["immediateActionsTaken"])
            ReportFieldRow("Follow-Up Action", report.recommendedFollowUpAction, report.fieldProvenance["recommendedFollowUpAction"])

            // Persons Involved
            if (!report.personsInvolved.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Persons Involved:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.personsInvolved!!.forEach { person ->
                        Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)) {
                            Text(text = person, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            // Witnesses
            if (!report.witnessInformation.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Witnesses:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.witnessInformation!!.forEach { witness ->
                        Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)) {
                            Text(text = witness, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            // Confirm & Finalize Button
            AnimatedVisibility(visible = workflowState is IncidentWorkflowStateMachine.State.Reviewing) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Confirm & Submit Final Report",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFieldRow(
    label: String,
    value: String?,
    provenance: Provenance?,
    isMandatory: Boolean = false,
    onEdit: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF37474F)
                )
                if (isMandatory) {
                    Text(text = " *", fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (provenance != null) {
                    Text(
                        text = provenance.name,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (onEdit != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✏️",
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onEdit() }
                    )
                }
            }
        }
        Text(
            text = value ?: "Not specified",
            fontSize = 14.sp,
            color = if (value != null) Color(0xFF212121) else Color(0xFF9E9E9E),
            fontWeight = if (value != null) FontWeight.Normal else FontWeight.Light
        )
    }
}
