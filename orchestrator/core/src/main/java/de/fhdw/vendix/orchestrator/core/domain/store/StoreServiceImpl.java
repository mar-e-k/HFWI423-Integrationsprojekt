package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.spring.data.caching.RedissonKey;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
class StoreServiceImpl extends AbstractCrudService<Store, Long> implements StoreService {

    private final StoreRepository storeRepository;
    private final RedissonClient redissonClient;

    StoreServiceImpl(StoreRepository storeRepository, RedissonClient redissonClient) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.redissonClient = redissonClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllLockedStores() {
        Set<Long> lockedStoreIds = redissonClient.getSet(RedissonKey.STORE_LOCK.setKey());
        return super.findAllById(lockedStoreIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllNonLockedStores() {
        Set<Long> lockedStoreIds = redissonClient.getSet(RedissonKey.STORE_LOCK.setKey());
        return super.findAllByIdNotIn(lockedStoreIds);
    }
}