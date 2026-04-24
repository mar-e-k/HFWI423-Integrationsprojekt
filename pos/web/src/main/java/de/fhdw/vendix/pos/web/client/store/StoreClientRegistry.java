package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.spring.app.context.ContextAlreadySetException;
import de.fhdw.vendix.commons.spring.app.context.ContextNotSetException;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class StoreClientRegistry {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Nullable
    private volatile ArticleProxyService article;
    @Nullable
    private volatile ReceiptProxyService receipt;

    public void initialize(ArticleProxyService article, ReceiptProxyService receipt) {
        if (article == null) {
            throw new IllegalArgumentException("Parameter 'article' cannot be null");
        }
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (!initialized.compareAndSet(false, true)) {
            throw new ContextAlreadySetException("Store clients already initialized");
        }

        this.article = article;
        this.receipt = receipt;
    }

    public ArticleProxyService getArticle() {
        if (article == null) {
            throw new ContextNotSetException("ArticleProxyService not initialized");
        }
        return article;
    }

    public ReceiptProxyService getReceipt() {
        if (receipt == null) {
            throw new ContextNotSetException("ReceiptProxyService not initialized");
        }
        return receipt;
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}