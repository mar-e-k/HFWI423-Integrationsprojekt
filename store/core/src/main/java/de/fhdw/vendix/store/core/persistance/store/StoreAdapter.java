package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class StoreAdapter extends AbstractDtoCrudAdapter<Store, StoreDTO, Long> {

    private final StoreEntityAdapter storeEntityAdapter;
    private final StoreMapper storeMapper;

    StoreAdapter(StoreEntityAdapter storeEntityAdapter, StoreMapper storeMapper) {
        super(storeEntityAdapter, storeMapper);
        this.storeEntityAdapter = storeEntityAdapter;
        this.storeMapper = storeMapper;
    }
}