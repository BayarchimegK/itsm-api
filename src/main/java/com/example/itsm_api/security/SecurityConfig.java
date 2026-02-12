package com.example.itsm_api.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080/realms/itsm}")
    private String issuerUri;

    @Autowired
    private JwtUserTypeCodeInterceptor jwtUserTypeCodeInterceptor;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://192.168.0.12:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}

    /**
     * Register JWT user type code interceptor
     * Extracts userTyCode from JWT token and stores in request attributes
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtUserTypeCodeInterceptor);
    }

    /**
     * Custom JWT Authentication Converter that extracts user attributes
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        var realmAccess = jwt.getClaimAsMap("realm_access");
        var authorities = new java.util.ArrayList<GrantedAuthority>();

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            var roles = (Collection<String>) realmAccess.get("roles");
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .forEach(authorities::add);
        }

        // Also map userTyCode claim values (e.g. R001 -> MANAGER) into ROLE_* authorities
        Object userTyClaim = jwt.getClaim("userTyCode");
        java.util.List<String> userTyCodes = new java.util.ArrayList<>();
        if (userTyClaim instanceof Collection) {
            @SuppressWarnings("unchecked")
            var list = (Collection<String>) userTyClaim;
            userTyCodes.addAll(list);
        } else if (userTyClaim instanceof String) {
            userTyCodes.add((String) userTyClaim);
        } else {
            // try attributes map fallback
            var attrs = jwt.getClaimAsMap("attributes");
            if (attrs != null && attrs.get("userTyCode") != null) {
                Object v = attrs.get("userTyCode");
                if (v instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    var list = (Collection<String>) v;
                    userTyCodes.addAll(list);
                } else if (v instanceof String) {
                    userTyCodes.add((String) v);
                }
            }
        }

        for (String code : userTyCodes) {
            String mapped = switch (code) {
                case "R001" -> "ADMIN"; // treat R001 as Admin by request
                case "R002" -> "MANAGER";
                case "R003" -> "HANDLER";
                case "R005" -> "REQUESTER";
                default -> null;
            };
            if (mapped != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + mapped));
            }
        }

        return authorities;
    }

    /**
     * Configure JWT decoder with Keycloak issuer
     * Uses lazy initialization to allow app to start even if Keycloak is unavailable
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return new LazyJwtDecoder(issuerUri);
    }

    /**
     * Custom JwtDecoder that defers Keycloak configuration resolution until first use
     */
    private static class LazyJwtDecoder implements JwtDecoder {
        private final String issuerUri;
        private JwtDecoder delegate;
        private boolean initialized = false;
        private Exception initException;

        LazyJwtDecoder(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        private void initializeDelegate() {
            if (!initialized) {
                try {
                    this.delegate = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
                } catch (Exception e) {
                    this.initException = e;
                    System.err.println("Warning: Failed to initialize JWT decoder - Keycloak may be unavailable: " + e.getMessage());
                }
                initialized = true;
            }
        }

        @Override
        public Jwt decode(String token) {
            initializeDelegate();
            if (delegate != null) {
                try {
                    return delegate.decode(token);
                } catch (org.springframework.security.oauth2.jwt.JwtException je) {
                    // already a JwtException -> rethrow so the framework treats it as auth failure
                    throw je;
                } catch (Exception e) {
                    // Unwrap causes to detect parser ParseException (thrown by Nimbus) and map it to JwtException
                    Throwable cause = e;
                    while (cause != null) {
                        if (cause instanceof java.text.ParseException) {
                            throw new org.springframework.security.oauth2.jwt.JwtException("Malformed JWT: " + cause.getMessage(), e);
                        }
                        cause = cause.getCause();
                    }
                    // wrap other exceptions to JwtException so Spring Security returns 401
                    throw new org.springframework.security.oauth2.jwt.JwtException("Error decoding JWT: " + e.getMessage(), e);
                }
            } else if (initException != null) {
                throw new IllegalStateException("JWT decoder not available - Keycloak configuration could not be resolved", initException);
            }
            throw new IllegalStateException("JWT decoder initialization failed");
        }
    }
}
