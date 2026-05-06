package de.fhdw.vendix.orchestrator.core.domain.register;

import de.fhdw.vendix.commons.spring.data.caching.RedissonKey;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
class RegisterServiceImpl extends AbstractCrudService<Register, Long> implements RegisterService {

    private final RegisterRepository registerRepository;
    private final RedissonClient redissonClient;

    RegisterServiceImpl(RegisterRepository registerRepository, RedissonClient redissonClient) {
        super(registerRepository);
        this.registerRepository = registerRepository;
        this.redissonClient = redissonClient;
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
        Set<Long> lockedRegisterIds = redissonClient.getSet(RedissonKey.REGISTER_LOCK.setKey());
        return super.findAllById(lockedRegisterIds);
    }

    @Override
    public List<Register> findAllNonLockedRegisters() {
        Set<Long> nonLockedRegisterIds = redissonClient.getSet(RedissonKey.REGISTER_LOCK.setKey());
        return super.findAllByIdNotIn(nonLockedRegisterIds);
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