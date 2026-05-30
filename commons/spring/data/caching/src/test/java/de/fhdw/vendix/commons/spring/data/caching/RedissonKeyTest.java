package de.fhdw.vendix.commons.spring.data.caching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedissonKeyTest {

    @Test
    void buildsGlobalAndConcreteIdentifiers() {
        assertThat(RedissonKey.STORE_LOCK.toGlobalIdentifier()).isEqualTo("store-id:*");
        assertThat(RedissonKey.REGISTER_LOCK.toIdentifier("7")).isEqualTo("register-id:7");
    }
}
