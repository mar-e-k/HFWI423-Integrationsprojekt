package de.fhdw.vendix.store.ui.business;


import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.AccountRole;
import de.fhdw.vendix.store.core.persistance.store_stock.StoreStock;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "stock", layout = StoreAppLayout.class)
@RolesAllowed(AccountRole.ROLE_ADMIN)
public class StockView extends VerticalLayout {

    public StockView() {
        add(initGrid());
    }

    private Grid<StoreStock> initGrid() {
        Grid<StoreStock> storeStockGrid = new Grid<>(StoreStock.class, false);
        storeStockGrid.setHeightFull();
        storeStockGrid.setWidthFull();
        storeStockGrid.addColumn(StoreStock::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStock::getArticle)
                .setHeader("Article")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStock::isActive)
                .setHeader("Active")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStock::getCurrentAmount)
                .setHeader("Current Amount")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStock::getPreferenceAmount)
                .setHeader("Preference Amount")
                .setAutoWidth(true)
                .setSortable(true);
        return storeStockGrid;
    }
}