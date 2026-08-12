package com.manutencao.agente.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size standard (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val formattedDate = dateFormat.format(Date(report.dateTimestamp))

        // 1. Cabeçalho Superior Elegante
        paint.color = Color.parseColor("#1E293B") // Slate Dark Navy
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Nome da Empresa
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(report.companyName.uppercase(), 30f, 38f, paint)

        // Título do Relatório
        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText(report.title, 30f, 62f, paint)

        // Badge do Tipo de Manutenção
        val typeBadgeColor = Color.parseColor(report.maintenanceType.primaryColorHex)
        paint.color = typeBadgeColor
        val badgeRect = RectF(400f, 25f, 565f, 55f)
        canvas.drawRoundRect(badgeRect, 15f, 15f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText(report.maintenanceType.title.uppercase(), 412f, 44f, paint)

        // 2. Tabela de Metadados / Informações Básicas
        var currentY = 120f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(RectF(30f, currentY, 565f, currentY + 70f), 8f, 8f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.isFakeBoldText = true

        canvas.drawText("Equipamento / Ativo:", 45f, currentY + 25f, paint)
        canvas.drawText("TAG / Identificação:", 45f, currentY + 50f, paint)
        canvas.drawText("Técnico Responsável:", 300f, currentY + 25f, paint)
        canvas.drawText("Data de Emissão:", 300f, currentY + 50f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(report.assetName, 155f, currentY + 25f, paint)
        canvas.drawText(report.assetTag, 155f, currentY + 50f, paint)
        canvas.drawText(report.technicianName, 415f, currentY + 25f, paint)
        canvas.drawText(formattedDate, 415f, currentY + 50f, paint)

        // 3. Nível de Severidade & Downtime
        currentY += 90f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("GRAVIDADE DA OCORRÊNCIA:", 30f, currentY, paint)

        val sevColor = Color.parseColor(report.severityLevel.colorHex)
        paint.color = sevColor
        canvas.drawRoundRect(RectF(210f, currentY - 14f, 290f, currentY + 6f), 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        canvas.drawText(report.severityLevel.label, 225f, currentY, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11f
        canvas.drawText("Tempo de Parada (Downtime): ${report.downtimeHours}", 330f, currentY, paint)

        // Divisor
        currentY += 20f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(30f, currentY, 565f, currentY, paint)

        // 4. Seções de Diagnóstico e Ações
        currentY += 30f
        drawSection(canvas, paint, "1. RESUMO EXECUTIVO & DIAGNÓSTICO", report.generatedSummary, 30f, currentY)

        currentY += 85f
        drawSection(canvas, paint, "2. ANÁLISE DE CAUSA RAIZ", report.rootCauseDiagnosis, 30f, currentY)

        currentY += 85f
        drawSection(canvas, paint, "3. AÇÕES EXECUTADAS EM CAMPO", report.actionsTaken, 30f, currentY)

        currentY += 85f
        drawSection(canvas, paint, "4. RECOMENDAÇÕES TÉCNICAS E PREVENTIVAS", report.recommendations, 30f, currentY)

        // 5. Lista de Peças Trocadas
        currentY += 90f
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("5. COMPONENTES E MATERIAIS UTILIZADOS:", 30f, currentY, paint)

        currentY += 15f
        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = Color.parseColor("#334155")

        if (report.partsReplaced.isEmpty()) {
            canvas.drawText("• Nenhum componente foi substituído nesta intervenção.", 40f, currentY, paint)
        } else {
            report.partsReplaced.take(3).forEach { part ->
                canvas.drawText("• $part", 40f, currentY, paint)
                currentY += 14f
            }
        }

        // 6. Campo de Assinatura no Rodapé
        val footerY = 790f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(350f, footerY, 545f, footerY, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Assinatura do Técnico Responsável", 370f, footerY + 15f, paint)
        canvas.drawText("Documento Gerado por Agente de IA Manutenção", 30f, footerY + 15f, paint)

        pdfDocument.finishPage(page)

        // Salvar Arquivo no Armazenamento Local
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val pdfFile = File(outputDir, "Relatorio_${report.assetTag}_${report.id.take(8)}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun drawSection(canvas: Canvas, paint: Paint, title: String, content: String, x: Float, y: Float) {
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText(title, x, y, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 9.5f
        paint.isFakeBoldText = false

        // Quebra automática de texto simples
        val maxWordsPerLine = 14
        val words = content.split(" ")
        var lineY = y + 16f
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.split(" ").size >= maxWordsPerLine) {
                canvas.drawText(currentLine.toString(), x + 10f, lineY, paint)
                lineY += 13f
                currentLine = StringBuilder()
            }
            currentLine.append(word).append(" ")
        }
        if (currentLine.isNotEmpty() && lineY < y + 75f) {
            canvas.drawText(currentLine.toString(), x + 10f, lineY, paint)
        }
    }

    fun getShareableUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
