package com.example.application.api.rabbitmq.producer;

public enum DomainQueue {
    LOGISTIC_STORE_RESTOCK("logistic.order.article");

    private final String queue;

    DomainQueue(String queue) {
        this.queue = queue;
    }

    public String getQueue() {
        return queue;
    }
}