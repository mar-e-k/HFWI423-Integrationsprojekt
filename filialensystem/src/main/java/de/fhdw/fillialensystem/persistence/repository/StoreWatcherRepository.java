package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.StoreWatcher;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreWatcherRepository extends CrudRepository<StoreWatcher, Long> {
    Optional<StoreWatcher> findByInstanceId(String instanceId);
    void deleteByInstanceId(String instanceId);
}
