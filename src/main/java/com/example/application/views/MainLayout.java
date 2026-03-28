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
import com.vaadin.flow.theme.lumo.LumoUtility;

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
        getStyle().set("--vaadin-app-layout-drawer-width", "280px");


        addHeaderContent();
        addDrawerContent();

        configurePolling();
        updateArticleBadge();
    }

    // --------------------------------------------------
    // Initialisierung
    // --------------------------------------------------

    private void configurePolling() {
        UI currentUi = UI.getCurrent();
        if (currentUi != null) {
            currentUi.setPollInterval(5000);
            currentUi.addPollListener(event -> updateArticleBadge());
        }
    }

    // --------------------------------------------------
    // Header
    // --------------------------------------------------

    private void addHeaderContent() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel("Menü umschalten");

        Span appName = new Span("Logistic");
        appName.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.MEDIUM,
                LumoUtility.TextColor.SECONDARY
        );

        viewTitle = new H1();
        viewTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Margin.NONE
        );

        HorizontalLayout brandArea = new HorizontalLayout(drawerToggle, appName);
        brandArea.setAlignItems(FlexComponent.Alignment.CENTER);
        brandArea.setSpacing(true);
        brandArea.getStyle().set("gap", "0.75rem");

        Avatar avatar = new Avatar("User");
        avatar.getStyle().set("background-color", "var(--lumo-contrast-10pct)");

        Span userName = new Span("Demo User");
        userName.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        HorizontalLayout userArea = new HorizontalLayout(userName, avatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.getStyle().set("gap", "0.5rem");

        HorizontalLayout header = new HorizontalLayout(brandArea, viewTitle, userArea);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);

        header.addClassNames(
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.BoxSizing.BORDER
        );

        header.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("min-height", "72px")
                .set("padding-left", "1rem")
                .set("padding-right", "1rem");

        addToNavbar(header);
    }

    // --------------------------------------------------
    // Drawer
    // --------------------------------------------------

    private void addDrawerContent() {
        Header drawerHeader = createDrawerHeader();
        Scroller navigationScroller = new Scroller(createNavigation());
        navigationScroller.setSizeFull();
        navigationScroller.addClassNames(LumoUtility.Padding.SMALL);

        Footer drawerFooter = createFooter();

        VerticalLayout drawerLayout = new VerticalLayout(drawerHeader, navigationScroller, drawerFooter);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setFlexGrow(1, navigationScroller);

        addToDrawer(drawerLayout);
    }

    private Header createDrawerHeader() {
        H2 title = new H2("Logistic Cockpit");
        title.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Margin.NONE
        );

        Span subtitle = new Span("Warehouse & Stock Management");
        subtitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );
        subtitle.getStyle()
                .set("line-height", "1.3")
                .set("white-space", "normal");

        Header header = new Header(title, subtitle);
        header.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Gap.XSMALL
        );

        header.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-base-color)")
                .set("min-height", "96px")
                .set("justify-content", "center");

        return header;
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassNames(
                LumoUtility.Padding.XSMALL,
                LumoUtility.Gap.XSMALL
        );

        Span sectionTitle = new Span("Navigation");
        sectionTitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Padding.Top.SMALL
        );

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();

        VerticalLayout navWrapper = new VerticalLayout();
        navWrapper.setPadding(false);
        navWrapper.setSpacing(false);
        navWrapper.add(sectionTitle);

        for (MenuEntry entry : menuEntries) {
            SideNavItem item = createNavItem(entry);
            nav.addItem(item);
        }

        navWrapper.add(nav);
        return nav;
    }

    private SideNavItem createNavItem(MenuEntry entry) {
        SideNavItem item;

        if (entry.icon() != null) {
            item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
        } else {
            item = new SideNavItem(entry.title(), entry.path());
        }

        item.addClassNames(
                LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.Horizontal.SMALL,
                LumoUtility.Padding.Vertical.XSMALL
        );

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
        badge.addClassNames(
                LumoUtility.Padding.Horizontal.XSMALL,
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.FontWeight.SEMIBOLD
        );
        badge.getElement().getThemeList().add("badge pill primary");
        badge.setVisible(false);
        return badge;
    }

    private Footer createFooter() {
        Span info = new Span("© " + java.time.Year.now().getValue() + " Logistic Demo • v1.0");
        info.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );

        Footer footer = new Footer(info);
        footer.addClassNames(LumoUtility.Padding.MEDIUM);
        footer.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        return footer;
    }

    // --------------------------------------------------
    // Badge
    // --------------------------------------------------

    private void updateArticleBadge() {
        if (articleBadge == null) {
            return;
        }

        int count = newArticleNotificationService.getCount();
        articleBadge.setText(String.valueOf(count));
        articleBadge.setVisible(count > 0);
    }

    // --------------------------------------------------
    // Navigation Title
    // --------------------------------------------------

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
        updateArticleBadge();
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}