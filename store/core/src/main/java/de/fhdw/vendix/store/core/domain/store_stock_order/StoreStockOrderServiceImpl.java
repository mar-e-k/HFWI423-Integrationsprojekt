package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class StoreStockOrderServiceImpl extends AbstractCrudService<StoreStockOrder, Long> implements StoreStockOrderService {

    private static final Logger log = LoggerFactory.getLogger(StoreStockOrderServiceImpl.class);

    private final StoreStockOrderRepository storeStockOrderRepository;

    StoreStockOrderServiceImpl(StoreStockOrderRepository storeStockOrderRepository) {
        super(storeStockOrderRepository);
        this.storeStockOrderRepository = storeStockOrderRepository;
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
            if (Boolean.TRUE.equals(request.urgent())) {
//                articleOrderMessagingService.sendUrgentOrder(request.storeId(), request.articleId(), request.amount());
            } else {
//                articleOrderMessagingService.sendOrder(request.storeId(), request.articleId(), request.amount());
            }

            order.mark(OrderStatus.ORDERED, "Order published to AMQP");
            storeStockOrderRepository.save(order);
            return new StoreStockOrderResponseDTO(
                    correlationId,
                    OrderStatus.ORDERED,
                    "/api/replenishment-orders/" + correlationId
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
//        storeStockOrderRepository
//                .findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(storeId, articleId, OPEN_STATUSES)
//                .ifPresent(order -> {
//                    log.atDebug().log("Marking replenishment order {} as received", order.getCorrelationId());
//                    order.mark(
//                            OrderStatus.RECEIVED,
//                            "Received " + amount + " units from logistics event"
//                    );
//                });
    }

    private StoreStockOrderDTO toStatusDTO(StoreStockOrder order) {
        Instant persistedCreatedAt = order.getCreatedAt();
        Instant createdAt = persistedCreatedAt == null ? Instant.EPOCH : persistedCreatedAt;
        Instant persistedChangedAt = order.getChangedAt();
        Instant updatedAt = persistedChangedAt == null ? createdAt : persistedChangedAt;

        return new StoreStockOrderDTO(
                order.getId(),
                order.getCorrelationId(),
                order.getStoreId(),
                order.getArticleId(),
                order.getAmount(),
                order.getUrgent(),
                order.getStatus(),
                order.getMessage(),
                createdAt,
                updatedAt
        );
    }
}
