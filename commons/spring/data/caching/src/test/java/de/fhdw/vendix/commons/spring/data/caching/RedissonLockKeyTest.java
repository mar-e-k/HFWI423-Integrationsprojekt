package de.fhdw.vendix.commons.spring.data.caching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedissonLockKeyTest {

    @Test
    void buildsGlobalAndConcreteIdentifiers() {
        assertThat(RedissonLockKey.STORE_LOCK.toGlobalIdentifier()).isEqualTo("store-id:*");
        assertThat(RedissonLockKey.REGISTER_LOCK.toIdentifier("7")).isEqualTo("register-id:7");
    }
}
