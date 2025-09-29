#!/bin/bash

# Test script for gameweek fixtures functionality
echo "Testing gameweek fixtures functionality..."

BASE_URL="http://localhost:8080/api"

# Test 1: Initialize gameweek 1 with 5 random matches
echo "1. Initializing gameweek 1 with 5 random matches..."
INIT_RESPONSE=$(curl -s -X POST "$BASE_URL/gameweek/initialize/gameweek-1" \
  -H "Content-Type: application/json")

echo "Initialize gameweek 1 response: $INIT_RESPONSE"

# Extract gameweek ID
GAMEWEEK_ID=$(echo $INIT_RESPONSE | grep -o '"id":[0-9]*' | cut -d: -f2 | head -1)
echo "GameWeek ID: $GAMEWEEK_ID"

if [ -z "$GAMEWEEK_ID" ]; then
    echo "Failed to initialize gameweek 1 or extract ID"
    exit 1
fi

# Test 2: Get formatted fixtures for gameweek 1
echo "2. Getting formatted fixtures for gameweek 1..."
FORMATTED_RESPONSE=$(curl -s -X GET "$BASE_URL/gameweek/$GAMEWEEK_ID/fixtures/formatted")
echo "Formatted fixtures response: $FORMATTED_RESPONSE"

# Test 3: Get regular fixtures for gameweek 1 (for comparison)
echo "3. Getting regular fixtures for gameweek 1..."
REGULAR_RESPONSE=$(curl -s -X GET "$BASE_URL/gameweek/$GAMEWEEK_ID/fixtures")
echo "Regular fixtures response: $REGULAR_RESPONSE"

# Test 4: Verify we have exactly 5 matches
echo "4. Verifying match count..."
MATCH_COUNT=$(echo $FORMATTED_RESPONSE | grep -o '\[' | wc -l)
echo "Number of matches found: $MATCH_COUNT"

if [ "$MATCH_COUNT" -eq 5 ]; then
    echo "✓ Correct number of matches (5)"
else
    echo "✗ Incorrect number of matches. Expected 5, got $MATCH_COUNT"
fi

# Test 5: Verify format is correct (should be [TeamA,TeamB] format)
echo "5. Verifying format..."
if echo "$FORMATTED_RESPONSE" | grep -q '\[.*,.*\]'; then
    echo "✓ Format is correct [TeamA,TeamB]"
else
    echo "✗ Format is incorrect"
fi

echo "Test completed!"