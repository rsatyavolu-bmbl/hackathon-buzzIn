package com.buzzin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfieCaptureScreen(
    profileName: String,
    locationType: String, // "COFFEE" or "RESTAURANT"
    onBack: () -> Unit,
    onSelfieCaptured: () -> Unit
) {
    var selfieCaptured by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var selectedLocationText by remember { mutableStateOf<String?>(null) }
    
    // Pre-defined location texts based on location type
    val locationOptions = if (locationType == "COFFEE") {
        listOf(
            "☕ By the counter",
            "🪟 Near the window",
            "📚 At the corner table"
        )
    } else {
        listOf(
            "🍽️ At the bar",
            "🚪 Near the entrance",
            "🎵 By the stage area"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            if (!selfieCaptured) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Preview",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Position yourself in the frame",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 14.sp
                    )
                }
            } else {
                // Captured selfie preview placeholder
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Captured Photo",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Selfie",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Looking great!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Send to $profileName",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                // Spacer to balance the layout
                Spacer(modifier = Modifier.size(40.dp))
            }
        }

        // Bottom Controls
        if (!selfieCaptured) {
            // Capture Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Take a selfie to share with $profileName",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
                
                FloatingActionButton(
                    onClick = { selfieCaptured = true },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color(0xFFFACC15),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        } else {
            // Location Selection and Send/Retake Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // Location Selection Title
                Text(
                    text = "Where are you?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Location Options
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locationOptions.forEach { option ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLocationText = option
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedLocationText == option) 
                                Color(0xFFFACC15) 
                            else 
                                Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (selectedLocationText == option) 2.dp else 1.dp,
                                color = if (selectedLocationText == option) 
                                    Color(0xFFFACC15) 
                                else 
                                    Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = option,
                                color = if (selectedLocationText == option) 
                                    Color.White 
                                else 
                                    Color.White.copy(alpha = 0.9f),
                                fontSize = 15.sp,
                                fontWeight = if (selectedLocationText == option) 
                                    FontWeight.Bold 
                                else 
                                    FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                        }
                    }
                }
                
                // Send and Retake Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Retake Button
                    OutlinedButton(
                        onClick = { 
                            selfieCaptured = false
                            selectedLocationText = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Retake",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Send Button
                    Button(
                        onClick = {
                            if (selectedLocationText != null) {
                                showSuccessMessage = true
                            }
                        },
                        enabled = selectedLocationText != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFACC15),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Success Message
        if (showSuccessMessage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Selfie Sent!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Your selfie has been sent to $profileName",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        selectedLocationText?.let { location ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                Text(
                                    text = location,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCA8A04),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        } ?: Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                onSelfieCaptured()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFACC15),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Done",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

