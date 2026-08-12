package com.manutencao.agente.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manutencao.agente.data.model.MaintenanceReport
import com.manutencao.agente.data.model.SeverityLevel
import com.manutencao.agente.data.pdf.PdfReportExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: MaintenanceReport,
    onReportUpdated: (MaintenanceReport) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val pdfExporter = remember { PdfReportExporter(context) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }

    var isEditMode by remember { mutableStateOf(false) }

    // Estados dos campos editáveis do relatório
    var editedTitle by remember(report) { mutableStateOf(report.title) }
    var editedAssetName by remember(report) { mutableStateOf(report.assetName) }
    var editedAssetTag by remember(report) { mutableStateOf(report.assetTag) }
    var editedSummary by remember(report) { mutableStateOf(report.generatedSummary) }
    var editedRootCause by remember(report) { mutableStateOf(report.rootCauseDiagnosis) }
    var editedActions by remember(report) { mutableStateOf(report.actionsTaken) }
    var editedRecommendations by remember(report) { mutableStateOf(report.recommendations) }
    var editedSeverity by remember(report) { mutableStateOf(report.severityLevel) }
    var editedDowntime by remember(report) { mutableStateOf(report.downtimeHours) }
    var editedParts by remember(report) { mutableStateOf(report.partsReplaced) }

    var newPartText by remember { mutableStateOf("") }

    val currentReport = remember(
        report, editedTitle, editedAssetName, editedAssetTag, editedSummary,
        editedRootCause, editedActions, editedRecommendations, editedSeverity,
        editedDowntime, editedParts
    ) {
        report.copy(
            title = editedTitle,
            assetName = editedAssetName,
            assetTag = editedAssetTag,
            generatedSummary = editedSummary,
            rootCauseDiagnosis = editedRootCause,
            actionsTaken = editedActions,
            recommendations = editedRecommendations,
            severityLevel = editedSeverity,
            downtimeHours = editedDowntime,
            partsReplaced = editedParts
        )
    }

    val typeColor = Color(android.graphics.Color.parseColor(currentReport.maintenanceType.primaryColorHex))
    val sevColor = Color(android.graphics.Color.parseColor(currentReport.severityLevel.colorHex))
    val formattedDate = remember(report.dateTimestamp) { dateFormat.format(Date(report.dateTimestamp)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Relatório" else "Visualizar Relatório", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            if (isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Ver Modo Leitura" else "Editar Relatório",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = {
                        val textToCopy = """
                            *${currentReport.title.uppercase()}*
                            *Empresa:* ${currentReport.companyName}
                            *Equipamento:* ${currentReport.assetName} (TAG: ${currentReport.assetTag})
                            *Técnico:* ${currentReport.technicianName}
                            *Data:* $formattedDate
                            *Severidade:* ${currentReport.severityLevel.label}
                            
                            *1. DIAGNÓSTICO:*
                            ${currentReport.generatedSummary}
                            
                            *2. CAUSA RAIZ:*
                            ${currentReport.rootCauseDiagnosis}
                            
                            *3. AÇÕES DE CAMPO:*
                            ${currentReport.actionsTaken}
                            
                            *4. RECOMENDAÇÕES:*
                            ${currentReport.recommendations}
                        """.trimIndent()

                        clipboardManager.setText(AnnotatedString(textToCopy))
                        Toast.makeText(context, "Relatório copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Texto")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner Modo de Edição Ativo
            AnimatedVisibility(visible = isEditMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Modo de Edição Ativo. Modifique qualquer texto do relatório abaixo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Cabeçalho com Informações Principais
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = typeColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentReport.maintenanceType.title.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        if (!isEditMode) {
                            Surface(
                                color = sevColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "GRAVIDADE: ${currentReport.severityLevel.label}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            label = { Text("Título do Relatório") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedAssetName,
                                onValueChange = { editedAssetName = it },
                                label = { Text("Equipamento / Ativo") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editedAssetTag,
                                onValueChange = { editedAssetTag = it },
                                label = { Text("TAG") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nível de Severidade:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            SeverityLevel.values().forEach { sev ->
                                FilterChip(
                                    selected = editedSeverity == sev,
                                    onClick = { editedSeverity = sev },
                                    label = { Text(sev.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = currentReport.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ativo: ${currentReport.assetName} (TAG: ${currentReport.assetTag})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Técnico: ${currentReport.technicianName} • ${currentReport.companyName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Data de emissão: $formattedDate • Parada: ${currentReport.downtimeHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Seções Editáveis ou de Leitura
            EditableSectionCard(
                title = "1. Resumo Executivo & Diagnóstico",
                content = editedSummary,
                onContentChange = { editedSummary = it },
                isEditMode = isEditMode,
                icon = Icons.Default.Assessment
            )

            EditableSectionCard(
                title = "2. Análise de Causa Raiz",
                content = editedRootCause,
                onContentChange = { editedRootCause = it },
                isEditMode = isEditMode,
                icon = Icons.Default.Search
            )

            EditableSectionCard(
                title = "3. Ações Executadas em Campo",
                content = editedActions,
                onContentChange = { editedActions = it },
                isEditMode = isEditMode,
                icon = Icons.Default.Build
            )

            EditableSectionCard(
                title = "4. Recomendações Técnicas",
                content = editedRecommendations,
                onContentChange = { editedRecommendations = it },
                isEditMode = isEditMode,
                icon = Icons.Default.Lightbulb
            )

            // Edição de Componentes & Peças Trocadas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("5. Peças e Materiais Utilizados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPartText,
                                onValueChange = { newPartText = it },
                                label = { Text("Adicionar Peça / Material") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                if (newPartText.isNotBlank()) {
                                    editedParts = editedParts + newPartText
                                    newPartText = ""
                                }
                            }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Adicionar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        editedParts.forEachIndexed { index, part ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• $part", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    editedParts = editedParts.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    } else {
                        if (editedParts.isEmpty()) {
                            Text("Nenhum componente substituído nesta intervenção.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        } else {
                            editedParts.forEach { part ->
                                Text("• $part", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // Salvar Edição
            if (isEditMode) {
                Button(
                    onClick = {
                        onReportUpdated(currentReport)
                        isEditMode = false
                        Toast.makeText(context, "Relatório atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Alterações do Relatório", fontWeight = FontWeight.Bold)
                }
            }

            // Botões de Ação para Exportação PDF e WhatsApp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val pdfFile = pdfExporter.generatePdfReport(currentReport)
                            val uri = pdfExporter.getShareableUri(pdfFile)

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório PDF"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gerar PDF")
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val pdfFile = pdfExporter.generatePdfReport(currentReport)
                            val uri = pdfExporter.getShareableUri(pdfFile)

                            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                setPackage("com.whatsapp")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(whatsappIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Abrindo compartilhador geral...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun EditableSectionCard(
    title: String,
    content: String,
    onContentChange: (String) -> Unit,
    isEditMode: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditMode) {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )
            } else {
                Text(
                    content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}
