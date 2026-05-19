package de.fhdw.vendix.commons.spring.web.api.scheme;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SecurityScheme(
        name = "storeRoutingHeader",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-Store-ID",
        description = "The Store ID to route the request to the correct store. Enter the ID of the store you want to use."
)
@SecurityRequirement(name = "storeRoutingHeader")
public @interface StoreRoutingOpenApiScheme {}