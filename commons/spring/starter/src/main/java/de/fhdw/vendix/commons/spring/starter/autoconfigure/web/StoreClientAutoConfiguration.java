package de.fhdw.vendix.commons.spring.starter.autoconfigure.web;

import de.fhdw.vendix.commons.spring.web.api.store.ArticleApi;
import de.fhdw.vendix.commons.spring.web.api.store.CheckoutApi;
import de.fhdw.vendix.commons.spring.web.api.store.ReceiptApi;
import de.fhdw.vendix.commons.spring.web.api.store.VoucherApi;
import de.fhdw.vendix.commons.spring.web.client.LoggingHandler;
import de.fhdw.vendix.commons.spring.web.client.StoreHeaderInjectorInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@AutoConfiguration
public class StoreClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpServiceProxyFactory storeClientFactory(OAuth2AuthorizedClientManager authorizedClientManager) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .requestInterceptor(
                        new OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
                )
                .requestInterceptor(
                        new StoreHeaderInjectorInterceptor()
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
    @ConditionalOnMissingBean
    public ArticleApi articleApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ArticleApi.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public CheckoutApi checkoutApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(CheckoutApi.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReceiptApi receiptApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(ReceiptApi.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public VoucherApi voucherApi(HttpServiceProxyFactory storeClientFactory) {
        return storeClientFactory.createClient(VoucherApi.class);
    }
}