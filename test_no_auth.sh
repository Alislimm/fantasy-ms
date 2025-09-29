#!/bin/bash

# Test script to verify APIs work without authentication

echo "Testing APIs without authentication tokens..."
echo "=========================================="

# Test a previously restricted endpoint
echo "1. Testing /api/user/1/has-fantasy-team (should work without auth):"
curl -X GET "http://localhost:8080/api/user/1/has-fantasy-team" \
  -H "Accept: application/json" \
  -w "Status: %{http_code}\n" \
  -s

echo ""

# Test another endpoint that would have required auth
echo "2. Testing /api/users/1/team (should work without auth):"
curl -X GET "http://localhost:8080/api/users/1/team" \
  -H "Accept: application/json" \
  -w "Status: %{http_code}\n" \
  -s

echo ""

# Test admin endpoint (should also work without auth now)
echo "3. Testing /api/admin/users (should work without auth):"
curl -X GET "http://localhost:8080/api/admin/users" \
  -H "Accept: application/json" \
  -w "Status: %{http_code}\n" \
  -s

echo ""
echo "All tests completed. All endpoints should return 2xx status codes (not 401/403)."