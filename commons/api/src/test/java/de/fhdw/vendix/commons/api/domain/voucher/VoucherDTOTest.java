package de.fhdw.vendix.commons.api.domain.voucher;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class VoucherDTOTest {

    private static final UUID CODE = UUID.fromString("33333333-3333-4333-8333-333333333333");

    @Test
    void acceptsValidVoucherDtos() {
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");

        assertThat(new VoucherDTO(1L, CODE, expiresAt, null).code()).isEqualTo(CODE);
        assertThat(new VoucherRequestDTO(CODE, expiresAt).expiresAt()).isEqualTo(expiresAt);
        assertThat(new VoucherResponseDTO(1L, CODE, expiresAt, null).id()).isEqualTo(1L);
    }

    @Test
    void rejectsMissingCodeOrInvalidResponseId() {
        assertThatThrownBy(() -> new VoucherDTO(1L, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VoucherRequestDTO(null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new VoucherResponseDTO(null, CODE, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
