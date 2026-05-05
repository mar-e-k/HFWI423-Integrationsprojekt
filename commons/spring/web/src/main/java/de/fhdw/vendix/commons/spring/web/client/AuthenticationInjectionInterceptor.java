package de.fhdw.vendix.commons.spring.web.client;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;

public final class AuthenticationInjectionInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationInjectionInterceptor.class);

    @Override
    @NullMarked
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest incomingRequest = attributes.getRequest();
            String authorizationHeader = incomingRequest.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
        } else {
             log.atWarn().log("Non-web thread execution: no incoming authorization header available");
        }

        return execution.execute(request, body);
    }
}