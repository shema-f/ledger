package com.example.myapplication.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.myapplication.data.local.KayiDatabase
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
                val db = KayiDatabase.getInstance(context.applicationContext)
                val moMoRepo = MoMoRepositoryImpl(db.moMoLogDao())
                val ledgerRepo = LedgerRepositoryImpl(db.customerDao(), db.ledgerRecordDao())
                val autoReconciliationEngine = AutoReconciliationEngine(
                    context = context.applicationContext,
                    moMoRepository = moMoRepo,
                    ledgerRepository = ledgerRepo
                )

                for (sms in messages) {
                    val body = sms.messageBody ?: continue
                    val timestamp = sms.timestampMillis

                    val parsed = MomoParser.parse(body, timestamp) ?: continue

                    if (moMoRepo.getMoMoLogByTxId(parsed.txId) != null) continue

                    val momoLog = MoMoLog(
                        senderName = parsed.senderName,
                        senderPhone = parsed.senderPhone,
                        amount = parsed.amount,
                        txId = parsed.txId,
                        balanceAfter = parsed.balanceAfter,
                        rawText = parsed.rawText,
                        timestamp = parsed.timestamp,
                        isReconciled = false
                    )

                    val logId = moMoRepo.addMoMoLog(momoLog)
                    val savedLog = momoLog.copy(id = logId)

                    autoReconciliationEngine.reconcile(savedLog)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
