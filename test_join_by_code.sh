#!/bin/bash

echo "Testing 5-digit Join Code Functionality"
echo "======================================="

BASE_URL="http://localhost:8080"

# Test 1: Login to get authentication token
echo -e "\n1. Logging in to get authentication token:"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "testpass"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.id')
echo "Token obtained: ${TOKEN:0:20}..."
echo "User ID: $USER_ID"

# Test 2: Create a private league (should generate 5-digit code)
echo -e "\n2. Creating a private league (should generate 5-digit join code):"
CREATE_LEAGUE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Private League with Join Code", "type": "PRIVATE"}')

echo "League creation response:"
echo $CREATE_LEAGUE_RESPONSE | jq .

JOIN_CODE=$(echo $CREATE_LEAGUE_RESPONSE | jq -r '.joinCode')
INVITE_CODE=$(echo $CREATE_LEAGUE_RESPONSE | jq -r '.inviteCode')
echo "Generated 5-digit join code: $JOIN_CODE"
echo "Generated invite code: $INVITE_CODE"

# Test 3: Create another user to test joining
echo -e "\n3. Creating another user to test joining:"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joinuser",
    "email": "joinuser@example.com",
    "password": "testpass",
    "favouriteTeamId": 1,
    "nationality": "USA"
  }')

echo "Registration response:"
echo $REGISTER_RESPONSE | jq .

# Test 4: Login with the new user
echo -e "\n4. Logging in with the new user:"
LOGIN2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "joinuser", "password": "testpass"}')

USER2_ID=$(echo $LOGIN2_RESPONSE | jq -r '.user.id')
echo "New user ID: $USER2_ID"

# Test 5: Create fantasy team for new user
echo -e "\n5. Creating fantasy team for new user:"
TEAM_RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/team" \
  -H "Authorization: Bearer $(echo $LOGIN2_RESPONSE | jq -r '.token')" \
  -H "Content-Type: application/json" \
  -d "{\"teamName\": \"Join Test Team\", \"ownerUserId\": $USER2_ID}")

echo "Team creation response:"
echo $TEAM_RESPONSE | jq .

# Test 6: Join league using 5-digit code
echo -e "\n6. Joining league using 5-digit join code:"
JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/join-by-code?joinCode=$JOIN_CODE&userId=$USER2_ID" \
  -H "Authorization: Bearer $(echo $LOGIN2_RESPONSE | jq -r '.token')")

echo "Join response:"
echo $JOIN_RESPONSE | jq .

# Test 7: Try to join again (should fail - already joined)
echo -e "\n7. Trying to join again (should fail - already joined):"
JOIN_AGAIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/join-by-code?joinCode=$JOIN_CODE&userId=$USER2_ID" \
  -H "Authorization: Bearer $(echo $LOGIN2_RESPONSE | jq -r '.token')")

echo "Second join attempt response:"
echo $JOIN_AGAIN_RESPONSE | jq .

# Test 8: Try to join with invalid code (should fail)
echo -e "\n8. Trying to join with invalid code (should fail):"
INVALID_JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/join-by-code?joinCode=99999&userId=$USER2_ID" \
  -H "Authorization: Bearer $(echo $LOGIN2_RESPONSE | jq -r '.token')")

echo "Invalid code join response:"
echo $INVALID_JOIN_RESPONSE | jq .

echo -e "\n======================================="
echo "5-digit join code functionality test completed!"