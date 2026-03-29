package com.example.application.views.newArticleView;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.NewArticleNotificationService;
import com.example.application.services.StorageLocationService;
import com.example.application.views.MainLayout;
import com.example.application.views.components.StorageLocationPickerDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;


import java.util.List;

@PageTitle("Neue Artikel") // Titel im Browser-Tab
@Route(value = "new-articles", layout = MainLayout.class) // Route unter /new-articles, eingebettet im MainLayout
@Menu(title = "Neue Artikel", icon = LineAwesomeIconUrl.FILTER_SOLID, order = 5) // Menüeintrag in der Sidebar
public class NewArticlesView extends Div {

    // Service für das Synchronisieren / Anlegen von Artikeln aus einem Fremdsystem
    private final ArticleSyncService articleSyncService;

    // Service für den Zugriff auf Lagerplätze (z.B. um Status auf USED zu setzen)
    private final StorageLocationService storageLocationService;

    // Grid, das die "Kandidaten" für neue Artikel anzeigt
    private final Grid<NewArticleCandidate> grid = new Grid<>(NewArticleCandidate.class, false);

    private final NewArticleNotificationService newArticleNotificationService;

    public NewArticlesView(ArticleSyncService articleSyncService,
                           StorageLocationService storageLocationService,
                           NewArticleNotificationService newArticleNotificationService) {
        this.articleSyncService = articleSyncService;
        this.storageLocationService = storageLocationService;
        this.newArticleNotificationService = newArticleNotificationService;

        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setSizeFull();

        configureGrid();

        layout.add(grid);
        add(layout);

        refresh();
    }

    /**
     * Konfiguriert die Spalten und Komponenten des Grids.
     * Hier werden sowohl einfache Text-Spalten als auch eine "Aktionen"-Spalte gebaut.
     */
    private void configureGrid() {
        // Spalte: Artikelnummer
        grid.addColumn(NewArticleCandidate::getArticleNumber)
                .setHeader("Artikelnummer")
                .setAutoWidth(true)
                .setFlexGrow(0); // Spalte soll nicht flexibel mitwachsen

        // Spalte: Artikelname
        grid.addColumn(NewArticleCandidate::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Spalte mit mehreren Eingabefeldern und Buttons zum Anlegen des Artikels
        grid.addComponentColumn(candidate -> {

            // Textfeld zur Anzeige des ausgewählten Lagerorts
            TextField locationField = new TextField();
            locationField.setPlaceholder("Storage Location"); // Platzhaltertext, bis etwas ausgewählt wurde
            locationField.setWidth("140px");
            locationField.setReadOnly(true); // Nutzer soll hier nicht manuell reinschreiben

            // ausgewählte Location "merken" (Workaround, weil wir im Lambda sind)
            final StorageLocation[] selectedLocationHolder = new StorageLocation[1];

            // Button, um Lagerplatz über einen Dialog auszuwählen
            Button chooseLocation = new Button("Location wählen", e -> {
                StorageLocationPickerDialog dlg =
                        new StorageLocationPickerDialog(
                                storageLocationService,
                                "Location für " + candidate.getName(),
                                selected -> {
                                    // Callback, wenn im Dialog ein Lagerplatz ausgewählt wurde
                                    if (selected != null) {
                                        selectedLocationHolder[0] = selected;              // im Array speichern
                                        locationField.setValue(selected.getGeneralId());   // im UI anzeigen
                                    }
                                });
                dlg.open(); // Dialog anzeigen
            });

            // Eingabefeld: Stück pro Palette
            IntegerField piecesPerPalletField = new IntegerField();
            piecesPerPalletField.setPlaceholder("Stk/Palette");
            piecesPerPalletField.setMin(1);        // es muss mindestens 1 sein
            piecesPerPalletField.setWidth("120px");

            // Eingabefeld: Mindestbestand
            IntegerField minStockField = new IntegerField();
            minStockField.setPlaceholder("Mindestbestand");
            minStockField.setMin(0);              // Mindestbestand darf auch 0 sein
            minStockField.setWidth("130px");

            // Button, um aus dem Kandidaten einen "richtigen" Artikel anzulegen
            Button createBtn = new Button("Artikel anlegen", click -> {
                try {
                    // Validierung: Lagerplatz muss gewählt sein
                    if (locationField.getValue() == null || locationField.getValue().isBlank()) {
                        Notification n = Notification.show("Bitte Storage Location wählen", 3000, Notification.Position.MIDDLE);
                        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    // Validierung: Stückzahl pro Palette muss > 0 sein
                    if (piecesPerPalletField.getValue() == null || piecesPerPalletField.getValue() <= 0) {
                        Notification n = Notification.show("Bitte gültige Stückzahl/Palette eingeben", 3000, Notification.Position.MIDDLE);
                        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }

                    // ArticleInfo in der Datenbank anlegen (über den Sync-Service)
                    ArticleInfo created = articleSyncService.createArticleInfoForCandidate(
                            candidate.getArticleId(),
                            locationField.getValue(),
                            piecesPerPalletField.getValue(),
                            minStockField.getValue()
                    );

                    // Lagerplatz auf USED setzen
                    if (selectedLocationHolder[0] != null) {
                        selectedLocationHolder[0].setStorageStatus("Used");
                        storageLocationService.save(selectedLocationHolder[0]);
                    }


                    Notification n = Notification.show(
                            "Artikel " + created.getArticleNumber() + " angelegt",
                            3000,
                            Notification.Position.MIDDLE
                    );
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    refresh();

                    MainLayout layout = findMainLayout();
                    if (layout != null) {
                        layout.refreshArticleBadge();
                    }

                } catch (Exception ex) {
                    // Fehlerfall: Fehlermeldung anzeigen
                    Notification n = Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            // Layout für die Zeile in der "Anlage"-Spalte:
            // [Location-Feld] [Location wählen] [Stk/Palette] [Mindestbestand] [Artikel anlegen]
            HorizontalLayout rowLayout = new HorizontalLayout(
                    locationField,
                    chooseLocation,
                    piecesPerPalletField,
                    minStockField,
                    createBtn
            );
            rowLayout.setAlignItems(FlexComponent.Alignment.BASELINE); // Elemente in einer Linie ausrichten
            return rowLayout;
        }).setHeader("Anlage"); // Spaltenüberschrift

        // Grid-Größe setzen (hier feste Höhe + volle Breite)
        grid.setHeight("500px");
        grid.setWidthFull();
    }

    private MainLayout findMainLayout() {
        com.vaadin.flow.component.Component current = this;
        while (current.getParent().isPresent()) {
            current = current.getParent().get();
            if (current instanceof MainLayout layout) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Lädt die Liste der neuen Artikelkandidaten neu und zeigt sie im Grid an.
     * Wird beim Start und nach dem Anlegen eines Artikels aufgerufen.
     */
    private void refresh() {
        List<NewArticleCandidate> items = articleSyncService.findNewArticlesFromContingents();
        grid.setItems(items);
    }
}
