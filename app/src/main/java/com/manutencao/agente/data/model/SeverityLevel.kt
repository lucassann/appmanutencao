package com.manutencao.agente.data.model

enum class SeverityLevel(val label: String, val colorHex: String) {
    BAIXO("Baixo", "#10B981"),
    MEDIO("Médio", "#F59E0B"),
    ALTO("Alto", "#F97316"),
    CRITICO("CRÍTICO", "#EF4444")
}
