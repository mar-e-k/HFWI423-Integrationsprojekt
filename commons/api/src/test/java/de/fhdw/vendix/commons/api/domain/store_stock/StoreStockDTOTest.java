package de.fhdw.vendix.commons.api.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.embeddable.PreferenceAmountDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NullAway")
class StoreStockDTOTest {

    @Test
    void acceptsValidStock() {
        StoreStockDTO dto = new StoreStockDTO(1L, 2L, article(), new PreferenceAmountDTO(1L, 5L, 10L), 4L);

        assertThat(dto.currentAmount()).isEqualTo(4L);
    }

    @Test
    void rejectsMissingArticleOrPreferenceAmount() {
        assertThatThrownBy(() -> new StoreStockDTO(1L, 2L, null, new PreferenceAmountDTO(1L, 5L, 10L), 4L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreStockDTO(1L, 2L, article(), null, 4L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static ArticleDTO article() {
        return new ArticleDTO(1L, "12345678", "Milk", "Fresh milk", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false);
    }
}
