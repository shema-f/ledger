package com.example.myapplication.domain.model

import java.util.concurrent.TimeUnit

enum class ExpirationStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED;

    companion object {
        fun calculate(expiryDateMillis: Long?, expiringDaysThreshold: Int = 30): ExpirationStatus {
            if (expiryDateMillis == null || expiryDateMillis == 0L) return ACTIVE
            
            val currentTimeMillis = System.currentTimeMillis()
            if (expiryDateMillis < currentTimeMillis) {
                return EXPIRED
            }
            
            val thresholdMillis = currentTimeMillis + TimeUnit.DAYS.toMillis(expiringDaysThreshold.toLong())
            if (expiryDateMillis <= thresholdMillis) {
                return EXPIRING_SOON
            }
            
            return ACTIVE
        }
    }
}
