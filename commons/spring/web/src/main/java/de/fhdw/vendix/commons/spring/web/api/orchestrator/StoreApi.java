package de.fhdw.vendix.commons.spring.web.api.orchestrator;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api/store")
@Tag(name = "Store", description = "Operations related to stores.")
@SecurityRequirement(name = "keycloakAuth")
public interface StoreApi {

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange()
    ResponseEntity<List<StoreDTO>> getStores();

    @Operation(
            summary = "Get store by ID",
            description = "Retrieves a single store by its ID."
    )
    @GetExchange("/id/{id}")
    ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/locked")
    ResponseEntity<List<StoreDTO>> getLockedStores();

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/non-locked")
    ResponseEntity<List<StoreDTO>> getNonLockedStores();

    @Operation(
            summary = "Get all registers for a store",
            description = "Retrieves a list of all registers for a given store."
    )
    @GetExchange("/id/{id}/register")
    ResponseEntity<List<RegisterDTO>> getStoreRegisters(@PathVariable Long id);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/id/{id}/register/locked")
    ResponseEntity<List<RegisterDTO>> getLockedStoreRegisters(@PathVariable Long id);

    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @GetExchange("/id/{id}/register/non-locked")
    ResponseEntity<List<RegisterDTO>> getNonLockedStoreRegisters(@PathVariable Long id);
}