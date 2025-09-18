#!/bin/bash

echo "Starting Fantasy Basketball application..."

# Start the application in the background
./gradlew bootRun &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 15

echo "Testing authentication..."

# Register the user
echo "1. Registering user Ali_sleem1@outlook.com..."
curl -v -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ali_sleem",
    "email": "Ali_sleem1@outlook.com", 
    "password": "testpass123",
    "nationality": "Lebanon"
  }' 2>&1

echo -e "\n\n2. Testing login with registered user..."

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "Ali_sleem1@outlook.com",
    "password": "testpass123"
  }'

echo -e "\n\n3. Testing login with wrong password (should fail)..."

# Test login with wrong password
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "Ali_sleem1@outlook.com",
    "password": "wrongpassword"
  }'

echo -e "\n\nStopping application..."
kill $APP_PID

echo "Test completed."