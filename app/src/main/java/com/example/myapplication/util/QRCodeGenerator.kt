package com.example.myapplication.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.myapplication.domain.model.Document
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object QRCodeGenerator {

    /**
     * Generates a QR Code Bitmap for the given content string and target size in pixels.
     */
    fun generateQrCode(content: String, sizePx: Int = 512): Bitmap {
        require(sizePx > 0) { "sizePx must be greater than 0" }
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            hints
        )
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Generates a structured JSON string payload containing verification data for a document.
     */
    fun generateVerificationPayload(document: Document): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val maskedNumber = maskDocumentNumber(document.documentNumber)
        val issueStr = document.issueDate?.let { "\"issueDate\":\"${dateFormat.format(Date(it))}\"," } ?: ""
        val expiryStr = document.expiryDate?.let { "\"expiryDate\":\"${dateFormat.format(Date(it))}\"," } ?: ""
        val issuerStr = if (document.issuer.isNotBlank()) "\"issuer\":\"${escapeJson(document.issuer)}\"," else ""
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        return "{" +
                "\"title\":\"${escapeJson(document.title)}\"," +
                "\"type\":\"${document.category.name}\"," +
                "\"categoryName\":\"${escapeJson(document.category.displayName)}\"," +
                "\"documentNumberMasked\":\"${escapeJson(maskedNumber)}\"," +
                issueStr +
                expiryStr +
                issuerStr +
                "\"status\":\"${document.expirationStatus.name}\"," +
                "\"timestamp\":\"$timestamp\"" +
                "}"
    }

    /**
     * Masks the document number, exposing only the last 4 characters.
     */
    fun maskDocumentNumber(number: String): String {
        if (number.length <= 4) return number
        val visibleChars = 4
        val maskedLength = number.length - visibleChars
        return "*".repeat(maskedLength) + number.takeLast(visibleChars)
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
