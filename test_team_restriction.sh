#!/bin/bash

echo "Testing fantasy team restriction..."

# First, register a test user
echo "Registering test user..."
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_restriction",
    "email": "testuser_restriction@example.com",
    "password": "password123"
  }'

echo -e "\n"

# Login to get user info
echo "Logging in to get user ID..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser_restriction",
    "password": "password123"
  }')

echo "Login response: $LOGIN_RESPONSE"

# Extract user ID (assuming JSON format with id field)
USER_ID=$(echo $LOGIN_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "User ID: $USER_ID"

if [ -z "$USER_ID" ]; then
    echo "Failed to get user ID, exiting..."
    exit 1
fi

echo -e "\n"

# Try to create first team (should succeed)
echo "Creating first fantasy team (should succeed)..."
TEAM1_RESPONSE=$(curl -s -X POST http://localhost:8080/api/user/team \
  -H "Content-Type: application/json" \
  -d "{
    \"teamName\": \"Test Team 1\",
    \"ownerUserId\": $USER_ID
  }")

echo "First team creation response: $TEAM1_RESPONSE"

echo -e "\n"

# Try to create second team (should fail)
echo "Creating second fantasy team (should fail with validation error)..."
TEAM2_RESPONSE=$(curl -s -X POST http://localhost:8080/api/user/team \
  -H "Content-Type: application/json" \
  -d "{
    \"teamName\": \"Test Team 2\",
    \"ownerUserId\": $USER_ID
  }")

echo "Second team creation response: $TEAM2_RESPONSE"

echo -e "\nTest completed!"