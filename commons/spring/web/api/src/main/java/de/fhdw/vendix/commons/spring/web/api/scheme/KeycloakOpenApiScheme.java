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
        type = SecuritySchemeType.OPENIDCONNECT,
        openIdConnectUrl = "/realms/vendix/.well-known/openid-configuration",
        description = "Authentication with Keycloak using OpenID Connect Discovery. The UI will use this endpoint to automatically configure authentication flows."
)
@SecurityRequirement(name = "keycloakAuth")
public @interface KeycloakOpenApiScheme { }