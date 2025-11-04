# BuzzIn API Reference

Complete API documentation for frontend integration.

---

## Table of Contents
1. [Data Models](#data-models)
2. [Mutations](#mutations)
3. [Queries](#queries)
4. [Custom Operations](#custom-operations)
5. [Subscriptions](#subscriptions)
6. [Error Handling](#error-handling)

---

## Data Models

### User
```graphql
type User {
  id: ID!
  name: String!
  age: Int!
  bio: String
  photoRes: String!          # Drawable resource name (e.g., "avatar_1")
  interests: [String]         # Array of interests
  checkIns: [CheckIn]        # User's check-ins
  swipesGiven: [Swipe]       # Swipes made by user
  swipesReceived: [Swipe]    # Swipes received by user
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

### Location
```graphql
type Location {
  id: ID!
  name: String!
  address: String!
  latitude: Float!
  longitude: Float!
  type: LocationType!        # COFFEE, RESTAURANT, BAR, PARK, GYM, OTHER
  checkIns: [CheckIn]
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

### CheckIn
```graphql
type CheckIn {
  id: ID!
  userId: ID!
  locationId: ID!
  user: User
  location: Location
  checkInTime: AWSDateTime!
  checkOutTime: AWSDateTime   # Null if still checked in
  isActive: Boolean!          # True if checkOutTime is null
  latitude: Float!            # User's GPS at check-in
  longitude: Float!
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

### Swipe
```graphql
type Swipe {
  id: ID!
  userId: ID!                 # Person swiping
  targetUserId: ID!           # Person being swiped on
  user: User
  targetUser: User
  locationId: ID!
  location: Location
  action: SwipeAction!        # LIKE or IGNORE
  timestamp: AWSDateTime!
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

### Match
```graphql
type Match {
  id: ID!
  user1Id: ID!
  user2Id: ID!
  user1: User
  user2: User
  locationId: ID!
  location: Location
  matchTime: AWSDateTime!
  messageSent: Boolean!       # True if message has been sent
  messages: [Message]
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

### Message
```graphql
type Message {
  id: ID!
  matchId: ID!
  match: Match
  senderId: ID!
  sender: User
  text: String!
  photoRes: String            # Optional drawable resource name
  timestamp: AWSDateTime!
  isRead: Boolean!
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

---

## Mutations

### 1. Create User
```graphql
mutation CreateUser {
  createUser(input: {
    name: "John Doe"
    age: 28
    photoRes: "avatar_1"
    bio: "Coffee enthusiast & tech lover"
    interests: ["coffee", "technology", "travel"]
  }) {
    id
    name
    age
    photoRes
  }
}
```

**Response:**
```json
{
  "data": {
    "createUser": {
      "id": "uuid-123",
      "name": "John Doe",
      "age": 28,
      "photoRes": "avatar_1"
    }
  }
}
```

---

### 2. Create Location
```graphql
mutation CreateLocation {
  createLocation(input: {
    name: "Starbucks Downtown"
    address: "123 Main Street, San Francisco, CA"
    latitude: 37.7749
    longitude: -122.4194
    type: COFFEE
  }) {
    id
    name
    type
  }
}
```

---

### 3. Check In to Location
```graphql
mutation CheckIn {
  createCheckIn(input: {
    userId: "user-uuid-123"
    locationId: "location-uuid-456"
    checkInTime: "2025-11-04T10:30:00Z"
    isActive: true
    latitude: 37.7749
    longitude: -122.4194
  }) {
    id
    isActive
    checkInTime
    user {
      name
    }
    location {
      name
    }
  }
}
```

**Important:** Set `isActive: true` when checking in.

---

### 4. Check Out (Buzz Out)
```graphql
mutation CheckOut {
  updateCheckIn(input: {
    id: "checkin-uuid-789"
    checkOutTime: "2025-11-04T12:30:00Z"
    isActive: false
  }) {
    id
    checkOutTime
    isActive
  }
}
```

**Important:** Set `isActive: false` and provide `checkOutTime`.

---

### 5. Send Message (One-time)
```graphql
mutation SendMessage {
  createMessage(input: {
    matchId: "match-uuid-101"
    senderId: "user-uuid-123"
    text: "Hey! Nice to match with you at Starbucks!"
    photoRes: "avatar_2"
    timestamp: "2025-11-04T13:00:00Z"
    isRead: false
  }) {
    id
    text
    sender {
      name
    }
  }
}
```

**Then update the match:**
```graphql
mutation UpdateMatch {
  updateMatch(input: {
    id: "match-uuid-101"
    messageSent: true
  }) {
    id
    messageSent
  }
}
```

---

## Queries

### 1. Get User by ID
```graphql
query GetUser {
  getUser(id: "user-uuid-123") {
    id
    name
    age
    bio
    photoRes
    interests
  }
}
```

---

### 2. List All Locations
```graphql
query ListLocations {
  listLocations {
    items {
      id
      name
      address
      latitude
      longitude
      type
    }
  }
}
```

---

### 3. Get User's Check-in History
```graphql
query GetUserCheckIns {
  listCheckIns(filter: {
    userId: { eq: "user-uuid-123" }
  }) {
    items {
      id
      checkInTime
      checkOutTime
      isActive
      location {
        name
        address
      }
    }
  }
}
```

---

### 4. Get User's Current Active Check-in
```graphql
query GetActiveCheckIn {
  listCheckIns(filter: {
    userId: { eq: "user-uuid-123" }
    isActive: { eq: true }
  }) {
    items {
      id
      location {
        id
        name
      }
      checkInTime
    }
  }
}
```

---

### 5. Get User's Matches
```graphql
query GetUserMatches {
  listMatches(filter: {
    or: [
      { user1Id: { eq: "user-uuid-123" } }
      { user2Id: { eq: "user-uuid-123" } }
    ]
  }) {
    items {
      id
      matchTime
      messageSent
      user1 {
        id
        name
        photoRes
      }
      user2 {
        id
        name
        photoRes
      }
      location {
        name
      }
    }
  }
}
```

---

### 6. Get Messages for a Match
```graphql
query GetMatchMessages {
  listMessages(filter: {
    matchId: { eq: "match-uuid-101" }
  }) {
    items {
      id
      text
      photoRes
      timestamp
      sender {
        name
        photoRes
      }
    }
  }
}
```

---

## Custom Operations

### 1. Get Active Users at Location
**Custom Lambda Query**

```graphql
query GetActiveUsersAtLocation {
  getActiveUsersAtLocation(locationId: "location-uuid-456") {
    id
    name
    age
    bio
    photoRes
    interests
  }
}
```

**Returns:** Array of User objects currently checked in at the location.

**Use Case:** Fetch users to display in swipe interface.

---

### 2. List Nearby Locations
**Custom Lambda Query**

```graphql
query ListNearbyLocations {
  listNearbyLocations(
    latitude: 37.7749
    longitude: -122.4194
    radiusKm: 1.0
  ) {
    id
    name
    address
    latitude
    longitude
    type
  }
}
```

**Parameters:**
- `latitude`: User's current latitude
- `longitude`: User's current longitude
- `radiusKm`: Search radius in kilometers (e.g., 1.0 = 1km)

**Returns:** Array of Locations within radius, sorted by distance (closest first).

**Use Case:** Show nearby venues for check-in.

---

### 3. Perform Swipe (with Match Detection)
**Custom Lambda Mutation**

```graphql
mutation PerformSwipe {
  performSwipe(
    userId: "user-uuid-123"
    targetUserId: "user-uuid-456"
    locationId: "location-uuid-789"
    action: LIKE
  ) {
    swipe {
      id
      action
      timestamp
    }
    matched
    match {
      id
      matchTime
      user1 {
        name
      }
      user2 {
        name
      }
    }
  }
}
```

**Parameters:**
- `userId`: Current user's ID
- `targetUserId`: User being swiped on
- `locationId`: Location where both are checked in
- `action`: `LIKE` or `IGNORE`

**Returns:**
- `swipe`: The created swipe record
- `matched`: Boolean - `true` if this swipe created a match
- `match`: Match object (only if matched = true)

**Logic:**
1. Creates swipe record
2. If action is `LIKE`, checks if target user already liked current user
3. If mutual like found, creates Match and sends push notifications
4. Returns result with match information

**Use Case:** Handle swipe action and immediately know if it's a match.

---

## Subscriptions

### Subscribe to Matches for User
**Real-time match notifications**

```graphql
subscription OnMatchCreated {
  onMatchCreatedForUser(userId: "user-uuid-123") {
    id
    user1Id
    user2Id
    matchTime
    location {
      name
    }
    user1 {
      name
      photoRes
    }
    user2 {
      name
      photoRes
    }
  }
}
```

**Triggered:** When `performSwipe` mutation creates a match involving this user.

**Use Case:** Show instant in-app match notification/animation.

**Android Example:**
```kotlin
val subscription = """
    subscription {
        onMatchCreatedForUser(userId: "$currentUserId") {
            id
            matchTime
            user1 { name photoRes }
            user2 { name photoRes }
            location { name }
        }
    }
""".trimIndent()

Amplify.API.subscribe(
    SimpleGraphQLRequest<String>(subscription, String::class.java),
    { Log.i("Match", "Subscription established") },
    { response ->
        // Parse match data and show notification
        showMatchDialog(response.data)
    },
    { error -> Log.e("Match", "Error", error) },
    { Log.i("Match", "Subscription completed") }
)
```

---

## Error Handling

### Common Errors

#### 1. User Already Checked In
```json
{
  "errors": [{
    "message": "User is already checked in to another location"
  }]
}
```
**Solution:** Check out from current location first.

---

#### 2. Duplicate Swipe
```json
{
  "errors": [{
    "message": "DynamoDB constraint violation"
  }]
}
```
**Reason:** User already swiped on this target at this location.
**Solution:** Filter out already-swiped users from UI.

---

#### 3. User Not Checked In
```json
{
  "errors": [{
    "message": "User must be checked in to swipe"
  }]
}
```
**Solution:** Verify user has active check-in before allowing swipes.

---

#### 4. Unauthorized
```json
{
  "errors": [{
    "message": "Unauthorized"
  }]
}
```
**Reason:** Missing or invalid API key.
**Solution:** Verify `amplifyconfiguration.json` is correctly configured.

---

## Best Practices

### 1. Check-In Flow
```
1. Request location permissions
2. Get user's GPS coordinates
3. Query listNearbyLocations(lat, lng, radius)
4. User selects location
5. Check if user already has active check-in
6. If yes, show "Buzz Out" option first
7. If no, call createCheckIn mutation
```

### 2. Swipe Flow
```
1. Verify user has active check-in
2. Query getActiveUsersAtLocation(locationId)
3. Filter out:
   - Current user
   - Already swiped users (query user's swipes at this location)
4. Display remaining users in swipe interface
5. On swipe, call performSwipe mutation
6. If matched=true, show match dialog immediately
7. Also listen to subscription for matches
```

### 3. Match Notification Flow
```
1. Subscribe to onMatchCreatedForUser on app start
2. When subscription fires:
   - Show in-app dialog/animation
   - Play sound/vibration
3. Push notification also sent via FCM
4. Notification tap opens match details
```

### 4. Message Flow
```
1. User opens match from matches list
2. Check if messageSent = false
3. If false, allow message composition
4. User writes text and optionally selects photo
5. Call createMessage mutation
6. Call updateMatch to set messageSent = true
7. Disable message input (one message per match)
```

---

## Rate Limits

**AppSync Free Tier:**
- 250,000 query/mutation operations per month
- 250,000 real-time updates per month

**For MVP:** Should be sufficient. Monitor usage in AWS Console.

---

## Testing Checklist

- [ ] Create test users
- [ ] Create test locations
- [ ] Test check-in/check-out flow
- [ ] Test listNearbyLocations with different radii
- [ ] Test getActiveUsersAtLocation
- [ ] Test swipe with LIKE action
- [ ] Test swipe with IGNORE action
- [ ] Verify match detection (mutual likes)
- [ ] Verify push notifications sent
- [ ] Test subscription receives matches
- [ ] Test message creation
- [ ] Test messageSent flag prevents duplicate messages
- [ ] Test error cases (duplicate check-in, etc.)

---

## Support

Questions? Check:
- Main README: `amplify/README.md`
- AWS AppSync Console: Test queries directly
- Amplify Docs: https://docs.amplify.aws/
