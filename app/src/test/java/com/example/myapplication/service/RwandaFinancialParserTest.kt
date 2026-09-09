package com.example.myapplication.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RwandaFinancialParserTest {

    @Test
    fun testMtnMomoEnglishInbound() {
        val text = "You have received 50,000 RWF from JEAN CLAUDE (250788123456) on your mobile money account at 2023-10-25 10:00:00. Financial Transaction Id: 123456789. New balance: 150,000 RWF."
        val parsed = RwandaFinancialParser.parse(text)

        assertNotNull(parsed)
        assertEquals("MTN MoMo", parsed?.provider)
        assertEquals(50000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("123456789", parsed?.txReference)
        assertEquals("JEAN CLAUDE (250788123456)", parsed?.senderOrRecipient)
        assertEquals(150000.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun testMtnMomoKinyarwandaInbound() {
        val text = "Yahawe 50,000 RWF kuva ku JEAN CLAUDE (250788123456) kuwa 2023-10-25. TxId: 123456789. Umubare mushya usigaye: 150,000 RWF."
        val parsed = RwandaFinancialParser.parse(text)

        assertNotNull(parsed)
        assertEquals("MTN MoMo", parsed?.provider)
        assertEquals(50000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("123456789", parsed?.txReference)
        assertEquals("JEAN CLAUDE (250788123456)", parsed?.senderOrRecipient)
        assertEquals(150000.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun testBkAlerts() {
        val text = "Credit Txn: A/C *1234... Amt: RWF 50,000... Ref: BK987654... Bal: RWF 450,000"
        val parsed = RwandaFinancialParser.parse(text)

        assertNotNull(parsed)
        assertEquals("Bank of Kigali", parsed?.provider)
        assertEquals(50000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("BK987654", parsed?.txReference)
        assertEquals("A/C *1234", parsed?.senderOrRecipient)
        assertEquals(450000.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun testAirtelMoney() {
        val text = "You have received RWF 12,000 from JEAN CLAUDE... Txn ID: AM54321"
        val parsed = RwandaFinancialParser.parse(text)

        assertNotNull(parsed)
        assertEquals("Airtel Money", parsed?.provider)
        assertEquals(12000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("AM54321", parsed?.txReference)
        assertEquals("JEAN CLAUDE", parsed?.senderOrRecipient)
        assertNull(parsed?.balanceAfter)
    }

    @Test
    fun testImBankAlert() {
        val text = "Credit alert: Your A/C *5678 has been credited with RWF 75,000 from ALICE M. Ref: IM123456. Available Bal: RWF 200,000"
        val parsed = RwandaFinancialParser.parse(text)

        assertNotNull(parsed)
        assertEquals("I&M Bank", parsed?.provider)
        assertEquals(75000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("IM123456", parsed?.txReference)
        assertEquals("ALICE M", parsed?.senderOrRecipient)
        assertEquals(200000.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun testMissingFields() {
        val noRefText = "You have received RWF 50,000 from JEAN CLAUDE"
        assertNull(RwandaFinancialParser.parse(noRefText))

        val nonFinancialText = "Hello, your monthly account statement is now available."
        assertNull(RwandaFinancialParser.parse(nonFinancialText))

        assertNull(RwandaFinancialParser.parse(""))
    }

    @Test
    fun testCleanDoubleHelper() {
        assertEquals(50000.0, "50,000".cleanDouble(), 0.001)
        assertEquals(1250000.50, "1,250,000.50".cleanDouble(), 0.001)
        assertEquals(75000.0, "RWF 75,000".cleanDouble(), 0.001)
        assertEquals(0.0, "".cleanDouble(), 0.001)
        assertEquals(0.0, "N/A".cleanDouble(), 0.001)
    }
}
