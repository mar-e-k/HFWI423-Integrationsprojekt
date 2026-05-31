package de.fhdw.vendix.commons.api.domain.article;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleDTOTest {

    @Test
    void acceptsValidArticle() {
        ArticleDTO dto = valid();

        assertThat(dto.gtin()).isEqualTo("12345678");
        assertThat(dto.sellingPrice()).isEqualByComparingTo("1.99");
    }

    @Test
    void rejectsNegativeId() {
        assertThatThrownBy(() -> new ArticleDTO(-1L, "12345678", "Milk", "Fresh", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidGtin() {
        assertThatThrownBy(() -> new ArticleDTO(1L, "ABC", "Milk", "Fresh", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativePricesAndTaxOutOfRange() {
        assertThatThrownBy(() -> new ArticleDTO(1L, "12345678", "Milk", "Fresh", "ACME", "Supplier", "pcs",
                BigDecimal.valueOf(-1), BigDecimal.TEN, BigDecimal.valueOf(19), 10L, true, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ArticleDTO(1L, "12345678", "Milk", "Fresh", "ACME", "Supplier", "pcs",
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(101), 10L, true, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static ArticleDTO valid() {
        return new ArticleDTO(1L, "12345678", "Milk", "Fresh milk", "ACME", "Supplier", "pcs",
                BigDecimal.valueOf(1.00), BigDecimal.valueOf(1.99), BigDecimal.valueOf(19), 10L, true, false);
    }
}
