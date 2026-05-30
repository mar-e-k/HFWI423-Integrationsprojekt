package de.fhdw.vendix.pos.ui.register.controller;

import com.vaadin.flow.component.notification.Notification;
import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.pos.ui.register.CartService;
import de.fhdw.vendix.pos.ui.register.RegisterState;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class RegisterController {

    private final CartService cartService;
    private final RegisterState state;

    public RegisterController(CartService cartService, RegisterState state) {
        this.cartService = cartService;
        this.state = state;
    }

    public void addToCart() {
        var article = state.getSelectedArticle();

        if (article.isEmpty()) {
            Notification.show("No article selected");
            return;
        }

        cartService.addLine(article.get(), state.getAmount());
    }

    public void checkout() {
        try {
            ReceiptResponseDTO response = cartService.checkout(PaymentMethod.CARD);
            Notification.show("Checkout completed. Receipt ID: " + response.id());
        } catch (Exception e) {
            String message = e.getMessage();
            Notification.show("Checkout failed: " + (message == null ? e.getClass().getSimpleName() : message));
        }
    }
}
