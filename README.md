# hackathon-buzzIn
Buzz-In to locations (a la Foursquare) and match with other Buzzed In members, hyper local and real time matchmaking (Bumble + BFF). Think concerts, bars, coffee shops, etc

## Architecture
- **Frontend**: Android application (Kotlin/Java)
- **Backend**: REST API services hosted on AWS Cloud
- **Deployment**: AWS (Lambda, API Gateway, DynamoDB, etc.)
- **Device Tracking**: Unique Application IDs for multi-device context management

## Setup Instructions for Developers

### Prerequisites
- Android Studio or Android SDK command-line tools
- Java JDK 17
- Google Maps API Key

### Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hackathon-buzzIn
   ```

2. **Set up local.properties**
   ```bash
   cp local.properties.template local.properties
   ```
   
   Edit `local.properties` and update:
   - `sdk.dir` - Path to your Android SDK
   - `MAPS_API_KEY` - Your personal Google Maps API key

3. **Get Your Google Maps API Key**
   
   a. Go to [Google Cloud Console](https://console.cloud.google.com/)
   
   b. Create a new project or select existing one
   
   c. Enable **Maps SDK for Android**
   
   d. Create API credentials:
      - Go to **APIs & Services** → **Credentials**
      - Click **+ CREATE CREDENTIALS** → **API key**
      - Copy the API key
   
   e. Restrict the API key (Recommended):
      - Click on your API key
      - Under **Application restrictions**, select **Android apps**
      - Add your package name: `com.buzzin.app`
      - Add your SHA-1 certificate fingerprint:
        ```bash
        # For debug builds:
        keytool -list -v -keystore ~/.android/debug.keystore \
                -alias androiddebugkey -storepass android -keypass android | grep SHA1
        ```
      - Under **API restrictions**, select **Maps SDK for Android**
      - Click **SAVE**
   
   f. Add the API key to your `local.properties`:
      ```properties
      MAPS_API_KEY=YOUR_API_KEY_HERE
      ```

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

### Important Notes

- **Never commit `local.properties`** - This file contains your personal API keys and SDK paths
- Each developer should use their own Google Maps API key
- The `local.properties.template` file is provided as a reference

### Project Structure

```
hackathon-buzzIn/
├── app/                    # Android application code
│   ├── src/main/
│   │   ├── java/          # Kotlin/Java source files
│   │   │   ├── data/      # Data layer (repositories, API, device management)
│   │   │   ├── ui/        # UI layer (screens, navigation, themes)
│   │   │   └── ...
│   │   └── res/           # Resources (layouts, strings, etc.)
│   └── build.gradle       # App-level build configuration
├── service/               # Backend service code (AWS)
├── local.properties       # Local config (gitignored)
├── DEVICE_ID_SYSTEM.md   # Device ID & context tracking documentation
└── README.md             # This file
```

### Key Features

#### Device ID & Context Management
The app automatically generates and manages unique identifiers for each device installation:
- **Application ID**: Persistent UUID for tracking device context across app sessions
- **User ID**: Links device to authenticated user account
- **Session ID**: Tracks individual app sessions for analytics
- **Multi-device support**: Users can buzz-in from multiple devices

See [DEVICE_ID_SYSTEM.md](DEVICE_ID_SYSTEM.md) for complete documentation on:
- How Application IDs work
- Backend integration examples
- API usage patterns
- Multi-device scenarios

#### Google Maps Integration
- Interactive map with scrollable, zoomable interface
- Location-based markers for coffee shops and restaurants
- Real-time "buzz-in" status for nearby locations
- Filter by place type (coffee, restaurant, etc.)

### Troubleshooting

**Maps not loading?**
- Verify your API key is correct in `local.properties`
- Check that you've added your SHA-1 fingerprint to Google Cloud Console
- Ensure Maps SDK for Android is enabled in your Google Cloud project

**Build errors?**
- Make sure `sdk.dir` in `local.properties` points to your Android SDK
- Verify Java JDK 17 is installed and `JAVA_HOME` is set correctly

**Device ID issues?**
- Check logs: `adb logcat | grep BuzzInApplication`
- View stored IDs: See [DEVICE_ID_SYSTEM.md](DEVICE_ID_SYSTEM.md) troubleshooting section
