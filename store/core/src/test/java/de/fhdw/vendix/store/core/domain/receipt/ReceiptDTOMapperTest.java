package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptDTOMapperTest {

    private static final UUID CASHIER = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
    private final ReceiptDTOMapper mapper = new ReceiptDTOMapperImpl();

    @Test
    void mapsRequestToDomainDto() {
        ReceiptDTO dto = mapper.toDomainDTO(new ReceiptRequestDTO(1L, 2L, CASHIER, PaymentMethod.CASH,
                ReceiptStatus.OPEN, List.of(), List.of()));

        assertThat(dto.id()).isNull();
        assertThat(dto.storeId()).isEqualTo(1L);
    }

    @Test
    void mapsDomainDtoToResponseDto() {
        ReceiptResponseDTO response = mapper.toResponseDTO(new ReceiptDTO(7L, 1L, 2L, CASHIER,
                PaymentMethod.CARD, ReceiptStatus.PRINTED));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.status()).isEqualTo(ReceiptStatus.PRINTED);
    }
}
