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
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@PageTitle("Lagerplatzverwaltung")
@Route("storage-location")
@Menu(order = 2, icon = LineAwesomeIconUrl.STORE_SOLID)
@Uses(Icon.class)
public class StorageLocationView extends Div {

    private final StorageLocationService service;
    private final ArticleInfoService articleInfoService;
    private final Grid<StorageLocation> grid = new Grid<>(StorageLocation.class, false);

    public StorageLocationView(StorageLocationService service, ArticleInfoService articleInfoService) {
        this.service = service;
        this.articleInfoService = articleInfoService;

        setSizeFull();
        addClassName("view-page");

        // Grid-Spalten
        grid.addColumn(StorageLocation::getId)
                .setHeader("ID")
                .setAutoWidth(true)
                .setSortable(true)
                .setFlexGrow(0);

        grid.addComponentColumn(item -> {
            Span id = new Span(buildGeneralId(item));
            id.addClassName("code-badge");
            return id;
        }).setHeader("General ID").setAutoWidth(true).setSortable(false);

        grid.addComponentColumn(item -> {
            Span zone = new Span(item.getStorageZone() != null ? item.getStorageZone() : "-");
            zone.addClassNames("badge", "badge-purple");
            return zone;
        }).setHeader("Zone").setAutoWidth(true).setSortable(true);

        grid.addColumn(StorageLocation::getShelfID).setHeader("Shelf").setSortable(true).setAutoWidth(true);
        grid.addColumn(StorageLocation::getCompartmentID).setHeader("Compartment").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(item -> {
            boolean available = "Available".equalsIgnoreCase(item.getStorageStatus());
            Span status = new Span(available ? "Available" : "Used");
            status.addClassNames("badge", available ? "badge-success" : "badge-error");
            return status;
        }).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(storageLocation -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setProperty("title", "Delete");

            if ("Used".equalsIgnoreCase(storageLocation.getStorageStatus())) {
                deleteButton.setEnabled(false);
                deleteButton.getElement().setProperty("title", "This storage location is assigned and cannot be deleted");
            } else {
                deleteButton.addClickListener(click -> {
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
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.addClassName("app-grid");
        grid.setSizeFull();
        refreshGrid();

        // Toolbar
        Button addBtn = new Button("+ Lagerplatz hinzufügen", e -> openAddDialog());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button syncBtn = new Button("Aktualisieren", e -> {
            int changed = service.syncStatusesWithArticles();
            Notification.show(changed + " storage location(s) aktualisiert", 4000, Notification.Position.BOTTOM_END);
            refreshGrid();
        });

        Button deleteAvailableBtn = new Button("Available loeschen", e -> {
            int deleted = service.deleteAllAvailable();
            Notification.show(deleted + " Available-Lagerplaetze geloescht", 3000, Notification.Position.BOTTOM_END);
            refreshGrid();
        });
        deleteAvailableBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(addBtn, syncBtn, deleteAvailableBtn);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("view-toolbar");

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();

        add(card);
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

    private String buildGeneralId(StorageLocation s) {
        if (s == null) return "";

        String zone = s.getStorageZone();
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
