package com.example.myapplication.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

open class SecurityManager(context: Context? = null) {

    private val prefs: SharedPreferences? by lazy {
        if (context == null) return@lazy null
        try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                "ideni_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.valueOf("AES256_SKEY"),
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences("ideni_fallback_prefs", Context.MODE_PRIVATE)
        }
    }

    open var isBiometricEnabled: Boolean
        get() = prefs?.getBoolean(KEY_BIOMETRIC_ENABLED, false) ?: false
        set(value) { prefs?.edit()?.putBoolean(KEY_BIOMETRIC_ENABLED, value)?.apply() }

    open val isPinSet: Boolean
        get() = prefs?.getString(KEY_PIN_HASH, null) != null

    open fun setPin(pin: String) {
        val hash = hashPin(pin)
        prefs?.edit()?.putString(KEY_PIN_HASH, hash)?.apply()
    }

    open fun verifyPin(pin: String): Boolean {
        val storedHash = prefs?.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    open fun clearPin() {
        prefs?.edit()?.remove(KEY_PIN_HASH)?.apply()
    }

    open var autoLockTimeoutMinutes: Int
        get() = prefs?.getInt(KEY_AUTO_LOCK_TIMEOUT, 0) ?: 0
        set(value) { prefs?.edit()?.putInt(KEY_AUTO_LOCK_TIMEOUT, value)?.apply() }

    open var lastUnlockedTime: Long
        get() = prefs?.getLong(KEY_LAST_UNLOCKED_TIME, 0L) ?: 0L
        set(value) { prefs?.edit()?.putLong(KEY_LAST_UNLOCKED_TIME, value)?.apply() }

    open var shopName: String
        get() = prefs?.getString(KEY_SHOP_NAME, "Smart Merchant") ?: "Smart Merchant"
        set(value) { prefs?.edit()?.putString(KEY_SHOP_NAME, value)?.apply() }

    open var businessSector: String
        get() = prefs?.getString(KEY_BUSINESS_SECTOR, "Retail") ?: "Retail"
        set(value) { prefs?.edit()?.putString(KEY_BUSINESS_SECTOR, value)?.apply() }

    open var baseCurrency: String
        get() = prefs?.getString(KEY_BASE_CURRENCY, "RWF") ?: "RWF"
        set(value) { prefs?.edit()?.putString(KEY_BASE_CURRENCY, value)?.apply() }

    open var googleUserEmail: String?
        get() = prefs?.getString(KEY_GOOGLE_USER_EMAIL, null)
        set(value) { prefs?.edit()?.putString(KEY_GOOGLE_USER_EMAIL, value)?.apply() }

    open var googleDisplayName: String?
        get() = prefs?.getString(KEY_GOOGLE_DISPLAY_NAME, null)
        set(value) { prefs?.edit()?.putString(KEY_GOOGLE_DISPLAY_NAME, value)?.apply() }

    open var isOfflineMode: Boolean
        get() = prefs?.getBoolean(KEY_OFFLINE_MODE, false) ?: false
        set(value) { prefs?.edit()?.putBoolean(KEY_OFFLINE_MODE, value)?.apply() }

    open fun shouldLockApp(): Boolean {
        if (!isPinSet && !isBiometricEnabled) return false
        val timeoutMs = autoLockTimeoutMinutes * 60 * 1000L
        val elapsed = System.currentTimeMillis() - lastUnlockedTime
        return elapsed > timeoutMs
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_AUTO_LOCK_TIMEOUT = "key_auto_lock_timeout"
        private const val KEY_LAST_UNLOCKED_TIME = "key_last_unlocked_time"

        private const val KEY_SHOP_NAME = "key_shop_name"
        private const val KEY_BUSINESS_SECTOR = "key_business_sector"
        private const val KEY_BASE_CURRENCY = "key_base_currency"
        private const val KEY_GOOGLE_USER_EMAIL = "key_google_user_email"
        private const val KEY_GOOGLE_DISPLAY_NAME = "key_google_display_name"
        private const val KEY_OFFLINE_MODE = "key_offline_mode"
    }
}
