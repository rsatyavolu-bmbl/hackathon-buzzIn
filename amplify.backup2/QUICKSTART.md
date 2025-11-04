# BuzzIn Backend - Quick Start Guide

## Prerequisites Checklist
- [ ] AWS Account created
- [ ] Node.js installed (v14+)
- [ ] Amplify CLI installed: `npm install -g @aws-amplify/cli`
- [ ] Firebase project created (for FCM)

---

## Step 1: Configure AWS (5 minutes)

```bash
amplify configure
```

Follow the prompts:
1. Sign in to AWS Console
2. Create IAM user with `AdministratorAccess` policy
3. Save Access Key ID and Secret Access Key
4. Choose region: `us-east-1` (recommended)

---

## Step 2: Initialize Project (2 minutes)

```bash
cd /Users/stephen.chen/StudioProjects/hackathon-buzzIn
amplify init
```

Configuration:
- **Project name:** `buzzin`
- **Environment:** `dev`
- **Default editor:** (your choice)
- **App type:** `android`
- **Choose AWS profile:** (select the one you just created)

---

## Step 3: Add API (30 seconds)

```bash
amplify add api
```

Choose:
- **Service:** GraphQL
- **API name:** `buzzinapi`
- **Authorization mode:** API key
- **API key expiration:** 365 days
- **Configure additional auth:** No
- **Schema template:** Skip (we already have schema.graphql)

The schema at `amplify/backend/api/buzzinapi/schema.graphql` will be used.

---

## Step 4: Add Lambda Functions (2 minutes)

```bash
# These directories and code already exist
# Amplify will detect them during push
```

The Lambda functions are already created in:
- `amplify/backend/function/getActiveUsersAtLocation/`
- `amplify/backend/function/listNearbyLocations/`
- `amplify/backend/function/performSwipe/`

---

## Step 5: Configure Firebase (5 minutes)

### Get FCM Server Key
1. Go to https://console.firebase.google.com/
2. Select your project (or create new)
3. Add Android app: package name = `com.buzzin.app`
4. Download `google-services.json` → save to `app/` directory
5. Go to Project Settings → Cloud Messaging
6. Copy **Server key** (under Cloud Messaging API - Legacy)

### Add Notifications to Amplify
```bash
amplify add notifications
```

Choose:
- **Resource name:** `BuzzInNotifications`
- **Select service:** Pinpoint
- **Select channel:** FCM
- **Provide FCM key:** (paste the Server key)

---

## Step 6: Deploy Everything (5-10 minutes)

```bash
amplify push
```

This will:
- Create AppSync GraphQL API
- Deploy 3 Lambda functions
- Create 6 DynamoDB tables
- Set up Pinpoint for push notifications
- Generate `amplifyconfiguration.json`

**Review the changes and confirm:** `Yes`

---

## Step 7: Configure Lambda Environment Variables (5 minutes)

After deployment, you need to update Lambda environment variables with table names.

### Get Table Names
```bash
amplify api console
```

Or check AWS Console → DynamoDB → Tables

Table names will be in format: `ModelName-[api-id]-dev`

### Update Each Lambda Function

Go to AWS Console → Lambda:

**For all 3 functions, add these environment variables:**
```
USER_TABLE = User-xxxxx-dev
LOCATION_TABLE = Location-xxxxx-dev
CHECKIN_TABLE = CheckIn-xxxxx-dev
SWIPE_TABLE = Swipe-xxxxx-dev
MATCH_TABLE = Match-xxxxx-dev
MESSAGE_TABLE = Message-xxxxx-dev
```

Replace `xxxxx` with your actual API ID.

---

## Step 8: Test API (5 minutes)

### Open AppSync Console
```bash
amplify api console
```

### Create Test User
```graphql
mutation CreateUser {
  createUser(input: {
    name: "Alice"
    age: 25
    photoRes: "avatar_1"
    bio: "Coffee lover"
    interests: ["coffee", "travel"]
  }) {
    id
    name
  }
}
```

### Create Test Location
```graphql
mutation CreateLocation {
  createLocation(input: {
    name: "Central Perk"
    address: "New York, NY"
    latitude: 40.7128
    longitude: -74.0060
    type: COFFEE
  }) {
    id
    name
  }
}
```

### Test Check-in
```graphql
mutation CheckIn {
  createCheckIn(input: {
    userId: "paste-user-id-here"
    locationId: "paste-location-id-here"
    checkInTime: "2025-11-04T10:00:00Z"
    isActive: true
    latitude: 40.7128
    longitude: -74.0060
  }) {
    id
    isActive
  }
}
```

---

## Step 9: Integrate with Android App (10 minutes)

### Copy Configuration File
```bash
# The file is generated at:
# amplify/aws-exports.js

# Copy to Android project:
cp amplify/aws-exports.js app/src/main/res/raw/amplifyconfiguration.json
```

### Update BuzzInApplication.kt

Uncomment the Amplify initialization code:
```kotlin
Amplify.addPlugin(AWSApiPlugin())
Amplify.addPlugin(AWSCognitoAuthPlugin())
Amplify.configure(applicationContext)
```

### Add google-services.json
Copy the Firebase `google-services.json` to:
```
app/google-services.json
```

### Build and Run
```bash
./gradlew clean build
```

---

## Step 10: Verify Push Notifications (5 minutes)

### Register Device Token
In your Android app, get FCM token and register with Pinpoint:
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val token = task.result
    // Register this token with your backend
    // Store in User model or separate endpoint table
}
```

### Test Notification
Use Pinpoint Console to send test notification:
1. AWS Console → Pinpoint
2. Select BuzzInNotifications project
3. Test messaging → Direct messages
4. Send test notification

---

## Troubleshooting

### Issue: "Amplify has not been configured"
**Solution:** Make sure `amplifyconfiguration.json` is in `res/raw/` and Amplify.configure() is called

### Issue: Lambda "Table not found"
**Solution:** Add environment variables to Lambda functions (Step 7)

### Issue: "Unauthorized" API error
**Solution:** Check API key in amplifyconfiguration.json, may need to regenerate

### Issue: Push notifications not received
**Solution:**
1. Verify FCM Server Key is correct in Pinpoint
2. Check device is registered with Pinpoint
3. Verify google-services.json is in app/

---

## Quick Commands

```bash
# View status
amplify status

# Deploy changes
amplify push

# Open API console
amplify api console

# View Lambda logs
amplify function logs performSwipe

# Pull backend environment
amplify pull

# Add new environment (prod)
amplify env add
```

---

## Next Steps

1. ✅ Seed database with sample locations
2. ✅ Test complete flow: check-in → swipe → match → notify
3. ✅ Configure production environment
4. ✅ Set up monitoring and alarms
5. ✅ Implement analytics tracking

---

## Total Time: ~45 minutes

**You're ready to build the MVP!** 🚀

For detailed API documentation, see: `amplify/API_REFERENCE.md`
