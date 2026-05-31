package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.spring.data.caching.RedissonLockUtils;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class StoreServiceImplTest {

    @Test
    void findAllLockedStoresUsesActiveLockIds() {
        StoreRepository repository = mock(StoreRepository.class);
        Store store = new Store(1L, "DE", "Hamburg", "Main", "1");
        when(repository.findAllById(Set.of(1L))).thenReturn(List.of(store));

        assertThat(new StoreServiceImpl(repository, lockUtils("store-id:1")).findAllLockedStores()).containsExactly(store);
        verify(repository).findAllById(Set.of(1L));
    }

    @Test
    void findAllNonLockedStoresFiltersRepositoryResults() {
        StoreRepository repository = mock(StoreRepository.class);
        Store locked = new Store(1L, "DE", "Hamburg", "Main", "1");
        Store open = new Store(2L, "DE", "Berlin", "Side", "2");
        when(repository.findAll()).thenReturn(List.of(locked, open));

        assertThat(new StoreServiceImpl(repository, lockUtils("store-id:1")).findAllNonLockedStores()).containsExactly(open);
    }

    private static RedissonLockUtils lockUtils(String... activeKeys) {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RKeys keys = mock(RKeys.class);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(keys.getKeys(any(KeysScanOptions.class))).thenReturn(List.of(activeKeys));
        return new RedissonLockUtils(redissonClient);
    }
}
