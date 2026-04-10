package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.security.jwt.JwtService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
public class OrchestratorClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public HttpServiceProxyFactory orchestratorClientFactory(JwtService jwtService) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(jwtService.generateToken());
                    request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(
                        s -> s.is2xxSuccessful() || s.is4xxClientError(),
                        (request, response) -> {
                            log.atDebug().log("[{}] from {}.",
                                    response.getStatusCode(),
                                    request.getURI()
                            );
                        })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockProxyService distributedLockProxyService(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(DistributedLockProxyService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionProxyService connectionProxyService(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(ConnectionProxyService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public AccountProxyService accountProxyService(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(AccountProxyService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public StoreProxyService storeProxyService(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(StoreProxyService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterProxyService registerProxyService(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(RegisterProxyService.class);
    }
}