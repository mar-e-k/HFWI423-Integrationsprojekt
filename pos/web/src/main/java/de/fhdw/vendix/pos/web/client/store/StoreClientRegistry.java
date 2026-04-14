package de.fhdw.vendix.pos.web.client.store;

import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class StoreClientRegistry {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private volatile ArticleProxyService article;
    private volatile ReceiptProxyService receipt;

    public void initialize(ArticleProxyService article, ReceiptProxyService receipt) {
        if (article == null) {
            throw new IllegalArgumentException("Parameter 'article' cannot be null");
        }
        if (receipt == null) {
            throw new IllegalArgumentException("Parameter 'receipt' cannot be null");
        }
        if (!initialized.compareAndSet(false, true)) {
            throw new IllegalStateException("Store clients already initialized");
        }

        // Safe publication via volatile writes after CAS
        this.article = article;
        this.receipt = receipt;
    }

    public ArticleProxyService getArticle() {
        ArticleProxyService client = article;
        if (client == null) {
            throw new IllegalStateException("ArticleProxyService not initialized");
        }
        return client;
    }

    public ReceiptProxyService getReceipt() {
        ReceiptProxyService client = receipt;
        if (client == null) {
            throw new IllegalStateException("ReceiptProxyService not initialized");
        }
        return client;
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}