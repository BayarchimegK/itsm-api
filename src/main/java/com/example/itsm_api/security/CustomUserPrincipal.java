package com.example.itsm_api.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom UserPrincipal that extracts user attributes from Keycloak JWT token
 */
public class CustomUserPrincipal implements UserDetails {
    private static final Logger log = LoggerFactory.getLogger(CustomUserPrincipal.class);

    private final String username;
    private final String email;
    private final String firstName;
    private final List<String> userTyCode;      // User Type Code (e.g., R005)
    private final List<String> userSttusCode;   // User Status Code (e.g., U002)
    private final List<String> deptCd;          // Department Code
    private final List<String> deptNm;          // Department Name
    private final List<String> position;        // Position
    private final List<String> classNm;         // Class Name
    private final Collection<? extends GrantedAuthority> authorities;
    private final Jwt jwt;

    public CustomUserPrincipal(Jwt jwt) {
        this.jwt = jwt;
        this.username = jwt.getClaimAsString("preferred_username");
        this.email = jwt.getClaimAsString("email");
        this.firstName = jwt.getClaimAsString("given_name");
        
        // Extract custom attributes - try direct claims first, then check "attributes" map
        this.userTyCode = extractClaimAsList(jwt, "userTyCode");
        this.userSttusCode = extractClaimAsList(jwt, "userSttusCode");
        this.deptCd = extractClaimAsList(jwt, "deptCd");
        this.deptNm = extractClaimAsList(jwt, "deptNm");
        this.position = extractClaimAsList(jwt, "position");
        this.classNm = extractClaimAsList(jwt, "classNm");

        // Log extracted values for debugging
        log.debug("JWT User Principal created - username: {}, userTyCode: {}, userSttusCode: {}", 
                  username, userTyCode, userSttusCode);
        
        // Log warning if critical attributes are missing
        if (userTyCode.isEmpty()) {
            log.warn("User {} has no userTyCode in JWT. Available claims: {}", 
                     username, jwt.getClaims().keySet());
        }

        // Build authorities: include realm roles and mapped userTyCode roles
        var authoritiesList = new java.util.ArrayList<GrantedAuthority>();

        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            var roles = (Collection<String>) realmAccess.get("roles");
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .forEach(authoritiesList::add);
        }

        // Map userTyCode values (R001,R002,...) into ROLE_* for Spring @PreAuthorize checks
        if (this.userTyCode != null && !this.userTyCode.isEmpty()) {
            this.userTyCode.stream().distinct().forEach(code -> {
                String mapped = switch (code) {
                    case "R001" -> "ADMIN"; // treat R001 as Admin
                    case "R002" -> "MANAGER";
                    case "R003" -> "HANDLER";
                    case "R005" -> "REQUESTER";
                    default -> null;
                };
                if (mapped != null) {
                    authoritiesList.add(new SimpleGrantedAuthority("ROLE_" + mapped));
                }
            });
        }

        this.authorities = authoritiesList;
    }

    /**
     * Extract a claim from JWT as a List of Strings.
     * First tries to get the claim directly from the JWT.
     * If not found, checks inside the "attributes" claim.
     * Handles both String and List values.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractClaimAsList(Jwt jwt, String claimName) {
        // Try to get claim directly from JWT
        Object directClaim = jwt.getClaim(claimName);
        if (directClaim != null) {
            if (directClaim instanceof List) {
                return (List<String>) directClaim;
            } else if (directClaim instanceof String) {
                return Collections.singletonList((String) directClaim);
            }
        }
        
        // Fallback: try to get from "attributes" map
        var attributes = jwt.getClaimAsMap("attributes");
        if (attributes != null) {
            var value = attributes.get(claimName);
            if (value instanceof List) {
                return (List<String>) value;
            } else if (value instanceof String) {
                return Collections.singletonList((String) value);
            }
        }
        
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 doesn't use passwords
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getters for custom attributes
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<String> getUserTyCode() {
        return userTyCode;
    }

    public List<String> getUserSttusCode() {
        return userSttusCode;
    }

    public List<String> getDeptCd() {
        return deptCd;
    }

    public List<String> getDeptNm() {
        return deptNm;
    }

    public List<String> getPosition() {
        return position;
    }

    public List<String> getClassNm() {
        return classNm;
    }

    public Jwt getJwt() {
        return jwt;
    }
}
