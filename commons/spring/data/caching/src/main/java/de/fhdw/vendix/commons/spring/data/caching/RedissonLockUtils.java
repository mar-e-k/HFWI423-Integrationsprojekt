package de.fhdw.vendix.commons.spring.data.caching;

import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public final class RedissonLockUtils {

    private static final Logger log = LoggerFactory.getLogger(RedissonLockUtils.class);

    private final RedissonClient redissonClient;

    public RedissonLockUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public Set<Long> getActiveLockedIds(RedissonLockKey keyType) {
        Set<Long> lockedIds = new HashSet<>();
        RKeys keys = redissonClient.getKeys();

        KeysScanOptions options = KeysScanOptions.defaults()
                .pattern(keyType.toGlobalIdentifier());

        Iterable<String> activeLockKeys = keys.getKeys(options);

        for (String key : activeLockKeys) {
            try {
                String idPart = key.substring(key.lastIndexOf(":") + 1);
                lockedIds.add(Long.parseLong(idPart));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                log.atWarn().log("Unexpected key structure found in Redis cluster for pattern [{}]: {}",
                        keyType.toGlobalIdentifier(), key, e);
            }
        }

        return lockedIds;
    }
}