# 🚀 Manual Amplify Deployment Steps

The automated deployment encountered configuration issues. Follow these manual steps to deploy:

## Step 1: Clean Start

```bash
# Remove incomplete configuration
rm -rf amplify

# Start fresh
mkdir amplify
cd amplify
```

## Step 2: Initialize Amplify (Interactive)

```bash
amplify init
```

**Enter these values:**
- Name for the project: `buzzin`
- Name for the environment: `dev`
- Default editor: `Visual Studio Code`
- Type of app: `android`
- Res directory: `../app/src/main/res`
- Authentication method: `AWS profile`
- AWS Profile: `default`

## Step 3: Add GraphQL API

```bash
amplify add api
```

**Enter these values:**
- Service: `GraphQL`
- API name: `buzzinapi`
- Authorization type: `API key`
- API key description: `BuzzIn API Key`
- API key expiration: `365` (1 year)
- Additional auth types: `N`
- Conflict detection: `N`
- GraphQL schema template: `Single object with fields`

## Step 4: Replace the Schema

After running `amplify add api`, replace the generated schema with our BuzzIn schema:

```bash
# Copy our schema
cp amplify.backup/backend/api/buzzinapi/schema.graphql amplify/backend/api/buzzinapi/schema.graphql
```

**Or manually paste this schema into `amplify/backend/api/buzzinapi/schema.graphql`:**

```graphql
type User @model @auth(rules: [{allow: public}]) {
  id: ID!
  name: String!
  age: Int!
  bio: String
  photoRes: String!
  interests: [String]
  checkIns: [CheckIn] @hasMany(indexName: "byUser", fields: ["id"])
  swipesGiven: [Swipe] @hasMany(indexName: "byUser", fields: ["id"])
  swipesReceived: [Swipe] @hasMany(indexName: "byTargetUser", fields: ["id"])
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

type Location @model @auth(rules: [{allow: public}]) {
  id: ID!
  name: String!
  address: String!
  latitude: Float!
  longitude: Float!
  type: LocationType!
  checkIns: [CheckIn] @hasMany(indexName: "byLocation", fields: ["id"])
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

enum LocationType {
  COFFEE
  RESTAURANT
  BAR
  PARK
  GYM
  OTHER
}

type CheckIn @model @auth(rules: [{allow: public}]) {
  id: ID!
  userId: ID! @index(name: "byUser", sortKeyFields: ["checkInTime"])
  user: User @belongsTo(fields: ["userId"])
  locationId: ID! @index(name: "byLocation", sortKeyFields: ["checkInTime"])
  location: Location @belongsTo(fields: ["locationId"])
  checkInTime: AWSDateTime!
  checkOutTime: AWSDateTime
  isActive: Boolean!
  latitude: Float
  longitude: Float
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

type Swipe @model @auth(rules: [{allow: public}]) {
  id: ID!
  userId: ID! @index(name: "byUser", sortKeyFields: ["timestamp"])
  targetUserId: ID! @index(name: "byTargetUser", sortKeyFields: ["timestamp"])
  user: User @belongsTo(fields: ["userId"])
  targetUser: User @belongsTo(fields: ["targetUserId"])
  locationId: ID!
  location: Location @belongsTo(fields: ["locationId"])
  action: SwipeAction!
  timestamp: AWSDateTime!
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

enum SwipeAction {
  LIKE
  IGNORE
}

type Match @model @auth(rules: [{allow: public}]) {
  id: ID!
  user1Id: ID!
  user2Id: ID!
  user1: User @belongsTo(fields: ["user1Id"])
  user2: User @belongsTo(fields: ["user2Id"])
  locationId: ID!
  location: Location @belongsTo(fields: ["locationId"])
  matchTime: AWSDateTime!
  messageSent: Boolean!
  messages: [Message] @hasMany(indexName: "byMatch", fields: ["id"])
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

type Message @model @auth(rules: [{allow: public}]) {
  id: ID!
  matchId: ID! @index(name: "byMatch", sortKeyFields: ["timestamp"])
  match: Match @belongsTo(fields: ["matchId"])
  senderId: ID!
  sender: User @belongsTo(fields: ["senderId"])
  text: String!
  photoRes: String
  timestamp: AWSDateTime!
  isRead: Boolean!
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}
```

## Step 5: Add Lambda Functions (Optional)

For custom business logic, add these functions:

```bash
amplify add function
```

**For each function (getActiveUsersAtLocation, listNearbyLocations, performSwipe):**
- Function name: `getActiveUsersAtLocation` (or other names)
- Runtime: `NodeJS`
- Template: `Hello World`
- Advanced settings: `No`

Then copy the function code from `amplify.backup/backend/function/*/src/index.js`

## Step 6: Deploy to AWS

```bash
amplify push --allow-destructive-graphql-schema-updates
```

This will:
- Create AppSync GraphQL API (~5 min)
- Create DynamoDB tables (~10 min)
- Deploy Lambda functions (~5 min each)
- **Total time: ~30-45 minutes**

## Step 7: Copy Configuration

After deployment completes:

```bash
# Configuration will be generated automatically
ls amplify/

# Copy to app
cp src/amplifyconfiguration.json app/src/main/res/raw/amplifyconfiguration.json
```

## Step 8: Rebuild App

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚡ Simpler Alternative: Use Local Backend

The local Docker backend is already working perfectly! If AWS deployment is taking too long:

```bash
# Backend is already running at http://localhost:4000/graphql
# App is already configured to use it (via 10.0.2.2:4000)
# No deployment needed!
```

---

## 📞 Need Help?

If you encounter issues, you can:
1. Check amplify logs: `amplify console`
2. View CloudFormation: https://console.aws.amazon.com/cloudformation
3. Continue using local backend while troubleshooting AWS
