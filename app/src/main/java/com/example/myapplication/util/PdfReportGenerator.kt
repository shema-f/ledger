package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PdfReportData(
    val shopName: String,
    val dateRangeText: String,
    val grossRevenue: Double,
    val cogs: Double,
    val grossProfit: Double,
    val walletBalances: List<Pair<String, Double>>,
    val uncollectedDebts: Double,
    val outstandingLoans: Double
)

object PdfReportGenerator {

    fun generatePdfReport(
        context: Context,
        data: PdfReportData
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Portrait (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 18f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 12f
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            textSize = 14f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(51, 65, 85) // Slate 700
            textSize = 12f
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 12f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            strokeWidth = 1f
        }

        val bgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
        }

        var yPos = 45f
        val margin = 40f
        val contentWidth = 515f

        // Header Section with Abstract Geometric Emblem
        val emblemSize = 32f
        val emblemRect = RectF(margin, yPos - 5f, margin + emblemSize, yPos + emblemSize - 5f)

        val emblemBgPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val emblemAccPaint1 = Paint().apply {
            color = Color.rgb(16, 185, 129) // Emerald Accent
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val emblemAccPaint2 = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber Gold Accent
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val logoResId = context.resources.getIdentifier("ic_ifaranga_logo", "drawable", context.packageName).let {
            if (it != 0) it else context.resources.getIdentifier("ic_imari_logo", "drawable", context.packageName)
        }
        val logoDrawable = if (logoResId != 0) ContextCompat.getDrawable(context, logoResId) else null
        if (logoDrawable != null) {
            logoDrawable.setBounds(margin.toInt(), yPos.toInt() - 5, (margin + emblemSize).toInt(), (yPos + emblemSize - 5f).toInt())
            logoDrawable.draw(canvas)
        } else {
            // Draw Abstract Geometric Emblem
            canvas.drawRoundRect(emblemRect, 6f, 6f, emblemBgPaint)
            val path1 = Path().apply {
                moveTo(margin + 8f, yPos + 22f)
                lineTo(margin + 16f, yPos + 6f)
                lineTo(margin + 24f, yPos + 22f)
                close()
            }
            canvas.drawPath(path1, emblemAccPaint1)
            val path2 = Path().apply {
                moveTo(margin + 16f, yPos + 10f)
                lineTo(margin + 26f, yPos + 24f)
                lineTo(margin + 6f, yPos + 24f)
                close()
            }
            canvas.drawPath(path2, emblemAccPaint2)
        }

        canvas.drawText("IFARANGA", margin + emblemSize + 12f, yPos + 10f, titlePaint)
        canvas.drawText("Know your money. / Amafaranga yawe. Uyamenye.", margin + emblemSize + 12f, yPos + 24f, subtitlePaint)
        yPos += 38f

        canvas.drawText("Merchant Shop Name: ${data.shopName.ifBlank { "Smart Merchant" }}", margin, yPos, subtitlePaint)
        yPos += 18f
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Period: ${data.dateRangeText}  |  Generated: $timestamp", margin, yPos, subtitlePaint)

        yPos += 15f
        canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        yPos += 25f

        val formatter = NumberFormat.getNumberInstance(Locale.US)

        // Section 1: Financial Summary Table (P&L)
        canvas.drawText("1. Financial Summary Statement", margin, yPos, sectionHeaderPaint)
        yPos += 15f

        // Table Header
        canvas.drawRect(margin, yPos, margin + contentWidth, yPos + 24f, bgPaint)
        canvas.drawText("Financial Metric", margin + 12f, yPos + 16f, boldTextPaint)
        canvas.drawText("Amount (RWF)", margin + 340f, yPos + 16f, boldTextPaint)
        yPos += 24f

        val financialRows = listOf(
            "Gross Revenue" to data.grossRevenue,
            "Cost of Goods Sold (COGS)" to data.cogs,
            "Gross Operating Profit" to data.grossProfit
        )

        for ((metricName, amount) in financialRows) {
            val isProfit = metricName.contains("Profit")
            val p = if (isProfit) boldTextPaint else textPaint
            canvas.drawText(metricName, margin + 12f, yPos + 18f, p)
            val formatted = "${formatter.format(amount)} RWF"
            canvas.drawText(formatted, margin + 340f, yPos + 18f, p)
            yPos += 24f
            canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        }

        yPos += 25f

        // Section 2: Wallet Balances Table
        canvas.drawText("2. Wallet & Bank Account Balances", margin, yPos, sectionHeaderPaint)
        yPos += 15f

        canvas.drawRect(margin, yPos, margin + contentWidth, yPos + 24f, bgPaint)
        canvas.drawText("Account / Wallet Name", margin + 12f, yPos + 16f, boldTextPaint)
        canvas.drawText("Current Balance (RWF)", margin + 340f, yPos + 16f, boldTextPaint)
        yPos += 24f

        val wallets = data.walletBalances.ifEmpty {
            listOf(
                "MTN MoMo" to 0.0,
                "Bank of Kigali (BK)" to 0.0,
                "Cash Drawer" to 0.0,
                "Airtel Money" to 0.0,
                "I&M Bank" to 0.0,
                "Equity Bank" to 0.0
            )
        }

        for ((walletName, balance) in wallets) {
            canvas.drawText(walletName, margin + 12f, yPos + 18f, textPaint)
            val formatted = "${formatter.format(balance)} RWF"
            canvas.drawText(formatted, margin + 340f, yPos + 18f, textPaint)
            yPos += 24f
            canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        }

        yPos += 25f

        // Section 3: Customer Debts & Business Loans Summary
        canvas.drawText("3. Debts & Business Loans Summary", margin, yPos, sectionHeaderPaint)
        yPos += 15f

        canvas.drawRect(margin, yPos, margin + contentWidth, yPos + 24f, bgPaint)
        canvas.drawText("Liability / Receivable Type", margin + 12f, yPos + 16f, boldTextPaint)
        canvas.drawText("Outstanding Amount (RWF)", margin + 340f, yPos + 16f, boldTextPaint)
        yPos += 24f

        val debtRows = listOf(
            "Uncollected Customer Debts (Receivables)" to data.uncollectedDebts,
            "Outstanding Business Loans (Payables)" to data.outstandingLoans
        )

        for ((label, amount) in debtRows) {
            canvas.drawText(label, margin + 12f, yPos + 18f, textPaint)
            val formatted = "${formatter.format(amount)} RWF"
            canvas.drawText(formatted, margin + 340f, yPos + 18f, textPaint)
            yPos += 24f
            canvas.drawLine(margin, yPos, margin + contentWidth, yPos, linePaint)
        }

        pdfDocument.finishPage(page)

        val reportsDir = try {
            val extDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (extDir != null && (extDir.exists() || extDir.mkdirs())) {
                extDir
            } else {
                File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
            }
        } catch (e: Exception) {
            File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        }

        val reportFile = File(reportsDir, "IFARANGA_Financial_Statement_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(reportFile)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()

        return reportFile
    }

    fun sharePdfReport(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share IFARANGA Financial Statement PDF"))
    }
}
