package de.fhdw.vendix.store.ui.business;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.store.ui.StoreAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "stock", layout = StoreAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.ADMIN)
public class StockView extends VerticalLayout {

    public StockView() {
        add(initGrid());
    }

    private Grid<StoreStockDTO> initGrid() {
        Grid<StoreStockDTO> storeStockGrid = new Grid<>(StoreStockDTO.class, false);
        storeStockGrid.setHeightFull();
        storeStockGrid.setWidthFull();
        storeStockGrid.addColumn(StoreStockDTO::id)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStockDTO::article)
                .setHeader("Article")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStockDTO::currentAmount)
                .setHeader("Current Amount")
                .setAutoWidth(true)
                .setSortable(true);
        storeStockGrid.addColumn(StoreStockDTO::preferenceAmount)
                .setHeader("Preference Amount")
                .setAutoWidth(true)
                .setSortable(true);
        return storeStockGrid;
    }
}