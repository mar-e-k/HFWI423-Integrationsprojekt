package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.store.core.domain.register.Register;

import java.util.Set;

public interface StoreService extends CrudService<Store, Long> {
    Set<Store> findAllActiveStores();

    Set<Store> findAllInactiveActiveStores();

    Set<Register> findAllRegisters(Long storeId);
}