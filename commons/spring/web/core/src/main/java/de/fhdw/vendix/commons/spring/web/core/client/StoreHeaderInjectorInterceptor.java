package de.fhdw.vendix.commons.spring.web.core.client;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.spring.app.context.ContextNotSetException;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.web.core.RoutingHeader;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public final class StoreHeaderInjectorInterceptor implements ClientHttpRequestInterceptor {

    private final StoreContext storeContext;
    private final RegisterContext registerContext;

    public StoreHeaderInjectorInterceptor(StoreContext storeContext, RegisterContext registerContext) {
        this.storeContext = storeContext;
        this.registerContext = registerContext;
    }

    @Override
    @NullMarked
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        Long id = resolveStoreContextId();

        HttpHeaders headers = request.getHeaders();
        headers.set(RoutingHeader.STORE_ROUTING.getHeader(), String.valueOf(id));

        return execution.execute(request, body);
    }

    private Long resolveStoreContextId() {
        if (storeContext.getStore() instanceof StoreDTO storeDTO && storeDTO.id() != null) {
            return storeDTO.id();
        }
        if (registerContext.getRegister() instanceof RegisterDTO registerDTO && registerDTO.id() != null) {
            return registerDTO.id();
        }
        throw new ContextNotSetException(
                "Cannot find storeId based on current Context. StoreContext and RegisterContext not set yet."
        );
    }
}