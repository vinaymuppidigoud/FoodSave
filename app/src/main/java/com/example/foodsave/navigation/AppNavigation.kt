package com.example.foodsave.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodsave.pages.DashboardScreen
import com.example.foodsave.pages.IntroScreen
import com.example.foodsave.pages.LoginScreen
import com.example.foodsave.pages.RegisterScreen

// Defines the app's navigation screens
sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNavigation() {

    // navigation controller
    val navController = rememberNavController()

    // Sets up the app's navigation
    NavHost(
        navController = navController,
        startDestination = Screen.Intro.route
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
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
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
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Dashboard screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}