package com.example.myapplication.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minAlertStock: Int = 5
)
