package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "store_watcher")
public class StoreWatcher extends AbstractEntity {

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Instance ID must not be blank")
    private String instanceId;

    @Column(nullable = false)
    @NotBlank(message = "Host must not be blank")
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    @NotBlank(message = "Application name must not be blank")
    private String applicationName;

    @Column(nullable = false)
    @NotNull(message = "Last heartbeat timestamp must not be null")
    private Instant lastHeartbeatTimestamp;

    public StoreWatcher() {
        super();
    }

    public StoreWatcher(String instanceId, String host, int port, String applicationName, Instant lastHeartbeatTimestamp) {
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.applicationName = applicationName;
        this.lastHeartbeatTimestamp = lastHeartbeatTimestamp;
    }

    // Getter and Setter
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Instant getLastHeartbeatTimestamp() {
        return lastHeartbeatTimestamp;
    }

    public void setLastHeartbeatTimestamp(Instant lastHeartbeatTimestamp) {
        this.lastHeartbeatTimestamp = lastHeartbeatTimestamp;
    }
}
