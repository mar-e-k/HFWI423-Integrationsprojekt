package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.spring.data.caching.RedissonLockKey;
import de.fhdw.vendix.commons.spring.data.caching.RedissonLockUtils;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
class StoreServiceImpl extends AbstractCrudService<Store, Long> implements StoreService {

    private final StoreRepository storeRepository;
    private final RedissonLockUtils redissonLockUtils;

    StoreServiceImpl(StoreRepository storeRepository, RedissonLockUtils redissonLockUtils) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.redissonLockUtils = redissonLockUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllLockedStores() {
        Set<Long> lockedStoreIds = redissonLockUtils.getActiveLockedIds(RedissonLockKey.STORE_LOCK);
        return super.findAllById(lockedStoreIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllNonLockedStores() {
        Set<Long> lockedStoreIds = redissonLockUtils.getActiveLockedIds(RedissonLockKey.STORE_LOCK);
        return super.findAllByIdNotIn(lockedStoreIds);
    }
}