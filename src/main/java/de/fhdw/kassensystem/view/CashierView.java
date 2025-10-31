package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import java.util.Collections;
import java.util.Optional;

@Route("/cashier")
@RolesAllowed({Roles.Type.CASHIER, Roles.Type.ADMIN})
@PageTitle("Cashier View")
public class CashierView extends BaseView {

    private final ArticleService articleService;
    private Grid<Article> articleGrid;
    private TextArea descriptionOutputField;

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

        // Grid Initialisierung
        articleGrid = new Grid<>(Article.class, false);
        articleGrid.setHeight("auto"); // Setzt die Höhe automatisch
        articleGrid.setAllRowsVisible(true); // Zeigt alle Zeilen an, ohne Scrollbalken
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false); // Initial unsichtbar

        // Artikelbeschreibung Initialisierung
        descriptionOutputField = new TextArea("Artikelbeschreibung"); // Titel angepasst
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidth("965px"); // Feste Breite für das Beschreibungsfeld
        descriptionOutputField.setHeight("60px"); // Höhe automatisch an Inhalt anpassen
        descriptionOutputField.setVisible(false); // Initial unsichtbar

        // Grid-Spalten Definierung
        articleGrid.addColumn(Article::getName).setHeader("Artikelname");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer");
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis");
        articleGrid.addColumn(article -> article.getStockLevel() + " Stück").setHeader("Lagerbestand");
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar");
        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz");

        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px"); // Feste Breite für das Suchfeld
        searchButton.setEnabled(false);
        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            boolean validInput = value.matches("^(A-\\d+|\\d+)$");
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
            input = input.matches("\\d+") ? "A-" + input : input; //Transform XXXX -> A-XXXX

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

        VerticalLayout mainLayout = new VerticalLayout(
                searchInputAndDescriptionLayout,
                errorLabel,
                articleGrid
        );

        mainLayout.setWidthFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }
}