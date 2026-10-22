package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "payment_receipts")
data class PaymentReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val receiptId: Long = 0,
    val receiptNumber: String,
    val studentId: Long,
    val studentName: String,
    val studentRollNumber: String,
    val studentGrade: String,
    val amountPaid: Double,
    val paymentMethod: String,
    val referenceNumber: String,
    val paymentDateEpoch: Long = System.currentTimeMillis(),
    val previousBalance: Double,
    val remainingBalance: Double,
    val notes: String = "",
    val issuedBy: String = "Accounts Department"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(paymentDateEpoch))
        }

    val formattedShortDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(paymentDateEpoch))
        }
}
