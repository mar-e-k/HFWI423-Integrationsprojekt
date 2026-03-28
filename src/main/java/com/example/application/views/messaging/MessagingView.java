package com.example.application.views.messaging;

import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import com.example.application.amqp.einkaufEvents.EinkaufEventPublisher;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Messaging")
@Route(value = "messaging", layout = com.example.application.views.MainLayout.class)
@Menu(
        title = "Messaging",
        icon = LineAwesomeIconUrl.ENVELOPE_SOLID,
        order = 50
)
public class MessagingView extends Div {

    private final MessagingEventService messagingEventService;
    private final EinkaufEventPublisher einkaufEventPublisher;

    private final Grid<MessagingEvent> grid = new Grid<>(MessagingEvent.class, false);

    public MessagingView(MessagingEventService messagingEventService, EinkaufEventPublisher einkaufEventPublisher) {
        this.messagingEventService = messagingEventService;
        this.einkaufEventPublisher = einkaufEventPublisher;

        setSizeFull();

        // Toolbar oben mit Button zum Publish NewDeal
        TextField articleIdField = new TextField("Article ID");
        articleIdField.setPlaceholder("z.B. 123");
        Button publishButton = new Button("Publish NewDeal", e -> publishNewDeal(articleIdField.getValue()));
        publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(articleIdField, publishButton);
        toolbar.setWidthFull();

        // Grid für empfangene Events
        configureGrid();

        // Layout
        VerticalLayout layout = new VerticalLayout(toolbar, grid);
        layout.setSizeFull();
        layout.setFlexGrow(1, grid);

        add(layout);

        // Lade Daten
        updateGrid();
    }

    private void configureGrid() {
        grid.addColumn(MessagingEvent::getEventType)
                .setHeader("Event Typ")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(MessagingEvent::getArticleId)
                .setHeader("Article ID")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(MessagingEvent::getAmount)
                .setHeader("Amount")
                .setAutoWidth(true);

        grid.addColumn(MessagingEvent::getDetails)
                .setHeader("Details")
                .setAutoWidth(true);

        grid.addColumn(MessagingEvent::getReceivedAt)
                .setHeader("Empfangen am")
                .setAutoWidth(true)
                .setSortable(true);
    }

    private void publishNewDeal(String articleIdStr) {
        try {
            long articleId = Long.parseLong(articleIdStr);
            einkaufEventPublisher.publishNewDeal(articleId);
            Notification.show("NewDeal Event erfolgreich versendet für Article ID: " + articleId, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (NumberFormatException e) {
            Notification.show("Ungültige Article ID: " + articleIdStr, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Fehler beim Versenden: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        grid.setItems(messagingEventService.findAll());
    }
}
