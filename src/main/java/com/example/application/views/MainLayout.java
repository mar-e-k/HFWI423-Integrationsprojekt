package com.example.application.views;

import com.example.application.services.NewArticleCountService;
import com.example.application.services.NewArticleNotificationService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.shared.Registration;

import java.time.Year;
import java.util.List;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private final NewArticleNotificationService newArticleNotificationService;
    private final NewArticleCountService newArticleCountService;

    private H1 viewTitle;
    private Span articleBadge;
    private Registration broadcasterRegistration;

    public MainLayout(NewArticleNotificationService newArticleNotificationService,
                      NewArticleCountService newArticleCountService) {
        this.newArticleNotificationService = newArticleNotificationService;
        this.newArticleCountService = newArticleCountService;

        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);

        addHeaderContent();
        addDrawerContent();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();

        broadcasterRegistration = newArticleNotificationService.register(
                ui,
                count -> updateBadgeWithCount(count)
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }

    /**
     * Direkter Aufruf aus NewArticlesView (gleiche UI, kein ui.access() nötig).
     */
    public void refreshArticleBadge() {
        updateArticleBadge();
    }

    private void updateArticleBadge() {
        int count = newArticleCountService.getCount();
        updateBadgeWithCount(count);
    }

    private void updateBadgeWithCount(int count) {
        if (articleBadge == null) return;
        articleBadge.setText(String.valueOf(count));
        articleBadge.setVisible(count > 0);
    }

    private void addHeaderContent() {
        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel("Menü umschalten");
        drawerToggle.addClassName("app-drawer-toggle");

        Span appName = new Span("Logistic Cockpit");
        appName.addClassName("app-header-eyebrow");

        viewTitle = new H1();
        viewTitle.addClassName("app-view-title");

        VerticalLayout titleBlock = new VerticalLayout(appName, viewTitle);
        titleBlock.setPadding(false);
        titleBlock.setSpacing(false);
        titleBlock.addClassName("app-header-title-block");

        Avatar avatar = new Avatar("User");
        avatar.addClassName("app-user-avatar");

        Span userName = new Span("Demo User");
        userName.addClassName("app-user-name");

        HorizontalLayout userArea = new HorizontalLayout(userName, avatar);
        userArea.setPadding(false);
        userArea.setSpacing(true);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.addClassName("app-topbar-user");

        HorizontalLayout headerBar = new HorizontalLayout(drawerToggle, titleBlock, userArea);
        headerBar.setWidthFull();
        headerBar.setPadding(false);
        headerBar.setSpacing(false);
        headerBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerBar.expand(titleBlock);
        headerBar.addClassName("app-topbar");

        addToNavbar(headerBar);
    }

    private void addDrawerContent() {
        Header drawerHeader = createDrawerHeader();

        Scroller navigationScroller = new Scroller(createNavigationLayout());
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

    private Component createNavigationLayout() {
        SideNav operativNav = new SideNav();
        operativNav.addClassName("app-sidenav");

        SideNav archivNav = new SideNav();
        archivNav.addClassName("app-sidenav");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        for (MenuEntry entry : menuEntries) {
            if ("Stock Changes".equals(entry.title())) {
                archivNav.addItem(createNavItem(entry));
            } else {
                operativNav.addItem(createNavItem(entry));
            }
        }

        Div operativWrapper = new Div(operativNav);
        operativWrapper.addClassName("app-sidenav-wrapper");

        Div archivWrapper = new Div(archivNav);
        archivWrapper.addClassName("app-sidenav-wrapper");
        archivWrapper.addClassName("nav-collapsed");

        VerticalLayout navLayout = new VerticalLayout(
                createNavSectionLabel("Operativ", operativWrapper, true),
                operativWrapper,
                createNavSectionLabel("Archiv", archivWrapper, false),
                archivWrapper
        );
        navLayout.setPadding(false);
        navLayout.setSpacing(false);
        return navLayout;
    }

    private Span createNavSectionLabel(String text, Div wrapper, boolean initiallyOpen) {
        Span chevron = new Span();
        chevron.addClassName("app-nav-section-chevron");
        if (!initiallyOpen) {
            chevron.addClassName("collapsed");
        }

        Span label = new Span(new Span(text), chevron);
        label.addClassName("app-nav-section-label");
        label.getStyle().set("cursor", "pointer");

        label.addClickListener(e -> {
            boolean isCollapsed = wrapper.hasClassName("nav-collapsed");
            if (isCollapsed) {
                wrapper.removeClassName("nav-collapsed");
                chevron.removeClassName("collapsed");
            } else {
                wrapper.addClassName("nav-collapsed");
                chevron.addClassName("collapsed");
            }
        });

        return label;
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

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
        updateArticleBadge();
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}