package de.fhdw.vendix.orchestrator.core.embeddable.instance_details;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Embeddable
@SuppressWarnings("NullAway")
public class InstanceDetails {

    @Column(nullable = false, updatable = false, name = "instance_uuid")
    private UUID uuid;

    @Column(nullable = false, updatable = false, name = "instance_host")
    @NotBlank
    private String host;

    @Column(nullable = false, updatable = false, name = "instance_server")
    @NotBlank
    private String server;

    @Column(nullable = false, updatable = false, name = "instance_port")
    @Min(0)
    private int port;

    public InstanceDetails() {
        super();
    }

    @Default
    public InstanceDetails(UUID uuid, String host, String server, int port) {
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

    public int getPort() {
        return port;
    }

    public String generateInstanceURL() {
        return "http://%s:%d/".formatted(
                server,
                port
        );
    }
}