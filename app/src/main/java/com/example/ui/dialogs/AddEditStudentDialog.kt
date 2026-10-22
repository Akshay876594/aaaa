package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StudentEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.FeeViewModel

@Composable
fun AddEditStudentDialog(
    studentToEdit: StudentEntity?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
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
    ) -> Unit
) {
    val isEdit = studentToEdit != null

    var fullName by remember { mutableStateOf(studentToEdit?.fullName ?: "") }
    var studentId by remember {
        mutableStateOf(studentToEdit?.studentId ?: "SCH-2026-${(100 + (10..99).random())}")
    }
    var grade by remember { mutableStateOf(studentToEdit?.grade ?: "Grade 10 - Science") }
    var parentName by remember { mutableStateOf(studentToEdit?.parentName ?: "") }
    var contactPhone by remember { mutableStateOf(studentToEdit?.contactPhone ?: "") }
    var contactEmail by remember { mutableStateOf(studentToEdit?.contactEmail ?: "") }

    var tuitionFeeText by remember {
        mutableStateOf(studentToEdit?.tuitionFee?.toString()?.removeSuffix(".0") ?: "2500")
    }
    var labFeeText by remember {
        mutableStateOf(studentToEdit?.labFee?.toString()?.removeSuffix(".0") ?: "300")
    }
    var libraryFeeText by remember {
        mutableStateOf(studentToEdit?.libraryFee?.toString()?.removeSuffix(".0") ?: "150")
    }
    var transportFeeText by remember {
        mutableStateOf(studentToEdit?.transportFee?.toString()?.removeSuffix(".0") ?: "200")
    }
    var discountText by remember {
        mutableStateOf(studentToEdit?.discount?.toString()?.removeSuffix(".0") ?: "0")
    }

    val dayMs = 24L * 60 * 60 * 1000
    var selectedDueDays by remember { mutableLongStateOf(14L) }
    var dueDateEpoch by remember {
        mutableLongStateOf(studentToEdit?.dueDateEpoch ?: (System.currentTimeMillis() + 14 * dayMs))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live calculations
    val tuition = tuitionFeeText.toDoubleOrNull() ?: 0.0
    val lab = labFeeText.toDoubleOrNull() ?: 0.0
    val library = libraryFeeText.toDoubleOrNull() ?: 0.0
    val transport = transportFeeText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val totalFee = tuition + lab + library + transport
    val netFee = (totalFee - discount).coerceAtLeast(0.0)

    val standardGrades = listOf(
        "Grade 9 - Standard",
        "Grade 10 - Science",
        "Grade 10 - Arts",
        "Grade 11 - Computer Science",
        "Grade 11 - Science",
        "Grade 12 - Commerce"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .testTag("add_edit_student_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Student Form",
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "Edit Student & Fees" else "Enroll New Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "Personal & Academic Info",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Student Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Roll / Student ID *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_id_input")
                    )

                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Grade / Class *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_grade_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Guardian / Contact Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent / Guardian Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_parent_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_phone_input")
                    )

                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_email_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Fee Structure Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tuitionFeeText,
                        onValueChange = { tuitionFeeText = it },
                        label = { Text("Tuition (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_tuition_input")
                    )

                    OutlinedTextField(
                        value = labFeeText,
                        onValueChange = { labFeeText = it },
                        label = { Text("Lab Fee (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_lab_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = libraryFeeText,
                        onValueChange = { libraryFeeText = it },
                        label = { Text("Library (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_library_input")
                    )

                    OutlinedTextField(
                        value = transportFeeText,
                        onValueChange = { transportFeeText = it },
                        label = { Text("Transport (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("student_transport_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Scholarship / Concession Discount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_discount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Due date selector
                Text(
                    text = "Payment Due In",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(7L to "7 Days", 14L to "14 Days", 30L to "30 Days", 60L to "60 Days").forEach { (days, label) ->
                        FilterChip(
                            selected = selectedDueDays == days,
                            onClick = {
                                selectedDueDays = days
                                dueDateEpoch = System.currentTimeMillis() + days * dayMs
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time calculation summary card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gross Total Fee:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FeeViewModel.formatCurrency(totalFee),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Concession / Scholarship:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "-${FeeViewModel.formatCurrency(discount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Academic Fee:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FeeViewModel.formatCurrency(netFee),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Student name cannot be empty"
                        return@Button
                    }
                    if (studentId.isBlank()) {
                        errorMessage = "Roll number / ID cannot be empty"
                        return@Button
                    }
                    if (netFee <= 0.0) {
                        errorMessage = "Net fee must be greater than ₹0"
                        return@Button
                    }

                    onSave(
                        studentToEdit?.id ?: 0L,
                        studentId.trim(),
                        fullName.trim(),
                        grade.trim(),
                        parentName.trim().ifBlank { "Parent/Guardian" },
                        contactPhone.trim(),
                        contactEmail.trim(),
                        tuition,
                        lab,
                        library,
                        transport,
                        discount,
                        dueDateEpoch
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("student_save_button")
            ) {
                Text(if (isEdit) "Update Records" else "Enroll Student")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("student_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
