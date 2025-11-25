package de.fhdw.commons.api.rabbitmq;

public enum DomainQueue {
    RECEIPT("receipt.command"),
    ACCOUNT("account.command"),
    ACCOUNT_ROLE("account.role.command"),;

    private final String queue;

    DomainQueue(String queue) {
        this.queue = queue;
    }

    public String getQueue() {
        return queue;
    }
}