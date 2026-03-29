package com.example.application.views.orderPickingView;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.persistence.EntityNotFoundException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.*;
import java.util.stream.Collectors;

@Route("order-picking")
@PageTitle("Order Picking")
@Menu(order = 6, icon = LineAwesomeIconUrl.TRUCK_SOLID)
public class orderPickingMainView extends VerticalLayout {

    private final KommissionService service;
    private final MessageLogisticRepository msgRepo;
    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final LogisticEventPublisher logisticEventPublisher;
    private final Grid<Kommission> grid = new Grid<>(Kommission.class, false);


    public orderPickingMainView(KommissionService service,
                                MessageLogisticRepository msgRepo,
                                ArticleInfoService articleInfoService,
                                ArticleInfoRepository articleInfoRepository,
                                LogisticEventPublisher logisticEventPublisher) {
        this.service = service;
        this.msgRepo = msgRepo;
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.logisticEventPublisher = logisticEventPublisher;
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
                if (e.getValue()) { // wenn "fertig" angeklickt

                    // 1. Alle Artikel dieser Kommission laden
                    List<MessageLogistic> artikel = msgRepo.findByKommissionId(k.getId());

                    // 2. Für jeden Artikel Bestand prüfen & ändern
                    for (MessageLogistic msg : artikel) {
                        int qty = (int) msg.getQuantity();

                        boolean success = articleInfoService.updateStock(msg.getArticleNumber(), (int) msg.getQuantity());


                        if (!success) {
                            Notification notif = new Notification();
                            notif.addThemeVariants(NotificationVariant.LUMO_ERROR);

                            notif.setPosition(Notification.Position.MIDDLE);
                            notif.setDuration(5000);

                            Span text = new Span("⚠️ Kommissionierung nicht möglich! Bestand im Lager für mind. einen Artikel zu gering!");
                            notif.add(text);

                            notif.open();
                            cb.setValue(false); // Checkbox zurücksetzen
                            continue;
                        }
                    }
                    k.setFinished(true);
                    service.save(k);

                    // 3. NEU: Bestätigung an Kasse senden über plaguv-Library
                    publishDeliveryToStore(k, artikel);

                    refreshGridItems();
                }
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

                posGrid.addColumn(pos -> {
                            try {
                                return service.getStockLevelForArticle((pos.getArticleNumber()));
                            } catch (EntityNotFoundException ee) {
                                return "Not found";
                            }
                        }).setHeader("Stock-Level")
                        .setFlexGrow(1);

                posGrid.addComponentColumn(msg -> {

                    // --- Menge ComboBox ---
                    int max = service.getStockLevelForArticle(msg.getArticleNumber());
                    List<Integer> values = java.util.stream.IntStream.rangeClosed(0, max).boxed().toList();

                    ComboBox<Integer> comboQty = new ComboBox<>();
                    comboQty.setItems(values);
                    int originalQty = (int) msg.getQuantity();
                    comboQty.setValue(originalQty);
                    comboQty.setWidth("120px");

                    // --- Anmerkung ComboBox ---
                    ComboBox<String> cbNote = new ComboBox<>();
                    cbNote.setItems("Artikel nicht vorhanden", "Artikelbestand zu gering");
                    cbNote.setPlaceholder("Anmerkung...");
                    cbNote.setWidth("200px");
                    cbNote.setRequired(false);
                    cbNote.setEnabled(false); // erst aktivieren, wenn Menge geändert wird

                    if (msg.getComment() != null) {
                        cbNote.setValue(msg.getComment());
                    }

                    // --- Listener für Menge ---
                    comboQty.addValueChangeListener(ev -> {
                        Integer selected = ev.getValue();
                        if (selected == null) return;

                        // Menge speichern
                        msg.setQuantity(selected);
                        msgRepo.save(msg);

                        Notification.show("Menge geändert auf: " + selected, 3000, Notification.Position.MIDDLE);

                        // Prüfen ob Menge verändert wurde
                        if (!selected.equals(originalQty)) {
                            cbNote.setEnabled(true);
                            cbNote.setRequired(true);
                        } else {
                            cbNote.setEnabled(false);
                            cbNote.setRequired(false);
                        }
                    });

                    // --- Listener für Anmerkung ---
                    cbNote.addValueChangeListener(ev -> {
                        msg.setComment(ev.getValue());
                        msgRepo.save(msg);
                    });

                    // Layout für beide Controls
                    VerticalLayout box = new VerticalLayout(comboQty, cbNote);
                    box.setPadding(false);
                    box.setSpacing(false);

                    return box;

                }).setHeader("Realisierte Menge + Anmerkung");




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
     * Sendet für jeden Artikel der abgeschlossenen Kommission
     * eine Bestätigungsnachricht an die Kasse über die plaguv-Library.
     */
    private void publishDeliveryToStore(Kommission k, List<MessageLogistic> artikel) {
        for (MessageLogistic msg : artikel) {
            try {
                ArticleInfo article = articleInfoRepository.findByArticleNumber(msg.getArticleNumber());

                if (article == null || article.getArticleId() == null) {
                    continue;
                }

                long storeId = Long.parseLong(k.getStoreId());
                long articleId = article.getArticleId();
                long quantity = msg.getQuantity();

                logisticEventPublisher.publishArticleDelivery(storeId, articleId, quantity);

                Notification.show("✅ Rückmeldung an Store " + storeId + " gesendet (Artikel " + articleId + ", Menge " + quantity + ")")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (NumberFormatException e) {
                Notification.show("❌ Fehler: StoreID '" + k.getStoreId() + "' ist keine gültige Zahl")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                Notification.show("❌ Fehler beim Senden an Kasse: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
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
     * Einfache „Ausgrau"-Logik direkt am Span,
     * ganz ohne extra CSS-Datei.
     */
    private void styleCell(Span span, Boolean finished) {
        if (Boolean.TRUE.equals(finished)) {
            span.getStyle().set("opacity", "0.5");
            span.getStyle().set("background-color", "#e0e0e0");
        }
    }
}