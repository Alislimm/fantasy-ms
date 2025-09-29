#!/bin/bash

echo "Testing Private League Auto-Join Functionality"
echo "=============================================="

BASE_URL="http://localhost:8080"

# Test 1: Try to join league without authentication (should fail)
echo -e "\n1. Testing auto-join without authentication (should fail):"
curl -s -X GET "$BASE_URL/api/invitations/join/test-invite-code" | jq .

# Test 2: Login first to get token
echo -e "\n2. Logging in to get authentication token:"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "testpass"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "Token obtained: ${TOKEN:0:20}..."

# Test 3: Try to join league with authentication but invalid code (should fail)
echo -e "\n3. Testing auto-join with invalid invite code (should fail):"
curl -s -X GET "$BASE_URL/api/invitations/join/invalid-code" \
  -H "Authorization: Bearer $TOKEN" | jq .

# Test 4: Create a test private league first
echo -e "\n4. Creating a test private league:"
CREATE_LEAGUE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/leagues" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Private League", "type": "PRIVATE"}')

INVITE_CODE=$(echo $CREATE_LEAGUE_RESPONSE | jq -r '.inviteCode')
echo "Created league with invite code: $INVITE_CODE"

# Test 5: Try to join the league with valid code (should succeed)
echo -e "\n5. Testing auto-join with valid invite code (should succeed):"
curl -s -X GET "$BASE_URL/api/invitations/join/$INVITE_CODE" \
  -H "Authorization: Bearer $TOKEN" | jq .

echo -e "\n=============================================="
echo "Auto-join functionality test completed!"