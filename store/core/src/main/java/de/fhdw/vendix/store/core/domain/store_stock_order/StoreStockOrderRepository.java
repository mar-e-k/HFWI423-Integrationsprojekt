package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

interface StoreStockOrderRepository extends JpaRepository<StoreStockOrder, Long> {

    Optional<StoreStockOrder> findByCorrelationId(UUID correlationId);

    Optional<StoreStockOrder> findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(
            Long storeId,
            Long articleId,
            Collection<OrderStatus> statuses
    );
}
