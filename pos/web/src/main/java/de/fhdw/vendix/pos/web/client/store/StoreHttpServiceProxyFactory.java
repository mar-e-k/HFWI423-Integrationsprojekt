package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.api.embeddable.InstanceDetailsDTO;
import de.fhdw.vendix.commons.spring.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
class StoreHttpServiceProxyFactory {
    private static final Logger log = LoggerFactory.getLogger(StoreHttpServiceProxyFactory.class);
    private final JwtService jwtService;

    public StoreHttpServiceProxyFactory(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public <T> T createClient(Class<T> clientType, InstanceDetailsDTO instance) {
        String baseUrl = "http://%s:%d".formatted(instance.server(), instance.port());

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(jwtService.generateToken());
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(
                        status -> status.is2xxSuccessful() || status.is4xxClientError(),
                        (request, response) -> log.atDebug().log(
                                "[{}] from {}",
                                response.getStatusCode(),
                                request.getURI()
                        )
                )
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                        .builderFor(RestClientAdapter.create(restClient))
                        .build();
        return proxyFactory.createClient(clientType);
    }
}
