package com.example.myapplication.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.myapplication.data.local.ImariDatabase
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.data.repository.MoMoRepositoryImpl
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.reconciliation.AutoReconciliationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MomoSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val db = ImariDatabase.getInstance(context.applicationContext)
                val moMoRepo = MoMoRepositoryImpl(db.moMoLogDao())
                val ledgerRepo = LedgerRepositoryImpl(db.customerDao(), db.ledgerRecordDao())
                val autoReconciliationEngine = AutoReconciliationEngine(
                    context = context.applicationContext,
                    moMoRepository = moMoRepo,
                    ledgerRepository = ledgerRepo
                )

                for (sms in messages) {
                    val body = sms.messageBody ?: continue
                    val sender = sms.displayOriginatingAddress ?: sms.originatingAddress
                    val timestamp = sms.timestampMillis

                    val parsedSms = RwandaFinancialParser.parse(sender, body, timestamp) ?: continue

                    if (moMoRepo.getMoMoLogByTxId(parsedSms.txReference) != null) continue

                    val momoLog = MoMoLog(
                        senderName = parsedSms.senderOrRecipient,
                        senderPhone = null,
                        amount = parsedSms.amount,
                        txId = parsedSms.txReference,
                        balanceAfter = parsedSms.balanceAfter,
                        rawText = body,
                        timestamp = parsedSms.timestamp,
                        isReconciled = false
                    )

                    val logId = moMoRepo.addMoMoLog(momoLog)
                    val savedLog = momoLog.copy(id = logId)

                    // Broadcast live alert state for interactive UI bottom sheet prompt
                    PaymentAlertManager.notifyAlert(parsedSms)

                    autoReconciliationEngine.reconcile(savedLog)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
