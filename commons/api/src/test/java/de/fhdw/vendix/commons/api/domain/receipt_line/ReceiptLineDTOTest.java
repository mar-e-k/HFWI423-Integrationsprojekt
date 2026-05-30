package de.fhdw.vendix.commons.api.domain.receipt_line;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceiptLineDTOTest {

    @Test
    void acceptsValidLine() {
        ReceiptLineDTO dto = new ReceiptLineDTO(1L, 2L, 3L, 4L, null, null);

        assertThat(dto.receiptId()).isEqualTo(2L);
        assertThat(dto.articleAmount()).isEqualTo(4L);
    }

    @Test
    void rejectsNegativeIdAndInvalidArticleAmount() {
        assertThatThrownBy(() -> new ReceiptLineDTO(-1L, 2L, 3L, 4L, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReceiptLineDTO(1L, 2L, 3L, 0L, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestAndResponseDtosAreStillExplicitlyUnsupported() {
        assertThatThrownBy(ReceiptLineRequestDTO::new)
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(ReceiptLineResponseDTO::new)
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
