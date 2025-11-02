package com.buzzin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buzzin.app.ui.screens.home.HomeScreen
import com.buzzin.app.ui.screens.splash.SplashScreen

@Composable
fun BuzzInNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BuzzInDestinations.SPLASH_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(BuzzInDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(BuzzInDestinations.HOME_ROUTE) {
                        popUpTo(BuzzInDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        composable(BuzzInDestinations.HOME_ROUTE) {
            HomeScreen()
        }
        
        // Add more destinations as needed
    }
}

object BuzzInDestinations {
    const val SPLASH_ROUTE = "splash"
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val PROFILE_ROUTE = "profile"
    const val BUZZ_IN_ROUTE = "buzzin"
    const val MATCHES_ROUTE = "matches"
    const val CHAT_ROUTE = "chat"
}

