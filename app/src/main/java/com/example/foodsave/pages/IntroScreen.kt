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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodsave.R

// colour palette
private val FoodSaveDarkGreen = Color(0xFF075B2A)
private val FoodSaveGreen = Color(0xFF35A936)
private val FoodSaveCream = Color(0xFFF8FCF4)
private val FoodSaveText = Color(0xFF173B25)

@Composable
fun IntroScreen(
    onGetStarted: () -> Unit
) {

    // screen layout
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
            modifier = Modifier.size(155.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main heading
        Text(
            text = "Welcome to FoodSave",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                letterSpacing = 0.sp
            ),
            color = FoodSaveDarkGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // app description
        Text(
            text = "Plan your food. Track expiry dates. Reduce food waste.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.1.sp
            ),
            color = FoodSaveText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Get started button
        Button(
            onClick = onGetStarted,

            modifier = Modifier
                .fillMaxWidth(0.56f)
                .height(48.dp),

            shape = RoundedCornerShape(12.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = FoodSaveDarkGreen,
                contentColor = Color.White
            ),

            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}