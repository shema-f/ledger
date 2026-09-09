package com.example.myapplication.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PaymentAlertManager {
    private val _livePaymentAlerts = MutableSharedFlow<FinancialSms>(extraBufferCapacity = 64)
    val livePaymentAlerts: SharedFlow<FinancialSms> = _livePaymentAlerts.asSharedFlow()

    fun notifyAlert(sms: FinancialSms) {
        _livePaymentAlerts.tryEmit(sms)
    }
}
