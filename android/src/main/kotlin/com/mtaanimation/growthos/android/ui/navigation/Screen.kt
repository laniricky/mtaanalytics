package com.mtaanimation.growthos.android.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 * Adding a new screen requires a single object addition here.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object Platforms : Screen("platforms")
    data object Goals : Screen("goals")
    data object Reports : Screen("reports")
    data object Revenue : Screen("revenue")
    data object Episodes : Screen("episodes")
    data object Settings : Screen("settings")
}
