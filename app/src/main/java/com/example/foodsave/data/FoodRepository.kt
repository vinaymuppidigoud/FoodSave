package com.example.foodsave.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao
) {

    // Observe food items
    fun getAllFood(userId: String): Flow<List<FoodItem>> {
        return foodDao.getAllFood(userId)
    }

    // Insert food
    suspend fun insertFood(food: FoodItem) {
        foodDao.insertFood(food)
    }

    // Update food
    suspend fun updateFood(food: FoodItem) {
        foodDao.updateFood(food)
    }

    // Mark food deleted
    suspend fun deleteFood(food: FoodItem) {
        foodDao.markDeleted(food.id, food.userId)
    }

    // Get pending records
    suspend fun getPendingFood(userId: String): List<FoodItem> {
        return foodDao.getPendingFood(userId)
    }

    // Get food list
    suspend fun getFoodList(userId: String): List<FoodItem> {
        return foodDao.getFoodList(userId)
    }

    // Update sync status
    suspend fun markSynced(
        id: Int,
        userId: String,
        imageUrl: String?
    ) {
        foodDao.markSynced(id, userId, imageUrl)
    }

    // Remove deleted records
    suspend fun removeDeletedFood(id: Int, userId: String) {
        foodDao.removeDeletedFood(id, userId)
    }

    // Insert or update
    suspend fun upsertFood(food: FoodItem) {
        foodDao.upsertFood(food)
    }
}