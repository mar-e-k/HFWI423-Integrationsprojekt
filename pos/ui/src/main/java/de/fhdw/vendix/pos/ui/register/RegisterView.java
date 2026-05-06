package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.commons.spring.web.api.orchestrator.StoreApi;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import de.fhdw.vendix.pos.ui.register.action_pad.ActionPad;
import de.fhdw.vendix.pos.ui.register.article_search.ArticleSearch;
import de.fhdw.vendix.pos.ui.register.article_search.ArticleSearchController;
import de.fhdw.vendix.pos.ui.register.controller.RegisterController;
import de.fhdw.vendix.pos.ui.register.receipt_view.ReceiptList;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = PosAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.CASHIER)
public class RegisterView extends HorizontalLayout {

    public RegisterView(
            StoreApi storeApi,
            CartService cartService,
            RegisterState state,
            RegisterController controller,
            ArticleSearchController searchController
    ) {

        ReceiptList receiptList = new ReceiptList(cartService, state);
        ArticleSearch articleSearch = new ArticleSearch(searchController, state);

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