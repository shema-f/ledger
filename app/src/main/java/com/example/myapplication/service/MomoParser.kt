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

    private val AMOUNT_PATTERN = Regex(
        "(?i)(?:Yahawe|You have received|Received)\\s+([0-9,]+(?:\\.[0-9]+)?)\\s*RWF"
    )
    private val FALLBACK_AMOUNT_PATTERN = Regex(
        "([0-9,]+(?:\\.[0-9]+)?)\\s*RWF"
    )

    private val TX_ID_PATTERN = Regex(
        "(?i)(?:Transaction ID|TxId|TxID|Txid)[:\\s]*([A-Za-z0-9]+)"
    )

    private val BALANCE_PATTERN = Regex(
        "(?i)(?:New balance|Umubare mushya usigaye)[:\\s]*([0-9,]+(?:\\.[0-9]+)?)\\s*RWF"
    )

    private val SENDER_WITH_PHONE_PATTERN = Regex(
        "(?i)(?:na|from)\\s+([A-Za-z\\s'\\-]+)\\s*\\(([^)]+)\\)"
    )

    private val SENDER_PHONE_ONLY_PATTERN = Regex(
        "(?i)(?:na|from)\\s+(\\+?[0-9\\s]{8,15})(?=\\s|\\.|,|$|\\.\\.\\.)"
    )

    private val SENDER_NAME_ONLY_PATTERN = Regex(
        "(?i)(?:na|from)\\s+([A-Za-z\\s'\\-]+?)(?=\\.|,|\\s+TxId|\\s+Transaction|\\s+\\.\\.\\.|\\s*$)"
    )

    fun parse(rawText: String, timestamp: Long = System.currentTimeMillis()): ParsedMoMoTx? {
        if (rawText.isBlank()) return null

        // 1. Extract Amount
        val amountMatch = AMOUNT_PATTERN.find(rawText) ?: FALLBACK_AMOUNT_PATTERN.find(rawText) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(",", "")
        val amount = amountStr.toDoubleOrNull() ?: return null

        // 2. Extract TxId
        val txIdMatch = TX_ID_PATTERN.find(rawText) ?: return null
        val txId = txIdMatch.groupValues[1].trim()
        if (txId.isBlank()) return null

        // 3. Extract Balance After
        val balanceMatch = BALANCE_PATTERN.find(rawText)
        val balanceAfter = balanceMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()

        // 4. Extract Sender Name & Sender Phone
        var senderName: String? = null
        var senderPhone: String? = null

        val withPhoneMatch = SENDER_WITH_PHONE_PATTERN.find(rawText)
        if (withPhoneMatch != null) {
            senderName = withPhoneMatch.groupValues[1].trim().ifEmpty { null }
            senderPhone = withPhoneMatch.groupValues[2].trim().ifEmpty { null }
        } else {
            val phoneOnlyMatch = SENDER_PHONE_ONLY_PATTERN.find(rawText)
            if (phoneOnlyMatch != null) {
                senderPhone = phoneOnlyMatch.groupValues[1].trim().ifEmpty { null }
            } else {
                val nameOnlyMatch = SENDER_NAME_ONLY_PATTERN.find(rawText)
                if (nameOnlyMatch != null) {
                    senderName = nameOnlyMatch.groupValues[1].trim().ifEmpty { null }
                }
            }
        }

        return ParsedMoMoTx(
            txId = txId,
            amount = amount,
            senderName = senderName,
            senderPhone = senderPhone,
            balanceAfter = balanceAfter,
            rawText = rawText,
            timestamp = timestamp
        )
    }
}
