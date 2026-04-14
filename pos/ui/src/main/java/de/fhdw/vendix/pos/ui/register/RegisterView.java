package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.client.store.api.ArticleProxyService;
import de.fhdw.vendix.commons.spring.web.client.store.api.ReceiptProxyService;
import de.fhdw.vendix.pos.ui.PosAppLayout;
import de.fhdw.vendix.pos.web.client.store.StoreClients;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Route(value = "register", layout = PosAppLayout.class)
@RolesAllowed(Role.ROLE_CASHIER)
public class RegisterView extends HorizontalLayout {

    private final ArticleProxyService articleProxyService;
    private final ReceiptProxyService receiptProxyService;
    private final Cart cart;

    private Optional<ArticleDTO> currentArticle;

    private final ReceiptView receiptView;
    private final ArticleSearchBar searchBar;
    private final ArticleStatusPanel statusPanel;
    private final ActionPad actionPad;

    public RegisterView(StoreClients storeClients, Cart cart) {
        this.articleProxyService = storeClients.article();
        this.receiptProxyService = storeClients.receipt();
        this.cart = cart;

        this.currentArticle = Optional.empty();

        this.receiptView = new ReceiptView(cart);
        this.searchBar = new ArticleSearchBar(this::searchArticle);
        this.statusPanel =  new ArticleStatusPanel();
        this.actionPad = new ActionPad(this::addCurrentArticleToCart);

        VerticalLayout rightSide = new VerticalLayout(
                searchBar,
                statusPanel,
                actionPad
        );
        rightSide.setSizeFull();

        add(receiptView, rightSide);
        expand(receiptView, rightSide);
    }

    private void searchArticle(String gtinText) {
        try {
            Long gtin = Long.valueOf(gtinText);

            ResponseEntity<ArticleDTO> response =
                    articleProxyService.getArticleByGtin(gtin);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                currentArticle = Optional.ofNullable(response.getBody());
                currentArticle.ifPresent(statusPanel::showArticle);

            } else {
                currentArticle = Optional.empty();
                statusPanel.showNotFound();
            }

        } catch (Exception e) {
            currentArticle = Optional.empty();
            statusPanel.showError("Invalid input");
        }
    }

    private void addCurrentArticleToCart(int amount) {
        if (currentArticle.isEmpty()) {
            Notification.show("No article selected");
            return;
        }

        cart.addLine(currentArticle.get(), amount);

        receiptView.refresh();
    }
}