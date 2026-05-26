package de.fhdw.vendix.commons.spring.web.api.scheme;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
        name = "X-Store-ID",
        in = ParameterIn.HEADER,
        required = true,
        description = "Store ID used for routing requests"
)
public @interface StoreRoutingOpenApiScheme {}