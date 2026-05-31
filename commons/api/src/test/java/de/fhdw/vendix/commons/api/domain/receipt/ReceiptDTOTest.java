package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class ReceiptDTOTest {

    private static final UUID CASHIER = UUID.fromString("11111111-1111-4111-8111-111111111111");

    @Test
    void receiptDtoAcceptsValidValues() {
        ReceiptDTO dto = new ReceiptDTO(1L, 2L, 3L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN);

        assertThat(dto.storeId()).isEqualTo(2L);
        assertThat(dto.status()).isEqualTo(ReceiptStatus.OPEN);
    }

    @Test
    void receiptDtoRejectsInvalidValues() {
        assertThatThrownBy(() -> new ReceiptDTO(-1L, 2L, 3L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReceiptDTO(1L, -2L, 3L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReceiptDTO(1L, 2L, 3L, null, PaymentMethod.CARD, ReceiptStatus.OPEN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void receiptRequestRejectsMissingRequiredCollections() {
        assertThatThrownBy(() -> new ReceiptRequestDTO(1L, 2L, CASHIER, PaymentMethod.CASH, ReceiptStatus.OPEN, null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReceiptRequestDTO(1L, 2L, CASHIER, PaymentMethod.CASH, ReceiptStatus.OPEN,
                List.of(new ReceiptLineDTO(1L, 1L, 1L, 1L, null, null)), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void receiptResponseRejectsMissingId() {
        assertThatThrownBy(() -> new ReceiptResponseDTO(null, 1L, 2L, CASHIER, PaymentMethod.CARD, ReceiptStatus.OPEN))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
