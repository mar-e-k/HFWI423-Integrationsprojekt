package de.fhdw.vendix.pos.ui.register;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartLineTest {

    @Test
    void rejectsArticleMismatch() {
        assertThatThrownBy(() -> new CartLine(new ReceiptLineDTO(1L, 1L, 2L, 1L, null, null), article(1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculatesPriceWithDiscountAndOverride() {
        CartLine cartLine = new CartLine(
                new ReceiptLineDTO(1L, 1L, 1L, 2L,
                        new DiscountOverrideDTO(BigDecimal.TEN, OverrideReason.PROMOTIONAL_ADJUSTMENT),
                        new PriceOverrideDTO(BigDecimal.valueOf(8), OverrideReason.SYSTEM_CORRECTION)),
                article(1L)
        );

        var result = cartLine.calculatePrice();

        assertThat(result.originalTotal()).isEqualByComparingTo("20");
        assertThat(result.finalTotal()).isEqualByComparingTo("14.4");
        assertThat(result.discounted()).isTrue();
        assertThat(result.overridden()).isTrue();
    }

    @Test
    void toReceiptLineKeepsArticleAndAmount() {
        CartLine cartLine = new CartLine(new ReceiptLineDTO(5L, 6L, 1L, 3L, null, null), article(1L));

        ReceiptLineDTO receiptLine = CartLine.toReceiptLine(cartLine);

        assertThat(receiptLine.articleId()).isEqualTo(1L);
        assertThat(receiptLine.articleAmount()).isEqualTo(3L);
        assertThat(receiptLine.id()).isZero();
        assertThat(receiptLine.receiptId()).isZero();
    }

    private static ArticleDTO article(Long id) {
        return new ArticleDTO(id, "12345678", "Milk", "Fresh milk", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false);
    }
}
