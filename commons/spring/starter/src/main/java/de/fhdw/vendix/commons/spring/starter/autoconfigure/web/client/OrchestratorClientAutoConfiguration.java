package de.fhdw.vendix.commons.spring.starter.autoconfigure.web.client;

import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakClientProperties;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.RegisterApi;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.StoreApi;
import de.fhdw.vendix.commons.spring.web.core.client.LoggingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class OrchestratorClientAutoConfiguration {

    @Bean
    public HttpServiceProxyFactory orchestratorClientFactory(
            OAuth2AuthorizedClientManager authorizedClientManager,
            KeycloakClientProperties clientProperties
    ) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(
                request -> clientProperties.appName()
        );

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .requestInterceptor(interceptor)
                .defaultStatusHandler(
                        s -> s.is2xxSuccessful() || s.is4xxClientError(),
                        new LoggingHandler()
                )
                .build();
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    @Bean
    public RegisterApi registerClient(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(RegisterApi.class);
    }

    @Bean
    public StoreApi storeClient(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(StoreApi.class);
    }
}