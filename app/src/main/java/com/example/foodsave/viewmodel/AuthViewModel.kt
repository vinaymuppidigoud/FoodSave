package com.example.foodsave.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.foodsave.firebase.AuthManager

class AuthViewModel : ViewModel() {

    // Auth manager
    private val authManager = AuthManager()
    // Loading state
    var isLoading = mutableStateOf(false)
        private set
    // Error state
    var errorMessage = mutableStateOf<String?>(null)
        private set

    // Register user
    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        // Firebase register
        authManager.register(email, password) { success, error ->
            isLoading.value = false
            if (success) {
                // Register success
                onSuccess()
            } else {
                // Register error
                errorMessage.value = error
            }
        }
    }

    // Login user
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading.value = true
        errorMessage.value = null

        // Firebase login
        authManager.login(email, password) { success, error ->
            isLoading.value = false
            if (success) {
                // Login success
                onSuccess()
            } else {
                // Login error
                errorMessage.value = error
            }
        }
    }

    // Logout user
    fun logout() {
        authManager.logout()
    }
}