package de.fhdw.vendix.store.core.domain.store_system;

import de.fhdw.vendix.store.core.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreSystemRepository extends JpaRepository<StoreSystem, Long> {
    void deleteByStore(Store store);
    void deleteByStoreId(Long storeId);
}