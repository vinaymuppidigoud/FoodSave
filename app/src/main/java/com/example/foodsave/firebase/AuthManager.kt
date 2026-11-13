package com.example.foodsave.firebase

import com.google.firebase.auth.FirebaseAuth

class AuthManager {

    // Firebase auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register user
    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(
                        false,
                        task.exception?.message ?: "Registration failed"
                    )
                }
            }
    }

    // Login user
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(
                        false,
                        task.exception?.message ?: "Login failed"
                    )
                }
            }
    }

    // Sign out
    fun logout() {
        auth.signOut()
    }

    // Check login
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get user email
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}