#!/bin/bash

echo "Testing /api/user/{userId}/has-fantasy-team endpoint..."

# Test user that should have a fantasy team (user ID 1 from previous tests)
echo -e "\n1. Testing user ID 1 (should have fantasy team):"
curl -X GET http://localhost:8080/api/user/1/has-fantasy-team \
  -H "Content-Type: application/json" \
  -v

# Test user that doesn't exist (user ID 999)
echo -e "\n\n2. Testing non-existent user ID 999:"
curl -X GET http://localhost:8080/api/user/999/has-fantasy-team \
  -H "Content-Type: application/json" \
  -v

# Test user that exists but doesn't have a fantasy team (user ID 2 if it exists)
echo -e "\n\n3. Testing user ID 2 (may not have fantasy team):"
curl -X GET http://localhost:8080/api/user/2/has-fantasy-team \
  -H "Content-Type: application/json" \
  -v

echo -e "\n\nTest completed."