package com.example.application.api.load;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import com.example.application.services.StorageLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Lasttest-Endpunkte für StorageLocationView.
 * Basis-URL: /api/load/storage-locations
 */
@RestController
@RequestMapping("/api/load/storage-locations")
public class LoadTestStorageController {

    private final StorageLocationService storageLocationService;
    private final StorageLocationRepository storageLocationRepository;

    public LoadTestStorageController(StorageLocationService storageLocationService,
                                     StorageLocationRepository storageLocationRepository) {
        this.storageLocationService = storageLocationService;
        this.storageLocationRepository = storageLocationRepository;
    }

    /** GET /api/load/storage-locations – alle Lagerplaetze */
    @GetMapping
    public List<StorageLocation> storageLocations() {
        return storageLocationService.findAll();
    }

    /** POST /api/load/storage-locations – neuen Lagerplatz anlegen */
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

    /** DELETE /api/load/storage-locations/{id} – Lagerplatz löschen */
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

    /** POST /api/load/storage-locations/sync – Status mit Artikeln synchronisieren */
    @PostMapping("/sync")
    public Map<String, Integer> syncStorageLocations() {
        int changed = storageLocationService.syncStatusesWithArticles();
        return Map.of("updated", changed);
    }
}
