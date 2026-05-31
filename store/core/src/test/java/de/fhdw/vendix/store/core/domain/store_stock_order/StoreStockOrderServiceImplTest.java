package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class StoreStockOrderServiceImplTest {

    @Test
    void requestReplenishmentPersistsOrderAndPublishesEvent() {
        StoreStockOrderRepository repository = mock(StoreStockOrderRepository.class);
        ArticleOrderPublisher publisher = mock(ArticleOrderPublisher.class);
        ObjectProvider<ArticleOrderPublisher> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(publisher);
        when(repository.save(any(StoreStockOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreStockOrderResponseDTO response = new StoreStockOrderServiceImpl(repository, provider)
                .requestReplenishment(new StoreStockOrderRequestDTO(1L, 2L, 3L, true));

        assertThat(response.status()).isEqualTo(OrderStatus.ORDERED);
        assertThat(response.statusUrl()).contains("/api/store-stock-order/correlation-id/");
        verify(publisher).publishOrder(1L, 2L, 3L, true);
    }

    @Test
    void requestReplenishmentMarksOrderFailedWhenPublisherIsMissing() {
        StoreStockOrderRepository repository = mock(StoreStockOrderRepository.class);
        ObjectProvider<ArticleOrderPublisher> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        when(repository.save(any(StoreStockOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> new StoreStockOrderServiceImpl(repository, provider)
                .requestReplenishment(new StoreStockOrderRequestDTO(1L, 2L, 3L, false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markReceivedUpdatesOldestOpenOrder() {
        StoreStockOrderRepository repository = mock(StoreStockOrderRepository.class);
        ObjectProvider<ArticleOrderPublisher> provider = mock(ObjectProvider.class);
        StoreStockOrder order = new StoreStockOrder(null, java.util.UUID.randomUUID(), 1L, 2L, 3L, false,
                OrderStatus.ORDERED, null);
        when(repository.findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(any(), any(), any()))
                .thenReturn(Optional.of(order));

        new StoreStockOrderServiceImpl(repository, provider).markReceived(1L, 2L, 3L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        verify(repository).save(order);
    }
}
