#!/bin/bash

echo "Testing Fantasy App Error Logging"
echo "================================="

# Base URL for the API
BASE_URL="http://localhost:8080/api"

echo "1. Testing 404 Not Found error (invalid endpoint)..."
curl -s -X GET "${BASE_URL}/invalid-endpoint" -w "HTTP Status: %{http_code}\n" | head -5

echo -e "\n2. Testing 400 Bad Request error (invalid JSON)..."
curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"invalid": json}' \
  -w "HTTP Status: %{http_code}\n" | head -5

echo -e "\n3. Testing 400 Validation error (missing fields)..."
curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": ""}' \
  -w "HTTP Status: %{http_code}\n" | head -5

echo -e "\n4. Testing 401 Unauthorized error (no auth token)..."
curl -s -X POST "${BASE_URL}/user/team" \
  -H "Content-Type: application/json" \
  -d '{"teamName": "Test Team", "ownerUserId": 1}' \
  -w "HTTP Status: %{http_code}\n" | head -5

echo -e "\n5. Testing 404 Not Found error (non-existent player)..."
curl -s -X GET "${BASE_URL}/players/999999" -w "HTTP Status: %{http_code}\n" | head -5

echo -e "\nError logging tests completed!"
echo "Check the console output and logs/fantasy-app.log file for detailed error logs."
echo "Each error should have:"
echo "- Unique ERROR_ID"
echo "- Timestamp"
echo "- Request information (URI, method, IP, user agent)"
echo "- Clear error message"