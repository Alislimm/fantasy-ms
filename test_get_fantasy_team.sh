#!/bin/bash

echo "Testing /api/user/{userId}/fantasy-team endpoint..."

echo "1. Testing user ID 1 (should have fantasy team):"
curl -X GET http://localhost:8080/api/user/1/fantasy-team \
  -H "Content-Type: application/json" \
  -v

echo -e "\n\n2. Testing user ID 2 (should not have fantasy team):"
curl -X GET http://localhost:8080/api/user/2/fantasy-team \
  -H "Content-Type: application/json" \
  -v

echo -e "\n\n3. Testing non-existent user ID 999:"
curl -X GET http://localhost:8080/api/user/999/fantasy-team \
  -H "Content-Type: application/json" \
  -v

echo -e "\n\nTest completed."