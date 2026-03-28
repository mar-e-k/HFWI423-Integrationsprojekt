package com.example.application.views;

import com.example.application.services.NewArticleNotificationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;

import java.time.Year;
import java.util.List;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final NewArticleNotificationService newArticleNotificationService;

    private H1 viewTitle;
    private Span articleBadge;

    public MainLayout(NewArticleNotificationService newArticleNotificationService) {
        this.newArticleNotificationService = newArticleNotificationService;

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);

        addHeaderContent();
        addDrawerContent();

        configurePolling();
        updateArticleBadge();
    }

    private void configurePolling() {
        UI currentUi = UI.getCurrent();
        if (currentUi != null) {
            currentUi.setPollInterval(5000);
            currentUi.addPollListener(event -> updateArticleBadge());
        }
    }

    private void addHeaderContent() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel("Menü umschalten");

        Span appName = new Span("Logistic");
        appName.addClassName("app-name");

        viewTitle = new H1();
        viewTitle.addClassName("app-view-title");

        HorizontalLayout brandArea = new HorizontalLayout(drawerToggle, appName);
        brandArea.setPadding(false);
        brandArea.setSpacing(false);
        brandArea.setAlignItems(FlexComponent.Alignment.CENTER);
        brandArea.addClassName("app-topbar-left");

        Avatar avatar = new Avatar("User");
        avatar.addClassName("app-user-avatar");

        Span userName = new Span("Demo User");
        userName.addClassName("app-user-name");

        HorizontalLayout userArea = new HorizontalLayout(userName, avatar);
        userArea.setPadding(false);
        userArea.setSpacing(false);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.addClassName("app-topbar-user");

        HorizontalLayout headerBar = new HorizontalLayout(brandArea, viewTitle, userArea);
        headerBar.setWidthFull();
        headerBar.setPadding(false);
        headerBar.setSpacing(false);
        headerBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerBar.expand(viewTitle);
        headerBar.addClassName("app-topbar");

        addToNavbar(headerBar);
    }

    private void addDrawerContent() {
        Header drawerHeader = createDrawerHeader();

        Scroller navigationScroller = new Scroller(createNavigation());
        navigationScroller.setSizeFull();
        navigationScroller.addClassName("app-drawer-scroller");

        Footer drawerFooter = createFooter();

        VerticalLayout drawerLayout = new VerticalLayout(drawerHeader, navigationScroller, drawerFooter);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setFlexGrow(1, navigationScroller);
        drawerLayout.addClassName("app-drawer-layout");

        addToDrawer(drawerLayout);
    }

    private Header createDrawerHeader() {
        H2 title = new H2("Logistic Cockpit");
        title.addClassName("app-drawer-title");

        Span subtitle = new Span("Warehouse & Stock Management");
        subtitle.addClassName("app-drawer-subtitle");

        Header header = new Header(title, subtitle);
        header.addClassName("app-drawer-header");

        return header;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("app-sidenav");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        for (MenuEntry entry : menuEntries) {
            nav.addItem(createNavItem(entry));
        }

        return nav;
    }

    private SideNavItem createNavItem(MenuEntry entry) {
        SideNavItem item;

        if (entry.icon() != null) {
            item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
        } else {
            item = new SideNavItem(entry.title(), entry.path());
        }

        item.addClassName("app-nav-item");

        String path = entry.path();
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;

        if ("new-articles".equals(normalizedPath)) {
            articleBadge = createArticleBadge();
            item.setSuffixComponent(articleBadge);
        }

        return item;
    }

    private Span createArticleBadge() {
        Span badge = new Span("0");
        badge.addClassName("menu-badge");
        badge.setVisible(false);
        return badge;
    }

    private Footer createFooter() {
        Span info = new Span("© " + Year.now().getValue() + " Logistic Demo • v1.0");
        info.addClassName("app-drawer-footer-text");

        Footer footer = new Footer(info);
        footer.addClassName("app-drawer-footer");

        return footer;
    }

    private void updateArticleBadge() {
        if (articleBadge == null) {
            return;
        }

        int count = newArticleNotificationService.getCount();
        articleBadge.setText(String.valueOf(count));
        articleBadge.setVisible(count > 0);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
        updateArticleBadge();
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}