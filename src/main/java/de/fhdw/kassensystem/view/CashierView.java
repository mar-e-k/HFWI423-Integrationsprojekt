package de.fhdw.kassensystem.view;

import com.vaadin.flow.component.button.Button;
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

import java.util.Optional;

@Route("/cashier")
@RolesAllowed({Roles.Type.CASHIER, Roles.Type.ADMIN})
@PageTitle("Cashier View")
public class CashierView extends BaseView {

    private final ArticleService articleService;

    public CashierView(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    protected String setTopbarTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void init() {
        // --- Components ---
        TextField searchField = new TextField("Artikelnummer Eingabefeld");
        Button searchButton = new Button("Suchen", new Icon(VaadinIcon.SEARCH));
        TextArea outputField = new TextArea("Artikeldetails");
        Span errorLabel = new Span();

        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchButton.setEnabled(false);
        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            boolean validInput = value.matches("^(A-\\d+|\\d+)$");
            searchButton.setEnabled(validInput);

            if (!validInput && !value.isEmpty()) {
                errorLabel.setText("Eingabe muss in Form von 'A-XXXX' oder 'XXXX' sein");
            } else {
                errorLabel.setText("");
            }
        });

        searchButton.addClickListener(event -> {
            String input = searchField.getValue().trim();
            if (input.isEmpty()) {
                errorLabel.setText("Eingabe darf nicht leer sein");
                return;
            } else {
                input = input.matches("\\d+") ? "A-" + input : input; //Transform XXXX -> A-XXXX
            }
            Optional<Article> article = articleService.findByArticleNumber(input);
            if (article.isPresent()) {
                searchField.clear();
                errorLabel.setText("");
                outputField.setVisible(true);
                outputField.setValue(article.get().toString());
            } else {
                errorLabel.setText("Artikel nicht gefunden");
                outputField.clear();
                outputField.setVisible(false);
            }
        });

        outputField.setReadOnly(true);
        outputField.setVisible(false);
        outputField.setWidthFull();

        errorLabel.getStyle().set("color", "red");
        errorLabel.setWidthFull();

        // --- Layout ---
        HorizontalLayout searchLayout = new HorizontalLayout(searchField, searchButton);

        searchLayout.setAlignItems(Alignment.END);
        searchLayout.setWidthFull();
        searchField.setWidth("300px");
        searchButton.setWidth("120px");

        VerticalLayout mainLayout = new VerticalLayout(
                searchLayout,
                errorLabel,
                outputField
        );

        mainLayout.setWidthFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        add(mainLayout);
    }
}