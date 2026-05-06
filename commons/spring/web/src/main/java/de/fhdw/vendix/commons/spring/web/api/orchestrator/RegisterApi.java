package de.fhdw.vendix.commons.spring.web.api.orchestrator;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api")
@Tag(name = "Register", description = "Operations related to registers.")
@SecurityRequirement(name = "keycloakAuth")
public interface RegisterApi {

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/register")
    ResponseEntity<List<RegisterDTO>> getRegisters();

    @Operation(
            summary = "Get register by ID",
            description = "Retrieves a single register by its ID."
    )
    @GetExchange("/register/{registerId}")
    ResponseEntity<RegisterDTO> getRegisterById(@PathVariable Long registerId);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/register/locked")
    ResponseEntity<List<RegisterDTO>> getLockedRegisters();

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/register/non-locked")
    ResponseEntity<List<RegisterDTO>> getNonLockedRegisters();


    @Operation(
            summary = "Get all registers for a store",
            description = "Retrieves a list of all registers for a given store."
    )
    @GetExchange("/store/{storeId}/register")
    ResponseEntity<List<RegisterDTO>> getStoreRegisters(@PathVariable Long storeId);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/store/{storeId}/register/locked")
    ResponseEntity<List<RegisterDTO>> getLockedStoreRegisters(@PathVariable Long storeId);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/store/{storeId}/register/non-locked")
    ResponseEntity<List<RegisterDTO>> getNonLockedStoreRegisters(@PathVariable Long storeId);
}