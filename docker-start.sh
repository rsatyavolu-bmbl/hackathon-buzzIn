#!/bin/bash

# BuzzIn Docker Quick Start Script

set -e

echo "🐳 BuzzIn Docker Setup"
echo "===================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running"
    echo "Please start Docker Desktop and try again"
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Get local IP address
echo "🔍 Finding your IP address..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | awk '{print $2}' | head -1)
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    LOCAL_IP=$(hostname -I | awk '{print $1}')
else
    # Windows/other
    LOCAL_IP="localhost"
fi

echo "📍 Your IP: $LOCAL_IP"
echo ""

# Build and start
echo "🔨 Building and starting server..."
echo "This may take 5-10 minutes on first run..."
echo ""

docker-compose up -d --build

# Wait for server to be ready
echo ""
echo "⏳ Waiting for server to start..."
sleep 5

# Check if server is running
if docker-compose ps | grep -q "Up"; then
    echo ""
    echo "✅ Server is running!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📱 DOWNLOAD APK FROM YOUR PHONE:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Connect your phone to the SAME WiFi network"
    echo "2. Open browser on your phone"
    echo "3. Go to this URL:"
    echo ""
    echo "   👉 http://$LOCAL_IP:8080/debug/app-debug.apk"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📋 Useful commands:"
    echo "  • View logs:     docker-compose logs -f"
    echo "  • Stop server:   docker-compose down"
    echo "  • Restart:       docker-compose restart"
    echo ""
    echo "💡 Share this URL with your team!"
else
    echo "❌ Server failed to start"
    echo "Check logs: docker-compose logs"
    exit 1
fi
