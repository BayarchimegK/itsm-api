package com.example.itsm_api.controller;

import com.example.itsm_api.security.AuthorizationService;
import com.example.itsm_api.security.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller demonstrating role-based authorization
 */
@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Public endpoint - accessible to all authenticated users
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo() {
        CustomUserPrincipal user = authorizationService.getCurrentUser();
        
        return ResponseEntity.ok(new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getUserTyCode(),
                user.getUserSttusCode(),
                user.getDeptNm(),
                user.getPosition()
        ));
    }

    /**
     * Restricted to users with VIEWER role
     */
    @GetMapping("/viewer")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<?> getViewerAccess() {
        return ResponseEntity.ok("Access granted to VIEWER role");
    }

    /**
     * Restricted to users with ADMIN role
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminAccess() {
        return ResponseEntity.ok("Access granted to ADMIN role");
    }

    /**
     * Restricted to users with CONSULTANT role
     */
    @GetMapping("/consultant")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<?> getConsultantAccess() {
        return ResponseEntity.ok("Access granted to CONSULTANT role");
    }

    /**
     * Restricted to users with CUSTOMER role
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCustomerAccess() {
        return ResponseEntity.ok("Access granted to CUSTOMER role");
    }

    /**
     * Restricted to users with OPERATOR role
     */
    @GetMapping("/operator")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<?> getOperatorAccess() {
        return ResponseEntity.ok("Access granted to OPERATOR role");
    }

    /**
     * Restricted to users with TEMP_USER role
     */
    @GetMapping("/temp-user")
    @PreAuthorize("hasRole('TEMP_USER')")
    public ResponseEntity<?> getTempUserAccess() {
        return ResponseEntity.ok("Access granted to TEMP_USER role");
    }

    /**
     * Debug endpoint - shows all information from the access token
     */
    @GetMapping("/debug-token")
    public ResponseEntity<?> debugToken() {
        var auth = authorizationService.getCurrentAuthentication();
        
        if (!(auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt)) {
            return ResponseEntity.ok(java.util.Map.of("error", "Not a JWT token"));
        }
        
        org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) auth.getPrincipal();
        
        // Build comprehensive token info
        var tokenInfo = new java.util.HashMap<>();
        tokenInfo.put("subject", jwt.getSubject());
        tokenInfo.put("issuer", jwt.getIssuer());
        tokenInfo.put("issuedAt", jwt.getIssuedAt());
        tokenInfo.put("expiresAt", jwt.getExpiresAt());
        tokenInfo.put("tokenValue", jwt.getTokenValue()); // Full JWT token
        
        // All claims
        var claims = new java.util.HashMap<>();
        jwt.getClaims().forEach((key, value) -> claims.put(key, value));
        tokenInfo.put("allClaims", claims);
        
        // Realm roles specifically
        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            tokenInfo.put("realm_access", realmAccess);
        }
        
        // Client roles
        var resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            tokenInfo.put("resource_access", resourceAccess);
        }
        
        // Spring Security authorities
        tokenInfo.put("springAuthorities", auth.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .collect(java.util.stream.Collectors.toList()));
        
        // User attributes
        CustomUserPrincipal user = authorizationService.getCurrentUser();
        tokenInfo.put("username", user.getUsername());
        tokenInfo.put("email", user.getEmail());
        tokenInfo.put("firstName", user.getFirstName());
        tokenInfo.put("userTyCode", user.getUserTyCode());
        tokenInfo.put("userSttusCode", user.getUserSttusCode());
        tokenInfo.put("deptNm", user.getDeptNm());
        tokenInfo.put("position", user.getPosition());
        
        return ResponseEntity.ok(tokenInfo);
    }

    /**
     * DTO for user info response
     */
    public record UserInfoResponse(
            String username,
            String email,
            String firstName,
            java.util.List<String> userTyCode,
            java.util.List<String> userSttusCode,
            java.util.List<String> deptNm,
            java.util.List<String> position
    ) {}

}
