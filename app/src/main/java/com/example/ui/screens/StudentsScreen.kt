package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentStatus
import com.example.ui.components.StudentCard
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.FeeViewModel

@Composable
fun StudentsScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedGrade by viewModel.selectedGradeFilter.collectAsStateWithLifecycle()

    val grades = listOf("All Grades") + allStudents.map { it.grade }.distinct()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .testTag("students_screen")
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Student Fee Roster",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track balances, record installments, and issue receipts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by student, ID, or guardian...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("students_search_input")
                )
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All (${allStudents.size})", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_status_all")
                    )

                    FilterChip(
                        selected = selectedStatus == PaymentStatus.OVERDUE,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (selectedStatus == PaymentStatus.OVERDUE) null else PaymentStatus.OVERDUE
                            )
                        },
                        label = { Text("Overdue", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_status_overdue")
                    )

                    FilterChip(
                        selected = selectedStatus == PaymentStatus.PARTIAL,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (selectedStatus == PaymentStatus.PARTIAL) null else PaymentStatus.PARTIAL
                            )
                        },
                        label = { Text("Partial", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_status_partial")
                    )

                    FilterChip(
                        selected = selectedStatus == PaymentStatus.PAID,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (selectedStatus == PaymentStatus.PAID) null else PaymentStatus.PAID
                            )
                        },
                        label = { Text("Paid in Full", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_status_paid")
                    )

                    FilterChip(
                        selected = selectedStatus == PaymentStatus.UNPAID,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (selectedStatus == PaymentStatus.UNPAID) null else PaymentStatus.UNPAID
                            )
                        },
                        label = { Text("Unpaid", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_status_unpaid")
                    )
                }
            }

            // Academic Program & Course Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGrade == null,
                        onClick = { viewModel.setGradeFilter(null) },
                        label = { Text("All Programs", fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_course_all")
                    )

                    listOf("BCA", "B.Ed", "M.Sc Zoology", "M.Sc Chemistry", "M.Sc Physics", "M.Sc").forEach { courseName ->
                        val isSelected = selectedGrade != null && selectedGrade!!.startsWith(courseName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setGradeFilter(if (isSelected) null else courseName)
                            },
                            label = { Text(courseName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_course_${courseName.replace(" ", "_").lowercase()}")
                        )
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "Showing ${filteredStudents.size} of ${allStudents.size} students",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Student Cards
            if (filteredStudents.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No students match your search criteria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        onRecordPayment = { viewModel.openGatewayPayment(student) },
                        onEditStudent = { viewModel.openEditStudentDialog(student) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
            }
        }

        // Floating Action Button to add a new student
        FloatingActionButton(
            onClick = { viewModel.openAddStudentDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_student_fab"),
            containerColor = PrimaryBlue,
            contentColor = androidx.compose.ui.graphics.Color.White
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Enroll Student"
            )
        }
    }
}
