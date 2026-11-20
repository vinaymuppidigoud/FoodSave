package com.example.foodsave.data

import kotlinx.coroutines.flow.Flow

// Food data repository
class FoodRepository(
    private val foodDao: FoodDao
) {

    // Get user food
    fun getAllFood(userId: String): Flow<List<FoodItem>> {
        return foodDao.getAllFood(userId)
    }

    // Add food
    suspend fun insertFood(food: FoodItem) {
        foodDao.insertFood(food)
    }

    // Update food
    suspend fun updateFood(food: FoodItem) {
        foodDao.updateFood(food)
    }

    // Delete food
    suspend fun deleteFood(food: FoodItem) {
        foodDao.deleteFood(food)
    }
}