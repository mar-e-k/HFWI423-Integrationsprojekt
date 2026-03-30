package de.fhdw.vendix.store.core.persistance.store.port;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.service.QueryService;

import java.util.Set;

interface StoreQueryService extends QueryService {
    Set<StoreDTO> getAllActiveStores();

    Set<StoreDTO> getAllInactiveActiveStores();
}