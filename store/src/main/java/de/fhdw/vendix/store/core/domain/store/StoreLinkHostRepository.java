package de.fhdw.vendix.store.core.domain.store;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLinkHostRepository extends JpaRepository<StoreLinkHost, Long> {
    void deleteByStore(Store store);
    void deleteByStoreId(Long storeId);
}