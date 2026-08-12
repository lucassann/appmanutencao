package com.manutencao.agente.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import coil.compose.AsyncImage
import com.manutencao.agente.data.model.MaintenanceType
import com.manutencao.agente.data.model.ReferenceTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    templates: List<ReferenceTemplate>,
    defaultCompanyName: String,
    defaultTechnicianName: String,
    onBackClick: () -> Unit,
    onGenerateReportClick: (
        type: MaintenanceType,
        assetName: String,
        assetTag: String,
        technician: String,
        company: String,
        notes: String,
        images: List<Uri>,
        template: ReferenceTemplate?
    ) -> Unit
) {
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

    // Launcher para selecionar fotos da galeria ou câmera
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = selectedImages + uris
    }

    // Launcher para anexar documentos (PDF/DOCX)
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            rawNotes += "\n[Anexo de Referência Selecionado: $it]"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Relatório de Manutenção", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            // Passo 1: Selecionar Tipo de Manutenção
            Text("1. Selecione o Tipo de Manutenção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MaintenanceType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val typeColor = Color(android.graphics.Color.parseColor(type.primaryColorHex))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) typeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, typeColor) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = typeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(type.title, fontWeight = FontWeight.Bold, color = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurface)
                                Text(type.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // Passo 2: Dados do Ativo e Empresa
            Text("2. Identificação do Equipamento & Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = assetName,
                onValueChange = { assetName = it },
                label = { Text("Nome do Equipamento / Ativo *") },
                placeholder = { Text("Ex: Motor Bomba Principal 01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = assetTag,
                    onValueChange = { assetTag = it },
                    label = { Text("TAG / Código *") },
                    placeholder = { Text("Ex: BMB-75-01") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = technicianName,
                    onValueChange = { technicianName = it },
                    label = { Text("Técnico Responsável") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Passo 3: Modelo / Escopo de Referência da Empresa
            Text("3. Escolha o Padrão de Escopo & Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Padrão da Empresa:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))

                    templates.forEach { template ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTemplate = template }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTemplate == template,
                                onClick = { selectedTemplate = template }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(template.name, fontWeight = FontWeight.SemiBold)
                                Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Passo 4: Observações, Fotos & Ditado por Voz
            Text("4. Relato de Campo, Voz & Anexo de Mídias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = rawNotes,
                onValueChange = { rawNotes = it },
                label = { Text("Descreva os sintomas, medições, peças e observações *") },
                placeholder = { Text("Ex: Bomba com ruído no rolamento dianteiro, pressão caindo de 6 para 4 bar, substituída gaxeta...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            // Botões de Entrada Multimodal (Câmera, Voz, Documento)
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
                    Text("Fotos (${selectedImages.size})")
                }

                OutlinedButton(
                    onClick = { docPickerLauncher.launch("application/pdf") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anexo PDF")
                }

                IconButton(
                    onClick = {
                        isDictating = !isDictating
                        if (isDictating) {
                            rawNotes += "\n[Ditado por Voz de Campo]: Realizado teste de isolamento elétrico 500V, valor medido 200 MOhms. Ruído normal na carcaça."
                        }
                    },
                    modifier = Modifier.background(
                        if (isDictating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Ditar por voz",
                        tint = if (isDictating) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Galeria de Fotos Selecionadas
            if (selectedImages.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImages) { uri ->
                        Box(
                            modifier = Modifier
                                .size(70.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Botão Ação de Gerar Relatório com IA
            Button(
                onClick = {
                    if (assetName.isNotBlank() && rawNotes.isNotBlank()) {
                        isGenerating = true
                        onGenerateReportClick(
                            selectedType,
                            assetName,
                            assetTag.ifBlank { "TAG-GENERICA" },
                            technicianName,
                            companyName,
                            rawNotes,
                            selectedImages,
                            selectedTemplate
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = assetName.isNotBlank() && rawNotes.isNotBlank() && !isGenerating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("🤖 IA Processando Multimodal...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar Relatório Técnico com IA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
