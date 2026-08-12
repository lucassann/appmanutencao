package com.manutencao.agente.data.model

data class MaintenanceReport(
    val id: String,
    val title: String,
    val maintenanceType: MaintenanceType,
    val assetName: String,
    val assetTag: String,
    val technicianName: String,
    val companyName: String,
    val dateTimestamp: Long,
    val rawNotes: String,
    val generatedSummary: String,
    val rootCauseDiagnosis: String,
    val actionsTaken: String,
    val recommendations: String,
    val severityLevel: SeverityLevel,
    val downtimeHours: String,
    val partsReplaced: List<String>,
    val imageUris: List<String>,
    val referenceTemplateId: String? = null,
    val status: String = "CONCLUIDO"
)
