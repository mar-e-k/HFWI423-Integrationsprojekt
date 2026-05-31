package de.fhdw.vendix.commons.spring.data.caching;

import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class RedissonLockUtilsTest {

    @Test
    void extractsNumericIdsAndIgnoresMalformedKeys() {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RKeys keys = mock(RKeys.class);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(keys.getKeys(any(KeysScanOptions.class))).thenReturn(List.of("store-id:1", "store-id:abc", "store-id:2"));

        Set<Long> lockedIds = new RedissonLockUtils(redissonClient).getActiveLockedIds(RedissonKey.STORE_LOCK);

        assertThat(lockedIds).containsExactlyInAnyOrder(1L, 2L);
    }
}
