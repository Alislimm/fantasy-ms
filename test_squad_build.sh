#!/bin/bash

echo "Testing /api/user/squad/build endpoint..."

# Test the exact request from the issue description
curl -X POST http://localhost:8080/api/user/squad/build \
  -H "Content-Type: application/json" \
  -d '{
    "teamName": "KingossTeam",
    "ownerUserId": 1,
    "starters": [2, 3, 5, 6, 4],
    "bench": [7, 8, 9],
    "captainPlayerId": 2,
    "viceCaptainPlayerId": 3
  }' \
  -v

echo -e "\n\nTest completed."