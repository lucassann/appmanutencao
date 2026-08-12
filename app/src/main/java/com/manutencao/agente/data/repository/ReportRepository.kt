package com.manutencao.agente.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.manutencao.agente.data.model.MaintenanceReport
import com.manutencao.agente.data.model.MaintenanceType
import com.manutencao.agente.data.model.ReferenceTemplate
import com.manutencao.agente.data.model.SeverityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ReportRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("agente_manutencao_prefs", Context.MODE_PRIVATE)

    private val _reports = MutableStateFlow<List<MaintenanceReport>>(emptyList())
    val reports: StateFlow<List<MaintenanceReport>> = _reports.asStateFlow()

    private val _templates = MutableStateFlow<List<ReferenceTemplate>>(emptyList())
    val templates: StateFlow<List<ReferenceTemplate>> = _templates.asStateFlow()

    init {
        // Inicializar com templates padrão de escopo da empresa
        val defaultTemplates = listOf(
            ReferenceTemplate(
                id = "tpl_petrobras",
                name = "Padrão Industrial Rigoroso (NBR/ISO)",
                description = "Escopo completo exigido para plantas químicas, refinarias e indústrias pesadas",
                sampleText = "Exige diagnóstico de causa raiz com 5 porquês, checklist de segurança NR-10/NR-12, medição de vibração e torque nominal.",
                requiredSections = listOf("Resumo", "Segurança NR", "Diagnóstico Causa Raiz", "Ações de Campo", "Recomendações Preditivas"),
                logoHeaderText = "ENGENHARIA DE MANUTENÇÃO INDUSTRIAL",
                isDefault = true
            ),
            ReferenceTemplate(
                id = "tpl_startup",
                name = "Padrão Partida Técnica e Comissionamento",
                description = "Template para start-up de novas máquinas, bombas, motores e quadros elétricos",
                sampleText = "Foco em medição de grandezas nominais vs. lidas, teste de isolamento elétrico, teste de vazão/pressão e termo de aceite com garantia.",
                requiredSections = listOf("Dados de Chapa", "Parâmetros Nominais", "Valores Medidos", "Testes de Carga", "Termo de Aceite"),
                logoHeaderText = "COMISSIONAMENTO & START-UP TÉCNICO",
                isDefault = false
            ),
            ReferenceTemplate(
                id = "tpl_preventiva_simplificada",
                name = "Padrão Preventiva Semanal / Mensal",
                description = "Relatório ágil focado em checklists, lubrificação e inspeção rotineira",
                sampleText = "Checklist rápido de inspeção visual, nível de óleo, reaperto elétrico e programação do próximo ciclo.",
                requiredSections = listOf("Status Checklist", "Serviços Executados", "Próxima Inspeção"),
                logoHeaderText = "INSPEÇÃO PREVENTIVA REGULAR",
                isDefault = false
            )
        )
        _templates.value = defaultTemplates

        // Adicionar alguns relatórios demonstrativos para popular o dashboard
        val initialReports = listOf(
            MaintenanceReport(
                id = UUID.randomUUID().toString(),
                title = "Manutenção Corretiva - Motor Bomba Principal 01",
                maintenanceType = MaintenanceType.CORRETIVA,
                assetName = "Conjunto Moto-Bomba Centrifuga 75CV",
                assetTag = "BMB-75-01",
                technicianName = "Carlos Eduardo Silva",
                companyName = "TechManut Manutenção Ltda",
                dateTimestamp = System.currentTimeMillis() - 86400000L, // Ontem
                rawNotes = "Bomba com forte vibração e vazamento no selo mecânico. Trocado selo e rolamento 6312.",
                generatedSummary = "Intervenção corretiva de urgência realizada no conjunto moto-bomba devido ao rompimento das faces do selo mecânico e desgaste acentuado no rolamento dianteiro.",
                rootCauseDiagnosis = "Desalinhamento axial provocado pela folga nos parafusos da base metálica combinado com cavitação severa na sucção.",
                actionsTaken = "Substituição do selo mecânico SiC/SiC, troca do rolamento 6312 C3, alinhamento óptico a laser e substituição do acoplamento flexível.",
                recommendations = "Instalar sensor de vibração sem fio para monitoramento contínuo e verificar válvula de retenção na sucção.",
                severityLevel = SeverityLevel.ALTO,
                downtimeHours = "3.5 horas",
                partsReplaced = listOf("Selo Mecânico 45mm Viton", "Rolamento SKF 6312 C3", "Elemento Elástico Acoplamento"),
                imageUris = emptyList()
            ),
            MaintenanceReport(
                id = UUID.randomUUID().toString(),
                title = "Partida Técnica - Compressor de Parafuso 100HP",
                maintenanceType = MaintenanceType.PARTIDA_TECNICA,
                assetName = "Compressor de Ar Comprimido Isento de Óleo",
                assetTag = "CMP-100-02",
                technicianName = "Carlos Eduardo Silva",
                companyName = "TechManut Manutenção Ltda",
                dateTimestamp = System.currentTimeMillis() - 259200000L, // 3 dias atrás
                rawNotes = "Start-up do novo compressor. Tensão 380V ok. Pressão de trabalho 8.5 bar ok.",
                generatedSummary = "Comissionamento e validação de garantia efetuados no compressor novo CMP-100-02. Todos os ensaios de partida aprovados.",
                rootCauseDiagnosis = "Equipamento novo em perfeito estado operacional.",
                actionsTaken = "Conexão elétrica, teste de rotação, calibração da válvula pressostática e teste de carga em regime por 2 horas.",
                recommendations = "Realizar primeira troca do filtro de admissão após 500 horas de operação inicial.",
                severityLevel = SeverityLevel.BAIXO,
                downtimeHours = "0 horas",
                partsReplaced = emptyList(),
                imageUris = emptyList()
            )
        )
        _reports.value = initialReports
    }

    fun addReport(report: MaintenanceReport) {
        _reports.value = listOf(report) + _reports.value
    }

    fun addTemplate(template: ReferenceTemplate) {
        _templates.value = _templates.value + template
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("gemini_api_key", apiKey).apply()
    }

    fun getApiKey(): String {
        return prefs.getString("gemini_api_key", "") ?: ""
    }

    fun saveCompanyName(companyName: String) {
        prefs.edit().putString("company_name", companyName).apply()
    }

    fun getCompanyName(): String {
        return prefs.getString("company_name", "TechManut Engenharia") ?: "TechManut Engenharia"
    }

    fun saveTechnicianName(name: String) {
        prefs.edit().putString("technician_name", name).apply()
    }

    fun getTechnicianName(): String {
        return prefs.getString("technician_name", "Eng. Técnico de Campo") ?: "Eng. Técnico de Campo"
    }
}
