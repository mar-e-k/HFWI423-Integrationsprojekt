package de.fhdw.vendix.pos.ui.register.receipt_view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.fhdw.vendix.pos.ui.register.CartService;
import de.fhdw.vendix.pos.ui.register.CartLine;
import de.fhdw.vendix.pos.ui.register.RegisterState;

import java.math.BigDecimal;

public class ReceiptList extends VerticalLayout {

    private final CartService cartService;
    private final Grid<CartLine> grid = new Grid<>(CartLine.class, false);
    private final RegisterState state;

    public ReceiptList(CartService cartService, RegisterState state) {
        this.cartService = cartService;
        this.state = state;

        setHeightFull();

        configureGrid();
        add(grid);

        refresh();
    }

    private void configureGrid() {

        grid.addColumn(this::renderIndexColumn)
                .setHeader("#")
                .setAutoWidth(true);
        grid.addColumn(this::renderName)
                .setHeader("Name")
                .setAutoWidth(true);
        grid.addColumn(this::renderAmount)
                .setHeader("Amount")
                .setAutoWidth(true);
        grid.addColumn(this::renderTax)
                .setHeader("Tax")
                .setAutoWidth(true);
        grid.addColumn(this::renderTotal)
                .setHeader("Total")
                .setAutoWidth(true);
        grid.asSingleSelect().addValueChangeListener(event ->
                state.setSelectedCartLine(event.getValue())
        );
        grid.setAllRowsVisible(true);
    }

    private String renderIndexColumn(CartLine line) {
        int index = cartService.getCartLines().indexOf(line) + 1;
        return String.valueOf(index);
    }

    private String renderName(CartLine line) {
        return line.article().name();
    }

    private Long renderAmount(CartLine line) {
        return line.line().articleAmount();
    }

    private BigDecimal renderTax(CartLine line) {
        return line.article().taxRate();
    }

    private Component renderTotal(CartLine line) {

        PriceResult price = line.calculatePrice();

        Span original = new Span(price.originalTotal().toString());
        Span finalPrice = new Span(price.finalTotal().toString());

        HorizontalLayout layout = new HorizontalLayout();

        if (price.overridden() || price.discounted()) {

            original.getStyle()
                    .set("text-decoration", "line-through")
                    .set("color", "gray");

            layout.add(original, finalPrice);

            if (price.discounted()) {
                finalPrice.getStyle().set("color", "green");
            }

            if (price.overridden()) {
                finalPrice.getStyle().set("font-weight", "bold");
            }

        } else {
            layout.add(finalPrice);
        }

        return layout;
    }
    public void refresh() {
        grid.setItems(cartService.getCartLines());
    }
}