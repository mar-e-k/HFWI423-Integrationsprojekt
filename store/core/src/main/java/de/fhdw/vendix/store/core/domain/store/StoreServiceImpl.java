package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.commons.spring.web.client.orchestrator.api.LockProxyService;
import de.fhdw.vendix.store.core.domain.register.Register;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class StoreServiceImpl extends AbstractEntityCrudAdapter<Store, Long> implements StoreService {

    private final StoreRepository storeRepository;
    private final LockProxyService lockProxyService;

    StoreServiceImpl(StoreRepository storeRepository, LockProxyService lockProxyService) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.lockProxyService = lockProxyService;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Store> findAllActiveStores() {
        Set<LockDTO> storeLocks = Objects.requireNonNull(lockProxyService
                .getLocks(null, TargetType.STORE, null)
                .getBody());
        Set<Long> storeLockIds = storeLocks.stream()
                .map(LockDTO::target)
                .map(EntityTargetDTO::id)
                .collect(Collectors.toUnmodifiableSet());
        return findAllById(storeLockIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Store> findAllInactiveActiveStores() {
        Set<LockDTO> storeLocks = Objects.requireNonNull(lockProxyService
                .getLocks(null, TargetType.STORE, null)
                .getBody());
        Set<Long> storeLockIds = storeLocks.stream()
                .map(LockDTO::target)
                .map(EntityTargetDTO::id)
                .collect(Collectors.toUnmodifiableSet());
        return findAllByIdNotIn(storeLockIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Register> findAllRegisters(Long storeId) {
        if (storeId == null || storeId < 0) {
            return Set.of();
        }
        return storeRepository.findAllRegisters(storeId);
    }
}