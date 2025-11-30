package de.fhdw.kassensystem.utility;

import de.fhdw.commons.api.dto.RegisterDTO;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RegisterClient implements ApplicationListener<WebServerInitializedEvent> {

    private RegisterDTO registerDTO;

    private String host = "localhost";
    private int port;

    public RegisterClient() {
        super();
    }

    public RegisterDTO getRegisterDTO() {
        return registerDTO;
    }

    public void setRegisterDTO(RegisterDTO registerDTO) {
        this.registerDTO = registerDTO;
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

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        port = event.getWebServer().getPort();
    }
}