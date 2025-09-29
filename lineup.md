# Fantasy Basketball System: Team Creation, Lineups & Leagues

## Overview
This document explains the relationships between entities in the fantasy basketball system and describes when database records are automatically created during team creation, lineup management, and league operations.

## Entity Relationships

### Core Entities and Their Relationships

#### 1. FantasyTeam
- **Primary Entity**: Represents a user's fantasy team
- **Key Fields**: 
  - `id` (Primary Key)
  - `teamName` (Team name chosen by user)
  - `owner` (ManyToOne relationship with User)
  - `budget` (BigDecimal, starts at 100.00)
  - `totalPoints` (Integer, starts at 0)
  - `transfersRemaining` (Integer, starts at 1)
- **Relationships**:
  - `OneToMany` with FantasyTeamPlayer (squad management)
  - Referenced by Lineup (via fantasy_team_id)
  - Referenced by FantasyLeagueTeam (league participation)

#### 2. FantasyTeamPlayer
- **Purpose**: Junction table linking FantasyTeam to BasketballPlayer
- **Key Fields**:
  - `fantasyTeam` (ManyToOne to FantasyTeam)
  - `player` (ManyToOne to BasketballPlayer)
  - `purchasePrice` (BigDecimal)
  - `active` (Boolean - whether player is currently in squad)
  - `acquiredAt` / `releasedAt` (Timestamps)
- **Unique Constraint**: `(fantasy_team_id, player_id, active)` - prevents duplicate active players

#### 3. Lineup
- **Purpose**: Represents a team's lineup for a specific game week
- **Key Fields**:
  - `fantasyTeam` (ManyToOne to FantasyTeam)
  - `gameWeek` (ManyToOne to GameWeek)
  - `createdAt` (Timestamp)
- **Relationships**:
  - `OneToMany` with LineupSlot
- **Unique Constraint**: `(fantasy_team_id, game_week_id)` - one lineup per team per game week

#### 4. LineupSlot
- **Purpose**: Individual player slots within a lineup
- **Key Fields**:
  - `lineup` (ManyToOne to Lineup)
  - `player` (ManyToOne to BasketballPlayer)
  - `starter` (Boolean - true for starters, false for bench)
  - `slotPosition` (String - e.g., "CPT" for captain, null otherwise)

#### 5. FantasyLeague
- **Purpose**: Represents a fantasy league
- **Key Fields**:
  - `name` (String - league name)
  - `type` (LeagueType enum - PUBLIC/PRIVATE)
  - `inviteCode` (String - for private leagues)
  - `createdBy` (ManyToOne to User)
- **Relationships**:
  - `OneToMany` with FantasyLeagueTeam

#### 6. FantasyLeagueTeam
- **Purpose**: Junction table linking leagues and teams
- **Key Fields**:
  - `league` (ManyToOne to FantasyLeague)
  - `team` (ManyToOne to FantasyTeam)
  - `totalPoints` (Integer - team's points in this league)
  - `rank` (Integer - team's rank in this league)
  - `joinedAt` (Timestamp)
- **Unique Constraint**: `(league_id, team_id)` - one entry per team per league

## Automatic Record Creation Workflows

### 1. Team Creation Process (`POST /api/user/team`)

When a user creates a fantasy team, the following records are **automatically created**:

#### Step 1: FantasyTeam Creation
```java
FantasyTeam team = new FantasyTeam();
team.setOwner(user);
team.setTeamName(requestedName);
team.setBudget(BigDecimal("100.00"));        // Default budget
team.setTotalPoints(0);                      // Starting points
team.setTransfersRemaining(1);               // Default transfers
// Auto-generated: id, createdAt, updatedAt
```

#### Step 2: User Flag Update
```java
user.setHasFantasyTeam(true);  // Prevents multiple teams per user
```

#### Step 3: Automatic League Creation & Joining
The system automatically creates (if not exists) and joins three types of leagues:

##### A. Overall League
- **League Name**: "Overall League"
- **Type**: PUBLIC
- **Auto-created**: If doesn't exist
- **Auto-joined**: Always

##### B. Favourite Team League (if user has favourite team)
- **League Name**: "Team League - TEAM_{team_id}"
- **Type**: PUBLIC
- **Auto-created**: If doesn't exist
- **Auto-joined**: If user has favouriteTeam set

##### C. Nationality League (if user has nationality)
- **League Name**: "Nation League - {NATIONALITY}"
- **Type**: PUBLIC
- **Auto-created**: If doesn't exist
- **Auto-joined**: If user has nationality set

#### Step 4: FantasyLeagueTeam Records
For each league joined, a FantasyLeagueTeam record is created:
```java
FantasyLeagueTeam link = new FantasyLeagueTeam();
link.setLeague(league);
link.setTeam(team);
link.setTotalPoints(0);          // Starting points
link.setRank(null);              // No rank initially
link.setJoinedAt(Instant.now()); // Join timestamp
```

### 2. Lineup Creation Process (`POST /api/user/lineup`)

When a user sets their lineup, the following records are **automatically created**:

#### Step 1: Lineup Creation (if not exists)
```java
Lineup lineup = new Lineup();
lineup.setFantasyTeam(team);
lineup.setGameWeek(gameWeek);
lineup.setCreatedAt(Instant.now());
```

#### Step 2: LineupSlot Creation
For each player in the lineup request:

##### Starters:
```java
LineupSlot starter = new LineupSlot();
starter.setLineup(lineup);
starter.setPlayer(basketballPlayer);
starter.setStarter(true);
starter.setSlotPosition(null);  // Or "CPT" for captain
```

##### Bench Players:
```java
LineupSlot benchPlayer = new LineupSlot();
benchPlayer.setLineup(lineup);
benchPlayer.setPlayer(basketballPlayer);
benchPlayer.setStarter(false);
benchPlayer.setSlotPosition(null);
```

#### Step 3: Captain Assignment (if specified)
- Captain gets `slotPosition = "CPT"`
- Previous captain (if any) gets `slotPosition = null`

### 3. Transfer Process (`POST /api/user/transfer`)

When a user makes a transfer, the following records are **automatically created/updated**:

#### Step 1: FantasyTeamPlayer Updates
- **Player Out**: `active = false`, `releasedAt = now()`
- **Player In**: New FantasyTeamPlayer record with `active = true`, `acquiredAt = now()`

#### Step 2: Transfer Record Creation
```java
Transfer transfer = new Transfer();
transfer.setFantasyTeam(team);
transfer.setGameWeek(gameWeek);
transfer.setPlayerOut(outPlayer);
transfer.setPlayerIn(inPlayer);
transfer.setPriceDifference(calculatedDifference);
transfer.setCreatedAt(Instant.now());
```

## Business Rules & Constraints

### Team Creation Rules
1. **One Team Per User**: Each user can only have one fantasy team
2. **Default Budget**: All teams start with 100.00 budget
3. **Auto-League Joining**: Teams automatically join public leagues based on user preferences

### Lineup Rules
1. **One Lineup Per Game Week**: Unique constraint prevents multiple lineups per team per game week
2. **Squad Validation**: All lineup players must be in the team's active squad
3. **Captain Must Be Starter**: Captain can only be selected from starting players
4. **Deadline Enforcement**: Lineups cannot be set for completed game weeks

### League Rules
1. **Unique Team Per League**: Teams can only join each league once
2. **Auto-Creation**: Public leagues are created automatically when first user with matching criteria creates a team
3. **Point Tracking**: FantasyLeagueTeam records track points and rankings per league

### Transfer Rules
1. **Active Squad Management**: Only active squad players can be transferred out
2. **No Duplicate Additions**: Cannot add a player already in active squad
3. **Deadline Enforcement**: Transfers blocked for completed game weeks
4. **Penalty System**: Multiple transfers in same game week incur point penalties

## Database Schema Summary

```
User (1) ──────────── (*) FantasyTeam
                           │
                           ├── (1) ────────── (*) FantasyTeamPlayer ──── (*) BasketballPlayer
                           │
                           ├── (1) ────────── (*) Lineup ──── (1) GameWeek
                           │                   │
                           │                   └── (1) ── (*) LineupSlot ──── (*) BasketballPlayer
                           │
                           └── (*) ────────── (*) FantasyLeagueTeam ──── (*) FantasyLeague
                                                                           │
                                                                           └── (*) User (createdBy)
```

## BasketballPlayer to FantasyTeamPlayer Relationship

### When Does a BasketballPlayer Become a FantasyTeamPlayer?

BasketballPlayer and FantasyTeamPlayer serve different purposes in the system:

- **BasketballPlayer**: Global pool of real players that exist independently
- **FantasyTeamPlayer**: Junction records that link specific players to specific fantasy teams

#### Initial Team Creation
When a user creates a fantasy team via `POST /api/user/team`, **NO FantasyTeamPlayer records are created initially**. Teams start with an empty squad (`squad = []` in API responses).

#### Player Addition Process
FantasyTeamPlayer records are created **ONLY** through the transfer system:

```java
// During POST /api/user/transfer
FantasyTeamPlayer inLink = new FantasyTeamPlayer();
inLink.setFantasyTeam(team);                    // Links to specific fantasy team
inLink.setPlayer(basketballPlayer);             // Links to global basketball player
inLink.setActive(true);                         // Player is in active squad
inLink.setAcquiredAt(Instant.now());            // Timestamp of acquisition
inLink.setPurchasePrice(player.getMarketValue()); // Price paid when acquired
```

### Player Selection Workflow

#### Step 1: Browse Available Players
Users can browse the global player pool via `GET /api/players` with filters:
- Team filter (`teamId`)
- Position filter (`position`) 
- Price range (`minPrice`, `maxPrice`)
- Ownership percentage filters (`ownershipGte`, `ownershipLte`)

#### Step 2: Select Players via Transfers
To add a player to their squad, users must:
1. **Have an existing player to transfer out** (except for initial squad building)
2. **Use the transfer endpoint**: `POST /api/user/transfer`
3. **Specify**: `playerOutId`, `playerInId`, `fantasyTeamId`, `gameWeekId`

#### Step 3: FantasyTeamPlayer Creation
When a transfer is successful:
- **Old player**: `active = false`, `releasedAt = now()`
- **New player**: New FantasyTeamPlayer record with `active = true`

### Multi-User Usage

#### Can FantasyTeamPlayer Records Be Used by Multiple Users?
**NO** - FantasyTeamPlayer records are **unique per fantasy team**:

1. **Separate Records**: Each fantasy team that acquires the same BasketballPlayer gets its own FantasyTeamPlayer record
2. **Independent Ownership**: Multiple teams can own the same BasketballPlayer simultaneously
3. **Different Purchase Prices**: Each team may have acquired the player at different market values
4. **Independent History**: Each team tracks its own `acquiredAt`, `releasedAt`, and `purchasePrice`

#### Example Scenario:
```
BasketballPlayer: "LeBron James" (ID: 1)
├── FantasyTeamPlayer (ID: 101)
│   ├── fantasyTeam: "Team A" 
│   ├── player: LeBron James
│   ├── purchasePrice: $15.5M
│   └── acquiredAt: 2025-09-01
├── FantasyTeamPlayer (ID: 102)  
│   ├── fantasyTeam: "Team B"
│   ├── player: LeBron James  
│   ├── purchasePrice: $16.0M
│   └── acquiredAt: 2025-09-10
└── FantasyTeamPlayer (ID: 103)
    ├── fantasyTeam: "Team C"
    ├── player: LeBron James
    ├── purchasePrice: $15.8M
    └── acquiredAt: 2025-09-15
```

### Database Constraints & Business Rules

#### Unique Constraint
```sql
UNIQUE(fantasy_team_id, player_id, active)
```
This prevents:
- Multiple active records for the same player in the same team
- But allows historical records when `active = false`

#### Ownership Tracking
The system tracks how many teams currently own each player:
```java
@Query("select count(distinct tp.fantasyTeam.id) from FantasyTeamPlayer tp 
       where tp.active = true and tp.player.id = :playerId")
long countActiveTeamsByPlayerId(@Param("playerId") Long playerId);
```

This data is used to calculate **ownership percentage** shown in the player list API.

### Initial Squad Building Strategy

Since there's no dedicated squad building endpoint, the system likely expects:

1. **Empty Team Start**: Teams begin with no players
2. **Transfer-Based Building**: Users build squads through transfers during the first game week
3. **Budget Management**: Users start with 100.00 budget to acquire initial squad
4. **Free Transfers**: Initial transfers may not count toward penalty limits

This design ensures all player acquisitions go through the same transfer validation and tracking system.

## Key Takeaways

1. **High Automation**: Most records are created automatically during user actions
2. **Data Integrity**: Unique constraints prevent duplicate relationships
3. **Flexible League System**: Automatic league creation and joining based on user preferences
4. **Historical Tracking**: Transfer and lineup history is maintained through timestamps
5. **Business Rule Enforcement**: Constraints ensure fantasy sports rules are followed at the database level
6. **Transfer-Only Player Addition**: All FantasyTeamPlayer records are created via transfers, not initial team creation
7. **Independent Team Records**: Multiple teams can own the same BasketballPlayer with separate FantasyTeamPlayer records