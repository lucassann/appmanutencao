package com.manutencao.agente.data.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.manutencao.agente.data.model.MaintenanceReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportExporter(private val context: Context) {

    fun generatePdfReport(report: MaintenanceReport): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Standard (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val formattedDate = dateFormat.format(Date(report.dateTimestamp))
        val osNumber = "OS-${report.id.take(8).uppercase()}"

        // 1. Moldura Externa do Documento Oficial (Estilo Prontuário Técnico)
        paint.color = Color.parseColor("#1E293B")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRect(20f, 20f, 575f, 822f, paint)

        // 2. Cabeçalho Principal (Tabela do Topo)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0F172A") // Navy Escuro Institucional
        canvas.drawRect(20f, 20f, 575f, 75f, paint)

        // Texto Nome da Empresa
        paint.color = Color.WHITE
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText(report.companyName.uppercase(), 35f, 45f, paint)

        paint.textSize = 9f
        paint.color = Color.parseColor("#94A3B8")
        paint.isFakeBoldText = false
        canvas.drawText("SISTEMA INTEGRADO DE ENGENHARIA DE MANUTENÇÃO", 35f, 62f, paint)

        // Texto Direita do Cabeçalho
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("ORDEM DE SERVIÇO: $osNumber", 380f, 43f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawText("EMISSÃO: $formattedDate", 380f, 60f, paint)

        // 3. Tarja do Tipo de Intervenção
        var currentY = 90f
        val typeHeaderColor = Color.parseColor(report.maintenanceType.primaryColorHex)
        paint.color = typeHeaderColor
        canvas.drawRect(20f, currentY, 575f, currentY + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("LAUDO TÉCNICO DE MANUTENÇÃO - ${report.maintenanceType.title.uppercase()}", 30f, currentY + 15f, paint)

        // 4. Ficha Técnica do Ativo (Grade de Atributos)
        currentY += 32f
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRect(20f, currentY, 575f, currentY + 75f, paint)

        paint.color = Color.parseColor("#334155")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        canvas.drawRect(20f, currentY, 575f, currentY + 75f, paint)
        canvas.drawLine(297f, currentY, 297f, currentY + 75f, paint) // Linha central vertical

        paint.style = Paint.Style.FILL
        paint.textSize = 9.5f

        // Coluna Esquerda da Ficha
        drawField(canvas, paint, "EQUIPAMENTO / ATIVO:", report.assetName, 30f, currentY + 20f)
        drawField(canvas, paint, "TAG / CÓDIGO DO ATIVO:", report.assetTag, 30f, currentY + 42f)
        drawField(canvas, paint, "TEMPO DE PARADA (DOWNTIME):", report.downtimeHours, 30f, currentY + 64f)

        // Coluna Direita da Ficha
        drawField(canvas, paint, "TÉCNICO EXECUTA NTE:", report.technicianName, 310f, currentY + 20f)
        drawField(canvas, paint, "CRITICIDADE / SEVERIDADE:", report.severityLevel.label.uppercase(), 310f, currentY + 42f)
        drawField(canvas, paint, "STATUS DA ORDEM DE SERVIÇO:", "CONCLUÍDO / APROVADO", 310f, currentY + 64f)

        // 5. Seções Formais de Engenharia
        currentY += 95f
        currentY = drawEngineeringBlock(canvas, paint, "1. CONDIÇÃO DE CAMPO & DIAGNÓSTICO INICIAL", report.generatedSummary, currentY)
        currentY = drawEngineeringBlock(canvas, paint, "2. LAUDO DE CAUSA RAIZ / ANÁLISE DE FALHA", report.rootCauseDiagnosis, currentY)
        currentY = drawEngineeringBlock(canvas, paint, "3. DETALHAMENTO DOS SERVIÇOS EXECUTADOS", report.actionsTaken, currentY)
        currentY = drawEngineeringBlock(canvas, paint, "4. RECOMENDAÇÕES TÉCNICAS E MONITORAMENTO", report.recommendations, currentY)

        // 6. Tabela de Peças & Insumos Aplicados
        currentY += 10f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("5. RELAÇÃO DE COMPONENTES E MATERIAIS SUBSTITUÍDOS", 30f, currentY, paint)

        currentY += 8f
        // Cabeçalho da Tabela
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(30f, currentY, 565f, currentY + 18f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.isFakeBoldText = true
        canvas.drawText("ITEM", 40f, currentY + 13f, paint)
        canvas.drawText("DESCRIÇÃO DO COMPONENTE / PEÇA", 90f, currentY + 13f, paint)
        canvas.drawText("SITUAÇÃO / APLICAÇÃO", 440f, currentY + 13f, paint)

        currentY += 18f
        paint.isFakeBoldText = false
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#334155")

        if (report.partsReplaced.isEmpty()) {
            canvas.drawText("01", 40f, currentY + 14f, paint)
            canvas.drawText("Nenhum material de reposição foi demandado nesta intervenção.", 90f, currentY + 14f, paint)
            canvas.drawText("Inspeção OK", 440f, currentY + 14f, paint)
            currentY += 20f
        } else {
            report.partsReplaced.take(4).forEachIndexed { idx, part ->
                val bgCol = if (idx % 2 == 0) "#FFFFFF" else "#F8FAFC"
                paint.color = Color.parseColor(bgCol)
                canvas.drawRect(30f, currentY, 565f, currentY + 18f, paint)

                paint.color = Color.parseColor("#334155")
                canvas.drawText(String.format("%02d", idx + 1), 40f, currentY + 13f, paint)
                canvas.drawText(part, 90f, currentY + 13f, paint)
                canvas.drawText("Substituído", 440f, currentY + 13f, paint)
                currentY += 18f
            }
        }

        // 7. Termo de Responsabilidade & Bloco de Assinaturas
        val signatureY = 745f

        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(30f, signatureY - 15f, 565f, signatureY + 55f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        canvas.drawText("Atestamos que os serviços técnicos acima discriminados foram executados conforme os padrões normativos de segurança e engenharia.", 40f, signatureY - 3f, paint)

        // Linhas de Assinatura
        paint.color = Color.parseColor("#64748B")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f

        canvas.drawLine(50f, signatureY + 32f, 260f, signatureY + 32f, paint)
        canvas.drawLine(335f, signatureY + 32f, 545f, signatureY + 32f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#0F172A")

        canvas.drawText(report.technicianName.uppercase(), 70f, signatureY + 43f, paint)
        canvas.drawText("SUPERVISÃO / ACEITE DO CLIENTE", 355f, signatureY + 43f, paint)

        paint.textSize = 7.5f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Técnico Executante / CFT-CREA", 75f, signatureY + 52f, paint)
        canvas.drawText("Assinatura e Carimbo", 390f, signatureY + 52f, paint)

        pdfDocument.finishPage(page)

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val pdfFile = File(outputDir, "Laudo_${report.assetTag}_${report.id.take(6)}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawField(canvas: Canvas, paint: Paint, label: String, value: String, x: Float, y: Float) {
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#475569")
        paint.textSize = 8.5f
        canvas.drawText(label, x, y, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#0F172A")
        val labelWidth = paint.measureText(label)
        canvas.drawText(value, x + labelWidth + 5f, y, paint)
    }

    private fun drawEngineeringBlock(canvas: Canvas, paint: Paint, title: String, content: String, startY: Float): Float {
        var y = startY
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText(title, 30f, y, paint)

        y += 12f
        paint.color = Color.parseColor("#334155")
        paint.textSize = 8.5f
        paint.isFakeBoldText = false

        val maxWordsPerLine = 16
        val words = content.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.split(" ").size >= maxWordsPerLine) {
                canvas.drawText(currentLine.toString(), 40f, y, paint)
                y += 11f
                currentLine = StringBuilder()
            }
            currentLine.append(word).append(" ")
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), 40f, y, paint)
            y += 11f
        }

        return y + 10f
    }

    fun getShareableUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
