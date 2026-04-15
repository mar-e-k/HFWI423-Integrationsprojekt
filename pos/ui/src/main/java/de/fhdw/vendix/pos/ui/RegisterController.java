package de.fhdw.vendix.pos.ui;

import com.vaadin.flow.component.notification.Notification;
import de.fhdw.vendix.pos.ui.register.Cart;
import de.fhdw.vendix.pos.ui.register.RegisterState;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class RegisterController {

    private final Cart cart;
    private final RegisterState state;

    public RegisterController(Cart cart, RegisterState state) {
        this.cart = cart;
        this.state = state;
    }

    public void addToCart() {
        var article = state.getSelectedArticle();

        if (article.isEmpty()) {
            Notification.show("No article selected");
            return;
        }

        cart.addLine(article.get(), state.getAmount());
    }
}