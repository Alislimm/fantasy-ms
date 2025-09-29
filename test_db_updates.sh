#!/bin/bash

# Test script to reproduce database update issues
echo "Testing database updates for GameWeek operations..."

BASE_URL="http://localhost:8080/api"

# Test 1: Create a new gameweek
echo "1. Creating a new gameweek..."
GAMEWEEK_RESPONSE=$(curl -s -X POST "$BASE_URL/gameweek" \
  -H "Content-Type: application/json" \
  -d '{
    "number": 99,
    "startDate": "2025-09-21",
    "endDate": "2025-09-27",
    "status": "UPCOMING"
  }')

echo "GameWeek creation response: $GAMEWEEK_RESPONSE"

# Extract gameweek ID
GAMEWEEK_ID=$(echo $GAMEWEEK_RESPONSE | grep -o '"id":[0-9]*' | cut -d: -f2)
echo "Created GameWeek ID: $GAMEWEEK_ID"

if [ -z "$GAMEWEEK_ID" ]; then
    echo "Failed to create gameweek or extract ID"
    exit 1
fi

# Test 2: Verify gameweek was saved
echo "2. Verifying gameweek was saved..."
VERIFY_RESPONSE=$(curl -s -X GET "$BASE_URL/gameweek/$GAMEWEEK_ID")
echo "GameWeek verification response: $VERIFY_RESPONSE"

# Test 3: Add a fixture to the gameweek
echo "3. Adding a fixture to gameweek..."
FIXTURE_RESPONSE=$(curl -s -X POST "$BASE_URL/gameweek/$GAMEWEEK_ID/fixtures" \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "kickoff": "2025-09-22T15:30:00Z",
    "venue": "Test Stadium"
  }')

echo "Fixture creation response: $FIXTURE_RESPONSE"

# Test 4: Verify fixture was saved
echo "4. Verifying fixture was saved..."
FIXTURES_RESPONSE=$(curl -s -X GET "$BASE_URL/gameweek/$GAMEWEEK_ID/fixtures")
echo "Fixtures verification response: $FIXTURES_RESPONSE"

# Test 5: Get all gameweeks to verify persistence
echo "5. Getting all gameweeks..."
ALL_GAMEWEEKS=$(curl -s -X GET "$BASE_URL/gameweek")
echo "All gameweeks response: $ALL_GAMEWEEKS"

echo "Test completed. Check responses above for any issues."