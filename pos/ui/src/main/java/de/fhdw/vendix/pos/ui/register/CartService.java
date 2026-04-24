package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.spring.security.SecurityService;
import de.fhdw.vendix.pos.core.register.RegisterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.security.auth.login.CredentialException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@SessionScope
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final RegisterContext registerContext;
    private final SecurityService securityService;
    private final List<CartLine> cartLines = new LinkedList<>();

    public CartService(RegisterContext registerContext, SecurityService securityService) {
        this.registerContext = registerContext;
        this.securityService = securityService;
    }

    public void addLine(ArticleDTO article, int amount) {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }
        Objects.requireNonNull(article.id());
        ReceiptLineDTO newLine = new ReceiptLineDTO(
                null,
                0L,
                article.id(),
                (long) amount,
                null,
                null
        );

        addOrMerge(newLine, article);
    }

    private void addOrMerge(ReceiptLineDTO newLine, ArticleDTO article) {

        for (int i = 0; i < cartLines.size(); i++) {
            CartLine existing = cartLines.get(i);

            if (isSameLine(existing.line(), newLine)) {

                ReceiptLineDTO merged = new ReceiptLineDTO(
                        existing.line().id(),
                        existing.line().receiptId(),
                        existing.line().articleId(),
                        existing.line().articleAmount() + newLine.articleAmount(),
                        existing.line().discountOverride(),
                        existing.line().priceOverride()
                );

                cartLines.set(i, new CartLine(merged, existing.article()));
                return;
            }
        }

        cartLines.add(new CartLine(newLine, article));
    }

    public List<CartLine> getCartLines() {
        return List.copyOf(cartLines);
    }

    public void clearCart() {
        cartLines.clear();
    }

    public ReceiptDTO generateReceipt() {
        try {
            log.atDebug().log("Generating receipt...");
            if (cartLines.isEmpty()) {
                throw new IllegalArgumentException("Cannot create a Receipt for an empty list");
            }

            UUID cashierUUID = securityService.getAuthenticatedUserUuid()
                    .orElseThrow(IllegalStateException::new);

            Objects.requireNonNull(registerContext.getRegister());
            Objects.requireNonNull(registerContext.getRegister().id());

            ReceiptDTO receipt = new ReceiptDTO(
                    null,
                    registerContext.getRegister().id(),
                    registerContext.getRegister().storeId(),
                    5L, // TODO: change field to accept uuid instead
                    PaymentMethod.CARD,
                    ReceiptStatus.OPEN
            );
            log.atDebug().log("Successfully generated receipt");
            clearCart();
            return receipt;
        } catch (Exception e) {
            log.atError().log("Failed to generate receipt", e);
            throw e;
        }
    }

    private boolean isSameLine(ReceiptLineDTO a, ReceiptLineDTO b) {
        return Objects.equals(a.articleId(), b.articleId()) &&
                Objects.equals(a.discountOverride(), b.discountOverride()) &&
                Objects.equals(a.priceOverride(), b.priceOverride());
    }
}