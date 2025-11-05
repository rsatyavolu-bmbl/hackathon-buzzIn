# Developer Setup Guide - AWS Amplify Backend

This guide will help you connect to the existing AWS Amplify backend after pulling from `main`.

## Prerequisites

1. **Node.js & npm** (v18 or later)
   ```bash
   node --version
   npm --version
   ```

2. **AWS Amplify CLI** (Gen 2)
   ```bash
   npm install -g @aws-amplify/cli
   ```

3. **AWS Account Access**
   - You'll need to be invited to the AWS account/Amplify app
   - Contact the project admin for access

## Quick Setup (Using Shared Backend)

Since the Amplify backend is already deployed, you just need the configuration files:

### Step 1: Copy Configuration Files

The repository includes a sample configuration. You need to get the actual configuration from a team member or AWS Console.

**Option A: Get from Team Member**
Ask a team member to send you:
- `amplify_outputs.json`
- `app/src/main/res/raw/amplifyconfiguration.json`

**Option B: Download from AWS Console**
1. Go to [AWS Amplify Console](https://console.aws.amazon.com/amplify/)
2. Select region: `us-east-1`
3. Find the app: `buzzin-backend`
4. Download the configuration files

### Step 2: Place Configuration Files

Copy the files to these locations:

```bash
# Root directory
cp /path/to/amplify_outputs.json ./amplify_outputs.json

# Android res/raw directory
cp /path/to/amplifyconfiguration.json ./app/src/main/res/raw/amplifyconfiguration.json
```

### Step 3: Configure Your User Identity

Each developer should have their own user ID to act as a different person in the app:

```bash
# Copy the sample config
cp app/src/main/res/raw/user_config_sample.json app/src/main/res/raw/user_config.json

# Edit with your own ID
nano app/src/main/res/raw/user_config.json
```

**Example user_config.json:**
```json
{
  "userId": "test-user-john-12345",
  "userName": "John Developer"
}
```

**Important**: Each developer should use a unique `userId` so you can test as different users!

Suggested format: `test-user-YOURNAME-12345`

### Step 4: Verify Setup

The configuration should contain:
- **GraphQL API URL**: `https://yutl6o6xvfhq3eeij2j3teg7aq.appsync-api.us-east-1.amazonaws.com/graphql`
- **Region**: `us-east-1`
- **API Key**: (starts with `da2-...`)

### Step 5: Build and Run

```bash
# Build the Android app
./gradlew assembleDebug

# Install on emulator
~/Library/Android/sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
```

## Backend Architecture

The app uses:
- **GraphQL API** with AppSync
- **DynamoDB** tables for data storage
- **Lambda Functions** for custom business logic:
  - `getActiveUsersAtLocation` - Get users checked in at a location
  - `listNearbyLocations` - Find locations within radius
  - `performSwipe` - Handle swipes and match detection

## API Endpoints

### GraphQL Queries
```graphql
# Get user
query GetUser {
  getUser(id: "user-id") {
    id
    name
    age
    bio
  }
}

# List nearby locations
query ListNearbyLocations {
  listNearbyLocations(latitude: 30.2672, longitude: -97.7431, radiusKm: 10.0)
}

# Get active users at location
query GetActiveUsers {
  getActiveUsersAtLocation(locationId: "loc-id")
}

# List check-ins
query ListCheckIns {
  listCheckIns(filter: {
    locationId: {eq: "loc-id"},
    isActive: {eq: true}
  }) {
    items {
      id
      userId
      locationId
      isActive
    }
  }
}
```

### GraphQL Mutations
```graphql
# Create check-in
mutation CreateCheckIn {
  createCheckIn(input: {
    userId: "user-id",
    locationId: "loc-id",
    checkInTime: "2025-11-05T12:00:00Z",
    isActive: true,
    latitude: 30.2672,
    longitude: -97.7431
  }) {
    id
    userId
    locationId
  }
}

# Update check-in (check out)
mutation UpdateCheckIn {
  updateCheckIn(input: {
    id: "checkin-id",
    isActive: false,
    checkOutTime: "2025-11-05T13:00:00Z"
  }) {
    id
    isActive
  }
}

# Perform swipe
mutation PerformSwipe {
  performSwipe(
    userId: "user-id",
    targetUserId: "target-id",
    locationId: "loc-id",
    action: "LIKE"
  )
}
```

## Testing Scripts

The repository includes helpful test scripts:

```bash
# Setup test profile and data
node setup-my-profile.js

# Remove all your check-ins
node remove-all-my-checkins.js

# Fix duplicate check-ins
node fix-duplicate-checkins.js
```

## Common Issues

### Issue: "No configuration found"
**Solution**: Make sure `amplifyconfiguration.json` is in `app/src/main/res/raw/`

### Issue: "API call failed"
**Solution**: Check that you have the correct API key and endpoint in the configuration

### Issue: "Lambda function timeout"
**Solution**: Some Lambda functions may have cold start delays. Retry the operation.

## Advanced: Deploy Your Own Backend (Optional)

If you want to deploy your own instance:

```bash
# Install dependencies
cd amplify
npm install

# Configure AWS credentials
aws configure

# Deploy to sandbox
npx ampx sandbox

# Deploy to production
npx ampx pipeline-deploy --branch main
```

## Environment Variables

The app uses a fixed test user ID: `test-user-fixed-id-12345`

In production, you would replace this with actual authentication.

## Need Help?

- Check the AWS Amplify Console for logs
- View GraphQL API schema in AppSync Console
- Check Lambda function logs in CloudWatch
- Contact team lead for access issues

## Security Notes

⚠️ **Never commit these files to git:**
- `amplify_outputs.json`
- `app/src/main/res/raw/amplifyconfiguration.json`
- `app/src/main/res/raw/awsconfiguration.json`

These files contain API keys and should remain in `.gitignore`.
