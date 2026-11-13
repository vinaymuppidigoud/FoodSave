package com.example.foodsave.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodsave.firebase.AuthManager

// colours
private val FoodSaveDarkGreen = Color(0xFF075B2A)
private val FoodSaveGreen = Color(0xFF35A936)
private val FoodSaveCream = Color(0xFFF8FCF4)
private val FoodSaveText = Color(0xFF173B25)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {

    // Get the current user's details
    val authManager = AuthManager()
    val email = authManager.getCurrentUserEmail()

    // Main dashboard layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodSaveCream)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        // Welcome message
        Text(
            text = "Welcome to FoodSave",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = FoodSaveDarkGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show the user's email
        Text(
            text = email ?: "User",
            color = FoodSaveText,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Food inventory card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Food Inventory",
                    color = FoodSaveDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No food items added yet.",
                    color = FoodSaveText,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expiry alerts card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Expiry Alerts",
                    color = FoodSaveDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No expiry alerts available.",
                    color = FoodSaveText,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Logout button
        Button(
            onClick = {
                authManager.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FoodSaveDarkGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}