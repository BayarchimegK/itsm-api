package com.example.itsm_api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to require specific user status code(s)
 * 
 * Usage:
 * @RequireUserSttusCode("U002")
 * @RequireUserSttusCode({"U001", "U002"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireUserSttusCode {
    String[] value();
}
