package com.buzzin.app.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

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

data class LocationProfile(
    val id: Int,
    val name: String,
    val age: Int,
    val bio: String,
    val imageUrl: String,
    val occupation: String,
    val interests: List<String>,
    val photos: List<String>
)

// Mock profile images
val profileImages = listOf(
    "https://images.unsplash.com/photo-1650057861788-b6b8606b77ef?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwb3J0cmFpdCUyMHBlcnNvbnxlbnwxfHx8fDE3NjIyMjU1NTR8MA&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1557053910-d9eadeed1c58?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx3b21hbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc2MjI1NDI1Mnww&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtYW4lMjBwb3J0cmFpdHxlbnwxfHx8fDE3NjIyODc2MjV8MA&ixlib=rb-4.1.0&q=80&w=1080",
    "https://images.unsplash.com/photo-1560250097-0b93528c311a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBoZWFkc2hvdHxlbnwxfHx8fDE3NjIyMDM1MDl8MA&ixlib=rb-4.1.0&q=80&w=1080"
)

val names = listOf("Alex", "Jordan", "Casey", "Morgan", "Riley", "Taylor", "Jamie", "Avery", "Quinn", "Drew", "Blake", "Sage")

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LocationDetailScreen(
    locationId: Int,
    locationName: String,
    locationType: LocationType,
    buzzInCount: Int,
    onBack: () -> Unit
) {
    val icon: ImageVector = if (locationType == LocationType.COFFEE) {
        Icons.Default.Coffee
    } else {
        Icons.Default.Restaurant
    }
    
    val typeLabel = if (locationType == LocationType.COFFEE) "Coffee Shop" else "Restaurant"
    
    val description = if (locationType == LocationType.COFFEE) {
        "A cozy coffee shop perfect for casual meetups and first dates. Enjoy artisan coffee and a relaxed atmosphere."
    } else {
        "A wonderful dining experience awaits. Great ambiance for dates and getting to know someone over delicious food."
    }

    val bioCoffee = "Coffee enthusiast looking to meet new people ☕"
    val bioRestaurant = "Foodie who loves trying new restaurants 🍽️"
    
    var selectedProfile by remember { mutableStateOf<LocationProfile?>(null) }
    
    // Generate mock profiles with multiple photos
    val profiles = List(buzzInCount) { i ->
        val mainPhoto = profileImages[i % profileImages.size]
        val otherPhotos = profileImages.filterIndexed { idx, _ -> idx != (i % profileImages.size) }
        
        LocationProfile(
            id = i + 1,
            name = names[i % names.size],
            age = 24 + (i % 10),
            bio = if (locationType == LocationType.COFFEE) bioCoffee else bioRestaurant,
            imageUrl = mainPhoto,
            occupation = if (locationType == LocationType.COFFEE) "Designer" else "Marketing Manager",
            interests = if (locationType == LocationType.COFFEE)
                listOf("Coffee", "Art", "Music", "Reading")
            else
                listOf("Foodie", "Travel", "Cooking", "Wine"),
            photos = listOf(mainPhoto) + otherPhotos.take(2)
        )
    }

    val pagerState = rememberPagerState(pageCount = { profiles.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Show full-screen profile view if selected
    selectedProfile?.let { profile ->
        FullScreenProfileView(
            profile = profile,
            locationName = locationName,
            onBack = { 
                selectedProfile = null
            },
            onAccept = {
                selectedProfile = null
                coroutineScope.launch {
                    if (pagerState.currentPage < profiles.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            onReject = {
                selectedProfile = null
                coroutineScope.launch {
                    if (pagerState.currentPage < profiles.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = locationName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
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
                            text = buzzInCount.toString(),
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
            }
        }
        
        // Content Section with Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Buzzed In Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFACC15))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "You're buzzed in!",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Others can see your profile here",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFCA8A04)
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

            // Description
            Text(
                text = "About this location",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
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

            // Horizontal Pager for Profile Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .fillMaxHeight(),
                    pageSpacing = 8.dp
                ) { page ->
                    val profile = profiles[page]
                    ProfileCard(
                        profile = profile,
                        onClick = {
                            selectedProfile = profile
                        },
                        onAccept = {
                            coroutineScope.launch {
                                if (page < profiles.size - 1) {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        },
                        onReject = {
                            coroutineScope.launch {
                                if (page < profiles.size - 1) {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        }
                    )
                }
            }

            // Page Indicator
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
                    
                    // Action Buttons
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

@Composable
fun FullScreenProfileView(
    profile: LocationProfile,
    locationName: String,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
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
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reject Button
                        FloatingActionButton(
                            onClick = onReject,
                            modifier = Modifier.size(56.dp),
                            containerColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Reject",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(48.dp))
                        
                        // Accept Button
                        FloatingActionButton(
                            onClick = onAccept,
                            modifier = Modifier.size(56.dp),
                            containerColor = Color(0xFFFACC15),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Accept",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
