package com.example.foodsave.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodsave.data.FoodDatabase
import com.example.foodsave.data.FoodItem
import com.example.foodsave.data.FoodRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Food screen ViewModel
class FoodViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository: FoodRepository

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Current user ID
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    // Food items state
    private val _foodItems =
        MutableStateFlow<List<FoodItem>>(emptyList())

    val foodItems: StateFlow<List<FoodItem>> =
        _foodItems.asStateFlow()

    // Error message state
    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    // Collection job
    private var foodCollectionJob: Job? = null

    init {
        val dao = FoodDatabase
            .getDatabase(application)
            .foodDao()

        repository = FoodRepository(dao)

        // Load saved food
        loadFood()
    }

    // Refresh food list
    fun refreshFood() {
        loadFood()
    }

    // Load user food
    private fun loadFood() {
        val currentUserId = userId

        // Cancel previous job
        foodCollectionJob?.cancel()

        // Clear old data
        _foodItems.value = emptyList()

        if (currentUserId.isBlank()) {
            return
        }

        foodCollectionJob = viewModelScope.launch {
            try {
                repository.getAllFood(currentUserId).collect { items ->
                    // Check current user
                    if (userId == currentUserId) {
                        _foodItems.value = items
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to load food items."
            }
        }
    }

    // Add food item
    fun addFood(
        name: String,
        quantity: Int,
        expiryDate: String
    ) {
        val currentUserId = userId

        if (currentUserId.isBlank()) {
            _errorMessage.value = "Please log in again."
            return
        }

        if (name.isBlank() || quantity <= 0) {
            _errorMessage.value =
                "Enter a valid food name and quantity."
            return
        }

        if (!isValidDate(expiryDate)) {
            _errorMessage.value =
                "Enter expiry date as yyyy-MM-dd."
            return
        }

        viewModelScope.launch {
            try {
                repository.insertFood(
                    FoodItem(
                        userId = currentUserId,
                        name = name.trim(),
                        quantity = quantity,
                        expiryDate = expiryDate
                    )
                )

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to save food item."
            }
        }
    }

    // Update food item
    fun updateFood(food: FoodItem) {
        val currentUserId = userId

        if (currentUserId.isBlank() ||
            food.userId != currentUserId
        ) {
            _errorMessage.value =
                "You are not authorised to update this item."
            return
        }

        if (food.name.isBlank() ||
            food.quantity <= 0 ||
            !isValidDate(food.expiryDate)
        ) {
            _errorMessage.value =
                "Enter valid food details."
            return
        }

        viewModelScope.launch {
            try {
                repository.updateFood(
                    food.copy(name = food.name.trim())
                )

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to update food item."
            }
        }
    }

    // Delete food item
    fun deleteFood(food: FoodItem) {
        val currentUserId = userId

        if (currentUserId.isBlank() ||
            food.userId != currentUserId
        ) {
            _errorMessage.value =
                "You are not authorised to delete this item."
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteFood(food)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to delete food item."
            }
        }
    }

    // Validate expiry date
    private fun isValidDate(date: String): Boolean {
        return try {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

            formatter.isLenient = false

            val parsedDate = formatter.parse(date)

            parsedDate != null &&
                    formatter.format(parsedDate) == date
        } catch (e: Exception) {
            false
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    // Clear ViewModel
    override fun onCleared() {
        foodCollectionJob?.cancel()
        super.onCleared()
    }
}