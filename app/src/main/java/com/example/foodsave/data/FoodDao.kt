package com.example.foodsave.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // Add food
    @Insert
    suspend fun insertFood(food: FoodItem)

    // Get user food
    @Query("""
        SELECT * FROM food_items
        WHERE userId = :userId
        ORDER BY expiryDate ASC
    """)
    fun getAllFood(userId: String): Flow<List<FoodItem>>

    // Update food
    @Update
    suspend fun updateFood(food: FoodItem)

    // Delete food
    @Delete
    suspend fun deleteFood(food: FoodItem)

    // Clear user food
    @Query("""
        DELETE FROM food_items
        WHERE userId = :userId
    """)
    suspend fun deleteUserFood(userId: String)
}