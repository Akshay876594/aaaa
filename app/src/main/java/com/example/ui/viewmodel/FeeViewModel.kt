package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PaymentReceiptEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.StudentEntity
import com.example.data.repository.FeeRepository
import com.example.util.FeeNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    DASHBOARD,
    STUDENT_PORTAL,
    STUDENTS,
    RECEIPTS,
    ANALYTICS
}

data class FinancialMetrics(
    val totalExpected: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val collectionRate: Float = 0f,
    val totalStudents: Int = 0,
    val overdueCount: Int = 0,
    val overdueAmount: Double = 0.0,
    val paidCount: Int = 0,
    val partialCount: Int = 0
)

class FeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FeeRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = FeeRepository(database.studentDao(), database.paymentReceiptDao())
    }

    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<PaymentReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Authentication State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _showAdminLoginDialog = MutableStateFlow(false)
    val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

    // Active Navigation
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Selected Student for Student-Facing Portal
    private val _selectedPortalStudentId = MutableStateFlow<Long?>(null)
    val selectedPortalStudentId: StateFlow<Long?> = _selectedPortalStudentId.asStateFlow()

    val currentPortalStudent: StateFlow<StudentEntity?> = combine(allStudents, _selectedPortalStudentId) { students, id ->
        if (students.isEmpty()) null
        else if (id != null) students.find { it.id == id } ?: students.firstOrNull()
        // Default to a student with pending balance like Sophia Chen (id=2) or Noah Patel (id=3) for great demo UX
        else students.find { it.outstandingBalance > 0 } ?: students.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Receipts for the current student in Student Portal
    val portalStudentReceipts: StateFlow<List<PaymentReceiptEntity>> = combine(allReceipts, currentPortalStudent) { receipts, student ->
        if (student == null) emptyList()
        else receipts.filter { it.studentId == student.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Students with fee due dates within the next 3 days
    val studentsDueWithin3Days: StateFlow<List<StudentEntity>> = allStudents.map { list ->
        list.filter { FeeNotificationHelper.isFeeDueWithin3Days(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAutomatedReminderEnabled = MutableStateFlow(true)
    val isAutomatedReminderEnabled: StateFlow<Boolean> = _isAutomatedReminderEnabled.asStateFlow()

    private val _lastNotificationDispatchTime = MutableStateFlow<Long?>(null)
    val lastNotificationDispatchTime: StateFlow<Long?> = _lastNotificationDispatchTime.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<PaymentStatus?>(null)
    val selectedStatusFilter: StateFlow<PaymentStatus?> = _selectedStatusFilter.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow<String?>(null)
    val selectedGradeFilter: StateFlow<String?> = _selectedGradeFilter.asStateFlow()

    private val _selectedCourseName = MutableStateFlow("BCA")
    val selectedCourseName: StateFlow<String> = _selectedCourseName.asStateFlow()

    private val _selectedSemesterNumber = MutableStateFlow(1)
    val selectedSemesterNumber: StateFlow<Int> = _selectedSemesterNumber.asStateFlow()

    // Dialog & Action States
    private val _activePaymentStudent = MutableStateFlow<StudentEntity?>(null)
    val activePaymentStudent: StateFlow<StudentEntity?> = _activePaymentStudent.asStateFlow()

    // Secure Gateway Sheet
    private val _showGatewayForStudent = MutableStateFlow<StudentEntity?>(null)
    val showGatewayForStudent: StateFlow<StudentEntity?> = _showGatewayForStudent.asStateFlow()

    private val _isGatewayProcessing = MutableStateFlow(false)
    val isGatewayProcessing: StateFlow<Boolean> = _isGatewayProcessing.asStateFlow()

    private val _activeReceiptForView = MutableStateFlow<PaymentReceiptEntity?>(null)
    val activeReceiptForView: StateFlow<PaymentReceiptEntity?> = _activeReceiptForView.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    private val _studentToEdit = MutableStateFlow<StudentEntity?>(null)
    val studentToEdit: StateFlow<StudentEntity?> = _studentToEdit.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    // Filtered Students Flow
    val filteredStudents: StateFlow<List<StudentEntity>> = combine(
        allStudents,
        _searchQuery,
        _selectedStatusFilter,
        _selectedGradeFilter
    ) { students, query, status, grade ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.fullName.contains(query, ignoreCase = true) ||
                    student.studentId.contains(query, ignoreCase = true) ||
                    student.grade.contains(query, ignoreCase = true) ||
                    student.parentName.contains(query, ignoreCase = true)

            val matchesStatus = status == null || student.status == status
            val matchesGrade = grade == null || student.grade.equals(grade, ignoreCase = true)

            matchesQuery && matchesStatus && matchesGrade
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Metrics Flow
    val metrics: StateFlow<FinancialMetrics> = allStudents.combine(allReceipts) { students, _ ->
        var expected = 0.0
        var collected = 0.0
        var outstanding = 0.0
        var overdueCount = 0
        var overdueAmount = 0.0
        var paidCount = 0
        var partialCount = 0

        students.forEach { s ->
            expected += s.netFee
            collected += s.paidAmount
            outstanding += s.outstandingBalance

            when (s.status) {
                PaymentStatus.PAID -> paidCount++
                PaymentStatus.PARTIAL -> partialCount++
                PaymentStatus.OVERDUE -> {
                    overdueCount++
                    overdueAmount += s.outstandingBalance
                }
                PaymentStatus.UNPAID -> {
                    if (s.isOverdue) {
                        overdueCount++
                        overdueAmount += s.outstandingBalance
                    }
                }
            }
        }

        val rate = if (expected > 0) (collected / expected).toFloat() else 0f

        FinancialMetrics(
            totalExpected = expected,
            totalCollected = collected,
            totalOutstanding = outstanding,
            collectionRate = rate,
            totalStudents = students.size,
            overdueCount = overdueCount,
            overdueAmount = overdueAmount,
            paidCount = paidCount,
            partialCount = partialCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialMetrics())

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setPortalStudent(student: StudentEntity) {
        _selectedPortalStudentId.value = student.id
    }

    fun openAdminLogin() {
        _showAdminLoginDialog.value = true
    }

    fun closeAdminLogin() {
        _showAdminLoginDialog.value = false
    }

    fun loginAdmin(userId: String, pass: String): Boolean {
        // Accepts "admin" / "admin123", "bursar" / "admin123", or any non-empty user with "admin123"
        val isValid = (userId.equals("admin", ignoreCase = true) && (pass == "admin123" || pass == "admin")) ||
                (userId.isNotBlank() && (pass == "admin123" || pass == "password" || pass == "admin"))

        if (isValid) {
            _isAdminLoggedIn.value = true
            _showAdminLoginDialog.value = false
            _currentTab.value = AppTab.DASHBOARD
            viewModelScope.launch {
                _snackbarEvent.emit("Admin access granted. Welcome, $userId!")
            }
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentTab.value = AppTab.STUDENT_PORTAL
        viewModelScope.launch {
            _snackbarEvent.emit("Logged out of Admin Portal.")
        }
    }

    fun toggleAutomatedReminder(enabled: Boolean) {
        _isAutomatedReminderEnabled.value = enabled
        viewModelScope.launch {
            _snackbarEvent.emit(if (enabled) "Automated 3-day push reminders enabled." else "Automated push reminders paused.")
        }
    }

    fun triggerAutomated3DayReminders(context: Context) {
        viewModelScope.launch {
            val eligible = allStudents.value.filter { FeeNotificationHelper.isFeeDueWithin3Days(it) }
            if (eligible.isEmpty()) {
                _snackbarEvent.emit("No student accounts currently have fees due within the next 3 days.")
                return@launch
            }

            val dispatched = FeeNotificationHelper.sendAutomatedRemindersForEligibleStudents(context, eligible)
            _lastNotificationDispatchTime.value = System.currentTimeMillis()

            if (dispatched.isNotEmpty()) {
                _snackbarEvent.emit("Sent automated push reminders for ${dispatched.size} student(s) with fees due within 3 days: ${dispatched.joinToString { it.fullName }}.")
            } else {
                _snackbarEvent.emit("Push notification permission required on device to display system alerts.")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: PaymentStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setGradeFilter(grade: String?) {
        _selectedGradeFilter.value = grade
    }

    fun selectCourseAndSemester(course: String, semester: Int, gradeString: String) {
        _selectedCourseName.value = course
        _selectedSemesterNumber.value = semester
    }

    fun openPaymentDialog(student: StudentEntity) {
        _activePaymentStudent.value = student
    }

    fun closePaymentDialog() {
        _activePaymentStudent.value = null
    }

    fun openGatewayPayment(student: StudentEntity) {
        _showGatewayForStudent.value = student
    }

    fun closeGatewayPayment() {
        _showGatewayForStudent.value = null
        _isGatewayProcessing.value = false
    }

    fun viewReceipt(receipt: PaymentReceiptEntity) {
        _activeReceiptForView.value = receipt
    }

    fun closeReceiptView() {
        _activeReceiptForView.value = null
    }

    fun openAddStudentDialog() {
        _studentToEdit.value = null
        _showAddStudentDialog.value = true
    }

    fun openEditStudentDialog(student: StudentEntity) {
        _studentToEdit.value = student
        _showAddStudentDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddStudentDialog.value = false
        _studentToEdit.value = null
    }

    fun recordPayment(
        studentId: Long,
        amount: Double,
        paymentMethod: String,
        referenceNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.recordPayment(
                studentId = studentId,
                amount = amount,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                notes = notes
            )
            result.onSuccess { receipt ->
                closePaymentDialog()
                _activeReceiptForView.value = receipt
                _snackbarEvent.emit("Payment recorded successfully! Receipt #${receipt.receiptNumber} generated.")
            }.onFailure { err ->
                _snackbarEvent.emit("Error recording payment: ${err.localizedMessage ?: "Unknown"}")
            }
        }
    }

    /**
     * Processes secure payment gateway transaction (Stripe / PayPal),
     * simulates secure gateway tokenization and network authorization,
     * updates Room database, and triggers automated receipt issuance.
     */
    fun processGatewayPayment(
        studentId: Long,
        amount: Double,
        gatewayName: String, // "Stripe (Visa •••• 4242)", "PayPal Express Checkout", "Google Pay"
        transactionReference: String,
        notes: String
    ) {
        viewModelScope.launch {
            _isGatewayProcessing.value = true
            // Secure gateway simulated roundtrip
            delay(1200)

            val result = repository.recordPayment(
                studentId = studentId,
                amount = amount,
                paymentMethod = gatewayName,
                referenceNumber = transactionReference,
                notes = notes
            )

            _isGatewayProcessing.value = false
            result.onSuccess { receipt ->
                closeGatewayPayment()
                _activeReceiptForView.value = receipt
                _snackbarEvent.emit("Payment of ${formatCurrency(amount)} processed successfully via $gatewayName! Receipt #${receipt.receiptNumber} issued.")
            }.onFailure { err ->
                _snackbarEvent.emit("Gateway error: ${err.localizedMessage ?: "Payment failed"}")
            }
        }
    }

    fun saveStudent(
        id: Long = 0,
        studentId: String,
        fullName: String,
        grade: String,
        parentName: String,
        contactPhone: String,
        contactEmail: String,
        tuitionFee: Double,
        labFee: Double,
        libraryFee: Double,
        transportFee: Double,
        discount: Double,
        dueDateEpoch: Long
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val newStudent = StudentEntity(
                    studentId = studentId,
                    fullName = fullName,
                    grade = grade,
                    parentName = parentName,
                    contactPhone = contactPhone,
                    contactEmail = contactEmail,
                    tuitionFee = tuitionFee,
                    labFee = labFee,
                    libraryFee = libraryFee,
                    transportFee = transportFee,
                    discount = discount,
                    paidAmount = 0.0,
                    dueDateEpoch = dueDateEpoch,
                    enrolledDateEpoch = System.currentTimeMillis()
                )
                val newId = repository.addStudent(newStudent)
                _selectedPortalStudentId.value = newId
                _snackbarEvent.emit("Student $fullName enrolled successfully!")
            } else {
                val current = allStudents.value.find { it.id == id }
                val updated = StudentEntity(
                    id = id,
                    studentId = studentId,
                    fullName = fullName,
                    grade = grade,
                    parentName = parentName,
                    contactPhone = contactPhone,
                    contactEmail = contactEmail,
                    tuitionFee = tuitionFee,
                    labFee = labFee,
                    libraryFee = libraryFee,
                    transportFee = transportFee,
                    discount = discount,
                    paidAmount = current?.paidAmount ?: 0.0,
                    dueDateEpoch = dueDateEpoch,
                    enrolledDateEpoch = current?.enrolledDateEpoch ?: System.currentTimeMillis()
                )
                repository.updateStudent(updated)
                _snackbarEvent.emit("Updated fee records for $fullName.")
            }
            closeAddEditDialog()
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _snackbarEvent.emit("Removed ${student.fullName} from roster.")
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            return format.format(amount)
        }

        fun formatDate(epochMillis: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(epochMillis))
        }

        fun generateReceiptShareText(receipt: PaymentReceiptEntity): String {
            return """
                ========================================
                       SCHOLARFEE ACADEMY
                    OFFICIAL FEE PAYMENT RECEIPT
                ========================================
                Receipt No : ${receipt.receiptNumber}
                Date       : ${receipt.formattedDate}
                Issued By  : ${receipt.issuedBy}
                ----------------------------------------
                STUDENT INFORMATION:
                Name       : ${receipt.studentName}
                Student ID : ${receipt.studentRollNumber}
                Class/Grade: ${receipt.studentGrade}
                ----------------------------------------
                PAYMENT BREAKDOWN:
                Amount Paid       : ${formatCurrency(receipt.amountPaid)}
                Payment Method    : ${receipt.paymentMethod}
                Reference / Txn ID: ${receipt.referenceNumber}
                Previous Balance  : ${formatCurrency(receipt.previousBalance)}
                Remaining Balance : ${formatCurrency(receipt.remainingBalance)}
                ----------------------------------------
                Status: OFFICIAL & VERIFIED
                Note  : ${if (receipt.notes.isNotBlank()) receipt.notes else "Fees accepted towards academic services."}
                ========================================
                Thank you for your prompt payment!
            """.trimIndent()
        }

        fun generatePaymentReminderText(student: StudentEntity): String {
            return """
                Dear ${student.parentName},

                This is a friendly reminder from ScholarFee Academy regarding the fee account for ${student.fullName} (${student.studentId}, ${student.grade}).

                Total Academic Fee   : ${formatCurrency(student.netFee)}
                Amount Paid to Date  : ${formatCurrency(student.paidAmount)}
                Outstanding Balance  : ${formatCurrency(student.outstandingBalance)}
                Payment Due Date     : ${formatDate(student.dueDateEpoch)}

                Kindly arrange payment before the due date via Stripe/online payment portal or at the school accounts desk.

                Warm regards,
                ScholarFee Finance Department
            """.trimIndent()
        }

        fun generateStudentAccountStatementText(student: StudentEntity, receipts: List<PaymentReceiptEntity>): String {
            val sb = java.lang.StringBuilder()
            sb.appendLine("========================================")
            sb.appendLine("       SCHOLARFEE ACADEMY")
            sb.appendLine("    STUDENT STATEMENT OF ACCOUNT")
            sb.appendLine("========================================")
            sb.appendLine("Student: ${student.fullName} (${student.studentId})")
            sb.appendLine("Grade  : ${student.grade}")
            sb.appendLine("Parent : ${student.parentName}")
            sb.appendLine("----------------------------------------")
            sb.appendLine("FEE BREAKDOWN:")
            sb.appendLine("Tuition Fee       : ${formatCurrency(student.tuitionFee)}")
            if (student.labFee > 0) sb.appendLine("Laboratory Fee    : ${formatCurrency(student.labFee)}")
            if (student.libraryFee > 0) sb.appendLine("Library & Books   : ${formatCurrency(student.libraryFee)}")
            if (student.transportFee > 0) sb.appendLine("Transport         : ${formatCurrency(student.transportFee)}")
            if (student.discount > 0) sb.appendLine("Scholarship/Disc. :-${formatCurrency(student.discount)}")
            sb.appendLine("----------------------------------------")
            sb.appendLine("Net Total Fee     : ${formatCurrency(student.netFee)}")
            sb.appendLine("Total Paid        : ${formatCurrency(student.paidAmount)}")
            sb.appendLine("OUTSTANDING BAL.  : ${formatCurrency(student.outstandingBalance)}")
            sb.appendLine("Payment Due Date  : ${formatDate(student.dueDateEpoch)}")
            sb.appendLine("Status            : ${student.status}")
            sb.appendLine("========================================")
            sb.appendLine("PAYMENT HISTORY (${receipts.size} transactions):")
            receipts.forEach { r ->
                sb.appendLine("- ${r.formattedShortDate} | ${r.receiptNumber} | ${formatCurrency(r.amountPaid)} (${r.paymentMethod})")
            }
            sb.appendLine("========================================")
            return sb.toString()
        }

        fun generateAuditLedgerReportText(
            students: List<StudentEntity>,
            receipts: List<PaymentReceiptEntity>,
            metrics: FinancialMetrics
        ): String {
            val sb = StringBuilder()
            sb.appendLine("==================================================")
            sb.appendLine("       SCHOLARFEE ACADEMY - BURSAR AUDIT LEDGER")
            sb.appendLine("              OFFICIAL INSTITUTIONAL REPORT")
            sb.appendLine("==================================================")
            sb.appendLine("Generated On        : ${formatDate(System.currentTimeMillis())}")
            sb.appendLine("Total Student Roster: ${metrics.totalStudents} students")
            sb.appendLine("Gross Billing Target: ${formatCurrency(metrics.totalExpected)}")
            sb.appendLine("Total Fees Collected: ${formatCurrency(metrics.totalCollected)} (${(metrics.collectionRate * 100).toInt()}%)")
            sb.appendLine("Outstanding Balance : ${formatCurrency(metrics.totalOutstanding)}")
            sb.appendLine("Overdue Accounts    : ${metrics.overdueCount} accounts (${formatCurrency(metrics.overdueAmount)})")
            sb.appendLine("Fully Cleared       : ${metrics.paidCount} students")
            sb.appendLine("Partially Paid      : ${metrics.partialCount} students")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("STUDENT SUMMARY ROSTER:")
            students.forEach { s ->
                val statusStr = s.status.name.padEnd(8)
                sb.appendLine("• ${s.studentId.padEnd(14)} | ${s.fullName.padEnd(20)} | ${s.grade.padEnd(16)} | Bal: ${formatCurrency(s.outstandingBalance).padEnd(10)} | $statusStr")
            }
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("RECENT TRANSACTIONS & RECEIPTS (${receipts.size} total):")
            receipts.take(15).forEach { r ->
                sb.appendLine("- ${r.receiptNumber.padEnd(14)} | ${r.formattedShortDate.padEnd(12)} | ${r.studentName.padEnd(18)} | ${formatCurrency(r.amountPaid).padEnd(10)} | ${r.paymentMethod}")
            }
            sb.appendLine("==================================================")
            sb.appendLine("End of Institutional Audit Ledger.")
            return sb.toString()
        }
    }
}
