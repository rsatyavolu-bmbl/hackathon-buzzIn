# 📱 BuzzIn App - Current Deployment Status

**Last Updated**: November 4, 2025, 4:30 PM

---

## ✅ What's Working

### 1. Local Docker Backend
- **Status**: ✅ RUNNING
- **URL**: http://localhost:4000/graphql
- **GraphiQL UI**: http://localhost:4000/graphql
- **Health**: http://localhost:4000/health
- **Test Data**: 4 users, 5 San Francisco locations

### 2. Android App
- **Status**: ✅ INSTALLED on emulator
- **Configuration**: Connected to local backend (10.0.2.2:4000)
- **Features Working**:
  - ✅ Amplify SDK initialization
  - ✅ API client (GraphQL queries/mutations)
  - ✅ Repository layer
  - ✅ Data models (User, Location, CheckIn, Match, Message)
  - ⚠️ Map screen (Google Maps API key needs enabling)

### 3. Git Repository
- **Status**: ✅ All changes committed
- **Branch**: main
- **Latest Commit**: Backend integration + local Docker setup

---

## ⏳ Pending: AWS Amplify Deployment

### Issue
AWS Amplify CLI requires **manual interactive setup** - cannot be automated.

### Attempts Made
1. ✅ Cleaned up corrupted configuration files
2. ✅ Removed nested amplify directories
3. ✅ Created fresh amplify directory with backend code only
4. ✅ Set up TMPDIR workaround for permissions
5. ❌ Automated `amplify init` - blocked by interactive prompts
6. ❌ Headless mode - configuration format issues

### Root Cause
- Amplify CLI prompts for Gen 1 vs Gen 2 choice (interactive only)
- Cannot run in non-interactive/automated mode
- Requires real terminal session with user input

---

## 🎯 Two Paths Forward

### Path A: Continue with Local Backend (RECOMMENDED for now)

**Why?**
- Already working perfectly
- Zero deployment time
- No AWS costs
- Full API functionality
- Ideal for development and testing

**How to use:**
```bash
# 1. Backend is already running! Check status:
curl http://localhost:4000/health

# 2. App is already configured and installed!
# Just launch it:
~/Library/Android/sdk/platform-tools/adb shell am start -n com.buzzin.app/.MainActivity

# 3. Monitor API calls:
~/Library/Android/sdk/platform-tools/adb logcat | grep -E "BuzzInApiClient|AmplifyRepository"
```

**Limitations:**
- ❌ No data persistence (in-memory only, resets on restart)
- ❌ No push notifications
- ❌ Not production-ready

**Perfect for:**
- ✅ MVP development
- ✅ Feature testing
- ✅ UI development
- ✅ Demo presentation

---

### Path B: Deploy to AWS Amplify (Manual Process)

**Why?**
- Production-grade infrastructure
- Data persistence in DynamoDB
- Push notifications via AWS Pinpoint/SNS
- Scalable and reliable

**How to deploy:**

**⚠️ REQUIRES MANUAL TERMINAL SESSION**

```bash
# 1. Open a NEW terminal window (not through automation)
cd /Users/stephen.chen/StudioProjects/hackathon-buzzIn

# 2. Set up environment
export TMPDIR=~/tmp
mkdir -p ~/tmp
export AMPLIFY_CLI_ENABLE_GEN_1=true

# 3. Initialize (INTERACTIVE - will ask questions)
amplify init

# Answer prompts:
# - Continue with Gen 1? → Y
# - Project name → buzzin (Enter)
# - Initialize with config? → Y
# - Auth method → AWS profile
# - Profile → default

# 4. Deploy backend (~30-45 minutes)
amplify push --yes

# 5. Copy configuration
cp src/amplifyconfiguration.json app/src/main/res/raw/amplifyconfiguration.json

# 6. Rebuild app
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Time required:** 30-45 minutes for AWS deployment

**See detailed guide:** `AMPLIFY_TROUBLESHOOTING.md`

---

## 🚀 Quick Commands Reference

### Check Backend Status
```bash
curl http://localhost:4000/health
```

### Restart Backend
```bash
cd backend
npm start
```

### Launch App on Emulator
```bash
~/Library/Android/sdk/platform-tools/adb shell am start -n com.buzzin.app/.MainActivity
```

### View App Logs
```bash
~/Library/Android/sdk/platform-tools/adb logcat | grep -E "BuzzInApiClient|Amplify"
```

### Test API Directly
```bash
# List locations
curl -X POST http://localhost:4000/graphql \
  -H 'Content-Type: application/json' \
  -d '{"query": "{ listLocations { id name address type } }"}'
```

### GraphiQL Web UI
Open in browser: http://localhost:4000/graphql

---

## 🐛 Known Issues

### 1. Map Screen Not Showing
**Issue**: Google Maps API key needs to be enabled
**Fix**:
1. Go to: https://console.cloud.google.com/apis/library/maps-android-backend.googleapis.com
2. Enable "Maps SDK for Android"
3. Wait 5 minutes for propagation
4. Rebuild app

### 2. AWS Amplify Deployment
**Issue**: Requires manual interactive setup
**Fix**: Follow manual steps in `AMPLIFY_TROUBLESHOOTING.md`

---

## 📊 Backend API Reference

See `BACKEND_LOCAL.md` for complete API documentation including:
- All queries (listLocations, getActiveUsersAtLocation, listMatches, etc.)
- All mutations (createUser, checkIn, performSwipe, createMessage, etc.)
- Sample GraphQL queries
- Test data IDs

---

## 📞 Summary

### Current State
✅ **Local backend running and tested**
✅ **App installed and configured**
✅ **All code committed to git**
⏳ **AWS deployment requires manual terminal session**

### Recommendation
**For immediate development/testing**: Use local Docker backend (already working!)
**For production deployment**: Run manual `amplify init` in terminal when ready

### Next Action
Your choice:
1. **Continue developing** with local backend (zero setup needed)
2. **Deploy to AWS** by opening terminal and running `amplify init` manually

Both backends use the same GraphQL API, so switching between them later is seamless.
