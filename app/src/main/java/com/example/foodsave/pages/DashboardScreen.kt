
package com.example.foodsave.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodsave.data.FoodItem
import com.example.foodsave.firebase.AuthManager
import com.example.foodsave.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// theme colors
private val FoodSaveDarkGreen = Color(0xFF075B2A)
private val FoodSaveGreen = Color(0xFF35A936)
private val FoodSaveCream = Color(0xFFF8FCF4)
private val FoodSaveText = Color(0xFF173B25)
private val FoodSaveRed = Color(0xFFD32F2F)
private val FoodSaveOrange = Color(0xFFFF9800)

@Composable
fun DashboardScreen(
    foodViewModel: FoodViewModel,
    onOpenPantry: () -> Unit,
    onAddFood: () -> Unit,
    onLogout: () -> Unit
) {
    // Observe food items
    val foodItems by foodViewModel.foodItems.collectAsState()

    // Get user email
    val authManager = AuthManager()
    val email = authManager.getCurrentUserEmail()

    // Filter expired items
    val expiredItems = foodItems.filter {
        getDaysUntilExpiry(it.expiryDate) < 0
    }

    // Filter upcoming expiry
    val expiringSoonItems = foodItems.filter {
        getDaysUntilExpiry(it.expiryDate) in 0..3
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodSaveCream)
            .padding(20.dp)
    ) {
        // Add top spacing
        Spacer(modifier = Modifier.height(16.dp))

        // Display welcome message
        Text(
            text = "Welcome to FoodSave",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = FoodSaveDarkGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show user email
        Text(
            text = email ?: "User",
            color = FoodSaveText,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Inventory summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            // Arrange inventory details
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Show inventory title
                Text(
                    text = "Food Inventory",
                    color = FoodSaveDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Display item count
                Text(
                    text = "${foodItems.size} food items",
                    color = FoodSaveGreen,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Show inventory message
                Text(
                    text = if (foodItems.isEmpty()) {
                        "No food items added yet."
                    } else {
                        "Keep track of your stored food."
                    },
                    color = FoodSaveText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Open pantry screen
                Button(
                    onClick = onOpenPantry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FoodSaveDarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("View Pantry")
                }

                // Open add screen
                Button(
                    onClick = onAddFood,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FoodSaveGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Food")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expiry alerts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            // Arrange alert details
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Show alert title
                Text(
                    text = "Expiry Alerts",
                    color = FoodSaveDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Show expired count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Expired",
                        color = FoodSaveRed,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = expiredItems.size.toString(),
                        color = FoodSaveRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show upcoming count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Expiring within 3 days",
                        color = FoodSaveOrange,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = expiringSoonItems.size.toString(),
                        color = FoodSaveOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Display expiry alerts
                if (expiredItems.isEmpty() &&
                    expiringSoonItems.isEmpty()
                ) {
                    Text(
                        text = "No expiry alerts available.",
                        color = FoodSaveText,
                        fontSize = 14.sp
                    )
                } else {
                    // List alert items
                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(
                            items = expiredItems + expiringSoonItems,
                            key = { it.id }
                        ) { food ->
                            ExpiryAlertItem(food)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout button
        Button(
            onClick = {
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
            // Show button text
            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpiryAlertItem(food: FoodItem) {
    // Calculate remaining days
    val daysLeft = getDaysUntilExpiry(food.expiryDate)

    // Set expiry status
    val status = if (daysLeft < 0) {
        "Expired"
    } else {
        "Expiring soon"
    }

    // Set status color
    val statusColor = if (daysLeft < 0) {
        FoodSaveRed
    } else {
        FoodSaveOrange
    }

    // Arrange item details
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Display food details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = food.name,
                color = FoodSaveText,
                fontWeight = FontWeight.Medium
            )

            // Show food quantity
            Text(
                text = "Quantity: ${food.quantity}",
                color = FoodSaveText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Show expiry status
        Text(
            text = status,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Calculate expiry days
private fun getDaysUntilExpiry(expiryDate: String): Long {
    return try {
        // Create date formatter
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        formatter.isLenient = false
        val expiry = formatter.parse(expiryDate) ?: return Long.MAX_VALUE

        // Get current date
        val today = formatter.parse(formatter.format(Date()))
            ?: return Long.MAX_VALUE

        // Calculate day difference
        TimeUnit.MILLISECONDS.toDays(
            expiry.time - today.time
        )
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}