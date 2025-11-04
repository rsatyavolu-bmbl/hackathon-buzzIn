# 🐳 Local Docker Backend - Quick Start

The BuzzIn app now has a **local GraphQL backend** running in Docker - no AWS required!

---

## ✅ What's Running

**Backend Server Status:**
- **URL**: http://localhost:4000/graphql
- **GraphiQL UI**: http://localhost:4000/graphql (interactive API explorer)
- **Health Check**: http://localhost:4000/health
- **Status**: ✅ RUNNING

**Seeded Test Data:**
- 4 users (Alex, Sam, Jordan, Taylor)
- 5 locations (Blue Bottle Coffee, Ritual Coffee, Dolores Park, The Alembic, Tartine Bakery)

---

## 🚀 Quick Start

### 1. Start Backend Server

```bash
cd backend
npm start
```

Or run in background:
```bash
cd backend
npm start > /tmp/backend.log 2>&1 &
```

### 2. Verify Server is Running

```bash
curl http://localhost:4000/health
# Should return: {"status":"ok","users":4,"locations":5,"checkIns":0,"matches":0}
```

### 3. Build and Install App

```bash
# Build APK
./gradlew assembleDebug

# Install to emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.buzzin.app/.MainActivity
```

---

## 📱 App Configuration

The app is configured to connect to local backend:

**File**: `app/src/main/res/raw/amplifyconfiguration.json`
```json
{
  "endpoint": "http://10.0.2.2:4000/graphql"
}
```

**Note**: `10.0.2.2` is the special IP that Android emulators use to access the host machine's `localhost`.

---

## 🧪 Test the API

### GraphiQL Web Interface

Open in browser: http://localhost:4000/graphql

Try these queries:

```graphql
# Get all locations
{
  listLocations {
    id
    name
    address
    type
  }
}

# Get nearby locations (San Francisco)
{
  listNearbyLocations(latitude: 37.7749, longitude: -122.4194, radiusKm: 2.0) {
    id
    name
    address
  }
}

# Create a user
mutation {
  createUser(input: {
    name: "Test User"
    age: 25
    photoRes: "avatar_test"
    bio: "Testing BuzzIn"
  }) {
    id
    name
  }
}
```

### Using curl

```bash
# List locations
curl -X POST http://localhost:4000/graphql \
  -H 'Content-Type: application/json' \
  -d '{"query": "{ listLocations { id name address type } }"}'

# Create user
curl -X POST http://localhost:4000/graphql \
  -H 'Content-Type: application/json' \
  -d '{"query": "mutation { createUser(input: { name: \"Alice\", age: 28, photoRes: \"avatar_5\" }) { id name } }"}'
```

---

## 🔧 Available API Operations

### Queries
- `listLocations` - Get all locations
- `listNearbyLocations(latitude, longitude, radiusKm)` - Find locations within radius
- `getActiveUsersAtLocation(locationId)` - Get users currently checked in
- `getUser(id)` - Get user by ID
- `listMatches(userId)` - Get user's matches
- `listMessages(matchId)` - Get messages for a match

### Mutations
- `createUser(input)` - Create new user
- `createCheckIn(input)` - Check in to location
- `updateCheckIn(input)` - Check out (set isActive: false)
- `performSwipe(userId, targetUserId, locationId, action)` - Like/Ignore user
- `createMessage(input)` - Send message to match

---

## 📊 Monitor Backend Logs

### View logs in real-time:
```bash
# If running in foreground, logs print to console

# If running in background:
tail -f /tmp/backend.log
```

### Check what's stored:
```bash
curl http://localhost:4000/health
```

Returns:
```json
{
  "status": "ok",
  "users": 4,
  "locations": 5,
  "checkIns": 0,
  "matches": 0
}
```

---

## 🐋 Using Docker Compose

For full stack deployment (backend + APK server):

```bash
# Start everything
docker-compose up --build

# Backend: http://localhost:4000/graphql
# APK Server: http://localhost:8080/debug/app-debug.apk
```

Stop with:
```bash
docker-compose down
```

---

## 🎯 Complete Test Flow

### 1. Create Test Users
```graphql
mutation {
  user1: createUser(input: {name: "Alice", age: 28, photoRes: "avatar_1"}) { id }
  user2: createUser(input: {name: "Bob", age: 30, photoRes: "avatar_2"}) { id }
}
```

### 2. Check In to Location
```graphql
mutation {
  checkIn(input: {
    userId: "USER_ID_1"
    locationId: "loc1"
    checkInTime: "2025-11-04T20:00:00Z"
    isActive: true
    latitude: 37.7825
    longitude: -122.4078
  }) {
    id
  }
}
```

### 3. Get Active Users
```graphql
{
  getActiveUsersAtLocation(locationId: "loc1") {
    id
    name
    age
  }
}
```

### 4. Perform Swipe (Try to Match)
```graphql
mutation {
  performSwipe(
    userId: "USER_ID_1"
    targetUserId: "USER_ID_2"
    locationId: "loc1"
    action: LIKE
  ) {
    matched
    match {
      id
      matchTime
      user1 { name }
      user2 { name }
    }
  }
}
```

---

## 🐛 Troubleshooting

### Server not starting
```bash
# Check if port 4000 is in use
lsof -i :4000

# Kill existing process
kill -9 <PID>

# Restart
cd backend && npm start
```

### App can't connect
**Problem**: "Network error" or "Connection refused"

**Solution**:
- Emulator: Use `http://10.0.2.2:4000/graphql`
- Physical device: Use your Mac's IP (find with `ifconfig | grep "inet "`)

### View backend logs
```bash
cd backend
npm start
# Logs print to console
```

---

## 📚 Backend Code Structure

```
backend/
├── package.json         # Dependencies
├── server.js           # GraphQL server with in-memory storage
├── Dockerfile          # Docker container config
└── node_modules/       # Installed packages
```

**Features:**
- ✅ Express + GraphQL server
- ✅ In-memory data storage (resets on restart)
- ✅ Full API matching AWS Amplify schema
- ✅ Seeded test data (users + locations)
- ✅ Match detection logic
- ✅ CORS enabled for web testing
- ✅ GraphiQL UI for testing

---

## 🎉 Summary

**You now have a fully functional local backend!**

✅ **Backend**: Running at http://localhost:4000/graphql
✅ **Test Data**: 4 users, 5 locations in San Francisco
✅ **API**: Full GraphQL API with all operations
✅ **Monitoring**: Health check + logs
✅ **Docker**: Optional containerized deployment

**No AWS account needed!** Everything runs locally on your Mac.

To monitor requests in real-time:
```bash
# In one terminal: run backend with logs
cd backend && npm start

# In another terminal: watch app logs
adb logcat | grep -E "BuzzInApiClient|AmplifyRepository"
```

🚀 Ready to test!
