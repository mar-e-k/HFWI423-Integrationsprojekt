package de.fhdw.fillialensystem.api.registry;

import de.fhdw.commons.api.dto.SystemClientDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
@Tag(name = "Kassensystem Registry", description = "Endpoints for operations related to the registry of a kassensystem")
public class KassensystemRegistryController {

    private final KassensystemRegistryService kassensystemRegistryService;

    public KassensystemRegistryController(KassensystemRegistryService kassensystemRegistryService) {
        this.kassensystemRegistryService = kassensystemRegistryService;
    }

    // ---- Endpoints ----

    @PostMapping
    @Operation(summary = "Register a kassensystem")
    public ResponseEntity<Void> registerKassensystem(@RequestBody SystemClientDTO systemClientDTO) {
        kassensystemRegistryService.addRegistry(systemClientDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deregister a kassensystem")
    public ResponseEntity<Void> deregisterKassensystem(@PathVariable String id) {
        kassensystemRegistryService.deleteRegistry(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
