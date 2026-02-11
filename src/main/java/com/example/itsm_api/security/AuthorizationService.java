package com.example.itsm_api.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Service for role-based authorization checks using user type and status codes
 */
@Service
public class AuthorizationService {

    /**
     * Get the current authenticated user principal
     */
    public CustomUserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
            Jwt jwt = jwtAuth.getToken();
            return new CustomUserPrincipal(jwt);
        }
        throw new IllegalStateException("No authenticated JWT user found");
    }

    /**
     * Get the current authentication object (for debugging)
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Check if user has specific user type code
     */
    public boolean hasUserTyCode(String... codes) {
        CustomUserPrincipal user = getCurrentUser();
        return user.getUserTyCode().stream()
                .anyMatch(code -> Arrays.asList(codes).contains(code));
    }

    /**
     * Check if user has specific user status code
     */
    public boolean hasUserSttusCode(String... codes) {
        CustomUserPrincipal user = getCurrentUser();
        return user.getUserSttusCode().stream()
                .anyMatch(code -> Arrays.asList(codes).contains(code));
    }

    /**
     * Check if user has all required user type codes
     */
    public boolean hasAllUserTyCodes(String... codes) {
        CustomUserPrincipal user = getCurrentUser();
        return Arrays.stream(codes)
                .allMatch(code -> user.getUserTyCode().contains(code));
    }

    /**
     * Check if user has all required user status codes
     */
    public boolean hasAllUserSttusCodeS(String... codes) {
        CustomUserPrincipal user = getCurrentUser();
        return Arrays.stream(codes)
                .allMatch(code -> user.getUserSttusCode().contains(code));
    }

    /**
     * Check if user has specific department code
     */
    public boolean hasDeptCd(String... codes) {
        CustomUserPrincipal user = getCurrentUser();
        return user.getDeptCd().stream()
                .anyMatch(code -> Arrays.asList(codes).contains(code));
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        return Arrays.stream(roles)
                .anyMatch(this::hasRole);
    }

    /**
     * Check if user has all specified roles
     */
    public boolean hasAllRoles(String... roles) {
        return Arrays.stream(roles)
                .allMatch(this::hasRole);
    }

    /**
     * Complex authorization check combining multiple conditions
     */
    public boolean canAccess(String requiredUserTyCode, List<String> allowedUserSttusCode, String requiredRole) {
        CustomUserPrincipal user = getCurrentUser();
        
        boolean hasTyCode = user.getUserTyCode().contains(requiredUserTyCode);
        boolean hasStatus = user.getUserSttusCode().stream()
                .anyMatch(allowedUserSttusCode::contains);
        boolean hasRequiredRole = hasRole(requiredRole);
        
        return hasTyCode && hasStatus && hasRequiredRole;
    }

    /**
     * Get user's user type code (first one if multiple)
     */
    public String getPrimaryUserTyCode() {
        List<String> codes = getCurrentUser().getUserTyCode();
        return codes.isEmpty() ? null : codes.get(0);
    }

    /**
     * Get user's department code (first one if multiple)
     */
    public String getPrimaryDeptCd() {
        List<String> codes = getCurrentUser().getDeptCd();
        return codes.isEmpty() ? null : codes.get(0);
    }

    /**
     * Check if user is admin (can be customized based on your roles)
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Check if user is viewer
     */
    public boolean isViewer() {
        return hasRole("viewer");
    }
}
