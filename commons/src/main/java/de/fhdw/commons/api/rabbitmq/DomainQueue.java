package de.fhdw.commons.api.rabbitmq;

public enum DomainQueue {
    LOGISTIC_STORE_RESTOCK("logistic.order.store.article");

    private final String queue;

    DomainQueue(String queue) {
        this.queue = queue;
    }

    public String getQueue() {
        return queue;
    }
}