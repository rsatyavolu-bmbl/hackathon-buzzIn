# BuzzIn Backend - Deployment Guide

## Overview
Complete AWS Amplify backend for BuzzIn MVP with GraphQL API, match detection, and push notifications.

## Prerequisites
- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Amplify CLI installed (`npm install -g @aws-amplify/cli`)
- Firebase project for push notifications (FCM)

---

## 1. AWS Setup

### Configure AWS Credentials
```bash
amplify configure
```

Follow prompts to:
- Sign in to AWS Console
- Create IAM user with AdministratorAccess
- Enter Access Key ID and Secret Access Key
- Choose region (recommend us-east-1)

---

## 2. Initialize Amplify Project

```bash
cd /Users/stephen.chen/StudioProjects/hackathon-buzzIn
amplify init
```

Configuration:
- Project name: `buzzin`
- Environment name: `dev`
- Default editor: (your choice)
- App type: `android`
- Choose your configured AWS profile

---

## 3. Firebase Cloud Messaging Setup

### Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Create new project "BuzzIn"
3. Add Android app with package name: `com.buzzin.app`
4. Download `google-services.json` → place in `app/` directory

### Step 2: Get FCM Server Key
1. Firebase Console → Project Settings → Cloud Messaging
2. Copy "Server key" (legacy)
3. Save for next step

### Step 3: Configure AWS Pinpoint (or SNS)

**Option A: AWS Pinpoint** (Recommended)
```bash
amplify add notifications
```
- Choose: FCM
- Provide FCM Server Key
- Choose channels: Push notifications

**Option B: AWS SNS** (Alternative)
```bash
# Create SNS Platform Application via AWS Console
# Use FCM Server Key as credential
# Update Lambda environment variables with Platform Application ARN
```

---

## 4. Deploy Backend

### Deploy all resources
```bash
amplify push
```

This will:
- Create AppSync GraphQL API
- Deploy 3 Lambda functions
- Create DynamoDB tables (6 tables)
- Set up IAM roles and permissions
- Configure push notifications
- Generate `amplifyconfiguration.json`

### Verify deployment
```bash
amplify status
```

Should show:
- API: `buzzinapi` (deployed)
- Functions: 3 Lambda functions (deployed)
- Notifications: Pinpoint/SNS (deployed)

---

## 5. Environment Variables

After deployment, Lambda functions need environment variable updates:

### Update Lambda Environment Variables
Go to AWS Console → Lambda → each function:

**All functions need:**
- `USER_TABLE`: `User-[api-id]-dev`
- `LOCATION_TABLE`: `Location-[api-id]-dev`
- `CHECKIN_TABLE`: `CheckIn-[api-id]-dev`
- `SWIPE_TABLE`: `Swipe-[api-id]-dev`
- `MATCH_TABLE`: `Match-[api-id]-dev`
- `MESSAGE_TABLE`: `Message-[api-id]-dev`

Get table names from AppSync console or DynamoDB console.

**performSwipe function also needs:**
- `SNS_PLATFORM_APPLICATION_ARN`: (from Pinpoint/SNS setup)

---

## 6. Configure Android App

### Update Android Project

1. **Copy generated config:**
```bash
cp amplify/aws-exports.js app/src/main/res/raw/amplifyconfiguration.json
```

2. **Add to .gitignore:**
```
amplifyconfiguration.json
google-services.json
```

3. **Enable Amplify in BuzzInApplication.kt:**
Uncomment the Amplify initialization code.

---

## 7. Testing the API

### GraphQL API Endpoint
Find in: AWS Console → AppSync → Settings → API URL

### Test with AppSync Console

**Create a test user:**
```graphql
mutation CreateUser {
  createUser(input: {
    name: "John Doe"
    age: 28
    photoRes: "avatar_1"
    bio: "Coffee enthusiast"
    interests: ["coffee", "tech", "travel"]
  }) {
    id
    name
    age
  }
}
```

**Create a test location:**
```graphql
mutation CreateLocation {
  createLocation(input: {
    name: "Starbucks Downtown"
    address: "123 Main St"
    latitude: 37.7749
    longitude: -122.4194
    type: COFFEE
  }) {
    id
    name
  }
}
```

**Check in to location:**
```graphql
mutation CheckIn {
  createCheckIn(input: {
    userId: "user-id-here"
    locationId: "location-id-here"
    checkInTime: "2025-11-04T10:00:00Z"
    isActive: true
    latitude: 37.7749
    longitude: -122.4194
  }) {
    id
    isActive
  }
}
```

**Get active users at location:**
```graphql
query GetActiveUsers {
  getActiveUsersAtLocation(locationId: "location-id-here") {
    id
    name
    age
    photoRes
  }
}
```

**Perform swipe:**
```graphql
mutation Swipe {
  performSwipe(
    userId: "user1-id"
    targetUserId: "user2-id"
    locationId: "location-id"
    action: LIKE
  ) {
    swipe {
      id
      action
    }
    matched
    match {
      id
      matchTime
    }
  }
}
```

---

## 8. API Documentation for Frontend Team

### Base Configuration
```kotlin
// Add to app/build.gradle
implementation 'com.amplifyframework:aws-api:2.14.5'
implementation 'com.amplifyframework:core:2.14.5'
```

### Initialize Amplify
```kotlin
// In BuzzInApplication.kt
Amplify.configure(applicationContext)
```

### API Operations

#### 1. Create User
```kotlin
val user = User.builder()
    .name("John Doe")
    .age(28)
    .photoRes("avatar_1")
    .bio("Coffee lover")
    .interests(listOf("coffee", "tech"))
    .build()

Amplify.API.mutate(
    ModelMutation.create(user),
    { response -> Log.i("API", "User created: ${response.data.id}") },
    { error -> Log.e("API", "Error", error) }
)
```

#### 2. Check In
```kotlin
val checkIn = CheckIn.builder()
    .userId(currentUserId)
    .locationId(selectedLocationId)
    .checkInTime(Temporal.DateTime.now())
    .isActive(true)
    .latitude(currentLat)
    .longitude(currentLng)
    .build()

Amplify.API.mutate(ModelMutation.create(checkIn), ...)
```

#### 3. Get Active Users at Location
```kotlin
val query = """
    query GetActiveUsers {
        getActiveUsersAtLocation(locationId: "$locationId") {
            id name age photoRes bio interests
        }
    }
""".trimIndent()

Amplify.API.query(
    SimpleGraphQLRequest<String>(query, String::class.java),
    { response -> /* parse JSON */ },
    { error -> /* handle error */ }
)
```

#### 4. Perform Swipe
```kotlin
val mutation = """
    mutation PerformSwipe {
        performSwipe(
            userId: "$userId"
            targetUserId: "$targetUserId"
            locationId: "$locationId"
            action: LIKE
        ) {
            matched
            match { id matchTime }
        }
    }
""".trimIndent()

Amplify.API.mutate(SimpleGraphQLRequest<String>(mutation, ...), ...)
```

#### 5. Subscribe to Matches
```kotlin
val subscription = """
    subscription OnMatchCreated {
        onMatchCreatedForUser(userId: "$currentUserId") {
            id user1Id user2Id locationId matchTime
        }
    }
""".trimIndent()

Amplify.API.subscribe(
    SimpleGraphQLRequest<String>(subscription, String::class.java),
    { Log.i("API", "Subscription started") },
    { response -> /* New match received! */ },
    { error -> Log.e("API", "Error", error) },
    { Log.i("API", "Subscription ended") }
)
```

---

## 9. Push Notification Integration (Android)

### Register device token
```kotlin
// Get FCM token
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val token = task.result

    // Register with backend (add to User model or separate endpoint)
    // Update user.deviceEndpointArn with SNS endpoint
}
```

### Handle notifications
```kotlin
// In your FirebaseMessagingService
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = remoteMessage.data
    when (data["type"]) {
        "MATCH" -> {
            // Show match notification
            val userName = data["userName"]
            val locationName = data["locationName"]
            showMatchNotification(userName, locationName)
        }
    }
}
```

---

## 10. Cost Optimization

**Free Tier Resources:**
- AppSync: 250K queries/month
- Lambda: 1M requests/month
- DynamoDB: 25GB storage, 200M requests/month
- Pinpoint: 5000 targeted users/month

**For MVP:** Should stay within free tier

---

## Support

For issues or questions:
- AWS Amplify Docs: https://docs.amplify.aws/
- AppSync Docs: https://docs.aws.amazon.com/appsync/
- Firebase FCM: https://firebase.google.com/docs/cloud-messaging

---

## Quick Commands Reference

```bash
# View status
amplify status

# Deploy changes
amplify push

# Pull backend config
amplify pull

# View API details
amplify api console

# View logs
amplify function logs <function-name>

# Remove backend (DANGER!)
amplify delete
```
