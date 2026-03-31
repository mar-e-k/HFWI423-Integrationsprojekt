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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        setPadding(false);
        setSpacing(false);

        // Toolbar
        Button refreshButton = new Button("Aktualisieren", e -> refreshGridItems());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle()
            .set("padding", "16px 20px")
            .set("border-bottom", "1px solid #e8edf5")
            .set("background", "linear-gradient(to right, #fafbff, #f8fafc)");

        // Grid columns
        grid.addComponentColumn(k -> {
            Span span = new Span(String.valueOf(k.getOrderPickingNumber()));
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Order Picking Nr.").setAutoWidth(true);

        grid.addComponentColumn(k -> {
            String text = k.getDate() != null ? k.getDate().toString() : "";
            Span span = new Span(text);
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Created").setAutoWidth(true);

        grid.addComponentColumn(k -> {
            String store = k.getStoreId() != null ? k.getStoreId() : "";
            Span span = new Span(store);
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Store").setAutoWidth(true);

        grid.addComponentColumn(k -> {
            boolean finished = Boolean.TRUE.equals(k.getFinished());
            Span badge = new Span(finished ? "Abgeschlossen" : "Offen");
            badge.getStyle()
                .set("padding", "2px 10px")
                .set("border-radius", "999px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");
            if (finished) {
                badge.getStyle().set("background", "#dcfce7").set("color", "#16a34a");
            } else {
                badge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
            }
            return badge;
        }).setHeader("Status").setAutoWidth(true);

        grid.addComponentColumn(k -> {
            Checkbox cb = new Checkbox(k.getFinished());
            cb.setEnabled(!k.getFinished());

            cb.addValueChangeListener(e -> {
                if (e.getValue()) {
                    List<MessageLogistic> artikel = msgRepo.findByKommissionId(k.getId());

                    for (MessageLogistic msg : artikel) {
                        boolean success = articleInfoService.updateStock(msg.getArticleNumber(), (int) msg.getQuantity());

                        if (!success) {
                            Notification notif = new Notification();
                            notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                            notif.setPosition(Notification.Position.MIDDLE);
                            notif.setDuration(5000);
                            notif.add(new Span("Kommissionierung nicht möglich! Bestand im Lager für mind. einen Artikel zu gering!"));
                            notif.open();
                            cb.setValue(false);
                            continue;
                        }
                    }
                    k.setFinished(true);
                    service.save(k);

                    publishDeliveryToStore(k, artikel);

                    refreshGridItems();
                }
            });

            return cb;
        }).setHeader("Finished").setAutoWidth(true);

        grid.addComponentColumn(k -> {
            Button open = new Button("Open Details");
            open.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            open.setEnabled(!k.getFinished());

            open.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Order Picking " + k.getOrderPickingNumber());
                dialog.setWidth("900px");

                List<MessageLogistic> artikel =
                        msgRepo.findByKommissionId(k.getId());

                Map<String, MessageLogistic> latestPerArticle = artikel.stream()
                        .collect(Collectors.toMap(
                                MessageLogistic::getArticleNumber,
                                m -> m,
                                (oldVal, newVal) -> newVal
                        ));

                List<MessageLogistic> artikelGefiltert = new ArrayList<>(latestPerArticle.values());

                artikelGefiltert = artikelGefiltert.stream()
                        .filter(a -> service.articleExists(a.getArticleNumber()))
                        .collect(Collectors.toList());

                Grid<MessageLogistic> posGrid = new Grid<>(MessageLogistic.class, false);
                posGrid.setWidthFull();
                posGrid.setHeight("400px");

                posGrid.addColumn(MessageLogistic::getArticleNumber)
                        .setHeader("Article-Nr.").setAutoWidth(true);

                posGrid.addColumn(pos -> {
                    try {
                        return service.getArticleNameByNumber(pos.getArticleNumber());
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Article-Name").setFlexGrow(1);

                posGrid.addColumn(m -> m.getQuantity())
                        .setHeader("Quantity").setAutoWidth(true);

                posGrid.addColumn(pos -> {
                    try {
                        return service.getStorageLocationForArticle((pos.getArticleNumber()));
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Storage-Location").setFlexGrow(1);

                posGrid.addColumn(pos -> {
                    try {
                        return service.getStockLevelForArticle((pos.getArticleNumber()));
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Stock-Level").setFlexGrow(1);

                posGrid.addComponentColumn(msg -> {
                    int max = service.getStockLevelForArticle(msg.getArticleNumber());
                    List<Integer> values = java.util.stream.IntStream.rangeClosed(0, max).boxed().toList();

                    ComboBox<Integer> comboQty = new ComboBox<>();
                    comboQty.setItems(values);
                    int originalQty = (int) msg.getQuantity();
                    comboQty.setValue(originalQty);
                    comboQty.setWidth("120px");

                    ComboBox<String> cbNote = new ComboBox<>();
                    cbNote.setItems("Artikel nicht vorhanden", "Artikelbestand zu gering");
                    cbNote.setPlaceholder("Anmerkung...");
                    cbNote.setWidth("200px");
                    cbNote.setRequired(false);
                    cbNote.setEnabled(false);

                    if (msg.getComment() != null) {
                        cbNote.setValue(msg.getComment());
                    }

                    comboQty.addValueChangeListener(ev -> {
                        Integer selected = ev.getValue();
                        if (selected == null) return;

                        msg.setQuantity(selected);
                        msgRepo.save(msg);

                        Notification.show("Menge geändert auf: " + selected, 3000, Notification.Position.MIDDLE);

                        if (!selected.equals(originalQty)) {
                            cbNote.setEnabled(true);
                            cbNote.setRequired(true);
                        } else {
                            cbNote.setEnabled(false);
                            cbNote.setRequired(false);
                        }
                    });

                    cbNote.addValueChangeListener(ev -> {
                        msg.setComment(ev.getValue());
                        msgRepo.save(msg);
                    });

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
        }).setHeader("Details").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();

        refreshGridItems();

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();
        add(card);
        setFlexGrow(1, card);
    }

    private void publishDeliveryToStore(Kommission k, List<MessageLogistic> artikel) {
        for (MessageLogistic msg : artikel) {
            try {
                if (msg.getArticleId() == null) {
                    continue;
                }

                long storeId = Long.parseLong(k.getStoreId());
                long articleId = msg.getArticleId();
                long quantity = msg.getQuantity();

                logisticEventPublisher.publishArticleDelivery(storeId, articleId, quantity);

                Notification.show("Rückmeldung an Store " + storeId + " gesendet (Artikel " + articleId + ", Menge " + quantity + ")")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (NumberFormatException e) {
                Notification.show("Fehler: StoreID '" + k.getStoreId() + "' ist keine gültige Zahl")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                Notification.show("Fehler beim Senden an Kasse: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void refreshGridItems() {
        List<Kommission> items = service.getAlleKommissionen();
        items.sort(Comparator.comparing(Kommission::getFinished));
        grid.setItems(items);
    }
}
