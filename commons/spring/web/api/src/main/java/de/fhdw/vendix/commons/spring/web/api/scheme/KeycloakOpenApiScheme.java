package de.fhdw.vendix.commons.spring.web.api.scheme;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SecurityScheme(
        name = "keycloakAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter Keycloak access token (Bearer JWT). Issued by Keycloak realm 'vendix'."
)
@SecurityRequirement(name = "keycloakAuth")
public @interface KeycloakOpenApiScheme { }