package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import fhdw.de.einkauf_service.entity.ReceivedDealNotification;
import fhdw.de.einkauf_service.repository.ReceivedDealNotificationRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "messages", layout = MainLayout.class)
public class MessageView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final ReceivedDealNotificationRepository notificationRepository;
    private final Grid<ReceivedDealNotification> grid = new Grid<>(ReceivedDealNotification.class, false);

    public MessageView(ReceivedDealNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;

        setSizeFull();
        setPadding(true);

        add(new H2("Nachrichten von Logistik"));
        configureGrid();
        add(grid);
        setFlexGrow(1, grid);
    }

    private void configureGrid() {
        grid.addColumn(new ComponentRenderer<>(notification -> {
            Span span = new Span(notification.getArticleNumber());
            if (!notification.isRead()) {
                span.getStyle().set("font-weight", "bold");
            }
            return span;
        })).setHeader("Artikelnummer (GTIN)").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(notification -> {
            Span span = new Span(notification.getArticleName());
            if (!notification.isRead()) {
                span.getStyle().set("font-weight", "bold");
            }
            return span;
        })).setHeader("Artikelname").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(notification -> {
            Span span = new Span(notification.getReceivedAt().format(FORMATTER));
            if (!notification.isRead()) {
                span.getStyle().set("font-weight", "bold");
            }
            return span;
        })).setHeader("Empfangen am").setAutoWidth(true).setSortable(true);

        grid.addColumn(new ComponentRenderer<>(notification -> {
            if (!notification.isRead()) {
                Span badge = new Span("Neu");
                badge.getElement().getThemeList().add("badge error pill");
                return badge;
            }
            return new Span();
        })).setHeader("Status").setAutoWidth(true).setFlexGrow(0);

        grid.setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<ReceivedDealNotification> notifications =
                notificationRepository.findAllByOrderByReceivedAtDesc();
        grid.setItems(notifications);

        // Mark all as read
        List<ReceivedDealNotification> unread = notificationRepository.findAllByReadFalse();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
