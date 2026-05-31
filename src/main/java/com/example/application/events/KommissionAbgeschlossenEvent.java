package com.example.application.events;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class KommissionAbgeschlossenEvent extends ApplicationEvent {

    private final long storeId;
    private final List<ArticleDelivery> deliveries;

    public KommissionAbgeschlossenEvent(Object source, long storeId, List<ArticleDelivery> deliveries) {
        super(source);
        this.storeId = storeId;
        this.deliveries = List.copyOf(deliveries);
    }

    public long getStoreId() {
        return storeId;
    }

    public List<ArticleDelivery> getDeliveries() {
        return deliveries;
    }

    public record ArticleDelivery(long articleId, long quantity) {}
}
