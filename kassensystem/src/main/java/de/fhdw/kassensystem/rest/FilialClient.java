package de.fhdw.kassensystem.rest;

import de.fhdw.commons.rest.GenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class FilialClient implements GenericClient {

    private final String ACTUATOR_URI = "/actuator/health";
    private final WebClient webClient;

    public FilialClient(@Value("${filial.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Mono<Void> checkStatus() {
        return webClient.get()
                .uri(ACTUATOR_URI)
                .retrieve()
                .bodyToMono(Void.class);
    }
}