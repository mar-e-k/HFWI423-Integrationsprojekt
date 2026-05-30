package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptMapperTest {

    private static final UUID CASHIER = UUID.fromString("99999999-9999-4999-8999-999999999999");
    private final ReceiptMapper mapper = new ReceiptMapperImpl();

    @Test
    void mapsEntityToDto() {
        ReceiptDTO dto = mapper.toDTO(new Receipt(1L, 2L, 3L, CASHIER, PaymentMethod.CASH, ReceiptStatus.OPEN));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.cashierUuid()).isEqualTo(CASHIER);
    }

    @Test
    void mapsDtoToEntityAndLists() {
        Receipt entity = mapper.toEntity(new ReceiptDTO(1L, 2L, 3L, CASHIER, PaymentMethod.CARD, ReceiptStatus.PRINTED));

        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(mapper.toEntities(List.of(new ReceiptDTO(2L, 2L, 3L, CASHIER, PaymentMethod.CASH, ReceiptStatus.OPEN))))
                .hasSize(1);
    }
}
