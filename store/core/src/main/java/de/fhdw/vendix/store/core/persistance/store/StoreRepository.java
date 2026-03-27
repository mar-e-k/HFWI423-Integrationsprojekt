package de.fhdw.vendix.store.core.persistance.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

interface StoreRepository extends JpaRepository<Store, Long> {
    @Query(
            """
            SELECT s
            FROM Store s
            WHERE EXISTS (
                SELECT l.targetId FROM Lock l WHERE l.targetType = 'STORE'
            )
            """
    )
    Set<Store> findAllActiveStores();


    @Query(
            """
            SELECT s
            FROM Store s
            WHERE NOT EXISTS (
                SELECT l.targetId FROM Lock l WHERE l.targetType = 'STORE'
            )
            """
    )
    Set<Store> findAllInactiveStores();
}