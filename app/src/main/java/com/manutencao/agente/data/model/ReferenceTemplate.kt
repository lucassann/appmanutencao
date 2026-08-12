package com.manutencao.agente.data.model

data class ReferenceTemplate(
    val id: String,
    val name: String,
    val description: String,
    val sampleText: String, // Escopo / Regras do layout
    val requiredSections: List<String>,
    val logoHeaderText: String = "SISTEMA DE MANUTENÇÃO TÉCNICA",
    val isDefault: Boolean = false
)
