# How to View Application IDs Across Multiple Devices

## Quick Answer

Your **Application ID** uniquely identifies each device installation. Each emulator or physical device will have its own unique Application ID.

## 🎯 Easiest Method: In-App Settings Screen

### Steps:
1. Open the BuzzIn app
2. Navigate to **Settings** (bottom navigation or menu)
3. Your **Application ID** is displayed at the top under "Device Information"
4. Tap the **Copy icon** to copy it to clipboard
5. For more details, tap **"Device ID Debug"** under Developer Tools

### What You'll See:
```
Device Information
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Application ID
5fbb5562-77cc-44b6-907b-9506725360f2  [📋]
This ID uniquely identifies this device installation
```

## 📱 Multiple Devices/Emulators

### Scenario: You have 3 emulators on different machines

**Machine 1 - Emulator A:**
```
Application ID: 5fbb5562-77cc-44b6-907b-9506725360f2
Android Device ID: 00e04c7c7b1268d4
```

**Machine 2 - Emulator B:**
```
Application ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
Android Device ID: 1234567890abcdef
```

**Machine 3 - Physical Device:**
```
Application ID: f9e8d7c6-b5a4-3210-9876-543210fedcba
Android Device ID: 9876543210fedcba
```

### How to Track Multiple Devices:

1. **Open Settings on each device**
2. **Copy the Application ID** from each device
3. **Save them in a spreadsheet or document:**

| Device Name | Machine | Application ID | Notes |
|-------------|---------|----------------|-------|
| Emulator A | MacBook Pro | 5fbb5562-77cc-44b6-907b-9506725360f2 | Development |
| Emulator B | iMac | a1b2c3d4-e5f6-7890-abcd-ef1234567890 | Testing |
| Pixel 6 | Physical | f9e8d7c6-b5a4-3210-9876-543210fedcba | Production |

## 🔧 Alternative Methods

### Method 1: Using ADB (Android Debug Bridge)

#### For a specific device:
```bash
# Connect to the device
adb -s <device_serial> shell

# View the Application ID from logs
adb -s <device_serial> logcat | grep "Application ID"
```

#### For all connected devices:
```bash
# List all devices
adb devices

# Output:
# List of devices attached
# emulator-5554	device
# emulator-5556	device
# 192.168.1.100:5555	device

# Get Application ID from each
adb -s emulator-5554 logcat -d | grep "Application ID"
adb -s emulator-5556 logcat -d | grep "Application ID"
adb -s 192.168.1.100:5555 logcat -d | grep "Application ID"
```

### Method 2: View SharedPreferences File

```bash
# For a specific device
adb -s <device_serial> shell run-as com.buzzin.app cat /data/data/com.buzzin.app/shared_prefs/buzzin_device_prefs.xml

# Output:
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="application_id">5fbb5562-77cc-44b6-907b-9506725360f2</string>
</map>
```

### Method 3: Check Application Logs on Startup

```bash
# For a specific device
adb -s <device_serial> logcat -d | grep "BuzzInApplication"

# Output:
BuzzInApplication: BuzzIn Application starting...
BuzzInApplication: Application ID: 5fbb5562-77cc-44b6-907b-9506725360f2
BuzzInApplication: Session ID: 765e289e-0a01-4903-a45e-41525e56f95b
BuzzInApplication: Android Device ID: 00e04c7c7b1268d4
```

## 🖥️ Remote Devices (Different Machines)

### If you have emulators on different machines:

#### Option 1: Use the In-App Settings (Recommended)
1. Open the app on each machine's emulator
2. Go to Settings
3. Take a screenshot or copy the Application ID
4. Share via email/Slack/document

#### Option 2: Remote ADB Connection
```bash
# On the remote machine, enable ADB over network
adb tcpip 5555

# Get the machine's IP address
ifconfig | grep "inet "

# On your local machine, connect to remote device
adb connect <remote_ip>:5555

# View Application ID
adb -s <remote_ip>:5555 logcat -d | grep "Application ID"
```

#### Option 3: Backend Database Query
Once devices are registered with your backend:

```bash
# Query DynamoDB for all devices
aws dynamodb scan \
    --table-name BuzzInDevices \
    --projection-expression "applicationId,deviceModel,registeredAt"

# Output:
{
    "Items": [
        {
            "applicationId": "5fbb5562-77cc-44b6-907b-9506725360f2",
            "deviceModel": "BasicAppEmulator",
            "registeredAt": "2025-11-04T14:26:54Z"
        },
        {
            "applicationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "deviceModel": "Pixel_6_API_33",
            "registeredAt": "2025-11-04T15:30:22Z"
        }
    ]
}
```

## 📊 Creating a Device Inventory

### Recommended Approach:

Create a simple spreadsheet to track all your test devices:

```csv
Device Name,Machine,Application ID,Android Device ID,Last Updated
Emulator A,MacBook Pro M1,5fbb5562-77cc-44b6-907b-9506725360f2,00e04c7c7b1268d4,2025-11-04
Emulator B,iMac,a1b2c3d4-e5f6-7890-abcd-ef1234567890,1234567890abcdef,2025-11-04
Pixel 6,Physical Device,f9e8d7c6-b5a4-3210-9876-543210fedcba,9876543210fedcba,2025-11-04
```

### Or use a simple script:

```bash
#!/bin/bash
# save as: list_all_devices.sh

echo "BuzzIn Application IDs Report"
echo "=============================="
echo ""

for device in $(adb devices | grep -v "List" | awk '{print $1}'); do
    if [ "$device" != "" ]; then
        echo "Device: $device"
        adb -s $device shell getprop ro.product.model
        adb -s $device logcat -d | grep "Application ID" | tail -1
        echo "---"
    fi
done
```

## 🔄 When Application IDs Change

Application IDs will **change** in these scenarios:

1. ✅ **App is uninstalled and reinstalled** - New ID generated
2. ✅ **App data is cleared** - New ID generated
3. ✅ **Factory reset on device** - New ID generated
4. ❌ **App is updated** - ID persists
5. ❌ **Device is restarted** - ID persists
6. ❌ **App is closed and reopened** - ID persists

## 🔗 Linking Devices to Users

Once a user logs in, all their devices are linked via User ID:

```
User: john@example.com (user_12345)
├── Device 1: 5fbb5562-77cc-44b6-907b-9506725360f2 (iPhone)
├── Device 2: a1b2c3d4-e5f6-7890-abcd-ef1234567890 (iPad)
└── Device 3: f9e8d7c6-b5a4-3210-9876-543210fedcba (Android)
```

Query all devices for a user:
```bash
aws dynamodb query \
    --table-name BuzzInDevices \
    --index-name UserIdIndex \
    --key-condition-expression "userId = :userId" \
    --expression-attribute-values '{":userId":{"S":"user_12345"}}'
```

## 📝 Best Practices

1. **Label your emulators** in Android Studio/AVD Manager with descriptive names
2. **Document Application IDs** immediately after first launch
3. **Use the Settings screen** - easiest and most reliable method
4. **Keep a device inventory** - especially important for team development
5. **Register devices with backend** - enables centralized tracking
6. **Take screenshots** of Settings screen for reference
7. **Use consistent naming** - e.g., "Dev-MacBook-Emulator-1"

## 🆘 Troubleshooting

### Problem: Can't see Application ID in Settings
**Solution:** Make sure you're on the latest version of the app. Pull latest code and rebuild.

### Problem: Application ID is different after app restart
**Solution:** This shouldn't happen. If it does:
1. Check if app data was cleared
2. Check SharedPreferences file
3. Look for errors in logcat

### Problem: Need to track 10+ devices
**Solution:** 
1. Register all devices with backend
2. Use backend database queries to list all devices
3. Create a simple admin dashboard

### Problem: Lost track of which device has which ID
**Solution:**
1. Open Settings on each device
2. The device model is shown (e.g., "BasicAppEmulator", "Pixel 6")
3. Cross-reference with your physical devices

## 📱 Quick Reference Card

```
┌─────────────────────────────────────────────────┐
│  How to Find Your Application ID                │
├─────────────────────────────────────────────────┤
│                                                  │
│  1. Open BuzzIn App                             │
│  2. Tap "Settings" (bottom nav)                 │
│  3. Look for "Application ID"                   │
│  4. Tap [📋] to copy                            │
│                                                  │
│  OR                                              │
│                                                  │
│  adb logcat | grep "Application ID"             │
│                                                  │
└─────────────────────────────────────────────────┘
```

## 🎓 Example Workflow

### Scenario: Setting up 3 test devices

```bash
# Step 1: Launch first emulator
emulator -avd Emulator_1 &

# Step 2: Install app
./gradlew installDebug

# Step 3: Get Application ID
adb logcat | grep "Application ID"
# Copy: 5fbb5562-77cc-44b6-907b-9506725360f2

# Step 4: Document it
echo "Emulator_1: 5fbb5562-77cc-44b6-907b-9506725360f2" >> device_ids.txt

# Step 5: Repeat for other emulators
emulator -avd Emulator_2 &
# ... repeat steps 2-4
```

## 🔐 Security Note

**Application IDs are NOT secret.** They are used for tracking and context management, not authentication. Never use Application ID alone for authorization decisions in your backend.

## 📚 Related Documentation

- [DEVICE_ID_SYSTEM.md](DEVICE_ID_SYSTEM.md) - Complete technical documentation
- [BACKEND_INTEGRATION_EXAMPLE.md](BACKEND_INTEGRATION_EXAMPLE.md) - Backend integration guide
- [README.md](README.md) - Project setup and overview

