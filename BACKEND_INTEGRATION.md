# 🔌 Backend Integration - Complete Guide

The BuzzIn Android app is now integrated with AWS Amplify backend!

---

## ✅ What's Been Integrated

### 1. **Amplify Initialization**
- `BuzzInApplication.kt` - Amplify configured on app start
- API plugin enabled for GraphQL operations

### 2. **API Client Layer**
- `BuzzInApiClient.kt` - Direct Amplify API calls
- Handles all GraphQL mutations, queries, subscriptions
- Singleton pattern for efficiency

### 3. **Data Models**
- `BackendModels.kt` - All backend data structures:
  - User, Location, CheckIn, Swipe, Match, Message
  - Enums: LocationType, SwipeAction

### 4. **Repository Layer**
- `AmplifyRepository.kt` - Clean API for ViewModels
- Wraps API client with Result types
- Easy error handling

---

## 📁 File Structure

```
app/src/main/java/com/buzzin/app/
├── BuzzInApplication.kt              # ✅ Amplify initialization
├── data/
│   ├── api/
│   │   └── BuzzInApiClient.kt        # ✅ GraphQL API calls
│   ├── model/
│   │   └── BackendModels.kt          # ✅ Data models
│   └── repository/
│       └── AmplifyRepository.kt      # ✅ Repository layer
└── ui/
    └── screens/                       # 🔄 To be updated with backend
```

---

## 🚀 Setup Steps

### Step 1: Deploy Backend

```bash
cd /path/to/hackathon-buzzIn
amplify push
```

This will create your backend and generate `amplifyconfiguration.json`.

### Step 2: Copy Configuration

```bash
# Copy generated config to app
cp amplify/aws-exports.js app/src/main/res/raw/amplifyconfiguration.json
```

### Step 3: Build and Run

```bash
./gradlew assembleDebug
# Or use Android Studio
```

---

## 💡 How to Use in Your Code

### Example 1: Check In to Location

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buzzin.app.data.repository.AmplifyRepository
import kotlinx.coroutines.launch

class CheckInViewModel : ViewModel() {
    private val repository = AmplifyRepository.getInstance()

    fun checkIn(userId: String, locationId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.checkIn(userId, locationId, lat, lng)
                .onSuccess { checkIn ->
                    // Success! User is checked in
                    println("Checked in: ${checkIn.id}")
                }
                .onFailure { error ->
                    // Handle error
                    println("Error: ${error.message}")
                }
        }
    }
}
```

### Example 2: Get Nearby Locations

```kotlin
class MapViewModel : ViewModel() {
    private val repository = AmplifyRepository.getInstance()

    fun loadNearbyLocations(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.getNearbyLocations(lat, lng, radiusKm = 1.0)
                .onSuccess { locations ->
                    // Display locations on map
                    locations.forEach { location ->
                        println("${location.name} at ${location.address}")
                    }
                }
                .onFailure { error ->
                    println("Error loading locations: ${error.message}")
                }
        }
    }
}
```

### Example 3: Swipe on User

```kotlin
class SwipeViewModel : ViewModel() {
    private val repository = AmplifyRepository.getInstance()

    fun swipeRight(userId: String, targetUserId: String, locationId: String) {
        viewModelScope.launch {
            repository.performSwipe(
                userId,
                targetUserId,
                locationId,
                SwipeAction.LIKE
            )
                .onSuccess { result ->
                    if (result.matched) {
                        // It's a match!
                        showMatchDialog(result.match)
                    }
                }
                .onFailure { error ->
                    println("Swipe failed: ${error.message}")
                }
        }
    }

    fun swipeLeft(userId: String, targetUserId: String, locationId: String) {
        viewModelScope.launch {
            repository.performSwipe(
                userId,
                targetUserId,
                locationId,
                SwipeAction.IGNORE
            )
                .onSuccess {
                    // User ignored
                }
        }
    }
}
```

### Example 4: Get Matches

```kotlin
class MatchesViewModel : ViewModel() {
    private val repository = AmplifyRepository.getInstance()

    fun loadMatches(userId: String) {
        viewModelScope.launch {
            repository.getUserMatches(userId)
                .onSuccess { matches ->
                    // Display matches
                    matches.forEach { match ->
                        println("Matched with ${match.user2?.name} at ${match.location?.name}")
                    }
                }
        }
    }
}
```

### Example 5: Send Message

```kotlin
class MessageViewModel : ViewModel() {
    private val repository = AmplifyRepository.getInstance()

    fun sendMessage(matchId: String, senderId: String, text: String) {
        viewModelScope.launch {
            repository.sendMessage(matchId, senderId, text)
                .onSuccess { message ->
                    println("Message sent: ${message.id}")
                }
                .onFailure { error ->
                    println("Failed to send: ${error.message}")
                }
        }
    }
}
```

---

## 🔧 Available Operations

### User Operations
```kotlin
repository.createUser(name, age, photoRes, bio)
```

### Location Operations
```kotlin
repository.getNearbyLocations(lat, lng, radiusKm)
```

### Check-in Operations
```kotlin
repository.checkIn(userId, locationId, lat, lng)
repository.checkOut(checkInId)
```

### Swipe Operations
```kotlin
repository.getActiveUsersAtLocation(locationId)
repository.performSwipe(userId, targetUserId, locationId, action)
```

### Match Operations
```kotlin
repository.getUserMatches(userId)
```

### Message Operations
```kotlin
repository.sendMessage(matchId, senderId, text, photoRes)
```

---

## 🎯 Next Steps for UI Integration

### 1. Update HomeScreen (Swipe UI)
```kotlin
// In HomeScreen.kt
val viewModel: SwipeViewModel = viewModel()

LaunchedEffect(locationId) {
    viewModel.loadUsers(locationId)
}

// On swipe
SwipeCard(
    onSwipeRight = { viewModel.swipeRight(currentUserId, targetUserId, locationId) },
    onSwipeLeft = { viewModel.swipeLeft(currentUserId, targetUserId, locationId) }
)
```

### 2. Update MapScreen (Locations)
```kotlin
// In MapScreen.kt
val viewModel: MapViewModel = viewModel()

LaunchedEffect(Unit) {
    viewModel.loadNearbyLocations(userLat, userLng)
}
```

### 3. Add Check-in Flow
```kotlin
// In LocationDetailScreen.kt
Button(onClick = {
    viewModel.checkIn(userId, locationId, lat, lng)
}) {
    Text("Buzz In")
}
```

### 4. Add Matches Screen
```kotlin
// In MatchesScreen.kt
val viewModel: MatchesViewModel = viewModel()

LaunchedEffect(Unit) {
    viewModel.loadMatches(currentUserId)
}
```

---

## 🐛 Troubleshooting

### "Amplify has not been configured"
**Solution:** Make sure `amplifyconfiguration.json` is in `app/src/main/res/raw/`

```bash
# Check file exists
ls app/src/main/res/raw/amplifyconfiguration.json
```

### "API not found" or "Unauthorized"
**Solution:** Deploy backend first

```bash
amplify push
cp amplify/aws-exports.js app/src/main/res/raw/amplifyconfiguration.json
```

### Compilation Errors
**Solution:** Sync Gradle

```bash
./gradlew clean build
```

### No Data Returned
**Solution:** Check backend has data

```bash
# Open AppSync console
amplify api console

# Run test query
query {
  listLocations {
    items {
      id
      name
    }
  }
}
```

---

## 📊 Testing the Integration

### Test Flow:

1. **Deploy Backend:**
   ```bash
   amplify push
   ```

2. **Seed Test Data:**
   Use mutations from `amplify/seed-data.graphql`

3. **Run App:**
   ```bash
   ./gradlew installDebug
   ```

4. **Check Logs:**
   ```bash
   adb logcat | grep BuzzInApplication
   # Should see: "Initialized Amplify successfully"
   ```

5. **Test an Operation:**
   ```kotlin
   // In any ViewModel
   viewModelScope.launch {
       val result = repository.getNearbyLocations(37.7749, -122.4194)
       Log.d("Test", "Locations: $result")
   }
   ```

---

## 📚 Resources

- **Backend API Docs:** `amplify/API_REFERENCE.md`
- **Backend Setup:** `amplify/QUICKSTART.md`
- **Sample Data:** `amplify/seed-data.graphql`
- **Amplify Android Docs:** https://docs.amplify.aws/android/

---

## ✅ Integration Checklist

- [x] Amplify initialized in Application class
- [x] API client created
- [x] Data models defined
- [x] Repository layer implemented
- [x] Sample config provided
- [ ] ViewModels created (next step)
- [ ] UI screens updated (next step)
- [ ] End-to-end testing

---

## 🎊 Summary

**Backend is ready to use!**

All you need to do:
1. Deploy backend: `amplify push`
2. Copy config: `amplifyconfiguration.json` to `res/raw/`
3. Use repository in your ViewModels
4. Handle success/error in UI

The foundation is built - now connect your UI screens to the backend! 🚀
