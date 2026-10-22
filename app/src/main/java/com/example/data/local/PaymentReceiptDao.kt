package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentReceiptDao {
    @Query("SELECT * FROM payment_receipts ORDER BY paymentDateEpoch DESC")
    fun getAllReceipts(): Flow<List<PaymentReceiptEntity>>

    @Query("SELECT * FROM payment_receipts WHERE studentId = :studentId ORDER BY paymentDateEpoch DESC")
    fun getReceiptsForStudent(studentId: Long): Flow<List<PaymentReceiptEntity>>

    @Query("SELECT * FROM payment_receipts WHERE receiptId = :id LIMIT 1")
    fun getReceiptById(id: Long): Flow<PaymentReceiptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PaymentReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<PaymentReceiptEntity>)

    @Query("SELECT COUNT(*) FROM payment_receipts")
    suspend fun getReceiptsCount(): Int

    @Query("SELECT MAX(receiptId) FROM payment_receipts")
    suspend fun getMaxReceiptId(): Long?
}
