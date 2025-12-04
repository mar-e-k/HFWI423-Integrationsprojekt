package com.example.application.views.newArticleView;

import com.example.application.services.ArticleSyncService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@PageTitle("Neue Artikel")
@Route(value = "new-articles", layout = MainLayout.class)
@Menu(
        title = "Neue Artikel",
        icon = LineAwesomeIconUrl.FILTER_SOLID,
        order = 30
)
public class NewArticlesView extends Div {

    private final ArticleSyncService articleSyncService;
    private final Grid<NewArticleCandidate> grid =
            new Grid<>(NewArticleCandidate.class, false);

    public NewArticlesView(ArticleSyncService articleSyncService) {
        this.articleSyncService = articleSyncService;
        setSizeFull();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);

        // Spalten: Artikelnummer & Name aus externer Tabelle
        grid.addColumn(NewArticleCandidate::getArticleNumber)
                .setHeader("Artikelnummer")
                .setAutoWidth(true);
        grid.addColumn(NewArticleCandidate::getName)
                .setHeader("Name")
                .setAutoWidth(true);

        // Eingabe-Spalte: Storage Location + Pieces per Pallet + Button
        grid.addComponentColumn(candidate -> {
            TextField storageField = new TextField();
            storageField.setPlaceholder("Storage Location");
            storageField.setWidth("150px");

            IntegerField piecesField = new IntegerField();
            piecesField.setPlaceholder("Stk/Palette");
            piecesField.setMin(1);
            piecesField.setWidth("120px");

            Button createBtn = new Button("Artikel anlegen", click -> {
                try {
                    articleSyncService.createArticleInfoForCandidate(
                            candidate.getArticleId(),
                            storageField.getValue(),
                            piecesField.getValue()
                    );
                    Notification n = Notification.show(
                            "Artikel " + candidate.getArticleNumber() + " angelegt",
                            3000, Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refresh();
                } catch (Exception ex) {
                    Notification n = Notification.show(ex.getMessage(), 5000, Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            return new HorizontalLayout(storageField, piecesField, createBtn);
        }).setHeader("Anlage");

        grid.setHeight("400px");
        layout.add(grid);
        add(layout);

        refresh();
    }

    private void refresh() {
        List<NewArticleCandidate> items =
                articleSyncService.findNewArticlesFromContingents();
        grid.setItems(items);
    }
}
