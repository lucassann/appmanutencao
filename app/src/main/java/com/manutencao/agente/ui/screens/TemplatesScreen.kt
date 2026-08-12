package com.manutencao.agente.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manutencao.agente.data.model.ReferenceTemplate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    templates: List<ReferenceTemplate>,
    onAddTemplate: (ReferenceTemplate) -> Unit,
    onUpdateTemplate: (ReferenceTemplate) -> Unit,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<ReferenceTemplate?>(null) }

    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newSample by remember { mutableStateOf("") }
    var newLogoHeader by remember { mutableStateOf("ENGENHARIA DE MANUTENÇÃO") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca de Escopos & Layouts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    templateToEdit = null
                    newName = ""
                    newDesc = ""
                    newSample = ""
                    newLogoHeader = "ENGENHARIA DE MANUTENÇÃO"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Modelo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Estes modelos definem a estrutura, o escopo e o layout que a IA e o gerador de PDF seguem para montar seus relatórios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(templates) { tpl ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tpl.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (tpl.isDefault) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "PADRÃO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = {
                                templateToEdit = tpl
                                newName = tpl.name
                                newDesc = tpl.description
                                newSample = tpl.sampleText
                                newLogoHeader = tpl.logoHeaderText
                                showAddDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Modelo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tpl.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Cabeçalho no PDF: ${tpl.logoHeaderText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Exemplo de Escopo:\n\"${tpl.sampleText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (templateToEdit == null) "Cadastrar Novo Modelo / Escopo" else "Editar Modelo de Escopo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nome do Modelo / Padrão") },
                        placeholder = { Text("Ex: Padrão Cliente Petrobras") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Descrição Breve") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLogoHeader,
                        onValueChange = { newLogoHeader = it },
                        label = { Text("Texto da Logomarca no PDF") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSample,
                        onValueChange = { newSample = it },
                        label = { Text("Regras de Escopo / Diretrizes de Texto") },
                        placeholder = { Text("Exige 5 Porquês, checklist NR-10, medição de vibração...") },
                        modifier = Modifier.height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            if (templateToEdit == null) {
                                onAddTemplate(
                                    ReferenceTemplate(
                                        id = UUID.randomUUID().toString(),
                                        name = newName,
                                        description = newDesc.ifBlank { "Modelo personalizado de escopo" },
                                        sampleText = newSample.ifBlank { "Estrutura personalizada" },
                                        logoHeaderText = newLogoHeader.ifBlank { "ENGENHARIA DE MANUTENÇÃO" },
                                        requiredSections = listOf("Resumo", "Diagnóstico", "Ações"),
                                        isDefault = false
                                    )
                                )
                            } else {
                                onUpdateTemplate(
                                    templateToEdit!!.copy(
                                        name = newName,
                                        description = newDesc,
                                        sampleText = newSample,
                                        logoHeaderText = newLogoHeader
                                    )
                                )
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (templateToEdit == null) "Salvar Modelo" else "Atualizar Modelo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
