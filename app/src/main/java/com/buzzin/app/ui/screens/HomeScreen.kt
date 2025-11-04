package com.buzzin.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

data class Profile(
    val id: Int,
    val name: String,
    val age: Int,
    val location: String,
    val distance: String,
    val bio: String,
    val occupation: String,
    val interests: List<String>,
    val photos: List<String>
)

val sampleProfiles = listOf(
    Profile(
        id = 1,
        name = "Sarah",
        age = 26,
        location = "Brooklyn, NY",
        distance = "2 miles away",
        bio = "Coffee enthusiast ☕ | Travel addict ✈️ | Dog mom to the cutest golden retriever 🐕",
        occupation = "Marketing Manager",
        interests = listOf("Travel", "Photography", "Yoga", "Coffee"),
        photos = listOf(
            "https://images.unsplash.com/photo-1690444963408-9573a17a8058?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHdvbWFuJTIwc21pbGluZ3xlbnwxfHx8fDE3NjIwMzAzMDh8MA&ixlib=rb-4.1.0&q=80&w=1080",
            "https://images.unsplash.com/photo-1638280219567-be72272eb375?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3b21hbiUyMGhhcHB5JTIwY2FzdWFsfGVufDF8fHx8MTc2MjEzNTM1OHww&ixlib=rb-4.1.0&q=80&w=1080",
            "https://images.unsplash.com/photo-1587723909168-8330a66d8ae7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwZXJzb24lMjB0cmF2ZWwlMjBhZHZlbnR1cmV8ZW58MXx8fHwxNzYyMTM1MzU5fDA&ixlib=rb-4.1.0&q=80&w=1080"
        )
    ),
    Profile(
        id = 2,
        name = "Michael",
        age = 29,
        location = "Manhattan, NY",
        distance = "3 miles away",
        bio = "Software engineer by day, aspiring chef by night 👨‍💻🍳 | Gym rat | Always down for brunch",
        occupation = "Software Engineer",
        interests = listOf("Cooking", "Fitness", "Hiking", "Music"),
        photos = listOf(
            "https://images.unsplash.com/photo-1680557345345-6f9ef109d252?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMG1hbiUyMG91dGRvb3J8ZW58MXx8fHwxNzYyMDczNjQ2fDA&ixlib=rb-4.1.0&q=80&w=1080",
            "https://images.unsplash.com/photo-1690444963408-9573a17a8058?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHdvbWFuJTIwc21pbGluZ3xlbnwxfHx8fDE3NjIwMzAzMDh8MA&ixlib=rb-4.1.0&q=80&w=1080"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var currentProfileIndex by remember { mutableStateOf(0) }
    var currentPhotoIndex by remember { mutableStateOf(0) }
    var showInfo by remember { mutableStateOf(false) }

    val currentProfile = sampleProfiles[currentProfileIndex]
    val totalPhotos = currentProfile.photos.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFC629),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = "bumble",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
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

        // Profile Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable {
                        // Handle photo navigation on tap
                    }
            ) {
                // Photo
                Image(
                    painter = rememberAsyncImagePainter(currentProfile.photos[currentPhotoIndex]),
                    contentDescription = currentProfile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Photo indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentProfile.photos.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    color = if (index == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                // Info button
                FloatingActionButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp),
                    containerColor = Color.White.copy(alpha = 0.9f),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color.Black
                    )
                }

                // Profile info - Above action buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 120.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentProfile.name,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${currentProfile.age}",
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = currentProfile.location,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Reject button
                    FloatingActionButton(
                        onClick = {
                            if (currentProfileIndex < sampleProfiles.size - 1) {
                                currentProfileIndex++
                                currentPhotoIndex = 0
                                showInfo = false
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Reject",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Like button
                    FloatingActionButton(
                        onClick = {
                            if (currentProfileIndex < sampleProfiles.size - 1) {
                                currentProfileIndex++
                                currentPhotoIndex = 0
                                showInfo = false
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color(0xFFFFC629),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
