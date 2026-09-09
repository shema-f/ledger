package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product): Long
    suspend fun updateStock(productId: Long, newStock: Int)
    suspend fun deductStockAndCalculateProfit(productId: Long, quantitySold: Int): Double
    suspend fun deleteProduct(id: Long)
}
