package com.example.application.views.storageLocationView;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.StorageLocationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Storage Location")
@Route("storage-location")
@Menu(order = 2, icon = LineAwesomeIconUrl.STORE_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class StorageLocationView extends Div {

    private final StorageLocationService service;
    private final Grid<StorageLocation> grid = new Grid<>(StorageLocation.class, false);

    public StorageLocationView(StorageLocationService service) {
        this.service = service;

        setSizeFull();
        addClassName("storage-location-view");

        // Grid-Spalten
        grid.addColumn(StorageLocation::getStorageZone).setHeader("Zone").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getCompartmentID).setHeader("Compartment-ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getShelfID).setHeader("Shelf-ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getStorageStatus).setHeader("Status").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(storageLocation -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), click -> {

                // Optional: kleine Sicherheitsabfrage
                Dialog confirm = new Dialog();
                confirm.setHeaderTitle("Delete Storage Location");

                confirm.add("Really delete this storage location?");

                Button cancel = new Button("Cancel", e -> confirm.close());
                Button confirmDelete = new Button("Delete", e -> {
                    service.delete(storageLocation);
                    refreshGrid();
                    confirm.close();
                    Notification.show("Storage Location deleted");
                });

                confirm.getFooter().add(new HorizontalLayout(cancel, confirmDelete));
                confirm.open();
            });

            // etwas Styling
            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY_INLINE
            );

            return deleteButton;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        refreshGrid();

        // Add-Button
        Button addBtn = new Button("Add", e -> openAddDialog());

        // Layout
        HorizontalLayout toolbar = new HorizontalLayout(addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        add(new VerticalLayout(toolbar));
        add(grid);
    }

    private void refreshGrid() {
        grid.setItems(service.findAll());     // neue Liste holen
        grid.getDataProvider().refreshAll();  // und den Provider refreshen
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Storage Location");

        // Felder
        TextField zone = new TextField("Zone");
        zone.setRequired(true);

        IntegerField compartmentId = new IntegerField("Compartment-ID");
        compartmentId.setStepButtonsVisible(true);
        compartmentId.setRequiredIndicatorVisible(true);

        IntegerField shelfId = new IntegerField("Shelf-ID");
        shelfId.setStepButtonsVisible(true);
        shelfId.setRequiredIndicatorVisible(true);

        TextField status = new TextField("Status");

        FormLayout form = new FormLayout(zone, shelfId, compartmentId, status);
        form.setWidth("480px");
        dialog.isDraggable();
        dialog.add(form);

        // Binder
        Binder<StorageLocation> binder = new Binder<>(StorageLocation.class);
        StorageLocation bean = new StorageLocation();

        binder.forField(zone).asRequired("Zone is mandatory")
                .bind(StorageLocation::getStorageZone, StorageLocation::setStorageZone);

        binder.forField(compartmentId).asRequired("Compartment-ID is mandatory")
                .bind(StorageLocation::getCompartmentID, StorageLocation::setCompartmentID);

        binder.forField(shelfId).asRequired("Shelf-ID is mandatory")
                .bind(StorageLocation::getShelfID, StorageLocation::setShelfID);

        binder.forField(status).bind(StorageLocation::getStorageStatus, StorageLocation::setStorageStatus);

        Button cancel = new Button("Cancel", e -> dialog.close());
        Button save = new Button("Save", e -> {
            try {
                binder.writeBean(bean); // Felder -> Bean

                // 2) Zone + Shelf + Place darf nicht doppelt sein
                if (service.existsByZoneShelfCompartment(
                        bean.getStorageZone(),
                        bean.getShelfID(),
                        bean.getCompartmentID()
                )) {
                    Notification.show(
                            "In Zone '" + bean.getStorageZone() +
                                    "', Shelf '" + bean.getShelfID() +
                                    "' existiert Compartment-ID '" + bean.getCompartmentID() + "' bereits.",
                            4000,
                            Notification.Position.MIDDLE
                    );
                    return;
                }

                service.save(bean);
                Notification.show("Storage Location Saved");
                dialog.close();
                refreshGrid();
            } catch (ValidationException ex) {
                Notification.show("Please Check Inputs");
            }
        });

        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }
}
