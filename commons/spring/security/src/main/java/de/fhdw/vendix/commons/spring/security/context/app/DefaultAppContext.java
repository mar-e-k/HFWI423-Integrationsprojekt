package de.fhdw.vendix.commons.spring.security.context.app;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import org.slf4j.MDC;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;

public final class DefaultAppContext implements AppContext {

    private final String applicationName;
    private final UUID instanceUUID;
    private final String hostname;
    private final String serverName;
    private int serverPort;

    public DefaultAppContext(Environment environment) {
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
    public void onApplicationEvent(WebServerInitializedEvent event) {
        serverPort = event.getWebServer().getPort();
        // At this point, everything should be loaded
        MDC.put("__host__", "%s:%d".formatted(serverName, serverPort));
        MDC.put("__instance__", instanceUUID.toString());
        if (applicationName.equalsIgnoreCase("orchestrator")) {
            MDC.put("__target_type__", TargetType.ORCHESTRATOR.name());
            MDC.put("__target_id__", "N/A");
        }
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public UUID getInstanceUUID() {
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

    public String getBaseUrl() {
        return "http://%s:%s".formatted(
                serverName,
                serverPort
        );
    }
}