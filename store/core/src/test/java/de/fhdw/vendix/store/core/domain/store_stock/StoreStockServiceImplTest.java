package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class StoreStockServiceImplTest {

    @Test
    void findByStoreIdAndArticleIdRejectsInvalidInput() {
        StoreStockRepository repository = mock(StoreStockRepository.class);
        StoreStockServiceImpl service = new StoreStockServiceImpl(repository, mock(StoreStockBulkRepository.class));

        assertThat(service.findByStoreIdAndArticleId(null, 1L)).isEmpty();
        assertThat(service.findByStoreIdAndArticleId(1L, -1L)).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void restockExistingArticleUpdatesAmount() {
        StoreStockRepository repository = mock(StoreStockRepository.class);
        StoreStock stock = new StoreStock(5L, 1L, 2L, 10L, new PreferenceAmount(1L, 5L, 10L));
        when(repository.findByStoreIdAndArticleId(1L, 2L)).thenReturn(Optional.of(stock));
        when(repository.existsById(5L)).thenReturn(true);
        when(repository.save(stock)).thenReturn(stock);

        new StoreStockServiceImpl(repository, mock(StoreStockBulkRepository.class)).restockArticle(1L, 2L, 4L);

        assertThat(stock.getCurrentAmount()).isEqualTo(14L);
        verify(repository).save(stock);
    }

    @Test
    void bulkOperationsValidateInputAndDelegate() {
        StoreStockBulkRepository bulkRepository = mock(StoreStockBulkRepository.class);
        StoreStockServiceImpl service = new StoreStockServiceImpl(mock(StoreStockRepository.class), bulkRepository);

        service.decrementArticles(1L, Map.of(2L, 3L));
        service.incrementArticles(1L, Map.of(2L, 3L));

        verify(bulkRepository).decrementArticles(1L, Map.of(2L, 3L));
        verify(bulkRepository).incrementArticles(1L, Map.of(2L, 3L));
        assertThatThrownBy(() -> service.decrementArticles(0L, Map.of(2L, 3L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.incrementArticles(1L, Map.of(2L, 0L)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
