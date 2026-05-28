package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;

import java.util.List;
import java.util.Optional;

public interface RegisterService extends CrudService<Register, Long> {
    Optional<Register> findById(Long id);

    List<Register> findAllByStoreId(Long storeId);

    List<Register> findAllLockedRegisters();

    List<Register> findAllNonLockedRegisters();

    List<Register> findAllLockedRegistersByStoreId(Long storeId);

    List<Register> findAllNonLockedRegistersByStoreId(Long storeId);
}