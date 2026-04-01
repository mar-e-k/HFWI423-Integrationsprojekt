package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.store.core.persistance.register.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

interface StoreRepository extends JpaRepository<Store, Long> {
    @Query(
            """
            SELECT s
            FROM Store s
            WHERE EXISTS (
                SELECT 1 FROM Lock l WHERE l.targetType = 'STORE' AND l.targetId = s.id
            )
            """
    )
    Set<Store> findAllActiveStores();


    @Query(
            """
            SELECT s
            FROM Store s
            WHERE NOT EXISTS (
                SELECT 1 FROM Lock l WHERE l.targetType = 'STORE' AND l.targetId = s.id
            )
            """
    )
    Set<Store> findAllInactiveStores();


    @Query(
            """
            SELECT r
            FROM Register r
            WHERE r.store.id = :storeId
            """
    )
    Set<Register> findAllRegisters(@Param("storeId") long storeId);
}