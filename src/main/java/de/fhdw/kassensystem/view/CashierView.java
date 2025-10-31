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

    // Store items in cart using article number as key
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
        TextField searchField = new TextField("Artikelnummer");
        Button searchButton = new Button("Suchen", new Icon(VaadinIcon.SEARCH));
        Span errorLabel = new Span();

        // Article grid (search results)
        articleGrid = new Grid<>(Article.class, false);
        articleGrid.setSelectionMode(Grid.SelectionMode.NONE);
        articleGrid.setVisible(false);
        articleGrid.setWidthFull();
        articleGrid.setAllRowsVisible(true);

        // Columns
        articleGrid.addColumn(Article::getName).setHeader("Artikelname");
        articleGrid.addColumn(Article::getArticleNumber).setHeader("Artikelnummer");
        articleGrid.addColumn(article -> article.getSellingPrice() + " €").setHeader("Verkaufspreis");
        articleGrid.addColumn(article -> article.getStockLevel() + " Stück").setHeader("Lagerbestand");
        articleGrid.addColumn(article -> article.getIsAvailable() ? "ja" : "nein").setHeader("Verfügbar");
        articleGrid.addColumn(article -> article.getTaxRatePercent() + " %").setHeader("Steuersatz");

        // Add-to-cart button
        articleGrid.addComponentColumn(article -> {
                    Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                    addButton.getElement().setProperty("title", "Add to cart");
                    addButton.addClickListener(e -> addToCart(article));
                    addButton.setEnabled(article.getIsAvailable());
                    return addButton;
                }).setHeader("Add")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        // Cart grid
        cartGrid = new Grid<>(CartItem.class, false);
        cartGrid.addColumn(CartItem::position).setHeader("Pos.").setAutoWidth(true);
        cartGrid.addColumn(item -> item.article().getName()).setHeader("Article name");
        cartGrid.addColumn(item -> item.article().getArticleNumber()).setHeader("Article number");
        cartGrid.addColumn(item -> String.format("%.2f €", item.article().getSellingPrice()))
                .setHeader("Unit price");
        cartGrid.addColumn(CartItem::quantity).setHeader("Quantity");
        cartGrid.addColumn(item -> String.format("%.2f €", item.article().getSellingPrice() * item.quantity()))
                .setHeader("Total price");

        // Remove button
        cartGrid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.getElement().setProperty("title", "Remove article");
            removeButton.addClickListener(e -> removeFromCart(item.article().getArticleNumber()));
            return removeButton;
        }).setHeader("");

        cartGrid.setWidthFull();
        cartGrid.getStyle().set("max-height", "50vh");
        cartGrid.getStyle().set("overflow-y", "auto");

        // Total label (sum of prices and quantities)
        totalLabel = new Span("Total quantity: 0 | Total price: 0,00 €");
        totalLabel.getStyle().set("font-weight", "bold");

        // Article description box
        descriptionOutputField = new TextArea("Article description");
        descriptionOutputField.setReadOnly(true);
        descriptionOutputField.setWidthFull();
        descriptionOutputField.setMinHeight("30px");
        descriptionOutputField.setMaxHeight("60px");
        descriptionOutputField.getStyle().set("resize", "none");
        descriptionOutputField.getStyle().set("white-space", "normal");
        descriptionOutputField.getStyle().set("font-size", "var(--lumo-font-size-s)");
        descriptionOutputField.setVisible(false);

        // Search field behavior
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("250px");
        searchButton.setEnabled(false);
        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            boolean validInput = value.matches("^(A-\\d+|\\d+)$");
            searchButton.setEnabled(validInput);
            if (!validInput && !value.isEmpty()) {
                errorLabel.setText("Input must be in the form 'A-XXXX' or 'XXXX'");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false);
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false);
            } else {
                errorLabel.setText("");
            }
        });

        // ENTER key triggers search
        searchField.addKeyPressListener(Key.ENTER, event -> {
            if (searchButton.isEnabled()) searchButton.click();
        });

        // Search button logic
        searchButton.addClickListener(event -> {
            String input = searchField.getValue().trim();
            if (input.isEmpty()) {
                errorLabel.setText("Input cannot be empty");
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
                errorLabel.setText("Article not found");
                descriptionOutputField.clear();
                descriptionOutputField.setVisible(false);
                articleGrid.setItems(Collections.emptyList());
                articleGrid.setVisible(false);
            }
        });

        errorLabel.getStyle().set("color", "red");
        errorLabel.setWidthFull();

        // Layout: Search field + description
        HorizontalLayout searchInputAndDescriptionLayout =
                new HorizontalLayout(searchField, searchButton, descriptionOutputField);
        searchInputAndDescriptionLayout.setAlignItems(FlexComponent.Alignment.END);
        searchInputAndDescriptionLayout.setSpacing(true);
        searchInputAndDescriptionLayout.setWidthFull();

        // Cart section
        VerticalLayout cartSection = new VerticalLayout(cartGrid, totalLabel);
        cartSection.setWidthFull();
        cartSection.setPadding(true);
        cartSection.setSpacing(false);
        cartSection.setAlignItems(FlexComponent.Alignment.STRETCH);
        cartSection.getStyle().set("margin-top", "20px");

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(
                searchInputAndDescriptionLayout,
                errorLabel,
                articleGrid,
                cartSection
        );
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        mainLayout.setWidthFull();

        add(mainLayout);
    }

    // Simple record for items in the cart
    private record CartItem(int position, Article article, int quantity) {}

    // Add article to the cart (merge by article number)
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

        Notification notification = Notification.show(
                article.getName() + " was added to the cart", 2000,
                Notification.Position.MIDDLE
        );
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    // Remove one quantity from the cart, or remove the article completely if count is 0
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
                cartItems.remove(articleNumber);
            }
            updateCartGrid();
        }
    }

    // Refresh the cart grid and total label
    private void updateCartGrid() {
        List<CartItem> items = cartItems.values().stream()
                .sorted(Comparator.comparingInt(CartItem::position))
                .toList();
        cartGrid.setItems(items);

        int totalQuantity = items.stream().mapToInt(CartItem::quantity).sum();
        double totalPrice = items.stream()
                .mapToDouble(item -> item.article().getSellingPrice() * item.quantity())
                .sum();

        totalLabel.setText(String.format("Total quantity: %d | Total price: %.2f €", totalQuantity, totalPrice));
    }
}
