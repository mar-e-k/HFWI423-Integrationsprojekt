package de.fhdw.vendix.store.api.controller;

import de.fhdw.vendix.commons.core.api.dto.RegisterDTO;
import de.fhdw.vendix.commons.core.api.dto.SystemClientDTO;
import de.fhdw.vendix.store.api.mapper.RegisterMapper;
import de.fhdw.vendix.store.persistence.service.other.RegisterRegistryService;
import de.fhdw.vendix.store.utility.RegisterClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
@Tag(name = "Kassensystem Registry", description = "Endpoints for operations related to the registry of a kassensystem")
public class RegisterRegistryController {

    private final RegisterRegistryService registerRegistryService;
    private final RegisterMapper registerMapper;

    public RegisterRegistryController(RegisterRegistryService registerRegistryService, RegisterMapper registerMapper) {
        this.registerRegistryService = registerRegistryService;
        this.registerMapper = registerMapper;
    }

    @PostMapping
    @Operation(summary = "Register a kassensystem")
    public ResponseEntity<RegisterDTO> registerKassensystem(@RequestBody SystemClientDTO systemClientDTO) {
        registerRegistryService.addRegistry(systemClientDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(registerRegistryService.findRegistryById(systemClientDTO.getId())
                        .map(RegisterClient::getRegister)
                        .map(registerMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deregister a kassensystem")
    public ResponseEntity<Void> deregisterKassensystem(@PathVariable String id) {
        registerRegistryService.deleteRegistry(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
