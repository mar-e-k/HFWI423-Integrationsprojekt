package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherDTOMapperTest {

    private final VoucherDTOMapper mapper = new VoucherDTOMapperImpl();

    @Test
    void mapsRequestToDomainDto() {
        UUID code = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");

        VoucherDTO dto = mapper.toDomainDTO(new VoucherRequestDTO(code, expiresAt));

        assertThat(dto.code()).isEqualTo(code);
        assertThat(dto.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void mapsDomainDtoToResponseDto() {
        UUID code = UUID.fromString("12121212-1212-4212-8212-121212121212");

        VoucherResponseDTO response = mapper.toResponseDTO(new VoucherDTO(3L, code, null, null));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.code()).isEqualTo(code);
    }
}
