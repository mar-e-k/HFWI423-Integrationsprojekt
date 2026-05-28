package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.spring.security.context.auth.DefaultUser;
import de.fhdw.vendix.pos.core.register.RegisterContext;
import de.fhdw.vendix.pos.web.client.store.StoreClients;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@SessionScope
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final RegisterContext registerContext;
    private final AuthenticationContext authenticationContext;
    private final StoreClients storeClients;
    private final List<CartLine> cartLines = new LinkedList<>();

    public CartService(
            RegisterContext registerContext,
            AuthenticationContext authenticationContext,
            StoreClients storeClients
    ) {
        this.registerContext = registerContext;
        this.authenticationContext = authenticationContext;
        this.storeClients = storeClients;
    }

    public void addLine(ArticleDTO article, int amount) {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }
        Long articleId = Objects.requireNonNull(article.id());
        ReceiptLineDTO newLine = new ReceiptLineDTO(
                null,
                0L,
                articleId,
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

    public CheckoutResponseDTO checkout(PaymentMethod paymentMethod) {
        try {
            log.atDebug().log("Submitting cart checkout...");
            if (cartLines.isEmpty()) {
                throw new IllegalArgumentException("Cannot checkout an empty cart");
            }

            DefaultUser cashier = authenticationContext.getAuthenticatedUser(DefaultUser.class)
                    .orElseThrow(IllegalStateException::new);
            RegisterDTO register = Objects.requireNonNull(registerContext.getRegister());
            Long registerId = Objects.requireNonNull(register.id());
            Long storeId = Objects.requireNonNull(register.storeId());
            Long cashierId = Objects.requireNonNull(cashier.authContext().account().id());

            CheckoutRequestDTO request = new CheckoutRequestDTO(
                    storeId,
                    registerId,
                    cashierId,
                    paymentMethod,
                    cartLines.stream()
                            .map(this::toCheckoutLine)
                            .toList(),
                    false
            );

            ResponseEntity<CheckoutResponseDTO> response = storeClients.checkout().checkout(request);
            @Nullable CheckoutResponseDTO body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new IllegalStateException("Store checkout failed with status " + response.getStatusCode());
            }

            clearCart();
            log.atInfo().log("Checkout completed with receiptId={}", body.receiptId());
            return body;
        } catch (Exception e) {
            log.atError().log("Failed to submit checkout", e);
            throw e;
        }
    }

    public ReceiptDTO generateReceipt() {
        try {
            log.atDebug().log("Generating receipt...");
            if (cartLines.isEmpty()) {
                throw new IllegalArgumentException("Cannot create a Receipt for an empty list");
            }
            DefaultUser cashier = authenticationContext.getAuthenticatedUser(DefaultUser.class)
                    .orElseThrow(IllegalStateException::new);
            RegisterDTO register = Objects.requireNonNull(registerContext.getRegister());
            Long registerId = Objects.requireNonNull(register.id());
            Long storeId = Objects.requireNonNull(register.storeId());
            Long cashierId = Objects.requireNonNull(cashier.authContext().account().id());
            ReceiptDTO receipt = new ReceiptDTO(
                    null,
                    registerId,
                    storeId,
                    cashierId,
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

    private CheckoutLineDTO toCheckoutLine(CartLine cartLine) {
        @Nullable DiscountOverrideDTO discountOverride = cartLine.line().discountOverride();
        return new CheckoutLineDTO(
                cartLine.line().articleId(),
                cartLine.line().articleAmount(),
                discountOverride == null
                        ? null
                        : discountOverride.amount()
        );
    }
}
