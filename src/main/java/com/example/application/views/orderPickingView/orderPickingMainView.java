package com.example.application.views.orderPickingView;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.example.application.services.WeeklyKommissionScheduler;
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
    private final WeeklyKommissionScheduler weeklyScheduler;
    private final Grid<Kommission> grid = new Grid<>(Kommission.class, false);

    public orderPickingMainView(KommissionService service,
                                MessageLogisticRepository msgRepo,
                                ArticleInfoService articleInfoService,
                                ArticleInfoRepository articleInfoRepository,
                                LogisticEventPublisher logisticEventPublisher,
                                WeeklyKommissionScheduler weeklyScheduler) {
        this.service = service;
        this.msgRepo = msgRepo;
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.logisticEventPublisher = logisticEventPublisher;
        this.weeklyScheduler = weeklyScheduler;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Einfache Toolbar zum Neuladen der Kommissionen
        Button refreshButton = new Button("Aktualisieren", e -> refreshGridItems());

        Button triggerButton = new Button("Kommissionierung auslösen", e -> {
            weeklyScheduler.createWeeklyKommissionen();
            refreshGridItems();
            Notification.show("Kommissionierung manuell ausgelöst", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        triggerButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout toolbar = new HorizontalLayout(refreshButton, triggerButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.getStyle()
                .set("padding", "16px 20px")
                .set("border-bottom", "1px solid #e8edf5")
                .set("background", "linear-gradient(to right, #fafbff, #f8fafc)");

        // Spalte: Order-Picking-Nummer
        grid.addComponentColumn(k -> {
            Span span = new Span(String.valueOf(k.getOrderPickingNumber()));
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Order Picking Nr.").setAutoWidth(true);

        // Spalte: Erstellungsdatum
        grid.addComponentColumn(k -> {
            String text = k.getDate() != null ? k.getDate().toString() : "";
            Span span = new Span(text);
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Created").setAutoWidth(true);

        // Spalte: Store
        grid.addComponentColumn(k -> {
            String store = k.getStoreId() != null ? k.getStoreId() : "";
            Span span = new Span(store);
            if (Boolean.TRUE.equals(k.getFinished())) {
                span.getStyle().set("opacity", "0.5");
            }
            return span;
        }).setHeader("Store").setAutoWidth(true);

        // Spalte: Status-Badge
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

        // Spalte: Kommission abschließen
        grid.addComponentColumn(k -> {
            Checkbox cb = new Checkbox(k.getFinished());
            cb.setEnabled(!k.getFinished());

            cb.addValueChangeListener(e -> {
                if (e.getValue()) {
                    List<MessageLogistic> artikel = msgRepo.findByKommissionId(k.getId());

                    for (MessageLogistic msg : artikel) {
                        ArticleInfo article = resolveArticle(msg);
                        if (article == null) {
                            continue;
                        }

                        // Bestand immer zuerst über articleId auflösen, alte Datensätze notfalls über articleNumber
                        int stock = getFullStockLevel(msg);
                        int realisierbar = Math.min((int) msg.getQuantity(), stock);

                        if (realisierbar != (int) msg.getQuantity()) {
                            msg.setQuantity(realisierbar);
                            msgRepo.save(msg);
                        }

                        if (realisierbar > 0) {
                            // updateStock arbeitet im Service noch mit articleNumber,
                            // deshalb holen wir den Artikel zuerst sauber aufgelöst
                            articleInfoService.updateStock(article.getArticleNumber(), realisierbar);
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

        // Spalte: Detailansicht öffnen
        grid.addComponentColumn(k -> {
            Button open = new Button("Open Details");
            open.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            open.setEnabled(!k.getFinished());

            open.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Order Picking " + k.getOrderPickingNumber());
                dialog.setWidth("900px");

                List<MessageLogistic> artikel = msgRepo.findByKommissionId(k.getId());

                // Doppelte Einträge pro Artikel zusammenfassen:
                // zuerst articleId verwenden, sonst auf articleNumber zurückfallen
                Map<String, MessageLogistic> latestPerArticle = artikel.stream()
                        .collect(Collectors.toMap(
                                this::buildArticleKey,
                                m -> m,
                                (oldVal, newVal) -> newVal
                        ));

                List<MessageLogistic> artikelGefiltert = new ArrayList<>(latestPerArticle.values());

                // Nur Artikel anzeigen, die sich noch in article_info auflösen lassen
                artikelGefiltert = artikelGefiltert.stream()
                        .filter(this::articleExists)
                        .collect(Collectors.toList());

                Grid<MessageLogistic> posGrid = new Grid<>(MessageLogistic.class, false);
                posGrid.setWidthFull();
                posGrid.setHeight("400px");

                posGrid.addColumn(msg -> {
                    ArticleInfo article = resolveArticle(msg);
                    return article != null ? article.getArticleId() : "Not found";
                }).setHeader("Article-ID").setAutoWidth(true);

                posGrid.addColumn(msg -> {
                    ArticleInfo article = resolveArticle(msg);
                    return article != null ? article.getArticleNumber() : "Not found";
                }).setHeader("Article-Nr.").setAutoWidth(true);

                posGrid.addColumn(pos -> {
                    try {
                        return getArticleName(pos);
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Article-Name").setFlexGrow(1);

                posGrid.addColumn(MessageLogistic::getQuantity)
                        .setHeader("Quantity").setAutoWidth(true);

                posGrid.addColumn(pos -> {
                    try {
                        return getStorageLocation(pos);
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Storage-Location").setFlexGrow(1);

                posGrid.addColumn(pos -> {
                    try {
                        return getFullStockLevel(pos);
                    } catch (EntityNotFoundException ee) {
                        return "Not found";
                    }
                }).setHeader("Stock-Level").setFlexGrow(1);

                posGrid.addComponentColumn(msg -> {
                    int max = getFullStockLevel(msg);
                    int requestedQty = (int) msg.getQuantity();

                    // Es darf nur maximal das ausgewählt werden, was wirklich im offenen Bestand liegt
                    int realisierbar = Math.min(requestedQty, max);

                    List<Integer> values = java.util.stream.IntStream.rangeClosed(0, realisierbar).boxed().toList();

                    ComboBox<Integer> comboQty = new ComboBox<>();
                    comboQty.setItems(values);
                    comboQty.setValue(realisierbar);
                    comboQty.setWidth("120px");
                    comboQty.setEnabled(realisierbar > 0);

                    ComboBox<String> cbNote = new ComboBox<>();
                    cbNote.setItems("Artikel nicht vorhanden", "Artikelbestand zu gering");
                    cbNote.setPlaceholder("Anmerkung...");
                    cbNote.setWidth("200px");
                    cbNote.setRequired(false);

                    // Wenn weniger geliefert werden kann, Menge und Hinweis direkt speichern
                    if (realisierbar < requestedQty) {
                        msg.setQuantity(realisierbar);
                        cbNote.setEnabled(true);
                        String note = realisierbar == 0 ? "Artikel nicht vorhanden" : "Artikelbestand zu gering";
                        cbNote.setValue(note);
                        msg.setComment(note);
                        msgRepo.save(msg);
                    } else {
                        cbNote.setEnabled(false);
                        if (msg.getComment() != null) {
                            cbNote.setValue(msg.getComment());
                        }
                    }

                    comboQty.addValueChangeListener(ev -> {
                        Integer selected = ev.getValue();
                        if (selected == null) return;

                        msg.setQuantity(selected);
                        msgRepo.save(msg);

                        Notification.show("Menge geändert auf: " + selected, 3000, Notification.Position.MIDDLE);

                        if (selected < requestedQty) {
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

    // Sendet nach dem Abschließen die Rückmeldung an die Filiale
    private void publishDeliveryToStore(Kommission k, List<MessageLogistic> artikel) {
        for (MessageLogistic msg : artikel) {
            try {
                ArticleInfo article = resolveArticle(msg);
                if (article == null || article.getArticleId() == null) {
                    continue;
                }

                long storeId = Long.parseLong(k.getStoreId());
                long articleId = article.getArticleId();
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

    // Baut einen stabilen Schlüssel pro Artikel: zuerst ID, sonst Number
    private String buildArticleKey(MessageLogistic msg) {
        if (msg.getArticleId() != null) {
            return "ID_" + msg.getArticleId();
        }
        if (msg.getArticleNumber() != null) {
            return "NR_" + msg.getArticleNumber();
        }
        return UUID.randomUUID().toString();
    }

    // Zentrale Auflösung eines Artikels: zuerst articleId, dann articleNumber
    private ArticleInfo resolveArticle(MessageLogistic msg) {
        if (msg == null) {
            return null;
        }

        if (msg.getArticleId() != null) {
            ArticleInfo byId = articleInfoRepository.findByArticleId(msg.getArticleId());
            if (byId != null) {
                return byId;
            }
        }

        if (msg.getArticleNumber() != null && !msg.getArticleNumber().isBlank()) {
            return articleInfoRepository.findByArticleNumber(msg.getArticleNumber());
        }

        return null;
    }

    // Prüft, ob sich der Artikel noch sauber auflösen lässt
    private boolean articleExists(MessageLogistic msg) {
        return resolveArticle(msg) != null;
    }

    // Holt den Artikelnamen bevorzugt über articleId
    private String getArticleName(MessageLogistic msg) {
        ArticleInfo article = resolveArticle(msg);
        if (article == null) {
            throw new EntityNotFoundException("Artikel nicht gefunden");
        }
        return article.getName();
    }

    // Holt den Lagerplatz bevorzugt über articleId
    private String getStorageLocation(MessageLogistic msg) {
        ArticleInfo article = resolveArticle(msg);
        if (article == null) {
            throw new EntityNotFoundException("Artikel nicht gefunden");
        }
        return article.getStorageLocation();
    }

    // Holt nur den offenen Fachbestand bevorzugt über articleId
    private int getOpenStockLevel(MessageLogistic msg) {
        ArticleInfo article = resolveArticle(msg);
        if (article == null) {
            throw new EntityNotFoundException("Artikel nicht gefunden");
        }
        return article.getStockLevel() != null ? article.getStockLevel() : 0;
    }

    // Holt den gesamten verfügbaren Bestand inklusive Reservepaletten
    private int getFullStockLevel(MessageLogistic msg) {
        ArticleInfo article = resolveArticle(msg);
        if (article == null) {
            throw new EntityNotFoundException("Artikel nicht gefunden");
        }
        return article.getTotalStock() != null ? article.getTotalStock() : 0;
    }
}