package de.fhdw.vendix.commons.security.spring.context;

import de.fhdw.vendix.security.api.context.AppContext;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public final class DefaultAppContext implements AppContext {

    private final UUID instanceUUID;
    private final String hostname;
    private final String serverName;

    private int serverPort;

    public DefaultAppContext(Environment environment) {
        this.instanceUUID = UUID.randomUUID();
        this.hostname = resolveHostname();
        this.serverName = resolveServerName(environment);
    }

    private String resolveHostname()  {
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
        this.serverPort = event.getWebServer().getPort();
    }

    @Override
    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    @Override
    public String getHostName() {
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