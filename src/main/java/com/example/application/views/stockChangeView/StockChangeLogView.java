package com.example.application.views.stockChangeView;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.data.domain.Sort;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.services.StockChangeLogService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Stock Changes")
@Route("stock-changes")
@Menu(order = 3, icon = LineAwesomeIconUrl.HISTORY_SOLID)
@Uses(Icon.class)
public class StockChangeLogView extends Div {

    private final StockChangeLogService service;
    private final Grid<StockChangeLog> grid = new Grid<>(StockChangeLog.class, false);
    private final TextField numberFilter = new TextField();

    public StockChangeLogView(StockChangeLogService service) {
        this.service = service;
        setSizeFull();

        add(buildCard());
    }

    private Component buildCard() {
        // Toolbar
        numberFilter.setPlaceholder("Artikelnummer suchen...");
        numberFilter.setWidth("260px");
        numberFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(numberFilter);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle()
            .set("padding", "16px 20px")
            .set("border-bottom", "1px solid #e8edf5")
            .set("background", "linear-gradient(to right, #fafbff, #f8fafc)");

        // Grid
        grid.addColumn(StockChangeLog::getChangedAt).setHeader("Datum").setAutoWidth(true).setSortable(true);
        grid.addColumn(StockChangeLog::getArticleNumber).setHeader("Artikelnummer").setAutoWidth(true).setSortable(true);
        grid.addColumn(StockChangeLog::getArticleName).setHeader("Artikel").setFlexGrow(1);

        grid.addComponentColumn(item -> {
            String type = item.getChangeType() != null ? item.getChangeType().toString() : "-";
            Span badge = new Span(type);
            badge.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");
            String t = type.toUpperCase();
            if (t.contains("ADD") || t.contains("INCREASE") || t.contains("GOODS")) {
                badge.getStyle().set("background", "#dcfce7").set("color", "#16a34a");
            } else if (t.contains("REMOVE") || t.contains("DECREASE") || t.contains("PICK")) {
                badge.getStyle().set("background", "#fee2e2").set("color", "#dc2626");
            } else {
                badge.getStyle().set("background", "#f1f5f9").set("color", "#64748b");
            }
            return badge;
        }).setHeader("Typ").setAutoWidth(true);

        grid.addColumn(StockChangeLog::getOldStock).setHeader("Alt").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            int delta = item.getDelta() != null ? item.getDelta() : 0;
            Span s = new Span((delta >= 0 ? "+" : "") + delta);
            s.getStyle().set("font-weight", "700")
                .set("color", delta >= 0 ? "#16a34a" : "#dc2626");
            return s;
        }).setHeader("Delta").setAutoWidth(true);

        grid.addColumn(StockChangeLog::getNewStock).setHeader("Neu").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getReason).setHeader("Grund").setFlexGrow(1);
        grid.addColumn(StockChangeLog::getChangedBy).setHeader("User").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        var items = service.findAll(Sort.by(Sort.Direction.DESC, "changedAt"));
        ListDataProvider<StockChangeLog> dp = new ListDataProvider<>(items);
        grid.setDataProvider(dp);
        grid.getListDataView().addFilter(item -> {
            String f = numberFilter.getValue();
            return f == null || f.isBlank() ||
                    (item.getArticleNumber() != null && item.getArticleNumber().toLowerCase().contains(f.toLowerCase()));
        });

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();
        return card;
    }
}
