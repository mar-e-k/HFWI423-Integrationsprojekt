package com.example.application.views.messaging;

import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import com.example.application.amqp.einkaufEvents.EinkaufEventPublisher;
import com.example.application.amqp.storeEvents.LogisticEventPublisher;
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
    private final LogisticEventPublisher logisticEventPublisher;
    private final Grid<MessagingEvent> grid = new Grid<>(MessagingEvent.class, false);

    public MessagingView(MessagingEventService messagingEventService, EinkaufEventPublisher einkaufEventPublisher, LogisticEventPublisher logisticEventPublisher) {
        this.messagingEventService = messagingEventService;
        this.einkaufEventPublisher = einkaufEventPublisher;
        this.logisticEventPublisher = logisticEventPublisher;

        setSizeFull();
        addClassName("view-page");

        // Toolbar
        TextField articleIdField = new TextField();
        articleIdField.setPlaceholder("Article ID (z.B. 123)");
        articleIdField.setWidth("200px");

        Button publishButton = new Button("Publish NewDeal", e -> publishNewDeal(articleIdField.getValue()));
        publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button testMessageButton = new Button("Test Message", e -> sendTestMessage());
        testMessageButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button refreshButton = new Button("Aktualisieren", e -> updateGrid());

        Button deleteAllButton = new Button("Alle loeschen", e -> {
            messagingEventService.deleteAll();
            updateGrid();
            Notification.show("Alle Messaging-Events geloescht", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteAllButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(articleIdField, publishButton, testMessageButton, refreshButton, deleteAllButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("view-toolbar");

        // Grid
        grid.addComponentColumn(item -> {
            String type = item.getEventType() != null ? item.getEventType() : "-";
            Span badge = new Span(type);
            badge.addClassNames("badge", "badge-purple");
            return badge;
        }).setHeader("Event Typ").setAutoWidth(true).setSortable(true);

        grid.addColumn(MessagingEvent::getArticleId).setHeader("Article ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(MessagingEvent::getAmount).setHeader("Amount").setAutoWidth(true);
        grid.addColumn(MessagingEvent::getDetails).setHeader("Details").setFlexGrow(1);
        grid.addColumn(MessagingEvent::getReceivedAt).setHeader("Empfangen am").setAutoWidth(true).setSortable(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addClassName("app-grid");
        grid.setSizeFull();

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, grid);

        Div card = new Div(content);
        card.addClassName("content-card");
        card.setSizeFull();
        add(card);

        updateGrid();
    }

    private void publishNewDeal(String articleIdStr) {
        try {
            long articleId = Long.parseLong(articleIdStr);
            einkaufEventPublisher.publishNewDeal(articleId);
            Notification.show("NewDeal Event erfolgreich versendet für Article ID: " + articleId, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (NumberFormatException e) {
            Notification.show("Ungueltige Article ID: " + articleIdStr, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Fehler beim Versenden: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void sendTestMessage() {
        logisticEventPublisher.publishArticleDelivery(1L, 10L, 5L);
    }

    private void updateGrid() {
        grid.setItems(messagingEventService.findAll());
    }
}
