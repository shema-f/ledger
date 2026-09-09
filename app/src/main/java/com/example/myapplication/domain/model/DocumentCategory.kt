package com.example.myapplication.domain.model

enum class DocumentCategory(val displayName: String) {
    DRIVER_LICENSE("Driver's License"),
    PASSPORT("Passport"),
    ID_CARD("National ID Card"),
    HEALTH_CARD("Health Insurance Card"),
    OTHER("Other Document");

    companion object {
        fun fromString(value: String): DocumentCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
