package com.example.foodsave.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodsave.data.FoodItem
import com.example.foodsave.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// theme colors
private val DarkGreen = Color(0xFF075B2A)
private val Cream = Color(0xFFF8FCF4)
private val TextGreen = Color(0xFF173B25)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    onAddFood: () -> Unit,
    onEditFood: (FoodItem) -> Unit,
    foodViewModel: FoodViewModel = viewModel()
) {

    // Observe food items
    val foodItems by foodViewModel.foodItems.collectAsState()

    // Track search text
    var searchText by remember { mutableStateOf("") }

    // Track selected filter
    var selectedFilter by remember { mutableStateOf("All") }

    // Filter pantry items
    val filteredItems = foodItems.filter { food ->
        val matchesSearch = food.name.contains(
            searchText,
            ignoreCase = true
        )

        val status = getExpiryStatus(food.expiryDate)

        val matchesFilter = when (selectedFilter) {
            "Fresh" -> status == "Fresh"
            "Expiring Soon" -> status == "Expiring Soon"
            "Expired" -> status == "Expired"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = Cream,
        floatingActionButton = {
            // Add food button
            FloatingActionButton(
                onClick = onAddFood,
                containerColor = DarkGreen,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Food"
                )
            }
        },
        topBar = {
            // top bar
            TopAppBar(
                title = {
                    Text(
                        "My Pantry",
                        color = DarkGreen
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        }
    ) { padding ->

        // Arrange pantry content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search food items
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search food") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show filter heading
            Text(
                "Filter by expiry status",
                color = TextGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scroll filter options
            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "All",
                    "Fresh",
                    "Expiring Soon",
                    "Expired"
                ).forEach { filter ->

                    // Select expiry filter
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show item count
            Text(
                "Food items: ${filteredItems.size}",
                color = DarkGreen,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display pantry items
            if (filteredItems.isEmpty()) {

                // Show empty message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No food items found.",
                        color = TextGreen
                    )
                }

            } else {

                // List food items
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id }
                    ) { food ->

                        // Display food card
                        FoodItemCard(
                            food = food,
                            onEdit = { onEditFood(food) },
                            onDelete = {
                                foodViewModel.deleteFood(food)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodItemCard(
    food: FoodItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    // Get expiry status
    val status = getExpiryStatus(food.expiryDate)

    // Set status color
    val statusColor = when (status) {
        "Fresh" -> Color(0xFF2E7D32)
        "Expiring Soon" -> Color(0xFFEF8C00)
        else -> Color(0xFFB3261E)
    }

    // Create food card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        // Arrange food details
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Display food information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        food.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGreen
                    )

                    // Show food quantity
                    Text(
                        "Quantity: ${food.quantity}",
                        color = TextGreen
                    )

                    // Show expiry date
                    Text(
                        "Expiry: ${food.expiryDate}",
                        color = TextGreen
                    )
                }

                // Show expiry status
                Text(
                    status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arrange action buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Edit food item
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = DarkGreen
                    )
                }

                // Delete food item
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFB3261E)
                    )
                }
            }
        }
    }
}

// Check expiry status
fun getExpiryStatus(expiryDate: String): String {
    return try {
        // Create date formatter
        val format = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )
        format.isLenient = false
        val expiry = format.parse(expiryDate) ?: return "Unknown"

        // Get current date
        val today = java.util.Calendar.getInstance().apply {
            set(
                java.util.Calendar.HOUR_OF_DAY,
                0
            )
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        // Prepare expiry date
        val expiryCalendar = java.util.Calendar.getInstance().apply {
            time = expiry
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        // Calculate day difference
        val days = (
                expiryCalendar.timeInMillis - today.timeInMillis
                ) / (24 * 60 * 60 * 1000)

        // Determine food status
        when {
            days < 0 -> "Expired"
            days <= 3 -> "Expiring Soon"
            else -> "Fresh"
        }

    } catch (e: Exception) {
        "Unknown"
    }
}