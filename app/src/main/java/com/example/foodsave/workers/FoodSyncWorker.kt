package com.example.foodsave.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.foodsave.data.FoodDatabase
import com.example.foodsave.data.FoodRepository
import com.example.foodsave.firebase.FirestoreManager
import com.google.firebase.auth.FirebaseAuth

class FoodSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // Get requested user
        val requestedUserId =
            inputData.getString("userId")
                ?: return Result.failure()

        return try {

            // Check authentication
            val currentUser = FirebaseAuth.getInstance()
                .currentUser

            // Wait for Firebase authentication
            if (currentUser == null) {
                Log.w(
                    "FoodSyncWorker",
                    "User not authenticated. Retrying."
                )
                return Result.retry()
            }

            // Verify user identity
            val userId = currentUser.uid

            // Prevent cross-user sync
            if (userId != requestedUserId) {
                Log.e(
                    "FoodSyncWorker",
                    "User ID mismatch. Sync cancelled."
                )
                return Result.failure()
            }

            // Initialise local database
            val dao = FoodDatabase
                .getDatabase(applicationContext)
                .foodDao()

            val repository = FoodRepository(dao)

            val firestoreManager = FirestoreManager()

            Log.d(
                "FoodSyncWorker",
                "Starting sync for $userId"
            )

            // Get pending records
            val pendingItems =
                repository.getPendingFood(userId)

            Log.d(
                "FoodSyncWorker",
                "Pending items: ${pendingItems.size}"
            )

            // Upload pending records
            for (food in pendingItems) {

                if (food.isDeleted) {

                    // Delete cloud record
                    firestoreManager.deleteFood(food)

                    // Remove local record
                    repository.removeDeletedFood(
                        food.id,
                        userId
                    )

                    Log.d(
                        "FoodSyncWorker",
                        "Deleted: ${food.name}"
                    )

                } else {

                    // Prepare cloud data
                    // Keep image locally
                    val cloudFood = food.copy(
                        imagePath = null,
                        imageUrl = null
                    )

                    // Upload to Firestore
                    firestoreManager.uploadFood(cloudFood)

                    // Update sync status
                    repository.markSynced(
                        food.id,
                        userId,
                        null
                    )

                    Log.d(
                        "FoodSyncWorker",
                        "Uploaded successfully: ${food.name}"
                    )
                }
            }

            // Fetch cloud records
            val cloudItems =
                firestoreManager.getFoodItems()

            Log.d(
                "FoodSyncWorker",
                "Cloud records: ${cloudItems.size}"
            )

            // Merge local records
            for (cloudFood in cloudItems) {

                val localFood = dao.getFoodById(
                    cloudFood.id,
                    userId
                )

                if (localFood == null) {

                    // Insert missing record
                    repository.upsertFood(
                        cloudFood.copy(
                            imagePath = null,
                            imageUrl = null
                        )
                    )

                } else if (
                    localFood.isSynced &&
                    !localFood.isDeleted
                ) {

                    // Update existing record
                    repository.upsertFood(
                        cloudFood.copy(
                            imagePath = localFood.imagePath,
                            imageUrl = null
                        )
                    )
                }
            }

            Log.d(
                "FoodSyncWorker",
                "Synchronization completed successfully"
            )
            Result.success()

        } catch (e: Exception) {

            // Handle sync errors
            Log.e(
                "FoodSyncWorker",
                "Synchronization failed",
                e
            )
            Result.retry()
        }
    }
}