# Fantasy Basketball League Documentation

This document provides comprehensive information about the Fantasy Basketball League system, including league creation, management, invitation system, and participation features.

## Overview

The Fantasy Basketball League system allows users to create and participate in competitive fantasy basketball leagues. Users can create private leagues with friends or join public leagues to compete against other fantasy managers.

## League Types

### PUBLIC Leagues
- **Open to all users:** Anyone can join without invitation
- **Automatic matchmaking:** System pairs users for competition
- **Global leaderboards:** Compete against the entire user base
- **No invite codes required**

### PRIVATE Leagues
- **Invitation-only:** Users must be invited to join
- **Controlled membership:** League creator manages who can participate
- **Custom competition:** Play against specific friends or groups
- **Unique invite codes:** Each league has a unique identifier

## League Structure

Each Fantasy League contains:

| Field | Description | Type |
|-------|-------------|------|
| `id` | Unique league identifier | Long |
| `name` | League display name (max 120 chars) | String |
| `type` | League type (PUBLIC/PRIVATE) | LeagueType |
| `inviteCode` | Unique invitation code (64 chars) | String |
| `joinCode` | 5-digit numeric code for easy joining (PRIVATE only) | String |
| `createdBy` | User who created the league | User |
| `createdAt` | League creation timestamp | Instant |
| `teams` | Collection of participating fantasy teams | Set<FantasyLeagueTeam> |

## League Management

### Creating a League

To create a new fantasy league, you'll need to:

1. **Choose a league type:** PUBLIC or PRIVATE
2. **Set a league name:** Descriptive name for your league
3. **Generate invite code:** Automatically created for PRIVATE leagues
4. **Invite participants:** For PRIVATE leagues only

**Important:** The user who creates a league is automatically joined to that league upon creation. You must have a fantasy team before creating a league.

**Example League Creation Request:**
```json
{
  "name": "Friends Championship 2024",
  "type": "PRIVATE"
}
```

### League Membership

League membership is managed through the `FantasyLeagueTeam` relationship:
- Each user's fantasy team can join multiple leagues
- Teams maintain separate standings in each league
- League creators can manage team participation

## Invitation System

The league invitation system enables seamless user recruitment for private leagues.

### League Invitations

Each invitation contains:
- **League reference:** Which league the invitation is for
- **Inviter information:** Who sent the invitation
- **Recipient email:** Target user's email address
- **Unique token:** Secure invitation identifier
- **Status tracking:** PENDING, ACCEPTED, DECLINED
- **Timestamps:** Creation and response times

### League Management API Endpoints

#### Create League
**POST** `/api/leagues/{userId}`

**Parameters:**
- `userId` (Path): ID of the user creating the league
- `name` (String): Name of the league
- `type` (String): League type ("PUBLIC" or "PRIVATE")

**Response:** FantasyLeague object with generated codes

**Authentication:** Required (user must be logged in)

**Example:**
```bash
POST /api/leagues/123
Content-Type: application/json
{
  "name": "My Private League",
  "type": "PRIVATE"
}
```

**Response:**
```json
{
  "id": 123,
  "name": "My Private League",
  "type": "PRIVATE",
  "inviteCode": "abc123xyz789abc123xyz789abc123",
  "joinCode": "12345",
  "createdBy": {
    "id": 1,
    "username": "creator"
  },
  "createdAt": "2025-09-28T22:17:00Z"
}
```

#### Join League by 5-Digit Code
**POST** `/api/leagues/join-by-code`

**Parameters:**
- `joinCode` (String): 5-digit numeric code
- `userId` (Long): ID of the user joining

**Response:** Success message indicating league joined

**Authentication:** Required (user must be logged in)

**Example:**
```bash
POST /api/leagues/join-by-code?joinCode=12345&userId=456
```

**Response:**
```json
"Successfully joined the league!"
```

#### Get User's Leagues
**GET** `/api/leagues/user/{userId}`

**Parameters:**
- `userId` (Path): ID of the user

**Response:** List of leagues the user has joined with basic information

**Authentication:** Required (user must be logged in)

**Example:**
```bash
GET /api/leagues/user/123
```

**Response:**
```json
[
  {
    "leagueId": 456,
    "leagueName": "My League",
    "leagueType": "PRIVATE",
    "totalPoints": 150,
    "rank": 2,
    "joinedAt": "2025-09-28T22:17:00Z"
  }
]
```

#### Get League Details with Rankings
**GET** `/api/leagues/{leagueId}`

**Parameters:**
- `leagueId` (Path): ID of the league

**Response:** League information with team rankings sorted by highest points

**Authentication:** Required (user must be logged in)

**Example:**
```bash
GET /api/leagues/456
```

**Response:**
```json
{
  "leagueId": 456,
  "leagueName": "My League",
  "leagueType": "PRIVATE",
  "rankings": [
    {
      "teamName": "Team Alpha",
      "totalPoints": 200
    },
    {
      "teamName": "Team Beta",
      "totalPoints": 150
    }
  ]
}
```

### Invitation API Endpoints

#### Create League Invitation
**POST** `/api/invitations`

**Parameters:**
- `leagueId` (Long): ID of the league
- `invitedByUserId` (Long): ID of the inviting user
- `email` (String): Email of the user to invite

**Response:** LeagueInvitation object

**Example:**
```bash
POST /api/invitations?leagueId=123&invitedByUserId=456&email=friend@example.com
```

#### Join Private League by Invite Code
**GET** `/api/invitations/join/{inviteCode}`

**Parameters:**
- `inviteCode` (String): Unique invite code for the private league

**Response:** Success message indicating league joined

**Authentication:** Required (user must be logged in)

**Example:**
```bash
GET /api/invitations/join/abc123xyz789
```

**Response:**
```json
"Successfully joined the league!"
```

#### Accept League Invitation (Legacy)
**POST** `/api/invitations/{token}/accept`

**Parameters:**
- `token` (String): Unique invitation token from email

**Response:** Updated LeagueInvitation with ACCEPTED status

**Note:** This endpoint is maintained for backward compatibility but the auto-join functionality is preferred.

#### Decline League Invitation (Legacy)
**POST** `/api/invitations/{token}/decline`

**Parameters:**
- `token` (String): Unique invitation token from email

**Response:** Updated LeagueInvitation with DECLINED status

### Invitation Workflow

#### New Auto-Join Process (Recommended)

1. **League Creator creates private league:**
   - System generates unique invite code
   - Creator shares invite link: `/api/invitations/join/{inviteCode}`
   - Link can be shared via any method (email, social media, messaging, etc.)

2. **Recipient accesses invite link:**
   - Can click link from anywhere (even outside the application)
   - Must be logged in to join (redirected to login if not authenticated)
   - Automatically joins league upon successful authentication

3. **Automatic league membership:**
   - No accept/decline required
   - User instantly becomes league member
   - Can start participating immediately

#### Legacy Token-Based Process (Still Supported)

1. **League Creator sends invitation:**
   - Uses POST `/api/invitations` with recipient email
   - System generates unique token
   - Email sent to recipient with invitation link

2. **Recipient receives email:**
   - Contains league information
   - Includes accept/decline links with tokens
   - Links expire after specified time period

3. **Recipient responds:**
   - Clicks accept/decline link
   - System processes response via token
   - League membership updated accordingly

## Invitation Status Types

| Status | Description |
|--------|-------------|
| `PENDING` | Invitation sent, waiting for response |
| `ACCEPTED` | User accepted and joined the league |
| `DECLINED` | User declined the invitation |

## League Participation

### Joining Public Leagues
- Browse available public leagues
- Join instantly without invitation
- Compete in open competition format

### Joining Private Leagues
- Receive invitation email from league creator
- Click invitation link to accept/decline
- Join league upon acceptance

### Team Competition
Once in a league:
- Set your weekly lineup
- Make transfers within league rules
- Compete for league championship
- Track standings and statistics

## League Features

### Scoring System
- Points calculated based on real player performances
- Weekly matchups between league members
- Season-long standings and rankings

### Transfer Market
- Trade players with other league members
- Subject to league-specific transfer rules
- Transfer deadlines and restrictions apply

### Communication
- League chat and messaging
- Trade negotiations
- League announcements

## League Administration

### League Creator Permissions
- Manage league settings
- Send invitations to new members
- Remove inactive participants
- Customize league rules

### League Settings
- Season duration
- Playoff format
- Scoring rules
- Transfer policies

## Best Practices

### For League Creators
1. **Choose descriptive league names**
2. **Set clear league rules before inviting**
3. **Invite engaged fantasy managers**
4. **Communicate regularly with league members**
5. **Monitor league activity and standings**

### For League Participants
1. **Respond promptly to invitations**
2. **Stay active throughout the season**
3. **Follow league-specific rules**
4. **Engage with other league members**
5. **Set lineups before deadlines**

## Security Considerations

### Invitation Security
- Unique tokens prevent unauthorized access
- Time-limited invitation validity
- Email verification ensures intended recipients
- Secure token generation and storage

### League Privacy
- Private leagues protect member information
- Invite codes provide controlled access
- League creator controls membership

## Integration with Other Systems

### Fantasy Teams
- Each user's fantasy team can participate in multiple leagues
- Separate performance tracking per league
- Independent standings and statistics

### User Management
- League membership tied to user accounts
- Authentication required for all league operations
- Role-based permissions for league management

### Notification System
- Email notifications for invitations
- League activity notifications
- Weekly reminder notifications

## Troubleshooting

### Common Issues

**Invitation not received:**
- Check spam/junk folders
- Verify email address accuracy
- Contact league creator for resend

**Cannot accept invitation:**
- Ensure invitation token is valid
- Check if invitation has expired
- Verify user account is active

**League not found:**
- Confirm league ID or invite code
- Check if league is still active
- Verify user permissions

### Support
For additional support with league functionality:
- Check API documentation
- Review error messages in responses
- Contact system administrators for technical issues

## Future Enhancements

Planned league system improvements:
- League templates and custom rules
- Advanced scoring systems
- Playoff bracket management
- League history and archives
- Mobile app integration
- Social media sharing