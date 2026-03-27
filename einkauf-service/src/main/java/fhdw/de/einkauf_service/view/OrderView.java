package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.dto.OrderItemRequestDTO;
import fhdw.de.einkauf_service.dto.OrderItemResponseDTO;
import fhdw.de.einkauf_service.dto.OrderResponseDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.enums.OrderStatus;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import fhdw.de.einkauf_service.service.SupplierService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "orders", layout = MainLayout.class)
public class OrderView extends VerticalLayout {

    private final PurchaseOrderService orderService;
    private final SupplierService supplierService;

    private final Grid<OrderResponseDTO> grid = new Grid<>(OrderResponseDTO.class, false);

    // Filter-Felder
    private final TextField orderNumberField = createSearchField("Bestellnummer");
    private final ComboBox<SupplierResponseDTO> supplierBox = new ComboBox<>("Lieferant");
    private final DatePicker dateFromPicker = new DatePicker("Von");
    private final DatePicker dateToPicker = new DatePicker("Bis");
    private final ComboBox<OrderStatus> statusBox = new ComboBox<>("Status");
    private final TextField articleIdField = createSearchField("Artikel-ID");

    private final Button clearButton = new Button("Filter zurücksetzen");
    private final Button reorderButton = new Button("Nachbestellung", new Icon(VaadinIcon.REFRESH));

    private final Map<Long, SupplierResponseDTO> supplierCache;

    public OrderView(PurchaseOrderService orderService, SupplierService supplierService) {
        this.orderService = orderService;
        this.supplierService = supplierService;
        this.supplierCache = supplierService.findAllSuppliers().stream()
                .collect(Collectors.toMap(SupplierResponseDTO::getId, Function.identity(), (a, b) -> a));

        setSizeFull();
        addClassName("page-view");

        add(new H2("Bestellhistorie"));

        configureGrid();
        configureFilters();
        configureButtons();

        HorizontalLayout searchLayout = new HorizontalLayout(
                orderNumberField, articleIdField, supplierBox, dateFromPicker, dateToPicker, statusBox, clearButton
        );
        searchLayout.setAlignItems(Alignment.END);
        searchLayout.setWidthFull();
        searchLayout.addClassName("search-toolbar");

        HorizontalLayout buttonLayout = new HorizontalLayout(reorderButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.addClassName("action-toolbar");

        add(searchLayout, grid, buttonLayout);
        setFlexGrow(1, grid);

        updateList();
    }

    // ==================== GRID KONFIGURATION ====================

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(OrderResponseDTO::orderNumber)
                .setHeader("Bestellnummer")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(order -> order.orderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .setHeader("Bestelldatum")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(OrderResponseDTO::supplierName)
                .setHeader("Lieferant")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(order -> order.status().toString())
                .setHeader("Status")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(order -> String.format("%.2f €", order.totalAmount()))
                .setHeader("Gesamtkosten")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(order -> order.expectedDeliveryDate() != null
                ? order.expectedDeliveryDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                : "-")
                .setHeader("Voraussichtliche Lieferung")
                .setAutoWidth(true);

        grid.addComponentColumn(order -> {
            Button detailsButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
            detailsButton.getStyle().set("background", "transparent");
            detailsButton.getElement().setAttribute("title", "Bestelldetails anzeigen");
            detailsButton.addClickListener(e -> showOrderDetails(order));
            return detailsButton;
        })
                .setHeader("Details")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.asSingleSelect().addValueChangeListener(event -> {
            boolean hasSelection = event.getValue() != null;
            reorderButton.setEnabled(hasSelection);
        });

        reorderButton.setEnabled(false);
    }

    // ==================== FILTER KONFIGURATION ====================

    private void configureFilters() {
        supplierBox.setItems(supplierCache.values());
        supplierBox.setItemLabelGenerator(SupplierResponseDTO::getName);
        supplierBox.setClearButtonVisible(true);

        statusBox.setItems(OrderStatus.values());
        statusBox.setClearButtonVisible(true);

        orderNumberField.setValueChangeMode(ValueChangeMode.EAGER);
        articleIdField.setValueChangeMode(ValueChangeMode.EAGER);

        orderNumberField.addValueChangeListener(e -> updateList());
        articleIdField.addValueChangeListener(e -> updateList());
        supplierBox.addValueChangeListener(e -> updateList());
        dateFromPicker.addValueChangeListener(e -> updateList());
        dateToPicker.addValueChangeListener(e -> updateList());
        statusBox.addValueChangeListener(e -> updateList());

        clearButton.addClickListener(e -> {
            orderNumberField.clear();
            articleIdField.clear();
            supplierBox.clear();
            dateFromPicker.clear();
            dateToPicker.clear();
            statusBox.clear();
            updateList();
        });
    }

    // ==================== BUTTON KONFIGURATION ====================

    private void configureButtons() {
        reorderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reorderButton.addClickListener(e -> {
            OrderResponseDTO selected = grid.asSingleSelect().getValue();
            if (selected != null) {
                openReorderDialog(selected);
            }
        });
    }

    // ==================== FILTERUNG UND UPDATE ====================

    private void updateList() {
        OrderFilterDTO filter = new OrderFilterDTO();

        if (!orderNumberField.getValue().isEmpty()) {
            filter.setOrderNumber(orderNumberField.getValue());
        }

        if (!articleIdField.getValue().isEmpty()) {
            try {
                filter.setArticleId(Long.parseLong(articleIdField.getValue()));
            } catch (NumberFormatException e) {
                // Ignorieren, wenn nicht numerisch
            }
        }

        SupplierResponseDTO selectedSupplier = supplierBox.getValue();
        if (selectedSupplier != null) {
            filter.setSupplierId(selectedSupplier.getId());
        }

        if (dateFromPicker.getValue() != null) {
            filter.setOrderDateFrom(dateFromPicker.getValue());
        }

        if (dateToPicker.getValue() != null) {
            filter.setOrderDateTo(dateToPicker.getValue());
        }

        if (statusBox.getValue() != null) {
            filter.setStatus(statusBox.getValue());
        }

        try {
            List<OrderResponseDTO> orders = orderService.getOrderHistory(filter);
            grid.setItems(orders);
        } catch (Exception e) {
            Notification.show("Fehler beim Laden der Bestellhistorie: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // ==================== DETAILANSICHT ====================

    private void showOrderDetails(OrderResponseDTO order) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Bestelldetails - " + order.orderNumber());
        dialog.setWidth("800px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Bestellkopf
        VerticalLayout orderHeader = new VerticalLayout(
                new Span("Bestellnummer: " + order.orderNumber()),
                new Span("Bestelldatum: " + order.orderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                new Span("Lieferant: " + order.supplierName()),
                new Span("Status: " + order.status().toString()),
                new Span("Gesamtkosten: " + String.format("%.2f €", order.totalAmount())),
                new Span("Voraussichtliche Lieferung: " + (order.expectedDeliveryDate() != null
                        ? order.expectedDeliveryDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        : "-"))
        );
        orderHeader.setPadding(true);
        orderHeader.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        orderHeader.getStyle().set("border-radius", "8px");
        content.add(orderHeader);

        // Artikel im Grid
        if (order.items() != null && !order.items().isEmpty()) {
            content.add(new Span("Bestellte Artikel:"));

            Grid<OrderItemResponseDTO> itemGrid = new Grid<>(OrderItemResponseDTO.class, false);
            itemGrid.setHeight("250px");

            itemGrid.addColumn(OrderItemResponseDTO::articleName)
                    .setHeader("Artikel")
                    .setAutoWidth(true);

            itemGrid.addColumn(OrderItemResponseDTO::quantity)
                    .setHeader("Menge")
                    .setAutoWidth(true);

            itemGrid.addColumn(item -> String.format("%.2f €", item.purchasePrice()))
                    .setHeader("Einzelpreis")
                    .setAutoWidth(true);

            itemGrid.addColumn(item -> String.format("%.2f €", item.purchasePrice() * item.quantity()))
                    .setHeader("Zwischensumme")
                    .setAutoWidth(true);

            itemGrid.setItems(order.items());
            content.add(itemGrid);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button reorderButton = new Button("Erneut bestellen", e -> {
            dialog.close();
            openReorderDialog(order);
        });
        reorderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button closeButton = new Button("Schließen", e -> dialog.close());

        buttons.add(reorderButton, closeButton);
        content.add(buttons);

        dialog.add(content);
        dialog.open();
    }

    // ==================== WIEDERBESTELLEN ====================

    private void openReorderDialog(OrderResponseDTO order) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nachbestellung - " + order.orderNumber());
        dialog.setWidth("700px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Warnung wenn Lieferant inaktiv ist
        SupplierResponseDTO supplier = supplierCache.get(order.supplierId());
        if (supplier != null && !supplier.getActive()) {
            Span warning = new Span("⚠️ Der Lieferant ist inaktiv. Artikel können möglicherweise nicht bestellt werden.");
            warning.getStyle().set("color", "#C92525");
            warning.getStyle().set("font-weight", "bold");
            content.add(warning);
        }

        // Artikel zum Bestellen anzeigen
        Grid<OrderItemResponseDTO> itemGrid = new Grid<>(OrderItemResponseDTO.class, false);
        itemGrid.setHeight("300px");

        itemGrid.addColumn(OrderItemResponseDTO::articleName)
                .setHeader("Artikel")
                .setAutoWidth(true);

        itemGrid.addColumn(OrderItemResponseDTO::quantity)
                .setHeader("Menge")
                .setAutoWidth(true);

        itemGrid.addColumn(item -> String.format("%.2f €", item.purchasePrice()))
                .setHeader("Einzelpreis")
                .setAutoWidth(true);

        itemGrid.setItems(order.items());
        content.add(itemGrid);

        // Bestätigungsbuttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button confirmButton = new Button("Bestätigen", e -> {
            try {
                // Alle Artikel bestellen
                List<OrderItemRequestDTO> itemsToReorder = order.items().stream()
                        .map(item -> new OrderItemRequestDTO(item.articleId(), item.quantity()))
                        .collect(Collectors.toList());

                OrderResponseDTO newOrder = orderService.reorder(order.id(), itemsToReorder);

                dialog.close();
                updateList();

                Notification.show("Nachbestellung erfolgreich! Bestellnummer: " + newOrder.orderNumber(),
                        5000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalStateException ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Fehler bei Nachbestellung: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button cancelButton = new Button("Abbrechen", e -> dialog.close());

        buttons.add(cancelButton, confirmButton);
        content.add(buttons);

        dialog.add(content);
        dialog.open();
    }

    // ==================== HELPER ====================

    private static TextField createSearchField(String label) {
        TextField tf = new TextField(label);
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        return tf;
    }
}
