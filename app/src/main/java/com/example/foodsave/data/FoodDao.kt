package com.example.foodsave.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // Insert food
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItem)

    // Insert or update food
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFood(food: FoodItem)

    // Get active food belonging to a user
    @Query("""
        SELECT * FROM food_items
        WHERE userId = :userId
        AND isDeleted = 0
        ORDER BY expiryDate ASC
    """)
    fun getAllFood(userId: String): Flow<List<FoodItem>>

    // Get all active food records
    @Query("""
        SELECT * FROM food_items
        WHERE userId = :userId
        AND isDeleted = 0
    """)
    suspend fun getFoodList(userId: String): List<FoodItem>

    // Get unsynchronised records
    @Query("""
        SELECT * FROM food_items
        WHERE userId = :userId
        AND isSynced = 0
    """)
    suspend fun getPendingFood(userId: String): List<FoodItem>

    // Find a food item
    @Query("""
        SELECT * FROM food_items
        WHERE id = :id
        AND userId = :userId
    """)
    suspend fun getFoodById(id: Int, userId: String): FoodItem?

    // Update food
    @Update
    suspend fun updateFood(food: FoodItem)

    // Delete a food item
    @Delete
    suspend fun deleteFood(food: FoodItem)

    // Mark food as deleted
    @Query("""
        UPDATE food_items
        SET isDeleted = 1, isSynced = 0
        WHERE id = :id AND userId = :userId
    """)
    suspend fun markDeleted(id: Int, userId: String)

    // Mark item synchronised
    @Query("""
        UPDATE food_items
        SET isSynced = 1, imageUrl = :imageUrl
        WHERE id = :id AND userId = :userId
    """)
    suspend fun markSynced(
        id: Int,
        userId: String,
        imageUrl: String?
    )

    // Remove item after cloud deletion
    @Query("""
        DELETE FROM food_items
        WHERE id = :id AND userId = :userId
    """)
    suspend fun removeDeletedFood(id: Int, userId: String)

    // Clear user's food records
    @Query("""
        DELETE FROM food_items
        WHERE userId = :userId
    """)
    suspend fun deleteUserFood(userId: String)
}