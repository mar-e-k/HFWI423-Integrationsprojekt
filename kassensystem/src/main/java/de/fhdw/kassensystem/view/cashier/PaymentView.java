package de.fhdw.kassensystem.view.cashier;

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
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextField;

import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.service.ReceiptService;
import de.fhdw.kassensystem.view.BaseView;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            cartItemsManager.updateGrid(cartGrid, totalLabel);
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
        cartGrid.addComponentColumn(i -> {
            Span container = new Span();
            BigDecimal base = i.getBaseUnitPrice();
            BigDecimal discounted = i.getDiscountedUnitPrice();

            if (i.hasDiscount() && i.getDiscountedQuantity() != null
                    && i.getDiscountedQuantity() >= i.getQuantity()) {
                // gesamte Menge rabattiert -> klar vorher/nachher anzeigen
                Span oldPrice = new Span(String.format("%.2f €", base));
                oldPrice.getStyle().set("text-decoration", "line-through");

                Span arrow = new Span(" → ");
                Span newPrice = new Span(String.format("%.2f €", discounted));

                container.add(oldPrice, arrow, newPrice);
            } else if (i.hasDiscount()) {
                // nur Teilmenge rabattiert -> kurze Info
                Span baseSpan = new Span(String.format("%.2f €", base) + " / ");
                Span discSpan = new Span(String.format("%.2f €", discounted) +
                        " (" + i.getDiscountedQuantity() + "x)");
                container.add(baseSpan, discSpan);
            } else {
                container.setText(String.format("%.2f €", base));
            }
            return container;
        }).setHeader("Stückpreis");
        cartGrid.addColumn(CartItem::getQuantity).setHeader("Menge");
        cartGrid.addColumn(i ->
                String.format("%.2f €", i.getTotalPriceWithDiscount())
        ).setHeader("Gesamtpreis");

        cartGrid.setWidthFull();

        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        Button paymentButton = new Button("Zahlung", e -> handlePayment());
        paymentButton.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_PRIMARY);

        downloadLink = new Anchor();
        downloadLink.setText("Bon herunterladen");
        downloadLink.getStyle().set("display", "none");

        HorizontalLayout footerLayout = new HorizontalLayout(totalLabel, paymentButton);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout cartSection = new VerticalLayout(cartGrid, footerLayout);
        cartSection.setWidthFull();

        add(cartSection, downloadLink);
    }

    private void handlePayment() {
        if (cartItemsManager.getCart().isEmpty()) {
            Notification.show("Warenkorb ist leer!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        openCashDialog();
    }

    private void finishPayment() {
        try {
            String fileName = "Bon-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";

            com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource resource = createResource(fileName);

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
            cartItemsManager.updateGrid(cartGrid, totalLabel);

        } catch (IOException ex) {
            Notification.show("Fehler beim Erstellen des Bons: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource createResource(String fileName) throws IOException {
        ByteArrayInputStream generatedPdfStream = receiptService.generateReceipt(cartItemsManager.getCart(), isCashPayment);

        byte[] pdfBytes = generatedPdfStream.readAllBytes();

        DownloadHandler handler = DownloadHandler.fromInputStream(
                event -> new DownloadResponse(
                        new ByteArrayInputStream(pdfBytes),
                        fileName,
                        "application/pdf",
                        pdfBytes.length
                )
        );

        return new com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource(handler, this.getElement());
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button backToCart = new Button("Zurück zum Warenkorb");
        backToCart.addClickListener(e -> UI.getCurrent().navigate("cashier"));
        return new HorizontalLayout(backToCart);
    }

    private boolean isCashPayment = false;

    private void openCashDialog() {
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setWidth("500px");
        dialog.setHeight("400px");

        H3 title = new H3("Zahlungsmethode");
        Button closeBtn = new Button(new Icon("lumo", "cross"), e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout(title, closeBtn);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Button payButton = new Button("Bestätigen");
        Button cardButton = new Button("Kartenzahlung");
        Button cashButton = new Button("Bargeldzahlung");

        cardButton.setWidth("218px");
        cashButton.setWidth("218px");
        cardButton.setHeight("200px");
        cashButton.setHeight("200px");
        if (isCashPayment) {
            cashButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            cardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }

        TextField cashGivenField = new TextField("Gegebenes Bargeld");
        cashGivenField.setWidth("150px");
        cashGivenField.setSuffixComponent(new Span("€"));
        cashGivenField.setVisible(isCashPayment);

        cardButton.addClickListener(e -> {
            isCashPayment = false;
            cashGivenField.setVisible(false);
            cardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cashButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        cashButton.addClickListener(e -> {
            isCashPayment = true;
            cashGivenField.setVisible(true);
            cashButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            cardButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        });

        HorizontalLayout paymentSelection = new HorizontalLayout(cardButton, cashButton);
        paymentSelection.setWidthFull();
        paymentSelection.setSpacing(true);

        HorizontalLayout footer = new HorizontalLayout(cashGivenField, payButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setAlignItems(FlexComponent.Alignment.END);
        footer.getStyle().set("margin-top", "auto");

        VerticalLayout bodyLayout = new VerticalLayout(paymentSelection);
        bodyLayout.setPadding(false);
        bodyLayout.setSpacing(true);
        bodyLayout.setWidthFull();
        bodyLayout.getStyle().set("margin-top", "10px");

        VerticalLayout wrapper = new VerticalLayout(header, bodyLayout, footer);
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();
        wrapper.getStyle().set("height", "100%");
        wrapper.getStyle().set("display", "flex");
        wrapper.getStyle().set("flex-direction", "column");

        payButton.addClickListener(e -> handlePayButtonClick(dialog, cashGivenField));
        dialog.add(wrapper);
        dialog.open();
    }

    private void handlePayButtonClick(Dialog dialog, TextField cashGivenField) {

        if (isCashPayment) {

            if (cashGivenField.isEmpty()) {
                Notification.show("Bargeldmenge angeben!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            BigDecimal cashGiven;
            try {
                cashGiven = new BigDecimal(cashGivenField.getValue().replace(",", "."));
            } catch (NumberFormatException ex) {
                Notification.show("Gültige Zahl angeben!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Totalbetrag korrekt berechnen
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : cartItemsManager.getCart()) {
                totalAmount = totalAmount.add(item.getTotalPriceWithDiscount());
            }

            BigDecimal change = cashGiven.subtract(totalAmount);

            if (change.compareTo(BigDecimal.ZERO) < 0) {
                Notification.show("Betrag zu niedrig!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Rückgeld anzeigen – AUTOMATISCH abschließen
            Notification changeNotification = new Notification();
            changeNotification.setPosition(Notification.Position.MIDDLE);
            changeNotification.setDuration(2000); // <- AUTOMATISCH schließen
            changeNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            Span text = new Span("Kunde bekommt " + String.format("%.2f €", change) + " Rückgeld.");

            changeNotification.add(text);
            changeNotification.open();

            // Dialog schließen
            dialog.close();

            // AUTOMATISCH nach 2 Sekunden den Bon drucken (wie Kartenzahlung)
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => $0.$server.autoFinishCash(), 1000);",
                    getElement()
            );

        } else {
            // Kartenzahlung
            dialog.close();
            showCardProcessingDialog();
        }
    }

    @ClientCallable
    private void autoFinishCash() {
        finishPayment();
    }

    private void showCardProcessingDialog() {
        final Dialog processing = new Dialog();
        processing.setModal(true);
        processing.setCloseOnEsc(false);
        processing.setCloseOnOutsideClick(false);
        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        H3 message = new H3("Folge den Schritten auf dem Kartelesegerät...");
        message.getStyle().set("text-align", "center");
        layout.add(message);
        processing.add(layout);

        Div serverRpc = new Div() {
            @ClientCallable
            public void closeDialogAndFinish() {
                processing.close();
                finishPayment();
            }
        };

        serverRpc.getStyle().set("display", "none");
        processing.add(serverRpc);
        processing.open();
        UI ui = UI.getCurrent();

        ui.getPage().executeJs(
                "setTimeout(() => { $0.textContent = 'Prozess war erfolgreich!'; }, 5000);",
                message.getElement()
        );

        ui.getPage().executeJs(
                "setTimeout(() => { $0.$server.closeDialogAndFinish(); }, 7000);",
                serverRpc.getElement()
        );
    }
}
