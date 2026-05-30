package de.fhdw.vendix.commons.spring.starter.autoconfigure.web.client;

import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.web.api.store.*;
import de.fhdw.vendix.commons.spring.web.core.client.LoggingHandler;
import de.fhdw.vendix.commons.spring.web.core.client.StoreHeaderInjectorInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@AutoConfiguration
public class StoreClientAutoConfiguration {

    @Bean
    public HttpServiceProxyFactory storeClientFactory(
            OAuth2AuthorizedClientManager authorizedClientManager,
            StoreContext storeContext,
            RegisterContext registerContext
    ) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .requestInterceptor(
                        new OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
                )
                .requestInterceptor(
                        new StoreHeaderInjectorInterceptor(storeContext, registerContext)
                )
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
    public ArticleApi articleApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ArticleApi.class);
    }

    @Bean
    public ReceiptApi receiptApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ReceiptApi.class);
    }

    @Bean
    public StoreStockApi storeStock(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(StoreStockApi.class);
    }

    @Bean
    public StoreStockOrderApi storeStockOrderApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(StoreStockOrderApi.class);
    }

    @Bean
    public VoucherApi voucherApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(VoucherApi.class);
    }
}