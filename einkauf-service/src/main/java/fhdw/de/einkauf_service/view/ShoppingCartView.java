package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.config.ShoppingCartSession;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;
import fhdw.de.einkauf_service.service.PurchaseOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Warenkorb-Ansicht für Nachbestellungen
 *
 * Funktionen:
 * - Warenkorb-Artikel anzeigen mit Lagerbestand-Prüfung
 * - Menge pro Artikel anpassen
 * - Artikel entfernen
 * - Gesamtpreis berechnen
 * - Bestellung absenden (gruppiert nach Lieferant)
 * - Bestätigung mit Bestellnummer und Lieferdatum
 */
@Route(value = "cart", layout = MainLayout.class)
public class ShoppingCartView extends VerticalLayout {

    // ========== SERVICES (Backend-Anbindung) ==========
    private final ShoppingCartSession cartSession;
    private final ArticleService articleService;
    private final PurchaseOrderService orderService;

    // ========== UI-KOMPONENTEN ==========
    private final Grid<CartItemDisplay> grid = new Grid<>(CartItemDisplay.class, false);
    private final Span totalPriceLabel = new Span();
    private final Button checkoutButton = new Button("Bestellung absenden", new Icon(VaadinIcon.CART_O));
    private final Button clearButton = new Button("Warenkorb leeren", new Icon(VaadinIcon.TRASH));

    // ========== KONSTRUKTOR ==========
    public ShoppingCartView(ShoppingCartSession cartSession,
                            ArticleService articleService,
                            PurchaseOrderService orderService) {
        this.cartSession = cartSession;
        this.articleService = articleService;
        this.orderService = orderService;

        setSizeFull();
        addClassName("page-view");

        H2 title = new H2("Warenkorb");
        add(title);

        configureGrid();
        HorizontalLayout buttonLayout = createButtonLayout();
        add(grid, buttonLayout, totalPriceLabel);
        setFlexGrow(1, grid);

        updateGrid();
    }

    // ========== GRID KONFIGURATION ==========
    private void configureGrid() {
        // SPALTE 1: GTIN (Artikelnummer)
        grid.addColumn(item -> item.article().getArticleNumber())
                .setHeader("GTIN")
                .setAutoWidth(true);

        // SPALTE 2: Artikelname
        grid.addColumn(item -> item.article().getName())
                .setHeader("Artikelname")
                .setAutoWidth(true);

        // SPALTE 3: Lieferantenauswahl (ComboBox)
        grid.addComponentColumn(item -> {
            ComboBox<SupplierResponseDTO> supplierSelect = new ComboBox<>();
            List<SupplierResponseDTO> suppliers = item.article().getSuppliers() != null
                    ? new ArrayList<>(item.article().getSuppliers())
                    : new ArrayList<>();
            supplierSelect.setItems(suppliers);
            supplierSelect.setItemLabelGenerator(SupplierResponseDTO::getName);

            // Default: Hauptlieferant auswählen
            if (item.article().getMainSupplier() != null) {
                supplierSelect.setValue(item.article().getMainSupplier());
            }

            // Listener bei Auswahl
            supplierSelect.addValueChangeListener(event -> {
                SupplierResponseDTO selected = event.getValue();
                // Hier kannst du ggf. speichern oder prüfen, falls nötig
                Notification.show("Lieferant für " + item.article().getName() +
                                " gesetzt: " + (selected != null ? selected.getName() : "-"),
                        2000, Notification.Position.BOTTOM_START);
            });

            return supplierSelect;
        }).setHeader("Lieferant auswählen").setAutoWidth(true);

        // SPALTE 4: Lagerbestand
        grid.addColumn(item -> item.article().getStockLevel())
                .setHeader("Lagerbestand")
                .setAutoWidth(true);

        // SPALTE 5: Einzelpreis
        grid.addColumn(item -> String.format("%.2f €", item.article().getPurchasePrice()))
                .setHeader("Einzelpreis")
                .setAutoWidth(true);

        // SPALTE 6: Menge (bearbeitbar)
        grid.addComponentColumn(item -> {
            IntegerField quantityField = new IntegerField();
            quantityField.setValue(item.quantity());
            quantityField.setMin(1);
            quantityField.setWidth("100px");
            quantityField.setStepButtonsVisible(true);

            quantityField.addValueChangeListener(event -> {
                Integer newQuantity = event.getValue();
                if (newQuantity == null || newQuantity <= 0) {
                    quantityField.setValue(event.getOldValue());
                    return;
                }
                updateQuantity(item.article().getId(), newQuantity);
            });
            return quantityField;
        }).setHeader("Menge").setAutoWidth(true);

        // SPALTE 7: Zwischensumme
        grid.addColumn(item -> String.format("%.2f €", item.subtotal()))
                .setHeader("Zwischensumme")
                .setAutoWidth(true);

        // SPALTE 8: Löschen-Button
        grid.addComponentColumn(item -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(e -> removeItem(item.article().getId()));
            return deleteButton;
        }).setHeader("Aktion").setAutoWidth(true).setFlexGrow(0);

        grid.setSizeFull();
    }


    // ========== BUTTON-LAYOUT ==========
    private HorizontalLayout createButtonLayout() {
        checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutButton.addClickListener(e -> checkout());

        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearButton.addClickListener(e -> clearCart());

        HorizontalLayout layout = new HorizontalLayout(clearButton, checkoutButton);
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layout.setSpacing(true);
        return layout;
    }

    // ========== DATEN LADEN & ANZEIGEN ==========
    private void updateGrid() {
        Map<Long, Integer> cartItems = cartSession.getItems();

        if (cartItems.isEmpty()) {
            grid.setItems(new ArrayList<>());
            totalPriceLabel.setText("Warenkorb ist leer");
            checkoutButton.setEnabled(false);
            return;
        }

        List<CartItemDisplay> displayItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            try {
                ArticleResponseDTO article = articleService.findArticleById(entry.getKey());
                double subtotal = article.getPurchasePrice() * entry.getValue();
                displayItems.add(new CartItemDisplay(article, entry.getValue(), subtotal));
            } catch (Exception ignored) {
            }
        }

        grid.setItems(displayItems);

        double total = displayItems.stream().mapToDouble(CartItemDisplay::subtotal).sum();
        totalPriceLabel.setText(String.format("Gesamtpreis: %.2f €", total));
        totalPriceLabel.getStyle()
                .set("font-size", "1.5em")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)");

        checkoutButton.setEnabled(!displayItems.isEmpty());
    }

    // ========== WARENKORB-OPERATIONEN ==========
    private void updateQuantity(Long articleId, Integer newQuantity) {
        cartSession.removeItem(articleId);
        cartSession.addItem(articleId, newQuantity);
        updateGrid();
        Notification.show("Menge aktualisiert", 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void removeItem(Long articleId) {
        cartSession.removeItem(articleId);
        updateGrid();
        Notification.show("Artikel entfernt", 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    private void clearCart() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Warenkorb leeren?");

        VerticalLayout dialogLayout = new VerticalLayout(
                new Span("Möchten Sie wirklich alle Artikel aus dem Warenkorb entfernen?")
        );

        Button confirmButton = new Button("Ja, leeren", e -> {
            cartSession.clearCart();
            updateGrid();
            confirmDialog.close();
            Notification.show("Warenkorb geleert", 2000, Notification.Position.BOTTOM_START);
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Abbrechen", e -> confirmDialog.close());

        confirmDialog.add(dialogLayout);
        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    // ========== BESTELLUNG ABSENDEN ==========
    private void checkout() {
        if (cartSession.getItems().isEmpty()) {
            Notification.show("Warenkorb ist leer!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            List<OrderResponseDTO> orders = orderService.createAndSendOrdersFromCart();
            showOrderConfirmation(orders);
            updateGrid();
            Notification amqpNotification = Notification.show(
                    "Logistik & Kassensystem wurden benachrichtigt", 4000, Notification.Position.BOTTOM_END);
            amqpNotification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalStateException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Fehler beim Bestellen: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showOrderConfirmation(List<OrderResponseDTO> orders) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("✅ Bestellung erfolgreich!");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        content.add(new H3("Ihre Bestellungen wurden erfolgreich aufgegeben:"));

        for (OrderResponseDTO order : orders) {
            VerticalLayout orderInfo = new VerticalLayout();
            orderInfo.setPadding(true);
            orderInfo.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            orderInfo.getStyle().set("border-radius", "8px");
            orderInfo.add(new Span("Bestellnummer: " + order.orderNumber()));
            content.add(orderInfo);
        }

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    // ========== HELPER-KLASSEN ==========
    private record CartItemDisplay(
            ArticleResponseDTO article,
            Integer quantity,
            Double subtotal
    ) {}
}
