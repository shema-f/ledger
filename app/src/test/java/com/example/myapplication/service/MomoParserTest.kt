package com.example.myapplication.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MomoParserTest {

    @Test
    fun parseKinyarwandaMessage() {
        val text = "Yahawe 5,000 RWF na GAKWAYA JEAN (250788123456) ... Transaction ID: 10492819281. Umubare mushya usigaye: 84,200 RWF"
        val parsed = MomoParser.parse(text)

        assertNotNull(parsed)
        assertEquals("10492819281", parsed?.txId)
        assertEquals(5000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("GAKWAYA JEAN", parsed?.senderName)
        assertEquals("250788123456", parsed?.senderPhone)
        assertEquals(84200.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun parseEnglishMessage() {
        val text = "You have received 15,000 RWF from Jean Baptiste Rukamba (250788654321) ... TxId: 15978544. New balance: 120,000 RWF"
        val parsed = MomoParser.parse(text)

        assertNotNull(parsed)
        assertEquals("15978544", parsed?.txId)
        assertEquals(15000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("Jean Baptiste Rukamba", parsed?.senderName)
        assertEquals("250788654321", parsed?.senderPhone)
        assertEquals(120000.0, parsed?.balanceAfter ?: 0.0, 0.001)
    }

    @Test
    fun parsePushNotificationFormat() {
        val text = "Received 10000 RWF from MUKAMANZI MARIE. TxId: 987654321."
        val parsed = MomoParser.parse(text)

        assertNotNull(parsed)
        assertEquals("987654321", parsed?.txId)
        assertEquals(10000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("MUKAMANZI MARIE", parsed?.senderName)
        assertNull(parsed?.senderPhone)
        assertNull(parsed?.balanceAfter)
    }

    @Test
    fun parseAirtelMoneyMessage() {
        val text = "You have received 20,000 RWF from 250731234567 ... Transaction ID: AM12345"
        val parsed = MomoParser.parse(text)

        assertNotNull(parsed)
        assertEquals("AM12345", parsed?.txId)
        assertEquals(20000.0, parsed?.amount ?: 0.0, 0.001)
        assertNull(parsed?.senderName)
        assertEquals("250731234567", parsed?.senderPhone)
        assertNull(parsed?.balanceAfter)
    }

    @Test
    fun parseInvalidMessage_returnsNull() {
        val text = "Your daily balance is 50,000 RWF. Have a good day!"
        val parsed = MomoParser.parse(text)
        assertNull(parsed)
    }
}
