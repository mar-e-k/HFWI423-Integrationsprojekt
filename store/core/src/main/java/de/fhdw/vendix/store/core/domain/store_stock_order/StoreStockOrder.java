package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.OrderStatus;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.persistance.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_store_stock_order_correlation", columnNames = "correlation_id")
        },
        indexes = {
                @Index(name = "idx_store_stock_order_correlation", columnList = "correlation_id"),
                @Index(name = "idx_store_stock_order_store_article_status", columnList = "store_id, article_id, status")
        }
)
public class StoreStockOrder extends AbstractSpringDataAuditingEntity<Long> {

    @Column(name = "correlation_id", nullable = false, updatable = false)
    @NotNull(message = "Correlation ID cannot be null")
    private UUID correlationId;

    @Column(name = "store_id", nullable = false, updatable = false)
    @NotNull(message = "Store ID cannot be null")
    @Min(value = 1, message = "Store ID must be at least 1")
    private Long storeId;

    @Column(name = "article_id", nullable = false, updatable = false)
    @NotNull(message = "Article ID cannot be null")
    @Min(value = 1, message = "Article ID must be at least 1")
    private Long articleId;

    @Column(nullable = false, updatable = false)
    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;

    @Column(nullable = false, updatable = false)
    @NotNull(message = "Urgent cannot be null")
    private Boolean urgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    private OrderStatus status;

    @Column(length = 512)
    @Nullable
    private String message;

    protected StoreStockOrder() {}


    public StoreStockOrder(
            UUID correlationId,
            Long storeId,
            Long articleId,
            Long amount,
            Boolean urgent,
            OrderStatus status
    ) {
        this.correlationId = correlationId;
        this.storeId = storeId;
        this.articleId = articleId;
        this.amount = amount;
        this.urgent = urgent;
        this.status = status;
        this.message = "Order accepted and waiting for AMQP publish";
    }

    @Default
    protected StoreStockOrder(
            @Nullable Long id,
            UUID correlationId,
            Long storeId,
            Long articleId,
            Long amount,
            Boolean urgent,
            OrderStatus status,
            @Nullable String message
    ) {
        super(id);
        this.correlationId = correlationId;
        this.storeId = storeId;
        this.articleId = articleId;
        this.amount = amount;
        this.urgent = urgent;
        this.status = status;
        this.message = message;
    }

    void mark(OrderStatus status, @Nullable String message) {
        if (status == null) {
            throw new IllegalArgumentException("Parameter 'status' cannot be null");
        }
        this.status = status;
        this.message = message;
    }

    UUID getCorrelationId() {
        return correlationId;
    }

    Long getStoreId() {
        return storeId;
    }

    Long getArticleId() {
        return articleId;
    }

    Long getAmount() {
        return amount;
    }

    Boolean getUrgent() {
        return urgent;
    }

    OrderStatus getStatus() {
        return status;
    }

    @Nullable String getMessage() {
        return message;
    }
}
