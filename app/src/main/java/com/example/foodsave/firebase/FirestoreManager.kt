package com.example.foodsave.firebase

import android.util.Log
import com.example.foodsave.data.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    private val storage =
        FirebaseStorage.getInstance()

    // Upload food data
    suspend fun uploadFood(food: FoodItem) {

        val userId = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        // Verify ownership
        if (food.userId != userId) {
            throw SecurityException(
                "Unauthorised user"
            )
        }

        // Prepare food data
        val foodData = hashMapOf(
            "id" to food.id,
            "userId" to userId,
            "name" to food.name,
            "quantity" to food.quantity,
            "expiryDate" to food.expiryDate,
            "imageUrl" to food.imageUrl
        )

        try {

            Log.d(
                "FirestoreDebug",
                "Uploading food: ${food.name}"
            )

            // Save cloud record
            firestore.collection("users")
                .document(userId)
                .collection("foodItems")
                .document(food.id.toString())
                .set(foodData)
                .await()

            Log.d(
                "FirestoreDebug",
                "SUCCESS: ${food.name} uploaded"
            )

        } catch (e: Exception) {
            Log.e(
                "FirestoreDebug",
                "UPLOAD FAILED: ${e.message}",
                e
            )
            throw e
        }
    }

    // Delete food data
    suspend fun deleteFood(food: FoodItem) {

        val userId = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        // Verify ownership
        if (food.userId != userId) {
            throw SecurityException(
                "Unauthorised user"
            )
        }

        // Remove cloud record
        firestore.collection("users")
            .document(userId)
            .collection("foodItems")
            .document(food.id.toString())
            .delete()
            .await()

        Log.d(
            "FirestoreDebug",
            "Food deleted: ${food.name}"
        )

        // Remove stored image
        try {
            storage.reference
                .child(
                    "food_images/$userId/${food.id}.jpg"
                )
                .delete()
                .await()

        } catch (e: Exception) {
            Log.w(
                "FirestoreDebug",
                "Storage image not found or could not be deleted",
                e
            )
        }
    }

    // Get cloud records
    suspend fun getFoodItems(): List<FoodItem> {

        val userId = auth.currentUser?.uid
            ?: throw Exception("User not logged in")

        // Fetch user records
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("foodItems")
            .get()
            .await()

        // Convert cloud records
        return snapshot.documents.mapNotNull { document ->

            val id = document.getLong("id")?.toInt()
                ?: return@mapNotNull null

            val name = document.getString("name")
                ?: return@mapNotNull null

            val quantity =
                document.getLong("quantity")?.toInt()
                    ?: return@mapNotNull null

            val expiryDate =
                document.getString("expiryDate")
                    ?: return@mapNotNull null

            FoodItem(
                id = id,
                userId = userId,
                name = name,
                quantity = quantity,
                expiryDate = expiryDate,
                imageUrl = document.getString("imageUrl"),
                imagePath = null,
                isSynced = true,
                isDeleted = false
            )
        }
    }
}