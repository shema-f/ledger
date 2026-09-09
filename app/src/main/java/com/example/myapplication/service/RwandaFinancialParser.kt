package com.example.myapplication.service

data class FinancialSms(
    val provider: String,
    val amount: Double,
    val txReference: String,
    val senderOrRecipient: String,
    val balanceAfter: Double?,
    val timestamp: Long
)

fun String.cleanDouble(): Double {
    if (this.isBlank()) return 0.0
    val withoutCommas = this.replace(",", "")
    val match = Regex("([0-9]+(?:\\.[0-9]+)?)").find(withoutCommas)
    return match?.value?.toDoubleOrNull() ?: 0.0
}

object RwandaFinancialParser {

    fun parse(sender: String?, body: String, timestamp: Long = System.currentTimeMillis()): FinancialSms? {
        if (body.isBlank()) return null

        val provider = determineProvider(sender, body) ?: return null

        val txRef = extractTxReference(body, provider) ?: return null
        val amount = extractAmount(body, provider)
        if (amount <= 0.0) return null

        val senderOrRecipient = extractSenderOrRecipient(body, provider)
        val balanceAfter = extractBalanceAfter(body, provider)

        return FinancialSms(
            provider = provider,
            amount = amount,
            txReference = txRef,
            senderOrRecipient = senderOrRecipient,
            balanceAfter = balanceAfter,
            timestamp = timestamp
        )
    }

    fun parse(body: String, timestamp: Long = System.currentTimeMillis()): FinancialSms? {
        return parse(null, body, timestamp)
    }

    private fun determineProvider(sender: String?, body: String): String? {
        val s = sender?.uppercase() ?: ""
        val b = body.uppercase()

        if (s.contains("MTN") || s.contains("MOMO") || s.contains("184")) return "MTN MoMo"
        if (s.contains("AIRTEL") || s.contains("AIRTELMONEY")) return "Airtel Money"
        if (s.contains("BK") || s.contains("BANK OF KIGALI") || s.contains("BKALERTS")) return "Bank of Kigali"
        if (s.contains("I&M") || s.contains("IMBANK") || s.contains("IANDM")) return "I&M Bank"

        return when {
            b.contains("MOMO") || b.contains("YAHAWE") || b.contains("MOBILE MONEY") ||
                    (b.contains("YOU HAVE RECEIVED") && (b.contains("25078") || b.contains("FINANCIAL TRANSACTION ID") || b.contains("MOMO"))) -> "MTN MoMo"

            b.contains("AIRTEL") || b.contains("AM54321") || b.contains("TXN ID: AM") || Regex("(?i)TXN ID:\\s*AM[0-9]+").containsMatchIn(body) -> "Airtel Money"

            b.contains("BANK OF KIGALI") || b.contains("CREDIT TXN: A/C") || b.contains("BK ALERTS") ||
                    b.contains("REF: BK") || Regex("(?i)REF:\\s*BK[0-9]+").containsMatchIn(body) -> "Bank of Kigali"

            b.contains("I&M") || b.contains("IM123456") || b.contains("REF: IM") || Regex("(?i)REF:\\s*IM[0-9]+").containsMatchIn(body) -> "I&M Bank"

            b.contains("YOU HAVE RECEIVED") || b.contains("RECEIVED") -> "MTN MoMo"
            else -> null
        }
    }

    private fun extractTxReference(body: String, provider: String): String? {
        val patterns = listOf(
            Regex("(?i)(?:Financial Transaction Id|Transaction ID|Txn ID|TxId|TxID|Txid|Ref ID|Ref)[:\\s]*([A-Za-z0-9]+)"),
            Regex("(?i)(?:Ref:?\\s*)([A-Za-z0-9]+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val ref = match.groupValues[1].trim()
                if (ref.isNotBlank()) return ref
            }
        }
        return null
    }

    private fun extractAmount(body: String, provider: String): Double {
        val patterns = listOf(
            Regex("(?i)(?:Amt|Amt:|Amt\\.|Amount|Amount:|credited with|received|Yahawe|You have received|Received)[:\\s]*(?:RWF\\s*)?([0-9,]+(?:\\.[0-9]+)?)"),
            Regex("(?i)(?:RWF\\s*)([0-9,]+(?:\\.[0-9]+)?)"),
            Regex("(?i)([0-9,]+(?:\\.[0-9]+)?)\\s*RWF")
        )
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val valStr = match.groupValues[1]
                val amount = valStr.cleanDouble()
                if (amount > 0.0) return amount
            }
        }
        return 0.0
    }

    private fun extractSenderOrRecipient(body: String, provider: String): String {
        val withPhonePattern = Regex("(?i)(?:from|kuva ku|kuva kuri|na)\\s+([A-Za-z0-9\\s'\\-]+?)\\s*\\(([^)]+)\\)")
        val withPhoneMatch = withPhonePattern.find(body)
        if (withPhoneMatch != null) {
            val name = withPhoneMatch.groupValues[1].trim()
            val phone = withPhoneMatch.groupValues[2].trim()
            return "$name ($phone)"
        }

        val namePattern = Regex("(?i)(?:from|kuva ku|kuva kuri|na)\\s+([A-Za-z0-9\\s'\\-]+?)(?=\\.|,|\\s+on|\\s+at|\\s+kuwa|\\s+Tx|\\s+Transaction|\\s+Txn|\\s+Ref|\\s*$)")
        val nameMatch = namePattern.find(body)
        if (nameMatch != null) {
            val name = nameMatch.groupValues[1].trim()
            if (name.isNotBlank()) return name
        }

        val acPattern = Regex("(?i)(A/C\\s*\\*?[0-9xX]+)")
        val acMatch = acPattern.find(body)
        if (acMatch != null) {
            return acMatch.groupValues[1].trim()
        }

        return provider
    }

    private fun extractBalanceAfter(body: String, provider: String): Double? {
        val patterns = listOf(
            Regex("(?i)(?:New balance|New Balance|Umubare mushya usigaye|Available Bal|Bal|Balance)[:\\s]*(?:RWF\\s*)?([0-9,]+(?:\\.[0-9]+)?)"),
            Regex("(?i)(?:Bal|Balance)[:\\s]*([0-9,]+(?:\\.[0-9]+)?)")
        )
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                val str = match.groupValues[1]
                val bal = str.cleanDouble()
                if (bal >= 0.0) return bal
            }
        }
        return null
    }
}
