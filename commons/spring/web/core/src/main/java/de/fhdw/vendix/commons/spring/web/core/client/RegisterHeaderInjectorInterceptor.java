package de.fhdw.vendix.commons.spring.web.core.client;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextNotSetException;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.web.core.RoutingHeader;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public final class RegisterHeaderInjectorInterceptor implements ClientHttpRequestInterceptor {

    private final RegisterContext registerContext;

    public RegisterHeaderInjectorInterceptor(RegisterContext registerContext) {
        this.registerContext = registerContext;
    }

    @Override
    @NullMarked
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        RegisterDTO register = registerContext.getRegister();
        if (register == null) {
            throw new ContextNotSetException(
                    "RegisterContext is not initialized for outbound request to " + request.getURI()
            );
        }

        Long id = register.id();
        if (id == null) {
            throw new IllegalStateException(
                    "RegisterDTO.id is null; cannot propagate register identity"
            );
        }

        HttpHeaders headers = request.getHeaders();
        headers.set(RoutingHeader.REGISTER_ROUTING.getHeader(), String.valueOf(id));

        return execution.execute(request, body);
    }
}