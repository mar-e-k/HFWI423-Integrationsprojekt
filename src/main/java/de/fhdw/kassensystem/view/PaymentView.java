package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.view.cashier.CartItem;
import de.fhdw.kassensystem.view.cashier.CartItemsManager;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route("/payment")
@PageTitle("Bezahlung")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class PaymentView extends BaseView implements BeforeEnterObserver {

    private final CartItemsManager cartItemsManager;

    private Grid<CartItem> cartGrid;
    private Span totalLabel;

    public PaymentView(CartItemsManager cartItemsManager) {
        this.cartItemsManager = cartItemsManager;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (cartItemsManager.getCart().isEmpty()) {
            // Wenn keine Daten vorhanden sind, zur Kasse zurückleiten
            event.rerouteTo(CashierView.class);
        } else {
            updateCartGrid();
        }
    }

    @Override
    protected String setTopbarTitle() {
        return "Bezahlung";
    }

    @Override
    protected void init() {
        // Warenkorb-Grid Initialisierung
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::getPosition).setHeader("Pos.").setAutoWidth(true);
        cartGrid.addColumn(item -> item.getArticle().getName()).setHeader("Artikelname").setWidth("200px");
        cartGrid.addColumn(item -> item.getArticle().getArticleNumber()).setHeader("Artikelnummer").setAutoWidth(true);
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice())).setHeader("Stückpreis").setAutoWidth(true);
        cartGrid.addColumn(CartItem::getQuantity).setHeader("Menge").setAutoWidth(true);
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity())))).setHeader("Gesamtpreis").setAutoWidth(true);

        cartGrid.setWidthFull();
        cartGrid.getStyle().set("max-height", "50vh");
        cartGrid.getStyle().set("overflow-y", "auto");

        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setWidthFull();
        cartSection.setPadding(false);
        cartSection.setSpacing(true);
        cartSection.setAlignItems(Alignment.STRETCH);

        add(cartSection);
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button backToCartButton = new Button("Zurück zum Warenkorb");
        backToCartButton.addClickListener(e -> UI.getCurrent().navigate("cashier"));
        return new HorizontalLayout(backToCartButton);
    }

    private void updateCartGrid() {
        List<CartItem> items = new ArrayList<>(cartItemsManager.getCart());

        items.sort(Comparator.comparingInt(CartItem::getPosition));

        int pos = 1;
        for (CartItem item : items) {
            item.setPosition(pos++);
        }

        cartGrid.setItems(items);
        cartGrid.getDataProvider().refreshAll();

        int totalQuantity = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalPrice = items.stream()
                .map(item -> item.getOverriddenPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLabel.setText(
                String.format("Gesamtanzahl: %d | Gesamtpreis: %s €",
                        totalQuantity,
                        totalPrice.toPlainString()
                )
        );
    }
}
