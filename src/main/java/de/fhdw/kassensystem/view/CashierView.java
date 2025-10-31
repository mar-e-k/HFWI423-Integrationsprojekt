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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

    private int nextPosition = 1;
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
        articleGrid.addColumn(Article::getName).setHeader("Artikelname");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer");
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis");
        articleGrid.addColumn(article -> article.getStockLevel() + " Stück").setHeader("Lagerbestand");
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar");
        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz");

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
        cartGrid.addColumn(CartItem::position).setHeader("Pos.").setAutoWidth(true);
        cartGrid.addColumn(item -> item.article().getName()).setHeader("Artikelname");
        cartGrid.addColumn(item -> item.article().getArticleNumber()).setHeader("Artikelnummer");
        cartGrid.addColumn(item -> String.format("%.2f €", item.article().getSellingPrice()))
                .setHeader("Stückpreis");
        cartGrid.addColumn(CartItem::quantity).setHeader("Menge");
        cartGrid.addColumn(item -> String.format("%.2f €", item.article().getSellingPrice() * item.quantity()))
                .setHeader("Gesamtpreis");

        // Entfernen-Button für Warenkorb
        cartGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.getElement().setProperty("title", "Artikel entfernen");
            removeButton.addClickListener(e -> removeFromCart(item.article().getArticleNumber())); // Entfernt Artikel oder reduziert Menge
            return removeButton;
        }).setHeader("");

        cartGrid.setWidthFull();
        cartGrid.getStyle().set("max-height", "50vh"); // Maximale Höhe: halbe Bildschirmhöhe
        cartGrid.getStyle().set("overflow-y", "auto"); // Scrollbar, wenn zu viele Einträge vorhanden sind

        // Gesamtpreis + Gesamtanzahl Anzeige
        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        // Artikelbeschreibung Initialisierung
        descriptionOutputField = new TextArea("Artikelbeschreibung");
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidthFull(); // Passt sich der verfügbaren Breite an
        descriptionOutputField.setMinHeight("30px");
        descriptionOutputField.setMaxHeight("60px"); // Beschränkung auf sinnvolle Höhe
        descriptionOutputField.getStyle().set("resize", "none");
        descriptionOutputField.getStyle().set("white-space", "normal");
        descriptionOutputField.getStyle().set("font-size", "var(--lumo-font-size-s)");
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
        cartSection.setPadding(true);
        cartSection.setSpacing(false);
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

    // interne Hilfsklasse für Warenkorb
    private record CartItem(int position, Article article, int quantity) {}

    // Artikel hinzufügen (nach Artikelnummer zusammenfassen)
    private void addToCart(Article article) {
        String key = article.getArticleNumber();
        if (cartItems.containsKey(key)) {
            CartItem existing = cartItems.get(key);
            CartItem updated = new CartItem(
                    existing.position(),
                    article,
                    existing.quantity() + 1
            );
            cartItems.put(key, updated);
        } else {
            cartItems.put(key, new CartItem(nextPosition++, article, 1));
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
            if (existing.quantity() > 1) {
                cartItems.put(articleNumber, new CartItem(
                        existing.position(),
                        existing.article(),
                        existing.quantity() - 1
                ));
            } else {
                cartItems.remove(articleNumber); // Entfernt komplett bei letzter Einheit
            }
            updateCartGrid();
        }
    }

    // Warenkorb aktualisieren + Gesamtwerte berechnen
    private void updateCartGrid() {
        List<CartItem> items = cartItems.values().stream()
                .sorted(Comparator.comparingInt(CartItem::position))
                .toList();
        cartGrid.setItems(items);

        int totalQuantity = items.stream().mapToInt(CartItem::quantity).sum();
        double totalPrice = items.stream()
                .mapToDouble(item -> item.article().getSellingPrice() * item.quantity())
                .sum();

        totalLabel.setText(String.format("Gesamtanzahl: %d | Gesamtpreis: %.2f €", totalQuantity, totalPrice));
    }
}
