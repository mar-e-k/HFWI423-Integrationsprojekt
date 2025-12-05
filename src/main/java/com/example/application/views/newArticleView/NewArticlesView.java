package com.example.application.views.newArticleView;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.StorageLocationService;
import com.example.application.views.MainLayout;
import com.example.application.views.components.StorageLocationPickerDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
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
@Menu(
        title = "Neue Artikel",
        icon = LineAwesomeIconUrl.FILTER_SOLID,
        order = 5
)
public class NewArticlesView extends Div {

    private final ArticleSyncService articleSyncService;
    private final StorageLocationService storageLocationService;

    private final Grid<NewArticleCandidate> grid =
            new Grid<>(NewArticleCandidate.class, false);

    public NewArticlesView(ArticleSyncService articleSyncService,
                           StorageLocationService storageLocationService) {
        this.articleSyncService = articleSyncService;
        this.storageLocationService = storageLocationService;

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

    private void configureGrid() {
        grid.addColumn(NewArticleCandidate::getArticleNumber)
                .setHeader("Artikelnummer")
                .setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(NewArticleCandidate::getName)
                .setHeader("Name")
                .setAutoWidth(true).setFlexGrow(0);

        // Eingabespalte: Location + Pieces/Palette + Button
        grid.addComponentColumn(candidate -> {

                    TextField locationField = new TextField();
                    locationField.setPlaceholder("Storage Location");
                    locationField.setWidth("140px");
                    locationField.setReadOnly(true);

                    Button chooseLocation = new Button("Location wählen", e -> {
                        StorageLocationPickerDialog dlg =
                                new StorageLocationPickerDialog(
                                        storageLocationService,
                                        "Location für " + candidate.getName(),
                                        selected -> {
                                            if (selected != null) {
                                                locationField.setValue(selected.getGeneralId());
                                            }
                                        });
                        dlg.open();
                    });

                    IntegerField piecesPerPalletField = new IntegerField();
                    piecesPerPalletField.setPlaceholder("Stk/Palette");
                    piecesPerPalletField.setMin(1);
                    piecesPerPalletField.setWidth("120px");

                    Button createBtn = new Button("Artikel anlegen", click -> {
                        try {
                            if (locationField.getValue() == null || locationField.getValue().isBlank()) {
                                Notification n = Notification.show("Bitte Storage Location wählen", 3000, Position.MIDDLE);
                                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }
                            if (piecesPerPalletField.getValue() == null || piecesPerPalletField.getValue() <= 0) {
                                Notification n = Notification.show("Bitte gültige Stückzahl/Palette eingeben", 3000, Position.MIDDLE);
                                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }

                            ArticleInfo created = articleSyncService.createArticleInfoForCandidate(
                                    candidate.getArticleId(),
                                    locationField.getValue(),
                                    piecesPerPalletField.getValue()
                            );

                            Notification n = Notification.show(
                                    "Artikel " + created.getArticleNumber() + " angelegt",
                                    3000, Position.MIDDLE);
                            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                            refresh();
                        } catch (Exception ex) {
                            Notification n = Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
                            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    });

                    HorizontalLayout rowLayout = new HorizontalLayout(
                            locationField,
                            chooseLocation,
                            piecesPerPalletField,
                            createBtn
                    );
                    rowLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
                    return rowLayout;
                }).setHeader("Anlage");

        grid.setHeight("500px");
        grid.setWidthFull();
    }

    private void refresh() {
        List<NewArticleCandidate> items =
                articleSyncService.findNewArticlesFromContingents();
        grid.setItems(items);
    }
}
