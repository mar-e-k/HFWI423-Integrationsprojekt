package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkHost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLinkHostRepository extends JpaRepository<StoreLinkHost, Long> {
    void deleteByStore(Store store);
    void deleteByStoreId(Long storeId);
}