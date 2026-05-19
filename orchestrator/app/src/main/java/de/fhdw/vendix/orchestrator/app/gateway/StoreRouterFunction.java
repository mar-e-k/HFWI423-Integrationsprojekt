package de.fhdw.vendix.orchestrator.app.gateway;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.*;

import java.net.URI;
import java.util.Optional;

@Component
public class StoreRouterFunction {

    private final DiscoveryClient discoveryClient;

    public StoreRouterFunction(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean
    public RouterFunction<ServerResponse> storeRoutes() {
        return RouterFunctions.route()
                .nest(
                        GatewayRequestPredicates.path("/api/receipt/**")
                                .or(GatewayRequestPredicates.path("/api/voucher/**")),
                        builder -> builder
//                                .route(RequestPredicates.all())
                                .filter(TokenRelayFilterFunctions.tokenRelay())
                                .filter(this::storeRoutingFilter)
                                .build()
                )
                .build();
    }

    private ServerResponse storeRoutingFilter(ServerRequest clientRequest, HandlerFunction<ServerResponse> next) throws Exception {
        String storeId = clientRequest.headers().firstHeader("X-Vendix-Store-Id");

        if (storeId == null) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).body("Missing Store-Id");
        }

        return findTargetInstanceUri(storeId)
                .map(targetUri -> {
                    clientRequest.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, targetUri);
                    try {
                        return HandlerFunctions.http().handle(clientRequest);
                    } catch (Exception e) {
                        return ServerResponse.status(500).build();
                    }
                })
                .orElseGet(() -> ServerResponse.status(404).body("No instance for " + storeId));
    }

    private Optional<URI> findTargetInstanceUri(String storeId) {
        return discoveryClient.getInstances("store-app").stream()
                .filter(instance ->
                        Optional.ofNullable(instance.getMetadata())
                                .map(metadata -> metadata.get("storeId"))
                                .filter(storeId::equals)
                                .isPresent()
                )
                .map(ServiceInstance::getUri)
                .findFirst();
    }
}