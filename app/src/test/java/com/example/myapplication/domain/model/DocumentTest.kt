package com.example.myapplication.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class DocumentTest {

    @Test
    fun `expirationStatus returns ACTIVE when expiryDate is null`() {
        val document = Document(
            title = "Test Doc",
            documentNumber = "12345",
            category = DocumentCategory.ID_CARD,
            expiryDate = null
        )
        assertEquals(ExpirationStatus.ACTIVE, document.expirationStatus)
    }

    @Test
    fun `expirationStatus returns EXPIRED when expiryDate is in past`() {
        val pastMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
        val document = Document(
            title = "Test Doc",
            documentNumber = "12345",
            category = DocumentCategory.ID_CARD,
            expiryDate = pastMillis
        )
        assertEquals(ExpirationStatus.EXPIRED, document.expirationStatus)
    }

    @Test
    fun `expirationStatus returns EXPIRING_SOON when expiryDate is within 30 days`() {
        val expiringSoonMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)
        val document = Document(
            title = "Test Doc",
            documentNumber = "12345",
            category = DocumentCategory.ID_CARD,
            expiryDate = expiringSoonMillis
        )
        assertEquals(ExpirationStatus.EXPIRING_SOON, document.expirationStatus)
    }

    @Test
    fun `expirationStatus returns ACTIVE when expiryDate is beyond 30 days`() {
        val futureMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(100)
        val document = Document(
            title = "Test Doc",
            documentNumber = "12345",
            category = DocumentCategory.ID_CARD,
            expiryDate = futureMillis
        )
        assertEquals(ExpirationStatus.ACTIVE, document.expirationStatus)
    }
}
