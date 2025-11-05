package com.buzzin.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

// Function to fetch nearby places using Google Places API (New)
suspend fun fetchNearbyPlaces(
    context: android.content.Context,
    location: LatLng,
    apiKey: String
): List<SocialPlace> {
    return try {
        android.util.Log.d("MapScreen", "Starting to fetch nearby places at location: ${location.latitude}, ${location.longitude}")
        android.util.Log.d("MapScreen", "API Key available: ${apiKey.isNotEmpty()}")

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            android.util.Log.d("MapScreen", "Initializing Places API")
            Places.initialize(context, apiKey)
        }

        val placesClient = Places.createClient(context)

        // Define fields to return in Place objects
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.TYPES
        )

        // Create circular search area: 0.002 degrees ≈ 222 meters radius
        val circle = CircularBounds.newInstance(location, 222.0)

        // Specify place types to include
        val includedTypes = listOf("restaurant", "cafe", "bar")

        // Build the search request using SearchNearbyRequest (NEW API)
        android.util.Log.d("MapScreen", "Creating SearchNearbyRequest with NEW Places API")
        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(includedTypes)
            .setMaxResultCount(5)
            .build()

        // Execute search
        android.util.Log.d("MapScreen", "Executing searchNearby request")
        val response = placesClient.searchNearby(searchNearbyRequest).await()

        android.util.Log.d("MapScreen", "Received ${response.places.size} places from API")

        // Convert results to SocialPlace objects
        val socialPlaces = response.places.map { place ->
            val types = place.placeTypes ?: emptyList()
            android.util.Log.d("MapScreen", "Place: ${place.name}, Types: $types")

            val placeType = when {
                types.contains("cafe") -> PlaceType.COFFEE
                types.contains("bar") -> PlaceType.BAR
                types.contains("restaurant") -> PlaceType.RESTAURANT
                else -> PlaceType.RESTAURANT
            }

            SocialPlace(
                id = place.id.hashCode(),
                name = place.name ?: "Unknown Place",
                type = placeType,
                location = place.latLng ?: location,
                activeUsers = (1..10).random()
            )
        }

        android.util.Log.d("MapScreen", "Returning ${socialPlaces.size} places")
        socialPlaces
    } catch (e: Exception) {
        android.util.Log.e("MapScreen", "Error fetching nearby places: ${e.message}", e)
        emptyList()
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    buzzInState: com.buzzin.app.BuzzInState = com.buzzin.app.BuzzInState(),
    onBuzzIn: (Int, String, LocationType, Int) -> Unit = { _, _, _, _ -> },
    onBuzzOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Default location: 41st and Lamar, Austin, TX (used as fallback)
    val center41stAndLamar = LatLng(30.30914, -97.7412)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var nearbyPlaces by remember { mutableStateOf<List<SocialPlace>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center41stAndLamar, 16f)
    }

    var selectedPlace by remember { mutableStateOf<SocialPlace?>(null) }
    var showLocationDetail by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            // Get user location when permission is granted
            scope.launch {
                try {
                    val cancellationTokenSource = CancellationTokenSource()
                    val location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()

                    location?.let { loc ->
                        val newLocation = LatLng(loc.latitude, loc.longitude)
                        userLocation = newLocation
                        android.util.Log.d("MapScreen", "User location obtained: ${newLocation.latitude}, ${newLocation.longitude}")

                        // Center camera on user location
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 16f)

                        // Fetch nearby places
                        val apiKey = context.packageManager
                            .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                            .metaData
                            .getString("com.google.android.geo.API_KEY") ?: ""
                        android.util.Log.d("MapScreen", "About to fetch nearby places after getting location")
                        nearbyPlaces = fetchNearbyPlaces(context, newLocation, apiKey)
                        android.util.Log.d("MapScreen", "Fetched ${nearbyPlaces.size} nearby places")
                    }
                } catch (e: Exception) {
                    // Handle location fetch error
                    android.util.Log.e("MapScreen", "Error getting location", e)
                }
            }
        }
    }

    // Request location permission on first composition
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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
                    isMyLocationEnabled = locationPermissionGranted,
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
                // User's current location marker (custom blue marker)
                userLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "You are here",
                        snippet = "Your current location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }

                // Add markers for each nearby place
                nearbyPlaces.forEach { place ->
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
                // Center map on user's location if available, otherwise fallback to default
                val targetLocation = userLocation ?: center41stAndLamar
                cameraPositionState.position = CameraPosition.fromLatLngZoom(targetLocation, 16f)

                // Fetch nearby places when button is clicked
                scope.launch {
                    val apiKey = context.packageManager
                        .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                        .metaData
                        .getString("com.google.android.geo.API_KEY") ?: ""
                    android.util.Log.d("MapScreen", "My Location button clicked, fetching places")
                    nearbyPlaces = fetchNearbyPlaces(context, targetLocation, apiKey)
                }
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
                                // Clear local map state - we're now buzzed in, managed by MainActivity
                                showLocationDetail = false
                                selectedPlace = null
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
