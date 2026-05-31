package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherMapperTest {

    private final VoucherMapper mapper = new VoucherMapperImpl();

    @Test
    void mapsEntityToDto() {
        UUID code = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");

        VoucherDTO dto = mapper.toDTO(new Voucher(1L, code, expiresAt, null));

        assertThat(dto.code()).isEqualTo(code);
        assertThat(dto.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        UUID code = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
        Voucher entity = mapper.toEntity(new VoucherDTO(2L, code, null, null));

        assertThat(entity.getCode()).isEqualTo(code);
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
