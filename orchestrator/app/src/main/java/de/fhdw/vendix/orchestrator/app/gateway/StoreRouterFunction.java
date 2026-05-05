package de.fhdw.vendix.orchestrator.app.gateway;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class StoreRouterFunction implements RouterFunction<ServerResponse> {

    private final DiscoveryClient discoveryClient;

    public StoreRouterFunction(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    @NullMarked
    public Optional<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return RouterFunctions.route()
                .nest(GatewayRequestPredicates.path("/api/receipt/**", "/api/voucher/**"), builder -> builder
                        .filter(this::storeRoutingFilter)
                )
                .build()
                .route(request);
    }

    private ServerResponse storeRoutingFilter(ServerRequest clientRequest, HandlerFunction<ServerResponse> next) throws Exception {
        // 1. Extract Routing Key
        String storeId = clientRequest.headers().firstHeader("X-Store-Id");
        if (storeId == null) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .body("Header 'X-Store-Id' is required for this endpoint.");
        }

        // 2. Discover Target Instance
        Optional<URI> targetUri = findTargetInstanceUri(storeId);
        if (targetUri.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .body("No active store-service found for Store-Id: " + storeId);
        }

        // 3. Mutate Request for Proxying
        // We attach the discovered URI to the internal Gateway attributes
        ServerRequest mutatedRequest = ServerRequest.from(clientRequest)
                .attribute(MvcUtils.GATEWAY_REQUEST_URL_ATTR, targetUri.get())
                .build();

        // 4. Delegate to the Proxy Handler
        return HandlerFunctions.http().handle(mutatedRequest);
    }

    private Optional<URI> findTargetInstanceUri(String storeId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("store-service");

        return instances.stream()
                .filter(instance -> {
                    Map<String, String> metadata = instance.getMetadata();
                    return metadata != null && storeId.equals(metadata.get("storeId"));
                })
                .map(ServiceInstance::getUri)
                .findFirst();
    }
}