package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minAlertStock: Int = 5
)
