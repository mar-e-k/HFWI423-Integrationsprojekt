package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.security.jwt.JwtService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class StoreClientAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StoreClientAutoConfiguration.class);
    

    @Bean
    @ConditionalOnMissingBean
    public HttpServiceProxyFactory storeClientFactory(JwtService jwtService) {
        RestClient restClient = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(jwtService.generateToken());
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
    public ArticleProxyService articleProxyService(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ArticleProxyService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReceiptProxyService receiptProxyService(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ReceiptProxyService.class);
    }
}