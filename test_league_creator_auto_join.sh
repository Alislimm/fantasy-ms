#!/bin/bash

echo "Testing League Creator Auto-Join Functionality"
echo "=============================================="

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

# Test 2: Ensure user has a fantasy team (create one if needed)
echo -e "\n2. Checking/creating fantasy team:"
TEAM_CHECK_RESPONSE=$(curl -s -X GET "$BASE_URL/api/user/has-fantasy-team" \
  -H "Authorization: Bearer $TOKEN")

echo "Has fantasy team: $TEAM_CHECK_RESPONSE"

if [ "$TEAM_CHECK_RESPONSE" = "false" ]; then
    echo "Creating fantasy team..."
    TEAM_RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/team" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"teamName\": \"Creator Test Team\", \"ownerUserId\": $USER_ID}")
    echo "Team creation response:"
    echo $TEAM_RESPONSE | jq .
fi

# Test 3: Create a public league
echo -e "\n3. Creating a public league (creator should auto-join):"
CREATE_PUBLIC_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Auto-Join Test Public League", "type": "PUBLIC"}')

echo "Public league creation response:"
echo $CREATE_PUBLIC_RESPONSE | jq .

PUBLIC_LEAGUE_ID=$(echo $CREATE_PUBLIC_RESPONSE | jq -r '.id')
echo "Created public league with ID: $PUBLIC_LEAGUE_ID"

# Test 4: Create a private league
echo -e "\n4. Creating a private league (creator should auto-join):"
CREATE_PRIVATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Auto-Join Test Private League", "type": "PRIVATE"}')

echo "Private league creation response:"
echo $CREATE_PRIVATE_RESPONSE | jq .

PRIVATE_LEAGUE_ID=$(echo $CREATE_PRIVATE_RESPONSE | jq -r '.id')
JOIN_CODE=$(echo $CREATE_PRIVATE_RESPONSE | jq -r '.joinCode')
echo "Created private league with ID: $PRIVATE_LEAGUE_ID"
echo "Join code: $JOIN_CODE"

# Test 5: Verify creator auto-join by trying to join again (should fail - already joined)
echo -e "\n5. Testing that creator is already joined (join again should fail):"
JOIN_AGAIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/join-by-code?joinCode=$JOIN_CODE&userId=$USER_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "Join again response (should indicate already joined):"
echo $JOIN_AGAIN_RESPONSE | jq .

# Test 6: Test without fantasy team (create new user without team)
echo -e "\n6. Testing league creation without fantasy team (should fail):"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "noteamuser",
    "email": "noteamuser@example.com",
    "password": "testpass",
    "favouriteTeamId": 1,
    "nationality": "USA"
  }')

echo "New user registration:"
echo $REGISTER_RESPONSE | jq .

# Login with new user
LOGIN2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "noteamuser", "password": "testpass"}')

TOKEN2=$(echo $LOGIN2_RESPONSE | jq -r '.token')

# Try to create league without team
CREATE_NO_TEAM_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues" \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d '{"name": "Should Fail League", "type": "PUBLIC"}')

echo "League creation without team response (should fail):"
echo $CREATE_NO_TEAM_RESPONSE | jq .

echo -e "\n=============================================="
echo "League creator auto-join functionality test completed!"