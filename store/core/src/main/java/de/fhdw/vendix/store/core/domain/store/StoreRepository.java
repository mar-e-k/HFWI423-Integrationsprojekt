package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.store.core.domain.register.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(
            """
            SELECT r
            FROM Register r
            WHERE r.store.id = :storeId
            """
    )
    Set<Register> findAllRegisters(@Param("storeId") long storeId);
}