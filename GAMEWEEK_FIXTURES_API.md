# GameWeek Fixtures API Guide

This guide explains how to add new GameWeek fixtures to the database using the REST API.

## Prerequisites

1. Ensure you have basketball teams already created in the database
2. The application should be running on `http://localhost:8080`

## API Endpoints

### 1. Create a new GameWeek

**POST** `/api/gameweek`

**Request Body:**
```json
{
  "number": 1,
  "startDate": "2025-09-22",
  "endDate": "2025-09-28",
  "status": "UPCOMING"
}
```

**Response:** Returns the created GameWeek object with its ID

**Example cURL command:**
```bash
curl -X POST http://localhost:8080/api/gameweek \
  -H "Content-Type: application/json" \
  -d '{
    "number": 1,
    "startDate": "2025-09-22",
    "endDate": "2025-09-28",
    "status": "UPCOMING"
  }'
```

### 2. Add fixtures to a GameWeek

**POST** `/api/gameweek/{gameWeekId}/fixtures`

**Request Body:**
```json
{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "kickoff": "2025-09-23T19:30:00Z",
  "venue": "Madison Square Garden"
}
```

**Response:** Returns the created Match object

**Example cURL command:**
```bash
curl -X POST http://localhost:8080/api/gameweek/1/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "kickoff": "2025-09-23T19:30:00Z",
    "venue": "Madison Square Garden"
  }'
```

### 3. Get all GameWeeks

**GET** `/api/gameweek`

**Example cURL command:**
```bash
curl http://localhost:8080/api/gameweek
```

### 4. Get a specific GameWeek

**GET** `/api/gameweek/{id}`

**Example cURL command:**
```bash
curl http://localhost:8080/api/gameweek/1
```

### 5. Get fixtures for a GameWeek

**GET** `/api/gameweek/{id}/fixtures`

**Example cURL command:**
```bash
curl http://localhost:8080/api/gameweek/1/fixtures
```

## Complete Example: Adding a GameWeek with Fixtures

Here's a complete example of adding a new GameWeek with multiple fixtures:

```bash
#!/bin/bash

# Step 1: Create a new GameWeek
echo "Creating GameWeek 2..."
GAMEWEEK_RESPONSE=$(curl -s -X POST http://localhost:8080/api/gameweek \
  -H "Content-Type: application/json" \
  -d '{
    "number": 2,
    "startDate": "2025-09-29",
    "endDate": "2025-10-05",
    "status": "UPCOMING"
  }')

echo "GameWeek created: $GAMEWEEK_RESPONSE"

# Extract GameWeek ID (assuming JSON response with "id" field)
GAMEWEEK_ID=$(echo $GAMEWEEK_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "GameWeek ID: $GAMEWEEK_ID"

# Step 2: Add multiple fixtures to the GameWeek
echo "Adding fixture 1..."
curl -X POST http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "kickoff": "2025-09-30T19:30:00Z",
    "venue": "Madison Square Garden"
  }'

echo "Adding fixture 2..."
curl -X POST http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 3,
    "awayTeamId": 4,
    "kickoff": "2025-10-01T20:00:00Z",
    "venue": "Staples Center"
  }'

echo "Adding fixture 3..."
curl -X POST http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 5,
    "awayTeamId": 6,
    "kickoff": "2025-10-02T18:00:00Z",
    "venue": "United Center"
  }'

echo "All fixtures added successfully!"

# Step 3: Verify the fixtures were added
echo "Fetching GameWeek fixtures..."
curl http://localhost:8080/api/gameweek/$GAMEWEEK_ID/fixtures
```

## GameWeek Status Values

- `UPCOMING`: GameWeek is scheduled but not yet active
- `ACTIVE`: GameWeek is currently ongoing
- `COMPLETED`: GameWeek has finished

## Important Notes

1. **Team IDs**: Make sure the `homeTeamId` and `awayTeamId` correspond to existing basketball teams in your database
2. **GameWeek Numbers**: Each GameWeek must have a unique number
3. **Kickoff Time**: Use ISO 8601 format for the kickoff time (UTC timezone recommended)
4. **Validation**: The API will validate that home and away teams are different
5. **Match Status**: Newly created fixtures will automatically have status "SCHEDULED"

## Error Handling

The API will return appropriate HTTP status codes:
- `200 OK`: Success
- `400 Bad Request`: Invalid request data or validation errors
- `404 Not Found`: GameWeek or team not found
- `500 Internal Server Error`: Server error

Common error scenarios:
- Trying to create a GameWeek with a number that already exists
- Referencing non-existent team IDs
- Setting the same team as both home and away team