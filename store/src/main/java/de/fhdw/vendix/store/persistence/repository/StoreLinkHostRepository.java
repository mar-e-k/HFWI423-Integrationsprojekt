package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.entity.StoreLinkHost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLinkHostRepository extends JpaRepository<StoreLinkHost, Long> {
    void deleteByStore(Store store);
    void deleteByStoreId(Long storeId);
}