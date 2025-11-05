package com.buzzin.app.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.amplifyframework.api.graphql.SimpleGraphQLRequest
import com.amplifyframework.core.Amplify
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.amplifyframework.api.graphql.GraphQLRequest

data class UserProfile(
    val id: String,
    val name: String,
    val age: Int,
    val bio: String?,
    val photoRes: String?,
    val interests: List<String>?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load user profile on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = "26d085ae-5f8b-4de4-bea7-cb1459f5c0b4"
                val profile = fetchUserProfile(userId)
                userProfile = profile
                isLoading = false
                Log.d("ProfileScreen", "Loaded profile: ${profile.name}")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error loading profile", e)
                errorMessage = "Failed to load profile"
                isLoading = false
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC629))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else if (userProfile?.photoRes != null) {
                Image(
                    painter = rememberAsyncImagePainter(userProfile!!.photoRes),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC629))
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            } else if (userProfile != null) {
                Text(
                    text = userProfile!!.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${userProfile!!.age} years old",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                if (userProfile!!.bio != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = userProfile!!.bio!!,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (userProfile!!.interests != null && userProfile!!.interests!!.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Interests",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userProfile!!.interests!!.joinToString(", "),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile options
            ProfileOption(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                onClick = { }
            )

            ProfileOption(
                icon = Icons.Default.Star,
                title = "Premium",
                onClick = { }
            )

            ProfileOption(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = { }
            )
        }
    }
}

@Composable
fun ProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color(0xFFFFC629),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// Function to fetch user profile from backend
suspend fun fetchUserProfile(userId: String): UserProfile = suspendCancellableCoroutine { continuation ->
    val query = """
        query GetUser {
            getUser(id: "$userId") {
                id
                name
                age
                bio
                photoRes
                interests
            }
        }
    """.trimIndent()

    val request = SimpleGraphQLRequest<String>(
        query,
        String::class.java,
        GraphQLRequest.VariablesSerializer { "{}" }
    )

    Amplify.API.query(
        request,
        { response ->
            try {
                val responseData = response.data
                Log.d("ProfileScreen", "Response: $responseData")

                // Parse the response
                val gson = Gson()
                val jsonObject = gson.fromJson(responseData as String, com.google.gson.JsonObject::class.java)
                val userData = jsonObject.getAsJsonObject("data").getAsJsonObject("getUser")

                val profile = UserProfile(
                    id = userData.get("id").asString,
                    name = userData.get("name").asString,
                    age = userData.get("age").asInt,
                    bio = if (userData.has("bio") && !userData.get("bio").isJsonNull)
                        userData.get("bio").asString else null,
                    photoRes = if (userData.has("photoRes") && !userData.get("photoRes").isJsonNull)
                        userData.get("photoRes").asString else null,
                    interests = if (userData.has("interests") && !userData.get("interests").isJsonNull) {
                        val interestsArray = userData.getAsJsonArray("interests")
                        interestsArray.map { element -> element.asString }
                    } else null
                )
                continuation.resume(profile)
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error parsing profile", e)
                continuation.resumeWithException(e)
            }
        },
        { error ->
            Log.e("ProfileScreen", "Query error", error)
            continuation.resumeWithException(Exception(error.message ?: "Unknown error"))
        }
    )
}
