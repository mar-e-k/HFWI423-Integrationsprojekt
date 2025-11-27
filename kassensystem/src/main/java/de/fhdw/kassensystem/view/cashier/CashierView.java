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
import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.commons.persistence.entity.AccountRoleEnum;
import de.fhdw.commons.view.AbstractMainView;
import de.fhdw.kassensystem.rest.proxy.services.ArticleProxyService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Route("/cashier")
@PageTitle("Cashier View")
@CssImport("./styles/styles.css")
@RolesAllowed({AccountRoleEnum.ROLE_CASHIER})
public class CashierView extends AbstractMainView implements BeforeEnterObserver {

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private final ArticleProxyService articleService;
    private final CartItemsManager cartItemsManager;

    private Grid<ArticleDTO> articleGrid;
    private Grid<CartItem> cartGrid;

    private TextArea descriptionOutputField;
    private TextField priceEditor;
    private IntegerField quantityEditor;
    private Span totalLabel;

    @Value("${spring.kassensystem.cashier.password}")
    private String password;

    public CashierView(ArticleProxyService articleService, CartItemsManager cartItemsManager) {
        this.articleService = articleService;
        this.cartItemsManager = cartItemsManager;
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
        articleGrid = new Grid<>(ArticleDTO.class, false);
        articleGrid.setHeight("auto");
        articleGrid.setAllRowsVisible(true);
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false);

        // Grid-Spalten Definierung
        articleGrid.addColumn(article -> article.getAvailable() ? "ja" : "nein").setHeader("Verfügbar").setWidth("30px");
        articleGrid.addColumn(ArticleDTO::getName).setHeader("Artikelname").setWidth("150px");
        articleGrid.addColumn(ArticleDTO::getArticleNumber).setHeader("Artikelnummer").setWidth("90px");
        articleGrid.addColumn(article -> {
            Double sellingPrice = article.getSellingPrice();
            return sellingPrice == null ? "kein Verkaufspreis" : String.format("%.2f €", sellingPrice);
        }).setHeader("Verkaufspreis").setWidth("60px");

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
        }).setHeader("Lagerbestand").setWidth("60px");

        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz").setAutoWidth(true);

        // Hinzufügen-Button (für Warenkorb)
        articleGrid.addComponentColumn(article -> {
            Button addButton = new Button(new Icon(VaadinIcon.PLUS));
            addButton.getElement().setProperty("title", "Zum Warenkorb hinzufügen");
            addButton.addClickListener(e -> addToCart(article));
            addButton.setEnabled(article.getAvailable());
            return addButton;
        }).setHeader("Hinzufügen").setAutoWidth(true).setTextAlign(ColumnTextAlign.END);

        // Warenkorb-Grid Initialisierung
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::getPosition).setHeader("Pos.").setWidth("70px");
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
                    if (editor.getItem() == null) return true;

                    if (price == null) {
                        return false;
                    }

                    return price.compareTo(MIN_PRICE) >= 0;
                }, "Preis muss mindestens 0,01 € betragen.")
                .bind(CartItem::getOverriddenPrice, CartItem::setOverriddenPrice);

        // Automatisches Speichern bei Enter oder Verlassen des Feldes (Blur)
        priceEditor.getElement().addEventListener("blur", e -> {
            if (editor.isOpen()) editor.save();
        });
        priceEditor.addKeyDownListener(Key.ENTER, e -> {
            if (editor.isOpen()) editor.save();
        });

        // Stückpreis-Spalte mit vorher/nachher Anzeige (bei Rabatt)
        cartGrid.addComponentColumn(item -> {
                    Span container = new Span();

                    BigDecimal base = item.getBaseUnitPrice();
                    BigDecimal discounted = item.getDiscountedUnitPrice();

                    if (item.hasDiscount() && item.getDiscountedQuantity() != null
                            && item.getDiscountedQuantity() >= item.getQuantity()) {
                        // gesamte Menge rabattiert -> klar vorher/nachher anzeigen
                        Span oldPrice = new Span(String.format("%.2f €", base));
                        oldPrice.getStyle().set("text-decoration", "line-through");

                        Span arrow = new Span(" → ");
                        Span newPrice = new Span(String.format("%.2f €", discounted));

                        container.add(oldPrice, arrow, newPrice);
                    } else if (item.hasDiscount()) {
                        // nur Teilmenge rabattiert -> kurze Info
                        Span baseSpan = new Span(String.format("%.2f €", base) + " / ");
                        Span discSpan = new Span(String.format("%.2f €", discounted) +
                                " (" + item.getDiscountedQuantity() + "x)");
                        container.add(baseSpan, discSpan);
                    } else {
                        container.setText(String.format("%.2f €", base));
                    }

                    return container;
                })
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

        // Gesamtpreis (mit Rabatt berücksichtigt)
        cartGrid.addColumn(item -> String.format("%.2f €", item.getTotalPriceWithDiscount()))
                .setHeader("Gesamtpreis")
                .setAutoWidth(true);

        // Rabatt-Button pro Position (zwischen Gesamtpreis und Löschen)
        cartGrid.addComponentColumn(item -> {
                    Button discountButton = new Button("Rabatt");
                    discountButton.getElement().setProperty("title", "Rabatt für diese Position festlegen");

                    discountButton.addClickListener(e -> {
                        BigDecimal defaultPercent = item.getDiscountPercent() != null
                                ? item.getDiscountPercent()
                                : new BigDecimal("30");
                        showDiscountDialog(item, defaultPercent);
                    });

                    return discountButton;
                }).setHeader("Rabatt")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

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

            Optional<ArticleDTO> article = articleService.findByArticleNumber(input);
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

    /**
     * Dialog zur Bearbeitung des Rabatts:
     * - Prozentsatz
     * - Menge der rabattierten Artikel
     * - Entfernen des Rabatts
     */
    private void showDiscountDialog(CartItem item, BigDecimal defaultPercent) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rabatt für '" + item.getArticle().getName() + "'");

        IntegerField percentField = new IntegerField("Rabatt in %");
        percentField.setMin(0);
        percentField.setMax(100);
        percentField.setStepButtonsVisible(true);

        IntegerField quantityField = new IntegerField("Menge mit Rabatt");
        quantityField.setMin(1);
        quantityField.setMax(item.getQuantity());
        quantityField.setStepButtonsVisible(true);

        // Vorbelegung
        if (item.getDiscountPercent() != null) {
            percentField.setValue(item.getDiscountPercent().intValue());
        } else {
            percentField.setValue(defaultPercent.intValue());
        }

        if (item.getDiscountedQuantity() != null) {
            quantityField.setValue(item.getDiscountedQuantity());
        } else {
            quantityField.setValue(item.getQuantity());
        }

        VerticalLayout layout = new VerticalLayout(percentField, quantityField);
        layout.setPadding(false);
        dialog.add(layout);

        Button removeButton = new Button("Rabatt entfernen", e -> {
            item.clearDiscount();
            cartItemsManager.updateGrid(cartGrid, totalLabel);
            dialog.close();
        });

        Button confirmButton = new Button("Anwenden", e -> {
            Integer percent = percentField.getValue();
            Integer qty = quantityField.getValue();

            if (percent == null || percent < 0 || percent > 100) {
                Notification.show("Bitte einen gültigen Prozentsatz zwischen 0 und 100 eingeben.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (qty == null || qty < 1 || qty > item.getQuantity()) {
                Notification.show("Menge muss zwischen 1 und " + item.getQuantity() + " liegen.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (percent == 0) {
                item.clearDiscount();
            } else {
                item.setDiscountPercent(BigDecimal.valueOf(percent));
                item.setDiscountedQuantity(qty);
            }

            cartItemsManager.updateGrid(cartGrid, totalLabel);
            dialog.close();
        });

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        percentField.addKeyPressListener(Key.ENTER, e -> confirmButton.click());
        quantityField.addKeyPressListener(Key.ENTER, e -> confirmButton.click());

        dialog.getFooter().add(removeButton, cancelButton, confirmButton);
        dialog.open();
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

    private void addToCart(ArticleDTO article) {
        // Wenn der Artikel keinen Verkaufspreis hat, muss der Kassierer einen eingeben
        if (article.getSellingPrice() == null) {
            showInitialPriceDialog(article);
            return;
        }

        List<CartItem> items = cartItemsManager.getCart();
        String articleNumber = article.getArticleNumber();

        CartItem existing = items.stream()
                .filter(item -> item.getArticle().getArticleNumber().equals(articleNumber))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            // sellingPrice kann null sein -> null-sicher behandeln
            Double sellingPrice = article.getSellingPrice();
            BigDecimal overriddenPrice = sellingPrice != null
                    ? BigDecimal.valueOf(sellingPrice)
                    : null; // Kassierer kann später bei Bedarf überschreiben

            items.add(new CartItem(article, items.size() + 1, 1, overriddenPrice));
        }

        cartItemsManager.updateGrid(cartGrid, totalLabel);

        Notification.show(
                article.getName() + " wurde dem Warenkorb hinzugefügt",
                2000,
                Notification.Position.MIDDLE
        ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showInitialPriceDialog(ArticleDTO article) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Preis für '" + article.getName() + "' festlegen");

        TextField priceField = new TextField("Verkaufspreis");
        priceField.setSuffixComponent(new Span("€"));
        priceField.setWidth("150px");

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        Button confirmButton = new Button("Übernehmen", e -> {
            String value = priceField.getValue();
            if (value == null || value.trim().isEmpty()) {
                Notification.show("Bitte einen Preis eingeben.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                // Komma oder Punkt erlauben
                String normalized = value.replace(",", ".").trim();
                BigDecimal price = new BigDecimal(normalized);

                if (price.compareTo(MIN_PRICE) < 0) {
                    Notification.show("Preis muss mindestens 0,01 € betragen.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // Artikel mit diesem Preis in den Warenkorb aufnehmen
                List<CartItem> items = cartItemsManager.getCart();
                CartItem newItem = new CartItem(article, items.size() + 1, 1, price);
                items.add(newItem);
                cartItemsManager.updateGrid(cartGrid, totalLabel);

                Notification.show(
                        article.getName() + " wurde dem Warenkorb hinzugefügt",
                        2000,
                        Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
            } catch (NumberFormatException ex) {
                Notification.show("Bitte einen gültigen Preis eingeben.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Enter im Feld löst Bestätigen aus
        priceField.addKeyPressListener(Key.ENTER, e -> confirmButton.click());

        dialog.add(priceField);
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
        priceField.focus();
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!cartItemsManager.getCart().isEmpty()) {
            cartItemsManager.updateGrid(cartGrid, totalLabel);
        }
    }
}
