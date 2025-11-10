package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.service.ReceiptService;
import de.fhdw.kassensystem.view.cashier.CartItem;
import de.fhdw.kassensystem.view.cashier.CartItemsManager;
import de.fhdw.kassensystem.view.cashier.CashierView;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route("/payment")
@PageTitle("Bezahlung")
@RolesAllowed(AccountRoleEnum.ROLE_CASHIER)
public class PaymentView extends BaseView implements BeforeEnterObserver {

    private final CartItemsManager cartItemsManager;
    private final ReceiptService receiptService;

    private Grid<CartItem> cartGrid;
    private Span totalLabel;

    private Anchor downloadLink;

    public PaymentView(CartItemsManager cartItemsManager, ReceiptService receiptService) {
        this.cartItemsManager = cartItemsManager;
        this.receiptService = receiptService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (cartItemsManager.getCart().isEmpty()) {
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
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::getPosition).setHeader("Pos.");
        cartGrid.addColumn(i -> i.getArticle().getName()).setHeader("Artikelname");
        cartGrid.addColumn(i -> i.getArticle().getArticleNumber()).setHeader("Artikelnummer");
        cartGrid.addColumn(i -> String.format("%.2f €", i.getEffectivePrice())).setHeader("Stückpreis");
        cartGrid.addColumn(CartItem::getQuantity).setHeader("Menge");
        cartGrid.addColumn(i ->
                String.format("%.2f €", i.getEffectivePrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        ).setHeader("Gesamtpreis");

        cartGrid.setWidthFull();

        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        Button paymentButton = new Button("Zahlung", e -> handlePayment());

        downloadLink = new Anchor();
        downloadLink.setText("Bon herunterladen");
        downloadLink.getStyle().set("display", "none");

        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setWidthFull();

        add(cartSection, paymentButton, downloadLink);
    }

    private void handlePayment() {
        try {
            String fileName = "Bon-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";

            ByteArrayInputStream generatedPdfStream = receiptService.generateReceipt(cartItemsManager.getCart());
            byte[] pdfBytes = generatedPdfStream.readAllBytes();

            DownloadHandler handler = DownloadHandler.fromInputStream(
                    event -> new DownloadResponse(
                            new ByteArrayInputStream(pdfBytes),
                            fileName,
                            "application/pdf",
                            pdfBytes.length
                    )
            );

            com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource resource =
                    new com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource(handler, this.getElement());

            String url = VaadinSession.getCurrent()
                    .getResourceRegistry()
                    .registerResource(resource)
                    .getResourceUri()
                    .toString();

            // Direkt herunterladen
            downloadLink.setHref(url);
            downloadLink.getElement().setAttribute("download", fileName);
            downloadLink.getElement().callJsFunction("click");

            Notification.show("Zahlung abgeschlossen — Bon wird heruntergeladen.",
                            2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            cartItemsManager.clearCart();
            updateCartGrid();

        } catch (IOException ex) {
            Notification.show("Fehler beim Erstellen des Bons: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateCartGrid() {
        List<CartItem> items = new ArrayList<>(cartItemsManager.getCart());
        items.sort(Comparator.comparingInt(CartItem::getPosition));

        int pos = 1;
        for (CartItem item : items) item.setPosition(pos++);

        cartGrid.setItems(items);

        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(i -> i.getEffectivePrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLabel.setText(String.format("Gesamtanzahl: %d | Gesamtpreis: %s €",
                totalQuantity, totalPrice.toPlainString()));
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button backToCart = new Button("Zurück zum Warenkorb");
        backToCart.addClickListener(e -> UI.getCurrent().navigate("cashier"));
        return new HorizontalLayout(backToCart);
    }
}
