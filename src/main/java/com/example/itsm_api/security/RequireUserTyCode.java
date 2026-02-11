package com.example.itsm_api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Custom annotation to require specific user type code(s)
 * 
 * Usage:
 * @RequireUserTyCode("R005")
 * @RequireUserTyCode({"R005", "R001"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUserTyCode {
    String[] value();
}
