package com.example.application.data.storageLocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    boolean existsByStorageZoneAndShelfIDAndCompartmentID(
            String storageZone,
            Integer shelfID,
            Integer compartmentID
    );

    boolean existsByStorageZoneAndShelfIDAndCompartmentIDAndIdNot(
            String storageZone,
            Integer shelfID,
            Integer compartmentID,
            Long id
    );

    List<StorageLocation> findByStorageStatus(String storageStatus);
}
