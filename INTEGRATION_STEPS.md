# ITSM API Authorization Framework - Integration Steps

## Overview

This document provides step-by-step instructions to integrate the role-based authorization framework into the ITSM API. The process involves minimal changes to existing code while adding comprehensive authorization enforcement.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Architecture Review](#architecture-review)
3. [Integration Steps](#integration-steps)
4. [Code Changes Required](#code-changes-required)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)
7. [Deployment Checklist](#deployment-checklist)

## Prerequisites

### Existing Components (Already in Place)

- ✅ Spring Boot 4.0.2 with Spring Security
- ✅ OAuth2 Resource Server with JWT
- ✅ Keycloak configured as identity provider
- ✅ MyBatis with role-based SQL filtering
- ✅ AspectJ AOP dependencies in pom.xml
- ✅ SrvcRsponsApiController with 21 endpoints
- ✅ SrvcRsponsService with business logic
- ✅ SrvcRsponsMapper with 40+ SQL queries

### New Components Created

- ✅ **SrAuthorizationService** - Role verification service
- ✅ **JwtUserTypeCodeInterceptor** - JWT claim extraction
- ✅ **SrAuthorizationAspect** - AOP authorization enforcement
- ✅ **SecurityExceptionHandler** - Global exception handling
- ✅ **SrvcRsponsApiControllerReference** - Template implementation

### Required Files

- ✅ IMPLEMENTATION_GUIDE.md - Architecture and authorization rules
- ✅ AUTHORIZATION_GUIDE.md - Existing authorization patterns
- ℹ️ INTEGRATION_STEPS.md - This file

## Architecture Review

### Flow Diagram

```
HTTP Request
    ↓
JwtUserTypeCodeInterceptor
    ├─ Extract userTyCode from JWT
    ├─ Extract userId from JWT
    └─ Store in request attributes
    ↓
SrvcRsponsApiController (Endpoint Method)
    ├─ Extract userTyCode from request.getAttribute()
    ├─ Extract userId from request.getAttribute()
    └─ Set both on SrvcRsponsVO
    ↓
SrAuthorizationAspect (AOP - Transparent)
    ├─ Intercept method execution
    ├─ Extract userTyCode from request
    ├─ Call authorizationService.verify*()
    └─ Throw AccessDeniedException if unauthorized
    ↓
    ├─ If Authorized → Continue to method body
    │    ↓
    │   SrvcRsponsService
    │    ├─ Pass userTyCode to mapper
    │    └─ Apply role-based data filtering
    │    ↓
    │   MyBatis Mapper
    │    ├─ Execute SQL with role-based WHERE
    │    └─ Return filtered results
    │
    └─ If Unauthorized → Throw AccessDeniedException
         ↓
    SecurityExceptionHandler
         ├─ Catch AccessDeniedException
         ├─ Format error response
         └─ Return HTTP 403 Forbidden
```

## Integration Steps

### Step 1: Update SecurityConfig

**File:** `src/main/java/com/example/itsm_api/security/SecurityConfig.java`

**Changes Made:**

- ✅ Added `@EnableAspectJAutoProxy` annotation
- ✅ Implemented `WebMvcConfigurer` interface
- ✅ Registered `JwtUserTypeCodeInterceptor` in `addInterceptors()`
- ✅ Added import for necessary Spring modules

**Status:** ✅ COMPLETE

**Verification:**

```bash
# Check that SecurityConfig has these annotations
grep -n "@EnableAspectJAutoProxy" src/main/java/com/example/itsm_api/security/SecurityConfig.java
grep -n "implements WebMvcConfigurer" src/main/java/com/example/itsm_api/security/SecurityConfig.java
grep -n "addInterceptors" src/main/java/com/example/itsm_api/security/SecurityConfig.java
```

### Step 2: Create Authorization Service

**File:** `src/main/java/com/example/itsm_api/security/SrAuthorizationService.java`

**Status:** ✅ COMPLETE

**Key Methods:**

```java
verifyCanCreateSr(String userTyCode)           // R002, R001
verifyCanCreateSrAsManager(String userTyCode)  // R001 only
verifyCanReceiveSr(String userTyCode)          // R003 only
verifyCanProcessSr(String userTyCode)          // R003 only
verifyCanVerifySr(String userTyCode)           // R003 only
verifyCanFinishSr(String userTyCode)           // R003 only
verifyCanEvaluateSr(String userTyCode)         // R002 only
verifyCanViewSrList(String userTyCode)         // All authenticated
```

**Verification:**

```bash
# Compile check
mvn clean compile
```

### Step 3: Create JWT Interceptor

**File:** `src/main/java/com/example/itsm_api/security/JwtUserTypeCodeInterceptor.java`

**Status:** ✅ COMPLETE

**Functionality:**

- Intercepts all HTTP requests
- Extracts `userTyCode` from JWT claims
- Supports multiple claim formats for flexibility
- Stores in request attributes for controller access

**Verification:**

```bash
# Check interceptor registration
grep -n "registry.addInterceptor" src/main/java/com/example/itsm_api/security/SecurityConfig.java
```

### Step 4: Create AOP Aspect

**File:** `src/main/java/com/example/itsm_api/security/SrAuthorizationAspect.java`

**Status:** ✅ COMPLETE

**Interception Points:**

- `@Before` advice on all SR operation methods
- Extracts userTyCode from request
- Calls appropriate verification method
- Throws `AccessDeniedException` if unauthorized

**Verification:**

```bash
# Check aspect annotations
grep -n "@Aspect" src/main/java/com/example/itsm_api/security/SrAuthorizationAspect.java
grep -n "@Before" src/main/java/com/example/itsm_api/security/SrAuthorizationAspect.java
```

### Step 5: Create Exception Handler

**File:** `src/main/java/com/example/itsm_api/security/SecurityExceptionHandler.java`

**Status:** ✅ COMPLETE

**Handles:**

- `AccessDeniedException` → HTTP 403 Forbidden
- `IllegalArgumentException` → HTTP 400 Bad Request
- `RuntimeException` → HTTP 500 Internal Server Error

**Verification:**

```bash
# Check exception handlers
grep -n "@RestControllerAdvice" src/main/java/com/example/itsm_api/security/SecurityExceptionHandler.java
```

### Step 6: Update application.yml

**File:** `src/main/resources/application.yml`

**Required Changes:**

```yaml
# Add or update these sections
spring:
  aop:
    auto: true # Enable AOP auto-configuration
    proxy-target-class: true # Use CGLIB proxying

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/itsm
          jwk-set-uri: http://localhost:8080/realms/itsm/protocol/openid-connect/certs
```

**Verification:**

```bash
# Check application.yml has AOP settings
grep -A2 "aop:" src/main/resources/application.yml
```

### Step 7: Update SrvcRsponsApiController (THE MAIN CHANGE)

**File:** `src/main/java/com/example/itsm_api/controller/SrvcRsponsApiController.java`

**Changes Required:**

1. **Add Import:**

```java
import com.example.itsm_api.security.SrAuthorizationService;
import javax.servlet.http.HttpServletRequest;
```

2. **Add Field:**

```java
@Autowired
private SrAuthorizationService authorizationService;  // For helper methods if needed
```

3. **Update Each Endpoint Method:**

**Pattern for GET endpoints (list, retrieve):**

```java
@GetMapping
public ResponseEntity<List<SrvcRsponsVO>> list(..., HttpServletRequest request) {
    // Extract user context
    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // Set context on VO
    SrvcRsponsVO queryVO = new SrvcRsponsVO();
    queryVO.setUserTyCode(userTyCode);
    queryVO.setUserId(userId);

    // Service applies role-based filtering
    List<SrvcRsponsVO> results = srvcRsponsService.retrievePagingList(queryVO);
    return ResponseEntity.ok(results);
}
```

**Pattern for POST/PUT endpoints (with special business logic):**

```java
@PostMapping
public ResponseEntity<SrvcRsponsResponseVO> create(
        @RequestHeader(value = "X-User-Id") String xUserId,
        @RequestBody SrvcRsponsVO vo,
        HttpServletRequest request) {

    // Extract from request attributes
    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // Fallback to header if JWT userId unavailable
    if (userId == null || userId.isEmpty()) {
        userId = xUserId;
    }

    // Set user context
    vo.setUserTyCode(userTyCode);
    vo.setUserId(userId);

    // AOP aspect intercepts here and calls:
    // authorizationService.verifyCanCreateSr(userTyCode)
    // If unauthorized, AccessDeniedException is thrown

    SrvcRsponsResponseVO response = srvcRsponsService.create(vo);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

**Pattern for endpoints with critical business logic (Request lockdown):**

```java
@PutMapping("/{id}/request")
public ResponseEntity<SrvcRsponsResponseVO> updateRequest(
        @PathVariable String id,
        @RequestBody SrvcRsponsVO vo,
        HttpServletRequest request) {

    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // Verify role
    if (!userTyCode.equals(SrAuthorizationService.ROLE_CUSTOMER)) {
        throw new AccessDeniedException("Only customers can update requests");
    }

    // Fetch existing SR
    SrvcRsponsVO existing = srvcRsponsService.retrieve(id);

    // CRITICAL: Check lockdown
    if (existing.getRspons1stDt() != null) {
        throw new IllegalStateException(
            "SR already received; request update is not allowed. " +
            "First response date: " + existing.getRspons1stDt());
    }

    // Set context and update
    vo.setUserTyCode(userTyCode);
    vo.setUserId(userId);

    SrvcRsponsResponseVO response = srvcRsponsService.updateRequest(vo);
    return ResponseEntity.ok(response);
}
```

### Step 8: Update SrvcRsponsService

**File:** `src/main/java/com/example/itsm_api/service/SrvcRsponsService.java`

**Changes Required:**

Update each retrieve/list method to accept and pass `userTyCode`:

```java
public List<SrvcRsponsVO> retrieveSrProcList(
        SrvcRsponsVO queryVO,
        String userTyCode,
        String userId) {

    // Set user context for mapper filtering
    queryVO.setUserTyCode(userTyCode);
    queryVO.setUserId(userId);

    // MyBatis applies role-based WHERE clause
    return srvcRsponsMapper.retrieveSrProcList(queryVO);
}
```

**Verification that MyBatis mapper already has filtering:**

```bash
# Search for role-based filtering in mapper
grep -n "userTyCode" src/main/resources/mapper/SrvcRsponsMapper.xml
grep -n "R001\|R002\|R003\|R005" src/main/resources/mapper/SrvcRsponsMapper.xml
```

## Code Changes Required

### Summary Table

| Component                  | File                                                       | Status  | Changes                                                                 |
| -------------------------- | ---------------------------------------------------------- | ------- | ----------------------------------------------------------------------- |
| SecurityConfig             | security/SecurityConfig.java                               | ✅ DONE | Added `@EnableAspectJAutoProxy`, interceptor registration               |
| SrAuthorizationService     | security/SrAuthorizationService.java                       | ✅ DONE | Created - 18 methods                                                    |
| JwtUserTypeCodeInterceptor | security/JwtUserTypeCodeInterceptor.java                   | ✅ DONE | Created - JWT extraction                                                |
| SrAuthorizationAspect      | security/SrAuthorizationAspect.java                        | ✅ DONE | Created - AOP enforcement                                               |
| SecurityExceptionHandler   | security/SecurityExceptionHandler.java                     | ✅ DONE | Created - Exception handling                                            |
| SrvcRsponsApiController    | controller/SrvcRsponsApiController.java                    | ⏳ TODO | Add HttpServletRequest to methods, extract userTyCode/userId, set on VO |
| SrvcRsponsService          | service/SrvcRsponsService.java                             | ⏳ TODO | Pass userTyCode to mapper methods                                       |
| application.yml            | resources/application.yml                                  | ⏳ TODO | Add AOP configuration                                                   |
| Reference Implementation   | controller/reference/SrvcRsponsApiControllerReference.java | ✅ DONE | Template - use as guide                                                 |

### Endpoint-by-Endpoint Changes

**GET /api/v1/sr** (list)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId from request
// ADD: Set on queryVO before calling service
// KEPT: Existing pagination and search logic
```

**GET /api/v1/sr/{id}** (retrieve)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// ADD: Role-based access check if needed (optional - service filtering may suffice)
```

**POST /api/v1/sr** (create - CRITICAL)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId from request.getAttribute()
// ADD: Set on vo before calling srvcRsponsService.create()
// KEPT: Auto-fill logic from UserService
// AOP will intercept and verify: verifyCanCreateSr(userTyCode)
```

**POST /api/v1/sr/manager** (createForManager - CRITICAL)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// ADD: Set on vo
// AOP will intercept and verify: verifyCanCreateSrAsManager(userTyCode)
// ADD: Validation - ensure R001 (done by aspect, but can double-check here)
```

**PUT /api/v1/sr/{id}/request** (updateRequest - CRITICAL)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// ADD: Manual role check: if (!userTyCode.equals(ROLE_CUSTOMER))
// ADD: Lockdown check: if (existing.getRspons1stDt() != null) throw
// ADD: Stage check: if (!status.equals("REQUEST")) throw
// ADD: Set userTyCode, userId on vo
```

**PUT /api/v1/sr/{id}/receive** (updateReceive)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanReceiveSr(userTyCode)
```

**PUT /api/v1/sr/{id}/response-first** (updateResponseFirst)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
```

**PUT /api/v1/sr/{id}/process** (updateProcess)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanProcessSr(userTyCode)
```

**PUT /api/v1/sr/{id}/verify** (updateVerify)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanVerifySr(userTyCode)
```

**PUT /api/v1/sr/{id}/finish** (updateFinish)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanFinishSr(userTyCode)
```

**PUT /api/v1/sr/{id}/evaluation** (updateEvaluation)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanEvaluateSr(userTyCode)
```

**POST /api/v1/sr/{id}/re-request** (reRequest)

```java
// ADD: HttpServletRequest parameter
// ADD: Extract userTyCode, userId
// SET: vo.setUserTyCode(), vo.setUserId()
// AOP will intercept and verify: verifyCanEvaluateSr(userTyCode)
```

## Testing

### Unit Tests for Authorization Service

**File to Create:** `src/test/java/com/example/itsm_api/security/SrAuthorizationServiceTest.java`

```java
@SpringBootTest
public class SrAuthorizationServiceTest {

    @Autowired
    private SrAuthorizationService authorizationService;

    @Test
    void testVerifyCanCreateSr_ManagerAllowed() {
        // Should not throw
        authorizationService.verifyCanCreateSr("R001");
    }

    @Test
    void testVerifyCanCreateSr_CustomerAllowed() {
        // Should not throw
        authorizationService.verifyCanCreateSr("R002");
    }

    @Test
    void testVerifyCanCreateSr_HandlerDenied() {
        assertThrows(AccessDeniedException.class,
            () -> authorizationService.verifyCanCreateSr("R003"));
    }

    @Test
    void testVerifyCanCreateSrAsManager_ManagerAllowed() {
        // Should not throw
        authorizationService.verifyCanCreateSrAsManager("R001");
    }

    @Test
    void testVerifyCanCreateSrAsManager_CustomerDenied() {
        assertThrows(AccessDeniedException.class,
            () -> authorizationService.verifyCanCreateSrAsManager("R002"));
    }
}
```

### Integration Tests with Keycloak

**File to Create:** `src/test/java/com/example/itsm_api/controller/SrAuthorizationIntegrationTest.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SrAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateSr_ManagerToken_Success() throws Exception {
        String managerToken = generateToken("R001", "manager-user");

        mockMvc.perform(post("/api/v1/sr/manager")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dmmndNm\": \"Test\", \"dmmndCntnt\": \"Test content\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateSr_CustomerToken_Success() throws Exception {
        String customerToken = generateToken("R002", "customer-user");

        mockMvc.perform(post("/api/v1/sr")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dmmndNm\": \"Test\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateSrManager_CustomerToken_Forbidden() throws Exception {
        String customerToken = generateToken("R002", "customer-user");

        mockMvc.perform(post("/api/v1/sr/manager")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dmmndNm\": \"Test\"}"))
            .andExpect(status().isForbidden());
    }

    private String generateToken(String userTyCode, String userId) {
        // TODO: Generate JWT token with userTyCode claim
        return "test.token.here";
    }
}
```

### Manual Testing with cURL

**Test 1: Manager Endpoint Access**

```bash
# Get manager token from Keycloak
MANAGER_TOKEN=$(curl -X POST \
  http://localhost:8080/realms/itsm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=itsm-api&client_secret=<secret>&grant_type=client_credentials&username=manager&password=<pwd>" \
  | jq -r '.access_token')

# Create SR as manager
curl -X POST http://localhost:8080/api/v1/sr/manager \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndNm": "Upgrade Service",
    "dmmndCntnt": "Upgrade database"
  }'

# Expected: 201 Created
```

**Test 2: Authorization Denial**

```bash
# Get customer token
CUSTOMER_TOKEN=$(curl -X POST \
  http://localhost:8080/realms/itsm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=itsm-api&client_secret=<secret>&grant_type=client_credentials&username=customer&password=<pwd>" \
  | jq -r '.access_token')

# Try to access manager endpoint
curl -X POST http://localhost:8080/api/v1/sr/manager \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dmmndNm": "Upgrade Service"
  }'

# Expected: 403 Forbidden
# Response: {"status": 403, "error": "Access Denied", "message": "Only managers can use /manager endpoint"}
```

**Test 3: Request Lockdown**

```bash
# Get SR ID (from previous create)
SR_ID="SR-2401-001"

# As handler, receive SR (sets RSPONS_1ST_DT)
HANDLER_TOKEN=$(... get handler token ...)
curl -X PUT http://localhost:8080/api/v1/sr/$SR_ID/receive \
  -H "Authorization: Bearer $HANDLER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"resoncPrcssDt": "2024-01-15"}'

# As customer, try to update request (should fail)
CUSTOMER_TOKEN=$(... get customer token ...)
curl -X PUT http://localhost:8080/api/v1/sr/$SR_ID/request \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"dmmndCntnt": "Updated content"}'

# Expected: 409 Conflict or 400 Bad Request
# Response: {"error": "Conflict", "message": "SR already received; request update is not allowed"}
```

## Troubleshooting

### Issue 1: AOP Not Intercepting

**Symptom:** Authorization checks not being called, no 403 errors

**Possible Causes:**

1. `@EnableAspectJAutoProxy` not added to SecurityConfig
2. `spring.aop.auto` not set to true in application.yml
3. Method not public (aspects only intercept public methods)
4. Wrong method signature

**Solution:**

```bash
# Check SecurityConfig
grep "@EnableAspectJAutoProxy" src/main/java/com/example/itsm_api/security/SecurityConfig.java

# Check application.yml
grep -A1 "spring.aop" src/main/resources/application.yml

# Ensure methods are public in SrvcRsponsApiController
grep "public ResponseEntity" src/main/java/com/example/itsm_api/controller/SrvcRsponsApiController.java
```

### Issue 2: userTyCode Not Found

**Symptom:** `"User type code not found in request"` error

**Possible Causes:**

1. JWT token doesn't contain userTyCode claim
2. JwtUserTypeCodeInterceptor not registered
3. Keycloak not adding userTyCode attribute to token

**Solution:**

```bash
# Check interceptor registration
grep "registry.addInterceptor" src/main/java/com/example/itsm_api/security/SecurityConfig.java

# Check Keycloak user configuration
# Ensure user has userTyCode mapper in Keycloak realm
# Client Scopes → Mappers → Check for userTyCode mapper

# Test token by decoding JWT
curl -X POST http://localhost:8080/realms/itsm/protocol/openid-connect/token \
  ... get token ... | jq '.access_token' | cut -d. -f2 | base64 -d | jq .
# Check for "userTyCode" claim in output
```

### Issue 3: Data Filtering Not Working

**Symptom:** Seeing SRs from other roles

**Possible Causes:**

1. userTyCode not set on VO before calling service
2. MyBatis mapper not receiving userTyCode
3. Role-based WHERE clause syntax error

**Solution:**

```bash
# Check that controller sets userTyCode on VO
grep -A5 "vo.setUserTyCode" src/main/java/com/example/itsm_api/controller/SrvcRsponsApiController.java

# Check service passes userTyCode to mapper
grep -B5 "srvcRsponsMapper" src/main/java/com/example/itsm_api/service/SrvcRsponsService.java | grep -A2 "userTyCode"

# Test with logging in mapper
# Add console logging to verify WHERE clause being applied
```

### Issue 4: Lockdown Not Preventing Updates

**Symptom:** Request can be edited after first response

**Possible Causes:**

1. Lockdown check not implemented in updateRequest()
2. RSPONS_1ST_DT not being set when SR received
3. Condition logic reversed

**Solution:**

```bash
# Check updateRequest() has lockdown check
grep -A10 "public ResponseEntity.*updateRequest" src/main/java/com/example/itsm_api/controller/SrvcRsponsApiController.java | grep -i "rspons1stdt\|lockdown"

# Check database - verify RSPONS_1ST_DT is set on receive
SELECT SR_NUM, RSPONS_1ST_DT FROM TB_SRVC_RSPONS WHERE SR_NUM = 'SR-2401-001';

# Check condition is correctly comparing != null
# Should be: if (existing.getRspons1stDt() != null) throw ...
```

## Deployment Checklist

### Pre-Deployment

- [ ] All 4 new security classes created and compiled
- [ ] SecurityConfig updated with AOP and interceptor
- [ ] SrvcRsponsApiController modified with HttpServletRequest extraction
- [ ] SrvcRsponsService updated to pass userTyCode to mapper
- [ ] application.yml has AOP configuration
- [ ] Unit tests pass: `mvn test`
- [ ] Integration tests pass: `mvn verify`
- [ ] No compilation errors: `mvn clean compile`

### Keycloak Configuration

- [ ] Realm `itsm` exists
- [ ] Client `itsm-api` configured
- [ ] Users created with roles (R001-R005)
- [ ] Each user has `userTyCode` attribute
- [ ] Token mapper configured to include `userTyCode` claim
- [ ] JWT signing certificate valid

### Database Configuration

- [ ] TB_SRVC_RSPONS table exists with all columns
- [ ] TB_SYS_CHARGER table exists (for handler service mapping)
- [ ] TB_CMMN_CODE table exists (for lookups)
- [ ] Application can connect to database
- [ ] All indexes created

### Testing in Dev/Staging

- [ ] Run authorization tests with different roles
- [ ] Verify 403 errors return correct format
- [ ] Test request lockdown with real SR
- [ ] Verify data filtering (R001 sees all, R003 sees assigned, R005 sees own)
- [ ] Monitor logs for AOP interception messages
- [ ] Test token expiration handling

### Production Deployment

- [ ] Code review completed
- [ ] Performance testing passed (AOP overhead acceptable)
- [ ] Security review passed
- [ ] Load testing completed
- [ ] Monitoring/logging configured
- [ ] Rollback plan prepared
- [ ] User documentation updated
- [ ] Training provided for operations team

## Next Steps

1. **Implement Controller Changes** - Update SrvcRsponsApiController using reference implementation
2. **Update Service Layer** - Modify service methods to pass userTyCode to mapper
3. **Configure application.yml** - Add AOP settings
4. **Write Tests** - Create unit and integration tests
5. **Manual Testing** - Verify with different role tokens
6. **Performance Testing** - Ensure AOP overhead is acceptable
7. **Deploy to Staging** - Test in staging environment
8. **Production Deployment** - Deploy to production

## Additional Resources

- [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) - Architecture and authorization rules
- [AUTHORIZATION_GUIDE.md](AUTHORIZATION_GUIDE.md) - Existing authorization patterns
- [README.md](README.md) - System overview and documentation
- `SrvcRsponsApiControllerReference.java` - Template implementation
- Spring Documentation: https://docs.spring.io/spring-framework/reference/core-aop.html
- AspectJ Documentation: https://www.eclipse.org/aspectj/
