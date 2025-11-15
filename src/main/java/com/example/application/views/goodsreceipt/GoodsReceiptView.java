package com.example.application.views.goodsreceipt;

import com.example.application.views.MainLayout;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.services.GoodsReceiptService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
// ⬇️ wichtig: dieses @Menu ist die Quelle für MenuConfiguration.getMenuEntries()
//import com.vaadin.flow.server.menu.Menu;

import java.time.LocalDate;

@PageTitle("Wareneingänge")
// Route an euer MainLayout hängen:
@Route(value = "goods-receipts", layout = MainLayout.class)
// ⬇️ dieser Eintrag macht den Menüpunkt sichtbar
@Menu(
        title = "Wareneingänge",
        // Icon-String muss zu eurer SvgIcon(..) Factory passen.
        // Wenn du unsicher bist, lass icon="" weg oder setze null.
        icon = "la-truck-loading-solid", // z.B. Line Awesome; sonst weglassen
        order = 40                       // Position im Menü (optional)
)
public class GoodsReceiptView extends Div {

    private final GoodsReceiptService service;
    private final Grid<GoodsReceipt> grid = new Grid<>(GoodsReceipt.class, false);

    public GoodsReceiptView(GoodsReceiptService service) {
        this.service = service;
        setSizeFull();

        // Toolbar
        Button add = new Button("Neuer Wareneingang", e -> openCreateDialog());
        HorizontalLayout toolbar = new HorizontalLayout(add);
        toolbar.setWidthFull();

        // Grid
        grid.addColumn(GoodsReceipt::getReceiptNumber).setHeader("WE-Nr.").setAutoWidth(true).setSortable(true);
        grid.addColumn(GoodsReceipt::getSupplierName).setHeader("Lieferant").setAutoWidth(true).setSortable(true);
        grid.addColumn(GoodsReceipt::getDeliveryNoteNumber).setHeader("Lieferschein").setAutoWidth(true);
        grid.addColumn(GoodsReceipt::getDeliveryDate).setHeader("Lieferdatum").setAutoWidth(true).setSortable(true);
        grid.addColumn(GoodsReceipt::getStatus).setHeader("Status").setAutoWidth(true).setSortable(true);
        grid.addColumn(GoodsReceipt::getCreatedAt).setHeader("Angelegt").setAutoWidth(true).setSortable(true);

        grid.setSizeFull();

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setMargin(false);
        content.setFlexGrow(1, grid);

        add(content);
        refresh();
    }

    private void refresh() {
        grid.setItems(service.findAll());
        grid.getDataProvider().refreshAll();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Wareneingang anlegen");

        TextField supplier = new TextField("Lieferant");
        supplier.setRequired(true);

        TextField deliveryNote = new TextField("Lieferscheinnummer");
        deliveryNote.setRequired(true);

        DatePicker deliveryDate = new DatePicker("Lieferdatum");
        deliveryDate.setRequired(true);
        deliveryDate.setValue(LocalDate.now());

        FormLayout form = new FormLayout(supplier, deliveryNote, deliveryDate);
        form.setWidth("480px");
        dialog.add(form);

        Binder<GoodsReceipt> binder = new Binder<>(GoodsReceipt.class);
        GoodsReceipt tmp = new GoodsReceipt(); // nur fürs Binding (wird nicht direkt gespeichert)

        binder.forField(supplier).asRequired("Lieferant ist erforderlich")
                .bind(GoodsReceipt::getSupplierName, GoodsReceipt::setSupplierName);
        binder.forField(deliveryNote).asRequired("Lieferscheinnummer ist erforderlich")
                .bind(GoodsReceipt::getDeliveryNoteNumber, GoodsReceipt::setDeliveryNoteNumber);
        binder.forField(deliveryDate).asRequired("Lieferdatum ist erforderlich")
                .bind(GoodsReceipt::getDeliveryDate, GoodsReceipt::setDeliveryDate);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        Button save = new Button("Speichern", e -> {
            if (binder.writeBeanIfValid(tmp)) {
                service.create(
                        tmp.getSupplierName(),
                        tmp.getDeliveryNoteNumber(),
                        tmp.getDeliveryDate()
                );
                Notification n = Notification.show("Wareneingang angelegt (Status: IN_PRUEFUNG)", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refresh();
            } else {
                Notification n = Notification.show("Bitte Pflichtfelder ausfüllen", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }
}
