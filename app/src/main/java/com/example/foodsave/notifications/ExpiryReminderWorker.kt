package com.example.foodsave.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.foodsave.data.FoodDatabase
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // Get user ID
        val requestedUserId =
            inputData.getString("userId")
                ?: return Result.failure()

        return try {

            // Check authentication
            val currentUser =
                FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(
                    "ExpiryReminderWorker",
                    "User not logged in"
                )
                return Result.success()
            }

            // Verify user ID
            if (currentUser.uid != requestedUserId) {
                Log.w(
                    "ExpiryReminderWorker",
                    "User ID mismatch"
                )
                return Result.success()
            }

            // Open database
            val dao = FoodDatabase
                .getDatabase(applicationContext)
                .foodDao()

            // Get food records
            val foodItems =
                dao.getFoodList(requestedUserId)

            // Format dates
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).apply {
                isLenient = false
            }

            // Get today's date
            val today = formatter.parse(
                formatter.format(Date())
            ) ?: return Result.retry()

            // Set reminder range
            val endCalendar = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, 3)
            }
            Log.d(
                "ExpiryReminderWorker",
                "Checking ${foodItems.size} food items"
            )

            // Check expiry dates
            for (food in foodItems) {

                val expiry = formatter.parse(
                    food.expiryDate
                ) ?: continue

                // Check expiry range
                if (
                    !expiry.before(today) &&
                    !expiry.after(endCalendar.time)
                ) {

                    // Show expiry alert
                    NotificationHelper.showExpiryNotification(
                        context = applicationContext,
                        foodName = food.name,
                        expiryDate = food.expiryDate,
                        notificationId = food.id
                    )
                    Log.d(
                        "ExpiryReminderWorker",
                        "Reminder sent for ${food.name}"
                    )
                }
            }

            // Schedule next check
            val nextRequest =
                OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
                    .setInitialDelay(2, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf("userId" to requestedUserId)
                    )
                    .build()

            // Queue reminder work
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "foodsave_expiry_reminders_$requestedUserId",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    nextRequest
                )
            Log.d(
                "ExpiryReminderWorker",
                "Next expiry check scheduled"
            )
            Result.success()

        } catch (e: Exception) {

            // Handle errors
            Log.e(
                "ExpiryReminderWorker",
                "Expiry reminder failed",
                e
            )
            Result.retry()
        }
    }
}