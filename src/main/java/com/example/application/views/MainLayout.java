package com.example.application.views;

import com.example.application.services.NewArticleNotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;
    private final NewArticleNotificationService newArticleNotificationService;
    private SideNavItem articleNavItem;
    private Span articleBadge;

    public MainLayout(NewArticleNotificationService newArticleNotificationService) {
        this.newArticleNotificationService = newArticleNotificationService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();

        // damit das UI regelmäßig den Zähler abfragt
        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> updateArticleBadge());
    }

    private void updateArticleBadge() {
        if (articleBadge == null) {
            return;
        }

        int count = newArticleNotificationService.getCount();
        articleBadge.setText(String.valueOf(count));
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Logistic");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {

            SideNavItem item;
            if (entry.icon() != null) {
                item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
            } else {
                item = new SideNavItem(entry.title(), entry.path());
            }
            System.out.println("MENU: title=" + entry.title() + " | path=" + entry.path()); // Debug
            String path = entry.path();

            //den Pfad der Artikel-View eintragen (noch nicht vorhanden, kommt noch. ISt zum Testen)
            if ("/new-articles".equals(path)) {   // anpassen

                articleNavItem = item;
                articleBadge = new Span();
                articleBadge.addClassNames(
                        LumoUtility.Padding.XSMALL,
                        LumoUtility.BorderRadius.LARGE
                );
                articleBadge.getElement().getThemeList().add("badge pill contrast");

                // immer sichtbar, initial 0
                articleBadge.setText("0");
                articleBadge.setVisible(true);

                // Badge an den Menüpunkt anhängen
                item.setSuffixComponent(articleBadge);
            }

            nav.addItem(item);
        });

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
