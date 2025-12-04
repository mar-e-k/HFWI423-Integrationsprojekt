package com.example.application.views.components;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.StorageLocationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

public class StorageLocationPickerDialog extends Dialog {

    /**
     * Listener, dem die ausgewählte Location gemeldet wird.
     */
    private final Consumer<StorageLocation> selectionListener;

    public StorageLocationPickerDialog(StorageLocationService storageLocationService,
                                       String title,
                                       Consumer<StorageLocation> selectionListener) {

        this.selectionListener = selectionListener;
        setHeaderTitle(title);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        // Filter Zone
        ComboBox<String> zoneFilter = new ComboBox<>("Zone");
        zoneFilter.setItems("All zones", "Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zoneFilter.setValue("All zones");

        Grid<StorageLocation> locGrid = new Grid<>(StorageLocation.class, false);
        locGrid.addColumn(StorageLocation::getStorageZone).setHeader("Zone").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getShelfID).setHeader("Shelf").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getCompartmentID).setHeader("Compartment").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getStorageStatus).setHeader("Status").setAutoWidth(true);
        locGrid.addColumn(StorageLocation::getGeneralId).setHeader("General ID").setAutoWidth(true);

        List<StorageLocation> allAvailable = storageLocationService.findAllAvailable();
        locGrid.setItems(allAvailable);
        locGrid.setSizeFull();

        zoneFilter.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value == null || "All zones".equals(value)) {
                locGrid.setItems(allAvailable);
            } else {
                locGrid.setItems(
                        allAvailable.stream()
                                .filter(loc -> value.equals(loc.getStorageZone()))
                                .toList()
                );
            }
        });

        // Klick auf Zeile -> Auswahl an Caller melden (Dialog wird NICHT automatisch geschlossen)
        locGrid.addItemClickListener(event -> {
            if (selectionListener != null) {
                selectionListener.accept(event.getItem());
            }
        });

        layout.add(zoneFilter, locGrid);
        layout.setFlexGrow(1, locGrid);
        add(layout);

        Button close = new Button("Close", e -> close());
        getFooter().add(close);

        setWidth("900px");
        setHeight("500px");
    }
}
