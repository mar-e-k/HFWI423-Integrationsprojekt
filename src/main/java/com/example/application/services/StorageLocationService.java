package com.example.application.services;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.util.List;

@Service
public class StorageLocationService {

    private final StorageLocationRepository repo;
    private final ArticleInfoService articleInfoService;

    public StorageLocationService(StorageLocationRepository repo,ArticleInfoService articleInfoService) {
        this.repo = repo;
        this.articleInfoService = articleInfoService;
    }

    public List<StorageLocation> findAll() {
        return repo.findAll();
    }

    public StorageLocation save(StorageLocation s) {
        return repo.save(s);
    }

    public void delete(StorageLocation s) {
        // 1) Status-Check
        if ("Used".equalsIgnoreCase(s.getStorageStatus())) {
            throw new IllegalStateException(
                    "Storage location " + s.getGeneralId()
                            + " is currently assigned and cannot be deleted."
            );
        }

        // 2) Sicherheitsnetz über ArticleInfo
        String generalId = s.getGeneralId();
        if (articleInfoService.existsForLocation(generalId)) {
            throw new IllegalStateException(
                    "Storage location " + generalId
                            + " is assigned to one or more articles and cannot be deleted."
            );
        }

        repo.delete(s);
    }

    public boolean existsByZoneShelfCompartment(String zone, Integer shelfId, Integer compartmentID) {
        return repo.existsByStorageZoneAndShelfIDAndCompartmentID(zone, shelfId, compartmentID);
    }

    public boolean existsDuplicateForEdit(StorageLocation s) {
        if (s.getId() == null) {
            return existsByZoneShelfCompartment(s.getStorageZone(), s.getShelfID(), s.getCompartmentID());
        }
        return repo.existsByStorageZoneAndShelfIDAndCompartmentIDAndIdNot(
                s.getStorageZone(),
                s.getShelfID(),
                s.getCompartmentID(),
                s.getId()
        );
    }

    public List<StorageLocation> findAllAvailable() {
        return repo.findByStorageStatus("Available");
    }

    public Optional<StorageLocation> findByZoneShelfCompartment(String zone, Integer shelfId, Integer compartmentId) {
        return repo.findByStorageZoneAndShelfIDAndCompartmentID(zone, shelfId, compartmentId);
    }

    /**
     * Gemeinsame Save-Methode mit Duplikatsprüfung + DataIntegrity-Handling.
     */
    public StorageLocation saveWithDuplicateCheck(StorageLocation s) {
        if (existsDuplicateForEdit(s)) {
            throw new IllegalStateException(
                    "Storage location already exists for Zone="
                            + s.getStorageZone()
                            + ", Shelf="
                            + s.getShelfID()
                            + ", Compartment="
                            + s.getCompartmentID()
            );
        }

        try {
            return repo.save(s);
        } catch (DataIntegrityViolationException ex) {
            // Fallback, falls DB-Constraint trotzdem zuschlägt (Race-Conditions etc.)
            throw new IllegalStateException("Could not save storage location due to database constraint", ex);
        }
    }
}


