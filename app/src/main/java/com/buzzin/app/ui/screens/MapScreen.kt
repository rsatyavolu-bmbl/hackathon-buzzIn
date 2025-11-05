package com.buzzin.app.ui.screens

import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// Sample data for social places
data class SocialPlace(
    val id: String,
    val name: String,
    val type: PlaceType,
    val location: LatLng,
    val activeUsers: Int = 0,
    val address: String = ""
)

enum class PlaceType {
    COFFEE,
    RESTAURANT,
    BAR,
    CONCERT,
    PARK,
    GYM,
    OTHER
}

// Function to fetch nearby locations from Amplify API
suspend fun fetchNearbyLocations(latitude: Double, longitude: Double, radiusKm: Double = 10.0): List<SocialPlace> {
    return withContext(Dispatchers.IO) {
        try {
            // Call the custom listNearbyLocations query
            val queryDoc = """
                query ListNearbyLocations {
                  listNearbyLocations(latitude: $latitude, longitude: $longitude, radiusKm: $radiusKm)
                }
            """.trimIndent()

            Log.d("MapScreen", "Calling listNearbyLocations API...")
            Log.d("MapScreen", "Query: $queryDoc")

            var result: List<SocialPlace>? = null
            var error: Exception? = null

            val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                queryDoc,
                emptyMap<String, Any>(),
                String::class.java,
                com.amplifyframework.api.graphql.GraphQLRequest.VariablesSerializer { "{}" }
            )

            Amplify.API.query(
                request,
                { response ->
                    Log.d("MapScreen", "API Response: ${response.data}")
                    try {
                        // Parse the GraphQL response
                        val responseJson = JSONObject(response.data as String)
                        val locationsJsonString = responseJson.getString("listNearbyLocations")
                        val jsonArray = JSONArray(locationsJsonString)
                        val locations = mutableListOf<SocialPlace>()

                        for (i in 0 until jsonArray.length()) {
                            val location = jsonArray.getJSONObject(i)
                            val typeStr = location.optString("type", "OTHER")
                            val placeType = try {
                                PlaceType.valueOf(typeStr)
                            } catch (e: Exception) {
                                PlaceType.OTHER
                            }

                            locations.add(
                                SocialPlace(
                                    id = location.optString("id", ""),
                                    name = location.getString("name"),
                                    type = placeType,
                                    location = LatLng(
                                        location.getDouble("latitude"),
                                        location.getDouble("longitude")
                                    ),
                                    activeUsers = location.optInt("activeUsers", 0),
                                    address = location.optString("address", "")
                                )
                            )
                        }

                        result = locations
                        Log.d("MapScreen", "Parsed ${locations.size} locations")
                    } catch (e: Exception) {
                        Log.e("MapScreen", "Error parsing response", e)
                        error = e
                    }
                },
                { err ->
                    Log.e("MapScreen", "API Error", err)
                    error = Exception(err.toString())
                }
            )

            // Wait for async result
            var timeout = 0
            while (result == null && error == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (error != null) throw error!!
            result ?: emptyList()
        } catch (e: Exception) {
            Log.e("MapScreen", "Failed to fetch locations", e)
            emptyList()
        }
    }
}

// Function to create a check-in with duplicate prevention
suspend fun createCheckIn(userId: String, locationId: String, latitude: Double, longitude: Double): Pair<Boolean, String?> {
    return withContext(Dispatchers.IO) {
        try {
            // First, check if user already has an active check-in at this location
            val checkQuery = """
                query ListCheckIns {
                  listCheckIns(filter: {
                    userId: {eq: "$userId"},
                    locationId: {eq: "$locationId"},
                    isActive: {eq: true}
                  }) {
                    items {
                      id
                      userId
                      locationId
                    }
                  }
                }
            """.trimIndent()

            Log.d("MapScreen", "Checking for existing check-in: userId=$userId, locationId=$locationId")

            var existingCheckIns: String? = null
            var checkError: Exception? = null

            val checkRequest = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                checkQuery,
                emptyMap<String, Any>(),
                String::class.java,
                com.amplifyframework.api.graphql.GraphQLRequest.VariablesSerializer { "{}" }
            )

            Amplify.API.query(
                checkRequest,
                { response ->
                    existingCheckIns = response.data as String
                    Log.d("MapScreen", "Existing check-ins response: $existingCheckIns")
                },
                { err ->
                    Log.e("MapScreen", "Check-in query failed", err)
                    checkError = Exception(err.toString())
                }
            )

            // Wait for check query result
            var timeout = 0
            while (existingCheckIns == null && checkError == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (checkError != null) {
                throw checkError!!
            }

            // Parse the response to check if any items exist
            existingCheckIns?.let { response ->
                try {
                    val responseJson = JSONObject(response)
                    val listCheckIns = responseJson.getJSONObject("listCheckIns")
                    val items = listCheckIns.getJSONArray("items")

                    if (items.length() > 0) {
                        Log.d("MapScreen", "User already has an active check-in at this location")
                        return@withContext Pair(false, "You're already checked in here!")
                    } else {
                        Log.d("MapScreen", "No existing check-in found, proceeding with creation")
                    }
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error parsing check-in query response", e)
                }
            }

            // No existing check-in found, proceed with creation
            val timestamp = java.time.Instant.now().toString()
            val mutation = """
                mutation CreateCheckIn {
                  createCheckIn(input: {
                    userId: "$userId",
                    locationId: "$locationId",
                    checkInTime: "$timestamp",
                    isActive: true,
                    latitude: $latitude,
                    longitude: $longitude
                  }) {
                    id
                    userId
                    locationId
                    isActive
                  }
                }
            """.trimIndent()

            Log.d("MapScreen", "Creating new check-in: userId=$userId, locationId=$locationId")

            var success = false
            var error: Exception? = null

            val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                mutation,
                emptyMap<String, Any>(),
                String::class.java,
                com.amplifyframework.api.graphql.GraphQLRequest.VariablesSerializer { "{}" }
            )

            Amplify.API.mutate(
                request,
                { response ->
                    Log.d("MapScreen", "Check-in created: ${response.data}")
                    success = true
                },
                { err ->
                    Log.e("MapScreen", "Check-in failed", err)
                    error = Exception(err.toString())
                }
            )

            // Wait for async result
            timeout = 0
            while (!success && error == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (error != null) {
                throw error!!
            }
            Pair(success, null)
        } catch (e: Exception) {
            Log.e("MapScreen", "Failed to create check-in", e)
            Pair(false, "Failed to check in: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    buzzInState: com.buzzin.app.BuzzInState = com.buzzin.app.BuzzInState(),
    onBuzzIn: (Int, String, LocationType, Int) -> Unit = { _, _, _, _ -> },
    onBuzzOut: () -> Unit = {}
) {
    // Coroutine scope for launching async operations
    val coroutineScope = rememberCoroutineScope()

    // Default location: 41st and Lamar, Austin, TX
    val center41stAndLamar = LatLng(30.30914, -97.7412)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center41stAndLamar, 16f)
    }

    var selectedPlace by remember { mutableStateOf<SocialPlace?>(null) }
    var showLocationDetail by remember { mutableStateOf(false) }
    // Disable My Location for now (requires runtime permissions)
    var showMyLocation by remember { mutableStateOf(false) }

    // State for locations from API
    var socialPlaces by remember { mutableStateOf<List<SocialPlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var checkInMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingIn by remember { mutableStateOf(false) }

    // Use a consistent test user ID (in production, this would come from authentication)
    // Using a fixed ID so duplicate check-in prevention works across app restarts
    val currentUserId = "test-user-fixed-id-12345"

    // Function to reload locations
    suspend fun reloadLocations() {
        isLoading = true
        errorMessage = null
        try {
            val fetchedLocations = fetchNearbyLocations(
                center41stAndLamar.latitude,
                center41stAndLamar.longitude,
                radiusKm = 10.0
            )
            socialPlaces = fetchedLocations
            Log.d("MapScreen", "Reloaded ${fetchedLocations.size} locations from API")
        } catch (e: Exception) {
            Log.e("MapScreen", "Error reloading locations", e)
            errorMessage = "Failed to reload locations: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Fetch locations from API when screen loads
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val fetchedLocations = fetchNearbyLocations(
                center41stAndLamar.latitude,
                center41stAndLamar.longitude,
                radiusKm = 10.0
            )
            socialPlaces = fetchedLocations
            Log.d("MapScreen", "Loaded ${fetchedLocations.size} locations from API")
            if (fetchedLocations.isEmpty()) {
                errorMessage = "No locations found nearby"
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Error loading locations", e)
            errorMessage = "Failed to load locations: ${e.message}"
        } finally {
            isLoading = false
        }
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
                locationId = selectedPlace!!.id.hashCode(),  // Convert String to Int
                locationName = selectedPlace!!.name,
                locationType = when(selectedPlace!!.type) {
                    PlaceType.COFFEE -> LocationType.COFFEE
                    PlaceType.RESTAURANT -> LocationType.RESTAURANT
                    PlaceType.BAR -> LocationType.RESTAURANT
                    PlaceType.CONCERT -> LocationType.RESTAURANT
                    else -> LocationType.RESTAURANT
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

        // Loading indicator
        if (isLoading) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Loading locations...")
                }
            }
        }

        // Error message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Check-in message
        checkInMessage?.let { message ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.contains("Successfully"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.weight(1f),
                        color = if (message.contains("Successfully"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = { checkInMessage = null }) {
                        Text("Dismiss")
                    }
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
                                selectedPlace?.let { place ->
                                    coroutineScope.launch {
                                        isCheckingIn = true
                                        checkInMessage = null

                                        val (success, errorMsg) = createCheckIn(
                                            userId = currentUserId,
                                            locationId = place.id,
                                            latitude = place.location.latitude,
                                            longitude = place.location.longitude
                                        )

                                        isCheckingIn = false

                                        if (success) {
                                            checkInMessage = "Successfully buzzed in at ${place.name}!"
                                            Log.d("MapScreen", "Check-in successful, calling onBuzzIn callback...")

                                            // Call onBuzzIn callback to update buzz in state
                                            val locationType = when(place.type) {
                                                PlaceType.COFFEE -> LocationType.COFFEE
                                                PlaceType.RESTAURANT -> LocationType.RESTAURANT
                                                PlaceType.BAR -> LocationType.RESTAURANT
                                                PlaceType.CONCERT -> LocationType.RESTAURANT
                                                else -> LocationType.RESTAURANT
                                            }
                                            onBuzzIn(place.id.hashCode(), place.name, locationType, place.activeUsers)
                                            showLocationDetail = true

                                            // Reload locations to update active user counts
                                            reloadLocations()
                                        } else {
                                            // Show specific error message or generic one
                                            checkInMessage = errorMsg ?: "Failed to buzz in. Please try again."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCheckingIn
                        ) {
                            if (isCheckingIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Buzz In")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                selectedPlace = null
                                checkInMessage = null
                            },
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
