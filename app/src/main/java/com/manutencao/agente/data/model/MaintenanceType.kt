package com.manutencao.agente.data.model

import androidx.compose.ui.graphics.Color

enum class MaintenanceType(
    val title: String,
    val description: String,
    val primaryColorHex: String,
    val iconName: String
) {
    CORRETIVA(
        title = "Manutenção Corretiva",
        description = "Falhas imprevisíveis, substituição imediata de peças e reparos de urgência",
        primaryColorHex = "#EF4444", // Red
        iconName = "Build"
    ),
    PREVENTIVA(
        title = "Manutenção Preventiva",
        description = "Inspeções periódicas programadas, lubrificação, ajustes e substituições preventivas",
        primaryColorHex = "#10B981", // Emerald Green
        iconName = "CheckCircle"
    ),
    PREDITIVA(
        title = "Manutenção Preditiva",
        description = "Análise de sintomas (vibração, termografia, ultrassom, óleo) e acompanhamento de curva P-F",
        primaryColorHex = "#06B6D4", // Cyan
        iconName = "Timeline"
    ),
    PARTIDA_TECNICA(
        title = "Partida Técnica / Comissionamento",
        description = "Start-up de equipamento novo/reformado, verificação de grandezas nominais e termo de entrega",
        primaryColorHex = "#8B5CF6", // Purple
        iconName = "PlayArrow"
    )
}
