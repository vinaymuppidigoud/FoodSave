package com.example.foodsave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.foodsave.navigation.AppNavigation
import com.example.foodsave.ui.theme.FoodSaveTheme

class MainActivity : ComponentActivity() {

    // Create screen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start app
        setContent {
            FoodSaveTheme {
                // App navigation
                AppNavigation()
            }
        }
    }
}