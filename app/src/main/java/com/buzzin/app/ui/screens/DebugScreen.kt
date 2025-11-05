package com.buzzin.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.buzzin.app.data.DeviceIdManager

/**
 * Debug screen to view and test Device ID system.
 * Shows all generated IDs and device metadata.
 * 
 * This screen is useful for:
 * - Development and testing
 * - Troubleshooting device tracking issues
 * - Verifying ID persistence
 * - Copying IDs for backend testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val deviceIdManager = DeviceIdManager.getInstance(context)
    val metadata = deviceIdManager.getDeviceMetadata(context)
    val clipboardManager = LocalClipboardManager.current
    
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device ID Debug", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(snackbarMessage)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Device Identification System",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "These IDs are used to track user context across multiple devices and save state to the backend database.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Application ID
            IdCard(
                title = "Application ID",
                subtitle = "Primary identifier for this device installation",
                value = metadata.applicationId,
                description = "• Persists across app restarts\n• Used as primary key in backend database\n• Unique per app installation",
                onCopy = {
                    clipboardManager.setText(AnnotatedString(metadata.applicationId))
                    snackbarMessage = "Application ID copied"
                    showSnackbar = true
                }
            )

            // User ID
            IdCard(
                title = "User ID",
                subtitle = "Links device to authenticated user",
                value = metadata.userId ?: "Not set (user not logged in)",
                description = "• Set after user authentication\n• Links multiple devices to same user\n• Cleared on logout",
                onCopy = {
                    metadata.userId?.let {
                        clipboardManager.setText(AnnotatedString(it))
                        snackbarMessage = "User ID copied"
                        showSnackbar = true
                    }
                }
            )

            // Session ID
            IdCard(
                title = "Session ID",
                subtitle = "Unique identifier for this app session",
                value = metadata.sessionId,
                description = "• Changes on every app launch\n• Used for analytics and session tracking\n• Helps identify user behavior patterns",
                onCopy = {
                    clipboardManager.setText(AnnotatedString(metadata.sessionId))
                    snackbarMessage = "Session ID copied"
                    showSnackbar = true
                }
            )

            // Android Device ID
            IdCard(
                title = "Android Device ID",
                subtitle = "Hardware-based device identifier",
                value = metadata.androidDeviceId,
                description = "• Unique per physical device\n• Changes on factory reset\n• Used for fraud detection",
                onCopy = {
                    clipboardManager.setText(AnnotatedString(metadata.androidDeviceId))
                    snackbarMessage = "Android Device ID copied"
                    showSnackbar = true
                }
            )

            // Composite ID
            IdCard(
                title = "Composite ID",
                subtitle = "Combined application and user identifier",
                value = deviceIdManager.getCompositeId(),
                description = "• Combines Application ID and User ID\n• Useful for user-device tracking\n• Format: app_<appId>_user_<userId>",
                onCopy = {
                    clipboardManager.setText(AnnotatedString(deviceIdManager.getCompositeId()))
                    snackbarMessage = "Composite ID copied"
                    showSnackbar = true
                }
            )

            // App Version
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "App Version",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = metadata.appVersion,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Testing Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            deviceIdManager.userId = "test_user_${System.currentTimeMillis()}"
                            snackbarMessage = "Test User ID set"
                            showSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Test User ID")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            deviceIdManager.clearUserId()
                            snackbarMessage = "User ID cleared"
                            showSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear User ID")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "⚠️ Warning: Clearing all IDs will reset the application identifier. Use only for testing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            deviceIdManager.clearAllIds()
                            snackbarMessage = "All IDs cleared - Restart app to see new IDs"
                            showSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear All IDs (Dangerous)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun IdCard(
    title: String,
    subtitle: String,
    value: String,
    description: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $title"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

