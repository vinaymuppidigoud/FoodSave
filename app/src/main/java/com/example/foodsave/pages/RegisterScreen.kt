package com.example.foodsave.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodsave.R
import com.example.foodsave.viewmodel.AuthViewModel

// colour palette
private val FoodSaveDarkGreen = Color(0xFF075B2A)
private val FoodSaveGreen = Color(0xFF35A936)
private val FoodSaveCream = Color(0xFFF8FCF4)
private val FoodSaveText = Color(0xFF173B25)
private val FoodSaveError = Color(0xFFB3261E)

// Register screen
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {

    // Store registration details
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Get authentication state
    val errorMessage by authViewModel.errorMessage
    val isLoading by authViewModel.isLoading

    // registration screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodSaveCream)
            .padding(horizontal = 28.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        // FoodSave logo
        Image(
            painter = painterResource(
                id = R.drawable.logo
            ),
            contentDescription = "FoodSave Logo",
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Create account heading
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = FoodSaveDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Registration message
        Text(
            text = "Join FoodSave and start reducing food waste",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp
            ),
            color = FoodSaveText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Email field
        OutlinedTextField(
            value = email,

            onValueChange = {
                email = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Email address")
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = FoodSaveGreen
                )
            },

            singleLine = true,

            shape = RoundedCornerShape(14.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FoodSaveGreen,
                unfocusedBorderColor = Color(0xFFB8CDBB),
                focusedLabelColor = FoodSaveDarkGreen,
                unfocusedLabelColor = FoodSaveText,
                cursorColor = FoodSaveGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,

            onValueChange = {
                password = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Password")
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = FoodSaveGreen
                )
            },

            visualTransformation = PasswordVisualTransformation(),

            singleLine = true,

            shape = RoundedCornerShape(14.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FoodSaveGreen,
                unfocusedBorderColor = Color(0xFFB8CDBB),
                focusedLabelColor = FoodSaveDarkGreen,
                unfocusedLabelColor = FoodSaveText,
                cursorColor = FoodSaveGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,

            onValueChange = {
                confirmPassword = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Confirm Password")
            },

            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password",
                    tint = FoodSaveGreen
                )
            },

            visualTransformation = PasswordVisualTransformation(),

            singleLine = true,

            shape = RoundedCornerShape(14.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FoodSaveGreen,
                unfocusedBorderColor = Color(0xFFB8CDBB),
                focusedLabelColor = FoodSaveDarkGreen,
                unfocusedLabelColor = FoodSaveText,
                cursorColor = FoodSaveGreen
            )
        )

        // Firebase error message
        if (errorMessage != null) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = errorMessage ?: "",
                color = FoodSaveError,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Register button
        Button(
            onClick = {
                if (email.isBlank()) {
                    return@Button
                }

                if (password.length < 6) {
                    return@Button
                }

                if (password != confirmPassword) {
                    return@Button
                }

                authViewModel.register(
                    email = email,
                    password = password,
                    onSuccess = onRegisterSuccess
                )
            },

            enabled = !isLoading,

            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),

            shape = RoundedCornerShape(14.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = FoodSaveDarkGreen,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF9DB5A3)
            )
        ) {

            // Show loading state
            if (isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(21.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Creating Account..."
                )

            } else {

                Text(
                    text = "Register",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Login option
        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? Login",
                color = FoodSaveGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}