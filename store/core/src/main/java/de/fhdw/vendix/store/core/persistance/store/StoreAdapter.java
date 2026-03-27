package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.port.StoreCommandPort;
import de.fhdw.vendix.commons.api.domain.store.port.StoreQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
class StoreAdapter extends AbstractDtoCrudAdapter<Store, StoreDTO, Long> implements StoreQueryPort, StoreCommandPort {

    private final StoreEntityAdapter storeEntityAdapter;
    private final StoreMapper storeMapper;

    StoreAdapter(StoreEntityAdapter storeEntityAdapter, StoreMapper storeMapper) {
        super(storeEntityAdapter, storeMapper);
        this.storeEntityAdapter = storeEntityAdapter;
        this.storeMapper = storeMapper;
    }

    @Override
    public Set<StoreDTO> getAllActiveStores() {
        return storeEntityAdapter.getAllActiveStores().stream()
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<StoreDTO> getAllInactiveActiveStores() {
        return storeEntityAdapter.getAllInactiveStores().stream()
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }
}