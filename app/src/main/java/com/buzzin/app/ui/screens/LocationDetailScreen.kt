package com.buzzin.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class LocationDetail(
    val id: Int,
    val name: String,
    val type: LocationType,
    val buzzInCount: Int
)

enum class LocationType {
    COFFEE,
    RESTAURANT
}

enum class ProfileState {
    ACTIVE,
    ACCEPTED,
    REJECTED
}

enum class ConnectionStatus {
    WAITING,
    CONNECTED,
    PASSED
}

// DetailProfile data class for ProfileCardWithActions compatibility
data class DetailProfile(
    val id: Int,
    val name: String,
    val age: Int,
    val distance: String,
    val bio: String,
    val occupation: String,
    val interests: List<String>,
    val photos: List<String>
)

data class LocationProfile(
    val id: Int,
    val userId: String, // Real user ID from database
    val name: String,
    val age: Int,
    val bio: String,
    val imageUrl: String,
    val occupation: String,
    val interests: List<String>,
    val photos: List<String>,
    var state: ProfileState = ProfileState.ACTIVE
)

// Mock profile images
val profileImages = listOf(
    "https://images.unsplash.com/photo-1650057861788-b6b8606b77ef?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHBlcnNvbnxlbnwxfHx8fDE3NjIyMjU1NTR8MA&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1557053910-d9eadeed1c58?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3b21hbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc2MjI1NDI1Mnww&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtYW4lMjBwb3J0cmFpdHxlbnwxfHx8fDE3NjIyODc2MjV8MA&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1560250097-0b93528c311a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBoZWFkc2hvdHxlbnwxfHx8fDE3NjIyMDM1MDl8MA&ixlib=rb-4.1.0&q=80&w=1080"
)

val names = listOf(
    // Female names
    "Emma", "Olivia", "Ava", "Isabella", "Sophia", "Mia", "Charlotte", "Amelia", "Harper", "Evelyn",
    "Abigail", "Emily", "Elizabeth", "Sofia", "Avery", "Ella", "Scarlett", "Grace", "Chloe", "Victoria",
    "Riley", "Aria", "Lily", "Aubrey", "Zoey", "Penelope", "Lillian", "Addison", "Layla", "Natalie",
    "Camila", "Hannah", "Brooklyn", "Zoe", "Nora", "Leah", "Savannah", "Audrey", "Claire", "Eleanor",

    // Male names
    "Liam", "Noah", "Oliver", "Elijah", "William", "James", "Benjamin", "Lucas", "Henry", "Alexander",
    "Mason", "Michael", "Ethan", "Daniel", "Jacob", "Logan", "Jackson", "Sebastian", "Jack", "Aiden",
    "Owen", "Samuel", "Matthew", "Joseph", "Levi", "Mateo", "David", "John", "Wyatt", "Carter",
    "Julian", "Luke", "Grayson", "Isaac", "Jayden", "Theodore", "Gabriel", "Anthony", "Dylan", "Leo",

    // Gender-neutral names
    "Jordan", "Taylor", "Morgan", "Casey", "Avery", "Quinn", "Reese", "Sage", "River", "Phoenix",
    "Dakota", "Skylar", "Rowan", "Cameron", "Blake", "Charlie", "Drew", "Finley", "Emerson", "Sawyer",
    "Kennedy", "Hayden", "Payton", "Parker", "Ashton", "Remi", "London", "Elliot", "Kendall", "Marlowe"
)

// Function to perform swipe and check for match
suspend fun performSwipe(
    userId: String,
    targetUserId: String,
    locationId: String,
    action: String // "LIKE" or "PASS"
): Pair<Boolean, Boolean> { // Returns (success, isMatch)
    return withContext(Dispatchers.IO) {
        try {
            val mutation = """
                mutation PerformSwipe {
                  performSwipe(
                    userId: "$userId",
                    targetUserId: "$targetUserId",
                    locationId: "$locationId",
                    action: "$action"
                  )
                }
            """.trimIndent()

            Log.d("LocationDetailScreen", "Performing swipe: userId=$userId, targetUserId=$targetUserId, action=$action")

            var responseData: String? = null
            var error: Exception? = null

            val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                mutation,
                emptyMap<String, Any>(),
                String::class.java,
                GraphQLRequest.VariablesSerializer { "{}" }
            )

            Amplify.API.mutate(
                request,
                { response ->
                    responseData = response.data as String
                    Log.d("LocationDetailScreen", "Swipe response: $responseData")
                },
                { err ->
                    Log.e("LocationDetailScreen", "Swipe failed", err)
                    error = Exception(err.toString())
                }
            )

            // Wait for async result
            var timeout = 0
            while (responseData == null && error == null && timeout < 50) {
                Thread.sleep(100)
                timeout++
            }

            if (error != null) {
                throw error!!
            }

            // Parse response to check for match
            responseData?.let { response ->
                try {
                    val responseJson = JSONObject(response)
                    val performSwipeResult = responseJson.getString("performSwipe")
                    val resultJson = JSONObject(performSwipeResult)
                    val isMatch = resultJson.optBoolean("isMatch", false)
                    Log.d("LocationDetailScreen", "Swipe recorded. Match: $isMatch")
                    return@withContext Pair(true, isMatch)
                } catch (e: Exception) {
                    Log.e("LocationDetailScreen", "Error parsing swipe response", e)
                }
            }

            Pair(true, false)
        } catch (e: Exception) {
            Log.e("LocationDetailScreen", "Failed to perform swipe", e)
            Pair(false, false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LocationDetailScreen(
    @Suppress("UNUSED_PARAMETER") locationId: Int,
    locationName: String,
    locationType: LocationType,
    buzzInCount: Int,
    onBack: () -> Unit,
    realLocationId: String = "", // Real location UUID from API
    currentUserId: String = "test-user-fixed-id-12345" // Current user's ID
) {
    val description = if (locationType == LocationType.COFFEE) {
        "A cozy coffee shop perfect for casual meetups and first dates. Enjoy artisan coffee and a relaxed atmosphere."
    } else {
        "A wonderful dining experience awaits. Great ambiance for dates and getting to know someone over delicious food."
    }

    val bioCoffee = "Coffee enthusiast looking to meet new people ☕"
    val bioRestaurant = "Foodie who loves trying new restaurants 🍽️"

    var selectedProfileIndex by remember { mutableStateOf<Int?>(null) }
    var showSelfieCapture by remember { mutableStateOf(false) }
    var selfieCaptureProfileName by remember { mutableStateOf("") }
    var swipedProfileIds by remember { mutableStateOf(setOf<Int>()) }
    var matchNotification by remember { mutableStateOf<String?>(null) }

    // Track connection status for each profile by their ID
    var profileConnectionStatuses by remember { mutableStateOf(mapOf<Int, ConnectionStatus>()) }

    // Dynamic buzz in count that updates from API
    var currentBuzzInCount by remember { mutableStateOf(buzzInCount) }

    // Function to fetch active users at location using direct GraphQL query
    suspend fun fetchActiveUsersCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("LocationDetailScreen", "Fetching active check-ins for location: $realLocationId")

                // Query active check-ins directly
                val queryDoc = """
                    query ListCheckIns {
                      listCheckIns(filter: {
                        locationId: {eq: "$realLocationId"},
                        isActive: {eq: true}
                      }) {
                        items {
                          id
                          userId
                        }
                      }
                    }
                """.trimIndent()

                var result: Int? = null

                val request = com.amplifyframework.api.graphql.SimpleGraphQLRequest<String>(
                    queryDoc,
                    emptyMap<String, Any>(),
                    String::class.java,
                    com.amplifyframework.api.graphql.GraphQLRequest.VariablesSerializer { "{}" }
                )

                Amplify.API.query(
                    request,
                    { response ->
                        Log.d("LocationDetailScreen", "Check-ins response: ${response.data}")
                        try {
                            val responseStr = response.data as String
                            val jsonResponse = JSONObject(responseStr)
                            val listCheckIns = jsonResponse.getJSONObject("listCheckIns")
                            val items = listCheckIns.getJSONArray("items")
                            result = items.length()
                            Log.d("LocationDetailScreen", "Active check-ins count: $result")
                        } catch (e: Exception) {
                            Log.e("LocationDetailScreen", "Error parsing check-ins", e)
                            result = 0
                        }
                    },
                    { err ->
                        Log.e("LocationDetailScreen", "Check-ins query failed", err)
                        result = 0
                    }
                )

                // Wait for async result
                var timeout = 0
                while (result == null && timeout < 50) {
                    Thread.sleep(100)
                    timeout++
                }

                result ?: 0 // Return 0 if query fails
            } catch (e: Exception) {
                Log.e("LocationDetailScreen", "Failed to fetch active check-ins", e)
                0 // Return 0 on error
            }
        }
    }

    // Fetch active users count periodically
    LaunchedEffect(realLocationId) {
        while (true) {
            val count = fetchActiveUsersCount()
            currentBuzzInCount = count
            Log.d("LocationDetailScreen", "Updated buzz in count: $currentBuzzInCount")
            kotlinx.coroutines.delay(5000) // Update every 5 seconds
        }
    }

    // Generate mock profiles with multiple photos - regenerate when count changes
    // TODO: Replace with real user data from API
    var allProfiles by remember { mutableStateOf(listOf<LocationProfile>()) }

    // Update profiles when buzz in count changes
    LaunchedEffect(currentBuzzInCount) {
        // Shuffle names to avoid repetition and create variety
        val shuffledNames = names.shuffled()

        allProfiles = List(currentBuzzInCount.coerceAtLeast(1)) { i ->
            val mainPhoto = profileImages[i % profileImages.size]
            val otherPhotos = profileImages.filterIndexed { idx, _ -> idx != (i % profileImages.size) }

            LocationProfile(
                id = i + 1,
                userId = "mock-user-${i + 1}", // Mock userId for now
                name = shuffledNames[i % shuffledNames.size], // Use shuffled names
                age = 24 + (i % 10),
                bio = if (locationType == LocationType.COFFEE) bioCoffee else bioRestaurant,
                imageUrl = mainPhoto,
                occupation = if (locationType == LocationType.COFFEE) "Designer" else "Marketing Manager",
                interests = if (locationType == LocationType.COFFEE)
                    listOf("Coffee", "Art", "Music", "Reading")
                else
                    listOf("Foodie", "Travel", "Cooking", "Wine"),
                photos = listOf(mainPhoto) + otherPhotos.take(2),
                state = ProfileState.ACTIVE
            )
        }
        Log.d("LocationDetailScreen", "Regenerated ${allProfiles.size} profiles with shuffled names")
    }

    // Show all profiles (don't filter out rejected ones, just advance past them)
    val profiles = allProfiles

    val pagerState = rememberPagerState(pageCount = { profiles.size })
    val coroutineScope = rememberCoroutineScope()

    // Show selfie capture screen if triggered
    if (showSelfieCapture) {
        SelfieCaptureScreen(
            profileName = selfieCaptureProfileName,
            locationType = locationType.name, // Pass "COFFEE" or "RESTAURANT"
            onBack = {
                showSelfieCapture = false
            },
            onSelfieCaptured = {
                showSelfieCapture = false
                // Get the page index - either from full-screen view or from pager
                val pageIndex = selectedProfileIndex ?: pagerState.currentPage

                // Mark this profile as CONNECTED
                val currentProfile = profiles[pageIndex]
                profileConnectionStatuses = profileConnectionStatuses + (currentProfile.id to ConnectionStatus.CONNECTED)

                // Move to next profile
                if (pageIndex < profiles.size - 1) {
                    // If we're in full-screen mode, update selectedProfileIndex
                    if (selectedProfileIndex != null) {
                        selectedProfileIndex = pageIndex + 1
                    }
                    // Advance the pager to next profile
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pageIndex + 1)
                    }
                } else {
                    // If this was the last profile and we're in full-screen, close it
                    if (selectedProfileIndex != null) {
                        selectedProfileIndex = null
                    }
                }
            }
        )
        return
    }

    // Show full-screen profile view if selected
    selectedProfileIndex?.let { index ->
        val profile = profiles[index]

        // Determine connection status for full-screen view
        val fullScreenConnectionStatus = profileConnectionStatuses[profile.id] ?: when (profile.state) {
            ProfileState.ACCEPTED -> ConnectionStatus.WAITING
            ProfileState.REJECTED -> ConnectionStatus.PASSED
            ProfileState.ACTIVE -> null
        }

        FullScreenProfileView(
            profile = profile,
            locationName = locationName,
            connectionStatus = fullScreenConnectionStatus,
            onBack = {
                selectedProfileIndex = null
            },
            onAccept = {
                android.util.Log.d("LocationDetailScreen", "Accept button pressed for ${profile.name}")
                
                // Perform swipe API call with LIKE action
                coroutineScope.launch {
                    val (success, isMatch) = performSwipe(
                        userId = currentUserId,
                        targetUserId = profile.userId,
                        locationId = realLocationId.ifEmpty { "mock-location-id" },
                        action = "LIKE"
                    )
                    if (success) {
                        // Update profile state to ACCEPTED
                        allProfiles = allProfiles.toMutableList().also { list ->
                            val profileIndex = list.indexOfFirst { it.id == profile.id }
                            if (profileIndex != -1) {
                                list[profileIndex] = list[profileIndex].copy(state = ProfileState.ACCEPTED)
                            }
                        }
                        
                        // Show match notification if it's a match
                        if (isMatch) {
                            matchNotification = "It's a Match with ${profile.name}! 🎉"
                            android.util.Log.d("LocationDetailScreen", "Match detected with ${profile.name}")
                        }
                    }
                }
            },
            onWaitingClick = {
                // Open selfie screen when clicking the waiting pill
                selfieCaptureProfileName = profile.name
                showSelfieCapture = true
            },
            onReject = {
                android.util.Log.d("LocationDetailScreen", "Reject button pressed for ${profiles[index].name}")

                val profile = profiles[index]

                // Perform swipe API call
                coroutineScope.launch {
                    val (success, _) = performSwipe(
                        userId = currentUserId,
                        targetUserId = profile.userId,
                        locationId = realLocationId.ifEmpty { "mock-location-id" },
                        action = "PASS"
                    )
                    if (success) {
                        // Update profile state to REJECTED
                        allProfiles = allProfiles.toMutableList().also { list ->
                            val profileIndex = list.indexOfFirst { it.id == profile.id }
                            if (profileIndex != -1) {
                                list[profileIndex] = list[profileIndex].copy(state = ProfileState.REJECTED)
                            }
                        }

                        // Move to next profile and keep showing in full screen
                        if (index < profiles.size - 1) {
                            selectedProfileIndex = index + 1
                            pagerState.animateScrollToPage(index + 1)
                        } else {
                            // If this was the last profile, close full screen view
                            selectedProfileIndex = null
                            // Ensure pager stays on the last profile
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            }
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Location Name
                Text(
                    text = locationName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Buzz Ins Count and Buzz Out Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Buzz Ins Pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentBuzzInCount.toString(),
                                color = Color(0xFFCA8A04),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Buzz Ins",
                                color = Color(0xFFCA8A04),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Buzz Out Button
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFACC15),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Buzz Out",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Content Section with Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Description
            Text(
                text = "About this location",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = description,
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // People Section Header
            Text(
                text = "People who buzzed in",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Horizontal Pager for Profile Cards or Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profiles.isEmpty()) {
                    // Show empty state when all profiles have been swiped
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "That's everyone!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You've seen all profiles at this location",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .fillMaxHeight(),
                    pageSpacing = 8.dp
                ) { page ->
                    val profile = profiles[page]
                    // Convert LocationProfile to DetailProfile for ProfileCardWithActions
                    val profileForCard = DetailProfile(
                        id = profile.id,
                        name = profile.name,
                        age = profile.age,
                        distance = "At this location",
                        bio = profile.bio,
                        occupation = profile.occupation,
                        interests = profile.interests,
                        photos = profile.photos
                    )

                    // Determine connection status based on profileConnectionStatuses map
                    val connectionStatus: ConnectionStatus? = profileConnectionStatuses[profile.id] ?: when (profile.state) {
                        ProfileState.ACCEPTED -> ConnectionStatus.WAITING
                        ProfileState.REJECTED -> ConnectionStatus.PASSED
                        ProfileState.ACTIVE -> null
                    }

                    ProfileCardWithActions(
                        profile = profileForCard,
                        locationName = locationName,
                        connectionStatus = connectionStatus,
                        onClick = {
                            selectedProfileIndex = page
                        },
                        onAccept = {
                            android.util.Log.d("LocationDetailScreen", "Accept button pressed for ${profile.name}")
                            
                            // Perform swipe API call with LIKE action
                            coroutineScope.launch {
                                val (success, isMatch) = performSwipe(
                                    userId = currentUserId,
                                    targetUserId = profile.userId,
                                    locationId = realLocationId.ifEmpty { "mock-location-id" },
                                    action = "LIKE"
                                )
                                if (success) {
                                    // Update profile state to ACCEPTED
                                    allProfiles = allProfiles.toMutableList().also { list ->
                                        val profileIndex = list.indexOfFirst { it.id == profile.id }
                                        if (profileIndex != -1) {
                                            list[profileIndex] = list[profileIndex].copy(state = ProfileState.ACCEPTED)
                                        }
                                    }
                                    
                                    // Show match notification if it's a match
                                    if (isMatch) {
                                        matchNotification = "It's a Match with ${profile.name}! 🎉"
                                        android.util.Log.d("LocationDetailScreen", "Match detected with ${profile.name}")
                                    }
                                }
                            }
                        },
                        onWaitingClick = {
                            // Open selfie screen when clicking the waiting pill
                            selfieCaptureProfileName = profile.name
                            showSelfieCapture = true
                        },
                        onReject = {
                            android.util.Log.d("LocationDetailScreen", "Reject button pressed for ${profile.name}")

                            // Perform swipe API call and update state
                            coroutineScope.launch {
                                val (success, _) = performSwipe(
                                    userId = currentUserId,
                                    targetUserId = profile.userId,
                                    locationId = realLocationId.ifEmpty { "mock-location-id" },
                                    action = "PASS"
                                )
                                if (success) {
                                    // Update profile state to REJECTED
                                    allProfiles = allProfiles.toMutableList().also { list ->
                                        val profileIndex = list.indexOfFirst { it.id == profile.id }
                                        if (profileIndex != -1) {
                                            list[profileIndex] = list[profileIndex].copy(state = ProfileState.REJECTED)
                                        }
                                    }

                                    // Auto-advance to next profile
                                    if (page < profiles.size - 1) {
                                        pagerState.animateScrollToPage(page + 1)
                                    }
                                }
                            }
                        }
                    )
                }
                }
            }

            // Page Indicator (only show if there are profiles)
            if (profiles.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(profiles.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(horizontal = 3.dp)
                                .background(
                                    color = if (index == pagerState.currentPage) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }

    // Match notification dialog
    matchNotification?.let { message ->
        AlertDialog(
            onDismissRequest = { matchNotification = null },
            title = {
                Text(
                    text = "It's a Match!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFACC15)
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { matchNotification = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFACC15)
                    )
                ) {
                    Text("Awesome!")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun ProfileCard(
    profile: LocationProfile,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Profile Image
                AsyncImage(
                    model = profile.imageUrl,
                    contentDescription = profile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Greyed out overlay when accepted/rejected
                if (profile.state != ProfileState.ACTIVE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                    )
                }

                // Profile Info and Buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "${profile.name}, ${profile.age}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = profile.bio,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Show state label when not active
                    if (profile.state == ProfileState.ACCEPTED) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF22C55E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ACCEPTED",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (profile.state == ProfileState.REJECTED) {
                        // Passed Status Pill
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Passed",
                                    color = Color(0xFFC71212),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Action Buttons (only show when ACTIVE)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reject Button
                            FloatingActionButton(
                                onClick = onReject,
                                modifier = Modifier.size(64.dp),
                                containerColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Reject",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(48.dp))

                            // Accept Button
                            FloatingActionButton(
                                onClick = onAccept,
                                modifier = Modifier.size(64.dp),
                                containerColor = Color(0xFFFACC15),
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Accept",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun ProfileCardWithActions(
    profile: DetailProfile,
    locationName: String,
    connectionStatus: ConnectionStatus?,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onWaitingClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .clickable { onClick() }
            ) {
                AsyncImage(
                    model = profile.photos.firstOrNull(),
                    contentDescription = profile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Profile Info Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = profile.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.age.toString(),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 20.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Actions Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (connectionStatus) {
                        ConnectionStatus.WAITING -> {
                            // Waiting Status Pill
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWaitingClick() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Waiting for response",
                                        color = Color(0xFFB45309),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        ConnectionStatus.PASSED -> {
                            // Passed Status Pill
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEE2E2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Passed",
                                        color = Color(0xFFC71212),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        ConnectionStatus.CONNECTED -> {
                            // Connected Status Pill
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Connected",
                                        color = Color(0xFF15803D),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            // Follow up Button
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFACC15)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Follow up",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Follow up",
                                    color = Color.White
                                )
                            }
                        }
                        null -> {
                            // Ignore Button
                            Button(
                                onClick = onReject,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Ignore",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ignore",
                                    color = Color(0xFF64748B)
                                )
                            }
                            
                            // Connect Button
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFACC15)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Connect",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Connect",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenProfileView(
    profile: LocationProfile,
    locationName: String,
    connectionStatus: ConnectionStatus? = null,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onWaitingClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Photo
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Profile Image
                AsyncImage(
                    model = profile.photos[0],
                    contentDescription = profile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .align(Alignment.TopStart)
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Profile Info and Buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Name and Age
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = profile.name,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profile.age.toString(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    // Location
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationName,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                    
                    // Bio
                    Text(
                        text = profile.bio,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    
                    // Interests
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.interests.take(4).forEach { interest ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = interest,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Show status/buttons based on connection status
                    when (connectionStatus) {
                        ConnectionStatus.WAITING -> {
                            // Waiting Status Pill (clickable to send selfie)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWaitingClick() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send Selfie",
                                        color = Color(0xFFB45309),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        ConnectionStatus.CONNECTED -> {
                            // Connected Status (not clickable) + Follow Up Button
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Connected pill (not clickable)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFD1FAE5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Connected",
                                            color = Color(0xFF047857),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Follow Up Button (clickable)
                                Button(
                                    onClick = { /* TODO: Add follow up action */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFACC15)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Follow Up",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        ConnectionStatus.PASSED -> {
                            // Passed Status Pill
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEE2E2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Passed",
                                        color = Color(0xFFC71212),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        null -> {
                        // Action Buttons (only show when ACTIVE)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ignore Button
                                Button(
                                    onClick = onReject,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF1F5F9)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Ignore",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ignore",
                                        color = Color(0xFF64748B)
                                    )
                                }

                                // Connect Button
                                Button(
                                    onClick = onAccept,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFACC15)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Connect",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Connect",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}
