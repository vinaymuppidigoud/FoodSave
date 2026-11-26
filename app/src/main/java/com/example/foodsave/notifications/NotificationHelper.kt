package com.example.foodsave.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.foodsave.R

object NotificationHelper {

    private const val CHANNEL_ID = "foodsave_expiry_channel"

    // Create channel
    fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Configure channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Food Expiry Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Notifications for food items approaching expiry"
            }

            // Get notification manager
            val manager = context.getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    // Show expiry alert
    fun showExpiryNotification(
        context: Context,
        foodName: String,
        expiryDate: String,
        notificationId: Int
    ) {

        // Check permission
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Create channel
        createNotificationChannel(context)

        // Build notification
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Food Expiry Reminder")
            .setContentText(
                "$foodName is approaching its expiry date: $expiryDate"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "$foodName is approaching its expiry date. Please check your pantry."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Display notification
        NotificationManagerCompat.from(context).notify(
            notificationId,
            notification
        )
    }
}