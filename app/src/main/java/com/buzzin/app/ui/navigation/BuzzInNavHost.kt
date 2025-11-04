package com.buzzin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buzzin.app.ui.screens.HomeScreen
import com.buzzin.app.ui.screens.MapScreen
import com.buzzin.app.ui.screens.ProfileScreen
import com.buzzin.app.ui.screens.SettingsScreen

@Composable
fun BuzzInNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BuzzInDestinations.HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(BuzzInDestinations.HOME_ROUTE) {
            HomeScreen()
        }
        
        composable(BuzzInDestinations.MAP_ROUTE) {
            MapScreen()
        }
        
        composable(BuzzInDestinations.PROFILE_ROUTE) {
            ProfileScreen()
        }
        
        composable(BuzzInDestinations.SETTINGS_ROUTE) {
            SettingsScreen()
        }
        
        // Add more destinations as needed
    }
}

object BuzzInDestinations {
    const val HOME_ROUTE = "home"
    const val MAP_ROUTE = "map"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    const val LOGIN_ROUTE = "login"
    const val BUZZ_IN_ROUTE = "buzzin"
    const val MATCHES_ROUTE = "matches"
    const val CHAT_ROUTE = "chat"
}
