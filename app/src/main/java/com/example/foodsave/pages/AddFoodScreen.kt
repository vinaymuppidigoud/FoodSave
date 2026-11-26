package com.example.foodsave.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import com.example.foodsave.data.FoodItem
import com.example.foodsave.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Locale


// App colours
private val DarkGreen = Color(0xFF075B2A)
private val Green = Color(0xFF35A936)
private val Cream = Color(0xFFF8FCF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    foodViewModel: FoodViewModel,
    existingFood: FoodItem? = null,
    capturedImagePath: String? = null
) {

    // Get current context
    val context = LocalContext.current

    // Form values
    var foodName by remember(existingFood) {
        mutableStateOf(existingFood?.name ?: "")
    }

    var quantity by remember(existingFood) {
        mutableStateOf(
            existingFood?.quantity?.toString() ?: ""
        )
    }

    var expiryDate by remember(existingFood) {
        mutableStateOf(existingFood?.expiryDate ?: "")
    }

    var imagePath by remember(existingFood) {
        mutableStateOf(existingFood?.imagePath)
    }

    // Local error message
    var localError by remember {
        mutableStateOf<String?>(null)
    }

    // Observe database error from ViewModel
    val databaseError by
    foodViewModel.errorMessage.collectAsState()

    // Receive captured image path from Camera screen
    LaunchedEffect(capturedImagePath) {

        if (!capturedImagePath.isNullOrBlank()) {

            imagePath = capturedImagePath
        }
    }

    // Main screen layout
    Scaffold(

        containerColor = Cream,

        // Screen toolbar
        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = if (existingFood == null) {
                            "Add Food"
                        } else {
                            "Edit Food"
                        },
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                },

                // Back button
                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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

        // Form content
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Form heading
            Text(
                text = "Enter your food details",
                color = DarkGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Display captured food image
            if (!imagePath.isNullOrBlank()) {

                val bitmap = remember(imagePath) {

                    BitmapFactory.decodeFile(imagePath)
                }

                if (bitmap != null) {

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Food image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Open camera button
            OutlinedButton(
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text("Capture Food Image")
            }

            // Food name input
            OutlinedTextField(

                value = foodName,

                onValueChange = {
                    foodName = it
                },

                label = {
                    Text("Food name")
                },

                placeholder = {
                    Text("e.g. Milk, Rice, Apples")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                shape = RoundedCornerShape(12.dp)
            )

            // Food quantity input
            OutlinedTextField(

                value = quantity,

                onValueChange = {
                    quantity = it
                },

                label = {
                    Text("Quantity")
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                shape = RoundedCornerShape(12.dp)
            )

            // Food expiry date input
            OutlinedTextField(

                value = expiryDate,

                onValueChange = {
                    expiryDate = it
                },

                label = {
                    Text("Expiry date")
                },

                placeholder = {
                    Text("yyyy-MM-dd")
                },

                supportingText = {
                    Text("Example: 2026-12-25")
                },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                shape = RoundedCornerShape(12.dp)
            )

            // Display validation or database errors
            if (localError != null || databaseError != null) {

                Text(
                    text = localError ?: databaseError.orEmpty(),
                    color = Color.Red
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            // Save or update food button
            Button(

                onClick = {

                    val amount = quantity.toIntOrNull()

                    // Validate form values
                    when {

                        foodName.isBlank() -> {

                            localError =
                                "Please enter a food name."
                        }

                        amount == null || amount <= 0 -> {

                            localError =
                                "Enter a valid quantity."
                        }

                        !isValidExpiryDate(expiryDate) -> {

                            localError =
                                "Enter a valid date (yyyy-MM-dd)."
                        }

                        else -> {

                            localError = null

                            foodViewModel.clearError()

                            // Add new food item
                            if (existingFood == null) {

                                foodViewModel.addFood(

                                    name = foodName,

                                    quantity = amount,

                                    expiryDate = expiryDate,

                                    imagePath = imagePath
                                )

                            } else {

                                // Update existing food item
                                foodViewModel.updateFood(

                                    existingFood.copy(

                                        name = foodName.trim(),

                                        quantity = amount,

                                        expiryDate = expiryDate,

                                        imagePath = imagePath,

                                        isSynced = false
                                    )
                                )
                            }

                            // Return to previous screen
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

                Text(

                    text = if (existingFood == null) {
                        "Save Food"
                    } else {
                        "Update Food"
                    },

                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// Validate expiry date format
private fun isValidExpiryDate(date: String): Boolean {

    return try {

        // Parse date using yyyy-MM-dd format
        val format = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        // Disable automatic invalid date correction
        format.isLenient = false

        val parsed = format.parse(date)

        // Confirm parsed date matches entered date
        parsed != null && format.format(parsed) == date

    } catch (e: Exception) {

        // Return false for invalid dates
        false
    }
}


// Android Studio Add Food Screen Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Add Food Screen Preview"
)
@Composable
fun AddFoodScreenPreview() {

    MaterialTheme {

        // Preview-only form UI
        Scaffold(

            containerColor = Cream,

            topBar = {

                TopAppBar(

                    title = {

                        Text(
                            text = "Add Food",
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    },

                    navigationIcon = {

                        IconButton(onClick = {}) {

                            Icon(
                                imageVector = Icons.Default.ArrowBack,
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

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),

                verticalArrangement = Arrangement.spacedBy(14.dp)

            ) {

                // Form heading
                Text(
                    text = "Enter your food details",
                    color = DarkGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Camera button preview
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text("Capture Food Image")
                }

                // Sample food name
                OutlinedTextField(
                    value = "Milk",
                    onValueChange = {},
                    label = {
                        Text("Food name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Sample quantity
                OutlinedTextField(
                    value = "2",
                    onValueChange = {},
                    label = {
                        Text("Quantity")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Sample expiry date
                OutlinedTextField(
                    value = "2026-12-25",
                    onValueChange = {},
                    label = {
                        Text("Expiry date")
                    },
                    supportingText = {
                        Text("Example: 2026-12-25")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                // Preview save button
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = Color.White
                    )
                ) {

                    Text(
                        text = "Save Food",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}