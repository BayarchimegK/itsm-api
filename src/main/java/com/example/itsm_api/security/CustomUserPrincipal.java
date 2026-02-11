package com.example.itsm_api.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom UserPrincipal that extracts user attributes from Keycloak JWT token
 */
public class CustomUserPrincipal implements UserDetails {

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
        
        // Extract custom attributes from the "attributes" claim in the JWT
        var attributes = jwt.getClaimAsMap("attributes");
        if (attributes != null) {
            this.userTyCode = extractList(attributes, "userTyCode");
            this.userSttusCode = extractList(attributes, "userSttusCode");
            this.deptCd = extractList(attributes, "deptCd");
            this.deptNm = extractList(attributes, "deptNm");
            this.position = extractList(attributes, "position");
            this.classNm = extractList(attributes, "classNm");
        } else {
            this.userTyCode = Collections.emptyList();
            this.userSttusCode = Collections.emptyList();
            this.deptCd = Collections.emptyList();
            this.deptNm = Collections.emptyList();
            this.position = Collections.emptyList();
            this.classNm = Collections.emptyList();
        }

        // Extract realm roles and convert to GrantedAuthority
        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            var roles = (Collection<String>) realmAccess.get("roles");
            this.authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .toList();
        } else {
            this.authorities = Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractList(Object map, String key) {
        if (map instanceof java.util.Map) {
            var value = ((java.util.Map<String, Object>) map).get(key);
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
