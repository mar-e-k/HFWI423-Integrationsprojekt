package com.example.application.views.storageLocationView;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleInfoService;
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

import java.util.List;
import java.util.Objects;

@PageTitle("Storage Location")
@Route("storage-location")
@Menu(order = 2, icon = LineAwesomeIconUrl.STORE_SOLID) // erscheint unter „Logistic“
@Uses(Icon.class)
public class StorageLocationView extends Div {

    private final StorageLocationService service;
    private final ArticleInfoService articleInfoService;
    private final Grid<StorageLocation> grid = new Grid<>(StorageLocation.class, false);

    public StorageLocationView(StorageLocationService service, ArticleInfoService articleInfoService) {
        this.service = service;
        this.articleInfoService = articleInfoService;

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
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY_INLINE
            );

            // Basis-Tooltip
            deleteButton.getElement().setProperty("title", "Delete");

            // Wenn Lagerplatz "Used" ist -> Button ausgrauen & deaktivieren
            if ("Used".equalsIgnoreCase(storageLocation.getStorageStatus())) {
                deleteButton.setEnabled(false);
                deleteButton.getElement().setProperty("title",
                        "This storage location is assigned and cannot be deleted");

                // Optisch etwas „disabled“ machen
                deleteButton.getStyle().set("opacity", "0.5");
                deleteButton.getStyle().set("cursor", "not-allowed");
            } else {
                // Nur für löschbare Locations den Click-Listener registrieren
                deleteButton.addClickListener(click -> {
                    // Sicherheitsabfrage wie bisher
                    Dialog confirm = new Dialog();
                    confirm.setHeaderTitle("Delete Storage Location");

                    confirm.add("Really delete this storage location?");

                    Button cancel = new Button("Cancel", e -> confirm.close());
                    Button confirmDelete = new Button("Delete", e -> {
                        try {
                            service.delete(storageLocation);
                            refreshGrid();
                            confirm.close();
                            Notification.show("Storage Location deleted");
                        } catch (IllegalStateException ex) {
                            Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_CENTER);
                        }
                    });

                    confirmDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);

                    confirm.getFooter().add(new HorizontalLayout(cancel, confirmDelete));
                    confirm.open();
                });
            }

            return deleteButton;
        }).setHeader("Delete").setAutoWidth(true).setFlexGrow(0);
        grid.setSizeFull();
        refreshGrid();

        // Add-Button
        Button addBtn = new Button("Add", e -> openAddDialog());
        // Sync Button
        Button syncBtn = new Button("Aktualisieren", e -> {
            int changed = service.syncStatusesWithArticles();  // Methode im StorageLocationService
            Notification.show(
                    changed + " storage location(s) aktualisiert",
                    4000,
                    Notification.Position.BOTTOM_END
            );
            refreshGrid();
        });

        syncBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Layout
        HorizontalLayout toolbar = new HorizontalLayout(addBtn, syncBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        add(new VerticalLayout(toolbar));
        add(grid);
    }

    private void refreshGrid() {
        List<StorageLocation> locations = service.findAll();

        locations.sort(
                java.util.Comparator
                        .comparingInt(this::extractZoneNumber)
                        .thenComparing(StorageLocation::getShelfID)
                        .thenComparing(StorageLocation::getCompartmentID)
        );

        grid.setItems(locations);
        grid.getDataProvider().refreshAll();
    }

    private int extractZoneNumber(StorageLocation location) {
        if (location == null || location.getStorageZone() == null) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(location.getStorageZone().replace("Zone", "").trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add storage location");

        ComboBox<String> zone = new ComboBox<>("Zone");
        zone.setItems("Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zone.setRequired(true);

        IntegerField shelfId = new IntegerField("Shelf-ID");
        shelfId.setRequiredIndicatorVisible(true);

        IntegerField compartmentId = new IntegerField("Compartment-ID");
        compartmentId.setRequiredIndicatorVisible(true);

        TextField status = new TextField("Status");
        status.setValue("Available");
        status.setReadOnly(true);

        Binder<StorageLocation> binder = new Binder<>(StorageLocation.class);
        StorageLocation bean = new StorageLocation();

        binder.forField(zone)
                .asRequired("Zone is required")
                .bind(StorageLocation::getStorageZone, StorageLocation::setStorageZone);

        binder.forField(shelfId)
                .asRequired("Shelf is required")
                .withConverter(
                        Integer::valueOf,
                        Integer::valueOf,
                        "Invalid number"
                )
                .bind(StorageLocation::getShelfID, StorageLocation::setShelfID);

        binder.forField(compartmentId)
                .asRequired("Compartment is required")
                .withConverter(
                        Integer::valueOf,
                        Integer::valueOf,
                        "Invalid number"
                )
                .bind(StorageLocation::getCompartmentID, StorageLocation::setCompartmentID);

        // Status: nur lesend, aber Wert im Bean behalten
        binder.forField(status)
                .bind(StorageLocation::getStorageStatus, (loc, v) -> {
                    if (loc.getStorageStatus() == null) {
                        loc.setStorageStatus("Available");
                    }
                });

        FormLayout formLayout = new FormLayout(zone, shelfId, compartmentId, status);
        dialog.add(formLayout);

        Button save = new Button("Save", e -> {
            if (binder.writeBeanIfValid(bean)) {
                try {
                    service.saveWithDuplicateCheck(bean);
                    Notification.show("Storage location saved");
                    refreshGrid();
                    dialog.close();
                } catch (IllegalStateException ex) {
                    Notification.show(ex.getMessage(), 4000, Notification.Position.BOTTOM_CENTER);
                }
            } else {
                Notification.show("Please check the entered values");
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openEditDialog(StorageLocation existing) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit storage location: " + existing.getGeneralId());

        ComboBox<String> zone = new ComboBox<>("Zone");
        zone.setItems("Zone 1", "Zone 2", "Zone 3", "Zone 4");
        zone.setRequired(true);

        IntegerField shelfId = new IntegerField("Shelf-ID");
        shelfId.setRequiredIndicatorVisible(true);

        IntegerField compartmentId = new IntegerField("Compartment-ID");
        compartmentId.setRequiredIndicatorVisible(true);

        TextField status = new TextField("Status");
        status.setReadOnly(true);

        Binder<StorageLocation> binder = new Binder<>(StorageLocation.class);
        binder.bind(zone, StorageLocation::getStorageZone, StorageLocation::setStorageZone);
        binder.bind(shelfId, StorageLocation::getShelfID, StorageLocation::setShelfID);
        binder.bind(compartmentId, StorageLocation::getCompartmentID, StorageLocation::setCompartmentID);
        binder.bind(status, StorageLocation::getStorageStatus, (loc, v) -> {
            // noop – read-only im UI, aber binder braucht einen Setter
        });

        binder.readBean(existing);

        FormLayout formLayout = new FormLayout(zone, shelfId, compartmentId, status);
        dialog.add(formLayout);

        Button save = new Button("Save", e -> {
            // General-ID vor der Änderung merken
            String oldGeneralId = existing.getGeneralId();

            if (binder.writeBeanIfValid(existing)) {
                try {
                    // Speichern der geänderten StorageLocation
                    service.saveWithDuplicateCheck(existing);

                    // Neue General-ID nach der Änderung
                    String newGeneralId = existing.getGeneralId();

                    // Wenn sich die General-ID geändert hat → alle Artikel updaten
                    if (!Objects.equals(oldGeneralId, newGeneralId)) {
                        int updated = articleInfoService
                                .updateStorageLocationForAll(oldGeneralId, newGeneralId);
                        Notification.show(
                                "Storage location updated (" + updated + " article(s) adjusted)",
                                4000,
                                Notification.Position.BOTTOM_CENTER
                        );
                    } else {
                        Notification.show(
                                "Storage location updated",
                                4000,
                                Notification.Position.BOTTOM_CENTER
                        );
                    }

                    refreshGrid();
                    dialog.close();

                } catch (IllegalStateException ex) {
                    Notification.show(
                            ex.getMessage(),
                            4000,
                            Notification.Position.BOTTOM_CENTER
                    );
                }
            } else {
                Notification.show(
                        "Please check the entered values",
                        4000,
                        Notification.Position.BOTTOM_CENTER
                );
            }
        });

        Button cancel = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancel, save);
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
