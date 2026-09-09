package com.example.myapplication.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    KINYARWANDA("Kinyarwanda", "rw"),
    FRENCH("Français", "fr")
}

object LanguageManager {
    private const val PREFS_NAME = "ifaranga_language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private var prefs: SharedPreferences? = null
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs?.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
        val lang = AppLanguage.entries.find { it.code == savedCode } ?: AppLanguage.ENGLISH
        _currentLanguage.value = lang
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs?.edit()?.putString(KEY_LANGUAGE, language.code)?.apply()
    }

    fun resetForTest(initialLanguage: AppLanguage = AppLanguage.ENGLISH) {
        prefs = null
        _currentLanguage.value = initialLanguage
    }
}
