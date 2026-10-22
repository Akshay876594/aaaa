package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPartial
import com.example.ui.viewmodel.FeeViewModel

@Composable
fun AnalyticsScreen(
    viewModel: FeeViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()

    // Group students by grade
    val gradeBreakdown = allStudents.groupBy { it.grade }.map { (grade, students) ->
        val totalExpected = students.sumOf { it.netFee }
        val totalPaid = students.sumOf { it.paidAmount }
        val balance = students.sumOf { it.outstandingBalance }
        val progress = if (totalExpected > 0) (totalPaid / totalExpected).toFloat() else 0f
        GradeAnalytics(grade, totalExpected, totalPaid, balance, students.size, progress)
    }

    // Payment methods breakdown
    val paymentMethods = allReceipts.groupBy {
        when {
            it.paymentMethod.contains("Stripe", ignoreCase = true) || it.paymentMethod.contains("Card", ignoreCase = true) -> "Stripe / Card"
            it.paymentMethod.contains("PayPal", ignoreCase = true) -> "PayPal Express"
            it.paymentMethod.contains("Bank", ignoreCase = true) -> "Bank Transfer"
            it.paymentMethod.contains("Cash", ignoreCase = true) -> "Cash Desk"
            it.paymentMethod.contains("UPI", ignoreCase = true) -> "UPI / Mobile"
            else -> "Other"
        }
    }.map { (method, receipts) ->
        val sum = receipts.sumOf { it.amountPaid }
        val count = receipts.size
        MethodAnalytics(method, sum, count)
    }.sortedByDescending { it.amount }

    // Fee component totals
    val tuitionTotal = allStudents.sumOf { it.tuitionFee }
    val labTotal = allStudents.sumOf { it.labFee }
    val libraryTotal = allStudents.sumOf { it.libraryFee }
    val transportTotal = allStudents.sumOf { it.transportFee }
    val discountTotal = allStudents.sumOf { it.discount }
    val grossTotal = tuitionTotal + labTotal + libraryTotal + transportTotal

    LazyColumn(
        modifier = modifier
            .testTag("analytics_screen")
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Financial Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Fee recovery benchmarks, departmental performance & payment channels",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Overall Performance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_performance_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Overall Fee Collection Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${(metrics.collectionRate * 100).toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = StatusPaid
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Recovered: ${FeeViewModel.formatCurrency(metrics.totalCollected)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                            Text(
                                text = "Pending: ${FeeViewModel.formatCurrency(metrics.totalOutstanding)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { metrics.collectionRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = StatusPaid,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${metrics.paidCount} of ${metrics.totalStudents} accounts fully settled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (metrics.overdueCount > 0) {
                            Text(
                                text = "${metrics.overdueCount} accounts overdue",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusOverdue
                            )
                        }
                    }
                }
            }
        }

        // Grade / Class Performance Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_grades_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Collection by Class & Cohort",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    gradeBreakdown.forEach { item ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.grade,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${FeeViewModel.formatCurrency(item.collected)} / ${FeeViewModel.formatCurrency(item.expected)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (item.progress >= 0.8f) StatusPaid else if (item.progress >= 0.5f) StatusPartial else PrimaryBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Payment Gateway & Channels Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_channels_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Payment Gateways & Inflow Channels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalChannelVolume = paymentMethods.sumOf { it.amount }

                    if (paymentMethods.isEmpty()) {
                        Text(
                            text = "No payments logged yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        paymentMethods.forEach { method ->
                            val share = if (totalChannelVolume > 0) (method.amount / totalChannelVolume).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${method.name} (${method.count} txns)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${FeeViewModel.formatCurrency(method.amount)} (${(share * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { share },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PrimaryBlue,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fee Structure Composition
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_composition_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Institution Fee Structure Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CompositionRow("Core Tuition Fees", tuitionTotal, grossTotal)
                    CompositionRow("Laboratory & IT Infrastructure", labTotal, grossTotal)
                    CompositionRow("Library & Scholarly Resources", libraryTotal, grossTotal)
                    CompositionRow("Campus Transport & Athletics", transportTotal, grossTotal)

                    if (discountTotal > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Scholarship Concessions Granted",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusPaid,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "-${FeeViewModel.formatCurrency(discountTotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaid
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CompositionRow(label: String, amount: Double, total: Double) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${FeeViewModel.formatCurrency(amount)} ($percentage%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class GradeAnalytics(
    val grade: String,
    val expected: Double,
    val collected: Double,
    val balance: Double,
    val studentCount: Int,
    val progress: Float
)

private data class MethodAnalytics(
    val name: String,
    val amount: Double,
    val count: Int
)
