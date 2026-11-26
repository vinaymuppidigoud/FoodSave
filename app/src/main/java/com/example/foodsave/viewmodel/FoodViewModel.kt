package com.example.foodsave.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.foodsave.camera.FoodImageManager
import com.example.foodsave.data.FoodDatabase
import com.example.foodsave.data.FoodItem
import com.example.foodsave.data.FoodRepository
import com.example.foodsave.notifications.ExpiryReminderWorker
import com.example.foodsave.workers.FoodSyncWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class FoodViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository: FoodRepository

    private val auth = FirebaseAuth.getInstance()

    private val workManager =
        WorkManager.getInstance(application)

    // Get current user
    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    // Store food items
    private val _foodItems =
        MutableStateFlow<List<FoodItem>>(emptyList())

    val foodItems: StateFlow<List<FoodItem>> =
        _foodItems.asStateFlow()

    // Store error messages
    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    private var foodCollectionJob: Job? = null

    // Track authentication changes
    private val authListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->

            loadFood()

            if (firebaseAuth.currentUser != null) {
                scheduleExpiryReminders()
                syncFood()
            }
        }

    init {

        // Initialise database
        val dao = FoodDatabase
            .getDatabase(application)
            .foodDao()

        repository = FoodRepository(dao)

        // Register auth listener
        auth.addAuthStateListener(authListener)
    }

    // Load user's food
    private fun loadFood() {
        val currentUserId = userId
        foodCollectionJob?.cancel()
        _foodItems.value = emptyList()
        if (currentUserId.isBlank()) return
        foodCollectionJob = viewModelScope.launch {

            try {

                repository.getAllFood(currentUserId)
                    .collect { items ->

                        if (userId == currentUserId) {
                            _foodItems.value = items
                        }
                    }

            } catch (e: Exception) {

                Log.e(
                    "FoodViewModel",
                    "Unable to load food items",
                    e
                )
                _errorMessage.value =
                    "Unable to load food items."
            }
        }
    }

    // Refresh food
    fun refreshFood() {
        loadFood()
        scheduleExpiryReminders()
        syncFood()
    }

    // Add new food
    fun addFood(
        name: String,
        quantity: Int,
        expiryDate: String,
        imagePath: String? = null
    ) {

        val currentUserId = userId

        if (currentUserId.isBlank()) {
            _errorMessage.value = "Please log in again."
            return
        }

        // Check food details
        if (
            name.isBlank() ||
            quantity <= 0 ||
            !isValidDate(expiryDate)
        ) {
            _errorMessage.value =
                "Enter valid food details."
            return
        }

        viewModelScope.launch {

            try {
                // Save food locally
                repository.insertFood(
                    FoodItem(
                        userId = currentUserId,
                        name = name.trim(),
                        quantity = quantity,
                        expiryDate = expiryDate,
                        imagePath = imagePath,
                        isSynced = false
                    )
                )
                _errorMessage.value = null
                syncFood()
                scheduleExpiryReminders()

            } catch (e: Exception) {

                Log.e(
                    "FoodViewModel",
                    "Food insert failed",
                    e
                )
                _errorMessage.value =
                    "Unable to save food item."
            }
        }
    }

    // Update food
    fun updateFood(food: FoodItem) {

        val currentUserId = userId

        // Check user access
        if (
            currentUserId.isBlank() ||
            food.userId != currentUserId
        ) {
            _errorMessage.value =
                "You are not authorised to update this item."
            return
        }

        // Validate food details
        if (
            food.name.isBlank() ||
            food.quantity <= 0 ||
            !isValidDate(food.expiryDate)
        ) {
            _errorMessage.value =
                "Enter valid food details."
            return
        }

        viewModelScope.launch {
            try {
                val existingFood = _foodItems.value
                    .find { it.id == food.id }

                // Save updated food
                repository.updateFood(
                    food.copy(
                        name = food.name.trim(),
                        isSynced = false,
                        isDeleted = false
                    )
                )

                // Remove old image
                if (
                    existingFood?.imagePath != null &&
                    existingFood.imagePath != food.imagePath
                ) {

                    FoodImageManager.deleteImage(
                        existingFood.imagePath
                    )
                }
                _errorMessage.value = null
                syncFood()
                scheduleExpiryReminders()

            } catch (e: Exception) {

                Log.e(
                    "FoodViewModel",
                    "Food update failed",
                    e
                )
                _errorMessage.value =
                    "Unable to update food item."
            }
        }
    }

    // Delete food
    fun deleteFood(food: FoodItem) {

        val currentUserId = userId

        // Check user access
        if (
            currentUserId.isBlank() ||
            food.userId != currentUserId
        ) {
            _errorMessage.value =
                "You are not authorised to delete this item."
            return
        }

        viewModelScope.launch {
            try {

                // Remove food locally
                repository.deleteFood(food)

                // Delete stored image
                food.imagePath?.let { path ->
                    FoodImageManager.deleteImage(path)
                }
                _errorMessage.value = null
                syncFood()
                scheduleExpiryReminders()

            } catch (e: Exception) {

                Log.e(
                    "FoodViewModel",
                    "Food deletion failed",
                    e
                )
                _errorMessage.value =
                    "Unable to delete food item."
            }
        }
    }

    // Schedule cloud sync
    fun syncFood() {

        val currentUserId = userId

        if (currentUserId.isBlank()) {

            Log.e(
                "FoodViewModel",
                "Sync cancelled: User not logged in"
            )

            return
        }

        // Require network access
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create sync request
        val request =
            OneTimeWorkRequestBuilder<FoodSyncWorker>()
                .setConstraints(constraints)
                .setInputData(
                    workDataOf("userId" to currentUserId)
                )
                .build()

        // Queue sync work
        workManager.enqueueUniqueWork(
            "foodsave_sync_$currentUserId",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )

        Log.d(
            "FoodViewModel",
            "Sync request enqueued for $currentUserId"
        )
    }

    // Schedule expiry checks
    fun scheduleExpiryReminders() {

        val currentUserId = userId

        if (currentUserId.isBlank()) return

        // Cancel previous reminder
        workManager.cancelUniqueWork(
            "foodsave_expiry_reminders"
        )

        // Create reminder request
        val request =
            OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
                .setInitialDelay(2, TimeUnit.MINUTES)
                .setInputData(
                    workDataOf("userId" to currentUserId)
                )
                .build()

        // Queue reminder work
        workManager.enqueueUniqueWork(
            "foodsave_expiry_reminders_$currentUserId",
            ExistingWorkPolicy.KEEP,
            request
        )
        Log.d(
            "FoodViewModel",
            "Expiry reminder scheduled for $currentUserId"
        )
    }

    // Check date format
    private fun isValidDate(date: String): Boolean {

        return try {
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )
            formatter.isLenient = false
            val parsed = formatter.parse(date)
            parsed != null &&
                    formatter.format(parsed) == date

        } catch (e: Exception) {
            false
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    // Clean up resources
    override fun onCleared() {
        foodCollectionJob?.cancel()
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }
}