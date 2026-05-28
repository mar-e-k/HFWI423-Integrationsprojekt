package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.spring.web.api.scheme.KeycloakOpenApiScheme;
import de.fhdw.vendix.commons.spring.web.api.scheme.StoreRoutingOpenApiScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/store-stock")
@Tag(
        name = "Store Stock",
        description = "Operations for managing store stock."
)
@KeycloakOpenApiScheme
@StoreRoutingOpenApiScheme
public interface StoreStockApi {

}