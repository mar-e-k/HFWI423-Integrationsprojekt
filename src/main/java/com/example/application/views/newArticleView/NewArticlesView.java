package com.example.application.views.newArticleView;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.BadgeNotifier;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.StorageLocationService;
import com.example.application.views.MainLayout;
import com.example.application.views.components.StorageLocationPickerDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

@PageTitle("Neue Artikel")
@Route(value = "new-articles", layout = MainLayout.class)
@Menu(title = "Neue Artikel", icon = LineAwesomeIconUrl.FILTER_SOLID, order = 5)
public class NewArticlesView extends Div {

    private final ArticleSyncService articleSyncService;
    private final StorageLocationService storageLocationService;
    private final BadgeNotifier badgeNotifier;

    private final Grid<NewArticleCandidate> grid = new Grid<>(NewArticleCandidate.class, false);

    public NewArticlesView(ArticleSyncService articleSyncService,
                           StorageLocationService storageLocationService,
                           BadgeNotifier badgeNotifier) {
        this.articleSyncService = articleSyncService;
        this.storageLocationService = storageLocationService;
        this.badgeNotifier = badgeNotifier;

        setSizeFull();
        addClassName("view-page");

        // Toolbar
        Button refreshButton = new Button("Aktualisieren", e -> refresh());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("view-toolbar");

        configureGrid();

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

    private void configureGrid() {
        grid.addColumn(NewArticleCandidate::getArticleNumber)
                .setHeader("Artikelnummer")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(NewArticleCandidate::getName)
                .setHeader("Name")
                .setFlexGrow(1);

        grid.addComponentColumn(candidate -> {
            TextField locationField = new TextField();
            locationField.setPlaceholder("Lagerplatz");
            locationField.setWidth("100px");
            locationField.setReadOnly(true);

            final StorageLocation[] selectedLocationHolder = new StorageLocation[1];

            Button chooseLocation = new Button("Location waehlen", e -> {
                StorageLocationPickerDialog dlg = new StorageLocationPickerDialog(
                        storageLocationService,
                        "Location fuer " + candidate.getName(),
                        selected -> {
                            if (selected != null) {
                                selectedLocationHolder[0] = selected;
                                locationField.setValue(selected.getGeneralId());
                            }
                        });
                dlg.open();
            });
            chooseLocation.addThemeVariants(ButtonVariant.LUMO_SMALL);

            IntegerField piecesPerPalletField = new IntegerField();
            piecesPerPalletField.setPlaceholder("Stk/Pal.");
            piecesPerPalletField.setMin(1);
            piecesPerPalletField.setWidth("90px");

            IntegerField minStockField = new IntegerField();
            minStockField.setPlaceholder("Mindestbest. (Pal.)");
            minStockField.setMin(0);
            minStockField.setWidth("130px");

            Button createBtn = new Button("Artikel anlegen", click -> {
                try {
                    if (locationField.getValue() == null || locationField.getValue().isBlank()) {
                        Notification n = Notification.show("Bitte Storage Location waehlen", 3000, Notification.Position.MIDDLE);
                        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    if (piecesPerPalletField.getValue() == null || piecesPerPalletField.getValue() <= 0) {
                        Notification n = Notification.show("Bitte gueltige Stueckzahl/Palette eingeben", 3000, Notification.Position.MIDDLE);
                        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }

                    ArticleInfo created = articleSyncService.createArticleInfoForCandidate(
                            candidate.getArticleId(),
                            locationField.getValue(),
                            piecesPerPalletField.getValue(),
                            minStockField.getValue()
                    );

                    if (selectedLocationHolder[0] != null) {
                        selectedLocationHolder[0].setStorageStatus("Used");
                        storageLocationService.save(selectedLocationHolder[0]);
                    }

                    Notification n = Notification.show("Artikel " + created.getArticleNumber() + " angelegt", 3000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    refresh();

                    MainLayout mainLayout = findMainLayout();
                    if (mainLayout != null) {
                        mainLayout.refreshArticleBadge();
                    }

                    badgeNotifier.notifyNow();

                } catch (Exception ex) {
                    Notification n = Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            createBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

            HorizontalLayout rowLayout = new HorizontalLayout(locationField, chooseLocation, piecesPerPalletField, minStockField, createBtn);
            rowLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            return rowLayout;
        }).setHeader("Anlage").setFlexGrow(2);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName("app-grid");
        grid.setSizeFull();
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

    private void refresh() {
        List<NewArticleCandidate> items = articleSyncService.findNewArticlesFromContingents();
        grid.setItems(items);
    }
}
