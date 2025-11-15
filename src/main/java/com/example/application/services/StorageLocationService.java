package com.example.application.services;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageLocationService {
    private final StorageLocationRepository repo;

    public StorageLocationService(StorageLocationRepository repo) {
        this.repo = repo;
    }

    public List<StorageLocation> findAll() {
        return repo.findAll();
    }

    public StorageLocation save(StorageLocation s) {
        return repo.save(s);
    }

    public boolean existsByZoneShelfCompartment(String zone, Integer shelfId, Integer compartmentID) {
        return repo.existsByStorageZoneAndShelfIDAndCompartmentID(zone, shelfId, compartmentID);
    }
    public void delete(StorageLocation s) {
        repo.delete(s);
    }

    public boolean existsDuplicateForEdit(StorageLocation s) {
        // Falls aus irgendeinem Grund noch keine ID da ist, verhalten wie "neu"
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
}

