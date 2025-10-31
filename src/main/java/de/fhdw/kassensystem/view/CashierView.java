package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.component.Key;

import java.util.*;

@Route("/cashier")
@RolesAllowed({Roles.Type.CASHIER, Roles.Type.ADMIN})
@PageTitle("Cashier View")
public class CashierView extends BaseView {

    private final ArticleService articleService;
    private Grid<Article> articleGrid;
    private Grid<CartItem> cartGrid;
    private TextArea descriptionOutputField;

    private final Map<Article, Integer> cartItems = new HashMap<>();

    // Label für Gesamtpreis & Artikelanzahl
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
        TextField searchField = new TextField("Artikelnummer");
        Button searchButton = new Button("Suchen", new Icon(VaadinIcon.SEARCH));
        Span errorLabel = new Span();

        // Artikel-Grid
        articleGrid = new Grid<>(Article.class, false);
        articleGrid.setHeight("auto");
        articleGrid.setAllRowsVisible(true);
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false);

        // Hinzufügen-Button
        articleGrid.addComponentColumn(article -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.getElement().setProperty("title", "Zum Warenkorb hinzufügen");
            addButton.addClickListener(e -> {
                cartItems.merge(article, 1, Integer::sum);
                updateCartGrid();
                Notification notification = Notification.show(
                        article.getName() + " wurde dem Warenkorb hinzugefügt", 2000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            addButton.setEnabled(article.getIsAvailable());
            return addButton;
        }).setHeader("Hinzufügen");

        // Warenkorb-Grid
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(item -> item.article().getName()).setHeader("Artikelname");
        cartGrid.addColumn(item -> item.article().getArticleNumber()).setHeader("Artikelnummer");
        cartGrid.addColumn(CartItem::quantity).setHeader("Menge");
        cartGrid.addColumn(item -> String.format("%.2f €", item.article().getSellingPrice() * item.quantity()))
                .setHeader("Gesamtpreis");

        //  Entfernen-Button
        cartGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.getElement().setProperty("title", "Artikel entfernen");
            removeButton.addClickListener(e -> {
                Article article = item.article();
                cartItems.computeIfPresent(article, (a, q) -> (q > 1) ? q - 1 : null);
                updateCartGrid();
            });
            return removeButton;
        }).setHeader("");

        cartGrid.setWidth("400px");
        cartGrid.setHeight("300px");
        cartGrid.setVisible(true);

        // Gesamtpreis + Anzahl
        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        descriptionOutputField = new TextArea("Artikelbeschreibung");
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidth("965px");
        descriptionOutputField.setHeight("60px");
        descriptionOutputField.setVisible(false);

        // Artikel-Grid Spalten
        articleGrid.addColumn(Article::getName).setHeader("Artikelname");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer");
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis");
        articleGrid.addColumn(article -> article.getStockLevel() + " Stück").setHeader("Lagerbestand");
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar");
        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz");

        // Suche
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchButton.setEnabled(false);
        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            boolean validInput = value.matches("^(A-\\d+|\\d+)$");
            searchButton.setEnabled(validInput);
            if (!validInput && !value.isEmpty()) {
                errorLabel.setText("Eingabe muss in Form von 'A-XXXX' oder 'XXXX' sein");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false);
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false);
            } else {
                errorLabel.setText("");
            }
        });

        searchField.addKeyPressListener(Key.ENTER, event -> {
            if (searchButton.isEnabled()) searchButton.click();
        });

        // Suchlogik
        searchButton.addClickListener(event -> {
            String input = searchField.getValue().trim();
            if (input.isEmpty()) {
                errorLabel.setText("Eingabe darf nicht leer sein");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false);
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false);
                return;
            }

            input = input.matches("\\d+") ? "A-" + input : input;
            Optional<Article> articleOpt = articleService.findByArticleNumber(input);

            if (articleOpt.isPresent()) {
                Article article = articleOpt.get();
                searchField.clear();
                errorLabel.setText("");
                descriptionOutputField.setValue(article.getDescription());
                descriptionOutputField.setVisible(true);
                articleGrid.setItems(Collections.singletonList(article));
                articleGrid.setVisible(true);
            } else {
                errorLabel.setText("Artikel nicht gefunden");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false);
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false);
            }
        });

        errorLabel.getStyle().set("color", "red");
        errorLabel.setWidthFull();

        // Layouts
        HorizontalLayout searchInputAndDescriptionLayout =
                new HorizontalLayout(searchField, searchButton, descriptionOutputField);
        searchInputAndDescriptionLayout.setAlignItems(Alignment.END);

        // Warenkorb + Gesamtanzeige
        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setPadding(false);
        cartSection.setSpacing(false);

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

    // interne Hilfsklasse
    private record CartItem(Article article, int quantity) {}

    // Warenkorb aktualisieren + Gesamtanzeige
    private void updateCartGrid() {
        List<CartItem> items = cartItems.entrySet().stream()
                .map(entry -> new CartItem(entry.getKey(), entry.getValue()))
                .toList();
        cartGrid.setItems(items);

        int totalQuantity = items.stream().mapToInt(CartItem::quantity).sum();
        double totalPrice = items.stream()
                .mapToDouble(item -> item.article().getSellingPrice() * item.quantity())
                .sum();

        totalLabel.setText(String.format("Gesamtanzahl: %d | Gesamtpreis: %.2f €", totalQuantity, totalPrice));
    }
}
