package de.fhdw.vendix.store.core.domain.replenishment;

import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderResponseDTO;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatus;
import de.fhdw.vendix.commons.api.domain.replenishment.ReplenishmentOrderStatusDTO;
import de.fhdw.vendix.store.core.messaging.ArticleOrderMessagingService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class ReplenishmentOrderServiceImpl implements ReplenishmentOrderService {

    private static final Logger log = LoggerFactory.getLogger(ReplenishmentOrderServiceImpl.class);
    private static final List<ReplenishmentOrderStatus> OPEN_STATUSES = List.of(
            ReplenishmentOrderStatus.PENDING,
            ReplenishmentOrderStatus.ORDERED
    );

    private final ArticleOrderMessagingService articleOrderMessagingService;
    private final ReplenishmentOrderRepository replenishmentOrderRepository;

    ReplenishmentOrderServiceImpl(
            ArticleOrderMessagingService articleOrderMessagingService,
            ReplenishmentOrderRepository replenishmentOrderRepository
    ) {
        this.articleOrderMessagingService = articleOrderMessagingService;
        this.replenishmentOrderRepository = replenishmentOrderRepository;
    }

    @Override
    public ReplenishmentOrderResponseDTO requestReplenishment(ReplenishmentOrderRequestDTO request) {
        UUID correlationId = UUID.randomUUID();
        ReplenishmentOrder order = replenishmentOrderRepository.save(new ReplenishmentOrder(
                correlationId,
                request.storeId(),
                request.articleId(),
                request.amount(),
                request.urgent()
        ));

        try {
            if (Boolean.TRUE.equals(request.urgent())) {
                articleOrderMessagingService.sendUrgentOrder(request.storeId(), request.articleId(), request.amount());
            } else {
                articleOrderMessagingService.sendOrder(request.storeId(), request.articleId(), request.amount());
            }

            order.mark(ReplenishmentOrderStatus.ORDERED, "Order published to AMQP");
            replenishmentOrderRepository.save(order);
            return new ReplenishmentOrderResponseDTO(
                    correlationId,
                    ReplenishmentOrderStatus.ORDERED,
                    "/api/replenishment-orders/" + correlationId
            );
        } catch (RuntimeException e) {
            order.mark(ReplenishmentOrderStatus.FAILED, e.getMessage());
            replenishmentOrderRepository.save(order);
            throw e;
        }
    }

    @Override
    public Optional<ReplenishmentOrderStatusDTO> findStatus(UUID correlationId) {
        if (correlationId == null) {
            return Optional.empty();
        }
        return replenishmentOrderRepository.findByCorrelationId(correlationId)
                .map(this::toStatusDTO);
    }

    @Override
    @Transactional
    public void markReceived(long storeId, long articleId, long amount) {
        replenishmentOrderRepository
                .findFirstByStoreIdAndArticleIdAndStatusInOrderByCreatedAtAsc(storeId, articleId, OPEN_STATUSES)
                .ifPresent(order -> {
                    log.atDebug().log("Marking replenishment order {} as received", order.getCorrelationId());
                    order.mark(
                            ReplenishmentOrderStatus.RECEIVED,
                            "Received " + amount + " units from logistics event"
                    );
                });
    }

    private ReplenishmentOrderStatusDTO toStatusDTO(ReplenishmentOrder order) {
        @Nullable Instant persistedCreatedAt = order.getCreatedAt();
        Instant createdAt = persistedCreatedAt == null ? Instant.EPOCH : persistedCreatedAt;
        @Nullable Instant persistedChangedAt = order.getChangedAt();
        Instant updatedAt = persistedChangedAt == null ? createdAt : persistedChangedAt;
        return new ReplenishmentOrderStatusDTO(
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
