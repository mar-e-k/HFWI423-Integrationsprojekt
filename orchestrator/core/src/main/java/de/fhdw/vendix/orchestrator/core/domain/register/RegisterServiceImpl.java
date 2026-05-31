package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.caching.RedissonLockKey;
import de.fhdw.vendix.commons.spring.data.caching.RedissonLockUtils;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
class RegisterServiceImpl extends AbstractCrudService<Register, Long> implements RegisterService {

    private final RegisterRepository registerRepository;
    private final RedissonLockUtils redissonLockUtils;

    RegisterServiceImpl(RegisterRepository registerRepository, RedissonLockUtils redissonLockUtils) {
        super(registerRepository);
        this.registerRepository = registerRepository;
        this.redissonLockUtils = redissonLockUtils;
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
        Set<Long> lockedRegisterIds = redissonLockUtils.getActiveLockedIds(RedissonLockKey.REGISTER_LOCK);
        return super.findAllById(lockedRegisterIds);
    }

    @Override
    public List<Register> findAllNonLockedRegisters() {
        Set<Long> lockedRegisterIds = redissonLockUtils.getActiveLockedIds(RedissonLockKey.REGISTER_LOCK);
        return super.findAllByIdNotIn(lockedRegisterIds);
    }

    @Override
    public List<Register> findAllLockedRegistersByStoreId(Long storeId) {
        if (storeId == null || storeId < 1L) {
            return List.of();
        }
        return findAllLockedRegisters().stream()
                .filter(register ->  Objects.equals(register.getStoreId(), storeId))
                .toList();
    }

    @Override
    public List<Register> findAllNonLockedRegistersByStoreId(Long storeId) {
        if (storeId == null || storeId < 1L) {
            return List.of();
        }
        return findAllNonLockedRegisters().stream()
                .filter(register ->  Objects.equals(register.getStoreId(), storeId))
                .toList();
    }
}