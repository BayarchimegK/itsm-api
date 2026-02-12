# ITSM API Authorization Implementation Guide

## Overview

This guide explains how the role-based authorization framework integrates with the ITSM API to enforce user type-based access control on Service Request (SR) operations.

## Architecture Components

### 1. **SrAuthorizationService** (`SrAuthorizationService.java`)

Core authorization service that defines role constants and provides verification methods for each SR operation.

**Roles Defined:**

- `ROLE_TEMP` (R000): Temporary user - No SR creation rights
- `ROLE_MANAGER` (R001): Manager - Can create SR, view all SRs, assign handlers
- `ROLE_CUSTOMER` (R002): Customer - Can create SR, evaluate completed SR, re-request
- `ROLE_CHARGER` (R003): Handler - Can receive, process, verify, finish SR
- `ROLE_CONSULTANT` (R004): Consultant - Can view and consult on SR (read-only)
- `ROLE_CUSTOM` (R005): Custom/Specialist - Can view own SRs, escalate issues

**Verification Methods:**

```java
void verifyCanCreateSr(String userTyCode)           // R002, R001
void verifyCanCreateSrAsManager(String userTyCode)  // R001 only
void verifyCanReceiveSr(String userTyCode)          // R003 only
void verifyCanProcessSr(String userTyCode)          // R003 only
void verifyCanVerifySr(String userTyCode)           // R003 only
void verifyCanFinishSr(String userTyCode)           // R003 only
void verifyCanEvaluateSr(String userTyCode)         // R002 only
void verifyCanViewSrList(String userTyCode)         // All authenticated
```

### 2. **JwtUserTypeCodeInterceptor** (`JwtUserTypeCodeInterceptor.java`)

Intercepts all HTTP requests and extracts `userTyCode` from JWT token claims, storing it in request attributes for downstream access.

**JWT Claim Extraction Strategy:**

- Primary: `userTyCode` claim
- Fallback 1: `user_type_code` claim (underscore format)
- Fallback 2: `custom:userTyCode` claim (custom namespace)
- Fallback 3: Extract from `roles` claim (e.g., "R001-manager" → "R001")
- Fallback 4: Extract from `resource_access[client].roles` (Keycloak client scope)

**JWT Token Example (Keycloak):**

```json
{
  "sub": "user-123",
  "name": "John Manager",
  "email": "john@example.com",
  "userTyCode": "R001",
  "department": "IT Management",
  "realm_access": {
    "roles": ["user", "manager"]
  }
}
```

### 3. **SrAuthorizationAspect** (`SrAuthorizationAspect.java`)

AOP aspect that intercepts controller method calls and enforces authorization checks before execution.

**Interception Pattern:**

```
Client Request
  → JwtUserTypeCodeInterceptor (extract userTyCode from JWT)
  → SrAuthorizationAspect (verify authorization using userTyCode)
  → SrvcRsponsApiController (execute endpoint logic)
  → SrvcRsponsService (apply role-based data filtering)
  → MyBatis Mapper (execute SQL with role-based WHERE clauses)
  → Database
```

### 4. **SecurityExceptionHandler** (`SecurityExceptionHandler.java`)

Global exception handler that catches `AccessDeniedException` and returns HTTP 403 Forbidden with descriptive error messages.

**Response Format:**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Access Denied",
  "message": "Only managers can use /manager endpoint"
}
```

## Implementation Steps

### Step 1: Enable AOP in SecurityConfig

The `SecurityConfig.java` has been updated with:

```java
@EnableAspectJAutoProxy  // Enable AspectJ AOP
public class SecurityConfig implements WebMvcConfigurer {

    // Register JWT interceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtUserTypeCodeInterceptor);
    }
}
```

### Step 2: Configure Application Properties

Update `application.yml` to configure Keycloak and enable AOP:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/itsm # Keycloak realm
          jwk-set-uri: http://localhost:8080/realms/itsm/protocol/openid-connect/certs

  aop:
    auto: true # Enable AOP auto-configuration
    proxy-target-class: true # Use CGLIB proxying for all beans
```

### Step 3: Update SrvcRsponsApiController

Integrate authorization checks and pass `userTyCode` to service layer:

**Example: Create SR Endpoint**

```java
@RestController
@RequestMapping("/api/v1/sr")
public class SrvcRsponsApiController {

    @Autowired
    private SrvcRsponsService srvcRsponsService;

    /**
     * Create Service Request
     * POST /api/v1/sr
     * Authorized: R002 (Customer), R001 (Manager via /sr/manager endpoint)
     * Authorization checked by: SrAuthorizationAspect.authorizeCreateSr()
     */
    @PostMapping
    public ResponseEntity<SrvcRsponsResponseVO> create(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestBody SrvcRsponsVO vo,
            HttpServletRequest request) {

        // Extract user info from request attributes (set by JwtUserTypeCodeInterceptor)
        String userTyCode = (String) request.getAttribute("userTyCode");
        String userId = (String) request.getAttribute("userId");

        // Set user context on VO
        vo.setUserTyCode(userTyCode);
        vo.setUserId(userId != null ? userId : xUserId);

        // SrAuthorizationAspect will call:
        // authorizationService.verifyCanCreateSr(userTyCode)
        // If verification fails, AccessDeniedException is thrown
        // SecurityExceptionHandler catches it and returns HTTP 403

        SrvcRsponsResponseVO response = srvcRsponsService.create(vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create Service Request (Manager Only)
     * POST /api/v1/sr/manager
     * Authorized: R001 (Manager only)
     * Authorization checked by: SrAuthorizationAspect.authorizeCreateSrAsManager()
     */
    @PostMapping("/manager")
    public ResponseEntity<SrvcRsponsResponseVO> createForManager(
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestBody SrvcRsponsVO vo,
            HttpServletRequest request) {

        String userTyCode = (String) request.getAttribute("userTyCode");
        String userId = (String) request.getAttribute("userId");

        vo.setUserTyCode(userTyCode);
        vo.setUserId(userId != null ? userId : xUserId);

        // SrAuthorizationAspect will call:
        // authorizationService.verifyCanCreateSrAsManager(userTyCode)
        // Only R001 (ROLE_MANAGER) passes verification

        SrvcRsponsResponseVO response = srvcRsponsService.createForMngr(vo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### Step 4: Update SrvcRsponsService for Role-Based Filtering

Modify service methods to pass `userTyCode` to mapper for data filtering:

**Example: Retrieve SR List**

```java
@Service
public class SrvcRsponsService {

    @Autowired
    private SrvcRsponsMapper srvcRsponsMapper;

    /**
     * Retrieve SR list with role-based filtering
     * R001 (Manager): See all SRs
     * R003 (Handler): See only assigned services (via TB_SYS_CHARGER)
     * R005 (Custom): See only own SRs
     */
    public List<SrvcRsponsVO> retrieveSrProcList(
            SrvcRsponsVO queryVO,
            String userTyCode,
            String userId,
            int pageNum,
            int pageSize) {

        // Set user context on VO for mapper filtering
        queryVO.setUserTyCode(userTyCode);
        queryVO.setUserId(userId);
        queryVO.setPageNum(pageNum);
        queryVO.setPageSize(pageSize);

        // MyBatis mapper applies role-based WHERE clause
        // If userTyCode = "R005": AND (SR.RQESTER_ID = userId OR LOCATE(userId, SR.REF_IDS))
        // If userTyCode = "R003": AND SR.TRGET_SRVC_CODE IN (SELECT ... FROM TB_SYS_CHARGER WHERE USER_ID = userId)
        // If userTyCode = "R001": No filtering (see all)

        return srvcRsponsMapper.retrieveSrProcList(queryVO);
    }
}
```

## Authorization Rules by Operation

### SR Creation

| Operation         | Endpoint                  | Allowed Roles | Special Rules                               |
| ----------------- | ------------------------- | ------------- | ------------------------------------------- |
| Create SR         | `POST /api/v1/sr`         | R001, R002    | Customer (R002) auto-fills from UserService |
| Manager Create SR | `POST /api/v1/sr/manager` | R001 only     | Manager pre-configures handler assignment   |

### SR Workflow Operations

| Operation    | Endpoint                             | Allowed Roles | Stage Transition               |
| ------------ | ------------------------------------ | ------------- | ------------------------------ |
| Receive      | `PUT /api/v1/sr/{id}/receive`        | R003          | REQUEST → RECEIVE              |
| Set Response | `PUT /api/v1/sr/{id}/response-first` | R003          | RECEIVE → PROCESS              |
| Process      | `PUT /api/v1/sr/{id}/process`        | R003          | PROCESS → PROCESS (continue)   |
| Verify       | `PUT /api/v1/sr/{id}/verify`         | R003          | PROCESS → VERIFY (conditional) |
| Finish       | `PUT /api/v1/sr/{id}/finish`         | R003          | VERIFY/PROCESS → FINISH        |
| Evaluate     | `PUT /api/v1/sr/{id}/evaluation`     | R002          | FINISH → EVALUATION            |
| Re-request   | `POST /api/v1/sr/{id}/re-request`    | R002          | EVALUATION → REQUEST           |

### Request Edit Lockdown

**Critical Business Rule:** Once handler receives SR (RSPONS_1ST_DT is set), customer cannot modify request.

**Implementation in Controller:**

```java
@PutMapping("/{id}/request")
public ResponseEntity<SrvcRsponsResponseVO> updateRequest(
        @PathVariable String id,
        @RequestBody SrvcRsponsVO vo,
        HttpServletRequest request) {

    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // Verify customer is updating their own request
    if (!userTyCode.equals(SrAuthorizationService.ROLE_CUSTOMER)) {
        throw new AccessDeniedException("Only customers can update request");
    }

    // Fetch existing SR
    SrvcRsponsVO existing = srvcRsponsService.retrieve(id);

    // CRITICAL: Check lockdown - if first response received, no edits allowed
    if (existing.getRspons1stDt() != null) {
        throw new IllegalStateException(
            "SR already received; request update is not allowed. " +
            "First response date: " + existing.getRspons1stDt());
    }

    // Only allow update if in REQUEST stage
    if (!existing.getSrProcsStus().equals(SrvcRsponsVO.STATUS_REQUEST)) {
        throw new IllegalStateException("Can only update request in REQUEST stage");
    }

    vo.setUserTyCode(userTyCode);
    vo.setUserId(userId);

    SrvcRsponsResponseVO response = srvcRsponsService.updateRequest(vo);
    return ResponseEntity.ok(response);
}
```

## Data Filtering by Role

### Manager (R001)

- **View:** All SRs in system
- **Create:** Direct SR creation with full control
- **Assign:** Can assign handlers directly
- **Filter in MyBatis:** No WHERE clause (see all)

### Customer (R002)

- **View:** SRs they created (RQESTER_ID) or where they're referenced
- **Create:** Standard SR creation with auto-filled user info
- **Modify:** Can only edit REQUEST stage before RSPONS_1ST_DT is set
- **Evaluate:** Can evaluate after SR_FINISH stage
- **Filter in MyBatis:** `AND (SR.RQESTER_ID = #{userId} OR LOCATE(#{userId}, SR.REF_IDS) > 0)`

### Handler (R003)

- **View:** SRs assigned to their services (via TB_SYS_CHARGER)
- **Create:** Cannot create, only process assigned work
- **Receive:** Can receive (mark as received)
- **Process:** Can execute work
- **Verify:** Can verify completion (if required)
- **Finish:** Can mark SR as complete
- **Filter in MyBatis:** `AND SR.TRGET_SRVC_CODE IN (SELECT SYS_CODE FROM TB_SYS_CHARGER WHERE USER_ID = #{userId})`

### Custom Role (R005)

- **View:** Only SRs they created or are referenced in
- **Create:** Can create specialized requests
- **Process:** Limited to own items
- **Filter in MyBatis:** `AND (SR.RQESTER_ID = #{userId} OR LOCATE(#{userId}, SR.REF_IDS) > 0)`

## Configuration Examples

### Keycloak User Configuration

**Manager (R001)**

```json
{
  "username": "john.manager",
  "attributes": {
    "userTyCode": "R001"
  },
  "clientRoles": {
    "itsm-api": ["manager"]
  }
}
```

**Customer (R002)**

```json
{
  "username": "alice.customer",
  "attributes": {
    "userTyCode": "R002"
  },
  "clientRoles": {
    "itsm-api": ["customer"]
  }
}
```

**Handler (R003)**

```json
{
  "username": "bob.handler",
  "attributes": {
    "userTyCode": "R003"
  },
  "clientRoles": {
    "itsm-api": ["handler"]
  }
}
```

### JWT Token Example

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cC...
```

Decoded JWT payload:

```json
{
  "jti": "abc123def456",
  "exp": 1705325400,
  "iat": 1705325100,
  "iss": "http://localhost:8080/realms/itsm",
  "sub": "user-id-123",
  "typ": "Bearer",
  "azp": "itsm-api",
  "sess_state": "sess-abc123",
  "name": "John Manager",
  "email": "john.manager@example.com",
  "email_verified": true,
  "userTyCode": "R001",
  "department": "IT Management",
  "preferred_username": "john.manager",
  "given_name": "John",
  "family_name": "Manager",
  "realm_access": {
    "roles": ["default-roles-itsm", "user", "manager"]
  },
  "resource_access": {
    "itsm-api": {
      "roles": ["user", "manager", "sr-admin"]
    }
  }
}
```

## Testing Authorization

### Test 1: Manager Creates SR

```bash
curl -X POST http://localhost:8080/api/v1/sr/manager \
  -H "Authorization: Bearer <manager-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndNm": "Upgrade Database",
    "dmmndCntnt": "Please upgrade MySQL to 8.0.35"
  }'

# Expected: 201 Created
```

### Test 2: Customer Creates SR

```bash
curl -X POST http://localhost:8080/api/v1/sr \
  -H "Authorization: Bearer <customer-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndNm": "Password Reset",
    "dmmndCntnt": "I forgot my password"
  }'

# Expected: 201 Created
```

### Test 3: Customer Attempts Manager Endpoint

```bash
curl -X POST http://localhost:8080/api/v1/sr/manager \
  -H "Authorization: Bearer <customer-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndNm": "Upgrade Database",
    "dmmndCntnt": "Please upgrade MySQL"
  }'

# Expected: 403 Forbidden
# Response: {"status": 403, "error": "Access Denied", "message": "Only managers can use /manager endpoint"}
```

### Test 4: Handler Receives SR

```bash
curl -X PUT http://localhost:8080/api/v1/sr/SR-2401-001/receive \
  -H "Authorization: Bearer <handler-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "resoncPrcssDt": "2024-01-15"
  }'

# Expected: 200 OK
```

### Test 5: Customer Edited Request After First Response (Lockdown)

```bash
curl -X PUT http://localhost:8080/api/v1/sr/SR-2401-001/request \
  -H "Authorization: Bearer <customer-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndCntnt": "Updated requirement"
  }'

# Expected: 409 Conflict or 400 Bad Request
# Response: {"error": "Conflict", "message": "SR already received; request update is not allowed"}
```

## Troubleshooting

### Issue: "User type code not found in request"

**Cause:** JWT token doesn't contain `userTyCode` claim
**Solution:**

1. Verify Keycloak user has `userTyCode` attribute
2. Check JWT token decoder configuration
3. Ensure `JwtUserTypeCodeInterceptor` is registered

### Issue: Access Denied on allowed operations

**Cause:**

- Wrong role code in userTyCode
- Token has expired
- Interceptor not extracting userTyCode correctly
  **Solution:**

1. Verify user role mapping in Keycloak
2. Check token expiration time
3. Log JWT claims using debugger

### Issue: 500 Error on database filtering

**Cause:** MyBatis SQL error in role-based WHERE clause
**Solution:**

1. Check TB_SYS_CHARGER table exists and has handler-service mappings
2. Verify SrvcRsponsVO has userTyCode and userId set
3. Review MyBatis mapper for SQL syntax errors

## Integration Checklist

- [ ] Enable AOP in SecurityConfig with `@EnableAspectJAutoProxy`
- [ ] Register JwtUserTypeCodeInterceptor in WebMvcConfigurer
- [ ] Configure Keycloak issuer URI in application.yml
- [ ] Set AOP proxy-target-class to true
- [ ] Add @Autowired SrAuthorizationService to controller
- [ ] Extract userTyCode from request attributes in endpoints
- [ ] Call appropriate authorization verification before operation
- [ ] Pass userTyCode to service layer
- [ ] Update service methods to pass userTyCode to mapper
- [ ] Update MyBatis queries with role-based WHERE clauses
- [ ] Add exception handler for AccessDeniedException
- [ ] Test authorization with different role tokens
- [ ] Configure Keycloak users with userTyCode attributes
- [ ] Deploy and verify in test environment

## Summary

The authorization framework provides:

1. **Centralized**: SrAuthorizationService contains all rules
2. **Intercepted**: JWT claims extracted automatically
3. **Aspect-based**: Transparent AOP enforcement
4. **Filtered**: Role-based data filtering at database level
5. **Consistent**: Standard exception handling and responses
6. **Auditable**: Clear logging of authorization decisions

All SR operations are protected by role-based access control, ensuring data security and compliance with business rules.
