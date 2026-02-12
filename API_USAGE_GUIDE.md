# How Clients Use the SR (Service Request) API

## Overview

The SR (Service Request) API provides a REST interface for managing service requests with role-based access control. The API is hosted at `http://localhost:8090/api/sr`

## Authentication

All endpoints (except `/api/public/health`) require JWT authentication via Keycloak. Include the JWT token in the `Authorization` header:

```
Authorization: Bearer <JWT_TOKEN>
```

## Role-Based Access Control

The API supports the following user roles that determine what SRs users can see and what actions they can perform:

- **R001**: Manager - Can view all SRs and perform administrative actions
- **R002**: Admin - Full access to all SRs and all operations
- **R003**: Handler - Can view assigned SRs and perform handler actions (receive, respond, process)
- **R005**: Requester - Can only view their own SRs and evaluate results

## Endpoints

### 1. Create a Service Request

**Request:**

```http
POST /api/sr/create
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcRsponsSj": "System access request",
  "srvcRsponsCn": "Need access to HR portal",
  "urgency": "2",
  "priority": "3",
  "trgetSrvcCode": "S001"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Service Request created successfully",
  "srNo": "SR-202501-001"
}
```

**Access:** All authenticated users (R001, R002, R003, R005)

---

### 2. Get a Specific Service Request

**Request:**

```http
GET /api/sr/SR-202501-001
Authorization: Bearer <JWT_TOKEN>
```

**Response:**

```json
{
  "srvcRsponsNo": "SR-202501-001",
  "requstDt": "2026-02-11T16:20:00",
  "rqesterId": "user123",
  "rqesterNm": "John Doe",
  "srvcRsponsSj": "System access request",
  "srvcRsponsCn": "Need access to HR portal",
  "chargerId": "handler456",
  "chargerUserNm": "Jane Smith",
  "processDt": "2026-02-11T16:25:00",
  "priority": "3",
  "status": "PROCESSING"
}
```

**Access:** All authenticated users (role-based: can only see accessible SRs)

---

### 3. List Service Requests

**Request:**

```http
GET /api/sr/list
Authorization: Bearer <JWT_TOKEN>
```

**Response:**

```json
{
  "success": true,
  "total": 15,
  "data": [
    {
      "srvcRsponsNo": "SR-202501-001",
      "requstDt": "2026-02-11T16:20:00",
      "rqesterId": "user123",
      "srvcRsponsSj": "System access request",
      "priority": "3"
    },
    {
      "srvcRsponsNo": "SR-202501-002",
      "requstDt": "2026-02-11T15:30:00",
      "rqesterId": "user456",
      "srvcRsponsSj": "Password reset",
      "priority": "1"
    }
  ]
}
```

**Access:** All authenticated users (results filtered by role and user)

---

### 4. Update Request Details

**Request:**

```http
PUT /api/sr/SR-202501-001/request
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcRsponsSj": "Updated subject",
  "srvcRsponsCn": "Updated description"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Updated successfully"
}
```

**Access:** Requester (before SR is assigned), Manager, Admin

---

### 5. Assign Handler to Service Request

**Request:**

```http
PUT /api/sr/SR-202501-001/assign-handler
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "chargerId": "handler456",
  "chargerUserNm": "Jane Smith"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Handler assigned successfully (ID: handler456)",
  "assignedHandlerId": "handler456",
  "assignedHandlerName": "Jane Smith"
}
```

**Access:** Manager, Admin

---

### 6. Record First Response (Handler Acknowledges SR)

**Request:**

```http
PUT /api/sr/SR-202501-001/receive
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "rspons1stDt": "2026-02-11T16:25:00",
  "rspons1stMt": "SR acknowledged, will start investigation"
}
```

**Response:**

```json
{
  "success": true,
  "message": "SR received and acknowledged"
}
```

**Access:** Handler, Manager, Admin

---

### 7. Record First Response to Requester

**Request:**

```http
PUT /api/sr/SR-202501-001/response-1st
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "processMt": "Initial investigation shows...",
  "estimatedCompletionDate": "2026-02-13"
}
```

**Response:**

```json
{
  "success": true,
  "message": "First response recorded"
}
```

**Access:** Handler, Manager, Admin

---

### 8. Record Processing Activity

**Request:**

```http
PUT /api/sr/SR-202501-001/process
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcProcessDtls": "Work in progress...",
  "processMt": "Investigating infrastructure issue"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Processing activity recorded"
}
```

**Access:** Handler, Manager, Admin

---

### 9. Update Processing Status

**Request:**

```http
PUT /api/sr/SR-202501-001/sr-process
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "processDt": "2026-02-11T17:00:00",
  "srvcProcessDtls": "Applied system patch"
}
```

**Response:**

```json
{
  "success": true,
  "message": "SR processing status updated"
}
```

**Access:** Handler, Manager, Admin

---

### 10. Verify SR Completion

**Request:**

```http
PUT /api/sr/SR-202501-001/verify
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcVerifyDtls": "Access granted successfully",
  "cnfrmrId": "manager123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "SR verified and completed"
}
```

**Access:** Manager, Admin, authenticated users

---

### 11. Finish/Close SR

**Request:**

```http
PUT /api/sr/SR-202501-001/finish
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcFinDtls": "Work completed, closed by manager"
}
```

**Response:**

```json
{
  "success": true,
  "message": "SR finished and closed"
}
```

**Access:** Manager, Admin

---

### 12. Record Evaluation/Feedback

**Request:**

```http
PUT /api/sr/SR-202501-001/evaluate
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "satisfactionScore": "5",
  "evalMt": "Excellent service, very responsive"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Evaluation recorded"
}
```

**Access:** All authenticated users

---

### 13. Request Re-evaluation/Re-open SR

**Request:**

```http
POST /api/sr/SR-202501-001/re-evaluate
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "reRequestMt": "Issue not fully resolved, reopening SR"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Re-evaluation requested"
}
```

**Access:** All authenticated users

---

### 14. Create Follow-up SR

**Request:**

```http
POST /api/sr/SR-202501-001/re-request
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "srvcRsponsSj": "Follow-up: Additional access needed",
  "srvcRsponsCn": "Now need access to financial system as well"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Follow-up SR created",
  "srNo": "SR-202501-003"
}
```

**Access:** All authenticated users

---

### 15. Delete/Cancel SR

**Request:**

```http
DELETE /api/sr/SR-202501-001
Authorization: Bearer <JWT_TOKEN>
```

**Response:**

```json
{
  "success": true,
  "message": "SR deleted successfully"
}
```

**Access:** Manager, Admin (soft delete - marks as deleted)

---

## Typical Workflow

### For Requester (R005):

1. **Create** a new SR via POST `/api/sr/create`
2. **View** their own SRs via GET `/api/sr/list`
3. **Track** specific SR status via GET `/api/sr/{id}`
4. **Provide feedback** via PUT `/api/sr/{id}/evaluate`
5. **Request re-evaluation** if needed via POST `/api/sr/{id}/re-evaluate`

### For Handler (R003):

1. **View** assigned SRs via GET `/api/sr/list`
2. **Acknowledge** SR via PUT `/api/sr/{id}/receive`
3. **Send initial response** via PUT `/api/sr/{id}/response-1st`
4. **Record work activity** via PUT `/api/sr/{id}/process`
5. **Update processing status** via PUT `/api/sr/{id}/sr-process`
6. **Pass to verification** (handler doesn't close, only manager can)

### For Manager (R001):

1. **View all** SRs via GET `/api/sr/list`
2. **Verify** completed work via PUT `/api/sr/{id}/verify`
3. **Finish** SR via PUT `/api/sr/{id}/finish`
4. **Review evaluations** and manage reopened SRs
5. **Delete** problematic SRs if needed via DELETE `/api/sr/{id}`

---

## Error Handling

All endpoints return error responses in the following format:

```json
{
  "success": false,
  "error": "Error message describing what went wrong"
}
```

HTTP Status Codes:

- `200 OK` - Successful operation
- `201 Created` - Resource successfully created
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - User doesn't have permission for this action
- `404 Not Found` - SR not found
- `500 Internal Server Error` - Server error

---

## Example Complete Workflow

### Step 1: Requester creates SR

```bash
curl -X POST http://localhost:8090/api/sr/create \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "srvcRsponsSj": "Need database access",
    "srvcRsponsCn": "Require read-only access to production database",
    "priority": "2"
  }'
```

Response: `SR-202501-005` created

### Step 2: Handler receives and acknowledges

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/receive \
  -H "Authorization: Bearer <HANDLER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "rspons1stDt": "2026-02-11T17:30:00"
  }'
```

### Step 3: Handler sends initial response

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/response-1st \
  -H "Authorization: Bearer <HANDLER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "processMt": "Will coordinate with DBA for access setup"
  }'
```

### Step 4: Handler records work

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/process \
  -H "Authorization: Bearer <HANDLER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "srvcProcessDtls": "DBA created read-only user account"
  }'
```

### Step 5: Manager verifies and closes

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/verify \
  -H "Authorization: Bearer <MANAGER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"srvcVerifyDtls": "Access verified working"}'
```

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/finish \
  -H "Authorization: Bearer <MANAGER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"srvcFinDtls": "Closed and resolved"}'
```

### Step 6: Requester provides feedback

```bash
curl -X PUT http://localhost:8090/api/sr/SR-202501-005/evaluate \
  -H "Authorization: Bearer <REQUESTER_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"satisfactionScore": "5", "evalMt": "Fast and professional"}'
```

---

## API Base URL

The API is accessible at:

- **Local Development**: `http://localhost:8090/api/sr`
- **Port**: `8090` (can be changed in `application.yml`)
- **Authentication**: Keycloak JWT required for all protected endpoints
- **Health Check**: `GET http://localhost:8090/api/public/health`
