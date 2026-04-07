package com.example.application.views.restockView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.example.application.services.RestockService;
import com.example.application.services.RestockOrderService;
import com.example.application.data.articleInfo.RestockItem;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@PageTitle("Restock")
@Route("restock")
@Menu(order = 6, icon = LineAwesomeIconUrl.WINDOW_RESTORE)
@Uses(Icon.class)
public class RestockView extends Div {

    private final RestockService restockService;
    private final RestockOrderService restockOrderService;
    private final Grid<RestockItem> grid = new Grid<>(RestockItem.class, false);
    private final Span emptyMessage = new Span("Es muessen aktuell keine Artikel nachbestellt werden.");
    private final Div minStockWarning = new Div();

    public RestockView(RestockService restockService,
                       RestockOrderService restockOrderService) {
        this.restockService = restockService;
        this.restockOrderService = restockOrderService;

        setSizeFull();
        addClassName("view-page");

        // Toolbar
        Button refreshButton = new Button("Aktualisieren", event -> {
            updateGrid();
        });

        Anchor exportButton = new Anchor();
        exportButton.setText("Exportieren");
        exportButton.getElement().setAttribute("download", true);
        exportButton.getStyle().set("text-decoration", "none");

        Button approveAllButton = new Button("Alle Bestellungen freigeben", event -> {
            try {
                approveAllOrders();
                Notification.show("Alle Bestellungen wurden freigegeben.");
                updateGrid();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        approveAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(refreshButton, exportButton, approveAllButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("view-toolbar");

        // Warning Banner
        Span warningIcon = new Span("⚠");
        Span warningText = new Span("Warnung: Für einige Artikel ist kein Mindestbestand eingetragen.");
        minStockWarning.add(warningIcon, warningText);
        minStockWarning.addClassName("warning-banner");
        minStockWarning.setVisible(false);

        // Grid-Spalten
        grid.addColumn(RestockItem::getArticleNumber).setHeader("Artikelnummer").setAutoWidth(true).setSortable(true);
        grid.addColumn(RestockItem::getName).setHeader("Name").setFlexGrow(1).setSortable(true);
        grid.addColumn(RestockItem::getReservePallets).setHeader("Reserve Pal.").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            String val = item.getMinStockDisplay();
            Span s = new Span(val);
            if (item.getMinStock() == null) {
                s.getStyle().set("color", "#dc2626").set("font-weight", "600");
            }
            return s;
        }).setHeader("Mindestbestand (in Pal.)").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            String val = item.getOrderAmountDisplay();
            if ("-".equals(val)) return new Span("-");
            Span s = new Span(val + " Pal.");
            int amount = item.getOrderAmount() != null ? item.getOrderAmount() : 0;
            if (amount > 0) {
                s.getStyle()
                    .set("padding", "2px 10px")
                    .set("border-radius", "999px")
                    .set("font-weight", "600")
                    .set("font-size", "0.8rem")
                    .set("background", "#fef3c7")
                    .set("color", "#92400e");
            }
            return s;
        }).setHeader("Nachbestellmenge").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button approveButton = new Button("Bestellung freigeben");
            approveButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            boolean validAmount = item.getOrderAmount() != null && item.getOrderAmount() > 0;
            boolean hasOpenOrder = restockOrderService.hasOpenOrderForArticle(item.getArticle());
            approveButton.setEnabled(validAmount && !hasOpenOrder);
            approveButton.addClickListener(click -> {
                try {
                    restockOrderService.approveOrder(item);
                    Notification.show("Bestellung fuer " + item.getName() + " freigegeben.");
                    updateGrid();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            });
            return approveButton;
        }).setHeader("Bestellung").setAutoWidth(true);

        grid.setPartNameGenerator(item -> item.getMinStock() == null ? "missing-minstock-row" : "");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName("app-grid");
        grid.setSizeFull();

        // Empty message
        emptyMessage.addClassName("empty-state-msg");
        emptyMessage.setVisible(false);

        VerticalLayout content = new VerticalLayout(toolbar, minStockWarning, emptyMessage, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();
        add(card);

        updateGrid();
        updateCsvDownload(exportButton);
    }

    private void updateGrid() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        grid.setItems(items);
        boolean missingMin = items.stream().anyMatch(i -> i.getMinStock() == null);
        minStockWarning.setVisible(missingMin);
        boolean isEmpty = items.isEmpty();
        grid.setVisible(!isEmpty);
        emptyMessage.setVisible(isEmpty);
    }

    private void updateCsvDownload(Anchor exportButton) {
        String csv = buildCsv();
        String base64 = Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8));
        exportButton.setHref("data:text/csv;base64," + base64);
    }

    private String buildCsv() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        StringBuilder sb = new StringBuilder("Artikelnummer;Name;Reserve Paletten;Mindestbestand (in Pal.);Nachbestellmenge (in Pal.)\n");
        for (RestockItem a : items) {
            sb.append(a.getArticleNumber()).append(";")
                    .append(a.getName()).append(";")
                    .append(a.getReservePallets()).append(";")
                    .append(a.getMinStockDisplay()).append(";")
                    .append(a.getOrderAmountDisplay()).append(";")
                    .append(a.getMinStock() == null ? "Kein Mindestbestand eingetragen" : "")
                    .append("\n");
        }
        return sb.toString();
    }

    private void approveAllOrders() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        for (RestockItem item : items) {
            boolean validAmount = item.getOrderAmount() != null && item.getOrderAmount() > 0;
            boolean hasOpenOrder = restockOrderService.hasOpenOrderForArticle(item.getArticle());
            if (validAmount && !hasOpenOrder) {
                restockOrderService.approveOrder(item);
            }
        }
    }
}
