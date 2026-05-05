package de.fhdw.vendix.commons.spring.web.api;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "keycloakAuth",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "${keycloak.auth-url}/realms/${keycloak.realm}/protocol/openid-connect/auth",
                        tokenUrl = "${keycloak.auth-url}/realms/${keycloak.realm}/protocol/openid-connect/token",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID Connect"),
                                @OAuthScope(name = "profile", description = "User profile information")
                        }
                )
        )
)
class KeycloakAuthFlowConfig {}