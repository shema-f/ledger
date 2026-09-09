package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY totalDebt DESC")
    fun getCustomersSortedByDebt(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY fullName ASC")
    fun getCustomersSortedByName(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE fullName LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET totalDebt = :totalDebt WHERE id = :customerId")
    suspend fun updateTotalDebt(customerId: Long, totalDebt: Double)

    @Query("""
        UPDATE customers 
        SET totalDebt = (
            SELECT COALESCE(SUM(CASE WHEN type = 'CREDIT' THEN amount WHEN type = 'PAYMENT' THEN -amount ELSE 0.0 END), 0.0)
            FROM ledger_records 
            WHERE customerId = :customerId
        )
        WHERE id = :customerId
    """)
    suspend fun recalculateCustomerTotalDebt(customerId: Long)

    @Query("SELECT COUNT(*) FROM customers WHERE totalDebt > 0")
    fun getActiveDebtorsCount(): Flow<Int>
}
