package de.fhdw.vendix.commons.api.domain;

import de.fhdw.vendix.commons.api.domain.article.ArticleRequestDTO;
import de.fhdw.vendix.commons.api.domain.article.ArticleResponseDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterRequestDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterResponseDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnsupportedRequestResponseDTOTest {

    @Test
    void unimplementedDtosFailExplicitly() {
        assertThatThrownBy(ArticleRequestDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(ArticleResponseDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(RegisterRequestDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(RegisterResponseDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(StoreRequestDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(StoreResponseDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(StoreStockRequestDTO::new).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(StoreStockResponseDTO::new).isInstanceOf(UnsupportedOperationException.class);
    }
}
