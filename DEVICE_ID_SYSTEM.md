# Device ID & Application Context System

## Overview

The BuzzIn app uses a comprehensive device identification system to track user context across multiple Android devices and save state to the backend database.

## Architecture

### Components

1. **DeviceIdManager** (`app/src/main/java/com/buzzin/app/data/DeviceIdManager.kt`)
   - Singleton class that manages all device and application identifiers
   - Persists IDs using SharedPreferences
   - Provides multiple types of identifiers for different use cases

2. **BuzzInApplication** (`app/src/main/java/com/buzzin/app/BuzzInApplication.kt`)
   - Initializes DeviceIdManager on app startup
   - Logs device metadata for debugging
   - Provides global access to DeviceIdManager

3. **BuzzInApiService** (`app/src/main/java/com/buzzin/app/data/api/BuzzInApiService.kt`)
   - Retrofit API interface
   - All endpoints include device/application ID headers

4. **BuzzInRepository** (`app/src/main/java/com/buzzin/app/data/repository/BuzzInRepository.kt`)
   - Automatically injects device IDs into API calls
   - Handles device registration and context synchronization

## Identifier Types

### 1. Application ID (Primary Identifier)
- **Type**: UUID (e.g., `"a1b2c3d4-e5f6-7890-abcd-ef1234567890"`)
- **Persistence**: Survives app restarts, cleared on app data clear
- **Use Case**: Primary key for backend database
- **Access**: `deviceIdManager.applicationId`

```kotlin
val appId = deviceIdManager.applicationId
// Example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

### 2. Android Device ID
- **Type**: Android Secure ID
- **Persistence**: Unique per device, changes on factory reset
- **Use Case**: Device-specific tracking, fraud detection
- **Access**: `deviceIdManager.getAndroidDeviceId(context)`

```kotlin
val deviceId = deviceIdManager.getAndroidDeviceId(context)
// Example: "9774d56d682e549c"
```

### 3. Session ID
- **Type**: UUID (regenerated each app launch)
- **Persistence**: Changes every app launch
- **Use Case**: Track individual app sessions, analytics
- **Access**: `deviceIdManager.sessionId`

```kotlin
val sessionId = deviceIdManager.sessionId
// Example: "b2c3d4e5-f6a7-8901-bcde-f12345678901"
```

### 4. User ID
- **Type**: String (set after authentication)
- **Persistence**: Survives app restarts, cleared on logout
- **Use Case**: Link device to authenticated user account
- **Access**: `deviceIdManager.userId`

```kotlin
// After login
deviceIdManager.userId = "user_12345"

// On logout
deviceIdManager.clearUserId()
```

### 5. Composite ID
- **Type**: String combining application and user IDs
- **Format**: `"app_<applicationId>_user_<userId>"`
- **Use Case**: Unique identifier for user-device combination
- **Access**: `deviceIdManager.getCompositeId()`

```kotlin
val compositeId = deviceIdManager.getCompositeId()
// Example: "app_a1b2c3d4-e5f6-7890-abcd-ef1234567890_user_user_12345"
```

## Usage Examples

### 1. Initialize in Application Class

```kotlin
class BuzzInApplication : Application() {
    lateinit var deviceIdManager: DeviceIdManager
        private set

    override fun onCreate() {
        super.onCreate()
        deviceIdManager = DeviceIdManager.getInstance(this)
        
        // Log device metadata
        val metadata = deviceIdManager.getDeviceMetadata(this)
        Log.d(TAG, "Application ID: ${metadata.applicationId}")
        Log.d(TAG, "Session ID: ${metadata.sessionId}")
    }
}
```

### 2. Access from Activity/Fragment

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var deviceIdManager: DeviceIdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get DeviceIdManager instance
        deviceIdManager = DeviceIdManager.getInstance(this)
        
        // Get application ID
        val appId = deviceIdManager.applicationId
        Log.d(TAG, "App ID: $appId")
    }
}
```

### 3. Use in API Calls

```kotlin
// Automatic injection via Repository
class BuzzInRepository(
    private val apiService: BuzzInApiService,
    private val deviceIdManager: DeviceIdManager,
    private val context: Context
) {
    suspend fun saveContext(
        lastLocation: LocationData?,
        preferences: Map<String, Any>
    ): Result<ContextResponse> {
        val response = apiService.saveContext(
            applicationId = deviceIdManager.applicationId,
            userId = deviceIdManager.userId,
            context = UserContextRequest(lastLocation, preferences)
        )
        return handleResponse(response)
    }
}
```

### 4. Register Device with Backend

```kotlin
// Call this on first app launch or after login
suspend fun registerDevice() {
    val repository = BuzzInRepository(apiService, deviceIdManager, context)
    val result = repository.registerDevice()
    
    result.onSuccess { response ->
        Log.d(TAG, "Device registered: ${response.deviceId}")
    }.onFailure { error ->
        Log.e(TAG, "Registration failed", error)
    }
}
```

### 5. Handle User Authentication

```kotlin
// After successful login
fun onLoginSuccess(userId: String) {
    deviceIdManager.userId = userId
    
    // Register device with user ID
    lifecycleScope.launch {
        repository.registerDevice()
    }
}

// On logout
fun onLogout() {
    deviceIdManager.clearUserId()
}
```

### 6. Save User Context

```kotlin
suspend fun saveUserContext() {
    val location = LocationData(
        latitude = 37.7749,
        longitude = -122.4194
    )
    
    val preferences = mapOf(
        "theme" to "dark",
        "notifications" to true,
        "radius" to 1000
    )
    
    val result = repository.saveContext(location, preferences)
    result.onSuccess {
        Log.d(TAG, "Context saved successfully")
    }
}
```

## Backend Integration

### Database Schema Example (DynamoDB)

```json
{
  "TableName": "BuzzInDevices",
  "KeySchema": [
    {
      "AttributeName": "applicationId",
      "KeyType": "HASH"
    }
  ],
  "AttributeDefinitions": [
    {
      "AttributeName": "applicationId",
      "AttributeType": "S"
    },
    {
      "AttributeName": "userId",
      "AttributeType": "S"
    }
  ],
  "GlobalSecondaryIndexes": [
    {
      "IndexName": "UserIdIndex",
      "KeySchema": [
        {
          "AttributeName": "userId",
          "KeyType": "HASH"
        }
      ]
    }
  ]
}
```

### API Headers

All API requests should include these headers:

```
X-Application-Id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
X-User-Id: user_12345 (optional, if authenticated)
X-Session-Id: b2c3d4e5-f6a7-8901-bcde-f12345678901
```

### Example Backend Endpoint (AWS Lambda)

```javascript
// Lambda function to save device context
exports.handler = async (event) => {
    const applicationId = event.headers['X-Application-Id'];
    const userId = event.headers['X-User-Id'] || 'anonymous';
    const body = JSON.parse(event.body);
    
    // Save to DynamoDB
    await dynamodb.put({
        TableName: 'BuzzInDevices',
        Item: {
            applicationId: applicationId,
            userId: userId,
            lastLocation: body.lastLocation,
            preferences: body.preferences,
            lastActiveTimestamp: Date.now(),
            updatedAt: new Date().toISOString()
        }
    }).promise();
    
    return {
        statusCode: 200,
        body: JSON.stringify({
            success: true,
            message: 'Context saved successfully'
        })
    };
};
```

## Multi-Device Support

### Scenario: User with Multiple Devices

1. **Device A** (Phone):
   - Application ID: `app-id-001`
   - User ID: `user_12345`
   - Context: Last location, preferences

2. **Device B** (Tablet):
   - Application ID: `app-id-002`
   - User ID: `user_12345`
   - Context: Last location, preferences

### Backend Query

```kotlin
// Get all devices for a user
GET /api/v1/devices?userId=user_12345

// Response
{
  "devices": [
    {
      "applicationId": "app-id-001",
      "deviceType": "phone",
      "lastActive": "2025-11-04T10:30:00Z"
    },
    {
      "applicationId": "app-id-002",
      "deviceType": "tablet",
      "lastActive": "2025-11-04T09:15:00Z"
    }
  ]
}
```

## Security Considerations

1. **Application ID is NOT a secret** - It's used for tracking, not authentication
2. **Never use Application ID for authorization** - Always verify user authentication
3. **Store sensitive data server-side** - Use Application ID as a key
4. **Implement rate limiting** - Prevent abuse using Application ID
5. **Log suspicious activity** - Track unusual patterns per Application ID

## Testing

### Unit Tests

```kotlin
@Test
fun testApplicationIdPersistence() {
    val manager = DeviceIdManager.getInstance(context)
    val id1 = manager.applicationId
    
    // Simulate app restart
    val manager2 = DeviceIdManager.getInstance(context)
    val id2 = manager2.applicationId
    
    assertEquals(id1, id2) // Should be the same
}

@Test
fun testSessionIdChanges() {
    val manager = DeviceIdManager.getInstance(context)
    val session1 = manager.sessionId
    val session2 = manager.sessionId
    
    assertNotEquals(session1, session2) // Should be different
}
```

### Manual Testing

```kotlin
// In your app, add a debug screen to view IDs
@Composable
fun DebugScreen() {
    val context = LocalContext.current
    val manager = DeviceIdManager.getInstance(context)
    val metadata = manager.getDeviceMetadata(context)
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Application ID: ${metadata.applicationId}")
        Text("User ID: ${metadata.userId ?: "Not set"}")
        Text("Android Device ID: ${metadata.androidDeviceId}")
        Text("Session ID: ${metadata.sessionId}")
        Text("App Version: ${metadata.appVersion}")
        Text("Composite ID: ${manager.getCompositeId()}")
    }
}
```

## Troubleshooting

### Issue: Application ID changes on every launch
**Solution**: Check SharedPreferences permissions and ensure context is application context

### Issue: User ID not persisting after logout
**Solution**: This is expected behavior. User ID should be cleared on logout.

### Issue: Backend not receiving Application ID
**Solution**: Verify Retrofit interceptor is adding headers correctly

## Best Practices

1. ✅ **Initialize early** - Set up DeviceIdManager in Application.onCreate()
2. ✅ **Use Application Context** - Always pass application context, not activity context
3. ✅ **Register on first launch** - Call registerDevice() on first app open
4. ✅ **Update on login** - Set userId after successful authentication
5. ✅ **Clear on logout** - Call clearUserId() when user logs out
6. ✅ **Include in all API calls** - Use headers for tracking
7. ✅ **Log for debugging** - Log Application ID in development builds
8. ❌ **Don't use for security** - Never trust client-provided IDs for authorization

## Migration Guide

If you have existing users, you can migrate them:

```kotlin
// Check if this is a first-time user
val isFirstTime = prefs.getBoolean("is_first_time", true)
if (isFirstTime) {
    // Generate new Application ID
    val appId = deviceIdManager.applicationId
    
    // Optionally migrate old data
    migrateOldUserData(appId)
    
    prefs.edit().putBoolean("is_first_time", false).apply()
}
```

## Support

For questions or issues with the Device ID system:
- Check logs: `adb logcat | grep BuzzInApplication`
- View stored IDs: `adb shell run-as com.buzzin.app cat /data/data/com.buzzin.app/shared_prefs/buzzin_device_prefs.xml`
- Clear IDs for testing: `deviceIdManager.clearAllIds()`

