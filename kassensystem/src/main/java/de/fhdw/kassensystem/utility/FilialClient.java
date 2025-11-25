package de.fhdw.kassensystem.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FilialClient {

    private final WebClient webClient;

    public FilialClient(@Value("${spring.filialsystem.uri}") String uri) {
        webClient = WebClient.builder().baseUrl(uri).build();
    }

    public WebClient getWebClient() {
        return webClient;
    }
}