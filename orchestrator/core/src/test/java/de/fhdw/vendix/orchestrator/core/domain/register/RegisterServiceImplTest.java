package de.fhdw.vendix.orchestrator.core.domain.register;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class RegisterServiceImplTest {

    @Test
    void findAllByStoreIdRejectsInvalidStoreId() {
        RegisterRepository repository = mock(RegisterRepository.class);

        assertThat(new RegisterServiceImpl(repository, lockUtils()).findAllByStoreId(0L)).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void findAllByStoreIdDelegatesValidStoreId() {
        RegisterRepository repository = mock(RegisterRepository.class);
        List<Register> registers = List.of(new Register(1L, 7L));
        when(repository.findAllByStoreId(7L)).thenReturn(registers);

        assertThat(new RegisterServiceImpl(repository, lockUtils()).findAllByStoreId(7L))
                .isSameAs(registers);
    }

    @Test
    void findLockedAndNonLockedRegistersUseRedissonLockIds() {
        RegisterRepository repository = mock(RegisterRepository.class);
        when(repository.findAllById(Set.of(1L))).thenReturn(List.of(new Register(1L, 7L)));
        when(repository.findAll()).thenReturn(List.of(new Register(1L, 7L), new Register(2L, 8L)));

        RegisterServiceImpl service = new RegisterServiceImpl(repository, lockUtils("register-id:1"));

        assertThat(service.findAllLockedRegisters()).hasSize(1);
        assertThat(service.findAllNonLockedRegisters()).extracting(Register::getStoreId).containsExactly(8L);
        verify(repository).findAllById(Set.of(1L));
    }

    @Test
    void filtersLockedRegistersByStoreId() {
        RegisterRepository repository = mock(RegisterRepository.class);
        when(repository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(new Register(1L, 7L), new Register(2L, 8L)));

        assertThat(new RegisterServiceImpl(repository, lockUtils("register-id:1", "register-id:2"))
                .findAllLockedRegistersByStoreId(7L))
                .extracting(Register::getStoreId)
                .containsExactly(7L);
    }

    private static RedissonLockUtils lockUtils(String... activeKeys) {
        RedissonClient redissonClient = mock(RedissonClient.class);
        RKeys keys = mock(RKeys.class);
        when(redissonClient.getKeys()).thenReturn(keys);
        when(keys.getKeys(any(KeysScanOptions.class))).thenReturn(List.of(activeKeys));
        return new RedissonLockUtils(redissonClient);
    }
}
