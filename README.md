# ITSM API - IT Service Management Backend

A secure, enterprise-grade IT Service Management (ITSM) REST API built with Spring Boot, Spring Security, and Keycloak. This application implements comprehensive role-based authorization for managing Service Requests (SR) with a complete workflow lifecycle, supporting 6 user types with distinct permissions and responsibilities.

## Project Overview

**ITSM API** provides a robust backend for IT service request management with:

- **Multi-tenancy Support**: 6 user types (R000-R005) with role-based access control
- **Service Request (SR) Workflow**: Complete lifecycle from creation to evaluation
- **OAuth2/JWT Security**: Keycloak integration with user attribute extraction
- **Audit Trail**: All operations logged with user ID, type, and timestamp
- **MyBatis ORM**: Efficient SQL-based data access with mapper patterns
- **RESTful API**: Full CRUD + workflow state transitions

## Technology Stack

| Component       | Version | Purpose                      |
| --------------- | ------- | ---------------------------- |
| Java            | 17      | Runtime                      |
| Spring Boot     | 4.0.2   | Web framework                |
| Spring Security | 4.0.2   | OAuth2 Resource Server       |
| Keycloak        | Latest  | Identity & Access Management |
| MyBatis         | Latest  | SQL mapper ORM               |
| MySQL           | 8.0+    | Relational database          |
| Lombok          | Latest  | Reduce boilerplate           |
| AspectJ         | Latest  | AOP implementation           |
| Maven           | 3.6+    | Build automation             |

## User Types & Roles

The system defines 6 user types with distinct permissions and workflow responsibilities:

| Code     | Role    | Description           | Capabilities                                |
| -------- | ------- | --------------------- | ------------------------------------------- |
| **R000** | TEMP    | Temporary User        | Limited access, viewing only                |
| **R001** | MNGR    | Manager/Administrator | Full SR lifecycle + pre-config workflow     |
| **R002** | CSTMR   | Customer/End User     | Create SR, update requests, evaluate        |
| **R003** | CHARGER | Service Handler       | Receive, respond, process, verify, complete |
| **R004** | CNSLT   | Consultant            | Advisory/support role                       |
| **R005** | R005    | Custom/Reserved       | View own SRs + assigned work                |

## Service Request (SR) Workflow

### Complete Lifecycle Flow

```
┌─────────────────────────────────────────────────────────┐
│ REQUEST (R002: Customer)                                │
│ • Create SR with title, description, attachments        │
│ • Auto-filled: name, email, phone from user info        │
│ • Subject limit: 40 characters                           │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ RECEIVE (R003: Handler) ← CRITICAL GATE                │
│ • Handler receives & acknowledges SR                    │
│ • Sets: RSPONS_1ST_DT = NOW()                          │
│ • Auto-determines verification needed (if type=Z1)     │
│ ⚠️  BLOCKS: All request updates after this point        │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ RESPONSE-FIRST (R003)                                   │
│ • Provide initial assessment/response                   │
│ • Set: Processing Standard, Difficulty Level           │
│ • Can trigger charger pre-assignment                    │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ PROCESS (R003)                                          │
│ • Execute actual work (code change, deploy, etc)       │
│ • Document: srvcProcessDtls                            │
│ • Track: Data updates, Program updates, Installation   │
│ • Set: PROCESS_DT = NOW()                              │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ VERIFY (R003) [OPTIONAL]                               │
│ • Only if service type = 'Z1'                          │
│ • Quality/result verification                           │
│ • Set: VERIFY_DT = NOW()                               │
│ • Record: srvcVerifyDtls                               │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ FINISH (R003)                                           │
│ • Mark work complete                                    │
│ • Record completion details: srvcFinDtls               │
│ • Set: FINISH_DT = NOW()                               │
└─────────┬───────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────────┐
│ EVALUATION (R002: Customer)                             │
│ • Rate service quality                                  │
│ • Provide feedback                                      │
│ • Option: Re-request if not satisfied                  │
└─────────────────────────────────────────────────────────┘
```

### Critical Business Rules

#### Rule 1: Request Edit Lockdown

Once first response is received, request details **CANNOT** be updated:

```java
// SrvcRsponsApiController.java - updateRequest() method
if (existing.getRspons1stDt() != null) {
    return ERROR: "SR already received; request update is not allowed"
}
```

Data integrity enforced: Requesters cannot modify requirements after handler acknowledgment.

#### Rule 2: Manager Pre-Configuration

Managers (R001) can pre-set all workflow parameters during creation:

```java
// POST /api/v1/sr/manager
{
  "srvcRsponsSj": "Install new application",
  "srvcRsponsCn": "Deploy billing system",
  "rqesterId": "user123",
  "chargerId": "handler456",           // Pre-assign handler
  "processStdrCode": "S201",            // Processing standard (SLA)
  "changeDfflyCode": "S004",            // Change difficulty
  "srvcRsponsBasisCode": "S301"         // Communication channel
}
```

#### Rule 3: Verification Auto-Detection

Verification stage is conditional based on service type:

```java
// updateReceive() method
CmmnCodeVO code = cmmnCodeService.retrieve(new CmmnCodeVO(request.getTrgetSrvcCode()));
if (resolved != null && "Z1".equals(resolved.getCmmnCodeSubNm1())) {
    request.setVerifyYn("Y");  // Verification required
} else {
    request.setVerifyYn("N");  // Skip verification
}
```

## Database Schema

### Core Table: TB_SRVC_RSPONS

Main entity storing the complete SR lifecycle:

| Column                     | Type         | Key Details                                   |
| -------------------------- | ------------ | --------------------------------------------- |
| **SRVC_RSPONS_NO**         | VARCHAR(20)  | PK, Format: SR-YYMM-NNN (auto-generated)      |
| **REQUST_DT**              | DATETIME     | Request creation timestamp                    |
| **PROCESS_MT**             | VARCHAR(6)   | Derived: YYYYMM format of requstDt            |
| **RQESTER_ID**             | VARCHAR(50)  | Requester user ID                             |
| **RQESTER_NM**             | VARCHAR(100) | Requester full name                           |
| **RQESTER_EMAIL**          | VARCHAR(100) | Requester email                               |
| **RQESTER_PSITN**          | VARCHAR(100) | Requester position                            |
| **RQESTER_CTTPC**          | VARCHAR(20)  | Requester phone                               |
| **RSPONS_1ST_DT**          | DATETIME     | First response timestamp (stage gate)         |
| **CHARGER_ID**             | VARCHAR(50)  | Service handler/charger user ID               |
| **PROCESS_STDR_CODE**      | VARCHAR(20)  | SLA/Processing standard (FK: TB_CMMN_CODE)    |
| **PROCESS_TERM**           | VARCHAR(50)  | Processing period/duration                    |
| **PROCESS_DT**             | DATETIME     | Work completion timestamp                     |
| **CHANGE_DFFLY_CODE**      | VARCHAR(20)  | Difficulty level (FK: TB_CMMN_CODE, type=S0)  |
| **SRVC_RSPONS_CL_CODE**    | VARCHAR(20)  | SR classification (FK: TB_CMMN_CODE, type=S1) |
| **SRVC_RSPONS_BASIS_CODE** | VARCHAR(20)  | Comm channel (FK: TB_CMMN_CODE, type=S3)      |
| **TRGET_SRVC_CODE**        | VARCHAR(20)  | Service code (FK: TB_CMMN_CODE, type=A0)      |
| **TRGET_SRVC_DETAIL_CODE** | VARCHAR(20)  | Service detail (FK: TB_CMMN_CODE, type=A2)    |
| **SRVC_RSPONS_SJ**         | VARCHAR(255) | SR subject (stored max 40 chars)              |
| **SRVC_RSPONS_CN**         | LONGTEXT     | Detailed description                          |
| **SRVC_PROCESS_DTLS**      | LONGTEXT     | Processing details/notes                      |
| **SRVC_VERIFY_DTLS**       | LONGTEXT     | Verification notes (if Z1)                    |
| **SRVC_FIN_DTLS**          | LONGTEXT     | Completion details                            |
| **VERIFY_DT**              | DATETIME     | Verification completion time                  |
| **VERIFY_ID**              | VARCHAR(50)  | Verification by user ID                       |
| **FINISH_DT**              | DATETIME     | Work finish timestamp                         |
| **FINISH_ID**              | VARCHAR(50)  | Finished by user ID                           |
| **DATA_UPDT_YN**           | CHAR(1)      | Data change flag (Y/N)                        |
| **PROGRM_UPDT_YN**         | CHAR(1)      | Program update flag (Y/N)                     |
| **STOP_INSTL_YN**          | CHAR(1)      | Service stop flag (Y/N)                       |
| **NONE_STOP_INSTL_YN**     | CHAR(1)      | Non-stop installation (Y/N)                   |
| **INSTL_YN**               | CHAR(1)      | Installation flag (Y/N)                       |
| **INFRA_OPERT_YN**         | CHAR(1)      | Infrastructure operation (Y/N)                |
| **CNFRMR_ID**              | VARCHAR(50)  | Confirmer user ID                             |
| **FNCT_IMPRVM_NO**         | VARCHAR(50)  | Function improvement ticket (FK)              |
| **WDTB_CNFIRM_NO**         | VARCHAR(50)  | Deployment confirmation (FK)                  |
| **INFRA_OPERT_NO**         | VARCHAR(50)  | Infrastructure operation (FK)                 |
| **RE_SRVC_RSPONS_NO**      | VARCHAR(20)  | Re-request reference (FK: self)               |
| **RE_REQUEST_DT**          | DATETIME     | Re-request timestamp                          |
| **REF_IDS**                | VARCHAR(500) | Related user IDs (comma-separated)            |
| **REQUST_ATCHMNFL_ID**     | VARCHAR(50)  | Request attachment UUID                       |
| **RSPONS_ATCHMNFL_ID**     | VARCHAR(50)  | Response attachment UUID                      |
| **DELETE_YN**              | CHAR(1)      | Soft delete flag (Y/N, default: N)            |
| **CREAT_DT**               | DATETIME     | Created timestamp                             |
| **CREAT_ID**               | VARCHAR(50)  | Created by user ID                            |
| **UPDT_DT**                | DATETIME     | Updated timestamp                             |
| **UPDT_ID**                | VARCHAR(50)  | Updated by user ID                            |

### Related Tables

- **TB_CMMN_CODE**: Common code lookup (Process Standards, Difficulties, Classifications)
- **TB_LOGIN_INFO**: User master data
- **TB_SYS_CHARGER**: Service-to-handler assignment mappings
- **TB_WDTB_CNFIRM**: Release/deployment confirmations
- **TB_INFRA_OPERT**: Infrastructure operation tracking
- **TB_ATCHMNFL**: File attachment storage

## Project Structure

```
src/main/
├── java/com/example/itsm_api/
│   ├── ItsmApiApplication.java                  # Main entry point
│   ├── controller/
│   │   ├── SrvcRsponsApiController.java         # REST endpoints (21 endpoints)
│   │   ├── ProtectedController.java             # Authorization examples
│   │   └── PublicController.java                # Unrestricted endpoints
│   ├── service/
│   │   └── SrvcRsponsService.java              # Business logic layer
│   ├── dao/
│   │   └── SrvcRsponsMapper.java (interface)   # MyBatis mapper
│   ├── vo/
│   │   └── SrvcRsponsVO.java                   # Value Object/DTO
│   └── security/
│       ├── CustomUserPrincipal.java            # JWT attribute mapper
│       ├── AuthorizationService.java           # Authorization logic
│       ├── AuthorizationAspect.java            # AOP interceptor
│       ├── RequireUserTyCode.java              # Custom annotation
│       ├── RequireUserSttusCode.java           # Custom annotation
│       └── SecurityConfig.java                 # OAuth2 configuration
└── resources/
    ├── application.yml                         # Spring Boot config
    ├── mapper/
    │   └── SrvcRsponsMapper.xml               # MyBatis SQL mapping (40+ queries)
    └── static/, templates/
```

## API Endpoints

### List & Query Endpoints

```
GET /api/v1/sr                      # All SRs (paginated, user-filtered)
GET /api/v1/sr/requests             # Pending requests (rspons1stDt = null)
GET /api/v1/sr/receives             # Received SRs (not yet processed)
GET /api/v1/sr/processes            # In-progress SRs (handler view)
GET /api/v1/sr/verifications        # Awaiting verification (Z1 type only)
GET /api/v1/sr/finishes             # Ready for completion
GET /api/v1/sr/evaluations          # Awaiting customer evaluation
```

### CRUD Operations

```
POST   /api/v1/sr                    # Create SR (R002+ customer)
POST   /api/v1/sr/manager            # Create SR (R001 manager only)
GET    /api/v1/sr/{id}               # Retrieve full SR details
PUT    /api/v1/sr/{id}               # Full SR update
DELETE /api/v1/sr/{id}               # Soft delete SR
```

### Workflow State Transitions

```
PUT /api/v1/sr/{id}/request          # Update request details
                                     # ⚠️ Blocked if rspons1stDt != null

PUT /api/v1/sr/{id}/receive          # R003: Receive & acknowledge
                                     # Sets: rspons1stDt, verifyYn

PUT /api/v1/sr/{id}/response-first   # R003: Initial response
                                     # Sets: processStdrCode, changeDfflyCode

PUT /api/v1/sr/{id}/process          # R003: Perform work
                                     # Sets: processDt, srvcProcessDtls

PUT /api/v1/sr/{id}/verify           # R003: Verify result (if Z1)
                                     # Sets: verifyDt, srvcVerifyDtls

PUT /api/v1/sr/{id}/finish           # R003: Complete work
                                     # Sets: finishDt, srvcFinDtls

PUT /api/v1/sr/{id}/evaluation       # R002: Evaluate service
                                     # Sets: changeDfflyCode (rating)

POST /api/v1/sr/{id}/re-request      # R002: Create new SR from evaluation
                                     # Copies relevant fields, creates new SRVC_RSPONS_NO
```

### Autocomplete/Lookup Endpoints

```
GET /api/v1/sr/requesters            # Distinct requester names/contact info
GET /api/v1/sr/requesters/first      # Initial request contact persons
GET /api/v1/sr/numbers               # SR number autocomplete (for charger)
```

### Query Parameters (All List Endpoints)

```
pageIndex=1                    # Page number (default: 1)
recordCountPerPage=15          # Records per page (default: 15)
srvcRsponsNo=SR-2602           # Filter by SR number
srvcRsponsSj=Install           # Filter by subject (LIKE)
srvcRsponsCn=Network           # Filter by content (LIKE)
trgetSrvcCode=SVC001           # Filter by service code
srvcRsponsClCode=S102           # Filter by SR classification
processStdrCode=S201            # Filter by processing standard
rqesterId=user123              # Filter by requester
cnfrmrId=user456               # Filter by confirmer
chargerId=user789              # Filter by assigned handler
processMt=202602               # Filter by process month
excludeprocessYn=Y             # Only unprocessed SRs
```

## Role-Based API Access Control

### Customer (R002)

```bash
# ✅ Can: Create SR
POST /api/v1/sr -d '{
  "srvcRsponsSj": "Install app",
  "srvcRsponsCn": "Need billing system",
  "trgetSrvcCode": "SVC001"
}'
# Auto-filled: rqesterId, rqesterNm, rqesterEmail, rqesterCttpc

# ✅ Can: Update request BEFORE first response
PUT /api/v1/sr/SR-2602-001/request -d '{ "srvcRsponsSj": "Updated" }'
# Only works if rspons1stDt is null

# ❌ Cannot: Update request AFTER first response
PUT /api/v1/sr/SR-2602-001/request -d '...'
# Returns 409 Conflict: "SR already received; request update is not allowed"

# ✅ Can: View own SRs only
GET /api/v1/sr?rqesterId=user123
# Filtered by: SR.RQESTER_ID = user123 OR LOCATE(user123, SR.REF_IDS)

# ✅ Can: Evaluate & re-request
PUT /api/v1/sr/SR-2602-001/evaluation -d '{ "changeDfflyCode": "S003" }'
POST /api/v1/sr/SR-2602-001/re-request -d '{ "srvcRsponsSj": "...new request..." }'
```

### Handler/Charger (R003)

```bash
# ✅ Can: Receive SR (acknowledge & set first response)
PUT /api/v1/sr/SR-2602-001/receive -d '{
  "trgetSrvcCode": "SVC001",
  "srvcRsponsBasisCode": "S301",
  "processStdrCode": "S201"
}'
# Sets: rspons1stDt = NOW(), verifyYn determined by service type

# ✅ Can: Provide first response
PUT /api/v1/sr/SR-2602-001/response-first -d '{
  "changeDfflyCode": "S002",
  "srvcRsponsClCode": "S102"
}'

# ✅ Can: Process work
PUT /api/v1/sr/SR-2602-001/process -d '{
  "srvcProcessDtls": "Updated dependency X, recompiled code",
  "progrmUpdtYn": "Y",
  "dataUpdtYn": "N"
}'

# ✅ Can: Verify (conditional - only if verifyYn = Y)
PUT /api/v1/sr/SR-2602-001/verify -d '{
  "srvcVerifyDtls": "Tested in production, all working"
}'

# ✅ Can: Mark complete
PUT /api/v1/sr/SR-2602-001/finish -d '{
  "srvcFinDtls": "Completed successfully, ready for customer use"
}'

# ✅ Can: View assigned/accessible SRs only
GET /api/v1/sr/processes
# Filtered by: charger assignment OR service type assignment in TB_SYS_CHARGER
```

### Manager (R001)

```bash
# ✅ Can: Create SR with full pre-configuration
POST /api/v1/sr/manager -d '{
  "rqesterId": "user123",
  "rqesterNm": "John Doe",
  "srvcRsponsSj": "Urgent: Deploy billing system",
  "srvcRsponsCn": "Deploy to production tonight",
  "trgetSrvcCode": "SVC001",
  "chargerId": "handler456",           # Pre-assign handler
  "processStdrCode": "S201",            # Set SLA
  "changeDfflyCode": "S004",            # Set difficulty
  "srvcRsponsBasisCode": "S301"         # Set communication channel
}'
# Result: SR created with all workflow parameters pre-set

# ✅ Can: View ALL SRs (no user filtering)
GET /api/v1/sr
# Returns all SRs across all users

# ✅ Can: Access all workflow endpoints
# Can perform any operation any other role can, plus:
# - Pre-assign chargers
# - Set processing standards/SLAs
```

## Security & Authentication

### JWT Token Flow

```
1. Client requests token from Keycloak
2. Keycloak issues JWT with claims:
   {
     "sub": "john.doe",
     "name": "John Doe",
     "email": "john@company.com",
     "userTyCode": ["R002"],          ← User type from Keycloak
     "userSttusCode": ["U002"],       ← Status (Active/Inactive)
     "deptCd": "IT-001",              ← Department code
     "position": "Senior Specialist"
   }

3. Client sends JWT in Authorization header:
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...

4. Spring Security validates JWT signature & expiry
   (issuer-uri: https://keycloak/auth/realms/itsm)

5. CustomUserPrincipal extracts attributes from claims

6. Endpoint checks authorization based on userTyCode
```

### Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/itsm_db
    username: itsm_user
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://keycloak.example.com/auth/realms/itsm
          jwk-set-uri: https://keycloak.example.com/auth/realms/itsm/protocol/openid-connect/certs

server:
  port: 8080
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Keycloak 18+

### Build

```bash
./mvnw clean package
```

### Run

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`

## Implementation Best Practices

### 1. Date Handling

- Dates auto-default to `NOW()` if not provided
- UI passes: `requstDtDateDisplay` + `requstDtTimeDisplay`
- Backend constructs via `makeRequstDt()` helper
- Database stores as DATETIME

### 2. Subject Truncation

```java
if (request.getSrvcRsponsSj() != null) {
    request.setSrvcRsponsSj(request.getSrvcRsponsSj()
        .substring(0, Math.min(request.getSrvcRsponsSj().length(), 40)));
}
```

### 3. Attachment Handling

```java
if (isEmpty(request.getRequstAtchmnflId())) {
    request.setRequstAtchmnflId(UUID.randomUUID().toString());
}
```

Each SR has separate attachment UUIDs for request and response files.

### 4. Audit Trail

All operations record:

- `CREAT_DT`, `CREAT_ID` - Initial creation
- `UPDT_DT`, `UPDT_ID` - Last modification
- Supports full history reconstruction

### 5. Pagination

- Default: 15 records/page
- Always returns: `totalCount` for UI paging controls
- Sorting: Varies by endpoint (priority, date, creation order)

## Documentation

- [AUTHORIZATION_GUIDE.md](AUTHORIZATION_GUIDE.md) - Security & custom annotations
- [HELP.md](HELP.md) - Spring Boot reference
- [SrvcRsponsMapper.xml](src/main/resources/mapper/SrvcRsponsMapper.xml) - 40+ SQL queries

## Building & Testing

```bash
# Compile only
./mvnw clean compile

# Run unit tests
./mvnw test

# Build JAR/WAR
./mvnw clean package -DskipTests
```

## Troubleshooting

### "SR already received; request update is not allowed"

- **Cause**: Attempted to update request after handler received it
- **Fix**: This is expected business logic - requests lock post-response
- **Workaround**: Create re-request via evaluation flow

### Handler can't see assigned SRs

- **Cause**: Not assigned to service codes in TB_SYS_CHARGER
- **Fix**: Add handler assignment record or set CHARGER_ID directly

### Verification stage not appearing

- **Cause**: Service type is not 'Z1'
- **Fix**: Verify CMMN_CODE_SUB_NM1 = 'Z1' for the service code

## License

MIT License

## Support

For questions: see [AUTHORIZATION_GUIDE.md](AUTHORIZATION_GUIDE.md)
