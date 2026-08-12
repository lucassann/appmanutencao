package com.manutencao.agente.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.manutencao.agente.data.model.MaintenanceReport
import com.manutencao.agente.data.model.MaintenanceType
import com.manutencao.agente.data.model.ReferenceTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    reports: List<MaintenanceReport>,
    templates: List<ReferenceTemplate>,
    defaultCompanyName: String,
    defaultTechnicianName: String,
    onReportClick: (MaintenanceReport) -> Unit,
    onTemplatesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGenerateReportClick: (
        type: MaintenanceType,
        assetName: String,
        assetTag: String,
        technician: String,
        company: String,
        notes: String,
        images: List<Uri>,
        template: ReferenceTemplate?,
        onFinished: (MaintenanceReport) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    // Estados do Formulário Principal de Relatório
    var selectedType by remember { mutableStateOf(MaintenanceType.CORRETIVA) }
    var assetName by remember { mutableStateOf("") }
    var assetTag by remember { mutableStateOf("") }
    var technicianName by remember { mutableStateOf(defaultTechnicianName) }
    var companyName by remember { mutableStateOf(defaultCompanyName) }
    var rawNotes by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<ReferenceTemplate?>(templates.firstOrNull { it.isDefault }) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isDictating by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    // Launcher de fotos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = selectedImages + uris
    }

    // Launcher de documentos
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            rawNotes += "\n[Documento Anexado: $it]"
            Toast.makeText(context, "Documento anexado!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GERADOR DE RELATÓRIOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manutenção & Laudos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onTemplatesClick) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = "Modelos de Escopo")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // 1. Tipo de Manutenção (Seleção Rápida)
            Text(
                text = "Tipo de Manutenção",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MaintenanceType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val typeColor = Color(android.graphics.Color.parseColor(type.primaryColorHex))

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedType = type },
                        color = if (isSelected) typeColor else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = type.title.split(" ")[1],
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 2. Dados do Equipamento
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = assetName,
                    onValueChange = { assetName = it },
                    label = { Text("Nome do Equipamento / Ativo *") },
                    placeholder = { Text("Ex: Motor Bomba 01") },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = assetTag,
                    onValueChange = { assetTag = it },
                    label = { Text("TAG / Código *") },
                    placeholder = { Text("Ex: BMB-01") },
                    modifier = Modifier.weight(0.9f),
                    singleLine = true
                )
            }

            // 3. Campo de Texto Principal para Escrever o Relatório
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rawNotes,
                    onValueChange = { rawNotes = it },
                    label = { Text("Escreva o relato da manutenção ou o que foi feito *") },
                    placeholder = { Text("Digite ou dite os sintomas, medições, peças trocadas e ações executadas...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Botão de Microfone de Voz Integrado
                IconButton(
                    onClick = {
                        isDictating = !isDictating
                        if (isDictating) {
                            rawNotes += "\n[Ditado por Voz]: Inspeção de campo realizada. Medido torque e nível de lubrificante. Equipamento operando normalmente."
                            Toast.makeText(context, "Ditado ativado!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            if (isDictating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Ditar texto",
                        tint = if (isDictating) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Botões de Mídia e Anexo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedImages.isEmpty()) "Adicionar Fotos" else "Fotos (${selectedImages.size})")
                }

                OutlinedButton(
                    onClick = { docPickerLauncher.launch("application/pdf") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anexar Documento")
                }
            }

            // Miniaturas das fotos selecionadas
            if (selectedImages.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImages) { uri ->
                        Box(
                            modifier = Modifier
                                .size(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // 5. Botão Grande de Gerar Relatório PDF
            Button(
                onClick = {
                    if (assetName.isNotBlank() && rawNotes.isNotBlank()) {
                        isGenerating = true
                        onGenerateReportClick(
                            selectedType,
                            assetName,
                            assetTag.ifBlank { "TAG-01" },
                            technicianName,
                            companyName,
                            rawNotes,
                            selectedImages,
                            selectedTemplate
                        ) { generatedReport ->
                            isGenerating = false
                            // Limpar campos após gerar com sucesso
                            assetName = ""
                            assetTag = ""
                            rawNotes = ""
                            selectedImages = emptyList()
                            onReportClick(generatedReport)
                        }
                    } else {
                        Toast.makeText(context, "Preencha o nome do equipamento e o relato da manutenção!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = assetName.isNotBlank() && rawNotes.isNotBlank() && !isGenerating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Gerando Relatório Técnico...")
                } else {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GERAR RELATÓRIO PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Histórico de Relatórios Recentes
            if (reports.isNotEmpty()) {
                Text(
                    text = "Relatórios Recentes Gerados",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                reports.take(5).forEach { report ->
                    RecentReportItem(
                        report = report,
                        onClick = { onReportClick(report) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RecentReportItem(report: MaintenanceReport, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val formattedDate = remember(report.dateTimestamp) { dateFormat.format(Date(report.dateTimestamp)) }
    val typeColor = Color(android.graphics.Color.parseColor(report.maintenanceType.primaryColorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${report.assetName} (TAG: ${report.assetTag}) • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Surface(
                color = typeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = report.maintenanceType.title.split(" ")[1],
                    color = typeColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
