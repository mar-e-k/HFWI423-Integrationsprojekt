package fhdw.de.einkauf_service.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.dependency.CssImport;
import fhdw.de.einkauf_service.repository.ReceivedDealNotificationRepository;

@CssImport("./styles/navigation.css")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final ReceivedDealNotificationRepository notificationRepository;
    private final Span unreadBadge = new Span();

    public MainLayout(ReceivedDealNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        createHeader();
    }

    private void createHeader() {
        H1 title = new H1("Einkaufssystem");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        RouterLink homeLink = new RouterLink("Home", HomeView.class);
        RouterLink articlesLink = new RouterLink("Artikel", ArticleView.class);
        RouterLink suppliersLink = new RouterLink("Lieferanten", SupplierView.class);
        RouterLink shelvesLink = new RouterLink("Regale", ShelfManagementView.class);
        RouterLink placementsLink = new RouterLink("Artikel platzieren", ShelfPlacementEditorView.class);
        RouterLink cartLink = new RouterLink("Warenkorb", ShoppingCartView.class);
        RouterLink contingentsLink = new RouterLink("Kontingente", ContingentView.class);
        RouterLink ordersLink = new RouterLink("Bestellhistorie", OrderView.class);

        // Messages link with badge
        unreadBadge.getElement().getThemeList().add("badge error pill small");
        unreadBadge.setVisible(false);
        Span envelopeIcon = new Span(VaadinIcon.ENVELOPE.create(), unreadBadge);
        envelopeIcon.getStyle().set("display", "flex").set("align-items", "center").set("gap", "4px");
        RouterLink messagesLink = new RouterLink(MessageView.class);
        messagesLink.add(envelopeIcon);
        messagesLink.setHighlightCondition(HighlightConditions.sameLocation());
        messagesLink.addClassName("nav-link");

        homeLink.setHighlightCondition(HighlightConditions.sameLocation());
        articlesLink.setHighlightCondition(HighlightConditions.sameLocation());
        suppliersLink.setHighlightCondition(HighlightConditions.sameLocation());
        shelvesLink.setHighlightCondition(HighlightConditions.sameLocation());
        placementsLink.setHighlightCondition(HighlightConditions.sameLocation());
        ordersLink.setHighlightCondition(HighlightConditions.sameLocation());
        cartLink.setHighlightCondition(HighlightConditions.sameLocation());
        contingentsLink.setHighlightCondition(HighlightConditions.sameLocation());

        homeLink.addClassName("nav-link");
        articlesLink.addClassName("nav-link");
        suppliersLink.addClassName("nav-link");
        shelvesLink.addClassName("nav-link");
        placementsLink.addClassName("nav-link");
        ordersLink.addClassName("nav-link");
        cartLink.addClassName("nav-link");
        contingentsLink.addClassName("nav-link");

        HorizontalLayout navLinks = new HorizontalLayout(
                homeLink, articlesLink, suppliersLink, cartLink,
                contingentsLink, ordersLink, shelvesLink, placementsLink, messagesLink
        );
        navLinks.setSpacing(true);
        navLinks.setPadding(true);
        navLinks.setAlignItems(FlexComponent.Alignment.CENTER);
        navLinks.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Margin.NONE, LumoUtility.Padding.Vertical.SMALL);

        HorizontalLayout header = new HorizontalLayout(title, navLinks);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.Padding.Vertical.SMALL
        );

        addToNavbar(header);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        long unread = notificationRepository.countByReadFalse();
        if (unread > 0) {
            unreadBadge.setText(String.valueOf(unread));
            unreadBadge.setVisible(true);
        } else {
            unreadBadge.setVisible(false);
        }
    }
}
