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
import com.vaadin.flow.server.VaadinSession;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Route("/payment")
@PageTitle("Bezahlung")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class PaymentView extends BaseView implements BeforeEnterObserver {

    private Grid<CartItem> cartGrid;
    private Map<String, CartItem> cartItems = new LinkedHashMap<>();
    private Span totalLabel;

    public PaymentView() {
        // Konstruktor
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, CartItem> cartData = (Map<String, CartItem>) VaadinSession.getCurrent().getAttribute("cartDataForPayment");

        if (cartData == null || cartData.isEmpty()) {
            // Wenn keine Daten vorhanden sind, zur Kasse zurückleiten
            event.rerouteTo(CashierView.class);
        } else {
            // Daten laden und aus der Session entfernen, um Wiederverwendung zu verhindern
            this.cartItems = cartData;
            VaadinSession.getCurrent().setAttribute("cartDataForPayment", null);
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
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice() * item.getQuantity())).setHeader("Gesamtpreis").setAutoWidth(true);

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
        if (cartItems == null) return;
        
        List<CartItem> items = new ArrayList<>(cartItems.values());
        items.sort(Comparator.comparingInt(CartItem::getPosition));
        int pos = 1;
        for (CartItem item : items) {
            item.setPosition(pos++);
        }

        cartGrid.setItems(items);

        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        double totalPrice = items.stream()
                .mapToDouble(item -> item.getEffectivePrice() * item.getQuantity())
                .sum();

        totalLabel.setText(String.format("Gesamtanzahl: %d | Gesamtpreis: %.2f €", totalQuantity, totalPrice));
    }
}
