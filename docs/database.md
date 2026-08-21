# SafeHer Database Schema & Data Models

## Room Database Entities

### 1. `UserEntity` (Table: `user_profile`)
| Field | Type | Description |
|---|---|---|
| `id` (PK) | `TEXT` | Firebase UID or Local UUID |
| `fullName` | `TEXT` | User's full name |
| `email` | `TEXT` | User's email |
| `phoneNumber` | `TEXT` | User's phone number |
| `bloodGroup` | `TEXT` | Blood group for emergency responders |
| `emergencyNote` | `TEXT` | Medical conditions, allergies, or notes |
| `createdAt` | `INTEGER` | Account creation timestamp |

### 2. `EmergencyContactEntity` (Table: `emergency_contacts`)
| Field | Type | Description |
|---|---|---|
| `id` (PK) | `INTEGER` (Auto) | Unique contact identifier |
| `name` | `TEXT` | Contact name |
| `phoneNumber` | `TEXT` | E.164 formatted phone number |
| `relationship` | `TEXT` | Relationship tag (e.g. Mother, Sister) |
| `isPrimary` | `INTEGER` (Boolean) | Primary priority flag |
| `priorityOrder` | `INTEGER` | Sort order for SMS dispatch |
| `isSynced` | `INTEGER` (Boolean) | Cloud sync status |

### 3. `IncidentEntity` (Table: `incidents`)
| Field | Type | Description |
|---|---|---|
| `id` (PK) | `TEXT` | UUID of the incident |
| `type` | `TEXT` | `SOS`, `SAFETY_TIMER_EXPIRED`, `JOURNEY_DEVIATION`, `MANUAL_EVIDENCE`, `TEST_ALERT` |
| `status` | `TEXT` | `ACTIVE`, `RESOLVED`, `CANCELLED` |
| `timestamp` | `INTEGER` | Milliseconds epoch at trigger time |
| `latitude` | `REAL` | GPS Latitude |
| `longitude` | `REAL` | GPS Longitude |
| `address` | `TEXT` | Reverse geocoded address |
| `audioEvidencePath` | `TEXT` | File path to local M4A audio recording |
| `notes` | `TEXT` | Resolution or distress notes |
| `durationSeconds` | `INTEGER` | Total active emergency duration |
| `contactsNotifiedCount` | `INTEGER` | Number of contacts alerted |
| `isSynced` | `INTEGER` (Boolean) | Cloud sync status |

### 4. `LocationWaypointEntity` (Table: `location_waypoints`)
| Field | Type | Description |
|---|---|---|
| `id` (PK) | `INTEGER` (Auto) | Waypoint ID |
| `incidentId` | `TEXT` | Associated incident/journey ID |
| `latitude` | `REAL` | Waypoint latitude |
| `longitude` | `REAL` | Waypoint longitude |
| `accuracy` | `REAL` | Location accuracy radius (meters) |
| `timestamp` | `INTEGER` | Time of GPS fix |
| `isSynced` | `INTEGER` (Boolean) | Cloud sync flag |

### 5. `PendingSyncEntity` (Table: `pending_sync_queue`)
| Field | Type | Description |
|---|---|---|
| `id` (PK) | `INTEGER` (Auto) | Queue item ID |
| `eventType` | `TEXT` | Type of event (`INCIDENT`, `CONTACT`, `LOCATION_POINT`) |
| `payloadJson` | `TEXT` | Serialized payload or entity ID |
| `createdAt` | `INTEGER` | Timestamp queued |
| `retryCount` | `INTEGER` | Number of failed retry attempts |