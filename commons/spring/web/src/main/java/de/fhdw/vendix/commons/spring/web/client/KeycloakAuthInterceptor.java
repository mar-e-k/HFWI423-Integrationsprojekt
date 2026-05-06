package de.fhdw.vendix.commons.spring.web.client;

import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRegistration;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakServicePrincipal;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;
import java.util.List;

public final class KeycloakAuthInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthInterceptor.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final KeycloakRegistration registration;
    private final KeycloakServicePrincipal servicePrincipal;

    public KeycloakAuthInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            KeycloakRegistration registration,
            KeycloakServicePrincipal servicePrincipal) {
        this.authorizedClientManager = authorizedClientManager;
        this.registration = registration;
        this.servicePrincipal = servicePrincipal;
    }

    @Override
    @NullMarked
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String principalName = (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : servicePrincipal.getValue();

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registration.getId())
                .principal(principalName)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        }

        request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));

        return execution.execute(request, body);
    }
}