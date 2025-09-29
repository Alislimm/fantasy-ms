#!/bin/bash

# Test script to reproduce the NoResourceFoundException issue

echo "Testing the problematic endpoint: /api/users/3/team"
echo "Expected: NoResourceFoundException"

# Make the request that's causing the issue
curl -X GET "http://localhost:8080/api/users/3/team" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -v

echo ""
echo "Testing the correct endpoint: /api/user/3/fantasy-team"
echo "Expected: Should work (if user exists)"

# Make the request to the correct endpoint
curl -X GET "http://localhost:8080/api/user/3/fantasy-team" \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -v