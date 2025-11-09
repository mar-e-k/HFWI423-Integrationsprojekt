package com.example.application.views.stockChangeView;

import com.vaadin.flow.data.provider.ListDataProvider;
import org.springframework.data.domain.Sort;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.services.StockChangeLogService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Stock Changes")
@Route("stock-changes")
@Menu(order = 3, icon = LineAwesomeIconUrl.HISTORY_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class StockChangeLogView extends Div {

    private final StockChangeLogService service;
    private final Grid<StockChangeLog> grid = new Grid<>(StockChangeLog.class, false);
    private final TextField numberFilter = new TextField("Search Article Number");

    public StockChangeLogView(StockChangeLogService service) {
        this.service = service;
        setSizeFull();

        numberFilter.setPlaceholder("e.g. 12345");
        numberFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        add(buildHeader(), buildGrid());
    }

    private Component buildHeader() {
        VerticalLayout header = new VerticalLayout(new Text("Stock change history"), numberFilter);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private Component buildGrid() {
        grid.addColumn(StockChangeLog::getChangedAt).setHeader("Changed at").setAutoWidth(true).setSortable(true);
        grid.addColumn(StockChangeLog::getArticleNumber).setHeader("Article Number").setAutoWidth(true).setSortable(true);
        grid.addColumn(StockChangeLog::getArticleName).setHeader("Article Name").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getChangeType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getOldStock).setHeader("Old").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getDelta).setHeader("Δ").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getNewStock).setHeader("New").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getReason).setHeader("Reason").setAutoWidth(true);
        grid.addColumn(StockChangeLog::getChangedBy).setHeader("User").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();

        // DataProvider mit default Sortierung: newest first
        var items = service.findAll(Sort.by(Sort.Direction.DESC, "changedAt"));
        ListDataProvider<StockChangeLog> dp = new ListDataProvider<>(items);
        grid.setDataProvider(dp);

        // Einfacher clientseitiger Filter auf Artikelnummer (Grid-seitig)
        grid.getListDataView().addFilter(item -> {
            String f = numberFilter.getValue();
            return f == null || f.isBlank() ||
                    (item.getArticleNumber() != null && item.getArticleNumber().toLowerCase().contains(f.toLowerCase()));
        });

        return grid;
    }
}
