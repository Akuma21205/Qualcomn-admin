package com.qualcomm.incident.feature.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qualcomm.incident.core.domain.IncidentReport

@Composable
fun ReportScreen(
    report: IncidentReport,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Incident Report",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(text = "Type: ${report.incidentType ?: "Unstated"}")
        Text(text = "Severity: ${report.severityLevel ?: "Unstated"}")
        Text(text = "Location: ${report.incidentLocation ?: "Unstated"}")
        Text(text = "Date/Time: ${report.dateTime ?: "Unstated"}")
        Text(text = "Description: ${report.incidentDescription ?: "Unstated"}")
    }
}
