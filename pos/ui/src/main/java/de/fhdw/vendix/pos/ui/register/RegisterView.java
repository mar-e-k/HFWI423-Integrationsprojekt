package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import de.fhdw.vendix.pos.ui.RegisterController;
import de.fhdw.vendix.pos.ui.register.action_pad.ActionPad;
import de.fhdw.vendix.pos.ui.register.article_search.ArticleSearch;
import de.fhdw.vendix.pos.ui.register.receipt_view.ReceiptList;
import de.fhdw.vendix.pos.web.client.store.StoreClients;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
public class RegisterView extends HorizontalLayout {

    public RegisterView(
            StoreClients storeClients,
            Cart cart,
            RegisterState state,
            RegisterController controller
    ) {

        ReceiptList receiptList = new ReceiptList(cart, state);
        ArticleSearch articleSearch = new ArticleSearch(storeClients.article(), state);

        ActionPad actionPad = new ActionPad(
                state,
                () -> {
                    controller.addToCart();
                    receiptList.refresh();
                }
        );

        VerticalLayout rightSide = new VerticalLayout(articleSearch, actionPad);
        rightSide.setSizeFull();

        add(receiptList, rightSide);
        expand(receiptList, rightSide);
    }
}