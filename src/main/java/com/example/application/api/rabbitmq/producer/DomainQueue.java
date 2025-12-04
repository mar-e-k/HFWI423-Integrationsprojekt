package com.example.application.api.rabbitmq.producer;

public enum DomainQueue {

    LOGISTIC_STORE_RESTOCK("logistic.order.store.article"),
    STORE_LOGISTIC_RESTOCK("store.article.order.logistic"),
    LOGISTIC_NEW_CONTINGENT_ARTICLE("logistic.article.new");

    private final String queue;

    DomainQueue(String queue) {
        this.queue = queue;
    }

    public String getQueue() {
        return queue;
    }
}