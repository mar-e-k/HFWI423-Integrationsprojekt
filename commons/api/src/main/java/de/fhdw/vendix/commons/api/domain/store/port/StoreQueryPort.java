package de.fhdw.vendix.commons.api.domain.store.port;

import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Set;

public interface StoreQueryPort extends QueryPort {
    Set<StoreDTO> getAllActiveStores();

    Set<StoreDTO> getAllInactiveActiveStores();
}