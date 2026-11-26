package com.example.foodsave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.foodsave.navigation.AppNavigation
import com.example.foodsave.notifications.NotificationHelper
import com.example.foodsave.ui.theme.FoodSaveTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create channel
        NotificationHelper.createNotificationChannel(this)

        // Set app content
        setContent {
            FoodSaveTheme {
                // Start navigation
                AppNavigation()
            }
        }
    }
}