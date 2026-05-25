package com.example.application.api.lagerplatz;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import com.example.application.services.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lagerplaetze")
@Tag(name = "Lagerplaetze", description = "Lagerplatzverwaltung")
public class LagerplatzController {

    private final StorageLocationService storageLocationService;
    private final StorageLocationRepository storageLocationRepository;

    public LagerplatzController(StorageLocationService storageLocationService,
                                StorageLocationRepository storageLocationRepository) {
        this.storageLocationService = storageLocationService;
        this.storageLocationRepository = storageLocationRepository;
    }

    @Operation(summary = "Alle Lagerplaetze abrufen")
    @GetMapping
    public List<StorageLocation> storageLocations() {
        return storageLocationService.findAll();
    }

    @Operation(summary = "Neuen Lagerplatz anlegen")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocation createStorageLocation(@RequestBody StorageLocation storageLocation) {
        try {
            storageLocation.setId(null);
            return storageLocationService.saveWithDuplicateCheck(storageLocation);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Lagerplatz loeschen")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStorageLocation(@PathVariable Long id) {
        StorageLocation existing = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "StorageLocation " + id + " nicht gefunden"));
        try {
            storageLocationService.delete(existing);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @Operation(summary = "Lagerplatz-Status mit Artikeln synchronisieren")
    @PostMapping("/sync")
    public Map<String, Integer> syncStorageLocations() {
        int changed = storageLocationService.syncStatusesWithArticles();
        return Map.of("updated", changed);
    }
}
