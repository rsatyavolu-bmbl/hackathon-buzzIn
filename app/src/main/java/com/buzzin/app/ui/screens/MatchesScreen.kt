package com.buzzin.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Match(
    val matchId: String,
    val userId: String,
    val userName: String,
    val userAge: Int,
    val userBio: String,
    val userPhoto: String,
    val locationName: String,
    val matchTime: String,
    val messageSent: Boolean
)

@Composable
fun MatchesScreen(currentUserId: String = "test-user-fixed-id-12345") {
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Load matches when screen appears
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            matches = loadMatches(currentUserId)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your Matches",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFACC15))
            }
        } else if (matches.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matches yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start swiping to find your matches!",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            // Matches list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches) { match ->
                    MatchCard(match = match)
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: Match) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to chat */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile photo
            AsyncImage(
                model = match.userPhoto,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = match.userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${match.userAge}",
                        fontSize = 18.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = match.userBio,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Matched at ${match.locationName}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Chat indicator
            if (!match.messageSent) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFACC15), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Say Hi!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Function to load matches from database
suspend fun loadMatches(currentUserId: String): List<Match> {
    return withContext(Dispatchers.IO) {
        try {
            // Query to get all matches for current user
            val query = """
                query ListMatches {
                  listMatches(filter: {or: [{user1Id: {eq: "$currentUserId"}}, {user2Id: {eq: "$currentUserId"}}]}) {
                    items {
                      id
                      user1Id
                      user2Id
                      locationId
                      matchTime
                      messageSent
                    }
                  }
                }
            """.trimIndent()

            Log.d("MatchesScreen", "Querying matches for user: $currentUserId")

            var responseData: String? = null
            var error: Exception? = null

            val request = SimpleGraphQLRequest<String>(
                query,
                emptyMap<String, Any>(),
                String::class.java,
                GraphQLRequest.VariablesSerializer { "{}" }
            )

            Amplify.API.query(
                request,
                { response ->
                    responseData = response.data as String
                    Log.d("MatchesScreen", "Matches response: $responseData")
                },
                { err ->
                    Log.e("MatchesScreen", "Failed to load matches", err)
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

            // Parse response
            responseData?.let { response ->
                try {
                    val responseJson = JSONObject(response)
                    val listMatchesResult = responseJson.getJSONObject("listMatches")
                    val itemsArray = listMatchesResult.getJSONArray("items")

                    val matchesList = mutableListOf<Match>()

                    for (i in 0 until itemsArray.length()) {
                        val matchItem = itemsArray.getJSONObject(i)
                        val matchId = matchItem.getString("id")
                        val user1Id = matchItem.getString("user1Id")
                        val user2Id = matchItem.getString("user2Id")
                        val locationId = matchItem.getString("locationId")
                        val matchTime = matchItem.getString("matchTime")
                        val messageSent = matchItem.optBoolean("messageSent", false)

                        // Determine which user is the matched user (not current user)
                        val matchedUserId = if (user1Id == currentUserId) user2Id else user1Id

                        // Get user details (for now using mock data)
                        val userDetails = getMockUserDetails(matchedUserId)
                        val locationName = "Hyde Park" // TODO: Fetch from Location table

                        matchesList.add(
                            Match(
                                matchId = matchId,
                                userId = matchedUserId,
                                userName = userDetails.first,
                                userAge = userDetails.second,
                                userBio = userDetails.third,
                                userPhoto = userDetails.fourth,
                                locationName = locationName,
                                matchTime = matchTime,
                                messageSent = messageSent
                            )
                        )
                    }

                    Log.d("MatchesScreen", "Found ${matchesList.size} matches")
                    return@withContext matchesList
                } catch (e: Exception) {
                    Log.e("MatchesScreen", "Error parsing matches", e)
                }
            }

            emptyList()
        } catch (e: Exception) {
            Log.e("MatchesScreen", "Failed to load matches", e)
            emptyList()
        }
    }
}

// Mock function to get user details
// TODO: Replace with actual User table query
fun getMockUserDetails(userId: String): Quadruple<String, Int, String, String> {
    return when (userId) {
        "mock-user-1" -> Quadruple("Alex", 28, "Software engineer who loves hiking", "https://images.unsplash.com/photo-1650057861788-b6b8606b77ef?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHBlcnNvbnxlbnwxfHx8fDE3NjIyMjU1NTR8MA&ixlib=rb-4.1.0&q=80&w=1080")
        "mock-user-2" -> Quadruple("Jordan", 25, "Artist and coffee enthusiast", "https://images.unsplash.com/photo-1557053910-d9eadeed1c58?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3b21hbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc2MjI1NDI1Mnww&ixlib=rb-4.1.0&q=80&w=1080")
        "mock-user-3" -> Quadruple("Sam", 30, "Entrepreneur and fitness junkie", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtYW4lMjBwb3J0cmFpdHxlbnwxfHx8fDE3NjIyODc2MjV8MA&ixlib=rb-4.1.0&q=80&w=1080")
        "mock-user-4" -> Quadruple("Taylor", 27, "Teacher who loves traveling", "https://images.unsplash.com/photo-1560250097-0b93528c311a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBoZWFkc2hvdHxlbnwxfHx8fDE3NjIyMDM1MDl8MA&ixlib=rb-4.1.0&q=80&w=1080")
        else -> Quadruple("Unknown", 25, "BuzzIn user", "https://images.unsplash.com/photo-1650057861788-b6b8606b77ef?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHBlcnNvbnxlbnwxfHx8fDE3NjIyMjU1NTR8MA&ixlib=rb-4.1.0&q=80&w=1080")
    }
}

// Helper class for quadruple
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
