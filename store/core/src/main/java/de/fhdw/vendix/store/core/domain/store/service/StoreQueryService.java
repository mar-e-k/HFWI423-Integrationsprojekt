package de.fhdw.vendix.store.core.domain.store.service;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Set;

interface StoreQueryService extends CrudQueryService<StoreDTO, Long> {
    Set<StoreDTO> getAllActiveStores();

    Set<StoreDTO> getAllInactiveActiveStores();

    Set<RegisterDTO> getAllRegisters(long storeId);
}