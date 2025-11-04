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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

// Sample places near 41st and Lamar in Austin, TX
val socialPlaces = listOf(
    SocialPlace(1, "Houndstooth Coffee", PlaceType.COFFEE, LatLng(30.3106, -97.74), 4),
    SocialPlace(2, "Draught House Pub", PlaceType.BAR, LatLng(30.3111, -97.7428), 9),
    SocialPlace(3, "Central Market North Lamar", PlaceType.RESTAURANT, LatLng(30.3077, -97.7399), 4),
    SocialPlace(4, "Mazur Coffee", PlaceType.COFFEE, LatLng(30.31165, -97.7423), 3),
    SocialPlace(5, "Rudy's BBQ", PlaceType.RESTAURANT, LatLng(30.3076, -97.74195), 7)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    buzzInState: com.buzzin.app.BuzzInState = com.buzzin.app.BuzzInState(),
    onBuzzIn: (Int, String, LocationType, Int) -> Unit = { _, _, _, _ -> },
    onBuzzOut: () -> Unit = {}
) {
    // Default location: 41st and Lamar, Austin, TX
    val center41stAndLamar = LatLng(30.30914, -97.7412)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center41stAndLamar, 16f)
    }
    
    var selectedPlace by remember { mutableStateOf<SocialPlace?>(null) }
    var showLocationDetail by remember { mutableStateOf(false) }
    // Disable My Location for now (requires runtime permissions)
    var showMyLocation by remember { mutableStateOf(false) }

    // Show LocationDetailScreen if user is buzzed in
    if (buzzInState.isBuzzedIn) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            LocationDetailScreen(
                locationId = buzzInState.locationId ?: 0,
                locationName = buzzInState.locationName ?: "",
                locationType = buzzInState.locationType ?: LocationType.RESTAURANT,
                buzzInCount = buzzInState.buzzInCount,
                onBack = onBuzzOut
            )
        }
        return
    }

    // Show LocationDetailScreen when "Buzz In" is clicked from map
    if (showLocationDetail && selectedPlace != null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            LocationDetailScreen(
                locationId = selectedPlace!!.id,
                locationName = selectedPlace!!.name,
                locationType = when(selectedPlace!!.type) {
                    PlaceType.COFFEE -> LocationType.COFFEE
                    PlaceType.RESTAURANT -> LocationType.RESTAURANT
                    PlaceType.BAR -> LocationType.RESTAURANT
                    PlaceType.CONCERT -> LocationType.RESTAURANT
                },
                buzzInCount = selectedPlace!!.activeUsers,
                onBack = {
                    // Buzz out
                    onBuzzOut()
                    showLocationDetail = false
                    selectedPlace = null
                }
            )
        }
        return
    }

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
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    rotationGesturesEnabled = true
                )
            ) {
                // User's current location marker
                Marker(
                    state = MarkerState(position = center41stAndLamar),
                    title = "You are here",
                    snippet = "Your current location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )

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
                // Center map on 41st and Lamar
                cameraPositionState.position = CameraPosition.fromLatLngZoom(center41stAndLamar, 16f)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 16.dp),
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
                            onClick = {
                                // Call onBuzzIn callback with location details
                                val locationType = when(place.type) {
                                    PlaceType.COFFEE -> LocationType.COFFEE
                                    PlaceType.RESTAURANT -> LocationType.RESTAURANT
                                    PlaceType.BAR -> LocationType.RESTAURANT
                                    PlaceType.CONCERT -> LocationType.RESTAURANT
                                }
                                onBuzzIn(place.id, place.name, locationType, place.activeUsers)
                                showLocationDetail = true
                            },
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
