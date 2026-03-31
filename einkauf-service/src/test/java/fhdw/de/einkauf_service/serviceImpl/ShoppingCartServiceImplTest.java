package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartSession cartSession;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ShoppingCartServiceImpl service;

    private Article buildArticle(Long id) {
        Article a = new Article();
        a.setId(id);
        a.setName("Testartikel");
        return a;
    }

    // --- validateAndAddToCart ---

    @Test
    void validateAndAddToCart_validQuantityAndExistingArticle_callsAddItem() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(buildArticle(1L)));

        service.validateAndAddToCart(1L, 3);

        verify(cartSession).addItem(1L, 3);
    }

    @Test
    void validateAndAddToCart_articleNotFound_throwsEntityNotFoundException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateAndAddToCart(99L, 2))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validateAndAddToCart_quantityZero_throwsIllegalArgumentException() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(buildArticle(1L)));

        assertThatThrownBy(() -> service.validateAndAddToCart(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartSession, never()).addItem(anyLong(), anyInt());
    }

    @Test
    void validateAndAddToCart_quantityNegative_throwsIllegalArgumentException() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(buildArticle(1L)));

        assertThatThrownBy(() -> service.validateAndAddToCart(1L, -5))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cartSession, never()).addItem(anyLong(), anyInt());
    }

    // --- removeItem ---

    @Test
    void removeItem_delegatesToCartSession() {
        service.removeItem(1L);

        verify(cartSession).removeItem(1L);
    }

    // --- getCurrentCartItems ---

    @Test
    void getCurrentCartItems_returnsMapFromSession() {
        Map<Long, Integer> expected = Map.of(1L, 2, 3L, 5);
        when(cartSession.getItems()).thenReturn(expected);

        Map<Long, Integer> result = service.getCurrentCartItems();

        assertThat(result).isEqualTo(expected);
    }

    // --- clearCart ---

    @Test
    void clearCart_delegatesToCartSession() {
        service.clearCart();

        verify(cartSession).clearCart();
    }
}
