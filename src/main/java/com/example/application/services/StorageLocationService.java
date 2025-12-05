package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StorageLocationService {

    private final StorageLocationRepository storageLocationRepository;
    private final ArticleInfoRepository articleInfoRepository;
    public StorageLocationService(StorageLocationRepository storageLocationRepository,
                                  ArticleInfoRepository articleInfoRepository) {
        this.storageLocationRepository = storageLocationRepository;
        this.articleInfoRepository = articleInfoRepository;
    }

    public List<StorageLocation> findAll() {
        return storageLocationRepository.findAll();
    }

    public StorageLocation save(StorageLocation s) {
        return storageLocationRepository.save(s);
    }

    public void delete(StorageLocation s) {
        // 1) Status-Check
        if ("Used".equalsIgnoreCase(s.getStorageStatus())) {
            throw new IllegalStateException(
                    "Storage location " + s.getGeneralId()
                            + " is currently assigned and cannot be deleted."
            );
        }

        // 2) Sicherheitsnetz über ArticleInfo (direkt über Repository)
        String generalId = s.getGeneralId();
        if (generalId != null && !generalId.isBlank()
                && articleInfoRepository.existsByStorageLocation(generalId)) {
            throw new IllegalStateException(
                    "Storage location " + generalId
                            + " is assigned to one or more articles and cannot be deleted."
            );
        }

        storageLocationRepository.delete(s);
    }

    /**
     * Synchronisiert den Status aller Lagerplätze mit den ArticleInfos.
     * - Wenn ein Lagerplatz von keinem Artikel verwendet wird -> "Available"
     * - Wenn ein Lagerplatz von mindestens einem Artikel verwendet wird -> "Used"
     *
     * @return Anzahl der geänderten Lagerplätze
     */
    @Transactional
    public int syncStatusesWithArticles() {
        List<StorageLocation> all = storageLocationRepository.findAll();
        int changed = 0;

        for (StorageLocation loc : all) {
            String generalId = loc.getGeneralId();
            if (generalId == null || generalId.isBlank()) {
                continue;
            }

            boolean usedByArticle = articleInfoRepository.existsByStorageLocation(generalId);

            String current = loc.getStorageStatus();
            String target = usedByArticle ? "Used" : "Available";

            if (!target.equalsIgnoreCase(current)) {
                loc.setStorageStatus(target);
                storageLocationRepository.save(loc);
                changed++;
            }
        }

        return changed;
    }

    public boolean existsByZoneShelfCompartment(String zone, Integer shelfId, Integer compartmentID) {
        return storageLocationRepository.existsByStorageZoneAndShelfIDAndCompartmentID(zone, shelfId, compartmentID);
    }

    public boolean existsDuplicateForEdit(StorageLocation s) {
        if (s.getId() == null) {
            return existsByZoneShelfCompartment(s.getStorageZone(), s.getShelfID(), s.getCompartmentID());
        }
        return storageLocationRepository.existsByStorageZoneAndShelfIDAndCompartmentIDAndIdNot(
                s.getStorageZone(),
                s.getShelfID(),
                s.getCompartmentID(),
                s.getId()
        );
    }

    public List<StorageLocation> findAllAvailable() {
        return storageLocationRepository.findByStorageStatus("Available");
    }

    public Optional<StorageLocation> findByZoneShelfCompartment(String zone, Integer shelfId, Integer compartmentId) {
        return storageLocationRepository.findByStorageZoneAndShelfIDAndCompartmentID(zone, shelfId, compartmentId);
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
            return storageLocationRepository.save(s);
        } catch (DataIntegrityViolationException ex) {
            // Fallback, falls DB-Constraint trotzdem zuschlägt (Race-Conditions etc.)
            throw new IllegalStateException("Could not save storage location due to database constraint", ex);
        }
    }
}


