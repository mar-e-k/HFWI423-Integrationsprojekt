package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.domain.register.RegisterMapper;
import de.fhdw.vendix.store.core.domain.store.service.StoreService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
class StoreAdapter extends AbstractDtoCrudAdapter<Store, StoreDTO, Long> implements StoreService {

    private final StoreEntityAdapter storeEntityAdapter;
    private final StoreMapper storeMapper;
    private final RegisterMapper registerMapper;

    StoreAdapter(StoreEntityAdapter storeEntityAdapter, StoreMapper storeMapper, RegisterMapper registerMapper) {
        super(storeEntityAdapter, storeMapper);
        this.storeEntityAdapter = storeEntityAdapter;
        this.storeMapper = storeMapper;
        this.registerMapper = registerMapper;
    }

    @Override
    public Set<StoreDTO> getAllActiveStores() {
        return storeEntityAdapter.findAllActiveStores().stream()
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<StoreDTO> getAllInactiveActiveStores() {
        return storeEntityAdapter.findAllInactiveStores().stream()
                .map(storeMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<RegisterDTO> getAllRegisters(long storeId) {
        return storeEntityAdapter.findAllRegisters(storeId).stream()
                .map(registerMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
    }
}