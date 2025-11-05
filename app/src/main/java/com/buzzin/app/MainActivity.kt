package com.buzzin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.buzzin.app.ui.screens.HomeScreen
import com.buzzin.app.ui.screens.MapScreen
import com.buzzin.app.ui.screens.ProfileScreen
import com.buzzin.app.ui.screens.SettingsScreen
import com.buzzin.app.ui.theme.BuzzInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuzzInTheme {
                MainScreen()
            }
        }
    }
}

// State for tracking user's buzzed in location
data class BuzzInState(
    val isBuzzedIn: Boolean = false,
    val locationId: Int? = null,
    val locationName: String? = null,
    val locationType: com.buzzin.app.ui.screens.LocationType? = null,
    val buzzInCount: Int = 0,
    val description: String? = null
)

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(1) } // Default to Discover (index 1)
    var buzzInState by remember { mutableStateOf(BuzzInState()) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Profile
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                
                // Discover
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Discover") },
                    label = { Text("Discover") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                
                // Buzz In
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Place, contentDescription = "Buzz In") },
                    label = { Text("Buzz In") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                
                // Liked You
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Liked You") },
                    label = { Text("Liked You") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                
                // Chats
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Email, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ProfileScreen()
                1 -> HomeScreen()
                2 -> MapScreen(
                    buzzInState = buzzInState,
                    onBuzzIn = { locationId, locationName, locationType, buzzInCount, description ->
                        buzzInState = BuzzInState(
                            isBuzzedIn = true,
                            locationId = locationId,
                            locationName = locationName,
                            locationType = locationType,
                            buzzInCount = buzzInCount,
                            description = description
                        )
                    },
                    onBuzzOut = {
                        buzzInState = BuzzInState()
                    }
                )
                3 -> SettingsScreen()
                4 -> SettingsScreen()
            }
        }
    }
}
