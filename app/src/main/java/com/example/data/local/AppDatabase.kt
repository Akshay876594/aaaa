package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.PaymentReceiptEntity
import com.example.data.model.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StudentEntity::class, PaymentReceiptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun paymentReceiptDao(): PaymentReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scholarfee_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial demo data asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            val initialStudents = listOf(
                StudentEntity(
                    id = 1,
                    studentId = "SCH-2026-101",
                    fullName = "Liam Davies",
                    grade = "Grade 10 - Science",
                    parentName = "Robert Davies",
                    contactPhone = "+1 (555) 234-8901",
                    contactEmail = "rdavies@example.com",
                    tuitionFee = 2400.0,
                    labFee = 350.0,
                    libraryFee = 150.0,
                    transportFee = 300.0,
                    discount = 200.0, // Net: 3000.0
                    paidAmount = 3000.0,
                    dueDateEpoch = now + (15 * dayMs),
                    enrolledDateEpoch = now - (90 * dayMs)
                ),
                StudentEntity(
                    id = 2,
                    studentId = "SCH-2026-102",
                    fullName = "Sophia Chen",
                    grade = "Grade 11 - Computer Science",
                    parentName = "Dr. Marcus Chen",
                    contactPhone = "+1 (555) 345-6712",
                    contactEmail = "marcus.chen@example.com",
                    tuitionFee = 2800.0,
                    labFee = 500.0,
                    libraryFee = 200.0,
                    transportFee = 0.0,
                    discount = 500.0, // Net: 3000.0
                    paidAmount = 1800.0, // Balance: 1200.0
                    dueDateEpoch = now + (2 * dayMs), // Due in 2 days (within 3 days)!
                    enrolledDateEpoch = now - (60 * dayMs)
                ),
                StudentEntity(
                    id = 3,
                    studentId = "SCH-2026-103",
                    fullName = "Noah Patel",
                    grade = "Grade 9 - Standard",
                    parentName = "Anil & Priya Patel",
                    contactPhone = "+1 (555) 456-7823",
                    contactEmail = "priya.patel@example.com",
                    tuitionFee = 2100.0,
                    labFee = 200.0,
                    libraryFee = 150.0,
                    transportFee = 250.0,
                    discount = 0.0, // Net: 2700.0
                    paidAmount = 700.0, // Balance: 2000.0
                    dueDateEpoch = now - (4 * dayMs), // OVERDUE!
                    enrolledDateEpoch = now - (120 * dayMs)
                ),
                StudentEntity(
                    id = 4,
                    studentId = "SCH-2026-104",
                    fullName = "Emma Watson-Reyes",
                    grade = "Grade 12 - Commerce",
                    parentName = "Elena Reyes",
                    contactPhone = "+1 (555) 567-8934",
                    contactEmail = "elena.reyes@example.com",
                    tuitionFee = 3000.0,
                    labFee = 300.0,
                    libraryFee = 200.0,
                    transportFee = 400.0,
                    discount = 400.0, // Net: 3500.0
                    paidAmount = 0.0, // Balance: 3500.0
                    dueDateEpoch = now - (10 * dayMs), // OVERDUE!
                    enrolledDateEpoch = now - (80 * dayMs)
                ),
                StudentEntity(
                    id = 5,
                    studentId = "SCH-2026-105",
                    fullName = "Lucas Miller",
                    grade = "Grade 10 - Arts",
                    parentName = "Sarah Miller",
                    contactPhone = "+1 (555) 678-9045",
                    contactEmail = "sarah.m@example.com",
                    tuitionFee = 2200.0,
                    labFee = 150.0,
                    libraryFee = 150.0,
                    transportFee = 200.0,
                    discount = 100.0, // Net: 2600.0
                    paidAmount = 1300.0, // Balance: 1300.0
                    dueDateEpoch = now + (20 * dayMs),
                    enrolledDateEpoch = now - (45 * dayMs)
                ),
                StudentEntity(
                    id = 6,
                    studentId = "SCH-2026-106",
                    fullName = "Ava Johnson",
                    grade = "Grade 11 - Science",
                    parentName = "David Johnson",
                    contactPhone = "+1 (555) 789-0156",
                    contactEmail = "david.j@example.com",
                    tuitionFee = 2700.0,
                    labFee = 450.0,
                    libraryFee = 150.0,
                    transportFee = 350.0,
                    discount = 350.0, // Net: 3300.0
                    paidAmount = 3300.0, // Fully paid
                    dueDateEpoch = now + (30 * dayMs),
                    enrolledDateEpoch = now - (150 * dayMs)
                ),
                StudentEntity(
                    id = 7,
                    studentId = "BCA-2026-001",
                    fullName = "Aarav Sharma",
                    grade = "BCA - Sem 1",
                    parentName = "Sunil Sharma",
                    contactPhone = "+91 98765 43210",
                    contactEmail = "aarav.sharma@example.edu",
                    tuitionFee = 22000.0,
                    labFee = 2000.0,
                    libraryFee = 1000.0,
                    transportFee = 0.0,
                    discount = 0.0,
                    paidAmount = 15000.0,
                    dueDateEpoch = now + (2 * dayMs), // Due in 2 days (within 3 days reminder)
                    enrolledDateEpoch = now - (30 * dayMs)
                ),
                StudentEntity(
                    id = 8,
                    studentId = "BED-2026-004",
                    fullName = "Priyanka Verma",
                    grade = "B.Ed - Sem 2",
                    parentName = "R.K. Verma",
                    contactPhone = "+91 98111 22334",
                    contactEmail = "priyanka.v@example.edu",
                    tuitionFee = 28000.0,
                    labFee = 2000.0,
                    libraryFee = 2000.0,
                    transportFee = 0.0,
                    discount = 2000.0,
                    paidAmount = 30000.0, // Fully paid
                    dueDateEpoch = now + (25 * dayMs),
                    enrolledDateEpoch = now - (40 * dayMs)
                ),
                StudentEntity(
                    id = 9,
                    studentId = "MSC-ZOO-101",
                    fullName = "Ananya Sen",
                    grade = "M.Sc Zoology - Sem 1",
                    parentName = "Dr. Subhash Sen",
                    contactPhone = "+91 99220 33445",
                    contactEmail = "ananya.sen@example.edu",
                    tuitionFee = 24000.0,
                    labFee = 3000.0,
                    libraryFee = 1000.0,
                    transportFee = 0.0,
                    discount = 0.0,
                    paidAmount = 0.0, // Overdue
                    dueDateEpoch = now - (3 * dayMs),
                    enrolledDateEpoch = now - (50 * dayMs)
                ),
                StudentEntity(
                    id = 10,
                    studentId = "MSC-CHM-202",
                    fullName = "Devendra Rao",
                    grade = "M.Sc Chemistry - Sem 2",
                    parentName = "G.V. Rao",
                    contactPhone = "+91 98450 12345",
                    contactEmail = "devendra.rao@example.edu",
                    tuitionFee = 25000.0,
                    labFee = 3500.0,
                    libraryFee = 1500.0,
                    transportFee = 0.0,
                    discount = 0.0,
                    paidAmount = 15000.0, // Partial
                    dueDateEpoch = now + (18 * dayMs),
                    enrolledDateEpoch = now - (70 * dayMs)
                ),
                StudentEntity(
                    id = 11,
                    studentId = "MSC-PHY-301",
                    fullName = "Kavita Nair",
                    grade = "M.Sc Physics - Sem 3",
                    parentName = "Radha Nair",
                    contactPhone = "+91 97440 55667",
                    contactEmail = "kavita.nair@example.edu",
                    tuitionFee = 25000.0,
                    labFee = 3500.0,
                    libraryFee = 1500.0,
                    transportFee = 0.0,
                    discount = 0.0,
                    paidAmount = 30000.0, // Fully paid
                    dueDateEpoch = now + (45 * dayMs),
                    enrolledDateEpoch = now - (90 * dayMs)
                ),
                StudentEntity(
                    id = 12,
                    studentId = "MSC-GEN-102",
                    fullName = "Siddharth Mehta",
                    grade = "M.Sc - Sem 1",
                    parentName = "Ashok Mehta",
                    contactPhone = "+91 98200 88990",
                    contactEmail = "sid.mehta@example.edu",
                    tuitionFee = 22000.0,
                    labFee = 2500.0,
                    libraryFee = 1500.0,
                    transportFee = 0.0,
                    discount = 1000.0,
                    paidAmount = 12500.0, // Partial
                    dueDateEpoch = now + (2 * dayMs), // Due in 2 days!
                    enrolledDateEpoch = now - (20 * dayMs)
                )
            )

            database.studentDao().insertAll(initialStudents)

            // Seed initial receipts corresponding to paid and partial payments
            val initialReceipts = listOf(
                PaymentReceiptEntity(
                    receiptId = 1,
                    receiptNumber = "REC-2026-0001",
                    studentId = 1,
                    studentName = "Liam Davies",
                    studentRollNumber = "SCH-2026-101",
                    studentGrade = "Grade 10 - Science",
                    amountPaid = 3000.0,
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "WIRE-8932014-X",
                    paymentDateEpoch = now - (12 * dayMs),
                    previousBalance = 3000.0,
                    remainingBalance = 0.0,
                    notes = "Full annual academic fee payment with early concession applied.",
                    issuedBy = "Chief Accounts Officer"
                ),
                PaymentReceiptEntity(
                    receiptId = 2,
                    receiptNumber = "REC-2026-0002",
                    studentId = 2,
                    studentName = "Sophia Chen",
                    studentRollNumber = "SCH-2026-102",
                    studentGrade = "Grade 11 - Computer Science",
                    amountPaid = 1800.0,
                    paymentMethod = "Credit Card",
                    referenceNumber = "AUTH-449102-VISA",
                    paymentDateEpoch = now - (5 * dayMs),
                    previousBalance = 3000.0,
                    remainingBalance = 1200.0,
                    notes = "First installment (60%) received. Second installment due in 7 days.",
                    issuedBy = "Finance Cashier"
                ),
                PaymentReceiptEntity(
                    receiptId = 3,
                    receiptNumber = "REC-2026-0003",
                    studentId = 3,
                    studentName = "Noah Patel",
                    studentRollNumber = "SCH-2026-103",
                    studentGrade = "Grade 9 - Standard",
                    amountPaid = 700.0,
                    paymentMethod = "UPI / Mobile",
                    referenceNumber = "UPI-902847118@bank",
                    paymentDateEpoch = now - (20 * dayMs),
                    previousBalance = 2700.0,
                    remainingBalance = 2000.0,
                    notes = "Registration and admission deposit cleared.",
                    issuedBy = "Accounts Department"
                ),
                PaymentReceiptEntity(
                    receiptId = 4,
                    receiptNumber = "REC-2026-0004",
                    studentId = 5,
                    studentName = "Lucas Miller",
                    studentRollNumber = "SCH-2026-105",
                    studentGrade = "Grade 10 - Arts",
                    amountPaid = 1300.0,
                    paymentMethod = "Cash",
                    referenceNumber = "CASH-RC-8812",
                    paymentDateEpoch = now - (3 * dayMs),
                    previousBalance = 2600.0,
                    remainingBalance = 1300.0,
                    notes = "Term 1 installment paid in cash at administrative desk.",
                    issuedBy = "Desk Registrar"
                ),
                PaymentReceiptEntity(
                    receiptId = 5,
                    receiptNumber = "REC-2026-0005",
                    studentId = 6,
                    studentName = "Ava Johnson",
                    studentRollNumber = "SCH-2026-106",
                    studentGrade = "Grade 11 - Science",
                    amountPaid = 3300.0,
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "NEFT-7892019-J",
                    paymentDateEpoch = now - (8 * dayMs),
                    previousBalance = 3300.0,
                    remainingBalance = 0.0,
                    notes = "Complete term clearance including laboratory equipment surcharge.",
                    issuedBy = "Chief Accounts Officer"
                ),
                PaymentReceiptEntity(
                    receiptId = 6,
                    receiptNumber = "REC-2026-0006",
                    studentId = 7,
                    studentName = "Aarav Sharma",
                    studentRollNumber = "BCA-2026-001",
                    studentGrade = "BCA - Sem 1",
                    amountPaid = 15000.0,
                    paymentMethod = "Stripe",
                    referenceNumber = "ch_live_bca893021",
                    paymentDateEpoch = now - (5 * dayMs),
                    previousBalance = 25000.0,
                    remainingBalance = 10000.0,
                    notes = "First semester tuition and computer lab installment paid via online gateway.",
                    issuedBy = "Online Gateway Automated Service"
                ),
                PaymentReceiptEntity(
                    receiptId = 7,
                    receiptNumber = "REC-2026-0007",
                    studentId = 8,
                    studentName = "Priyanka Verma",
                    studentRollNumber = "BED-2026-004",
                    studentGrade = "B.Ed - Sem 2",
                    amountPaid = 30000.0,
                    paymentMethod = "PayPal",
                    referenceNumber = "PAYID-BED77651",
                    paymentDateEpoch = now - (10 * dayMs),
                    previousBalance = 30000.0,
                    remainingBalance = 0.0,
                    notes = "Complete semester 2 education course fees cleared.",
                    issuedBy = "Online Gateway Automated Service"
                )
            )

            database.paymentReceiptDao().insertAll(initialReceipts)
        }
    }
}
