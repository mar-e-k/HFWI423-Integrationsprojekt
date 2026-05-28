package de.fhdw.vendix.commons.spring.web.core.client;

import de.fhdw.vendix.commons.spring.app.context.ContextNotSetException;
import de.fhdw.vendix.commons.spring.app.context.register.RegisterContext;
import de.fhdw.vendix.commons.spring.app.context.store.StoreContext;
import de.fhdw.vendix.commons.spring.web.core.RoutingHeader;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;

public final class StoreHeaderInjectorInterceptor implements ClientHttpRequestInterceptor {

    @Nullable
    private final StoreContext storeContext;

    @Nullable
    private final RegisterContext registerContext;

    public StoreHeaderInjectorInterceptor(StoreContext storeContext) {
        Assert.notNull(storeContext, "StoreContext cannot be");
        this.storeContext = storeContext;
        this.registerContext = null;
    }

    public StoreHeaderInjectorInterceptor(RegisterContext registerContext) {
        Assert.notNull(registerContext, "");
        this.storeContext = null;
        this.registerContext = registerContext;
    }

    @Override
    @NullMarked
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        Long id = resolveId();

        if (id == null) {
            throw new IllegalStateException(
                    "Store id is null"
            );
        }

        HttpHeaders headers = request.getHeaders();
        headers.set(RoutingHeader.STORE_ROUTING.getHeader(), String.valueOf(id));

        return execution.execute(request, body);
    }

    private Long resolveId() {

        Long storeId = null;
        Long registerId = null;

        if (storeContext != null) {
            var store = storeContext.getStore();
            if (store == null) {
                throw new ContextNotSetException("StoreContext not initialized");
            }
            if (store.id() == null) {
                throw new IllegalStateException("Store.id is null");
            }
            storeId = store.id();
        }

        if (registerContext != null) {
            var register = registerContext.getRegister();
            if (register == null) {
                throw new ContextNotSetException("RegisterContext not initialized");
            }
            if (register.storeId() == null) {
                throw new IllegalStateException("Register.storeId is null");
            }
            registerId = register.storeId();
        }

        if (storeId != null && registerId != null) {
            throw new IllegalStateException(
                    "Both StoreContext and RegisterContext are set; exactly one is allowed"
            );
        }

        if (storeId != null) {
            return storeId;
        }

        if (registerId != null) {
            return registerId;
        }

        throw new IllegalStateException("No routing context set");
    }
}