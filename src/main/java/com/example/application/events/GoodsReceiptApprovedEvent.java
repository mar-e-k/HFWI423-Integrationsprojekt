package com.example.application.events;

import org.springframework.context.ApplicationEvent;

public class GoodsReceiptApprovedEvent extends ApplicationEvent {

    private final Long receiptId;

    public GoodsReceiptApprovedEvent(Object source, Long receiptId) {
        super(source);
        this.receiptId = receiptId;
    }

    public Long getReceiptId() {
        return receiptId;
    }
}
