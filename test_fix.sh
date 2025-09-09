#!/bin/bash

echo "Testing the fix for circular reference in POST /api/user/team"
echo "============================================================"

# Start with a clean slate - register a new user and get token
echo "1. Registering a test user..."
curl -s -X POST http://localhost:8080/api/user/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testfix","email":"testfix@example.com","password":"password"}' > /dev/null

echo "2. Logging in to get JWT token..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail":"testfix","password":"password"}' | jq -r .token)

if [ "$TOKEN" = "null" ]; then
    echo "Failed to get token - check if app is running"
    exit 1
fi

echo "3. Creating fantasy team (this was causing infinite loop)..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/user/team \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"teamName":"TestTeam","ownerUserId":1}')

echo "4. Response (should be finite, no infinite loop):"
echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"

# Check if response contains repetitive nested structures
if echo "$RESPONSE" | grep -q '"owner".*"fantasyTeams".*"owner".*"fantasyTeams"'; then
    echo "ERROR: Still contains circular reference!"
    exit 1
else
    echo "SUCCESS: No circular reference detected!"
fi