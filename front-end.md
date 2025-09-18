# Fantasy Basketball API Documentation

This document provides a comprehensive overview of all APIs available for frontend integration with the Fantasy Basketball backend.

## Authentication APIs

### POST /api/auth/login
**Description:** Authenticate user and receive JWT token  
**Request Body:**
```json
{
  "usernameOrEmail": "string",
  "password": "string"
}
```
**Response:**
```json
{
  "token": "string",
  "user": {
    "id": "number",
    "email": "string",
    "username": "string", 
    "role": "string"
  }
}
```

### POST /api/auth/register
**Description:** Register a new user account  
**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "favouriteTeamId": "number",
  "nationality": "string"
}
```
**Response:**
```json
{
  "id": "number",
  "email": "string"
}
```

### GET /api/auth/me
**Description:** Get current user information  
**Headers:** `Authorization: Bearer {token}`  
**Response:**
```json
{
  "id": "number",
  "email": "string",
  "username": "string",
  "roles": "string"
}
```

## User Management APIs

### POST /api/user/team
**Description:** Create a fantasy team  
**Headers:** Authorization required  
**Request Body:**
```json
{
  "teamName": "string",
  "ownerUserId": "number"
}
```
**Response:** FantasyTeam object

### POST /api/user/lineup
**Description:** Set team lineup  
**Headers:** Authorization required  
**Request Body:**
```json
{
  "teamId": "number",
  "players": [
    {
      "playerId": "number",
      "position": "string"
    }
  ]
}
```
**Response:** Lineup object

### POST /api/user/transfer
**Description:** Make player transfer  
**Headers:** Authorization required  
**Request Body:**
```json
{
  "teamId": "number",
  "playerInId": "number",
  "playerOutId": "number"
}
```
**Response:** Transfer object

## Player APIs

### GET /api/players
**Description:** Get list of basketball players with filtering  
**Query Parameters:**
- `teamId` (optional): Filter by basketball team ID
- `position` (optional): Filter by position (PG, SG, SF, PF, C)
- `minPrice` (optional): Minimum market value
- `maxPrice` (optional): Maximum market value
- `ownershipGte` (optional): Minimum ownership percentage
- `ownershipLte` (optional): Maximum ownership percentage
- `page` (default: 0): Page number for pagination
- `size` (default: 50): Page size

**Response:**
```json
[
  {
    "id": "number",
    "firstName": "string",
    "lastName": "string",
    "position": "string",
    "teamId": "number",
    "teamName": "string",
    "price": "decimal",
    "ownershipPct": "number"
  }
]
```

## Game Week APIs

### Available at:** `/api/gameweek`
- Game week management endpoints
- Current game week information
- Match schedules and results

## Leaderboard APIs

### Available at:** `/api/leaderboard`
- League standings
- Player rankings
- Points calculations

## Invitation APIs

### Available at:** `/api/invitations`
- League invitation management
- Send/accept/decline invitations

## Notification APIs

### Available at:** `/api/notifications`
- User notifications
- System announcements

## Admin APIs

### Available at:** `/api/admin`
**Note:** Requires ADMIN role
- User management
- System configuration
- Data management

### Available at:** `/api/admin/gameops`
**Note:** Requires ADMIN role
- Game operations
- Match management
- Player data updates

## Authentication Requirements

Most endpoints require authentication via JWT token in the Authorization header:
```
Authorization: Bearer {your_jwt_token}
```

## Error Responses

The API returns standard HTTP status codes:
- `200`: Success
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (missing or invalid token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found
- `500`: Internal Server Error

Error response format:
```json
{
  "error": "string",
  "message": "string",
  "timestamp": "string"
}
```

## How to Create Your Fantasy Team (Step-by-Step Guide)

### Step 1: Account Setup
1. **Register a new account:** Use `POST /api/auth/register` with your details
   ```json
   {
     "username": "your_username",
     "email": "your_email@example.com", 
     "password": "your_secure_password",
     "favouriteTeamId": 1,
     "nationality": "USA"
   }
   ```

2. **Login to get your authentication token:** Use `POST /api/auth/login`
   ```json
   {
     "usernameOrEmail": "your_username",
     "password": "your_secure_password"
   }
   ```
   
3. **Save the JWT token** from the response - you'll need it for all protected endpoints

### Step 2: Create Your Fantasy Team
1. **Create your team:** Use `POST /api/user/team` with Authorization header
   ```json
   {
     "teamName": "My Awesome Team",
     "ownerUserId": 123
   }
   ```
   
   **Important:** Your team will be created with a budget of **1000 coins** (Note: The current system uses 1000 coins, not 100 as mentioned in some references)

### Step 3: Browse and Select Players
1. **Browse available players:** Use `GET /api/players` to see all available players
   - Filter by position: `?position=PG` (PG, SG, SF, PF, C)
   - Filter by price range: `?minPrice=50&maxPrice=200`
   - Filter by team: `?teamId=1`

2. **Understand player pricing:** Each player has a `price` field showing their market value
   - Player prices vary based on performance and demand
   - You can see ownership percentage (`ownershipPct`) to gauge popularity

### Step 4: Build Your Squad (Budget Management)
**Budget Rules:**
- **Total Budget:** 1000 coins
- **Squad Size:** Exactly 8 players (5 starters + 3 bench)
- **Position Limits:** 
  - Point Guards (PG): Maximum 2
  - Shooting Guards (SG): Maximum 2
  - Small Forwards (SF): Maximum 2
  - Power Forwards (PF): Maximum 2
  - Centers (C): Maximum 1

**Budget Planning Tips:**
1. **Calculate total cost:** Add up all selected players' prices
2. **Stay within budget:** Total must not exceed 1000 coins
3. **Balance your squad:** Don't spend all budget on 2-3 expensive players
4. **Consider bench players:** You need 3 bench players who are typically cheaper

**Example Squad Budget Breakdown:**
- 2 Premium players (200-250 coins each) = 450 coins
- 3 Mid-tier players (100-150 coins each) = 375 coins  
- 3 Budget players (50-75 coins each) = 175 coins
- **Total: 1000 coins**

### Step 5: Add Players to Your Team
When creating your team, you can include an initial squad:
```json
{
  "teamName": "My Team",
  "ownerUserId": 123,
  "initialSquad": [1, 2, 3, 4, 5, 6, 7, 8]
}
```

**The system will validate:**
- All 8 players exist and are active
- No duplicate players
- Position limits are respected
- Total cost doesn't exceed 1000 coins

### Step 6: Set Your Starting Lineup
1. **Create your lineup:** Use `POST /api/user/lineup`
   ```json
   {
     "teamId": 456,
     "players": [
       {"playerId": 1, "position": "PG"},
       {"playerId": 2, "position": "SG"}, 
       {"playerId": 3, "position": "SF"},
       {"playerId": 4, "position": "PF"},
       {"playerId": 5, "position": "C"}
     ]
   }
   ```

### Step 7: Make Transfers (Optional)
1. **Transfer players:** Use `POST /api/user/transfer` to swap players
   ```json
   {
     "teamId": 456,
     "playerInId": 10,
     "playerOutId": 8
   }
   ```
   
   **Transfer Rules:**
   - You have limited transfers per game week
   - New player cost + remaining budget must cover the transfer
   - Position limits still apply after transfer

### Budget Management Summary
- **Starting Budget:** 1000 coins
- **Remaining Budget:** Shows after each player addition/transfer  
- **Squad Requirement:** Exactly 8 players within budget
- **Strategy:** Balance expensive stars with budget-friendly role players

## Getting Started (Quick Reference)

1. **Register a new account:** `POST /api/auth/register`
2. **Login to get token:** `POST /api/auth/login`
3. **Use token in Authorization header** for protected endpoints
4. **Create fantasy team:** `POST /api/user/team`
5. **Browse players:** `GET /api/players`
6. **Set lineup and make transfers** as needed

## Notes

- All request bodies should be sent as `application/json`
- Dates are in ISO 8601 format
- Decimal values are used for monetary amounts and statistics
- The API uses standard RESTful conventions