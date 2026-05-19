package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "received_deal_notification",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_received_deal_notification_external_event_id",
                columnNames = "external_event_id"
        )
)
public class ReceivedDealNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long articleId;

    @Column
    private String articleNumber;

    @Column
    private String articleName;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "external_event_id", length = 128)
    private String externalEventId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getArticleId() { return articleId; }
    public void setArticleId(long articleId) { this.articleId = articleId; }

    public String getArticleNumber() { return articleNumber; }
    public void setArticleNumber(String articleNumber) { this.articleNumber = articleNumber; }

    public String getArticleName() { return articleName; }
    public void setArticleName(String articleName) { this.articleName = articleName; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getExternalEventId() { return externalEventId; }
    public void setExternalEventId(String externalEventId) { this.externalEventId = externalEventId; }
}
