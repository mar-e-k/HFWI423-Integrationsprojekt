package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.StoreLock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreLockRepository extends CrudRepository<StoreLock, Long> {
    Optional<StoreLock> findByStoreId(String storeId);
    void deleteByStoreId(String storeId);
    boolean existsByLockedByInstanceId(String instanceId);
}
