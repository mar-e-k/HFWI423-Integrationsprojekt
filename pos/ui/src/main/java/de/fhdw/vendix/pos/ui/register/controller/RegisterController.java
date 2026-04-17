package de.fhdw.vendix.pos.ui.register.controller;

import com.vaadin.flow.component.notification.Notification;
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
}