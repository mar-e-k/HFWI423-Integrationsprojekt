package de.fhdw.vendix.commons.spring.web.client;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public final class RegisterHeaderInjectorInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NullMarked
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
