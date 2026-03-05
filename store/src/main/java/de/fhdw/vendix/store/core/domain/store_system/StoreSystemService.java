package de.fhdw.vendix.store.core.domain.store_system;

import de.fhdw.vendix.commons.api.domain.store_system.port.StoreSystemCommandPort;
import de.fhdw.vendix.commons.api.domain.store_system.port.StoreSystemQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class StoreSystemService extends AbstractSpringDataCrudAdapter<StoreSystem, Long> implements StoreSystemCommandPort, StoreSystemQueryPort {

    private final StoreSystemRepository repository;
    private final StoreSystemMapper mapper;

    public StoreSystemService(StoreSystemRepository repository, StoreSystemMapper mapper) {
        super(repository);
        this.repository = repository;
        this.mapper = mapper;
    }
}