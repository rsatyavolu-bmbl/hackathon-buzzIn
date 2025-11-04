# 🚀 AWS Amplify Deployment - Step by Step

## Current Status
✅ Local backend removed
✅ Amplify backend code ready
✅ AWS credentials configured (Account: 265385634318)
⏳ Ready for deployment

---

## STEP 1: Open Terminal

**Open a NEW Terminal window** (not through automation) and navigate to your project:

```bash
cd /Users/stephen.chen/StudioProjects/hackathon-buzzIn
```

---

## STEP 2: Set Up Environment Variables

Run these commands to avoid permission issues:

```bash
# Create temp directory
mkdir -p ~/tmp

# Set environment variables
export TMPDIR=~/tmp
export AMPLIFY_CLI_ENABLE_GEN_1=true
```

---

## STEP 3: Initialize Amplify (INTERACTIVE)

Run this command:

```bash
amplify init
```

### Answer the prompts:

1. **Do you want to continue with Amplify Gen 1?**
   - Answer: `Y`

2. **Enter a name for the project**
   - Answer: `buzzin` (press Enter)

3. **Initialize the project with the above configuration?**
   - Answer: `Y`

4. **Select the authentication method you want to use:**
   - Answer: `AWS profile` (press Enter)

5. **Please choose the profile you want to use**
   - Answer: `default` (press Enter)

### Expected Output:
```
✔ Successfully created initial AWS cloud resources for deployments.
✔ Initialized provider successfully.
Initialized your environment successfully.
```

---

## STEP 4: Deploy Backend to AWS

After successful initialization, deploy with:

```bash
amplify push --yes
```

### What This Does:
- Creates AppSync GraphQL API
- Creates 6 DynamoDB tables (User, Location, CheckIn, Swipe, Match, Message)
- Deploys 3 Lambda functions:
  - `getActiveUsersAtLocation` - Query users at location
  - `listNearbyLocations` - Find nearby venues
  - `performSwipe` - Handle swipes + match detection

### Deployment Time:
**30-45 minutes** (AWS is creating all resources)

### Expected Output:
```
✔ Successfully pulled backend environment dev from the cloud.
✔ GraphQL schema compiled successfully.
✔ All resources are updated in the cloud
```

---

## STEP 5: Copy Configuration to App

After deployment completes, copy the generated configuration:

```bash
# Check the file exists
ls src/amplifyconfiguration.json

# Copy to Android app
cp src/amplifyconfiguration.json app/src/main/res/raw/amplifyconfiguration.json
```

---

## STEP 6: Rebuild and Install App

```bash
# Set Java home
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Clean and build
./gradlew clean assembleDebug

# Install on emulator
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## STEP 7: Test the App

```bash
# Launch app
~/Library/Android/sdk/platform-tools/adb shell am start -n com.buzzin.app/.MainActivity

# Monitor API calls
~/Library/Android/sdk/platform-tools/adb logcat -c
~/Library/Android/sdk/platform-tools/adb logcat | grep -E "BuzzInApiClient|Amplify"
```

---

## 🐛 Troubleshooting

### "The specified key does not exist"
**Solution**: Make sure you set the environment variables in STEP 2

### "Cannot prompt in non-interactive shell"
**Solution**: Must run in a real terminal window, not automated

### "EACCES: permission denied"
**Solution**: Run `export TMPDIR=~/tmp` again

### Deployment stuck or taking too long
**Normal**: AWS deployment takes 30-45 minutes. Don't interrupt!

---

## 📊 What Gets Created in AWS

### AppSync (GraphQL API)
- API endpoint URL
- API Key for authentication
- GraphQL schema with all types

### DynamoDB Tables (6)
- User
- Location
- CheckIn
- Swipe
- Match
- Message

### Lambda Functions (3)
- getActiveUsersAtLocation
- listNearbyLocations
- performSwipe

### Cost
**FREE** - Everything fits within AWS Free Tier limits

---

## ✅ Verification Steps

After deployment, verify everything worked:

### 1. Check Amplify Status
```bash
amplify status
```

Should show all resources deployed.

### 2. Check AWS Console
- AppSync: https://console.aws.amazon.com/appsync
- DynamoDB: https://console.aws.amazon.com/dynamodb
- Lambda: https://console.aws.amazon.com/lambda

### 3. Test GraphQL API
```bash
# Get the API endpoint
cat src/amplifyconfiguration.json | grep endpoint
```

---

## 📝 Summary

1. ✅ Open terminal
2. ✅ Set environment variables
3. ⏳ Run `amplify init` (answer prompts)
4. ⏳ Run `amplify push --yes` (wait 30-45 min)
5. ⏳ Copy config to app
6. ⏳ Rebuild and test

**Ready to start? Open your terminal and begin with STEP 1!**
