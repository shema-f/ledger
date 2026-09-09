package com.example.myapplication.util

import com.example.myapplication.domain.model.Document
import com.example.myapplication.domain.model.DocumentCategory
import com.example.myapplication.domain.model.ExpirationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class QRCodeGeneratorTest {

    @Test
    fun `maskDocumentNumber masks numbers longer than 4 characters`() {
        val masked = QRCodeGenerator.maskDocumentNumber("DL987654321")
        assertEquals("*******4321", masked)
    }

    @Test
    fun `maskDocumentNumber returns same number if 4 characters or less`() {
        assertEquals("123", QRCodeGenerator.maskDocumentNumber("123"))
        assertEquals("1234", QRCodeGenerator.maskDocumentNumber("1234"))
    }

    @Test
    fun `generateVerificationPayload generates valid JSON with document details`() {
        val issueDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(100)
        val expiryDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(200)

        val document = Document(
            id = 1L,
            title = "State Driver License",
            documentNumber = "DL12345678",
            category = DocumentCategory.DRIVER_LICENSE,
            issueDate = issueDate,
            expiryDate = expiryDate,
            issuer = "DMV",
            notes = "Class C"
        )

        val payload = QRCodeGenerator.generateVerificationPayload(document)

        assertTrue(payload.contains("\"title\":\"State Driver License\""))
        assertTrue(payload.contains("\"type\":\"DRIVER_LICENSE\""))
        assertTrue(payload.contains("\"categoryName\":\"Driver's License\""))
        assertTrue(payload.contains("\"documentNumberMasked\":\"******5678\""))
        assertTrue(payload.contains("\"issuer\":\"DMV\""))
        assertTrue(payload.contains("\"status\":\"${ExpirationStatus.ACTIVE.name}\""))
        assertTrue(payload.contains("\"issueDate\":"))
        assertTrue(payload.contains("\"expiryDate\":"))
        assertTrue(payload.contains("\"timestamp\":"))
    }
}
