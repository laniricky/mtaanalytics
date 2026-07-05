package com.mtaanimation.growthos.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mtaanimation.growthos.android.ui.auth.AuthViewModel
import com.mtaanimation.growthos.android.ui.auth.LoginScreen
import com.mtaanimation.growthos.android.ui.auth.RegisterScreen
import com.mtaanimation.growthos.android.ui.dashboard.DashboardScreen

/**
 * Root navigation graph.
 *
 * AUTH FLOW: Login ↔ Register → Dashboard
 *
 * After a successful login or registration the back stack is cleared so
 * pressing Back does not return to the auth screens.
 *
 * A single [AuthViewModel] is scoped to the NavController so both Login and
 * Register share state without duplicating the Ktor client.
 */
@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String = Screen.Login.route) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.navigateUp() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.Platforms.route) {
            com.mtaanimation.growthos.android.ui.platforms.PlatformsScreen(navController = navController)
        }

        composable(Screen.LogStats.route) {
            com.mtaanimation.growthos.android.ui.goals.GoalsScreen(navController = navController)
        }

        composable(Screen.Revenue.route) {
            com.mtaanimation.growthos.android.ui.revenue.RevenueScreen(navController = navController)
        }
        
        composable(Screen.Episodes.route) {
            com.mtaanimation.growthos.android.ui.episodes.EpisodesScreen(navController = navController)
        }

        composable(Screen.Uploads.route) {
            com.mtaanimation.growthos.android.ui.uploads.UploadsScreen(navController = navController)
        }

        composable(Screen.CustomGoals.route) {
            com.mtaanimation.growthos.android.ui.customgoals.CustomGoalsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            com.mtaanimation.growthos.android.ui.settings.SettingsScreen(navController = navController)
        }
    }
}
