package de.fhdw.vendix.commons.spring.web.api.orchestrator;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/register")
@Tag(name = "Register", description = "Operations related to registers.")
@SecurityRequirement(name = "keycloakAuth")
public interface RegisterApi {

    @Operation(
            summary = "Get register by ID",
            description = "Retrieves a single register by its ID."
    )
    @GetExchange("/id/{id}")
    ResponseEntity<RegisterDTO> getRegisterById(@PathVariable Long id);
}