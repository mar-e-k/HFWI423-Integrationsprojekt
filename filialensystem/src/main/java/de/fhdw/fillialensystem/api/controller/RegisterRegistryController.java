package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.SystemClientDTO;
import de.fhdw.fillialensystem.persistence.service.other.RegisterRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
@Tag(name = "Kassensystem Registry", description = "Endpoints for operations related to the registry of a kassensystem")
public class RegisterRegistryController {

    private final RegisterRegistryService registerRegistryService;

    public RegisterRegistryController(RegisterRegistryService registerRegistryService) {
        this.registerRegistryService = registerRegistryService;
    }

    @PostMapping
    @Operation(summary = "Register a kassensystem")
    public ResponseEntity<Void> registerKassensystem(@RequestBody SystemClientDTO systemClientDTO) {
        registerRegistryService.addRegistry(systemClientDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
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
