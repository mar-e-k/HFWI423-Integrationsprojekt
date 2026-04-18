package fhdw.de.einkauf_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Zentrale Verwaltung aller Custom Metrics für den Einkaufs-Service.
 * Exponiert Metriken an Prometheus für Monitoring und Observability.
 */
@Component
public class MetricsRegistry {

    private final MeterRegistry meterRegistry;

    // ==================== Order Metrics ====================
    public final Counter ordersCreated;
    public final Counter ordersCompleted;
    public final Counter ordersFailed;
    public final Timer orderProcessingTime;

    // ==================== Article Metrics ====================
    public final Counter articlesAdded;
    public final Counter articlesUpdated;
    public final Counter articlesDeleted;
    public final Counter articlesViewed;
    public final Timer articleCreationTime;

    // ==================== Shopping Cart Metrics ====================
    public final Counter cartItemsAdded;
    public final Counter cartItemsRemoved;
    public final Counter checkoutsCompleted;
    public final Counter checkoutsFailed;
    public final Timer checkoutProcessingTime;

    // ==================== Contingent Metrics ====================
    public final Counter contingentAllocated;
    public final Counter contingentFreed;
    public final Counter contingentExceeded;

    // ==================== Shelf Metrics ====================
    public final Counter shelfPlacementsCreated;
    public final Counter shelfPlacementsRemoved;

    // ==================== Supplier Metrics ====================
    public final Counter suppliersAdded;
    public final Counter suppliersUpdated;

    // ==================== AMQP Event Metrics ====================
    public final Counter eventsPublished;
    public final Counter eventsReceived;
    public final Counter eventProcessingErrors;
    public final Timer eventProcessingTime;

    // ==================== Cache Metrics ====================
    public final Counter cacheHits;
    public final Counter cacheMisses;

    public MetricsRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Order Metrics
        this.ordersCreated = Counter.builder("einkauf_service.orders.created.total")
                .description("Total number of orders created")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.ordersCompleted = Counter.builder("einkauf_service.orders.completed.total")
                .description("Total number of orders completed successfully")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.ordersFailed = Counter.builder("einkauf_service.orders.failed.total")
                .description("Total number of failed orders")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.orderProcessingTime = Timer.builder("einkauf_service.orders.processing.time")
                .description("Order processing time duration")
                .tag("service", "einkauf-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Article Metrics
        this.articlesAdded = Counter.builder("einkauf_service.articles.added.total")
                .description("Total number of articles added")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.articlesUpdated = Counter.builder("einkauf_service.articles.updated.total")
                .description("Total number of articles updated")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.articlesDeleted = Counter.builder("einkauf_service.articles.deleted.total")
                .description("Total number of articles deleted")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.articlesViewed = Counter.builder("einkauf_service.articles.viewed.total")
                .description("Total number of article view events")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.articleCreationTime = Timer.builder("einkauf_service.articles.creation.time")
                .description("Article creation processing time")
                .tag("service", "einkauf-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Shopping Cart Metrics
        this.cartItemsAdded = Counter.builder("einkauf_service.cart.items.added.total")
                .description("Total number of items added to shopping carts")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.cartItemsRemoved = Counter.builder("einkauf_service.cart.items.removed.total")
                .description("Total number of items removed from shopping carts")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.checkoutsCompleted = Counter.builder("einkauf_service.checkouts.completed.total")
                .description("Total number of completed checkouts")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.checkoutsFailed = Counter.builder("einkauf_service.checkouts.failed.total")
                .description("Total number of failed checkouts")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.checkoutProcessingTime = Timer.builder("einkauf_service.checkouts.processing.time")
                .description("Checkout processing time duration")
                .tag("service", "einkauf-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Contingent Metrics
        this.contingentAllocated = Counter.builder("einkauf_service.contingent.allocated.total")
                .description("Total contingent units allocated")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.contingentFreed = Counter.builder("einkauf_service.contingent.freed.total")
                .description("Total contingent units freed")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.contingentExceeded = Counter.builder("einkauf_service.contingent.exceeded.total")
                .description("Total contingent overflow incidents")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        // Shelf Metrics
        this.shelfPlacementsCreated = Counter.builder("einkauf_service.shelf.placements.created.total")
                .description("Total number of shelf placements created")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.shelfPlacementsRemoved = Counter.builder("einkauf_service.shelf.placements.removed.total")
                .description("Total number of shelf placements removed")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        // Supplier Metrics
        this.suppliersAdded = Counter.builder("einkauf_service.suppliers.added.total")
                .description("Total number of suppliers added")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.suppliersUpdated = Counter.builder("einkauf_service.suppliers.updated.total")
                .description("Total number of suppliers updated")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        // AMQP Event Metrics
        this.eventsPublished = Counter.builder("einkauf_service.amqp.events.published.total")
                .description("Total number of AMQP events published")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.eventsReceived = Counter.builder("einkauf_service.amqp.events.received.total")
                .description("Total number of AMQP events received")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.eventProcessingErrors = Counter.builder("einkauf_service.amqp.events.processing.errors.total")
                .description("Total number of AMQP event processing errors")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.eventProcessingTime = Timer.builder("einkauf_service.amqp.events.processing.time")
                .description("AMQP event processing time duration")
                .tag("service", "einkauf-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Cache Metrics
        this.cacheHits = Counter.builder("einkauf_service.cache.hits.total")
                .description("Total number of cache hits")
                .tag("service", "einkauf-service")
                .register(meterRegistry);

        this.cacheMisses = Counter.builder("einkauf_service.cache.misses.total")
                .description("Total number of cache misses")
                .tag("service", "einkauf-service")
                .register(meterRegistry);
    }
}

