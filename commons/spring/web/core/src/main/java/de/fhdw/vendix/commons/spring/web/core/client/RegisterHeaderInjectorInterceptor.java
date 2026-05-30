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
        Long id = resolveRegisterContextId();

        HttpHeaders headers = request.getHeaders();
        headers.set(RoutingHeader.REGISTER_ROUTING.getHeader(), String.valueOf(id));

        return execution.execute(request, body);
    }

    private Long resolveRegisterContextId() {
        if (registerContext.getRegister() instanceof RegisterDTO registerDTO && registerDTO.id() != null) {
            return registerDTO.id();
        }
        throw new ContextNotSetException(
                "Cannot find registerId based on current Context. RegisterContext not set yet."
        );
    }
}