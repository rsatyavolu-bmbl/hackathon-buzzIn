# 🔧 AWS Amplify Deployment - Troubleshooting Guide

## Current Status

❌ **Automated deployment blocked** - Amplify CLI requires interactive input
✅ **Local backend working** - Docker backend fully functional at http://localhost:4000/graphql

---

## Issues Encountered

### 1. "The specified key does not exist"
**Cause**: Corrupted `team-provider-info.json` referenced non-existent AWS resources
**Fix Applied**: Removed all configuration files and created fresh amplify directory

### 2. "Cannot prompt in non-interactive shell"
**Cause**: Amplify CLI requires interactive input for Gen 1 vs Gen 2 choice
**Requires**: Manual terminal session

### 3. "EACCES: permission denied, mkdtemp"
**Cause**: macOS System Integrity Protection blocks system temp directory
**Fix**: Use custom TMPDIR

---

## ✅ Manual Deployment Steps (RECOMMENDED)

### Prerequisites
- ✅ AWS CLI installed and configured (Account: 265385634318)
- ✅ Amplify CLI installed (`npm install -g @aws-amplify/cli`)
- ✅ Clean amplify directory with backend code only

### Step 1: Initialize Amplify (Interactive)

**Open a new terminal window** and run:

```bash
cd /Users/stephen.chen/StudioProjects/hackathon-buzzIn

# Set custom temp directory to avoid permissions issue
export TMPDIR=~/tmp
mkdir -p ~/tmp

# Force Gen 1 to avoid prompt
export AMPLIFY_CLI_ENABLE_GEN_1=true

# Initialize (this will be interactive)
amplify init
```

**Answer the prompts:**
1. **Do you want to continue with Amplify Gen 1?** → `Y`
2. **Enter a name for the project** → `buzzin` (press Enter)
3. **Initialize the project with the above configuration?** → `Y`
4. **Select the authentication method** → `AWS profile`
5. **Please choose the profile you want to use** → `default`

**Expected output:**
```
✔ Successfully created initial AWS cloud resources for deployments.
✔ Initialized provider successfully.
Initialized your environment successfully.
```

### Step 2: Deploy Backend

After successful initialization:

```bash
amplify push --yes
```

This will deploy:
- AppSync GraphQL API (~5 min)
- DynamoDB tables (6 tables) (~10 min)
- Lambda functions (3 functions) (~15 min)

**Total deployment time: ~30-45 minutes**

### Step 3: Copy Configuration

After deployment completes:

```bash
# The configuration file will be at:
ls src/amplifyconfiguration.json

# Copy to Android app
cp src/amplifyconfiguration.json app/src/main/res/raw/amplifyconfiguration.json
```

### Step 4: Rebuild App

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🐳 Alternative: Continue with Local Backend

The local Docker backend is **already working perfectly** and includes:

✅ Full GraphQL API matching AWS schema
✅ All queries and mutations
✅ Match detection logic
✅ Test data (4 users, 5 locations)
✅ Health monitoring endpoint
✅ No deployment time or AWS costs

**To use local backend:**

```bash
# 1. Ensure backend is running
cd backend
npm start

# 2. App is already configured to use it!
# (app/src/main/res/raw/amplifyconfiguration.json already points to 10.0.2.2:4000)

# 3. Just rebuild and test
./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Local backend URL**: http://localhost:4000/graphql
**From Android emulator**: http://10.0.2.2:4000/graphql

---

## 📊 Comparison

| Feature | AWS Amplify | Local Docker |
|---------|-------------|--------------|
| Deployment time | 30-45 minutes | < 1 minute |
| Cost | AWS Free Tier (limits apply) | Free |
| Data persistence | Yes (DynamoDB) | No (in-memory) |
| Push notifications | Yes (Pinpoint/SNS) | No |
| Production ready | Yes | No (dev only) |
| Current status | ❌ Blocked by CLI issues | ✅ Working |

---

## 🎯 Recommendation

### For MVP/Testing (Now)
**Use local Docker backend** - It's working, fast, and perfect for development.

### For Production (Later)
**Deploy to AWS Amplify** when you have time for manual deployment process.

---

## 📞 Next Steps

### Option A: Continue with Local Backend
```bash
# Backend is already running!
curl http://localhost:4000/health

# Just test the app
~/Library/Android/sdk/platform-tools/adb shell am start -n com.buzzin.app/.MainActivity
```

### Option B: Deploy to AWS (Manual)
1. Open new terminal window
2. Run `export TMPDIR=~/tmp && export AMPLIFY_CLI_ENABLE_GEN_1=true`
3. Run `amplify init` and answer prompts
4. Run `amplify push --yes`
5. Wait 30-45 minutes
6. Copy configuration file to app
7. Rebuild app

---

## 🐛 Common Errors

### "The specified key does not exist"
**Solution**: Already fixed - configuration cleaned up

### "Cannot prompt in non-interactive shell"
**Solution**: Run `amplify init` in a real terminal window (not via automated scripts)

### "EACCES: permission denied"
**Solution**: Use `export TMPDIR=~/tmp` before running amplify commands

### "No Amplify backend project files detected"
**Solution**: Run `amplify init` first to initialize the project

---

## 📚 Resources

- [Amplify Gen 1 Documentation](https://docs.amplify.aws/cli/)
- [Amplify Android Setup](https://docs.amplify.aws/android/start/getting-started/setup/)
- [Local Backend Guide](BACKEND_LOCAL.md)
- [Manual Deploy Steps](AMPLIFY_DEPLOY_MANUAL.md)
