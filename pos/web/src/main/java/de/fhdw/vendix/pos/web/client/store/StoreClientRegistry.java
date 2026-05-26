package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.spring.security.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.security.context.ContextNotSetException;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.CheckoutProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class StoreClientRegistry {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Nullable
    private volatile ArticleProxyService article;
    @Nullable
    private volatile ReceiptProxyService receipt;
    @Nullable
    private volatile CheckoutProxyService checkout;

    public void initialize(ArticleProxyService article, ReceiptProxyService receipt, CheckoutProxyService checkout) {
        if (article == null) {
            throw new IllegalArgumentException("Parameter 'article' cannot be null");
        }
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (checkout == null) {
            throw new IllegalArgumentException("Parameter 'checkout' cannot be null");
        }
        if (!initialized.compareAndSet(false, true)) {
            throw new ContextAlreadySetException("Store clients already initialized");
        }

        this.article = article;
        this.receipt = receipt;
        this.checkout = checkout;
    }

    public ArticleProxyService getArticle() {
        @Nullable ArticleProxyService current = article;
        if (current == null) {
            throw new ContextNotSetException("ArticleProxyService not initialized");
        }
        return current;
    }

    public ReceiptProxyService getReceipt() {
        @Nullable ReceiptProxyService current = receipt;
        if (current == null) {
            throw new ContextNotSetException("ReceiptProxyService not initialized");
        }
        return current;
    }

    public CheckoutProxyService getCheckout() {
        @Nullable CheckoutProxyService current = checkout;
        if (current == null) {
            throw new ContextNotSetException("CheckoutProxyService not initialized");
        }
        return current;
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}
