package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentStatus {
    PAID,
    PARTIAL,
    OVERDUE,
    UNPAID
}

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String,
    val fullName: String,
    val grade: String,
    val parentName: String,
    val contactPhone: String,
    val contactEmail: String,
    val tuitionFee: Double,
    val labFee: Double = 0.0,
    val libraryFee: Double = 0.0,
    val transportFee: Double = 0.0,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueDateEpoch: Long = System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000,
    val enrolledDateEpoch: Long = System.currentTimeMillis()
) {
    val totalFee: Double
        get() = tuitionFee + labFee + libraryFee + transportFee

    val netFee: Double
        get() = (totalFee - discount).coerceAtLeast(0.0)

    val outstandingBalance: Double
        get() = (netFee - paidAmount).coerceAtLeast(0.0)

    val paymentProgress: Float
        get() = if (netFee > 0.0) (paidAmount / netFee).coerceIn(0.0, 1.0).toFloat() else 1.0f

    val isOverdue: Boolean
        get() = outstandingBalance > 0.01 && System.currentTimeMillis() > dueDateEpoch

    val status: PaymentStatus
        get() = when {
            outstandingBalance <= 0.01 -> PaymentStatus.PAID
            isOverdue -> PaymentStatus.OVERDUE
            paidAmount > 0.01 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }
}
