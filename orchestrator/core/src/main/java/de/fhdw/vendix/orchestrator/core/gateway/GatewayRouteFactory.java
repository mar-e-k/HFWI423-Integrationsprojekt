package de.fhdw.vendix.orchestrator.core.gateway;

import de.fhdw.vendix.orchestrator.core.embeddable.instance_details.InstanceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Component
public class GatewayRouteFactory {

    public RouterFunction<ServerResponse> createRoutes(InstanceDetails instance) {
        // If context is null, path is /unassigned/uuid
        // If context exists (e.g. 'store-1'), path is /store/store-1/
        String pathPrefix = (instance.getContext() != null)
                ? "/" + instance.getAppType() + "/" + instance.getContext()
                : "/unassigned/" + instance.getInstanceId();

        String targetUri = "http://localhost:" + instance.getPort();

        return GatewayRouterFunctions.route(instance.getInstanceId())
                .route(RequestPredicates.path(pathPrefix + "/**"),
                        ProxyRouterFunctions.proxy(URI.create(targetUri)))
                .build();
    }
}