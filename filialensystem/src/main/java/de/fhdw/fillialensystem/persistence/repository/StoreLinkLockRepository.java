package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreLinkLockRepository extends JpaRepository<StoreLinkLock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT 1 from StoreLinkLock s WHERE s.store = :store")
    StoreLinkLock lockByStore(@Param("store") Store store);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT 1 from StoreLinkLock s WHERE s.store.id = :storeId")
    StoreLinkLock lockByStoreId(@Param("storeId") Long storeId);

    void deleteByStore(Store store);

    void deleteByStoreId(Long storeId);
}