package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.util.FeeNotificationHelper
import com.example.ui.components.SecurePaymentGatewaySheet
import com.example.ui.dialogs.AddEditStudentDialog
import com.example.ui.dialogs.AdminLoginDialog
import com.example.ui.dialogs.ReceiptDetailDialog
import com.example.ui.dialogs.RecordPaymentDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReceiptsScreen
import com.example.ui.screens.StudentPortalScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.FeeViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScholarFeeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarFeeApp(
    viewModel: FeeViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val activePaymentStudent by viewModel.activePaymentStudent.collectAsStateWithLifecycle()
    val showGatewayForStudent by viewModel.showGatewayForStudent.collectAsStateWithLifecycle()
    val isGatewayProcessing by viewModel.isGatewayProcessing.collectAsStateWithLifecycle()
    val activeReceiptForView by viewModel.activeReceiptForView.collectAsStateWithLifecycle()
    val showAddStudentDialog by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val studentToEdit by viewModel.studentToEdit.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        FeeNotificationHelper.createNotificationChannel(context)
    }

    LaunchedEffect(Unit) {
        FeeNotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Predictive back handler: if not on Dashboard/Portal, return to home
    BackHandler(enabled = currentTab != AppTab.DASHBOARD && currentTab != AppTab.STUDENT_PORTAL) {
        viewModel.setTab(if (isAdminLoggedIn) AppTab.DASHBOARD else AppTab.STUDENT_PORTAL)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ScholarFee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isAdminLoggedIn && currentTab == AppTab.DASHBOARD) "Admin Dashboard (Bursar)"
                                else if (currentTab == AppTab.STUDENT_PORTAL) "Student Self-Service Portal"
                                else "School Accounts Hub",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (isAdminLoggedIn) {
                        // Admin Logged In Badge & Logout action
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    // Toggle between Admin Dashboard and Student Portal preview
                                    if (currentTab == AppTab.STUDENT_PORTAL) {
                                        viewModel.setTab(AppTab.DASHBOARD)
                                    } else {
                                        viewModel.setTab(AppTab.STUDENT_PORTAL)
                                    }
                                }
                                .testTag("admin_role_badge"),
                            color = StatusPaidBg,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Active",
                                    tint = StatusPaid,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentTab == AppTab.STUDENT_PORTAL) "View Admin Hub" else "Admin Active",
                                    color = StatusPaid,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.logoutAdmin() },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("top_bar_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Not logged in: Show Admin Sign In Button with Lock Icon
                        Button(
                            onClick = { viewModel.openAdminLogin() },
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .height(36.dp)
                                .testTag("top_bar_admin_login_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Admin Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = {
                        if (!isAdminLoggedIn) {
                            viewModel.openAdminLogin()
                        } else {
                            viewModel.setTab(AppTab.DASHBOARD)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isAdminLoggedIn) Icons.Default.Dashboard else Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Dashboard"
                        )
                    },
                    label = { Text(if (isAdminLoggedIn) "Admin Hub" else "Admin Login", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENT_PORTAL,
                    onClick = { viewModel.setTab(AppTab.STUDENT_PORTAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Student Portal"
                        )
                    },
                    label = { Text("Student", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_tab_student_portal")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENTS,
                    onClick = { viewModel.setTab(AppTab.STUDENTS) },
                    icon = {
                        if (metrics.overdueCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = StatusOverdue) {
                                        Text("${metrics.overdueCount}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Roster"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Roster"
                            )
                        }
                    },
                    label = { Text("Roster", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_students")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.RECEIPTS,
                    onClick = { viewModel.setTab(AppTab.RECEIPTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipts"
                        )
                    },
                    label = { Text("Receipts", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_receipts")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ANALYTICS,
                    onClick = { viewModel.setTab(AppTab.ANALYTICS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Analytics", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.STUDENT_PORTAL -> StudentPortalScreen(viewModel = viewModel)
                AppTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                AppTab.RECEIPTS -> ReceiptsScreen(viewModel = viewModel)
                AppTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal: Admin Login Dialog (User ID & Password)
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { viewModel.closeAdminLogin() },
            onLogin = { userId, password -> viewModel.loginAdmin(userId, password) }
        )
    }

    // Modal: Secure Payment Gateway (Stripe & PayPal)
    if (showGatewayForStudent != null) {
        SecurePaymentGatewaySheet(
            student = showGatewayForStudent!!,
            isProcessing = isGatewayProcessing,
            onDismiss = { viewModel.closeGatewayPayment() },
            onProcessPayment = { amount, gateway, reference, notes ->
                viewModel.processGatewayPayment(
                    studentId = showGatewayForStudent!!.id,
                    amount = amount,
                    gatewayName = gateway,
                    transactionReference = reference,
                    notes = notes
                )
            }
        )
    }

    // Modal: Standard Admin Record Payment Dialog
    if (activePaymentStudent != null) {
        RecordPaymentDialog(
            student = activePaymentStudent!!,
            onDismiss = { viewModel.closePaymentDialog() },
            onConfirm = { studentId, amount, method, reference, notes ->
                viewModel.recordPayment(
                    studentId = studentId,
                    amount = amount,
                    paymentMethod = method,
                    referenceNumber = reference,
                    notes = notes
                )
            }
        )
    }

    // Modal: Automated Official Receipt Details & Sharing
    if (activeReceiptForView != null) {
        ReceiptDetailDialog(
            receipt = activeReceiptForView!!,
            onDismiss = { viewModel.closeReceiptView() }
        )
    }

    // Modal: Enroll New Student / Edit Student & Fee Breakdown
    if (showAddStudentDialog) {
        AddEditStudentDialog(
            studentToEdit = studentToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, studentId, fullName, grade, parentName, contactPhone, contactEmail, tuition, lab, library, transport, discount, dueDateEpoch ->
                viewModel.saveStudent(
                    id = id,
                    studentId = studentId,
                    fullName = fullName,
                    grade = grade,
                    parentName = parentName,
                    contactPhone = contactPhone,
                    contactEmail = contactEmail,
                    tuitionFee = tuition,
                    labFee = lab,
                    libraryFee = library,
                    transportFee = transport,
                    discount = discount,
                    dueDateEpoch = dueDateEpoch
                )
            }
        )
    }
}
