package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.StoreWatcher;
import de.fhdw.fillialensystem.persistence.repository.StoreWatcherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StoreWatcherService extends AbstractCrudService<StoreWatcher, Long> {

    private final StoreWatcherRepository storeWatcherRepository;

    public StoreWatcherService(StoreWatcherRepository storeWatcherRepository) {
        super(storeWatcherRepository);
        this.storeWatcherRepository = storeWatcherRepository;
    }

    public Optional<StoreWatcher> findByInstanceId(String instanceId) {
        return storeWatcherRepository.findByInstanceId(instanceId);
    }

    @Transactional
    public void deleteByInstanceId(String instanceId) {
        storeWatcherRepository.deleteByInstanceId(instanceId);
    }
}
