# 🐳 BuzzIn Docker Setup Guide

Easy Docker-based deployment for sharing the BuzzIn app between developers.

---

## 🚀 Quick Start (2 Commands)

```bash
# 1. Build and start the server
docker-compose up --build

# 2. Download APK from any device on your network
# Go to: http://YOUR_IP:8080/debug/app-debug.apk
```

That's it! The APK is now available to download.

---

## 📋 Prerequisites

- **Docker Desktop** installed ([download here](https://www.docker.com/products/docker-desktop))
- **Docker Compose** (included with Docker Desktop)
- Git repository access

---

## 🛠️ Setup for Developers

### First Time Setup

```bash
# 1. Clone the repository
git clone git@github.com:rsatyavolu-bmbl/hackathon-buzzIn.git
cd hackathon-buzzIn

# 2. Build and run
docker-compose up --build
```

**Wait for build to complete** (~5-10 minutes first time, then cached)

### Subsequent Runs

```bash
# Start server (using cached build)
docker-compose up

# Or run in background
docker-compose up -d
```

---

## 📱 How to Download APK

### Option 1: From Mobile Device (Recommended)

1. **Get your computer's IP address:**
   ```bash
   # On Mac/Linux
   ifconfig | grep "inet " | grep -v 127.0.0.1

   # On Windows
   ipconfig
   ```

2. **On Android device:**
   - Connect to same WiFi network
   - Open browser
   - Go to: `http://YOUR_IP:8080/debug/app-debug.apk`
   - Download and install

### Option 2: From Browser

Open in any browser on your network:
```
http://localhost:8080/debug/app-debug.apk     # From same computer
http://YOUR_IP:8080/debug/app-debug.apk       # From other devices
```

---

## 🎯 Docker Commands Reference

### Basic Operations

```bash
# Start server
docker-compose up

# Start in background (detached)
docker-compose up -d

# Stop server
docker-compose down

# Rebuild after code changes
docker-compose up --build

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### Advanced Operations

```bash
# Rebuild without cache (clean build)
docker-compose build --no-cache

# Remove all containers and volumes
docker-compose down -v

# Run specific service
docker-compose up buzzin-builder

# Execute command in running container
docker-compose exec buzzin-builder bash

# Build APK only (no server)
docker-compose run --rm buzzin-builder ./gradlew assembleDebug
```

---

## 📂 Project Structure

```
hackathon-buzzIn/
├── Dockerfile                 # Android build environment
├── docker-compose.yml         # Service orchestration
├── .dockerignore             # Files to exclude from Docker
├── docker/
│   └── nginx.conf            # Optional nginx config
├── app/
│   └── build/outputs/apk/    # Built APKs (mounted volume)
└── DOCKER_SETUP.md           # This file
```

---

## 🔧 Configuration

### Change Port

Edit `docker-compose.yml`:
```yaml
ports:
  - "9090:8080"  # Use port 9090 instead
```

### Use Nginx Instead (Production)

```bash
# Start with nginx profile
docker-compose --profile production up

# Access via:
# http://YOUR_IP:80/debug/app-debug.apk
```

---

## 🐛 Troubleshooting

### "Cannot connect to Docker daemon"
**Solution:** Start Docker Desktop

```bash
# Check Docker is running
docker ps
```

### "Port 8080 already in use"
**Solution:** Stop other services or change port

```bash
# Find what's using port 8080
lsof -i :8080

# Kill it
kill <PID>

# Or change port in docker-compose.yml
```

### "Build failed"
**Solution:** Clean rebuild

```bash
# Clean everything
docker-compose down -v
docker system prune -a

# Rebuild
docker-compose up --build
```

### "Can't download from phone"
**Solutions:**

1. **Check same WiFi network**
   ```bash
   # Verify your IP
   ifconfig | grep inet
   ```

2. **Check firewall**
   - Allow Docker in firewall settings
   - Test from browser on same computer first

3. **Use correct URL**
   ```
   ✅ http://192.168.1.100:8080/debug/app-debug.apk
   ❌ http://localhost:8080/debug/app-debug.apk  (won't work from phone)
   ```

### Gradle build errors
**Solution:** Clear Gradle cache

```bash
# Remove gradle cache volume
docker-compose down -v

# Rebuild
docker-compose up --build
```

---

## 💡 Advanced Usage

### Build Specific Variant

```bash
# Build release APK
docker-compose run --rm buzzin-builder ./gradlew assembleRelease

# APK will be at: app/build/outputs/apk/release/app-release.apk
```

### Run Tests

```bash
# Unit tests
docker-compose run --rm buzzin-builder ./gradlew test

# Android tests (requires emulator)
docker-compose run --rm buzzin-builder ./gradlew connectedAndroidTest
```

### Access Build Artifacts

```bash
# Copy APK out of container
docker cp buzzin-apk-server:/app/app/build/outputs/apk/debug/app-debug.apk ./BuzzIn.apk

# Or just use the mounted volume
ls app/build/outputs/apk/debug/
```

---

## 🌐 Sharing with Remote Team

### Option 1: Use ngrok (Tunnel)

```bash
# Install ngrok
brew install ngrok

# Start Docker server
docker-compose up -d

# Create tunnel
ngrok http 8080

# Share the ngrok URL with team
# Example: https://abc123.ngrok.io/debug/app-debug.apk
```

### Option 2: Cloud Hosting

Deploy to cloud platform:

```bash
# Google Cloud Run
gcloud run deploy buzzin-apk --source .

# AWS ECS
# (Configure ECS task definition)

# Azure Container Instances
az container create --resource-group mygroup --name buzzin-apk
```

---

## 📊 Comparison: Docker vs Other Methods

| Method | Setup Time | Consistency | Team Sharing | Dependencies |
|--------|------------|-------------|--------------|--------------|
| **Docker** | 2 min | ✅ Perfect | ✅ Easy | Just Docker |
| Local Build | 30 min | ⚠️ Varies | ❌ Complex | Android SDK, Java |
| Android Studio | 1 hour | ⚠️ Varies | ❌ Manual | Full IDE |
| CI/CD | 2 hours | ✅ Perfect | ✅ Easy | CI setup |

---

## 🎓 For New Developers

**Complete setup from scratch:**

1. **Install Docker Desktop**
   - Download: https://www.docker.com/products/docker-desktop
   - Install and start Docker Desktop

2. **Clone repository**
   ```bash
   git clone <repo-url>
   cd hackathon-buzzIn
   ```

3. **Start server**
   ```bash
   docker-compose up
   ```

4. **Get download link**
   ```bash
   # Get your IP
   ifconfig | grep "inet " | grep -v 127.0.0.1

   # URL format
   echo "http://YOUR_IP:8080/debug/app-debug.apk"
   ```

5. **Download on phone**
   - Connect to same WiFi
   - Open browser
   - Enter URL
   - Install APK

**Done! You're ready to develop.** 🎉

---

## 🔐 Security Notes

- **Local network only** - Server only accessible on your LAN
- **No authentication** - Fine for development, not for production
- **Firewall friendly** - Docker handles port forwarding
- **Isolated environment** - Build happens in container, not on your machine

---

## 📝 Tips & Best Practices

### Keep Docker Running Efficiently

```bash
# Clean up old images/containers periodically
docker system prune -a

# Keep only what you need
docker-compose down -v  # When done for the day
```

### Speed Up Builds

```bash
# Use Gradle daemon (already configured)
# Builds are cached in Docker volume

# Parallel builds (in build.gradle)
org.gradle.parallel=true
org.gradle.caching=true
```

### Monitor Resource Usage

```bash
# Check Docker resource usage
docker stats

# Check disk usage
docker system df
```

---

## 🚀 Next Steps

1. **Customize build** - Edit `Dockerfile` for your needs
2. **Add CI/CD** - Integrate with GitHub Actions
3. **Set up staging** - Create separate environments
4. **Add monitoring** - Track downloads and usage

---

## 📞 Support

**Issues?**
- Check logs: `docker-compose logs -f`
- Rebuild: `docker-compose up --build`
- Clean slate: `docker-compose down -v && docker-compose up --build`

**Need help?**
- Docker docs: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/

---

## ✅ Summary

**For developers to get started:**
```bash
docker-compose up
# Then download from: http://YOUR_IP:8080/debug/app-debug.apk
```

**That's it!** No SDK installation, no environment setup, just Docker. 🐳
