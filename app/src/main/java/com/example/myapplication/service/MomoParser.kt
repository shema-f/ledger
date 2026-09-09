package com.example.myapplication.service

data class ParsedMoMoTx(
    val txId: String,
    val amount: Double,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val balanceAfter: Double? = null,
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis()
)

object MomoParser {

    fun parse(rawText: String, timestamp: Long = System.currentTimeMillis()): ParsedMoMoTx? {
        if (rawText.isBlank()) return null

        val financialSms = RwandaFinancialParser.parse(rawText, timestamp)
        if (financialSms != null) {
            var senderName: String? = financialSms.senderOrRecipient
            var senderPhone: String? = null

            val phoneMatch = Regex("\\(([^)]+)\\)").find(financialSms.senderOrRecipient)
            if (phoneMatch != null) {
                senderPhone = phoneMatch.groupValues[1].trim()
                senderName = financialSms.senderOrRecipient.substringBefore("(").trim().ifEmpty { null }
            } else if (Regex("^\\+?[0-9]{8,15}$").matches(financialSms.senderOrRecipient)) {
                senderPhone = financialSms.senderOrRecipient
                senderName = null
            }

            return ParsedMoMoTx(
                txId = financialSms.txReference,
                amount = financialSms.amount,
                senderName = senderName,
                senderPhone = senderPhone,
                balanceAfter = financialSms.balanceAfter,
                rawText = rawText,
                timestamp = timestamp
            )
        }

        return null
    }
}
