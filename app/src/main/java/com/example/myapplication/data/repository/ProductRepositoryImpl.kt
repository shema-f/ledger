package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }

    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product.toEntity())
    }

    override suspend fun updateStock(productId: Long, newStock: Int) {
        productDao.updateStock(productId, newStock)
    }

    override suspend fun deductStockAndCalculateProfit(productId: Long, quantitySold: Int): Double {
        val product = productDao.getProductById(productId) ?: return 0.0
        val updatedStock = product.currentStock - quantitySold
        productDao.updateStock(productId, updatedStock)
        val unitProfit = product.sellingPrice - product.buyingPrice
        return unitProfit * quantitySold
    }

    override suspend fun deleteProduct(id: Long) {
        productDao.deleteProduct(id)
    }
}
