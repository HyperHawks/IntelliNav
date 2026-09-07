#!/bin/bash
set -e

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

echo "Git Workflow complete."
