#!/bin/bash

echo "Testing the modified /api/teams/{id}/players endpoint..."
echo "Starting the application..."

# Start the application in the background
./gradlew bootRun &
APP_PID=$!

# Wait for the application to start
echo "Waiting for application to start..."
sleep 30

# Test the endpoint
echo "Testing GET /api/teams/3/players"
curl -X GET "http://localhost:8080/api/teams/3/players" \
  -H "Authorization;" \
  -v

echo ""
echo "Test completed."

# Clean up - stop the application
kill $APP_PID 2>/dev/null || true
wait $APP_PID 2>/dev/null || true