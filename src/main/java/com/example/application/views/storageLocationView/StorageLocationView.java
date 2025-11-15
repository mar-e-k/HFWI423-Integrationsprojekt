package com.example.application.views.storageLocationView;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.StorageLocationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

        grid.addColumn(this::buildGeneralId)
                .setHeader("General ID")
                .setAutoWidth(true)
                .setSortable(false);
        grid.addColumn(StorageLocation::getStorageZone).setHeader("Zone").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getShelfID).setHeader("Shelf-ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getCompartmentID).setHeader("Compartment-ID").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Span status = new Span(item.getStorageStatus());

            status.getStyle().set("padding", "0.1rem 0.4rem");
            status.getStyle().set("border-radius", "0.25rem");
            status.getStyle().set("font-size", "var(--lumo-font-size-s)");
            status.getStyle().set("font-weight", "600");

            if ("Available".equalsIgnoreCase(item.getStorageStatus())) {
                status.getStyle().set("background-color", "#2ecc71");
                status.getStyle().set("color", "white");
            } else if ("Used".equalsIgnoreCase(item.getStorageStatus())) {
                status.getStyle().set("background-color", "#e74c3c");
                status.getStyle().set("color", "white");
            }

            return status;
        }).setHeader("Status").setAutoWidth(true);

//        grid.setClassNameGenerator(storageLocation -> {
//            if ("Available".equalsIgnoreCase(storageLocation.getStorageStatus())) {
//                return "status-available";
//            } else if ("Used".equalsIgnoreCase(storageLocation.getStorageStatus())) {
//                return "status-used";
//            }
//            return null; // keine Extra-Klasse
//        });

        grid.addComponentColumn(storageLocation -> {
            Button editButton = new Button(VaadinIcon.EDIT.create(), click -> {
                openEditDialog(storageLocation);
            });
            editButton.getElement().setProperty("title", "Edit");
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            editButton.getStyle().set("background-color", "gold");
            editButton.getStyle().set("color", "black");

            return editButton;
        }).setHeader("Edit").setAutoWidth(true);

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
        }).setHeader("Delete").setAutoWidth(true).setFlexGrow(0);
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
        ComboBox<String> zone = new ComboBox<>("Zone");
        zone.setItems("Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zone.setRequiredIndicatorVisible(true);

        IntegerField compartmentId = new IntegerField("Compartment-ID");
        compartmentId.setStepButtonsVisible(true);
        compartmentId.setRequiredIndicatorVisible(true);

        IntegerField shelfId = new IntegerField("Shelf-ID");
        shelfId.setStepButtonsVisible(true);
        shelfId.setRequiredIndicatorVisible(true);

        TextField status = new TextField("Status");
        status.setValue("Available");
        status.setReadOnly(true);       // Nutzer kann nichts ändern

        FormLayout form = new FormLayout(zone, shelfId, compartmentId, status);
        form.setWidth("480px");
        dialog.isDraggable();
        dialog.add(form);

        // Binder
        Binder<StorageLocation> binder = new Binder<>(StorageLocation.class);
        StorageLocation bean = new StorageLocation();

        binder.forField(zone)
                .asRequired("Zone is mandatory")
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

    private void openEditDialog(StorageLocation existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Storage Location");

        ComboBox<String> zone = new ComboBox<>("Zone");
        zone.setItems("Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zone.setRequiredIndicatorVisible(true);
        IntegerField compartmentId = new IntegerField("Compartment-ID");
        IntegerField shelfId = new IntegerField("Shelf-ID");
        TextField status = new TextField("Status");
        status.setReadOnly(true); // Status NICHT änderbar

        FormLayout form = new FormLayout(zone, shelfId, compartmentId, status);
        form.setWidth("480px");
        dialog.add(form);

        Binder<StorageLocation> binder = new Binder<>(StorageLocation.class);

        binder.forField(zone)
                .asRequired("Zone is mandatory")
                .bind(StorageLocation::getStorageZone, StorageLocation::setStorageZone);

        binder.forField(compartmentId)
                .asRequired("Compartment-ID is mandatory")
                .bind(StorageLocation::getCompartmentID, StorageLocation::setCompartmentID);

        binder.forField(shelfId)
                .asRequired("Shelf-ID is mandatory")
                .bind(StorageLocation::getShelfID, StorageLocation::setShelfID);

        // Status nur anzeigen, nicht ändern
        binder.forField(status)
                .bind(StorageLocation::getStorageStatus, (bean, value) -> {
                });

        binder.readBean(existing);

        Button cancel = new Button("Cancel", e -> dialog.close());
        Button save = new Button("Save", e -> {
            try {
                binder.writeBean(existing);

                // Duplikat-Check: gleiche Zone + Shelf + Compartment, aber andere ID
                if (service.existsDuplicateForEdit(existing)) {
                    Notification.show(
                            "In Zone '" + existing.getStorageZone() +
                                    "', Shelf '" + existing.getShelfID() +
                                    "' existiert Compartment-ID '" + existing.getCompartmentID() + "' bereits.",
                            4000,
                            Notification.Position.MIDDLE
                    );
                    return;
                }

                service.save(existing);
                Notification.show("Storage Location Updated");
                dialog.close();
                refreshGrid();
            } catch (ValidationException ex) {
                Notification.show("Please Check Inputs");
            }
        });

        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }

    private String buildGeneralId(StorageLocation s) {
        if (s == null) return "";

        String zone = s.getStorageZone();
        // "Zone 3" -> "3"
        String zoneNumber = "";
        if (zone != null) {
            zoneNumber = zone.replace("Zone", "").trim();
        }

        Integer shelf = s.getShelfID();
        Integer compartment = s.getCompartmentID();

        return "Z" + zoneNumber + ".S" + (shelf != null ? shelf : 0)
                + ".C" + (compartment != null ? compartment : 0);
    }
}
