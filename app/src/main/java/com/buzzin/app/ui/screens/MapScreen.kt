package com.buzzin.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amplifyframework.core.Amplify
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// Calculate distance between two LatLng points in meters (Haversine formula)
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

// Sample data for social places
data class SocialPlace(
    val id: String,
    val name: String,
    val type: PlaceType,
    val location: LatLng,
    val activeUsers: Int = 0,
    val address: String = "",
    val description: String = ""
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

// Function to save a location to the backend database
suspend fun saveLocationToBackend(
    id: String,
    name: String,
    latitude: Double,
    longitude: Double,
    address: String,
    type: PlaceType
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val mutation = """
                mutation CreateLocation {
                  createLocation(input: {
                    id: "$id",
                    name: "${name.replace("\"", "\\\"")}",
                    latitude: $latitude,
                    longitude: $longitude,
                    address: "${address.replace("\"", "\\\"")}",
                    type: $type
                  }) {
                    id
                    name
                  }
                }
            """.trimIndent()

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
                    Log.d("MapScreen", "Location saved: ${response.data}")
                    success = true
                },
                { err ->
                    // Check if error is "duplicate key" which means it already exists
                    val errorMsg = err.toString()
                    if (errorMsg.contains("duplicate") || errorMsg.contains("already exists")) {
                        Log.d("MapScreen", "Location already exists: $id")
                        success = true // Treat as success
                    } else {
                        Log.e("MapScreen", "Failed to save location", err)
                        error = Exception(err.toString())
                    }
                }
            )

            var timeout = 0
            while (!success && error == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (error != null) throw error!!
            success
        } catch (e: Exception) {
            Log.e("MapScreen", "Error saving location to backend", e)
            false
        }
    }
}

// Function to search Places API and save to backend
suspend fun searchAndSavePlacesFromGoogle(
    placesClient: PlacesClient,
    center: LatLng,
    radiusMeters: Double = 400.0
): List<SocialPlace> {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("MapScreen", "Searching Google Places near ${center.latitude}, ${center.longitude}")

            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.TYPES,
                Place.Field.EDITORIAL_SUMMARY
            )

            // Search for restaurants, cafes, and bars
            val includedTypes = listOf(
                "restaurant",
                "cafe",
                "bar"
            )

            val circle = CircularBounds.newInstance(center, radiusMeters)

            val searchRequest = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(includedTypes)
                .setMaxResultCount(10)
                .build()

            val searchTask = placesClient.searchNearby(searchRequest)
            val searchResponse = searchTask.await()

            val places = searchResponse.places
            Log.d("MapScreen", "Found ${places.size} places from Google Places API")

            val socialPlaces = mutableListOf<SocialPlace>()

            for (place in places) {
                try {
                    val placeLatLng = place.latLng ?: continue
                    val placeName = place.name ?: "Unknown Place"
                    val placeAddress = place.address ?: ""
                    val placeId = place.id ?: UUID.randomUUID().toString()
                    val placeDescription = place.editorialSummary ?: ""

                    // Determine place type from Google types
                    val placeType = when {
                        place.placeTypes?.any { it.contains("cafe") || it.contains("coffee") } == true -> PlaceType.COFFEE
                        place.placeTypes?.any { it.contains("bar") || it.contains("night_club") } == true -> PlaceType.BAR
                        place.placeTypes?.any { it.contains("restaurant") } == true -> PlaceType.RESTAURANT
                        else -> PlaceType.OTHER
                    }

                    // Generate a consistent backend ID
                    val backendId = "google-place-${placeId}"

                    // Save to backend (async, don't block on errors)
                    try {
                        saveLocationToBackend(
                            id = backendId,
                            name = placeName,
                            latitude = placeLatLng.latitude,
                            longitude = placeLatLng.longitude,
                            address = placeAddress,
                            type = placeType
                        )
                        Log.d("MapScreen", "Saved place to backend: $placeName")
                    } catch (e: Exception) {
                        Log.w("MapScreen", "Failed to save place to backend: $placeName", e)
                        // Continue anyway - we can still show the place
                    }

                    socialPlaces.add(
                        SocialPlace(
                            id = backendId,
                            name = placeName,
                            type = placeType,
                            location = LatLng(placeLatLng.latitude, placeLatLng.longitude),
                            activeUsers = 0, // Will be updated later
                            address = placeAddress,
                            description = placeDescription
                        )
                    )
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error processing place", e)
                }
            }

            Log.d("MapScreen", "Processed ${socialPlaces.size} places")
            socialPlaces

        } catch (e: Exception) {
            Log.e("MapScreen", "Error searching Google Places", e)
            emptyList()
        }
    }
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

// Function to create a check-in with single location enforcement
suspend fun createCheckIn(userId: String, locationId: String, latitude: Double, longitude: Double): Pair<Boolean, String?> {
    return withContext(Dispatchers.IO) {
        try {
            // First, check if user has ANY active check-ins (at any location)
            val checkQuery = """
                query ListCheckIns {
                  listCheckIns(filter: {
                    userId: {eq: "$userId"},
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

            Log.d("MapScreen", "Checking for any existing active check-ins for userId=$userId")

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

            // Parse the response and deactivate any existing active check-ins
            existingCheckIns?.let { response ->
                try {
                    val responseJson = JSONObject(response)
                    val listCheckIns = responseJson.getJSONObject("listCheckIns")
                    val items = listCheckIns.getJSONArray("items")

                    if (items.length() > 0) {
                        Log.d("MapScreen", "Found ${items.length()} active check-in(s), deactivating them...")

                        val now = java.time.Instant.now().toString()

                        // Deactivate all existing active check-ins
                        for (i in 0 until items.length()) {
                            val checkIn = items.getJSONObject(i)
                            val checkInId = checkIn.getString("id")
                            val oldLocationId = checkIn.getString("locationId")

                            // Skip if it's the same location
                            if (oldLocationId == locationId) {
                                Log.d("MapScreen", "User already checked in at this location")
                                return@withContext Pair(false, "You're already checked in here!")
                            }

                            Log.d("MapScreen", "Deactivating check-in $checkInId")

                            val deactivateMutation = """
                                mutation UpdateCheckIn {
                                  updateCheckIn(input: {
                                    id: "$checkInId",
                                    isActive: false,
                                    checkOutTime: "$now"
                                  }) {
                                    id
                                    isActive
                                  }
                                }
                            """.trimIndent()

                            var deactivateSuccess = false
                            var deactivateError: Exception? = null

                            val deactivateRequest = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                                deactivateMutation,
                                emptyMap<String, Any>(),
                                String::class.java,
                                com.amplifyframework.api.graphql.GraphQLRequest.VariablesSerializer { "{}" }
                            )

                            Amplify.API.mutate(
                                deactivateRequest,
                                {
                                    Log.d("MapScreen", "Deactivated check-in $checkInId")
                                    deactivateSuccess = true
                                },
                                { err ->
                                    Log.e("MapScreen", "Failed to deactivate check-in $checkInId", err)
                                    deactivateError = Exception(err.toString())
                                }
                            )

                            // Wait for deactivation
                            var deactivateTimeout = 0
                            while (!deactivateSuccess && deactivateError == null && deactivateTimeout < 50) {
                                Thread.sleep(100)
                                deactivateTimeout++
                            }
                        }
                    } else {
                        Log.d("MapScreen", "No existing active check-ins found")
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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    buzzInState: com.buzzin.app.BuzzInState = com.buzzin.app.BuzzInState(),
    onBuzzIn: (Int, String, LocationType, Int, String) -> Unit = { _, _, _, _, _ -> },
    onBuzzOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Initialize Places API
    val placesClient = remember {
        if (!Places.isInitialized()) {
            val apiKey = context.getString(context.resources.getIdentifier("maps_api_key", "string", context.packageName))
            Places.initialize(context, apiKey)
        }
        Places.createClient(context)
    }

    // Default location: 41st and Lamar, Austin, TX (used as fallback)
    val center41stAndLamar = LatLng(30.30914, -97.7412)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var nearbyPlaces by remember { mutableStateOf<List<SocialPlace>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center41stAndLamar, 16f)
    }

    var selectedPlace by remember { mutableStateOf<SocialPlace?>(null) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            // Get user location when permission is granted
            coroutineScope.launch {
                try {
                    val cancellationTokenSource = CancellationTokenSource()
                    val location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()

                    location?.let { loc ->
                        val newLocation = LatLng(loc.latitude, loc.longitude)
                        userLocation = newLocation
                        Log.d("MapScreen", "User location obtained: ${newLocation.latitude}, ${newLocation.longitude}")

                        // Center camera on user location
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newLocation, 16f)

                        // Location fetch will be triggered by LaunchedEffect(userLocation)
                        Log.d("MapScreen", "User location set, nearby places will be fetched automatically")
                    }
                } catch (e: Exception) {
                    // Handle location fetch error
                    Log.e("MapScreen", "Error getting location", e)
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

    // State for locations from API
    var socialPlaces by remember { mutableStateOf<List<SocialPlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetchingPlaces by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var checkInMessage by remember { mutableStateOf<String?>(null) }

    // Monitor location changes and fetch new places when user moves
    var lastFetchLocation by remember { mutableStateOf<LatLng?>(null) }

    DisposableEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) {
            return@DisposableEffect onDispose { }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30000L // Update every 30 seconds
        ).apply {
            setMinUpdateIntervalMillis(15000L) // Minimum 15 seconds between updates
            setMaxUpdateDelayMillis(60000L)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    userLocation = newLocation

                    // Check if we've moved significantly (500m) from last fetch location
                    val shouldFetchNewPlaces = lastFetchLocation?.let { lastLoc ->
                        val distance = calculateDistance(
                            lastLoc.latitude, lastLoc.longitude,
                            newLocation.latitude, newLocation.longitude
                        )
                        distance > 500 // Fetch new places if moved more than 500 meters
                    } ?: true // First time, always fetch

                    if (shouldFetchNewPlaces) {
                        Log.d("MapScreen", "User moved significantly, fetching new places at ${newLocation.latitude}, ${newLocation.longitude}")
                        isFetchingPlaces = true
                        coroutineScope.launch {
                            try {
                                val fetchedPlaces = searchAndSavePlacesFromGoogle(
                                    placesClient,
                                    newLocation
                                )
                                nearbyPlaces = fetchedPlaces
                                socialPlaces = fetchedPlaces
                                lastFetchLocation = newLocation
                                Log.d("MapScreen", "Fetched ${fetchedPlaces.size} new places")
                            } catch (e: Exception) {
                                Log.e("MapScreen", "Error fetching places on location change", e)
                            } finally {
                                isFetchingPlaces = false
                            }
                        }
                    }
                }
            }
        }

        try {
            // Permission is checked by locationPermissionGranted guard at top of DisposableEffect
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("MapScreen", "Location permission not granted", e)
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d("MapScreen", "Location updates stopped")
        }
    }

    var isCheckingIn by remember { mutableStateOf(false) }

    // Load user ID from config file (each developer can have their own)
    // In production, this would come from authentication
    val currentUserId = remember {
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("user_config", "raw", context.packageName)
            )
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            jsonObject.getString("userId")
        } catch (e: Exception) {
            Log.e("MapScreen", "Failed to load user config, using default", e)
            "test-user-fixed-id-12345"
        }
    }

    // Function to check out (deactivate active check-in)
    suspend fun checkOut() {
        try {
            Log.d("MapScreen", "Checking out user: $currentUserId")

            // Query for active check-ins
            val checkQuery = """
                query ListCheckIns {
                  listCheckIns(filter: {
                    userId: {eq: "$currentUserId"},
                    isActive: {eq: true}
                  }) {
                    items {
                      id
                      locationId
                    }
                  }
                }
            """.trimIndent()

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
                    Log.d("MapScreen", "Check-out query response: $existingCheckIns")
                },
                { err ->
                    Log.e("MapScreen", "Check-out query failed", err)
                    checkError = Exception(err.toString())
                }
            )

            // Wait for query result
            var timeout = 0
            while (existingCheckIns == null && checkError == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (checkError != null) {
                Log.e("MapScreen", "Check-out query error", checkError)
                return
            }

            // Parse and deactivate check-ins
            existingCheckIns?.let { response ->
                try {
                    val responseJson = JSONObject(response)
                    val listCheckIns = responseJson.getJSONObject("listCheckIns")
                    val items = listCheckIns.getJSONArray("items")

                    if (items.length() > 0) {
                        val now = java.time.Instant.now().toString()

                        for (i in 0 until items.length()) {
                            val checkIn = items.getJSONObject(i)
                            val checkInId = checkIn.getString("id")

                            Log.d("MapScreen", "Deactivating check-in: $checkInId")

                            val mutation = """
                                mutation UpdateCheckIn {
                                  updateCheckIn(input: {
                                    id: "$checkInId",
                                    isActive: false,
                                    checkOutTime: "$now"
                                  }) {
                                    id
                                    isActive
                                  }
                                }
                            """.trimIndent()

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
                                {
                                    Log.d("MapScreen", "Successfully checked out")
                                    success = true
                                },
                                { err ->
                                    Log.e("MapScreen", "Check-out failed", err)
                                    error = Exception(err.toString())
                                }
                            )

                            // Wait for mutation
                            var mutationTimeout = 0
                            while (!success && error == null && mutationTimeout < 50) {
                                Thread.sleep(100)
                                mutationTimeout++
                            }
                        }

                        Log.d("MapScreen", "Check-out complete")
                    } else {
                        Log.d("MapScreen", "No active check-ins to deactivate")
                    }
                } catch (e: Exception) {
                    Log.e("MapScreen", "Error parsing check-out response", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Check-out failed", e)
        }
    }

    // Function to reload locations
    suspend fun reloadLocations() {
        isLoading = true
        errorMessage = null
        try {
            val targetLocation = userLocation ?: center41stAndLamar
            val fetchedLocations = fetchNearbyLocations(
                targetLocation.latitude,
                targetLocation.longitude,
                radiusKm = 1.0
            )
            nearbyPlaces = fetchedLocations
            socialPlaces = fetchedLocations
            Log.d("MapScreen", "Reloaded ${fetchedLocations.size} locations from backend")
            //if (fetchedLocations.isEmpty()) {
            //    errorMessage = "No locations found nearby"
            //}
        } catch (e: Exception) {
            Log.e("MapScreen", "Error reloading locations", e)
            errorMessage = "Failed to reload locations: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Fetch locations from Google Places API after user location is set
    LaunchedEffect(userLocation) {
        // Only fetch if we have a user location and haven't fetched yet
        val currentUserLocation = userLocation
        if (currentUserLocation != null && nearbyPlaces.isEmpty() && !isLoading) {
            isLoading = true
            errorMessage = null
            try {
                Log.d("MapScreen", "Initial load after user location set: Searching Google Places")
                val fetchedLocations = searchAndSavePlacesFromGoogle(
                    placesClient,
                    currentUserLocation
                )
                nearbyPlaces = fetchedLocations
                socialPlaces = fetchedLocations
                lastFetchLocation = currentUserLocation
                Log.d("MapScreen", "Loaded ${fetchedLocations.size} locations from Google Places")
                //if (fetchedLocations.isEmpty()) {
                //    errorMessage = "No locations found nearby"
                //}
            } catch (e: Exception) {
                Log.e("MapScreen", "Error loading locations", e)
                errorMessage = "Failed to load locations: ${e.message}"
            } finally {
                isLoading = false
            }
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
                onBack = {
                    // Clear check-in message
                    checkInMessage = null
                    // First update UI state immediately
                    onBuzzOut()
                    // Then do backend cleanup in background
                    coroutineScope.launch {
                        checkOut()
                        reloadLocations()
                    }
                },
                realLocationId = buzzInState.realLocationId ?: "",
                currentUserId = currentUserId
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
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
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

        // Unified status indicator for loading, errors, and empty states
        /*if (isLoading || isFetchingPlaces || errorMessage != null || (!isLoading && socialPlaces.isEmpty())) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        errorMessage != null -> MaterialTheme.colorScheme.errorContainer
                        else -> Color.White.copy(alpha = 0.95f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        isLoading || isFetchingPlaces -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Finding nearby places...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                        errorMessage != null -> {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        socialPlaces.isEmpty() -> {
                            Text(
                                text = "No locations found nearby. Try moving to a different area.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }*/

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
                // Center map on user's location if available, otherwise fallback to default
                val targetLocation = userLocation ?: center41stAndLamar
                cameraPositionState.position = CameraPosition.fromLatLngZoom(targetLocation, 16f)

                // Reload nearby locations when button is clicked
                coroutineScope.launch {
                    nearbyPlaces = fetchNearbyLocations(targetLocation.latitude, targetLocation.longitude, radiusKm = 1.0)
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
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
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
                                    color = Color.Black
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
                                            Log.d("MapScreen", "Check-in successful, updating global state...")

                                            // Update global buzz-in state so it persists across navigation
                                            val locationType = when(place.type) {
                                                PlaceType.COFFEE -> LocationType.COFFEE
                                                PlaceType.RESTAURANT -> LocationType.RESTAURANT
                                                PlaceType.BAR -> LocationType.RESTAURANT
                                                PlaceType.CONCERT -> LocationType.RESTAURANT
                                                else -> LocationType.RESTAURANT
                                            }

                                            onBuzzIn(
                                                place.id.hashCode(),
                                                place.name,
                                                locationType,
                                                place.activeUsers,
                                                place.id  // realLocationId
                                            )

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
