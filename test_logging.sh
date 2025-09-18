#!/bin/bash

echo "Starting Fantasy Basketball application to test API logging..."

# Start the application in the background
./gradlew bootRun &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 15

echo "Testing API logging with various endpoints..."

echo -e "\n1. Testing registration endpoint..."
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_logger",
    "email": "test_logger@example.com", 
    "password": "testpass123",
    "nationality": "Test"
  }' > /dev/null 2>&1

echo -e "\n2. Testing login endpoint..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "test_logger@example.com",
    "password": "testpass123"
  }')

echo -e "\n3. Testing GET endpoint with query parameters..."
curl -s -X GET "http://localhost:8080/api/players?page=0&size=5&position=PG" \
  -H "Accept: application/json" > /dev/null 2>&1

echo -e "\n4. Testing authenticated endpoint..."
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ ! -z "$TOKEN" ]; then
    curl -s -X GET http://localhost:8080/api/auth/me \
      -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
else
    echo "No token found, skipping authenticated endpoint test"
fi

echo -e "\n5. Testing endpoint that should return 404..."
curl -s -X GET http://localhost:8080/api/nonexistent > /dev/null 2>&1

echo -e "\n6. Testing endpoint with invalid data (should return 400)..."
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "",
    "password": ""
  }' > /dev/null 2>&1

echo -e "\n\nAPI logging test completed. Check the application logs above for:"
echo "- [API][Request] entries showing incoming requests with timestamps"
echo "- [API][Response] entries showing outgoing responses with status codes and durations"
echo "- Masked sensitive data (passwords should show as ***)"
echo "- Different HTTP methods and status codes"

echo -e "\nStopping application..."
kill $APP_PID 2>/dev/null

echo "Logging test completed."