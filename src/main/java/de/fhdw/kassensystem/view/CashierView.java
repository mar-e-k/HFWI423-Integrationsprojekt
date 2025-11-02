package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.Article;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import de.fhdw.kassensystem.utility.config.Roles;
import jakarta.annotation.security.RolesAllowed;

import java.util.*;

@Route("/cashier")
@RolesAllowed({Roles.Type.CASHIER, Roles.Type.ADMIN})
@PageTitle("Cashier View")
public class CashierView extends BaseView {

    private final ArticleService articleService;
    private Grid<Article> articleGrid;
    private Grid<CartItem> cartGrid;
    private TextArea descriptionOutputField;

    // Map nach Artikelnummer statt Article-Objekt
    private final Map<String, CartItem> cartItems = new LinkedHashMap<>();

    private Span totalLabel;

    public CashierView(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    protected String setTopbarTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void init() {
        // Komponenten
        TextField searchField = new TextField("Artikelnummer");
        Button searchButton = new Button("Suchen", new Icon(VaadinIcon.SEARCH));
        Span errorLabel = new Span();

        // Grid Initialisierung (Artikelanzeige)
        articleGrid = new Grid<>(Article.class, false);
        articleGrid.setHeight("auto"); // Setzt die Höhe automatisch
        articleGrid.setAllRowsVisible(true); // Zeigt alle Zeilen an, ohne Scrollbalken
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false); // Initial unsichtbar

        // Grid-Spalten Definierung
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar").setWidth("70px");
        articleGrid.addColumn(Article::getName).setHeader("Artikelname").setWidth("200px");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer").setAutoWidth(true);
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis").setAutoWidth(true);
        articleGrid.addColumn(article -> article.getStockLevel() + " Stück").setHeader("Lagerbestand").setAutoWidth(true);
        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz").setAutoWidth(true);

        // Hinzufügen-Button (für Warenkorb)
        articleGrid.addComponentColumn(article -> {
                    Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                    addButton.getElement().setProperty("title", "Zum Warenkorb hinzufügen");
                    addButton.addClickListener(e -> addToCart(article)); // Artikel wird dem Warenkorb hinzugefügt
                    addButton.setEnabled(article.getIsAvailable());
                    return addButton;
                }).setHeader("Hinzufügen")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // Warenkorb-Grid Initialisierung
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::getPosition).setHeader("Pos.").setAutoWidth(true);
        cartGrid.addColumn(item -> item.getArticle().getName()).setHeader("Artikelname").setWidth("200px");
        cartGrid.addColumn(item -> item.getArticle().getArticleNumber()).setHeader("Artikelnummer").setAutoWidth(true);

        // Editor für Preisänderung
        var editor = cartGrid.getEditor();
        var binder = new com.vaadin.flow.data.binder.Binder<CartItem>(CartItem.class);
        editor.setBinder(binder);

        // TextField für den Preis-Editor
        TextField priceEditor = new TextField();
        priceEditor.setSuffixComponent(new Span("€"));
        priceEditor.setWidth("80px");
        priceEditor.getStyle().set("text-align", "right");

       // Binder
        binder.forField(priceEditor)
                .withConverter(
                        value -> {
                            if (value == null || value.isBlank()) return null; // leer -> null
                            try {
                                return Double.parseDouble(value.replace(",", "."));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        },
                        value -> value != null ? String.format("%.2f", value) : ""
                )
                .bind(CartItem::getOverriddenPrice, CartItem::setOverriddenPrice);

        // Spalte Stückpreis
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice()))
                .setHeader("Stückpreis")
                .setAutoWidth(true)
                .setEditorComponent(priceEditor);

        // Editor öffnen beim Doppelklick
        cartGrid.addItemDoubleClickListener(event -> {
            CartItem item = event.getItem();
            editor.editItem(item);

            // Originalpreis beim ersten Öffnen übernehmen, falls leer
            Double currentPrice = item.getOverriddenPrice();
            priceEditor.setValue(currentPrice != null
                    ? String.format("%.2f", currentPrice)
                    : String.format("%.2f", item.getArticle().getSellingPrice()));

            priceEditor.focus();
        });

        // Enter-Taste im Editor
        priceEditor.addKeyDownListener(Key.ENTER, event -> {
            if (editor.isOpen()) {
                String input = priceEditor.getValue().trim().replace(",", ".");
                CartItem item = editor.getItem();

                // Leeres Feld → Originalpreis wieder setzen
                if (input.isEmpty()) {
                    item.setOverriddenPrice(null);
                    editor.save();
                    editor.cancel();
                    updateCartGrid();
                    return;
                }

                // Nur gültige Zahlen zulassen
                if (!input.matches("\\d+(\\.\\d{0,2})?")) {
                    Notification.show("Bitte nur gültige Zahlen eingeben!", 2000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // Eingabe speichern
                item.setOverriddenPrice(Double.parseDouble(input));
                editor.save();
                editor.cancel();
                updateCartGrid();

                // Hier kommt die Bestätigung
                Notification.show(
                        "Der Preis von " + item.getArticle().getName() + " wurde auf "
                                + String.format("%.2f €", item.getEffectivePrice()) + " geändert",
                        2000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        // Menge und Gesamtpreis
        cartGrid.addColumn(CartItem::getQuantity).setHeader("Menge").setAutoWidth(true);
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice() * item.getQuantity()))
                .setHeader("Gesamtpreis").setAutoWidth(true);

        // Entfernen-Button
        cartGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.getElement().setProperty("title", "Artikel entfernen");
            removeButton.addClickListener(e -> removeFromCart(item.getArticle().getArticleNumber()));
            return removeButton;
        }).setHeader("Löschen")
        .setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        cartGrid.setWidthFull();
        cartGrid.getStyle().set("max-height", "50vh"); // Maximale Höhe: halbe Bildschirmhöhe
        cartGrid.getStyle().set("overflow-y", "auto"); // Scrollbar, wenn zu viele Einträge vorhanden sind

        // Gesamtpreis + Gesamtanzahl Anzeige
        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        // Artikelbeschreibung Initialisierung
        descriptionOutputField = new TextArea("Artikelbeschreibung"); // Titel angepasst
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidth("965px"); // Feste Breite für das Beschreibungsfeld
        descriptionOutputField.setHeight("60px"); // Höhe automatisch an Inhalt anpassen
        descriptionOutputField.setVisible(false); // Initial unsichtbar

        // Sucheingabe Validierung
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px"); // Feste Breite für das Suchfeld
        searchButton.setEnabled(false);
        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            boolean validInput = value.matches("^(A-\\d+|\\d+)$"); // Akzeptiert A-123 oder 123
            searchButton.setEnabled(validInput);
            if (!validInput && !value.isEmpty()) {
                errorLabel.setText("Eingabe muss in Form von 'A-XXXX' oder 'XXXX' sein");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false); // Unsichtbar bei ungültiger Eingabe
                articleGrid.setItems(Collections.emptyList()); // Grid leeren
                articleGrid.setVisible(false); // Unsichtbar bei ungültiger Eingabe
            } else {
                errorLabel.setText("");
            }
        });

        // Enter-Taste Listener
        searchField.addKeyPressListener(Key.ENTER, event -> {
            if (searchButton.isEnabled()) {
                searchButton.click();
            }
        });

        // Suchlogik (Artikelabfrage aus DB)
        searchButton.addClickListener(event -> {
            String input = searchField.getValue().trim();
            if (input.isEmpty()) {
                errorLabel.setText("Eingabe darf nicht leer sein");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false); // Unsichtbar bei leerer Eingabe
                articleGrid.setItems(Collections.emptyList()); // Grid leeren
                articleGrid.setVisible(false); // Unsichtbar bei leerer Eingabe
                return;
            }
            input = input.matches("\\d+") ? "A-" + input : input; // Transform XXXX -> A-XXXX

            Optional<Article> article = articleService.findByArticleNumber(input);
            if (article.isPresent()) {
                searchField.clear();
                errorLabel.setText("");
                descriptionOutputField.setValue(article.get().getDescription());
                descriptionOutputField.setVisible(true); // Sichtbar bei gefundenem Artikel
                articleGrid.setItems(Collections.singletonList(article.get()));
                articleGrid.setVisible(true); // Sichtbar bei gefundenem Artikel
            } else {
                errorLabel.setText("Artikel nicht gefunden"); // Fehlermeldung in errorLabel
                descriptionOutputField.clear(); // Beschreibung leeren
                descriptionOutputField.setVisible(false); // Unsichtbar bei nicht gefundenem Artikel
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false); // Unsichtbar bei nicht gefundenem Artikel
            }
        });

        errorLabel.getStyle().set("color", "red");
        errorLabel.setWidthFull();

        // --- Layout ---
        HorizontalLayout searchInputAndDescriptionLayout = new HorizontalLayout(searchField, searchButton, descriptionOutputField);
        searchInputAndDescriptionLayout.setAlignItems(Alignment.END);

        // Warenkorb-Bereich mit Gesamtanzeige
        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setWidthFull();
        cartSection.setPadding(false);
        cartSection.setSpacing(true);
        cartSection.setAlignItems(Alignment.STRETCH);

        // Hauptlayout
        VerticalLayout mainLayout = new VerticalLayout(
                searchInputAndDescriptionLayout,
                errorLabel,
                articleGrid,
                cartSection
        );

        mainLayout.setWidthFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }

    // Hilfsklasse für Warenkorb
    private static class CartItem {

        private int position;
        private final Article article;
        private int quantity;
        private Double overriddenPrice; // null = kein manueller Preis

        public CartItem(int position, Article article, int quantity) {
            this.position = position;
            this.article = article;
            this.quantity = quantity;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Article getArticle() {
            return article;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Double getOverriddenPrice() {
            return overriddenPrice;
        }

        public void setOverriddenPrice(Double overriddenPrice) {
            this.overriddenPrice = overriddenPrice;
        }

        public double getEffectivePrice() {
            return overriddenPrice != null ? overriddenPrice : article.getSellingPrice();
        }

        public boolean isPriceOverridden() {
            return overriddenPrice != null;
        }
    }

    // Artikel hinzufügen (nach Artikelnummer zusammenfassen)
    private void addToCart(Article article) {
        String key = article.getArticleNumber();
        if (cartItems.containsKey(key)) {
            CartItem existing = cartItems.get(key);
            // Menge direkt erhöhen (nicht neues Objekt erzeugen!)
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cartItems.put(key, new CartItem(cartItems.size() + 1, article, 1));
        }

        updateCartGrid();

        // Visuelle Rückmeldung
        Notification notification = Notification.show(
                article.getName() + " wurde dem Warenkorb hinzugefügt", 2000,
                Notification.Position.MIDDLE
        );
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // Artikel entfernen oder Menge reduzieren
    private void removeFromCart(String articleNumber) {
        CartItem existing = cartItems.get(articleNumber);
        if (existing != null) {
            if (existing.getQuantity() > 1) {
                // Menge einfach um 1 verringern
                existing.setQuantity(existing.getQuantity() - 1);
            } else {
                // Wenn nur noch 1 vorhanden war → ganz entfernen
                cartItems.remove(articleNumber);
            }
            updateCartGrid();
        }
    }

    // Warenkorb aktualisieren + Gesamtwerte berechnen
    private void updateCartGrid() {
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