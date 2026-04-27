package de.fhdw.vendix.orchestrator.core.gateway;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> dynamicRoutes(
            InstanceRegistry registry,
            VendixRouteFactory routeFactory) {

        // This effectively "refreshes" the routing table by
        // re-scanning the registry for every request (or use a cache)
        return RouterFunctions.route()
                .add(() -> registry.getAll().stream()
                        .map(routeFactory::createRoutes)
                        .reduce(RouterFunction::and)
                        .orElse(null))
                .build();
    }
}