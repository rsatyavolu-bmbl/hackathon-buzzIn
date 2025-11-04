# Buzz In - Android Kotlin/Jetpack Compose

This is the Kotlin/Jetpack Compose implementation of the Bumble-style dating app UI.

**Package ID:** `com.buzzin.app`

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/buzzin/app/
│       │   ├── MainActivity.kt                    # Main activity with navigation
│       │   └── ui/
│       │       ├── screens/
│       │       │   ├── HomeScreen.kt             # Discover screen
│       │       │   ├── MapScreen.kt              # Buzz In map view
│       │       │   ├── ProfileScreen.kt          # User profile
│       │       │   └── SettingsScreen.kt         # Placeholder screen
│       │       └── theme/
│       │           └── BuzzInTheme.kt            # Material 3 theme
│       └── AndroidManifest.xml
└── build.gradle.kts
```

## Features

### 1. HomeScreen (Discover)
- Full-screen dating profile cards with swipeable photos
- Photo indicators at the top
- Profile info overlay (name, age, location) above action buttons
- Action buttons at bottom:
  - ❌ Red X icon for reject
  - ❤️ Yellow heart icon for like (Bumble yellow #FFC629)
- Info button in top-right corner
- Coil image loading from Unsplash

### 2. MapScreen (Buzz In)
- Stylized map view with grid pattern
- Street overlays
- Yellow pin markers with white icons:
  - ☕ Coffee shop icon
  - 🍽️ Restaurant icon
- Triangular pin pointers
- Blue pulsing dot for current location

### 3. Bottom Navigation
- 5 tabs: Profile, Discover (default), Buzz In, Liked You, Chats
- Material 3 NavigationBar
- Bumble yellow accent color

### 4. Theme
- Primary color: Bumble Yellow (#FFC629)
- Material 3 design system
- Light and dark mode support

## Setup Instructions

### Prerequisites
- Android Studio (latest version recommended)
- Kotlin 1.9+
- Android SDK 26+ (minimum)
- Android SDK 34 (target)

### Installation

1. **Create new Android Studio project** or use existing one

2. **Copy the files** from `app/src/main/java/com/buzzin/app/` to your Android project

3. **The package is already set to `com.buzzin.app`** - no changes needed unless you want a different package

4. **Sync Gradle** files to download dependencies

5. **Add required resources** (if not present):
   - Add app icon resources in `res/mipmap/`
   - Add theme in `res/values/themes.xml`:
     ```xml
     <resources>
         <style name="Theme.BuzzIn" parent="android:Theme.Material.Light.NoActionBar" />
     </resources>
     ```
   - Add string resources in `res/values/strings.xml`:
     ```xml
     <resources>
         <string name="app_name">Buzz In</string>
     </resources>
     ```

6. **Build and run** the app on emulator or device

## Dependencies

Key libraries used:

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")

// Image loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")
```

## Key Technologies

- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Latest Material Design components
- **Coil** - Efficient image loading with caching
- **Kotlin Coroutines** - For async operations
- **State Management** - Compose's `remember` and `mutableStateOf`

## React to Compose Migration

| React/TypeScript | Kotlin/Compose |
|-----------------|----------------|
| `useState` | `remember { mutableStateOf() }` |
| `div` with `className` | `Box/Column/Row` with `Modifier` |
| Tailwind CSS | Modifier chains |
| `onClick` handler | `onClick` lambda |
| `ImageWithFallback` | `rememberAsyncImagePainter` |
| Lucide icons | Material Icons |
| CSS colors | `Color(0xFFHEXVALUE)` |

## Color Palette

```kotlin
Bumble Yellow: Color(0xFFFFC629)
Background: Color(0xFFF8FAFC)
Surface: Color.White
Text: Color(0xFF1E293B)
Location Blue: Color(0xFF3B82F6)
Reject Red: Color.Red
Gray: Color.Gray
```

## Next Steps

### Enhancements to Add:
1. **Swipe gestures** for profile cards
   ```kotlin
   Modifier.pointerInput(Unit) {
       detectDragGestures { ... }
   }
   ```

2. **Photo navigation** with tap zones
   ```kotlin
   Modifier.clickable { }
   ```

3. **Animations** for transitions
   ```kotlin
   AnimatedVisibility, animateFloatAsState
   ```

4. **ViewModels** for state management
   ```kotlin
   class HomeViewModel : ViewModel() { ... }
   ```

5. **Repository pattern** for data
   ```kotlin
   interface ProfileRepository { ... }
   ```

6. **Navigation Component** for screens
   ```kotlin
   NavHost(navController, startDestination = "home") { ... }
   ```

7. **Info panel** slide-up animation
   ```kotlin
   ModalBottomSheet { ... }
   ```

8. **Real Google Maps** integration
   ```kotlin
   implementation("com.google.maps.android:maps-compose:...")
   ```

## File Breakdown

### MainActivity.kt
- Entry point of the app
- Sets up bottom navigation with 5 tabs
- Uses `Scaffold` with `NavigationBar`
- Switches screens based on selected tab

### HomeScreen.kt
- Profile card UI with full-screen photos
- Photo indicators and navigation
- Profile info overlay
- Action buttons (reject/like)
- State management for current profile/photo

### MapScreen.kt
- Custom Canvas drawing for map background
- Grid and street patterns
- Place markers with yellow pins
- Current location indicator
- Composable `PlaceMarker` component

### ProfileScreen.kt
- User profile view
- Profile image placeholder
- Settings options cards
- Navigation to settings

### BuzzInTheme.kt
- Material 3 color scheme
- Light and dark theme support
- Bumble yellow as primary color
- Typography setup

## Troubleshooting

**Build errors:**
- Ensure Android SDK 34 is installed
- Sync Gradle files
- Check package names match

**Images not loading:**
- Verify internet permission in AndroidManifest.xml
- Check network connectivity
- Coil requires internet for remote images

**Icons missing:**
- Ensure `material-icons-extended` dependency is added
- Import correct icon from `androidx.compose.material.icons.filled`

**Theme not applied:**
- Check `themes.xml` exists in `res/values/`
- Verify `android:theme` in AndroidManifest.xml

## License

This is a UI prototype for educational purposes. Not affiliated with Bumble.

---

**Ready to use!** Just copy to your Android project and start building.
