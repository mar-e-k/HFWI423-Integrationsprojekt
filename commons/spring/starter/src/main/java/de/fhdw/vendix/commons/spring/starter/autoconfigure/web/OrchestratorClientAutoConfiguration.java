package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.security.jwt.JwtService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.AccountProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.ConnectionProxyService;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.LockProxyService;
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
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public LockProxyService lockProxyService(HttpServiceProxyFactory orchestratorClientFactory) {
        return orchestratorClientFactory.createClient(LockProxyService.class);
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
}