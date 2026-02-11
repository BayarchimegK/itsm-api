package com.example.itsm_api.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080/realms/itsm}")
    private String issuerUri;

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
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        var roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
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
                } catch (Exception e) {
                    System.err.println("ERROR decoding JWT token: " + e.getMessage());
                    System.err.println("Issuer URI: " + issuerUri);
                    e.printStackTrace();
                    throw e;
                }
            } else if (initException != null) {
                throw new IllegalStateException("JWT decoder not available - Keycloak configuration could not be resolved", initException);
            }
            throw new IllegalStateException("JWT decoder initialization failed");
        }
    }
}
