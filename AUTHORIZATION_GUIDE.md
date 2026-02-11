# Role-Based Authorization Implementation Guide

## Overview

This implementation provides a comprehensive role-based authorization system for the ITSM API that integrates with Keycloak's user attributes including `userTyCode` (User Type Code) and `userSttusCode` (User Status Code).

## Architecture Components

### 1. **CustomUserPrincipal**

(`security/CustomUserPrincipal.java`)

- Extracts user attributes from Keycloak JWT token
- Maps Keycloak user attributes to secure principals in the application
- Provides access to:
  - `userTyCode`: User type code (e.g., R005 - Regular Employee, R001 - Manager)
  - `userSttusCode`: User status code (e.g., U001 - Inactive, U002 - Active)
  - `deptCd`: Department code
  - `deptNm`: Department name
  - `position`: Position/title
  - `classNm`: Class/classification name
  - Realm roles from Keycloak

### 2. **AuthorizationService**

(`security/AuthorizationService.java`)

- Central service for authorization checks
- Methods for checking user type codes, status codes, roles, and departments
- Supports complex authorization logic combining multiple conditions

### 3. **Custom Annotations**

- `@RequireUserTyCode`: Restrict endpoint access to specific user type codes
- `@RequireUserSttusCode`: Restrict endpoint access to specific user status codes

### 4. **SecurityConfig**

(`security/SecurityConfig.java`)

- Spring Security OAuth2 configuration
- JWT token decoder for Keycloak issuer
- Custom JwtAuthenticationConverter that creates CustomUserPrincipal

### 5. **AuthorizationAspect**

(`security/AuthorizationAspect.java`)

- AOP implementation for custom annotation handling
- Intercepts calls to methods with `@RequireUserTyCode` and `@RequireUserSttusCode`
- Throws `AccessDeniedException` if authorization fails

### 6. **Example Controllers**

- `ProtectedController`: Demonstrates various authorization patterns
- `PublicController`: Shows unrestricted endpoints

## Usage Examples

### Basic User Info

```java
@GetMapping("/user-info")
public ResponseEntity<?> getUserInfo() {
    CustomUserPrincipal user = authorizationService.getCurrentUser();

    // Access user attributes
    String username = user.getUsername();
    String email = user.getEmail();
    List<String> userTyCode = user.getUserTyCode();      // [R005]
    List<String> userSttusCode = user.getUserSttusCode(); // [U002]

    return ResponseEntity.ok(user);
}
```

### Check Single User Type Code

```java
@GetMapping("/employee-area")
@RequireUserTyCode("R005")
public ResponseEntity<?> getEmployeeArea() {
    return ResponseEntity.ok("Employee area accessed");
}
```

### Check Multiple User Type Codes (Any)

```java
@GetMapping("/hr-or-manager-area")
public ResponseEntity<?> getHROrManagerArea() {
    if (!authorizationService.hasUserTyCode("R001", "R005")) {
        throw new AccessDeniedException("Access restricted");
    }
    return ResponseEntity.ok("Area accessed");
}
```

### Check User Status Code

```java
@GetMapping("/active-user-area")
public ResponseEntity<?> getActiveUserArea() {
    if (!authorizationService.hasUserSttusCode("U002")) {
        throw new AccessDeniedException("Only active users");
    }
    return ResponseEntity.ok("Active user area");
}
```

### Check with @PreAuthorize (Keycloak Roles)

```java
@GetMapping("/viewer-access")
@PreAuthorize("hasRole('VIEWER')")
public ResponseEntity<?> getViewerAccess() {
    return ResponseEntity.ok("Access granted");
}
```

### Complex Authorization

```java
@GetMapping("/advanced-access")
public ResponseEntity<?> getAdvancedAccess() {
    boolean canAccess = authorizationService.canAccess(
        "R005",                              // Required user type code
        Arrays.asList("U001", "U002"),       // Allowed status codes
        "VIEWER"                             // Required role
    );

    if (!canAccess) {
        throw new AccessDeniedException("Insufficient permissions");
    }
    return ResponseEntity.ok("Access granted");
}
```

## AuthorizationService Methods

### User Information

```java
CustomUserPrincipal getCurrentUser()           // Get current authenticated user
String getPrimaryUserTyCode()                  // Get first user type code
String getPrimaryDeptCd()                      // Get first department code
```

### User Type Code Checks

```java
boolean hasUserTyCode(String... codes)         // Check if user has ANY of codes
boolean hasAllUserTyCodes(String... codes)     // Check if user has ALL codes
```

### User Status Code Checks

```java
boolean hasUserSttusCode(String... codes)      // Check if user has ANY of codes
boolean hasAllUserSttusCodeS(String... codes)  // Check if user has ALL codes
```

### Department Checks

```java
boolean hasDeptCd(String... codes)             // Check if user belongs to departments
```

### Role Checks

```java
boolean hasRole(String role)                   // Check single role
boolean hasAnyRole(String... roles)            // Check if has ANY role
boolean hasAllRoles(String... roles)           // Check if has ALL roles
boolean isAdmin()                              // Check if ADMIN
boolean isViewer()                             // Check if VIEWER
```

### Complex Checks

```java
boolean canAccess(String userTyCode,
                  List<String> userSttusCode,
                  String role)                 // Multi-condition check
```

## Keycloak User Attributes Mapping

The JWT token from Keycloak should contain an "attributes" claim with user-specific data:

```json
{
  "preferred_username": "123456",
  "email": "123456@abc.com",
  "given_name": "일이삼",
  "attributes": {
    "userTyCode": ["R005"],
    "userSttusCode": ["U002"],
    "deptCd": ["D1"],
    "deptNm": ["인사부"],
    "position": ["인사부"],
    "classNm": ["사원"]
  },
  "realm_access": {
    "roles": ["VIEWER", "USER"]
  }
}
```

## Configuration

### application.yml

```yaml
server:
  port: 8088

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/itsm
```

### Keycloak Realm Configuration

Ensure your Keycloak realm's protocol mappers include:

1. **Realm Roles** mapper to include roles in JWT
2. **User Attributes** mapper to include custom attributes

## Security Best Practices

1. **Always validate** user type and status codes from Keycloak
2. **Use annotations** for clear authorization intent
3. **Combine multiple checks** for sensitive operations
4. **Log authorization failures** for audit trails
5. **Use @PreAuthorize** for role-based access (Keycloak roles)
6. **Use custom annotations** for business-rule-based access (user type/status)

## Testing

For testing authorization without Keycloak:

```java
@SpringBootTest
@WithMockUser(authorities = "ROLE_VIEWER")
public class ControllerTest {

    @Test
    public void testProtectedEndpoint() {
        // Test will have mock user with VIEWER role
    }
}
```

## Endpoint Examples

### Public Endpoints

```
GET /api/public/health
```

### Protected Endpoints

```
GET /api/protected/user-info              (Authenticated users only)
GET /api/protected/viewer-access          (VIEWER role)
GET /api/protected/employee-area          (R005 user type)
GET /api/protected/active-user-area       (U002 status code)
GET /api/protected/admin-only             (ADMIN role)
GET /api/protected/my-department          (All authenticated)
```

## Integration with Frontend (Next.js)

When calling from the frontend, include the JWT token:

```javascript
const response = await fetch("/api/protected/user-info", {
  headers: {
    Authorization: `Bearer ${keycloakToken}`,
  },
});
```

## Troubleshooting

### User attributes not found

- Verify Keycloak has user attributes mapper configured
- Check JWT token contains "attributes" claim
- Validate attribute names match configuration

### AccessDeniedException on valid users

- Check user type and status codes in Keycloak
- Verify authorization service method parameters
- Review security logs for details

### Roles not recognized

- Ensure roles are in "realm_access.roles" in JWT
- Check Spring Security ROLE\_ prefix handling
- Validate @PreAuthorize syntax
