package com.example.application.data.storageLocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    boolean existsByStorageZoneAndShelfIDAndCompartmentID(
            String storageZone,
            Integer shelfID,
            Integer compartmentID
    );

    Optional<StorageLocation> findByStorageZoneAndShelfIDAndCompartmentID(
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

    @Modifying
    @Transactional
    @Query("DELETE FROM StorageLocation s WHERE s.storageStatus = 'Available'")
    int deleteAllAvailable();
}
