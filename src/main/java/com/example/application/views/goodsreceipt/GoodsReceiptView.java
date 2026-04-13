package com.example.application.views.goodsreceipt;

import com.example.application.views.MainLayout;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.services.GoodsReceiptService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Wareneingänge")
@Route(value = "goods-receipts", layout = MainLayout.class)
@Menu(
        title = "Wareneingänge",
        icon = LineAwesomeIconUrl.BOX_SOLID,
        order = 40
)
public class GoodsReceiptView extends Div {

    private final GoodsReceiptService service;
    private final ArticleInfoRepository articleInfoRepository;
    private final Grid<GoodsReceipt> grid = new Grid<>(GoodsReceipt.class, false);

    public GoodsReceiptView(GoodsReceiptService service,
                            ArticleInfoRepository articleInfoRepository) {
        this.service = service;
        this.articleInfoRepository = articleInfoRepository;
        setSizeFull();
        addClassName("view-page");

        // Toolbar
        Button add = new Button("Neuer Wareneingang", e -> openCreateDialog());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(add);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("view-toolbar");

        // Grid columns
        grid.addColumn(GoodsReceipt::getReceiptNumber)
                .setHeader("WE-Nr.")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(GoodsReceipt::getSupplierName)
                .setHeader("Lieferant")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(GoodsReceipt::getDeliveryNoteNumber)
                .setHeader("Lieferschein")
                .setAutoWidth(true);

        grid.addColumn(GoodsReceipt::getDeliveryDate)
                .setHeader("Lieferdatum")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addComponentColumn(gr -> {
            String status = gr.getStatus() != null ? gr.getStatus().toString() : "-";
            Span badge = new Span(status);
            badge.addClassName("badge");
            String upper = status.toUpperCase();
            if (upper.contains("ABGESCHLOSSEN") || upper.contains("COMPLETE") || upper.contains("DONE")) {
                badge.addClassName("badge-success");
            } else if (upper.contains("GESPERRT") || upper.contains("BLOCKED") || upper.contains("ERROR")) {
                badge.addClassName("badge-error");
            } else {
                badge.addClassName("badge-purple");
            }
            return badge;
        }).setHeader("Status").setAutoWidth(true).setSortable(true);

        grid.addColumn(GoodsReceipt::getCreatedAt)
                .setHeader("Angelegt")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addComponentColumn(gr -> {
            Button inspect = new Button("Prüfen", e -> openInspectionDialog(gr));
            inspect.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            Button delete = new Button("Löschen", e -> deleteReceipt(gr));
            delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout actions = new HorizontalLayout(inspect, delete);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Aktionen").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName("app-grid");
        grid.setSizeFull();

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();
        add(card);

        refresh();
    }

    private void refresh() {
        grid.setItems(service.findAll());
        grid.getDataProvider().refreshAll();
    }

    private void deleteReceipt(GoodsReceipt gr) {
        Dialog confirm = new Dialog();
        confirm.setHeaderTitle("Wareneingang löschen");

        confirm.add(new Span("Soll \"" + gr.getReceiptNumber() + "\" wirklich gelöscht werden?"));

        Button cancel = new Button("Abbrechen", e -> confirm.close());

        Button delete = new Button("Löschen", e -> {
            confirm.close();
            try {
                service.deleteIfAllowed(gr.getId());
                Notification n = Notification.show("Wareneingang " + gr.getReceiptNumber() + " wurde gelöscht.", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refresh();
            } catch (IllegalStateException ex) {
                Dialog error = new Dialog();
                error.setHeaderTitle("Löschen nicht möglich");
                error.add(new Span(
                        "Der Wareneingang \"" + gr.getReceiptNumber() + "\" hat den Status \""
                        + gr.getStatus() + "\" und kann daher nicht gelöscht werden. "
                        + "Nur Wareneingänge im Status \"IN_PRUEFUNG\" dürfen gelöscht werden."
                ));
                Button ok = new Button("OK", ev -> error.close());
                ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                error.getFooter().add(ok);
                error.open();
            }
        });
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        confirm.getFooter().add(cancel, delete);
        confirm.open();
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Wareneingang aus Bestellung anlegen");

        ComboBox<String> supplier = new ComboBox<>("Lieferant");
        supplier.setItems(service.findDistinctSupplierNames());
        supplier.setAllowCustomValue(true);
        supplier.addCustomValueSetListener(e -> supplier.setValue(e.getDetail()));
        supplier.setRequired(true);

        TextField deliveryNote = new TextField("Lieferscheinnummer");
        deliveryNote.setRequired(true);

        DatePicker deliveryDate = new DatePicker("Lieferdatum");
        deliveryDate.setRequired(true);
        deliveryDate.setValue(LocalDate.now());

        FormLayout form = new FormLayout(supplier, deliveryNote, deliveryDate);
        form.setWidth("480px");

        List<RestockOrder> openOrders = service.findOpenRestockOrders();

        Grid<RestockOrder> restockGrid = new Grid<>(RestockOrder.class, false);
        restockGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        restockGrid.setWidthFull();
        restockGrid.setHeight("300px");

        restockGrid.addColumn(RestockOrder::getId).setHeader("ID").setAutoWidth(true);
        restockGrid.addColumn(RestockOrder::getArticleNumber).setHeader("Artikel-Nr.").setAutoWidth(true);
        restockGrid.addColumn(RestockOrder::getArticleName).setHeader("Artikel").setAutoWidth(true);

        restockGrid.addColumn(ro -> {
            ArticleInfo article = articleInfoRepository.findByArticleNumber(ro.getArticleNumber());
            if (article == null
                    || article.getPiecesPerPallet() == null
                    || article.getPiecesPerPallet() <= 0
                    || ro.getQuantity() == null) {
                return "-";
            }
            int ppp = article.getPiecesPerPallet();
            int pallets = ro.getQuantity() / ppp;
            return String.valueOf(pallets);
        }).setHeader("Menge (Pal.)").setAutoWidth(true);

        restockGrid.addColumn(ro -> ro.getCreatedAt() != null ? ro.getCreatedAt().toString() : "")
                .setHeader("Bestellt am").setAutoWidth(true);

        restockGrid.setItems(openOrders);

        Span info = new Span(
                "Wähle eine oder mehrere offene Bestellungen aus, " +
                        "die mit diesem Wareneingang (in Paletten) geliefert wurden."
        );

        VerticalLayout layout = new VerticalLayout(form, info, restockGrid);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setMargin(false);
        layout.setWidth("900px");

        dialog.add(layout);

        Binder<GoodsReceipt> binder = new Binder<>(GoodsReceipt.class);
        GoodsReceipt tmp = new GoodsReceipt();

        binder.forField(supplier).asRequired("Lieferant ist erforderlich")
                .bind(GoodsReceipt::getSupplierName, GoodsReceipt::setSupplierName);
        binder.forField(deliveryNote).asRequired("Lieferscheinnummer ist erforderlich")
                .bind(GoodsReceipt::getDeliveryNoteNumber, GoodsReceipt::setDeliveryNoteNumber);
        binder.forField(deliveryDate).asRequired("Lieferdatum ist erforderlich")
                .bind(GoodsReceipt::getDeliveryDate, GoodsReceipt::setDeliveryDate);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        Button save = new Button("Anlegen", e -> {
            if (!binder.writeBeanIfValid(tmp)) {
                Notification n = Notification.show("Bitte Pflichtfelder ausfüllen", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Set<RestockOrder> selected = restockGrid.getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                Notification n = Notification.show("Bitte mindestens eine Bestellung auswählen", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            List<Long> restockIds = selected.stream()
                    .map(RestockOrder::getId)
                    .collect(Collectors.toList());

            try {
                service.createFromRestockOrders(
                        restockIds,
                        tmp.getSupplierName(),
                        tmp.getDeliveryNoteNumber(),
                        tmp.getDeliveryDate()
                );
                Notification n = Notification.show("Wareneingang aus Bestellung angelegt", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refresh();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                Notification n = Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }

    private void openInspectionDialog(GoodsReceipt receipt) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Wareneingang prüfen: " + receipt.getReceiptNumber());

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String infoText = "Lieferant: " + receipt.getSupplierName()
                + " | Lieferschein: " + receipt.getDeliveryNoteNumber()
                + " | Lieferdatum: " + (receipt.getDeliveryDate() != null ? receipt.getDeliveryDate().format(df) : "-");

        Span info = new Span(infoText);

        Grid<GoodsReceiptItem> itemGrid = new Grid<>(GoodsReceiptItem.class, false);
        itemGrid.setWidthFull();
        itemGrid.setHeight("300px");

        itemGrid.addColumn(item -> item.getArticle() != null ? item.getArticle().getArticleNumber() : "")
                .setHeader("Artikel-Nr.").setAutoWidth(true);

        itemGrid.addColumn(item -> item.getArticle() != null ? item.getArticle().getName() : "")
                .setHeader("Artikel").setAutoWidth(true);

        itemGrid.addColumn(GoodsReceiptItem::getExpectedQuantity)
                .setHeader("Soll-Menge (Pal.)").setAutoWidth(true);

        itemGrid.addColumn(GoodsReceiptItem::getActualQuantity)
                .setHeader("Ist-Menge (Pal.)").setAutoWidth(true);

        itemGrid.addComponentColumn(item -> {
            String status = item.getStatus() != null ? item.getStatus().toString() : "-";
            Span badge = new Span(status);
            badge.addClassName("badge");
            String upper = status.toUpperCase();
            if (upper.contains("FREIGEGEBEN")) {
                badge.addClassName("badge-success");
            } else if (upper.contains("GESPERRT")) {
                badge.addClassName("badge-error");
            } else {
                badge.addClassName("badge-neutral");
            }
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        itemGrid.addColumn(GoodsReceiptItem::getDefectNotes)
                .setHeader("Mängel").setAutoWidth(true);

        IntegerField actualQtyField = new IntegerField("Ist-Menge (Paletten)");
        actualQtyField.setMin(0);
        actualQtyField.setStep(1);

        TextArea defectNotesField = new TextArea("Mängel / Abweichungen");
        defectNotesField.setWidthFull();
        defectNotesField.setHeight("120px");

        Binder<GoodsReceiptItem> itemBinder = new Binder<>(GoodsReceiptItem.class);

        itemBinder.forField(actualQtyField)
                .bind(GoodsReceiptItem::getActualQuantity, GoodsReceiptItem::setActualQuantity);

        itemBinder.forField(defectNotesField)
                .bind(GoodsReceiptItem::getDefectNotes, GoodsReceiptItem::setDefectNotes);

        itemGrid.asSingleSelect().addValueChangeListener(e -> {
            GoodsReceiptItem selected = e.getValue();
            if (selected != null) {
                itemBinder.setBean(selected);
            } else {
                itemBinder.setBean(null);
                actualQtyField.clear();
                defectNotesField.clear();
            }
        });

        Button addItem = new Button("Position hinzufügen",
                e -> openAddItemDialog(receipt, itemGrid, itemBinder));
        addItem.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);

        Button saveItem = new Button("Änderungen speichern", e -> {
            GoodsReceiptItem bean = itemBinder.getBean();
            if (bean == null) {
                Notification.show("Bitte zuerst eine Position auswählen", 3000, Position.MIDDLE);
                return;
            }
            service.updateItem(bean.getId(), actualQtyField.getValue(), defectNotesField.getValue());
            Notification n = Notification.show("Position aktualisiert", 3000, Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            reloadItems(receipt, itemGrid, itemBinder);
        });
        saveItem.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button markFree = new Button("Freigeben", e -> {
            GoodsReceiptItem bean = itemBinder.getBean();
            if (bean == null) {
                Notification.show("Bitte zuerst eine Position auswählen", 3000, Position.MIDDLE);
                return;
            }
            service.setItemStatus(bean.getId(), GoodsReceiptItemStatus.FREIGEGEBEN);
            Notification n = Notification.show("Position freigegeben", 3000, Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            reloadItems(receipt, itemGrid, itemBinder);
        });

        Button markBlocked = new Button("Sperren", e -> {
            GoodsReceiptItem bean = itemBinder.getBean();
            if (bean == null) {
                Notification.show("Bitte zuerst eine Position auswählen", 3000, Position.MIDDLE);
                return;
            }
            service.setItemStatus(bean.getId(), GoodsReceiptItemStatus.GESPERRT);
            Notification n = Notification.show("Position gesperrt", 3000, Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            reloadItems(receipt, itemGrid, itemBinder);
        });

        HorizontalLayout itemButtons = new HorizontalLayout(saveItem, markFree, markBlocked);

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidth("900px");

        layout.add(info, addItem, itemGrid, actualQtyField, defectNotesField, itemButtons);

        dialog.add(layout);

        Button close = new Button("Schließen", e -> dialog.close());

        Button complete = new Button("Prüfung abschließen", e -> {
            try {
                service.completeInspection(receipt.getId());
                Notification n = Notification.show("Prüfung abgeschlossen", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                refresh();
            } catch (IllegalStateException ex) {
                Notification n = Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        complete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(new HorizontalLayout(close, complete));

        reloadItems(receipt, itemGrid, itemBinder);

        dialog.open();
    }

    private void reloadItems(GoodsReceipt receipt,
                             Grid<GoodsReceiptItem> grid,
                             Binder<GoodsReceiptItem> binder) {
        List<GoodsReceiptItem> items = service.getItemsForReceipt(receipt.getId());
        grid.setItems(items);
        grid.getDataProvider().refreshAll();
        binder.setBean(null);
    }

    private void openAddItemDialog(GoodsReceipt receipt,
                                   Grid<GoodsReceiptItem> itemGrid,
                                   Binder<GoodsReceiptItem> itemBinder) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Position hinzufügen");

        ComboBox<ArticleInfo> articleCombo = new ComboBox<>("Artikel");
        articleCombo.setItemLabelGenerator(a -> a.getArticleNumber() + " - " + a.getName());
        articleCombo.setItems(articleInfoRepository.findAll());
        articleCombo.setRequired(true);
        articleCombo.setWidthFull();

        IntegerField expectedQty = new IntegerField("Soll-Menge (Paletten)");
        expectedQty.setMin(0);
        expectedQty.setStep(1);
        expectedQty.setRequiredIndicatorVisible(true);

        IntegerField actualQty = new IntegerField("Ist-Menge (Paletten)");
        actualQty.setMin(0);
        actualQty.setStep(1);

        TextArea defects = new TextArea("Mängel / Abweichungen");
        defects.setWidthFull();

        FormLayout form = new FormLayout(articleCombo, expectedQty, actualQty, defects);
        form.setWidth("500px");
        dialog.add(form);

        Button cancel = new Button("Abbrechen", e -> dialog.close());
        Button save = new Button("Speichern", e -> {
            if (articleCombo.getValue() == null) {
                Notification n = Notification.show("Bitte Artikel auswählen", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            if (expectedQty.getValue() == null) {
                Notification n = Notification.show("Bitte Soll-Menge eingeben", 3000, Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            service.addItemToReceipt(
                    receipt.getId(),
                    articleCombo.getValue(),
                    expectedQty.getValue(),
                    actualQty.getValue(),
                    defects.getValue()
            );

            Notification n = Notification.show("Position hinzugefügt", 3000, Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
            reloadItems(receipt, itemGrid, itemBinder);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(new HorizontalLayout(cancel, save));
        dialog.open();
    }
}
