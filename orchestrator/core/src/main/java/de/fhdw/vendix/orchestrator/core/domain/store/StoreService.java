package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.api.structure.service.CrudService;

import java.util.List;

public interface StoreService extends CrudService<Store, Long> {
    List<Store> findAllLockedStores();

    List<Store> findAllNonLockedStores();
}