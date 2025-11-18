package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;
import fhdw.de.einkauf_service.service.PurchaseOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ShoppingCartSession cartSession;      // Session-basierter Warenkorb
    private final ArticleService articleService;        // Artikel-Informationen abrufen
    private final PurchaseOrderService orderService;    // Bestellungen erstellen

    // ========== UI-KOMPONENTEN ==========
    private final Grid<CartItemDisplay> grid = new Grid<>(CartItemDisplay.class, false);
    private final Span totalPriceLabel = new Span();
    private final Button checkoutButton = new Button("Bestellung absenden", new Icon(VaadinIcon.CART_O));
    private final Button clearButton = new Button("Warenkorb leeren", new Icon(VaadinIcon.TRASH));

    // ========== KONSTRUKTOR ==========
    /**
     * Spring injiziert automatisch die benötigten Services
     */
    public ShoppingCartView(ShoppingCartSession cartSession, 
                           ArticleService articleService,
                           PurchaseOrderService orderService) {
        this.cartSession = cartSession;
        this.articleService = articleService;
        this.orderService = orderService;

        // Layout-Einstellungen
        setSizeFull();        // View nimmt volle Breite/Höhe
        setPadding(true);     // Innerer Abstand

        // Header erstellen
        H2 title = new H2("🛒 Warenkorb");
        add(title);

        // Grid (Tabelle) konfigurieren
        configureGrid();

        // Button-Leiste erstellen
        HorizontalLayout buttonLayout = createButtonLayout();

        // Layout zusammenbauen (von oben nach unten)
        add(grid, buttonLayout, totalPriceLabel);
        setFlexGrow(1, grid);  // Grid nimmt verfügbaren Platz ein

        // Initiale Daten laden
        updateGrid();
    }

    // ========== GRID KONFIGURATION ==========
    /**
     * Konfiguriert die Tabelle mit allen Spalten
     */
    private void configureGrid() {
        // SPALTE 1: GTIN (Artikelnummer)
        grid.addColumn(item -> item.article().getArticleNumber())
            .setHeader("GTIN")
            .setAutoWidth(true);

        // SPALTE 2: Artikelname
        grid.addColumn(item -> item.article().getName())
            .setHeader("Artikelname")
            .setAutoWidth(true);

        // SPALTE 3: Lieferant
        grid.addColumn(item -> item.article().getSupplierName())
            .setHeader("Lieferant")
            .setAutoWidth(true);

        // SPALTE 4: Lagerbestand anzeigen
       grid.addColumn(item -> item.article().getStockLevel()).setHeader("Lagerbestand");


        // SPALTE 5: Einzelpreis
        grid.addColumn(item -> String.format("%.2f €", item.article().getPurchasePrice()))
            .setHeader("Einzelpreis")
            .setAutoWidth(true);

        // SPALTE 6: Menge (bearbeitbar mit IntegerField)
        grid.addComponentColumn(item -> {
            IntegerField quantityField = new IntegerField();
            quantityField.setValue(item.quantity());
            quantityField.setMin(1);
            quantityField.setWidth("100px");
            quantityField.setStepButtonsVisible(true);  // +/- Buttons anzeigen

            // Event: Wenn Benutzer die Menge ändert
            quantityField.addValueChangeListener(event -> {
                Integer newQuantity = event.getValue();
                
                // Validierung 1: Menge muss größer 0 sein
                if (newQuantity == null || newQuantity <= 0) {
                    quantityField.setValue(event.getOldValue());
                    return;
                }
                
                // Menge im Backend aktualisieren
                updateQuantity(item.article().getId(), newQuantity);
            });

            return quantityField;
        }).setHeader("Menge").setAutoWidth(true);

        // SPALTE 7: Zwischensumme (Menge × Einzelpreis)
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
    /**
     * Erstellt die Button-Leiste (Warenkorb leeren, Bestellen)
     */
    private HorizontalLayout createButtonLayout() {
        // "Bestellung absenden" Button (grün, primär)
        checkoutButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        checkoutButton.addClickListener(e -> checkout());

        // "Warenkorb leeren" Button (rot, sekundär)
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearButton.addClickListener(e -> clearCart());

        HorizontalLayout layout = new HorizontalLayout(clearButton, checkoutButton);
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);  // Buttons rechts
        layout.setSpacing(true);

        return layout;
    }

    // ========== DATEN LADEN & ANZEIGEN ==========
  
    private void updateGrid() {
    // 1. Warenkorb aus Session holen
    Map<Long, Integer> cartItems = cartSession.getItems();

    // 2. Prüfen ob Warenkorb leer ist
    if (cartItems.isEmpty()) {
        grid.setItems(new ArrayList<>());
        totalPriceLabel.setText("Warenkorb ist leer");
        checkoutButton.setEnabled(false);
        return;
    }

    // 3. ALLE Artikel aus Backend laden
    ArticleFilterDTO emptyFilter = new ArticleFilterDTO();
    List<ArticleResponseDTO> allArticles = articleService.findFilteredArticles(emptyFilter);
        
    // 4. Nur die Artikel filtern, die im Warenkorb sind
    List<ArticleResponseDTO> articlesInCart = allArticles.stream()
        .filter(article -> cartItems.containsKey(article.getId()))
        .collect(Collectors.toList());
    
    // 5. Daten kombinieren: Article + Menge + Zwischensumme
    List<CartItemDisplay> displayItems = new ArrayList<>();
    for (ArticleResponseDTO article : articlesInCart) {
        Integer quantity = cartItems.get(article.getId());
        double subtotal = article.getPurchasePrice() * quantity;
        displayItems.add(new CartItemDisplay(article, quantity, subtotal));
    }

    // 6. Grid füllen
    grid.setItems(displayItems);

    // 7. Gesamtpreis berechnen
    double total = 0.0;
    for (CartItemDisplay item : displayItems) {
        total += item.subtotal();
    }
    
    totalPriceLabel.setText(String.format("Gesamtpreis: %.2f €", total));
    totalPriceLabel.getStyle()
        .set("font-size", "1.5em")
        .set("font-weight", "bold")
        .set("color", "var(--lumo-primary-text-color)");

    checkoutButton.setEnabled(!displayItems.isEmpty());
    
}

    // ========== WARENKORB-OPERATIONEN ==========
    
    /**
     * Aktualisiert die Menge eines Artikels im Warenkorb
     */
    private void updateQuantity(Long articleId, Integer newQuantity) {
        // Alte Menge entfernen
        cartSession.removeItem(articleId);
        // Neue Menge hinzufügen
        cartSession.addItem(articleId, newQuantity);
        // Grid neu laden
        updateGrid();
        
        Notification.show("Menge aktualisiert", 2000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Entfernt einen Artikel aus dem Warenkorb
     */
    private void removeItem(Long articleId) {
        cartSession.removeItem(articleId);
        updateGrid();
        
        Notification.show("Artikel entfernt", 2000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }

    /**
     * Leert den kompletten Warenkorb (mit Bestätigung)
     */
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
        // Validierung: Warenkorb darf nicht leer sein
        if (cartSession.getItems().isEmpty()) {
            Notification.show("Warenkorb ist leer!", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Backend-Service aufrufen
            List<OrderResponseDTO> orders = orderService.createAndSendOrdersFromCart();
            
            // Erfolg: Bestätigungs-Dialog anzeigen
            showOrderConfirmation(orders);
            
            // Grid aktualisieren (Warenkorb ist jetzt leer)
            updateGrid();
            
        } catch (IllegalStateException e) {
            // Backend-Fehler (z.B. "Warenkorb ist leer")
            Notification.show(e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            // Allgemeiner Fehler
            Notification.show("Fehler beim Bestellen: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Zeigt Bestätigungs-Dialog mit Bestellnummern und Lieferdatum
     * 
     * @param orders Liste der erstellten Bestellungen (eine pro Lieferant)
     */
    private void showOrderConfirmation(List<OrderResponseDTO> orders) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeaderTitle("✅ Bestellung erfolgreich!");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        content.add(new H3("Ihre Bestellungen wurden erfolgreich aufgegeben:"));

        // Für jede Bestellung eine Info-Box erstellen
        for (OrderResponseDTO order : orders) {
            VerticalLayout orderInfo = new VerticalLayout();
            orderInfo.setPadding(true);
            orderInfo.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
            orderInfo.getStyle().set("border-radius", "8px");

            orderInfo.add(new Span("Bestellnummer: " + order.orderNumber()));
            //Brauchen wir?? orderInfo.add(new Span("Voraussichtliche Lieferung: " + order.expectedDeliveryDate()));

            content.add(orderInfo);
        }

        Button closeButton = new Button("Schließen", e -> {
            dialog.close();
            // Optional: Zur Bestellübersicht navigieren
            // getUI().ifPresent(ui -> ui.navigate("orders"));
        });
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    // ========== HELPER-KLASSEN ==========
    /**
     * Hilfsklasse zur Anzeige von Warenkorb-Artikeln im Grid
     */
    private record CartItemDisplay(
        ArticleResponseDTO article, 
        Integer quantity, 
        Double subtotal
    ) {}
}