package com.example.foodsave.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.foodsave.data.FoodItem
import com.example.foodsave.firebase.AuthManager
import com.example.foodsave.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// colors
private val FoodSaveDarkGreen = Color(0xFF075B2A)
private val FoodSaveGreen = Color(0xFF35A936)
private val FoodSaveCream = Color(0xFFF8FCF4)
private val FoodSaveText = Color(0xFF173B25)
private val FoodSaveRed = Color(0xFFD32F2F)
private val FoodSaveOrange = Color(0xFFFF9800)


// Main Dashboard Screen
@Composable
fun DashboardScreen(
    foodViewModel: FoodViewModel,
    onOpenPantry: () -> Unit,
    onAddFood: () -> Unit,
    onLogout: () -> Unit
) {

    // Get context
    val context = LocalContext.current

    // Observe food items from ViewModel
    val foodItems by foodViewModel.foodItems.collectAsState()

    // Get current user email
    val authManager = remember { AuthManager() }
    val email = authManager.getCurrentUserEmail()

    // Notification permission launcher
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            // Permission result
            if (!granted) {
                // Notification permission was denied
            }
        }

    // Track notification permission request
    var notificationPermissionRequested by remember {
        mutableStateOf(false)
    }

    // Check and request notification permission
    LaunchedEffect(Unit) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted && !notificationPermissionRequested) {

                notificationPermissionRequested = true

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    // Filter expired food items
    val expiredItems = foodItems.filter {
        getDaysUntilExpiry(it.expiryDate) < 0L
    }

    // Filter food items expiring within 3 days
    val expiringSoonItems = foodItems.filter {
        getDaysUntilExpiry(it.expiryDate) in 0L..3L
    }

    // Dashboard layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FoodSaveCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome section
        Text(
            text = "Welcome to FoodSave",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = FoodSaveDarkGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display current user email
        Text(
            text = email ?: "User",
            color = FoodSaveText,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Food inventory summary card
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

                Spacer(modifier = Modifier.height(10.dp))

                // Display total food count
                Text(
                    text = "${foodItems.size} food items",
                    color = FoodSaveGreen,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Display inventory information
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

                // Navigate to Pantry screen
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

                // Navigate to Add Food screen
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

                Spacer(modifier = Modifier.height(12.dp))

                // Display expired food count
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

                // Display upcoming expiry count
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

                // Display expiry alert items
                if (expiredItems.isEmpty() && expiringSoonItems.isEmpty()) {

                    Text(
                        text = "No expiry alerts available.",
                        color = FoodSaveText,
                        fontSize = 14.sp
                    )

                } else {

                    Column {

                        (expiredItems + expiringSoonItems).forEach { food ->

                            ExpiryAlertItem(food)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout button
        Button(
            onClick = onLogout,
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}


// Display individual food expiry alert
@Composable
private fun ExpiryAlertItem(food: FoodItem) {

    // Calculate remaining days until expiry
    val daysLeft = getDaysUntilExpiry(food.expiryDate)

    // Determine expiry status text
    val status = when {

        daysLeft < 0L -> "Expired"

        daysLeft == 0L -> "Expires today"

        daysLeft == 1L -> "1 day left"

        else -> "$daysLeft days left"
    }

    // Determine expiry status color
    val statusColor = when {

        daysLeft < 0L -> FoodSaveRed

        else -> FoodSaveOrange
    }

    // Expiry alert item layout
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            // Display food name
            Text(
                text = food.name,
                color = FoodSaveText,
                fontWeight = FontWeight.Medium
            )

            // Display food quantity
            Text(
                text = "Quantity: ${food.quantity}",
                color = FoodSaveText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Display expiry status
        Text(
            text = status,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// Calculate remaining days until food expiry
private fun getDaysUntilExpiry(expiryDate: String): Long {

    return try {

        // Parse expiry date using yyyy-MM-dd format
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).apply {
            isLenient = false
        }

        val expiry = formatter.parse(expiryDate)
            ?: return Long.MAX_VALUE

        // Get today's date without time
        val today = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Get expiry date without time
        val expiryCalendar = Calendar.getInstance().apply {

            time = expiry

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Extract today's year and day of year
        val todayDate = today.get(Calendar.YEAR) to
                today.get(Calendar.DAY_OF_YEAR)

        // Extract expiry year and day of year
        val expiryDateParts = expiryCalendar.get(Calendar.YEAR) to
                expiryCalendar.get(Calendar.DAY_OF_YEAR)

        // Calculate today's starting timestamp
        val todayStart = Calendar.getInstance().apply {

            set(
                todayDate.first,
                Calendar.JANUARY,
                1,
                0,
                0,
                0
            )

            set(Calendar.MILLISECOND, 0)

            add(
                Calendar.DAY_OF_YEAR,
                todayDate.second - 1
            )
        }

        // Calculate expiry date starting timestamp
        val expiryStart = Calendar.getInstance().apply {

            set(
                expiryDateParts.first,
                Calendar.JANUARY,
                1,
                0,
                0,
                0
            )

            set(Calendar.MILLISECOND, 0)

            add(
                Calendar.DAY_OF_YEAR,
                expiryDateParts.second - 1
            )
        }

        // Calculate date difference in milliseconds
        val difference =
            expiryStart.timeInMillis - todayStart.timeInMillis

        // Convert milliseconds into days
        Math.floorDiv(
            difference,
            24L * 60L * 60L * 1000L
        )

    } catch (e: Exception) {

        // Return maximum value if date is invalid
        Long.MAX_VALUE
    }
}


// Android Studio Dashboard Preview
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "FoodSave Dashboard Preview"
)
@Composable
fun DashboardScreenPreview() {

    MaterialTheme {

        // Preview-only Dashboard UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FoodSaveCream)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome section
            Text(
                text = "Welcome to FoodSave",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = FoodSaveDarkGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "user@example.com",
                color = FoodSaveText,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inventory preview card
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5 food items",
                        color = FoodSaveGreen,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Keep track of your stored food.",
                        color = FoodSaveText,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FoodSaveDarkGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("View Pantry")
                    }

                    Button(
                        onClick = {},
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

            // Expiry alerts preview card
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                            text = "1",
                            color = FoodSaveRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                            text = "2",
                            color = FoodSaveOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sample expired food
                    PreviewExpiryAlertItem(
                        name = "Milk",
                        quantity = "1 litre",
                        status = "Expired",
                        statusColor = FoodSaveRed
                    )

                    // Sample upcoming food
                    PreviewExpiryAlertItem(
                        name = "Bread",
                        quantity = "2 packets",
                        status = "1 day left",
                        statusColor = FoodSaveOrange
                    )

                    PreviewExpiryAlertItem(
                        name = "Apples",
                        quantity = "5 pieces",
                        status = "3 days left",
                        statusColor = FoodSaveOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout preview button
            Button(
                onClick = {},
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// Preview-only expiry alert item
@Composable
private fun PreviewExpiryAlertItem(
    name: String,
    quantity: String,
    status: String,
    statusColor: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = name,
                color = FoodSaveText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Quantity: $quantity",
                color = FoodSaveText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = status,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}