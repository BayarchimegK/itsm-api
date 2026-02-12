# Quick Reference - Authorization Framework Integration

## 🎯 TL;DR (One Page Summary)

The role-based authorization framework is **95% complete**. You only need to:

1. **Update SrvcRsponsApiController** - Add `HttpServletRequest` parameter to methods
2. **Extract userTyCode/userId** - Get from `request.getAttribute()`
3. **Set on VO** - `vo.setUserTyCode(userTyCode)` and `vo.setUserId(userId)`

**That's it!** AOP automatically intercepts and authenticates.

---

## 🔑 Key Components Ready to Use

### SrAuthorizationService

```java
@Autowired
private SrAuthorizationService authorizationService;  // Already available

// Methods you can call:
authorizationService.verifyCanCreateSr(userTyCode);
authorizationService.verifyCanReceiveSr(userTyCode);
authorizationService.verifyCanProcessSr(userTyCode);
// ... etc
```

### JwtUserTypeCodeInterceptor

- **Status**: Running automatically
- **Function**: Extracts `userTyCode` from JWT
- **Available as**: `request.getAttribute("userTyCode")`

### SrAuthorizationAspect

- **Status**: Configured and running
- **Function**: Intercepts controller methods and verifies authorization
- **Behavior**: Throws `AccessDeniedException` if unauthorized (caught → HTTP 403)

---

## 📝 3-Minute Controller Update Pattern

### Before (Current)

```java
@PostMapping
public ResponseEntity<SrvcRsponsResponseVO> create(
        @RequestBody SrvcRsponsVO vo) {
    return srvcRsponsService.create(vo);
}
```

### After (Need to Add)

```java
@PostMapping
public ResponseEntity<SrvcRsponsResponseVO> create(
        @RequestBody SrvcRsponsVO vo,
        HttpServletRequest request) {  // ← ADD THIS

    // Extract user info from request attributes (set by interceptor)
    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // Set on VO
    vo.setUserTyCode(userTyCode);
    vo.setUserId(userId);

    // AOP will intercept and verify automatically
    return srvcRsponsService.create(vo);
}
```

### What Happens Next (Automatic)

```
SrAuthorizationAspect intercepts ↓
  authorizationService.verifyCanCreateSr(userTyCode) ↓
    If authorized: Continue to service layer ✓
    If denied: Throw AccessDeniedException ✗
      ↓
  SecurityExceptionHandler catches ✗
    ↓
  Return HTTP 403 Forbidden with error message
```

---

## 📋 Endpoint Checklist

Copy this pattern to all 21 endpoints in `SrvcRsponsApiController`:

- [ ] `create()` - POST /api/v1/sr
- [ ] `createForManager()` - POST /api/v1/sr/manager
- [ ] `retrieve()` - GET /api/v1/sr/{id}
- [ ] `list()` - GET /api/v1/sr
- [ ] `updateRequest()` - PUT /api/v1/sr/{id}/request
- [ ] `updateReceive()` - PUT /api/v1/sr/{id}/receive
- [ ] `updateResponseFirst()` - PUT /api/v1/sr/{id}/response-first
- [ ] `updateProcess()` - PUT /api/v1/sr/{id}/process
- [ ] `updateVerify()` - PUT /api/v1/sr/{id}/verify
- [ ] `updateFinish()` - PUT /api/v1/sr/{id}/finish
- [ ] `updateEvaluation()` - PUT /api/v1/sr/{id}/evaluation
- [ ] `reRequest()` - POST /api/v1/sr/{id}/re-request
- [ ] ... and 9 more

---

## Special Cases

### Case 1: Request Lockdown (updateRequest endpoint)

```java
@PutMapping("/{id}/request")
public ResponseEntity<SrvcRsponsResponseVO> updateRequest(
        @PathVariable String id,
        @RequestBody SrvcRsponsVO vo,
        HttpServletRequest request) {

    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    // CRITICAL BUSINESS RULE: Check lockdown
    SrvcRsponsVO existing = srvcRsponsService.retrieve(id);
    if (existing.getRspons1stDt() != null) {
        throw new IllegalStateException(
            "SR already received; request update is not allowed");
    }

    vo.setUserTyCode(userTyCode);
    vo.setUserId(userId);

    return ResponseEntity.ok(srvcRsponsService.updateRequest(vo));
}
```

### Case 2: List Endpoints

```java
@GetMapping
public ResponseEntity<List<SrvcRsponsVO>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        HttpServletRequest request) {  // ← ADD

    String userTyCode = (String) request.getAttribute("userTyCode");
    String userId = (String) request.getAttribute("userId");

    SrvcRsponsVO queryVO = new SrvcRsponsVO();
    queryVO.setUserTyCode(userTyCode);
    queryVO.setUserId(userId);
    queryVO.setPageNum(pageNum);
    queryVO.setPageSize(pageSize);

    // Service will apply role-based filtering via MyBatis
    List<SrvcRsponsVO> results = srvcRsponsService.retrievePagingList(queryVO);
    return ResponseEntity.ok(results);
}
```

---

## 🔐 Role Codes Reference

```
R000 = ROLE_TEMP      (Temporary user - no SR ops)
R001 = ROLE_MANAGER   (Manager - full access)
R002 = ROLE_CUSTOMER  (Customer - create, evaluate, update own)
R003 = ROLE_CHARGER   (Handler - process workflow)
R004 = ROLE_CONSULTANT (Consultant - read-only)
R005 = ROLE_CUSTOM    (Custom specialist - own SRs only)
```

---

## 🚀 Quick Test

### 1. Get Manager Token

```bash
MANAGER_TOKEN=$(curl -X POST http://localhost:8080/realms/itsm/protocol/openid-connect/token \
  -d "client_id=itsm-api&username=manager&password=xxx&grant_type=password" \
  | jq -r '.access_token')
```

### 2. Test Protected Endpoint

```bash
curl -X POST http://localhost:8080/api/v1/sr/manager \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"dmmndNm": "Test"}'
```

### 3. Expected Results

- ✓ 201 Created (authorized)
- ✗ 403 Forbidden (unauthorized role)

---

## ✅ Verification Checklist

After updating controller:

- [ ] Code compiles: `mvn clean compile`
- [ ] No errors: `./mvnw clean build`
- [ ] Test one endpoint locally
- [ ] Verify 201/200 with correct role
- [ ] Verify 403 with wrong role
- [ ] Check logs for AOP interception
- [ ] Verify request lockdown works
- [ ] Verify data filtering works

---

## 📚 Full Documentation

| Document                              | Purpose                        |
| ------------------------------------- | ------------------------------ |
| IMPLEMENTATION_GUIDE.md               | Complete architecture & rules  |
| INTEGRATION_STEPS.md                  | Step-by-step integration guide |
| SrvcRsponsApiControllerReference.java | Full controller template       |
| AUTHORIZATION_FRAMEWORK_SUMMARY.md    | Complete project summary       |

---

## 🆘 Troubleshooting

| Issue                                | Solution                                           |
| ------------------------------------ | -------------------------------------------------- |
| NullPointerException on `userTyCode` | Add import `javax.servlet.http.HttpServletRequest` |
| AOP not intercepting                 | Check SecurityConfig has `@EnableAspectJAutoProxy` |
| 403 on allowed operations            | Check user has correct role in Keycloak            |
| userTyCode not found                 | Ensure Keycloak adds userTyCode claim to JWT       |

---

## 🎬 Getting Started Now

1. Open `SrvcRsponsApiController.java`
2. Copy pattern above
3. Add `HttpServletRequest request` to method signature
4. Add 3 lines to extract userTyCode/userId
5. Add 2 lines to set on VO
6. Test with token

**Estimated time: 1-2 hours for all 21 endpoints**

---

## Key Files You'll Edit

```
src/
├── main/
│   ├── java/
│   │   └── com/example/itsm_api/
│   │       ├── controller/
│   │       │   └── SrvcRsponsApiController.java  ← EDIT THIS
│   │       ├── service/
│   │       │   └── SrvcRsponsService.java        (no major changes needed)
│   │       └── security/
│   │           ├── SecurityConfig.java           ✅ ALREADY UPDATED
│   │           ├── SrAuthorizationService.java   ✅ CREATED
│   │           ├── JwtUserTypeCodeInterceptor.java ✅ CREATED
│   │           ├── SrAuthorizationAspect.java    ✅ CREATED
│   │           └── SecurityExceptionHandler.java ✅ CREATED
│   └── resources/
│       └── application.yml                        ← ADD AOP CONFIG
└── test/
    └── java/
        └── ... (create authorization tests)
```

---

## 💡 Pro Tips

1. **Reuse the Reference Implementation** - `SrvcRsponsApiControllerReference.java` shows every endpoint pattern
2. **Copy-paste from template** - Don't write from scratch
3. **Test incrementally** - Update 3 endpoints, test, then continue
4. **Watch the logs** - You'll see "AOP interception" in console
5. **Use Keycloak admin panel** - Verify users have correct roles

---

## 📞 Quick Command Reference

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Build
mvn clean package

# Run app
java -jar target/itsm-api-0.0.1-SNAPSHOT.jar

# Check logs for AOP
grep -i "aop\|aspect" logs/*.log
```

---

## 🎯 Success Criteria

After integration is complete:

✓ All endpoints protected by role  
✓ HTTP 403 returned for unauthorized access  
✓ userTyCode extracted from JWT automatically  
✓ Data filtered by role at database level  
✓ Request lockdown prevents edits after first response  
✓ All tests pass  
✓ Keycloak integration working  
✓ No null pointer exceptions  
✓ Performance acceptable (< 1ms AOP overhead)  
✓ Ready for production deployment

---

**You've got this! 🚀**
