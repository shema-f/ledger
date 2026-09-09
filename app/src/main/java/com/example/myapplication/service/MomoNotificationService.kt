package com.example.myapplication.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.myapplication.data.local.ImariDatabase
import com.example.myapplication.data.repository.LedgerRepositoryImpl
import com.example.myapplication.data.repository.MoMoRepositoryImpl
import com.example.myapplication.domain.model.MoMoLog
import com.example.myapplication.domain.reconciliation.AutoReconciliationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MomoNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val fullContent = "$title $text $bigText".trim()
        if (fullContent.isBlank()) return

        val parsedSms = RwandaFinancialParser.parse(title, fullContent, sbn.postTime) ?: return

        serviceScope.launch {
            val db = ImariDatabase.getInstance(applicationContext)
            val moMoRepo = MoMoRepositoryImpl(db.moMoLogDao())
            val ledgerRepo = LedgerRepositoryImpl(db.customerDao(), db.ledgerRecordDao())

            if (moMoRepo.getMoMoLogByTxId(parsedSms.txReference) != null) return@launch

            val momoLog = MoMoLog(
                senderName = parsedSms.senderOrRecipient,
                senderPhone = null,
                amount = parsedSms.amount,
                txId = parsedSms.txReference,
                balanceAfter = parsedSms.balanceAfter,
                rawText = fullContent,
                timestamp = parsedSms.timestamp,
                isReconciled = false
            )

            val logId = moMoRepo.addMoMoLog(momoLog)
            val savedLog = momoLog.copy(id = logId)

            // Broadcast live alert state for interactive UI bottom sheet prompt
            PaymentAlertManager.notifyAlert(parsedSms)

            val autoReconciliationEngine = AutoReconciliationEngine(
                context = applicationContext,
                moMoRepository = moMoRepo,
                ledgerRepository = ledgerRepo
            )
            autoReconciliationEngine.reconcile(savedLog)
        }
    }
}
