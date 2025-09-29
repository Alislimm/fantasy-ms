#!/bin/bash

echo "Testing New League APIs"
echo "======================"

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

# Test 2: Create a league with new API (using userId as path parameter)
echo -e "\n2. Creating a league using new API with userId as path parameter:"
CREATE_LEAGUE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test New API League"}')

echo "League creation response:"
echo $CREATE_LEAGUE_RESPONSE | jq .

LEAGUE_ID=$(echo $CREATE_LEAGUE_RESPONSE | jq -r '.id')
echo "Created league with ID: $LEAGUE_ID"

# Test 3: Get all leagues joined by user
echo -e "\n3. Getting all leagues joined by user:"
USER_LEAGUES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/leagues/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "User leagues response:"
echo $USER_LEAGUES_RESPONSE | jq .

# Test 4: Get detailed league information with rankings
echo -e "\n4. Getting detailed league information with rankings:"
LEAGUE_DETAILS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/leagues/$LEAGUE_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "League details response:"
echo $LEAGUE_DETAILS_RESPONSE | jq .

# Test 5: Create another user and team to test rankings
echo -e "\n5. Creating another user to test rankings:"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "testuser2@example.com",
    "password": "testpass",
    "favouriteTeamId": 1,
    "nationality": "USA"
  }')

echo "Registration response:"
echo $REGISTER_RESPONSE | jq .

# Test 6: Login with second user
echo -e "\n6. Logging in with second user:"
LOGIN2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser2", "password": "testpass"}')

TOKEN2=$(echo $LOGIN2_RESPONSE | jq -r '.token')
USER2_ID=$(echo $LOGIN2_RESPONSE | jq -r '.user.id')
echo "Second user ID: $USER2_ID"

# Test 7: Create team for second user
echo -e "\n7. Creating fantasy team for second user:"
TEAM2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/team" \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d "{\"teamName\": \"Second Test Team\", \"ownerUserId\": $USER2_ID}")

echo "Second team creation response:"
echo $TEAM2_RESPONSE | jq .

# Test 8: Join league with second user using join-by-code
JOIN_CODE=$(echo $CREATE_LEAGUE_RESPONSE | jq -r '.joinCode // empty')
if [ ! -z "$JOIN_CODE" ] && [ "$JOIN_CODE" != "null" ]; then
    echo -e "\n8. Second user joining league using join code:"
    JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues/join-by-code?joinCode=$JOIN_CODE&userId=$USER2_ID" \
      -H "Authorization: Bearer $TOKEN2")
    
    echo "Join response:"
    echo $JOIN_RESPONSE | jq .
    
    # Test 9: Check updated league details with both users
    echo -e "\n9. Getting updated league details with multiple teams:"
    UPDATED_LEAGUE_DETAILS=$(curl -s -X GET "$BASE_URL/api/leagues/$LEAGUE_ID" \
      -H "Authorization: Bearer $TOKEN")
    
    echo "Updated league details response:"
    echo $UPDATED_LEAGUE_DETAILS | jq .
fi

# Test 10: Test get user leagues for second user
echo -e "\n10. Getting leagues for second user:"
USER2_LEAGUES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/leagues/user/$USER2_ID" \
  -H "Authorization: Bearer $TOKEN2")

echo "Second user leagues response:"
echo $USER2_LEAGUES_RESPONSE | jq .

echo -e "\n======================"
echo "New League APIs test completed!"