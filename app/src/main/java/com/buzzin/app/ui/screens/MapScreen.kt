package com.buzzin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Sample data for social places
data class SocialPlace(
    val id: Int,
    val name: String,
    val type: PlaceType,
    val location: LatLng,
    val activeUsers: Int = 0
)

enum class PlaceType {
    COFFEE,
    RESTAURANT,
    BAR,
    CONCERT
}

// Sample places in San Francisco area
val socialPlaces = listOf(
    SocialPlace(1, "Blue Bottle Coffee", PlaceType.COFFEE, LatLng(37.7749, -122.4194), 5),
    SocialPlace(2, "The Grove", PlaceType.RESTAURANT, LatLng(37.7739, -122.4312), 12),
    SocialPlace(3, "Philz Coffee", PlaceType.COFFEE, LatLng(37.7858, -122.4064), 8),
    SocialPlace(4, "Zuni Café", PlaceType.RESTAURANT, LatLng(37.7764, -122.4234), 15),
    SocialPlace(5, "Sightglass Coffee", PlaceType.COFFEE, LatLng(37.7699, -122.4102), 6),
    SocialPlace(6, "Foreign Cinema", PlaceType.RESTAURANT, LatLng(37.7599, -122.4194), 10),
    SocialPlace(7, "Ritual Coffee", PlaceType.COFFEE, LatLng(37.7489, -122.4194), 4),
    SocialPlace(8, "Nopa", PlaceType.RESTAURANT, LatLng(37.7749, -122.4394), 18)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    // Default location: San Francisco
    val sanFrancisco = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(sanFrancisco, 13f)
    }
    
    var selectedPlace by remember { mutableStateOf<SocialPlace?>(null) }
    // Disable My Location for now (requires runtime permissions)
    var showMyLocation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TopAppBar(
                title = { 
                    Column {
                        Text("Buzz In", fontWeight = FontWeight.Bold)
                        Text(
                            "Find nearby social spots",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = showMyLocation,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    rotationGesturesEnabled = true
                )
            ) {
                // Add markers for each social place
                socialPlaces.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.location),
                        title = place.name,
                        snippet = "${place.activeUsers} people buzzed in",
                        onClick = {
                            selectedPlace = place
                            true
                        }
                    )
                }
            }
        }

        // Floating Action Button - My Location
        FloatingActionButton(
            onClick = {
                // Center map on current location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(sanFrancisco, 13f)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
                tint = Color.White
            )
        }

        // Place Info Card (when a place is selected)
        selectedPlace?.let { place ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (place.type == PlaceType.COFFEE) 
                                        Icons.Default.Coffee 
                                    else 
                                        Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = place.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Active users badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${place.activeUsers} buzzed in",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* Buzz In action */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Buzz In")
                        }
                        OutlinedButton(
                            onClick = { selectedPlace = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        // Legend/Filter chips at the top
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { /* Filter coffee shops */ },
                label = { Text("Coffee") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            FilterChip(
                selected = true,
                onClick = { /* Filter restaurants */ },
                label = { Text("Restaurants") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
