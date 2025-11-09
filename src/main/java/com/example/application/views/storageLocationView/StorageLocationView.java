package com.example.application.views.storageLocationView;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.StorageLocationService;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Storage Location")
@Route("storage-location")
@Menu(order = 2, icon = LineAwesomeIconUrl.STORE_SOLID)
@Uses(Icon.class)
public class StorageLocationView extends Div {

    private final Grid<StorageLocation> grid = new Grid<>(StorageLocation.class, false);

    public StorageLocationView(StorageLocationService service) {
        addClassName("storage-location-view");
        setSizeFull();

        grid.addColumn(StorageLocation::getStorageZone)
                .setHeader("Zone")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(StorageLocation::getStoragePlaceID)
                .setHeader("Platz-ID")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(StorageLocation::getShelfID)
                .setHeader("Regal-ID")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(StorageLocation::getStorageStatus)
                .setHeader("Status")
                .setSortable(true)
                .setAutoWidth(true);


        grid.setItems(service.findAll());

        grid.setSizeFull();
        add(grid);
    }

}
