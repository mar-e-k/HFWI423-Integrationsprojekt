package de.fhdw.vendix.pos.ui.register;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;

import java.math.BigDecimal;

public class ReceiptView extends VerticalLayout {

    private final Cart cart;
    private final Grid<CartLine> grid = new Grid<>(CartLine.class, false);

    public ReceiptView(Cart cart) {
        this.cart = cart;

        setWidth("40%");
        setHeightFull();

        configureGrid();

        add(grid);
        refresh();
    }

    private void configureGrid() {

        grid.addColumn(new ComponentRenderer<>(line -> {
            int index = cart.getCartLines().indexOf(line) + 1;
            return new Span(String.valueOf(index));
        })).setHeader("#").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(cartLine -> {

            ArticleDTO article = cartLine.article();
            ReceiptLineDTO line = cartLine.line();

            BigDecimal basePrice = article.sellingPrice();
            BigDecimal finalPrice = basePrice;

            if (line.priceOverride() != null) {
                finalPrice = line.priceOverride().amount();
            }

            if (line.discountOverride() != null) {
                finalPrice = finalPrice.subtract(line.discountOverride().amount());
            }

            VerticalLayout wrapper = new VerticalLayout();
            wrapper.setPadding(false);
            wrapper.setSpacing(false);

            Span name = new Span(article.name());
            name.getStyle().set("font-weight", "bold");

            HorizontalLayout row = new HorizontalLayout();

            Span amount = new Span("x" + line.articleAmount());

            Span price = new Span("€" + finalPrice);

            if (finalPrice.compareTo(basePrice) != 0) {
                Span original = new Span("€" + basePrice);
                original.getStyle().set("text-decoration", "line-through");
                original.getStyle().set("color", "gray");

                row.add(amount, original, price);
            } else {
                row.add(amount, price);
            }

            wrapper.add(name, row);
            return wrapper;

        })).setHeader("Receipt").setFlexGrow(1);

        grid.setAllRowsVisible(true);
    }

    public void refresh() {
        grid.setItems(cart.getCartLines());
    }
}