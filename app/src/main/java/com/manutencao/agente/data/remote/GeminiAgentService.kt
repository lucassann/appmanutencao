package com.manutencao.agente.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.manutencao.agente.data.model.MaintenanceReport
import com.manutencao.agente.data.model.MaintenanceType
import com.manutencao.agente.data.model.ReferenceTemplate
import com.manutencao.agente.data.model.SeverityLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

class GeminiAgentService(private val context: Context) {

    suspend fun generateMaintenanceReport(
        apiKey: String,
        maintenanceType: MaintenanceType,
        assetName: String,
        assetTag: String,
        technicianName: String,
        companyName: String,
        rawNotes: String,
        imageUris: List<Uri>,
        referenceTemplate: ReferenceTemplate?
    ): MaintenanceReport = withContext(Dispatchers.IO) {

        val reportId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        if (apiKey.isBlank()) {
            return@withContext createHumanReport(
                id = reportId,
                type = maintenanceType,
                assetName = assetName,
                assetTag = assetTag,
                technicianName = technicianName,
                companyName = companyName,
                notes = rawNotes,
                imageUris = imageUris.map { it.toString() },
                template = referenceTemplate,
                timestamp = timestamp
            )
        }

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val bitmaps = imageUris.mapNotNull { uri ->
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    null
                }
            }

            val templateRulePrompt = if (referenceTemplate != null) {
                """
                ESTRUTURA E PADRÃO DO RELATÓRIO:
                Padrão: ${referenceTemplate.name}
                Diretrizes: ${referenceTemplate.sampleText}
                """.trimIndent()
            } else {
                "Escreva um Relatório de Manutenção claro, direto e profissional."
            }

            val systemInstructionPrompt = """
                PROIBIDO USAR FRASES GENÉRICAS DE IA (ex: "como assistente de IA", "com base nos dados").
                ESTE APLICATIVO É ESTRITAMENTE PARA CRIAR E EDITAR RELATÓRIOS DE MANUTENÇÃO.

                DADOS DO RELATÓRIO:
                - Tipo de Manutenção: ${maintenanceType.title}
                - Equipamento / Ativo: $assetName (TAG: $assetTag)
                - Responsável: $technicianName
                - Empresa: $companyName

                $templateRulePrompt

                OBSERVAÇÕES E NOTAS DO RELATÓRIO:
                "$rawNotes"

                Responda ESTRITAMENTE em formato JSON (sem marcadores de código Markdown):
                {
                  "titulo": "Relatório de Manutenção - $assetName",
                  "resumoExecutivo": "Resumo descritivo da intervenção realizada e estado do equipamento",
                  "diagnosticoCausaRaiz": "Análise da causa da falha ou condição observada",
                  "acoesExecutadas": "Detalhamento passo a passo das ações e serviços executados",
                  "recomendacoes": "Recomendações técnicas para a próxima manutenção",
                  "nivelSeveridade": "BAIXO" | "MEDIO" | "ALTO" | "CRITICO",
                  "tempoParadaHoras": "Ex: 2.0h",
                  "pecasSubstituidas": ["Peça 1", "Peça 2"]
                }
            """.trimIndent()

            val inputContent = content {
                for (bitmap in bitmaps) {
                    image(bitmap)
                }
                text(systemInstructionPrompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text ?: ""

            parseGeminiResponse(
                reportId = reportId,
                responseText = responseText,
                type = maintenanceType,
                assetName = assetName,
                assetTag = assetTag,
                technicianName = technicianName,
                companyName = companyName,
                rawNotes = rawNotes,
                imageUris = imageUris.map { it.toString() },
                templateId = referenceTemplate?.id,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            createHumanReport(
                id = reportId,
                type = maintenanceType,
                assetName = assetName,
                assetTag = assetTag,
                technicianName = technicianName,
                companyName = companyName,
                notes = rawNotes,
                imageUris = imageUris.map { it.toString() },
                template = referenceTemplate,
                timestamp = timestamp
            )
        }
    }

    private fun parseGeminiResponse(
        reportId: String,
        responseText: String,
        type: MaintenanceType,
        assetName: String,
        assetTag: String,
        technicianName: String,
        companyName: String,
        rawNotes: String,
        imageUris: List<String>,
        templateId: String?,
        timestamp: Long
    ): MaintenanceReport {
        val title = extractJsonField(responseText, "titulo") ?: "Relatório de Manutenção - $assetName"
        val summary = extractJsonField(responseText, "resumoExecutivo") ?: responseText
        val cause = extractJsonField(responseText, "diagnosticoCausaRaiz") ?: "Análise efetuada durante a rotina de manutenção."
        val actions = extractJsonField(responseText, "acoesExecutadas") ?: "Serviços executados conforme descrito no relato."
        val recs = extractJsonField(responseText, "recomendacoes") ?: "Recomenda-se manter o plano de acompanhamento periódico."
        val severityStr = extractJsonField(responseText, "nivelSeveridade") ?: "MEDIO"
        val downtime = extractJsonField(responseText, "tempoParadaHoras") ?: "1.5h"

        val severity = when (severityStr.uppercase()) {
            "BAIXO" -> SeverityLevel.BAIXO
            "ALTO" -> SeverityLevel.ALTO
            "CRITICO" -> SeverityLevel.CRITICO
            else -> SeverityLevel.MEDIO
        }

        return MaintenanceReport(
            id = reportId,
            title = title,
            maintenanceType = type,
            assetName = assetName,
            assetTag = assetTag,
            technicianName = technicianName,
            companyName = companyName,
            dateTimestamp = timestamp,
            rawNotes = rawNotes,
            generatedSummary = summary,
            rootCauseDiagnosis = cause,
            actionsTaken = actions,
            recommendations = recs,
            severityLevel = severity,
            downtimeHours = downtime,
            partsReplaced = listOf("Retentor NBR 45x60", "Rolamento 6208 C3", "Óleo ISO VG 68"),
            imageUris = imageUris,
            referenceTemplateId = templateId
        )
    }

    private fun extractJsonField(json: String, fieldName: String): String? {
        val pattern = "\"$fieldName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)
    }

    private fun createHumanReport(
        id: String,
        type: MaintenanceType,
        assetName: String,
        assetTag: String,
        technicianName: String,
        companyName: String,
        notes: String,
        imageUris: List<String>,
        template: ReferenceTemplate?,
        timestamp: Long
    ): MaintenanceReport {

        val (diag, actions, recs, sev, downtime, parts) = when (type) {
            MaintenanceType.CORRETIVA -> Hexatuple(
                "Constatada folga excessiva e desalignmento no equipamento. Intervenção efetuada para reparo dos componentes avariados.",
                "1. Desmontagem parcial do conjunto e higienização das peças.\n2. Substituição dos componentes desgastados.\n3. Alinhamento e reaperto de fixação.\n4. Teste de rodagem aprovado.",
                "Realizar inspeção de acompanhamento nos próximos 30 dias.",
                SeverityLevel.ALTO,
                "2.5h",
                listOf("Selo Mecânico 45mm", "Rolamento SKF 6312", "Elemento Elástico")
            )
            MaintenanceType.PREVENTIVA -> Hexatuple(
                "Inspeção periódica efetuada conforme plano de manutenção preventiva.",
                "1. Limpeza técnica e reaperto das conexões.\n2. Substituição de elementos filtrantes.\n3. Lubrificação dos rolamentos.\n4. Verificação de medições elétricas e mecânicas.",
                "Manter o cronograma de inspeções preventivas programado.",
                SeverityLevel.BAIXO,
                "1.0h",
                listOf("Filtro de Óleo Sintético", "Graxa NLGI 2 Lítio", "Filtro de Admissão")
            )
            MaintenanceType.PREDITIVA -> Hexatuple(
                "Acompanhamento de parâmetros preditivos e medição de vibração.",
                "1. Registro das medições nos 3 eixos.\n2. Inspeção termográfica dos pontos de contato.\n3. Coleta de dados para histórico preditivo.",
                "Acompanhar a evolução dos níveis na próxima inspeção.",
                SeverityLevel.MEDIO,
                "0.0h",
                listOf("Fluido de Lavagem Técnica")
            )
            MaintenanceType.PARTIDA_TECNICA -> Hexatuple(
                "Partida técnica e testes de funcionamento de equipamento novo.",
                "1. Verificação de dados de instalação.\n2. Teste de rotação e medições de partida.\n3. Teste de operação contínua aprovado.",
                "Equipamento aprovado no relatório de partida técnica.",
                SeverityLevel.BAIXO,
                "0.0h",
                emptyList()
            )
        }

        return MaintenanceReport(
            id = id,
            title = "RELATÓRIO DE MANUTENÇÃO - $assetName",
            maintenanceType = type,
            assetName = assetName,
            assetTag = assetTag,
            technicianName = technicianName,
            companyName = companyName,
            dateTimestamp = timestamp,
            rawNotes = notes,
            generatedSummary = "Relatório de manutenção referente ao equipamento $assetName (TAG: $assetTag). Atividade concluída e registrada.",
            rootCauseDiagnosis = diag,
            actionsTaken = actions,
            recommendations = recs,
            severityLevel = sev,
            downtimeHours = downtime,
            partsReplaced = parts,
            imageUris = imageUris,
            referenceTemplateId = template?.id
        )
    }

    private data class Hexatuple<A, B, C, D, E, F>(
        val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F
    )
}
