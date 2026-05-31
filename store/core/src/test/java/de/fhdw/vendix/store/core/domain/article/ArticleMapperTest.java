package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleMapperTest {

    private final ArticleMapper mapper = new ArticleMapperImpl();

    @Test
    void mapsEntityToDtoWithRenamedFields() {
        ArticleDTO dto = mapper.toDTO(new Article(1L, "12345678", "Fresh", "ACME", "Milk", 1.0, 1.99,
                10, "Supplier", 19.0, "pcs", true, false));

        assertThat(dto.gtin()).isEqualTo("12345678");
        assertThat(dto.stock()).isEqualTo(10L);
        assertThat(dto.taxRate()).isEqualByComparingTo("19.0");
    }

    @Test
    void mapsDtoToEntityAndLists() {
        Article entity = mapper.toEntity(new ArticleDTO(2L, "1234567890123", "Water", "Still", "ACME",
                "Supplier", "bottle", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(7), 5L, true, true));

        assertThat(entity.getArticleNumber()).isEqualTo("1234567890123");
        assertThat(entity.isHasDeposit()).isTrue();
        assertThat(mapper.toDTOs(List.of(entity))).hasSize(1);
    }
}
