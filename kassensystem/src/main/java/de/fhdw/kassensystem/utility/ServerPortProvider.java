package de.fhdw.kassensystem.utility;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServerPortProvider implements ApplicationListener<WebServerInitializedEvent> {

    private int port;

    public ServerPortProvider() {
        super();
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
    }

    public int getPort() {
        return port;
    }
}