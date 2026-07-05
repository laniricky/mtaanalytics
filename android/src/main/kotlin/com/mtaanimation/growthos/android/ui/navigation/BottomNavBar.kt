package com.mtaanimation.growthos.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.mtaanimation.growthos.android.ui.theme.BrandMuted
import com.mtaanimation.growthos.android.ui.theme.BrandOrange
import com.mtaanimation.growthos.android.ui.theme.BrandSurface

/**
 * Shared bottom navigation bar used across all main screens.
 * Navigates between Dashboard, Platforms, Goals, Revenue and Settings.
 */
@Composable
fun AppBottomNavBar(navController: NavController) {
    data class NavItem(val label: String, val icon: ImageVector, val screen: Screen)

    val items = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard),
        NavItem("Check-In", Icons.Default.Add, Screen.LogStats),
        NavItem("Milestones", Icons.Default.Flag, Screen.CustomGoals),
        NavItem("Revenue", Icons.Default.AttachMoney, Screen.Revenue),
        NavItem("Settings", Icons.Default.Settings, Screen.Settings)
    )

    NavigationBar(containerColor = BrandSurface) {
        val currentRoute = navController.currentDestination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (currentRoute == item.screen.route) BrandOrange else BrandMuted
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentRoute == item.screen.route) BrandOrange else BrandMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BrandOrange.copy(alpha = 0.15f)
                )
            )
        }
    }
}
