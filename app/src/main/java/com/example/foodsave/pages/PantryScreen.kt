package com.example.foodsave.pages

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodsave.data.FoodItem
import com.example.foodsave.viewmodel.FoodViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// App colors
private val DarkGreen = Color(0xFF075B2A)
private val Cream = Color(0xFFF8FCF4)
private val TextGreen = Color(0xFF173B25)


// Main Pantry Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    onAddFood: () -> Unit,
    onEditFood: (FoodItem) -> Unit,
    foodViewModel: FoodViewModel = viewModel()
) {

    // Observe food items from ViewModel
    val foodItems by foodViewModel.foodItems.collectAsState()

    // Track search text
    var searchText by remember {
        mutableStateOf("")
    }

    // Track selected filter
    var selectedFilter by remember {
        mutableStateOf("All")
    }

    // Track selected food for delete confirmation
    var foodToDelete by remember {
        mutableStateOf<FoodItem?>(null)
    }

    // Filter pantry items based on search and expiry status
    val filteredItems = foodItems.filter { food ->

        // Check whether food name matches search
        val matchesSearch = food.name.contains(
            searchText,
            ignoreCase = true
        )

        // Get food expiry status
        val status = getExpiryStatus(food.expiryDate)

        // Check selected filter
        val matchesFilter = when (selectedFilter) {

            "Fresh" -> status == "Fresh"

            "Expiring Soon" -> status == "Expiring Soon"

            "Expired" -> status == "Expired"

            else -> true
        }

        matchesSearch && matchesFilter
    }

    // Pantry screen layout
    Scaffold(

        containerColor = Cream,

        // Add food floating action button
        floatingActionButton = {

            FloatingActionButton(
                onClick = onAddFood,
                containerColor = DarkGreen,
                contentColor = Color.White
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Food"
                )
            }
        },

        // Top app bar
        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "My Pantry",
                        color = DarkGreen
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        }

    ) { padding ->

        // Pantry content
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)

        ) {

            // Search food items
            OutlinedTextField(

                value = searchText,

                onValueChange = {
                    searchText = it
                },

                label = {
                    Text("Search food")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                shape = RoundedCornerShape(12.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // Filter heading
            Text(
                text = "Filter by expiry status",
                color = TextGreen
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Expiry filter options
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

                    FilterChip(

                        selected = selectedFilter == filter,

                        onClick = {
                            selectedFilter = filter
                        },

                        label = {
                            Text(filter)
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // Display filtered food count
            Text(
                text = "Food items: ${filteredItems.size}",
                color = DarkGreen,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Display food items or empty message
            if (filteredItems.isEmpty()) {

                Box(

                    modifier = Modifier.fillMaxSize(),

                    contentAlignment = Alignment.Center

                ) {

                    Text(
                        text = "No food items found.",
                        color = TextGreen
                    )
                }

            } else {

                // Display food items in a scrollable list
                LazyColumn(

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    items(

                        items = filteredItems,

                        key = { it.id }

                    ) { food ->

                        // Individual food card
                        FoodItemCard(

                            food = food,

                            onEdit = {
                                onEditFood(food)
                            },

                            onDelete = {
                                foodToDelete = food
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    foodToDelete?.let { food ->
        AlertDialog(
            onDismissRequest = {
                foodToDelete = null
            },

            title = {
                Text("Delete Food")
            },

            text = {
                Text(
                    "Are you sure you want to delete ${food.name}?"
                )
            },

            // Confirm delete
            confirmButton = {
                TextButton(
                    onClick = {
                        foodViewModel.deleteFood(food)
                        foodToDelete = null
                    }

                ) {

                    Text(
                        text = "Delete",
                        color = Color(0xFFB3261E)
                    )
                }
            },

            // Cancel delete
            dismissButton = {
                TextButton(
                    onClick = {
                        foodToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Individual Food Item Card
@Composable
fun FoodItemCard(
    food: FoodItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    // Get food expiry status
    val status = getExpiryStatus(food.expiryDate)

    // Set expiry status color
    val statusColor = when (status) {
        "Fresh" -> Color(0xFF2E7D32)
        "Expiring Soon" -> Color(0xFFEF8C00)
        "Expired" -> Color(0xFFB3261E)
        else -> Color.Gray
    }

    // Select local image first, then cloud image
    val imageModel = remember(
        food.imagePath,
        food.imageUrl
    ) {
        val localFile = food.imagePath
            ?.let { File(it) }
            ?.takeIf { it.exists() }
        localFile ?: food.imageUrl
    }

    // Food card layout
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Display food image when available
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Image of ${food.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Food details and expiry status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    // Display food name
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Display food quantity
                    Text(
                        text = "Quantity: ${food.quantity}",
                        color = TextGreen
                    )

                    // Display expiry date
                    Text(
                        text = "Expiry: ${food.expiryDate}",
                        color = TextGreen
                    )
                }

                // Expiry status label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Food action buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()

            ) {

                // Edit food button
                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Food",
                        tint = DarkGreen
                    )
                }

                // Delete food button
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Food",
                        tint = Color(0xFFB3261E)
                    )
                }
            }
        }
    }
}


// Check food expiry status
fun getExpiryStatus(expiryDate: String): String {

    return try {
        // Parse expiry date
        val format = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        format.isLenient = false

        val expiry = format.parse(expiryDate)
            ?: return "Unknown"

        // Get today's date at midnight
        val today = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Prepare expiry date at midnight
        val expiryCalendar = Calendar.getInstance().apply {
            time = expiry
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Calculate remaining days
        val days = (
                expiryCalendar.timeInMillis -
                        today.timeInMillis
                ) / (24 * 60 * 60 * 1000)

        // Determine expiry status
        when {
            days < 0 -> "Expired"
            days <= 3 -> "Expiring Soon"
            else -> "Fresh"
        }

    } catch (e: Exception) {
        "Unknown"
    }
}


// Android Studio Pantry Screen Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "FoodSave Pantry Preview"
)
@Composable
fun PantryScreenPreview() {

    MaterialTheme {

        // Preview-only Pantry UI
        Scaffold(

            containerColor = Cream,

            // Preview top bar
            topBar = {

                TopAppBar(

                    title = {

                        Text(
                            text = "My Pantry",
                            color = DarkGreen
                        )
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Cream
                    )
                )
            },

            // Preview add button
            floatingActionButton = {

                FloatingActionButton(

                    onClick = {},

                    containerColor = DarkGreen,

                    contentColor = Color.White

                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Food"
                    )
                }
            }

        ) { padding ->

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)

            ) {

                // Preview search field
                OutlinedTextField(

                    value = "",

                    onValueChange = {},

                    label = {
                        Text("Search food")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // Preview filter heading
                Text(
                    text = "Filter by expiry status",
                    color = TextGreen
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Preview filter chips
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

                        FilterChip(

                            selected = filter == "All",

                            onClick = {},

                            label = {
                                Text(filter)
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // Preview food count
                Text(
                    text = "Food items: 3",
                    color = DarkGreen,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Preview sample food cards
                LazyColumn(

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    item {

                        PantryPreviewFoodCard(
                            name = "Rice",
                            quantity = "5 kg",
                            expiryDate = "2027-01-15",
                            status = "Fresh",
                            statusColor = Color(0xFF2E7D32)
                        )
                    }

                    item {

                        PantryPreviewFoodCard(
                            name = "Milk",
                            quantity = "2 litres",
                            expiryDate = "2026-11-27",
                            status = "Expiring Soon",
                            statusColor = Color(0xFFEF8C00)
                        )
                    }

                    item {

                        PantryPreviewFoodCard(
                            name = "Bread",
                            quantity = "1 packet",
                            expiryDate = "2026-11-20",
                            status = "Expired",
                            statusColor = Color(0xFFB3261E)
                        )
                    }
                }
            }
        }
    }
}


// Preview-only food card
@Composable
private fun PantryPreviewFoodCard(
    name: String,
    quantity: String,
    expiryDate: String,
    status: String,
    statusColor: Color
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Food details
            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.Top

            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGreen
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Quantity: $quantity",
                        color = TextGreen
                    )

                    Text(
                        text = "Expiry: $expiryDate",
                        color = TextGreen
                    )
                }

                // Sample expiry status
                Surface(

                    shape = RoundedCornerShape(8.dp),

                    color = statusColor.copy(alpha = 0.12f)

                ) {

                    Text(

                        text = status,

                        color = statusColor,

                        style = MaterialTheme.typography.labelMedium,

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Preview action buttons
            Row(

                horizontalArrangement = Arrangement.End,

                modifier = Modifier.fillMaxWidth()

            ) {

                IconButton(onClick = {}) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Food",
                        tint = DarkGreen
                    )
                }

                IconButton(onClick = {}) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Food",
                        tint = Color(0xFFB3261E)
                    )
                }
            }
        }
    }
}