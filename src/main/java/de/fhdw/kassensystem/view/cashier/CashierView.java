package de.fhdw.kassensystem.view.cashier;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.fhdw.kassensystem.persistence.entity.AccountRoleEnum;
import de.fhdw.kassensystem.persistence.entity.imported.Article;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import de.fhdw.kassensystem.view.BaseView;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.*;

@Route("/cashier")
@PageTitle("Cashier View")
@CssImport("./styles/styles.css")
@RolesAllowed({AccountRoleEnum.ROLE_CASHIER, AccountRoleEnum.ROLE_ADMIN})
public class CashierView extends BaseView implements BeforeEnterObserver {

    private final ArticleService articleService;
    private final CartItemsManager cartItemsManager;

    private Grid<Article> articleGrid;
    private Grid<CartItem> cartGrid;

    private TextArea descriptionOutputField;
    private TextField priceEditor;
    private IntegerField quantityEditor;
    private Span totalLabel;

    @Value("${spring.kassensystem.cashier.password}")
    private String password;

    public CashierView(ArticleService articleService, CartItemsManager cartItemsManager) {
        this.articleService = articleService;
        this.cartItemsManager = cartItemsManager;
    }

    @Override
    protected String setTopbarTitle() {
        return "Kassen-Dashboard";
    }

    @Override
    protected HorizontalLayout createTopBarButtons() {
        Button paymentButton = new Button("Kauf abschließen");
        paymentButton.addClickListener(e -> {
            if (cartItemsManager.getCart().isEmpty()) {
                Notification.show("Der Warenkorb ist leer.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                UI.getCurrent().navigate(PaymentView.class);
            }
        });
        return new HorizontalLayout(paymentButton);
    }

    @Override
    protected void init() {
        // Komponenten
        TextField searchField = new TextField("Artikelnummer");
        Button searchButton = new Button("Suchen", new Icon(VaadinIcon.SEARCH));
        Span errorLabel = new Span();

        // Grid Initialisierung (Artikelanzeige)
        articleGrid = new Grid<>(Article.class, false);
        articleGrid.setHeight("auto");
        articleGrid.setAllRowsVisible(true);
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false);

        // Grid-Spalten Definierung
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar").setWidth("70px");
        articleGrid.addColumn(Article::getName).setHeader("Artikelname").setWidth("200px");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer").setAutoWidth(true);
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis").setAutoWidth(true);

        // Spalte für Lagerbestand mit Warnung
        articleGrid.addComponentColumn(article -> {
            Span stockLabel = new Span(article.getStockLevel() + " Stück");
            HorizontalLayout layout = new HorizontalLayout(stockLabel);
            layout.setAlignItems(Alignment.CENTER);
            if (article.getStockLevel() < 5) {
                Icon warningIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O);
                warningIcon.setColor("orange");
                warningIcon.setTooltipText("Geringer Bestand");
                layout.add(warningIcon);
            }
            return layout;
        }).setHeader("Lagerbestand").setAutoWidth(true);

        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz").setAutoWidth(true);

        // Hinzufügen-Button (für Warenkorb)
        articleGrid.addComponentColumn(article -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.getElement().setProperty("title", "Zum Warenkorb hinzufügen");
            addButton.addClickListener(e -> addToCart(article));
            addButton.setEnabled(article.getIsAvailable());
            return addButton;
        }).setHeader("Hinzufügen").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        // Warenkorb-Grid Initialisierung
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::getPosition).setHeader("Pos.").setAutoWidth(true);
        cartGrid.addColumn(item -> item.getArticle().getName()).setHeader("Artikelname").setWidth("200px");
        cartGrid.addColumn(item -> item.getArticle().getArticleNumber()).setHeader("Artikelnummer").setAutoWidth(true);

        // Editor für Preis- und Mengenänderung
        var editor = cartGrid.getEditor();
        var binder = new Binder<>(CartItem.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        // Editor für Preis
        priceEditor = new TextField();
        priceEditor.setSuffixComponent(new Span("€"));
        priceEditor.setWidth("100px");
        priceEditor.getStyle().set("text-align", "right");

        // Validator abhängig davon, ob Artikel schon einen Verkaufspreis hat
        binder.forField(priceEditor)
                // Null im Modell -> "" im Textfeld
                .withNullRepresentation("")
                .withConverter(new StringToBigDecimalConverter("Bitte eine gültige Zahl eingeben"))
                .withValidator(price -> {
                    CartItem current = editor.getItem();
                    if (current == null) return true;

                    // Preis MUSS gesetzt sein
                    if (price == null) {
                        return false;
                    }

                    // Preis darf NICHT 0 sein
                    if (price.compareTo(BigDecimal.ZERO) == 0) {
                        return false;
                    }

                    // Preis muss > 0 sein
                    return price.compareTo(BigDecimal.ZERO) > 0;
                }, "Preis muss größer als 0 sein.")
                .bind(CartItem::getOverriddenPrice, CartItem::setOverriddenPrice);


        // Automatisches Speichern bei Enter oder Verlassen des Feldes (Blur)
        priceEditor.getElement().addEventListener("blur", e -> {
            if (editor.isOpen()) editor.save();
        });
        priceEditor.addKeyDownListener(Key.ENTER, e -> {
            if (editor.isOpen()) editor.save();
        });

        Grid.Column<CartItem> priceColumn = cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice()))
                .setHeader("Stückpreis")
                .setAutoWidth(true)
                .setEditorComponent(priceEditor)
                .setKey("price");

        // Editor für Menge
        quantityEditor = new IntegerField();
        quantityEditor.setWidth("80px");
        binder.forField(quantityEditor).bind(CartItem::getQuantity, CartItem::setQuantity);

        // Automatisches Speichern bei Enter oder Verlassen des Feldes (Blur)
        quantityEditor.getElement().addEventListener("blur", e -> {
            if (editor.isOpen()) editor.save();
        });
        quantityEditor.addKeyDownListener(Key.ENTER, e -> {
            if (editor.isOpen()) editor.save();
        });

        Grid.Column<CartItem> quantityColumn = cartGrid.addColumn(CartItem::getQuantity)
                .setHeader("Menge")
                .setAutoWidth(true)
                .setEditorComponent(quantityEditor)
                .setKey("quantity");

        // Gesamtpreis
        cartGrid.addColumn(item -> String.format("%.2f €", item.getEffectivePrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .setHeader("Gesamtpreis").setAutoWidth(true);

        // Klick-Listener für die Zellen
        cartGrid.addItemClickListener(event -> {
            // Wenn der Editor für ein anderes Element geöffnet ist, speichern Sie zuerst.
            if (editor.isOpen() && !editor.getItem().equals(event.getItem())) {
                editor.save();
            }

            CartItem item = event.getItem();
            if (item == null) {
                return; // Klick auf Header/Footer ignorieren
            }

            if (!editor.isOpen()) {
                String columnKey = event.getColumn().getKey();
                if ("price".equals(columnKey)) {
                    boolean hasSellingPrice = item.getArticle().getSellingPrice() != null;
                    if (hasSellingPrice) {
                        // Artikel mit bestehendem Verkaufspreis -> Passwort nötig
                        showPasswordDialogForPriceChange(item);
                    } else {
                        // Artikel ohne Verkaufspreis -> Kassierer darf direkt Preis setzen
                        editor.editItem(item);
                        priceEditor.setReadOnly(false);
                        quantityEditor.setReadOnly(true);
                        BigDecimal currentPrice = item.getOverriddenPrice();
                        priceEditor.setValue(currentPrice != null ? currentPrice.toPlainString() : "");
                        priceEditor.focus();
                    }
                } else if ("quantity".equals(columnKey)) {
                    editor.editItem(item);
                    priceEditor.setReadOnly(true);
                    quantityEditor.setReadOnly(false);
                    quantityEditor.focus();
                }
            }
        });

        // Speicher-Listener für den Editor
        editor.addSaveListener(event -> {
            CartItem item = event.getItem();
            if (item.getQuantity() <= 0) {
                item.setQuantity(1); // Setzt auf 1 zurück, wenn ungültig
                Notification.show("Menge muss größer als 0 sein.", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            cartItemsManager.updateGrid(cartGrid, totalLabel); // Aktualisiert das Grid und die Summen
            editor.cancel(); // Schließt den Editor nach dem Speichern
        });

        editor.addCancelListener(e -> {
            priceEditor.setReadOnly(true);
            quantityEditor.setReadOnly(true);
        });

        // Entfernen-Button
        cartGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.getElement().setProperty("title", "Artikel entfernen");
            removeButton.addClickListener(e -> showDeleteDialog(item));
            return removeButton;
        }).setHeader("Löschen").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        cartGrid.setWidthFull();
        cartGrid.getStyle().set("max-height", "50vh");
        cartGrid.getStyle().set("overflow-y", "auto");

        totalLabel = new Span("Gesamtanzahl: 0 | Gesamtpreis: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        descriptionOutputField = new TextArea("Artikelbeschreibung");
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidth("965px");
        descriptionOutputField.setHeight("60px");
        descriptionOutputField.setVisible(false);

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
            if (searchButton.isEnabled()) {
                searchButton.click();
            }
        });

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

            Optional<Article> article = articleService.findByArticleNumber(input);
            if (article.isPresent()) {
                Double price = article.get().getSellingPrice();

                // Nur negative Preise blocken – null ist erlaubt (Kassierer kann später setzen)
                if (price != null && price < 0) {
                    errorLabel.setText("Artikel hat einen ungültigen (negativen) Verkaufspreis");
                    descriptionOutputField.clear();
                    descriptionOutputField.setVisible(false);
                    articleGrid.setItems(Collections.emptyList());
                    articleGrid.setVisible(false);
                    return;
                }

                searchField.clear();
                errorLabel.setText("");
                descriptionOutputField.setValue(article.get().getDescription());
                descriptionOutputField.setVisible(true);
                articleGrid.setItems(Collections.singletonList(article.get()));
                articleGrid.setVisible(true);
                articleGrid.getElement().executeJs(
                        "this.classList.remove('fade-in');" +
                                "void this.offsetWidth;" +
                                "this.classList.add('fade-in');"
                );
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

        HorizontalLayout searchInputAndDescriptionLayout = new HorizontalLayout(searchField, searchButton, descriptionOutputField);
        searchInputAndDescriptionLayout.setAlignItems(Alignment.END);

        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setWidthFull();
        cartSection.setPadding(false);
        cartSection.setSpacing(true);
        cartSection.setAlignItems(Alignment.STRETCH);

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

    private void showPasswordDialogForPriceChange(CartItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Preisänderung-Freigabe");

        PasswordField passwordField = new PasswordField("Passwort");
        passwordField.setRequired(true);

        dialog.add(passwordField);

        Button confirmButton = new Button("Bestätigen", e -> {
            if (passwordField.getValue().equals(password)) {
                dialog.close();
                cartGrid.getEditor().editItem(item);
                priceEditor.setReadOnly(false);
                quantityEditor.setReadOnly(true);
                BigDecimal currentPrice = item.getOverriddenPrice();
                priceEditor.setValue(currentPrice != null
                        ? String.format("%.2f", currentPrice)
                        : String.format("%.2f", item.getArticle().getSellingPrice()));
                priceEditor.focus();
            } else {
                Notification.show("Falsches Passwort!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        passwordField.addKeyPressListener(Key.ENTER, e -> confirmButton.click());

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
        passwordField.focus();
    }

    private void showDeleteDialog(CartItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Artikel '" + item.getArticle().getName() + "' löschen");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.STRETCH);

        Button deleteAllButton = new Button("Ganze Position löschen (" + item.getQuantity() + " Stück)", e -> {
            removeCartItemCompletely(item.getArticle().getArticleNumber());
            dialog.close();
        });

        dialogLayout.add(deleteAllButton);

        if (item.getQuantity() > 1) {
            HorizontalLayout quantityLayout = new HorizontalLayout();
            quantityLayout.setAlignItems(Alignment.BASELINE);
            IntegerField quantityField = new IntegerField("Menge zum Löschen");
            quantityField.setMin(1);
            quantityField.setMax(item.getQuantity());
            quantityField.setValue(1);

            Button deleteQuantityButton = new Button("Menge löschen", e -> {
                Integer quantityToRemove = quantityField.getValue();
                if (quantityToRemove != null && quantityToRemove > 0 && quantityToRemove <= item.getQuantity()) {
                    reduceCartItemQuantity(item.getArticle().getArticleNumber(), quantityToRemove);
                    dialog.close();
                } else {
                    Notification.show("Ungültige Menge. Bitte geben Sie eine Zahl zwischen 1 und " + item.getQuantity() + " ein.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            quantityLayout.add(quantityField, deleteQuantityButton);
            dialogLayout.add(quantityLayout);
        }

        dialog.add(dialogLayout);

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());
        dialog.getFooter().add(cancelButton);

        dialog.open();
    }

    private void removeCartItemCompletely(String articleNumber) {
        CartItem removedItem = cartItemsManager.getCart().stream()
                .filter(item -> item.getArticle().getArticleNumber().equals(articleNumber))
                .findFirst()
                .orElse(null);

        if (removedItem != null) {
            cartItemsManager.getCart().remove(removedItem);
            cartItemsManager.updateGrid(cartGrid, totalLabel);
            Notification.show(
                    "Position '" + removedItem.getArticle().getName() + "' wurde entfernt.",
                    2000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }

    private void reduceCartItemQuantity(String articleNumber, int quantityToRemove) {
        cartItemsManager.getCart().stream()
                .filter(item -> item.getArticle().getArticleNumber().equals(articleNumber))
                .findFirst()
                .ifPresent(item -> {
                    int newQuantity = item.getQuantity() - quantityToRemove;

                    if (newQuantity > 0) {
                        item.setQuantity(newQuantity);
                    } else {
                        cartItemsManager.getCart().remove(item);
                    }

                    cartItemsManager.updateGrid(cartGrid, totalLabel);
                });
    }

    private void addToCart(Article article) {
        List<CartItem> items = cartItemsManager.getCart();
        String articleNumber = article.getArticleNumber();

        CartItem existing = items.stream()
                .filter(item -> item.getArticle().getArticleNumber().equals(articleNumber))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            // purchasePrice kann null sein -> null-sicher behandeln
            Double purchasePrice = article.getPurchasePrice();
            BigDecimal overriddenPrice = purchasePrice != null
                    ? BigDecimal.valueOf(purchasePrice)
                    : null; // Kassierer kann später einen Preis setzen

            items.add(new CartItem(article, items.size() + 1, 1, overriddenPrice));
        }

        cartItemsManager.updateGrid(cartGrid, totalLabel);

        Notification.show(
                article.getName() + " wurde dem Warenkorb hinzugefügt",
                2000,
                Notification.Position.MIDDLE
        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!cartItemsManager.getCart().isEmpty()) {
            cartItemsManager.updateGrid(cartGrid, totalLabel);
        }
    }
}