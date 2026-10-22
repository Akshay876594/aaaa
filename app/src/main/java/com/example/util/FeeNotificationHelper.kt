package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.StudentEntity
import com.example.ui.viewmodel.FeeViewModel
import kotlin.math.ceil

object FeeNotificationHelper {
    const val CHANNEL_ID = "fee_due_reminders_channel"
    const val CHANNEL_NAME = "Fee Payment Reminders"
    const val CHANNEL_DESCRIPTION = "Automated reminders for students with fees due within 3 days"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if the student has an outstanding balance and their due date is within the next 3 days (0 < diff <= 3 days).
     */
    fun isFeeDueWithin3Days(student: StudentEntity): Boolean {
        if (student.outstandingBalance <= 0.01) return false
        val now = System.currentTimeMillis()
        val diffMs = student.dueDateEpoch - now
        val threeDaysMs = 3L * 24 * 60 * 60 * 1000
        return diffMs in 1..threeDaysMs
    }

    /**
     * Calculates days remaining until due date (e.g. 1, 2, or 3 days).
     */
    fun getDaysUntilDue(student: StudentEntity): Int {
        val diffMs = student.dueDateEpoch - System.currentTimeMillis()
        if (diffMs <= 0) return 0
        val dayMs = 24.0 * 60 * 60 * 1000
        return ceil(diffMs / dayMs).toInt().coerceAtLeast(1)
    }

    /**
     * Dispatches an Android push notification reminder for a student whose fee is due within 3 days.
     */
    fun sendFeeDueNotification(context: Context, student: StudentEntity): Boolean {
        createNotificationChannel(context)

        // On Android 13+ check POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        val daysLeft = getDaysUntilDue(student)
        val formattedBalance = FeeViewModel.formatCurrency(student.outstandingBalance)
        val formattedDueDate = FeeViewModel.formatDate(student.dueDateEpoch)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            student.id.toInt() + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ Fee Due in $daysLeft Day${if (daysLeft > 1) "s" else ""}: ${student.fullName}"
        val shortContent = "Outstanding balance of $formattedBalance is due on $formattedDueDate."
        val bigContent = """
            Dear ${student.parentName},

            This is an automated reminder that the tuition fee balance for ${student.fullName} (${student.studentId}, ${student.grade}) of $formattedBalance is due in $daysLeft day(s) on $formattedDueDate.

            Please clear fees via Stripe, PayPal, or at the school accounts desk.
        """.trimIndent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Pay Now",
                pendingIntent
            )
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(
                (student.id.toInt() * 100) + 1,
                notification
            )
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Checks all students and automatically dispatches push notification reminders
     * for all students whose due date is within the next 3 days.
     */
    fun sendAutomatedRemindersForEligibleStudents(
        context: Context,
        students: List<StudentEntity>
    ): List<StudentEntity> {
        val eligibleStudents = students.filter { isFeeDueWithin3Days(it) }
        val dispatched = mutableListOf<StudentEntity>()

        for (student in eligibleStudents) {
            val sent = sendFeeDueNotification(context, student)
            if (sent) {
                dispatched.add(student)
            }
        }

        return dispatched
    }
}
