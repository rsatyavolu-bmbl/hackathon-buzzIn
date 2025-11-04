package com.buzzin.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SocialPlace(
    val id: Int,
    val name: String,
    val type: PlaceType,
    val position: Pair<Float, Float> // x, y as percentage (0-1)
)

enum class PlaceType {
    COFFEE,
    RESTAURANT
}

val socialPlaces = listOf(
    SocialPlace(1, "The Coffee House", PlaceType.COFFEE, Pair(0.35f, 0.25f)),
    SocialPlace(2, "Bistro 42", PlaceType.RESTAURANT, Pair(0.60f, 0.35f)),
    SocialPlace(3, "Brew & Bean", PlaceType.COFFEE, Pair(0.25f, 0.50f)),
    SocialPlace(4, "La Tavola", PlaceType.RESTAURANT, Pair(0.70f, 0.45f)),
    SocialPlace(5, "Grind Coffee", PlaceType.COFFEE, Pair(0.45f, 0.65f)),
    SocialPlace(6, "Sunset Grill", PlaceType.RESTAURANT, Pair(0.65f, 0.70f)),
    SocialPlace(7, "Morning Brew", PlaceType.COFFEE, Pair(0.50f, 0.30f)),
    SocialPlace(8, "The Garden", PlaceType.RESTAURANT, Pair(0.55f, 0.55f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        TopAppBar(
            title = { Text("Buzz In", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { /* Settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Map View
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Map background with grid and streets
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                // Background gradient
                drawRect(
                    color = Color(0xFFE2E8F0),
                    size = size
                )

                // Draw grid pattern
                val gridSpacing = 50f
                for (i in 0..(width / gridSpacing).toInt()) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(i * gridSpacing, 0f),
                        end = Offset(i * gridSpacing, height),
                        strokeWidth = 1f
                    )
                }
                for (i in 0..(height / gridSpacing).toInt()) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(0f, i * gridSpacing),
                        end = Offset(width, i * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // Draw main streets
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.3f),
                    start = Offset(0f, height * 0.3f),
                    end = Offset(width, height * 0.3f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.3f),
                    start = Offset(0f, height * 0.6f),
                    end = Offset(width, height * 0.6f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.3f),
                    start = Offset(width * 0.3f, 0f),
                    end = Offset(width * 0.3f, height),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = 0.3f),
                    start = Offset(width * 0.7f, 0f),
                    end = Offset(width * 0.7f, height),
                    strokeWidth = 2f
                )
            }

            // Current location marker (center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
            ) {
                // Pulsing background circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
                // Center dot
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            color = Color(0xFF3B82F6),
                            shape = CircleShape
                        )
                )
            }

            // Social place markers
            socialPlaces.forEach { place ->
                PlaceMarker(
                    place = place,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (place.position.first * 320).dp,
                            y = (place.position.second * 600).dp
                        )
                )
            }
        }
    }
}

@Composable
fun PlaceMarker(place: SocialPlace, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Pin marker circle
        Surface(
            modifier = Modifier
                .size(40.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = Color(0xFFFFC629),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (place.type == PlaceType.COFFEE) Icons.Default.Coffee else Icons.Default.Restaurant,
                    contentDescription = place.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Pin triangle pointer
        Canvas(
            modifier = Modifier
                .size(width = 16.dp, height = 12.dp)
                .offset(y = (-2).dp)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFFFC629),
                style = Fill
            )
        }
    }
}
