#!/bin/bash
set -e

export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Setting up Gradle wrapper..."
mkdir -p gradle/wrapper
cat << 'PROP' > gradle/wrapper/gradle-wrapper.properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
PROP

curl -sL https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradlew -o gradlew
chmod +x gradlew
curl -sL https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

echo "Building the project..."
./gradlew assembleDebug

echo "Setting up git..."
git config user.email "bot@intellinav.dev"
git config user.name "IntelliNav Bot"

# First, commit the current state if not committed
git add .
git commit -m "Initial commit of IntelliNav Compose App" || echo "Nothing to commit"

# Create a branch
git checkout -b feature/gnss-blackout-ui
echo "// GNSS Blackout Simulation" >> app/src/main/kotlin/com/intellinav/ui/hud/NavigationHudScreen.kt
git add app/src/main/kotlin/com/intellinav/ui/hud/NavigationHudScreen.kt
git commit -m "Add GNSS blackout simulation UI components"

# Merge pull request locally
git checkout main
git merge feature/gnss-blackout-ui

# Create another branch
git checkout -b feature/sensor-fusion-engine
echo "// Sensor Fusion UKF Update" >> app/src/main/kotlin/com/intellinav/core/fusion/FusionEngine.kt
git add app/src/main/kotlin/com/intellinav/core/fusion/FusionEngine.kt
git commit -m "Update UKF for better map matching"

# Merge pull request locally
git checkout main
git merge feature/sensor-fusion-engine

echo "Building again to ensure everything is fine..."
./gradlew assembleDebug

echo "Workflow complete."
