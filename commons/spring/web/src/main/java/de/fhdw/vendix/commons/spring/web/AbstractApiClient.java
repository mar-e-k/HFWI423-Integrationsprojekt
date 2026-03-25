package de.fhdw.vendix.commons.spring.web;

import de.fhdw.vendix.security.api.jwt.JwtService;
import de.fhdw.vendix.security.api.jwt.payload.JwtPayload;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

public abstract class AbstractApiClient {

    private final JwtService jwtService;
    private final WebClient webClient;

    protected AbstractApiClient(JwtService jwtService, String baseUrl) {
        this.jwtService = jwtService;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    protected <T> T get(String uri, Class<T> responseType) {
        return execute(
                webClient.get()
                        .uri(uri),
                responseType
        );
    }

    protected <T, R> T post(String uri, R body, Class<T> responseType) {
        return execute(
                webClient.post()
                        .uri(uri)
                        .bodyValue(body),
                responseType
        );
    }

    protected <T, R> T put(String uri, R body, Class<T> responseType) {
        return execute(
                webClient.put()
                        .uri(uri)
                        .bodyValue(body),
                responseType
        );
    }

    protected void delete(String uri) {
        execute(
                webClient.delete()
                        .uri(uri),
                Void.class
        );
    }

    protected String resolveBearerToken() {
        JwtPayload payload = buildJwtPayload();
        return jwtService.generateToken(payload);
    }

    protected abstract JwtPayload buildJwtPayload();

    protected RuntimeException mapWebClientException(WebClientResponseException exception) {
        return new RuntimeException(
                "HTTP %s:%s".formatted(
                        exception.getStatusCode(),
                        exception.getResponseBodyAsString()
                ),
                exception
        );
    }

    private <T> T execute(WebClient.RequestHeadersSpec<?> request, Class<T> responseType) {
        T result = request
                .headers(headers -> headers.setBearerAuth(resolveBearerToken()))
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Client error: " + body)))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Server error: " + body)))
                )
                .bodyToMono(responseType)
                .switchIfEmpty(Mono.error(new IllegalStateException("Empty response body")))
                .timeout(Duration.ofSeconds(5)) // TODO: should probably be a setting
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException)
                .block();

        if (result == null) {
            throw new IllegalStateException("Unexpected null response");
        }

        return result;
    }
}