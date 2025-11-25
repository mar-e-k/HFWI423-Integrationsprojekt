package de.fhdw.fillialensystem.api.registry;

import de.fhdw.commons.api.dto.SystemClientDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registry")
public class KassensystemRegistryController {

    private final KassensystemRegistryService kassensystemRegistryService;

    public KassensystemRegistryController(KassensystemRegistryService kassensystemRegistryService) {
        this.kassensystemRegistryService = kassensystemRegistryService;
    }

    @PostMapping
    public ResponseEntity<Void> addKassensystemRegistry(@RequestBody SystemClientDTO systemClientDTO) {
        kassensystemRegistryService.addRegistry(systemClientDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteKassensystemRegistry(@RequestParam String id) {
        kassensystemRegistryService.deleteRegistry(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
