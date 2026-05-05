package de.fhdw.vendix.commons.spring.web.client;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import java.io.IOException;

public final class LoggingHandler implements RestClient.ResponseSpec.ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingHandler.class);

    @Override
    @NullMarked
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.trace("Handling request {} with status code {}", request, response.getStatusCode());
    }
}