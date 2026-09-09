package com.example.myapplication.domain.reconciliation

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.myapplication.domain.model.Customer
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.repository.LedgerRepository
import com.example.myapplication.domain.repository.MoMoRepository
import kotlinx.coroutines.flow.first
import kotlin.math.min

class AutoReconciliationEngine(
    private val context: Context,
    private val moMoRepository: MoMoRepository,
    private val ledgerRepository: LedgerRepository
) {

    suspend fun reconcile(momoLog: MoMoLog): Boolean {
        if (momoLog.isReconciled) return false

        val customers = ledgerRepository.getCustomersSortedByDebt().first()
        if (customers.isEmpty()) return false

        var matchedCustomer: Customer? = null

        // 1. Phone matching: normalizes phone numbers and checks suffix match (last 9 digits)
        val senderPhoneSuffix = normalizePhone(momoLog.senderPhone)
        if (senderPhoneSuffix != null) {
            matchedCustomer = customers.find { customer ->
                val custPhoneSuffix = normalizePhone(customer.phoneNumber)
                custPhoneSuffix != null && custPhoneSuffix == senderPhoneSuffix
            }
        }

        // 2. Name matching: if phone matching doesn't find a customer, check fullName or nickname
        if (matchedCustomer == null) {
            val sName = momoLog.senderName?.lowercase()?.trim()
            if (!sName.isNullOrEmpty()) {
                matchedCustomer = customers.find { customer ->
                    val cName = customer.fullName.lowercase().trim()
                    val cNick = customer.nickname?.lowercase()?.trim()

                    val nameMatch = cName.isNotEmpty() && (sName.contains(cName) || cName.contains(sName))
                    val nickMatch = !cNick.isNullOrEmpty() && (sName.contains(cNick) || cNick.contains(sName))

                    nameMatch || nickMatch
                }
            }
        }

        // 3. If customer found with totalDebt > 0:
        if (matchedCustomer != null && matchedCustomer.totalDebt > 0) {
            val paymentAmount = min(momoLog.amount, matchedCustomer.totalDebt)

            // Automatically create a PAYMENT ledger record
            ledgerRepository.recordPayment(
                customerId = matchedCustomer.id,
                amount = paymentAmount,
                description = "Auto-reconciled MoMo payment (${momoLog.txId})",
                momoTxId = momoLog.txId
            )

            // Mark MoMoLog as reconciled
            if (momoLog.id != 0L) {
                moMoRepository.markReconciled(momoLog.id, true)
            } else {
                moMoRepository.markReconciledByTxId(momoLog.txId, true)
            }

            // Post local Android notification
            sendNotification(matchedCustomer, momoLog, paymentAmount)

            return true
        }

        return false
    }

    private fun normalizePhone(phone: String?): String? {
        if (phone.isNullOrBlank()) return null
        val digits = phone.filter { it.isDigit() }
        return if (digits.length >= 9) {
            digits.takeLast(9)
        } else if (digits.isNotEmpty()) {
            digits
        } else {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(matchedCustomer: Customer, momoLog: MoMoLog, paymentAmount: Double) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channelId = "momo_reconciliation_channel"
            val channel = NotificationChannel(
                channelId,
                "MoMo Payment Reconciliation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for auto-reconciled MoMo payments"
            }
            notificationManager.createNotificationChannel(channel)

            val title = "Payment Auto-Reconciled"
            val senderDisplay = momoLog.senderName ?: momoLog.senderPhone ?: "MoMo Sender"
            val message = "Received ${momoLog.amount.toInt()} RWF from $senderDisplay. Reconciled ${paymentAmount.toInt()} RWF for ${matchedCustomer.fullName}."

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(momoLog.txId.hashCode(), notification)
        } catch (_: Exception) {
            // Ignored in test environment where Android NotificationManager APIs are unmocked
        }
    }
}
