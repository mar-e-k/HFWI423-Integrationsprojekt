package de.fhdw.vendix.commons.spring.app.context.system;

import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;

public final class DefaultSystemContext implements SystemContext {

    private final ApplicationEventPublisher publisher;

    private final String applicationName;
    private final UUID instanceUUID;
    private final String hostname;
    private final String serverName;
    private int serverPort;

    public DefaultSystemContext(Environment environment, ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.applicationName = Objects.requireNonNull(environment.getProperty("spring.application.name"), "Property 'spring.application.name' is not set");
        this.instanceUUID = UUID.randomUUID();
        this.hostname = resolveHostname();
        this.serverName = resolveServerName(environment);
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveServerName(Environment env) {
        String configured = env.getProperty("server.address");

        if (configured == null || configured.equals("0.0.0.0")) {
            return "localhost";
        }

        return configured;
    }

    @EventListener
    public void onWebServerInitializedEvent(WebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
        publisher.publishEvent(new SystemContextInitializedEvent(this, this));
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public UUID getInstanceUuid() {
        return instanceUUID;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }
}