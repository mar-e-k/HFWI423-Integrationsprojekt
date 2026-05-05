package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RegisterServiceImpl extends AbstractCrudService<Register, Long> implements RegisterService {

    private final RegisterRepository registerRepository;

    RegisterServiceImpl(RegisterRepository registerRepository) {
        super(registerRepository);
        this.registerRepository = registerRepository;
    }

    @Override
    public List<Register> findAllByStoreId(Long storeId) {
        if (storeId == null || storeId < 1L) {
            return List.of();
        }
        return registerRepository.findAllByStoreId(storeId);
    }

    @Override
    public List<Register> findAllLockedRegisters() {
        return registerRepository.findAllLockedRegisters();
    }

    @Override
    public List<Register> findAllNonLockedRegisters() {
        return registerRepository.findAllNonLockedRegisters();
    }

    @Override
    public List<Register> findAllLockedRegistersByStoreId(Long storeId) {
        if (storeId == null || storeId < 1L) {
            return List.of();
        }
        return registerRepository.findAllLockedRegistersByStoreId(storeId);
    }

    @Override
    public List<Register> findAllNonLockedRegistersByStoreId(Long storeId) {
        if (storeId == null || storeId < 1L) {
            return List.of();
        }
        return registerRepository.findAllNonLockedRegistersByStoreId(storeId);
    }
}