package com.example.itsm_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Interceptor for extracting and injecting user type code from JWT token
 * into the request context for authorization checks
 */
@Component
public class JwtUserTypeCodeInterceptor implements HandlerInterceptor {

    /**
     * Extract user type code claim from JWT token
     * Claim name: "userTyCode" or fallback to "user_type_code"
     * 
     * JWT token structure (example from Keycloak):
     * {
     *   "sub": "user-id",
     *   "name": "John Manager",
     *   "email": "john@example.com",
     *   "userTyCode": "R001",
     *   "department": "IT Management",
     *   "iam:exp": 1234567890
     * }
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        try {
            // Get authentication from Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Try to extract userTyCode from JWT claims
                String userTyCode = extractUserTypeCode(jwt);
                
                if (userTyCode != null) {
                    // Store in request attribute for later access
                    request.setAttribute("userTyCode", userTyCode);
                    
                    // Also set as header for easier access
                    request.setAttribute("X-User-Type-Code", userTyCode);
                }
                
                // Extract and store user ID
                String userId = jwt.getClaimAsString("sub");
                if (userId != null) {
                    request.setAttribute("userId", userId);
                }
            }
        } catch (Exception e) {
            // Log but don't fail - let the authorization aspect handle the error
            System.err.println("Failed to extract user type code from JWT: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * Extract userTyCode from JWT claims
     * Tries multiple claim names for compatibility with different Keycloak configurations
     * Claim names (in priority order):
     * 1. "userTyCode" - preferred
     * 2. "user_type_code" - underscore format
     * 3. "custom:userTyCode" - custom namespace (AWS Cognito style)
     * 4. "resource_access" -> "roles" - fallback to mapped roles
     */
    private String extractUserTypeCode(Jwt jwt) {
        // Primary claim name
        Object userTyCodeClaim = jwt.getClaim("userTyCode");
        if (userTyCodeClaim != null && userTyCodeClaim instanceof String) {
            return (String) userTyCodeClaim;
        }

        // Alternate claim name (underscore)
        userTyCodeClaim = jwt.getClaim("user_type_code");
        if (userTyCodeClaim != null && userTyCodeClaim instanceof String) {
            return (String) userTyCodeClaim;
        }

        // Custom namespace claim
        userTyCodeClaim = jwt.getClaim("custom:userTyCode");
        if (userTyCodeClaim != null && userTyCodeClaim instanceof String) {
            return (String) userTyCodeClaim;
        }

        // Fallback: extract from roles if userTyCode is encoded as role
        Object roles = jwt.getClaim("roles");
        if (roles instanceof List) {
            List<String> roleList = (List<String>) roles;
            for (String role : roleList) {
                // Check if role starts with user type code (R000-R005)
                if (role.matches("^R00[0-5].*")) {
                    return role.substring(0, 4); // Extract R00X
                }
            }
        }

        // Enhanced fallback: check resource_access structure (Keycloak client scope)
        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof Map) {
            Map<String, Object> resources = (Map<String, Object>) resourceAccess;
            for (Map.Entry<String, Object> entry : resources.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> clientRoles = (Map<String, Object>) entry.getValue();
                    Object clientRolesList = clientRoles.get("roles");
                    
                    if (clientRolesList instanceof List) {
                        List<String> roles_list = (List<String>) clientRolesList;
                        for (String role : roles_list) {
                            if (role.matches("^R00[0-5].*")) {
                                return role.substring(0, 4);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
