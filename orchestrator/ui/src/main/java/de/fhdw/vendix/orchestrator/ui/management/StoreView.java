package de.fhdw.vendix.orchestrator.ui.management;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.store.Store;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "store", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class StoreView extends VerticalLayout {

    public StoreView() {
        add(initGrid());
    }

    private Grid<Store> initGrid() {
        Grid<Store> storeGrid = new Grid<>(Store.class, false);
        storeGrid.setHeightFull();
        storeGrid.setWidthFull();
        storeGrid.addColumn(Store::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true);
        storeGrid.addColumn(Store::getCity)
                .setHeader("City")
                .setAutoWidth(true)
                .setSortable(true);
        storeGrid.addColumn(Store::getCountry)
                .setHeader("Country")
                .setAutoWidth(true)
                .setSortable(true);
        storeGrid.addColumn(Store::getStreet)
                .setHeader("Street")
                .setAutoWidth(true)
                .setSortable(true);
        storeGrid.addColumn(Store::getStreetNumber)
                .setHeader("Street Number")
                .setAutoWidth(true)
                .setSortable(true);
        storeGrid.addColumn(Store::getStreetNumber)
                .setHeader("Street Number")
                .setAutoWidth(true)
                .setSortable(true);
        return storeGrid;
    }
}