#!/bin/bash

# Script to register basketball teams
BASE_URL="http://localhost:8080/api/admin/teams"

echo "Registering basketball teams..."

# Team 1: Hoops
echo "Creating team: Hoops"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Hoops",
    "shortName": "HOO",
    "city": "Beirut"
  }'
echo ""

# Team 2: Sporting Al Mouttahed
echo "Creating team: Sporting Al Mouttahed"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Sporting Al Mouttahed",
    "shortName": "MUT",
    "city": "Tripoli"
  }'
echo ""

# Team 3: Champville
echo "Creating team: Champville"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Champville",
    "shortName": "CHA",
    "city": "Dekweneh"
  }'
echo ""

# Team 4: Atlas
echo "Creating team: Atlas"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Atlas",
    "shortName": "ATL",
    "city": "Zahle"
  }'
echo ""

# Team 5: Antranik
echo "Creating team: Antranik"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Antranik",
    "shortName": "ANT",
    "city": "Beirut"
  }'
echo ""

# Team 6: NSA
echo "Creating team: NSA"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "NSA",
    "shortName": "NSA",
    "city": "Zouk Mikael"
  }'
echo ""

# Team 7: Byblos
echo "Creating team: Byblos"
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Byblos",
    "shortName": "BYB",
    "city": "Byblos"
  }'
echo ""

echo "All teams registered successfully!"