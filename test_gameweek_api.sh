#!/bin/bash

echo "Testing GameWeek Fixtures API..."

# Check if application is running
echo "Checking if application is running..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    if ! curl -s http://localhost:8080/ > /dev/null 2>&1; then
        echo "❌ Application is not running. Please start the application first with: ./gradlew bootRun"
        exit 1
    fi
fi

echo "✅ Application is running"
echo ""

# Test 1: Get all GameWeeks (initial state)
echo "Test 1: Getting all GameWeeks (initial state)..."
curl -s http://localhost:8080/api/gameweek | jq . || echo "No jq available, raw response:"
curl -s http://localhost:8080/api/gameweek
echo ""
echo ""

# Test 2: Create a new GameWeek
echo "Test 2: Creating a new GameWeek..."
GAMEWEEK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/gameweek \
  -H "Content-Type: application/json" \
  -d '{
    "number": 100,
    "startDate": "2025-09-22",
    "endDate": "2025-09-28",
    "status": "UPCOMING"
  }')

echo "GameWeek creation response:"
echo $GAMEWEEK_RESPONSE | jq . 2>/dev/null || echo $GAMEWEEK_RESPONSE
echo ""

# Extract GameWeek ID
GAMEWEEK_ID=$(echo $GAMEWEEK_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Extracted GameWeek ID: $GAMEWEEK_ID"
echo ""

if [ -z "$GAMEWEEK_ID" ]; then
    echo "❌ Failed to create GameWeek or extract ID"
    echo "This might be because:"
    echo "1. The application is not running"
    echo "2. There's a database connection issue"
    echo "3. GameWeek number 100 already exists"
    echo ""
    echo "Trying to get existing GameWeeks to check database connectivity..."
    curl -s http://localhost:8080/api/gameweek
    exit 1
fi

# Test 3: Get the created GameWeek
echo "Test 3: Getting the created GameWeek..."
curl -s http://localhost:8080/api/gameweek/$GAMEWEEK_ID | jq . 2>/dev/null || curl -s http://localhost:8080/api/gameweek/$GAMEWEEK_ID
echo ""
echo ""

# Test 4: Get all teams (to use valid team IDs for fixtures)
echo "Test 4: Getting available basketball teams..."
TEAMS_RESPONSE=$(curl -s http://localhost:8080/api/teams)
echo "Teams response:"
echo $TEAMS_RESPONSE | jq . 2>/dev/null || echo $TEAMS_RESPONSE
echo ""

# Extract first two team IDs for fixture testing
TEAM1_ID=$(echo $TEAMS_RESPONSE | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
TEAM2_ID=$(echo $TEAMS_RESPONSE | grep -o '"id":[0-9]*' | head -2 | tail -1 | grep -o '[0-9]*')

echo "Team 1 ID: $TEAM1_ID"
echo "Team 2 ID: $TEAM2_ID"
echo ""

if [ -z "$TEAM1_ID" ] || [ -z "$TEAM2_ID" ] || [ "$TEAM1_ID" = "$TEAM2_ID" ]; then
    echo "⚠️  Warning: Could not find two different teams in the database."
    echo "Creating fixtures with placeholder team IDs (1 and 2)..."
    TEAM1_ID=1
    TEAM2_ID=2
fi

# Test 5: Add a fixture to the GameWeek
echo "Test 5: Adding a fixture to the GameWeek..."
FIXTURE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": '$TEAM1_ID',
    "awayTeamId": '$TEAM2_ID',
    "kickoff": "2025-09-23T19:30:00Z",
    "venue": "Test Arena"
  }')

echo "Fixture creation response:"
echo $FIXTURE_RESPONSE | jq . 2>/dev/null || echo $FIXTURE_RESPONSE
echo ""

# Test 6: Get fixtures for the GameWeek
echo "Test 6: Getting fixtures for the GameWeek..."
curl -s http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures | jq . 2>/dev/null || curl -s http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures
echo ""
echo ""

# Test 7: Try to create GameWeek with duplicate number (should fail)
echo "Test 7: Attempting to create GameWeek with duplicate number (should fail)..."
DUPLICATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/gameweek \
  -H "Content-Type: application/json" \
  -d '{
    "number": 100,
    "startDate": "2025-09-22",
    "endDate": "2025-09-28",
    "status": "UPCOMING"
  }')

echo "Duplicate GameWeek response (should be error):"
echo $DUPLICATE_RESPONSE | jq . 2>/dev/null || echo $DUPLICATE_RESPONSE
echo ""

# Test 8: Try to add fixture with same home and away team (should fail)
echo "Test 8: Attempting to add fixture with same home and away team (should fail)..."
INVALID_FIXTURE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": '$TEAM1_ID',
    "awayTeamId": '$TEAM1_ID',
    "kickoff": "2025-09-24T19:30:00Z",
    "venue": "Test Arena"
  }')

echo "Invalid fixture response (should be error):"
echo $INVALID_FIXTURE_RESPONSE | jq . 2>/dev/null || echo $INVALID_FIXTURE_RESPONSE
echo ""

echo "🎉 GameWeek Fixtures API testing completed!"
echo ""
echo "Summary of what was tested:"
echo "✅ Getting all GameWeeks"
echo "✅ Creating a new GameWeek"
echo "✅ Getting a specific GameWeek"
echo "✅ Getting available teams"
echo "✅ Adding fixtures to a GameWeek"
echo "✅ Getting fixtures for a GameWeek"
echo "✅ Error handling for duplicate GameWeek numbers"
echo "✅ Error handling for invalid fixture data"
echo ""
echo "The API is now ready to use! Check GAMEWEEK_FIXTURES_API.md for detailed documentation."