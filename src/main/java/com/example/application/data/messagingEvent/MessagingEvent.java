package com.example.application.data.messagingEvent;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class MessagingEvent extends AbstractEntity {

    @NotNull
    @Column(name = "event_type", nullable = false)
    private String eventType; // "NewQuota" or "DeleteQuota"

    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "amount")
    private Long amount; // For NewQuota

    @Size(max = 1000)
    @Column(name = "details")
    private String details; // Additional info

    @NotNull
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    // Constructors
    public MessagingEvent() {}

    public MessagingEvent(String eventType, Long articleId, Long amount, String details) {
        this.eventType = eventType;
        this.articleId = articleId;
        this.amount = amount;
        this.details = details;
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
