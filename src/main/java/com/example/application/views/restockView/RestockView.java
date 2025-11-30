package com.example.application.views.restockView;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;

import com.example.application.services.RestockService;
import com.example.application.services.RestockOrderService;
import com.example.application.data.article.RestockItem;

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
    private final Span emptyMessage = new Span("Es müssen aktuell keine Artikel nachbestellt werden.");
    private final Span minStockWarning = new Span("Warnung: Für einige Artikel ist kein Mindestbestand eingetragen.");

    public RestockView(RestockService restockService,
                       RestockOrderService restockOrderService) {
        this.restockService = restockService;
        this.restockOrderService = restockOrderService;
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        // Export-Button (Anchor-Variante)
        Anchor exportButton = new Anchor();
        exportButton.setText("Exportieren");
        exportButton.getElement().setAttribute("download", true);

        // Aktualisieren-Button
        Button refreshButton = new Button("Aktualisieren", event -> {
            updateGrid();
            updateCsvDownload(exportButton);
        });
        // alle Bestellungen freigeben Button
        Button approveAllButton = new Button("Alle Bestellungen freigeben", event -> {
            try {
                approveAllOrders();
                Notification.show("Alle Bestellungen wurden freigegeben.");
                updateGrid();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        // Grid-Spalten definieren
        grid.addColumn(RestockItem::getArticleNumber).setHeader("Artikelnummer");
        grid.addColumn(RestockItem::getName).setHeader("Name");
        grid.addColumn(RestockItem::getStockLevel).setHeader("Bestand");
        grid.addColumn(RestockItem::getMinStockDisplay).setHeader("Mindestbestand");
        grid.addColumn(RestockItem::getOrderAmountDisplay).setHeader("Nachbestellmenge");
        grid.addComponentColumn(item -> {

            Button approveButton = new Button("Bestellung freigeben");

            boolean validAmount = item.getOrderAmount() != null && item.getOrderAmount() > 0;
            boolean hasOpenOrder = restockOrderService.hasOpenOrderForArticle(item.getArticle());

            approveButton.setEnabled(validAmount && !hasOpenOrder);

            approveButton.addClickListener(click -> {
                try {
                    restockOrderService.approveOrder(item);
                    Notification.show("Bestellung für " + item.getName() + " freigegeben.");
                    updateGrid(); // Wichtig!
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
                }
            });

            return approveButton;

        }).setHeader("Bestellung");

        //Artikel hervorheben, wenn kein Mindestbestand eingetragen ist
        grid.setPartNameGenerator(item -> {
            if (item.getMinStock() == null) {
                return "missing-minstock-row";
            }
            return "";
        });

        grid.setClassNameGenerator(item -> {
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) {
                return "restock-order-open";
            }
            return null;
        });


        grid.setHeight("300px");

        //Message, wenn Liste leer ist
        emptyMessage.getStyle().set("color", "gray");
        emptyMessage.getStyle().set("font-style", "italic");
        emptyMessage.setVisible(false); // Start: unsichtbar

        //Warning Message, wenn kein Wert für Mindestbestand gesetzt ist
        minStockWarning.getStyle().set("color", "var(--lumo-error-color)");
        minStockWarning.getStyle().set("font-weight", "600");
        minStockWarning.setVisible(false);


        // Komponenten ins Layout
        layout.add(refreshButton, exportButton, approveAllButton, emptyMessage, grid, minStockWarning);
        add(layout);

        // Initial Daten laden
        updateGrid();
        updateCsvDownload(exportButton);
    }

    private void updateGrid() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        grid.setItems(items);

        boolean missingMin = items.stream().anyMatch(i -> i.getMinStock() == null);

        minStockWarning.setVisible(missingMin);

        if (items.isEmpty()) {
            grid.setVisible(false);
            emptyMessage.setVisible(true);
        } else {
            grid.setVisible(true);
            emptyMessage.setVisible(false);
        }
    }

    private void updateCsvDownload(Anchor exportButton) {
        String csv = buildCsv();

        // CSV in Base64 kodieren → funktioniert in JEDER Vaadin-Version ohne StreamResource
        String base64 = Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8));

        // Data-URL als Download
        exportButton.setHref("data:text/csv;base64," + base64);
    }

    // CSV Export
    private String buildCsv() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        StringBuilder sb = new StringBuilder("Artikelnummer;Name;Bestand;Mindestbestand;Nachbestellmenge\n");

        for (RestockItem a : items) {
            sb.append(a.getArticleNumber()).append(";")
                    .append(a.getName()).append(";")
                    .append(a.getStockLevel()).append(";")
                    .append(a.getMinStockDisplay()).append(";")
                    .append(a.getOrderAmountDisplay()).append(";")

                    // Warnhinweis-Spalte
                    .append(a.getMinStock() == null
                            ? "Kein Mindestbestand eingetragen"
                            : "")
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
