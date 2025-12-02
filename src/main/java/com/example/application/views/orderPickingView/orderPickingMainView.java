package com.example.application.views.orderPickingView;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.persistence.EntityNotFoundException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("order-picking")
@PageTitle("Order Picking")
@Menu(order = 6, icon = LineAwesomeIconUrl.TRUCK_SOLID)
public class orderPickingMainView extends VerticalLayout {

    private final KommissionService service;
    private final MessageLogisticRepository msgRepo;
    private final Grid<Kommission> grid = new Grid<>(Kommission.class, false);

    public orderPickingMainView(KommissionService service, MessageLogisticRepository msgRepo) {
        this.service = service;
        this.msgRepo = msgRepo;
        setSizeFull();

        // Order Picking Nr. als ComponentColumn, damit wir stylen können
        grid.addComponentColumn(k -> {
            Span span = new Span(String.valueOf(k.getOrderPickingNumber()));
            styleCell(span, k.getFinished());
            return span;
        }).setHeader("Order Picking Nr.");

        // Created
        grid.addComponentColumn(k -> {
            String text = k.getDate() != null ? k.getDate().toString() : "";
            Span span = new Span(text);
            styleCell(span, k.getFinished());
            return span;
        }).setHeader("Created");

        // Store
        grid.addComponentColumn(k -> {
            String store = k.getStoreId() != null ? k.getStoreId() : "";
            Span span = new Span(store);
            styleCell(span, k.getFinished());
            return span;
        }).setHeader("Store");

        // Finished-Checkbox-Spalte
        grid.addComponentColumn(k -> {
            Checkbox cb = new Checkbox(k.getFinished());

            // fertige Kommissionen nicht mehr änderbar
            cb.setEnabled(!k.getFinished());

            cb.addValueChangeListener(e -> {
                // falls man unfertige Kommissionen anklickt
                k.setFinished(e.getValue());
                service.save(k);
                refreshGridItems();
            });

            return cb;
        }).setHeader("Finished");

        // Details-Button
        grid.addComponentColumn(k -> {
            Button open = new Button("Open Details");

            // fertige Kommissionen nicht mehr öffnbar (optional)
            open.setEnabled(!k.getFinished());

            open.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Order Picking " + k.getOrderPickingNumber());
                dialog.setWidth("900px");

                List<MessageLogistic> artikel =
                        msgRepo.findByStoreIdAndQuantityGreaterThan(k.getStoreId(), 0);

                Map<String, MessageLogistic> latestPerArticle = artikel.stream()
                        .collect(Collectors.toMap(
                                MessageLogistic::getArticleNumber,
                                m -> m,
                                (oldVal, newVal) -> newVal // neuer überschreibt alten
                        ));

                List<MessageLogistic> artikelGefiltert = new ArrayList<>(latestPerArticle.values());

                artikelGefiltert = artikelGefiltert.stream()
                        .filter(a -> service.articleExists(a.getArticleNumber()))
                        .collect(Collectors.toList());


                Grid<MessageLogistic> posGrid = new Grid<>(MessageLogistic.class, false);
                posGrid.setWidthFull();
                posGrid.setHeight("400px");

                posGrid.addColumn(MessageLogistic::getArticleNumber)
                        .setHeader("Article-Nr.")
                        .setAutoWidth(true);

                posGrid.addColumn(pos -> {
                            try {
                                return service.getArticleNameByNumber(pos.getArticleNumber());
                            } catch (EntityNotFoundException ee) {
                                return "Not found";
                            }
                        }).setHeader("Article-Name")
                        .setFlexGrow(1);



                posGrid.addColumn(m -> m.getQuantity())
                        .setHeader("Quantity")
                        .setAutoWidth(true);

                posGrid.addColumn(pos -> {
                            try {
                                return service.getStorageLocationForArticle((pos.getArticleNumber()));
                            } catch (EntityNotFoundException ee) {
                                return "Not found";
                            }
                        }).setHeader("Storage-Location")
                        .setFlexGrow(1);



                posGrid.setItems(artikelGefiltert);

                VerticalLayout layout = new VerticalLayout(posGrid);
                layout.setWidthFull();
                layout.setPadding(false);
                layout.setSpacing(false);

                dialog.add(layout);
                dialog.getFooter().add(new Button("Close", ev -> dialog.close()));
                dialog.open();
            });

            return open;
        }).setHeader("Details");

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        refreshGridItems();

        add(new H2("Open Order Picking"), grid);
    }

    /**
     * Lädt alle Kommissionen neu und sortiert fertige ans Ende.
     */
    private void refreshGridItems() {
        List<Kommission> items = service.getAlleKommissionen();
        items.sort(Comparator.comparing(Kommission::getFinished)); // false zuerst, true danach
        grid.setItems(items);
    }

    /**
     * Einfache „Ausgrau“-Logik direkt am Span,
     * ganz ohne extra CSS-Datei.
     */
    private void styleCell(Span span, Boolean finished) {
        if (Boolean.TRUE.equals(finished)) {
            span.getStyle().set("opacity", "0.5");
            span.getStyle().set("background-color", "#e0e0e0");
        }
    }
}