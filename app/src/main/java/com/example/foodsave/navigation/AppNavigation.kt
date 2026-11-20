package com.example.foodsave.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodsave.data.FoodItem
import com.example.foodsave.firebase.AuthManager
import com.example.foodsave.pages.AddFoodScreen
import com.example.foodsave.pages.DashboardScreen
import com.example.foodsave.pages.IntroScreen
import com.example.foodsave.pages.LoginScreen
import com.example.foodsave.pages.PantryScreen
import com.example.foodsave.pages.RegisterScreen
import com.example.foodsave.viewmodel.FoodViewModel

// App screen routes
sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Pantry : Screen("pantry")
    object AddFood : Screen("add_food")
    object EditFood : Screen("edit_food")
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // Create auth manager
    val authManager = remember {
        AuthManager()
    }

    // Check login status
    val startDestination = remember {
        if (authManager.isUserLoggedIn()) {
            Screen.Dashboard.route
        } else {
            Screen.Intro.route
        }
    }

    // Shared ViewModel
    val foodViewModel: FoodViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Intro screen
        composable(Screen.Intro.route) {
            IntroScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Login screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    foodViewModel.refreshFood()

                    navController.navigate(Screen.Dashboard.route) {
                        // Clear auth screens
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },

                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Register screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    foodViewModel.refreshFood()

                    navController.navigate(Screen.Dashboard.route) {
                        // Clear auth screens
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },

                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Dashboard screen
        composable(Screen.Dashboard.route) {

            // Refresh food data
            androidx.compose.runtime.LaunchedEffect(Unit) {
                foodViewModel.refreshFood()
            }

            DashboardScreen(
                foodViewModel = foodViewModel,

                onOpenPantry = {
                    navController.navigate(Screen.Pantry.route)
                },

                onAddFood = {
                    navController.navigate(Screen.AddFood.route)
                },

                onLogout = {
                    // Firebase logout
                    authManager.logout()

                    // Return to intro
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Pantry screen
        composable(Screen.Pantry.route) {
            PantryScreen(
                onAddFood = {
                    navController.navigate(Screen.AddFood.route)
                },

                onEditFood = { food: FoodItem ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedFood", food)

                    navController.navigate(Screen.EditFood.route)
                },

                foodViewModel = foodViewModel
            )
        }

        // Add food screen
        composable(Screen.AddFood.route) {
            AddFoodScreen(
                onBack = {
                    navController.popBackStack()
                },

                foodViewModel = foodViewModel
            )
        }

        // Edit food screen
        composable(Screen.EditFood.route) {
            val selectedFood: FoodItem? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FoodItem>("selectedFood")

            if (selectedFood != null) {

                AddFoodScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    foodViewModel = foodViewModel,

                    existingFood = selectedFood
                )
            }
        }
    }
}