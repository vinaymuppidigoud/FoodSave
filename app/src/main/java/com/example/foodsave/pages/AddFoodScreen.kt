package com.example.foodsave.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodsave.data.FoodItem
import com.example.foodsave.viewmodel.FoodViewModel

// theme colors
private val DarkGreen = Color(0xFF075B2A)
private val Green = Color(0xFF35A936)
private val Cream = Color(0xFFF8FCF4)
private val TextGreen = Color(0xFF173B25)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onBack: () -> Unit,
    foodViewModel: FoodViewModel = viewModel(),
    existingFood: FoodItem? = null
) {

    // Initialize food name
    var foodName by remember(existingFood) {
        mutableStateOf(existingFood?.name ?: "")
    }

    // Initialize quantity
    var quantity by remember(existingFood) {
        mutableStateOf(existingFood?.quantity?.toString() ?: "")
    }

    // Initialize expiry date
    var expiryDate by remember(existingFood) {
        mutableStateOf(existingFood?.expiryDate ?: "")
    }

    // Track local errors
    var localError by remember { mutableStateOf<String?>(null) }

    // Observe database errors
    val databaseError by foodViewModel.errorMessage.collectAsState()

    Scaffold(
        containerColor = Cream,
        topBar = {
            // top bar
            TopAppBar(
                title = {
                    // Show screen title
                    Text(
                        if (existingFood == null) "Add Food" else "Edit Food",
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    // Handle back navigation
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        }
    ) { padding ->

        // Arrange input fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show form heading
            Text(
                text = "Enter your food details",
                color = TextGreen,
                style = MaterialTheme.typography.bodyLarge
            )

            // Enter food name
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food name") },
                placeholder = { Text("e.g. Milk, Rice, Apples") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Enter food quantity
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                placeholder = { Text("Enter quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Enter expiry date
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = { Text("Expiry date") },
                placeholder = { Text("yyyy-MM-dd") },
                supportingText = {
                    Text("Example: 2026-12-25")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Display validation errors
            if (localError != null || databaseError != null) {
                Text(
                    text = localError ?: databaseError.orEmpty(),
                    color = Color(0xFFB3261E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit food details
            Button(
                onClick = {
                    // Convert quantity value
                    val amount = quantity.toIntOrNull()

                    // Validate input fields
                    when {
                        foodName.isBlank() -> {
                            localError = "Please enter a food name."
                        }

                        amount == null || amount <= 0 -> {
                            localError = "Enter a valid quantity."
                        }

                        !isValidExpiryDate(expiryDate) -> {
                            localError = "Enter a valid date (yyyy-MM-dd)."
                        }

                        else -> {
                            localError = null
                            foodViewModel.clearError()

                            // Save or update food
                            if (existingFood == null) {
                                foodViewModel.addFood(
                                    foodName,
                                    amount,
                                    expiryDate
                                )
                            } else {
                                foodViewModel.updateFood(
                                    existingFood.copy(
                                        name = foodName.trim(),
                                        quantity = amount,
                                        expiryDate = expiryDate
                                    )
                                )
                            }

                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = Color.White
                )
            ) {
                // Show button label
                Text(
                    if (existingFood == null) "Save Food" else "Update Food",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Validate expiry date
private fun isValidExpiryDate(date: String): Boolean {
    return try {
        // Create date formatter
        val format = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.US
        )
        format.isLenient = false
        format.parse(date) != null
    } catch (e: Exception) {
        // Handle invalid dates
        false
    }
}