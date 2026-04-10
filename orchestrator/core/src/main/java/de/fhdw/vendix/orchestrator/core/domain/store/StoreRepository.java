package de.fhdw.vendix.orchestrator.core.domain.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(
            """
            SELECT s
            FROM Store s
            WHERE EXISTS (
                SELECT 1
                FROM DistributedLock l
                WHERE l.target.id = s.id
                    AND l.target.type = STORE
            )
            """
    )
    List<Store> findAllLockedStores();

    @Query(
            """
            SELECT s
            FROM Store s
            WHERE NOT EXISTS (
                SELECT 1
                FROM DistributedLock l
                WHERE l.target.id = s.id
                    AND l.target.type = STORE
            )
            """
    )
    List<Store> findAllNonLockedStores();
}