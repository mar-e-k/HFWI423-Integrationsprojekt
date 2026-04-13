package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.service.CrudService;

import java.util.List;

public interface RegisterService extends CrudService<Register, Long> {
    List<Register> findAllByStoreId(Long storeId);

    List<Register> findAllLockedRegisters();

    List<Register> findAllNonLockedRegisters();

    List<Register> findAllLockedRegistersByStoreId(Long storeId);

    List<Register> findAllNonLockedRegistersByStoreId(Long storeId);
}