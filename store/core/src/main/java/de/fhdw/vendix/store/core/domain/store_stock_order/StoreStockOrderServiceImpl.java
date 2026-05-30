package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class StoreStockOrderServiceImpl extends AbstractCrudService<StoreStockOrder, Long> implements StoreStockOrderService {

    private static final Logger log = LoggerFactory.getLogger(StoreStockOrderServiceImpl.class);
    private static final Set<OrderStatus> OPEN_STATUSES = EnumSet.of(OrderStatus.PENDING, OrderStatus.ORDERED);

    private final StoreStockOrderRepository storeStockOrderRepository;
    private final ObjectProvider<ArticleOrderPublisher> articleOrderPublisherProvider;

    StoreStockOrderServiceImpl(
            StoreStockOrderRepository storeStockOrderRepository,
            ObjectProvider<ArticleOrderPublisher> articleOrderPublisherProvider
    ) {
        super(storeStockOrderRepository);
        this.storeStockOrderRepository = storeStockOrderRepository;
        this.articleOrderPublisherProvider = articleOrderPublisherProvider;
    }

    @Override
    public StoreStockOrderResponseDTO requestReplenishment(StoreStockOrderRequestDTO request) {
        UUID correlationId = UUID.randomUUID();
        StoreStockOrder storeStockOrder = new StoreStockOrder(
                correlationId,
                request.storeId(),
                request.articleId(),
                request.amount(),
                request.urgent(),
                OrderStatus.PENDING
        );
        StoreStockOrder order = super.create(storeStockOrder);

        try {
            @Nullable ArticleOrderPublisher articleOrderPublisher = articleOrderPublisherProvider.getIfAvailable();
            if (articleOrderPublisher == null) {
                throw new IllegalStateException("No ArticleOrderPublisher configured");
            }
            articleOrderPublisher.publishOrder(
                    request.storeId(),
                    request.articleId(),
                    request.amount(),
                    Boolean.TRUE.equals(request.urgent())
            );

            order.mark(OrderStatus.ORDERED, "Order published to AMQP");
            storeStockOrderRepository.save(order);
            return new StoreStockOrderResponseDTO(
                    correlationId,
                    OrderStatus.ORDERED,
                    "/api/store-stock-order/correlation-id/" + correlationId
            );
        } catch (RuntimeException e) {
            order.mark(OrderStatus.FAILED, e.getMessage());
            storeStockOrderRepository.save(order);
            throw e;
        }
    }

    @Override
    public Optional<StoreStockOrderDTO> findStatus(UUID correlationId) {
        if (correlationId == null) {
            return Optional.empty();
        }
        return storeStockOrderRepository.findByCorrelationId(correlationId)
                .map(this::toStatusDTO);
    }

    @Override
    @Transactional
    public void markReceived(long storeId, long articleId, long amount) {
        storeStockOrderRepository
                .findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(storeId, articleId, OPEN_STATUSES)
                .ifPresentOrElse(order -> {
                    log.atDebug().log("Marking replenishment order {} as received", order.getCorrelationId());
                    order.mark(
                            OrderStatus.RECEIVED,
                            "Received " + amount + " units from logistics event"
                    );
                    storeStockOrderRepository.save(order);
                }, () -> log.atDebug().log(
                        "No open replenishment order found for storeId={} articleId={}",
                        storeId,
                        articleId
                ));
    }

    private StoreStockOrderDTO toStatusDTO(StoreStockOrder order) {
        return new StoreStockOrderDTO(
                order.getId(),
                order.getCorrelationId(),
                order.getStoreId(),
                order.getArticleId(),
                order.getAmount(),
                order.getUrgent(),
                order.getStatus(),
                order.getMessage()
        );
    }
}
