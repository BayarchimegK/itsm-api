# How to Assign SR Handlers

## Overview

Assigning a handler (charger) to a Service Request (SR) is a critical step in the SR workflow. A handler is the person responsible for executing and completing the SR. This guide explains all methods available to assign handlers.

## Methods to Assign a Handler

### Method 1: Direct Handler Assignment (Recommended)

**Use this when:** A manager/admin needs to assign a specific handler to an SR at any time.

**Endpoint:**

```http
PUT /api/sr/{id}/assign-handler
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Parameters:**

- `{id}` - The SR ID (e.g., "SR-202501-001")

**Request Body:**

```json
{
  "chargerId": "handler123",
  "chargerUserNm": "Jane Smith"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Handler assigned successfully (ID: handler123)",
  "assignedHandlerId": "handler123",
  "assignedHandlerName": "Jane Smith"
}
```

**Example with curl:**

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/assign-handler \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "chargerId": "handler456",
    "chargerUserNm": "John Handler"
  }'
```

**Who can use:** Manager (R001), Admin (R002)

---

### Method 2: Assign Handler During Receive

**Use this when:** Handler is acknowledging receipt of the SR for the first time.

**Endpoint:**

```http
PUT /api/sr/{id}/receive
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "rspons1stDt": "2026-02-11T16:25:00",
  "rspons1stCn": "SR acknowledged, starting investigation",
  "chargerId": "handler123",
  "chargerNm": "Jane Smith",
  "priority": "2"
}
```

**Response:**

```json
{
  "success": true,
  "message": "SR received and acknowledged"
}
```

**Who can use:** Handler (R003), Manager (R001), Admin (R002)

---

### Method 3: Assign Handler During First Response

**Use this when:** Handler is sending the first response/update to the requester and setting the assigned handler.

**Endpoint:**

```http
PUT /api/sr/{id}/response-1st
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "rspons1stCn": "Initial investigation shows database issue",
  "chargerId": "handler123",
  "chargerNm": "Jane Smith"
}
```

**Who can use:** Handler (R003), Manager (R001), Admin (R002)

---

### Method 4: Update Handler During Processing

**Use this when:** Need to change the handler while recording processing activity (reassignment).

**Endpoint:**

```http
PUT /api/sr/{id}/process
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**

```json
{
  "processCn": "Coordinating with infrastructure team",
  "chargerId": "handler456",
  "chargerNm": "Bob Smith"
}
```

**Who can use:** Handler (R003), Manager (R001), Admin (R002)

---

## Typical Workflow for Handler Assignment

### Scenario 1: Manager Assigns Handler Immediately After SR Creation

```
Customer creates SR
    ↓
Manager reviews SR
    ↓
Manager assigns handler via endpoint 1 (assign-handler)
    ↓
Handler receives notification and acknowledges
    ↓
Handler starts work
```

**Commands:**

```bash
# Manager assigns handler
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/assign-handler \
  -H "Authorization: Bearer MANAGER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "chargerId": "handler789",
    "chargerUserNm": "Alice Handler"
  }'

# Handler acknowledges receipt
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/receive \
  -H "Authorization: Bearer HANDLER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "rspons1stDt": "2026-02-11T17:00:00",
    "rspons1stCn": "SR received and acknowledged"
  }'
```

---

### Scenario 2: Handler Self-Assigns Upon Responding

```
Customer creates SR
    ↓
Handler views unassigned SRs (in list)
    ↓
Handler sends first response AND assigns themselves
    ↓
Handler works on the SR
```

**Commands:**

```bash
# Handler sends response and assigns themselves
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/response-1st \
  -H "Authorization: Bearer HANDLER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "rspons1stCn": "Starting investigation on system access issue",
    "chargerId": "handler789",
    "chargerNm": "Alice Handler"
  }'
```

---

### Scenario 3: Reassign Handler During Work

```
Handler A starts work
    ↓
Handler A realizes they need specialist
    ↓
Manager reassigns to Handler B
    ↓
Handler B continues work
```

**Commands:**

```bash
# Manager reassigns handler
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/assign-handler \
  -H "Authorization: Bearer MANAGER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "chargerId": "specialist_handler",
    "chargerUserNm": "Expert Handler"
  }'

# Or Handler A can reassign while updating work status
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/process \
  -H "Authorization: Bearer HANDLER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "processCn": "Escalating to infrastructure specialist",
    "chargerId": "specialist_handler",
    "chargerNm": "Expert Handler"
  }'
```

---

## Handler Information in SR Database

When a handler is assigned, the SR stores:

| Field           | Database Column | Description                                 |
| --------------- | --------------- | ------------------------------------------- |
| Handler ID      | `CHARGER_ID`    | User ID of the handler                      |
| Handler Name    | `CHARGER_NM`    | Display name of the handler                 |
| Assignment Time | `MODIFY_DT`     | When the handler was last assigned/modified |

---

## Getting Handler Information

### View Handler of a Specific SR

**Request:**

```http
GET /api/sr/SR-202501-001
Authorization: Bearer <JWT_TOKEN>
```

**Response will include:**

```json
{
  "srvcRsponsNo": "SR-202501-001",
  "chargerId": "handler123",
  "chargerUserNm": "Jane Smith",
  "modifyDt": "2026-02-11T16:25:00",
  ...
}
```

### View All SRs with Their Assigned Handlers

**Request:**

```http
GET /api/sr/list
Authorization: Bearer <JWT_TOKEN>
```

**Response includes handler info for each SR:**

```json
{
  "success": true,
  "total": 5,
  "data": [
    {
      "srvcRsponsNo": "SR-202501-001",
      "chargerId": "handler123",
      "chargerUserNm": "Jane Smith",
      ...
    },
    {
      "srvcRsponsNo": "SR-202501-002",
      "chargerId": null,
      "chargerUserNm": null,
      "message": "Unassigned"
    }
  ]
}
```

---

## Best Practices for Handler Assignment

### 1. **Assign Based on Service Type**

```json
{
  "chargerId": "database_specialist",
  "chargerUserNm": "DB Team Lead"
}
```

Assign handlers who have expertise in the requested service.

### 2. **Consider Current Workload**

Before assigning, check if the handler is already overloaded with other SRs.

### 3. **Notify Handler After Assignment**

```bash
# Assign handler
PUT /api/sr/{id}/assign-handler

# Then send notification via your notification system
# (email, chat, etc.)
```

### 4. **Support Handler Escalation**

```bash
# Handler can request reassignment by updating workload
PUT /api/sr/{id}/process
-d '{
  "processCn": "Escalating to specialized team - outside my expertise",
  "chargerId": "specialist_handler",
  "chargerUserNm": "Specialist"
}'
```

### 5. **Track Handler Changes**

The `MODIFY_DT` (modification date) field shows when the handler was last changed, allowing you to audit assignments.

---

## Error Handling

### Handler Not Found

```json
{
  "success": false,
  "error": "Handler ID 'invalid_id' not found in system"
}
```

### Permission Denied

```json
{
  "success": false,
  "error": "User does not have permission to assign handlers. Only Manager (R001) and Admin (R002) can assign handlers."
}
```

### SR Not Found

```json
{
  "success": false,
  "error": "Service Request not found"
}
```

---

## API Endpoint Summary for Handler Assignment

| Operation                      | Method | Endpoint                      | Who Can Use             |
| ------------------------------ | ------ | ----------------------------- | ----------------------- |
| **Assign/Reassign Handler**    | PUT    | `/api/sr/{id}/assign-handler` | Manager, Admin          |
| **Assign on Receive**          | PUT    | `/api/sr/{id}/receive`        | Handler, Manager, Admin |
| **Assign on First Response**   | PUT    | `/api/sr/{id}/response-1st`   | Handler, Manager, Admin |
| **Reassign on Process Update** | PUT    | `/api/sr/{id}/process`        | Handler, Manager, Admin |
| **View Handler Info**          | GET    | `/api/sr/{id}`                | All authenticated users |
| **View All SRs with Handlers** | GET    | `/api/sr/list`                | All authenticated users |

---

## Complete Handler Assignment Workflow Example

```bash
#!/bin/bash

# 1. Requester creates SR
curl -X POST http://localhost:8090/api/sr/create \
  -H "Authorization: Bearer REQUESTER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "srvcRsponsSj": "Database access request",
    "srvcRsponsCn": "Need access to production database for reporting"
  }'
# Response: SR-202501-001 created

# 2. Manager assigns handler immediately
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/assign-handler \
  -H "Authorization: Bearer MANAGER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "chargerId": "dba_handler",
    "chargerUserNm": "DBA Team"
  }'

# 3. Handler (DBA Team) acknowledges and starts work
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/receive \
  -H "Authorization: Bearer HANDLER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "rspons1stDt": "2026-02-11T17:30:00",
    "rspons1stCn": "Creating database user account for access"
  }'

# 4. Handler updates work progress
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/process \
  -H "Authorization: Bearer HANDLER_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "processCn": "User account created with read-only permissions"
  }'

# 5. Manager verifies and closes
curl -X PUT http://localhost:8090/api/sr/SR-202501-001/verify \
  -H "Authorization: Bearer MANAGER_JWT" \
  -H "Content-Type: application/json" \
  -d '{"srvcVerifyDtls": "Access verified working"}'

curl -X PUT http://localhost:8090/api/sr/SR-202501-001/finish \
  -H "Authorization: Bearer MANAGER_JWT" \
  -H "Content-Type: application/json" \
  -d '{"srvcFinDtls": "SR completed successfully"}'
```
