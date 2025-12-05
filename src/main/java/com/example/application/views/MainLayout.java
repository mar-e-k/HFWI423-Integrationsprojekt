package com.example.application.views;

import com.example.application.services.NewArticleNotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
 * Globales Layout (Header + Drawer + Footer).
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

        // Polling für Badge-Aktualisierung
        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> updateArticleBadge());

        //Hintergrundfarbe für die App
       // getElement().getThemeList().add("dark");
    }

    // ---------------------------------------------------------------------
    // Badge-Logik
    // ---------------------------------------------------------------------

    private void updateArticleBadge() {
        if (articleBadge == null) {
            return;
        }
        int count = newArticleNotificationService.getCount();
        articleBadge.setText(String.valueOf(count));
    }

    // ---------------------------------------------------------------------
    // Header
    // ---------------------------------------------------------------------

    private void addHeaderContent() {
        // Left: Drawer Toggle + AppName
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menü umschalten");

        Span appName = new Span("Logistic");
        appName.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Margin.NONE
        );

        // Center: aktueller Seitentitel
        viewTitle = new H1();
        viewTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        // Right: User-Bereich (Platzhalter)
        Avatar userAvatar = new Avatar("User");
        userAvatar.addClassNames(LumoUtility.Margin.End.MEDIUM);

        Span userName = new Span("Demo User");
        userName.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        HorizontalLayout userArea = new HorizontalLayout(userName, userAvatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.addClassNames(LumoUtility.Gap.SMALL);

        HorizontalLayout headerBar = new HorizontalLayout(toggle, appName, viewTitle, userArea);
        headerBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerBar.setWidthFull();
        headerBar.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.BoxSizing.BORDER
        );

        // einfache Border unten per Style setzen
        headerBar.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        // Titel in der Mitte, User rechts
        headerBar.expand(viewTitle);
        headerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        addToNavbar(headerBar);
    }

    // ---------------------------------------------------------------------
    // Drawer / Navigation
    // ---------------------------------------------------------------------

    private void addDrawerContent() {
        // App-Brand im Drawer
        H2 drawerTitle = new H2("Logistic Cockpit");
        drawerTitle.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Margin.NONE
        );

        Span subtitle = new Span("Warehouse & Stock Management");
        subtitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );

        VerticalHeader header = new VerticalHeader(drawerTitle, subtitle);

        Scroller scroller = new Scroller(createNavigation());
        scroller.addClassNames(
                LumoUtility.Padding.SMALL
        );

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassNames(
                LumoUtility.Padding.NONE,
                LumoUtility.Gap.SMALL
        );

        // "Überschrift" für die Navigation als deaktiviertes Item
        SideNavItem sectionTitleItem = new SideNavItem("Navigation");
        sectionTitleItem.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Padding.Top.SMALL
        );
        sectionTitleItem.setEnabled(false);  // nicht klickbar
        nav.addItem(sectionTitleItem);

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            SideNavItem item;
            if (entry.icon() != null) {
                item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
            } else {
                item = new SideNavItem(entry.title(), entry.path());
            }

            item.addClassNames(
                    LumoUtility.BorderRadius.MEDIUM,
                    LumoUtility.Padding.Horizontal.SMALL
            );

            String path = entry.path();

            // Pfad der View mit den neuen Artikeln
            if ("/new-articles".equals(path)) {

                articleNavItem = item;
                articleBadge = new Span();
                articleBadge.addClassNames(
                        LumoUtility.Padding.Horizontal.XSMALL,
                        LumoUtility.Padding.Vertical.XSMALL,
                        LumoUtility.BorderRadius.LARGE
                );
                articleBadge.getElement().getThemeList().add("badge pill primary");

                articleBadge.setText("0");
                articleBadge.setVisible(true);

                item.setSuffixComponent(articleBadge);
            }

            nav.addItem(item);
        });

        return nav;
    }

    private static class VerticalHeader extends Header {
        public VerticalHeader(H2 title, Span subtitle) {
            addClassNames(
                    LumoUtility.Padding.MEDIUM,
                    LumoUtility.Display.FLEX,
                    LumoUtility.FlexDirection.COLUMN,
                    LumoUtility.Gap.XSMALL
            );

            // Unterkante als feine Linie
            getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

            add(title, subtitle);
        }
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.XSMALL
        );

        // Oberkante als feine Linie
        footer.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        Span info = new Span("© " + java.time.Year.now().getValue() + " Logistic Demo • v1.0");
        footer.add(info);
        return footer;
    }

    // ---------------------------------------------------------------------
    // Page Title
    // ---------------------------------------------------------------------

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
