package de.fhdw.vendix.orchestrator.core.embeddable.instance_details;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Embeddable
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_connection", columnNames = {"instance_server", "instance_port"})
})
@SuppressWarnings("NullAway")
public class InstanceDetails {

    @Column(nullable = false, updatable = false, name = "instance_uuid")
    @NotNull(message = "UUID cannot be null")
    private UUID uuid;

    @Column(nullable = false, updatable = false, name = "instance_host")
    @NotBlank(message = "Host cannot be blank")
    private String host;

    @Column(nullable = false, updatable = false, name = "instance_server")
    @NotBlank(message = "Server cannot be blank")
    private String server;

    @Column(nullable = false, updatable = false, name = "instance_port")
    @Min(value = 0, message = "Port must be at least 0")
    @NotNull(message = "Port cannot be null")
    private Integer port;

    public InstanceDetails() {
        super();
    }

    @Default
    public InstanceDetails(UUID uuid, String host, String server, Integer port) {
        this.uuid = uuid;
        this.host = host;
        this.server = server;
        this.port = port;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }

    public Integer getPort() {
        return port;
    }

    public String generateInstanceURL() {
        return "http://%s:%d/".formatted(
                server,
                port
        );
    }
}