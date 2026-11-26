package com.example.foodsave.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.foodsave.data.FoodItem
import com.example.foodsave.firebase.AuthManager
import com.example.foodsave.pages.*
import com.example.foodsave.viewmodel.FoodViewModel

// App screens routes
sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Pantry : Screen("pantry")
    object AddFood : Screen("add_food")
    object EditFood : Screen("edit_food")
    object Camera : Screen("camera")
}

@Composable
fun AppNavigation() {
    // Create navigation controller
    val navController = rememberNavController()

    // Initialise authentication
    val authManager = remember {
        AuthManager()
    }

    // Choose starting screen
    val startDestination = remember {
        if (authManager.isUserLoggedIn()) {
            Screen.Dashboard.route
        } else {
            Screen.Intro.route
        }
    }

    // Get food view model
    val foodViewModel: FoodViewModel = viewModel()

    // Set navigation routes
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Show intro screen
        composable(Screen.Intro.route) {
            IntroScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Show login screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    foodViewModel.refreshFood()
                    navController.navigate(Screen.Dashboard.route) {
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

        // Show register screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    foodViewModel.refreshFood()
                    navController.navigate(Screen.Dashboard.route) {
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

        // Show dashboard screen
        composable(Screen.Dashboard.route) {
            // Refresh food records
            LaunchedEffect(Unit) {
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
                    authManager.logout()
                    navController.navigate(Screen.Intro.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Show pantry screen
        composable(Screen.Pantry.route) {
            PantryScreen(
                onAddFood = {
                    navController.navigate(Screen.AddFood.route)
                },
                onEditFood = { food: FoodItem ->
                    // Pass selected food
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedFood", food)
                    navController.navigate(Screen.EditFood.route)
                },
                foodViewModel = foodViewModel
            )
        }

        // Show add food screen
        composable(Screen.AddFood.route) {
            // Get captured image
            val imagePath by
            navController.currentBackStackEntry!!
                .savedStateHandle
                .getStateFlow("capturedImagePath", "")
                .collectAsState()
            AddFoodScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                foodViewModel = foodViewModel,
                capturedImagePath = imagePath.ifBlank {
                    null
                }
            )
        }

        // Show edit food screen
        composable(Screen.EditFood.route) {
            // Get selected food
            val selectedFood: FoodItem? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<FoodItem>("selectedFood")

            if (selectedFood != null) {
                AddFoodScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenCamera = {
                        navController.navigate(Screen.Camera.route)
                    },
                    foodViewModel = foodViewModel,
                    existingFood = selectedFood
                )
            }
        }

        // Show camera screen
        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { path ->
                    // Save captured image
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("capturedImagePath", path)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}