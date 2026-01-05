package de.fhdw.kassensystem.utility;

import de.fhdw.commons.api.dto.RegisterDTO;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class RegisterClient {

    private RegisterDTO register;
    private int port;

    private final String instanceId = UUID.randomUUID().toString();
    private final String host = "localhost";

    public RegisterClient() {
        super();
    }

    public RegisterDTO getRegister() {
        return register;
    }

    public synchronized void setRegister(RegisterDTO register) {
        this.register = register;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }
}