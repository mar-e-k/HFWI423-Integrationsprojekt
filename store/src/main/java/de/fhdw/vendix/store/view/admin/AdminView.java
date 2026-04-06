package de.fhdw.vendix.store.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.entity.Account;
import de.fhdw.vendix.store.persistence.service.AccountService;
import de.fhdw.vendix.store.view.MainView;
import de.fhdw.vendix.commons.ui.utlity.DateTimeFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Route("/admin")
@PageTitle("Admin View")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
public class AdminView extends AppLayout implements BeforeEnterObserver {

    private final AccountService accountService;
    private Grid<Account> grid;

    public AdminView(AccountService accountService) {
        this.accountService = accountService;
        createHeader();
        addToDrawer(createSidebar());
    }

    private void createHeader() {
        H1 viewTitle = new H1(getPageTitle());
        viewTitle.getStyle().set("margin", "0");
        viewTitle.getStyle().set("color", "var(--vaadin-primary-color, var(--lumo-primary-color))");
        viewTitle.getStyle().set("font-size", "1.5rem");

        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);

        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-m)");
        liveClockLabel.getStyle().set("font-weight", "500");
        liveClockLabel.getStyle().set("color", "var(--vaadin-secondary-text-color, var(--lumo-secondary-text-color))");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.MOON), click -> {
            UI ui = UI.getCurrent();
            ui.getPage().executeJs("""
                const html = document.documentElement;
                const current = getComputedStyle(html).colorScheme;
                const isDark = current.includes('dark');
                html.style.colorScheme = isDark ? 'light' : 'dark';
                return !isDark;
            """).then(Boolean.class, isDarkNow -> {
                ((Button) click.getSource()).setIcon(new Icon(isDarkNow ? VaadinIcon.SUN_O : VaadinIcon.MOON));
            });
        });
        themeToggleButton.setTooltipText("Dark Mode umschalten");
        themeToggleButton.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        Button logoutButton = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT), e -> UI.getCurrent().getPage().setLocation("/logout"));
        logoutButton.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);
        rightSection.setPadding(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("background", "var(--vaadin-header-background, var(--lumo-base-color))");
        header.getStyle().set("border-bottom", "1px solid var(--vaadin-border-color, var(--lumo-divider-color))");
        header.getStyle().set("padding", "0 var(--lumo-space-m)");

        addToNavbar(header);

        UI.getCurrent().getPage().executeJs("""
            const label = document.getElementById('live-clock-label');
            if (label) {
                setInterval(() => {
                    const now = new Date();
                    label.textContent = now.toLocaleString('de-DE', {
                        year: 'numeric', month: '2-digit', day: '2-digit',
                        hour: '2-digit', minute: '2-digit', second: '2-digit'
                    });
                }, 1000);
            }
        """);
    }

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);
        sidebar.getStyle().set("background", "var(--vaadin-header-background, var(--lumo-base-color))");

        sidebar.add(
                createSidebarLink("Home",    VaadinIcon.HOME,    MainView.class),
                createSidebarLink("Benutzer",   VaadinIcon.USER,    AdminView.class),
                createSidebarLink("Accounts", VaadinIcon.GROUP,  RoleView.class),
                createSidebarLink("Kassen",  VaadinIcon.CASH,    RegisterAddView.class),
                createSidebarLink("Bestand", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Belege",  VaadinIcon.RECORDS, DailyReceiptReportingView.class),
                createSidebarLink("Lasttests", VaadinIcon.CHART_LINE, PerformanceTestView.class)
        );
        return sidebar;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
        Icon i = new Icon(icon);
        i.getStyle().set("width", "24px");
        i.getStyle().set("height", "24px");

        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");
        textSpan.getStyle().set("font-weight", "500");
        textSpan.getStyle().set("font-size", "var(--lumo-font-size-m)");

        RouterLink link = new RouterLink(navigationTarget);
        link.add(i, textSpan);
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-m)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "all 0.3s ease");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--vaadin-text-color, var(--lumo-body-text-color))");
        link.getStyle().set("cursor", "pointer");

        // Hover-Effekt
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
            link.getStyle().set("box-shadow", "0 2px 6px rgba(0, 0, 0, 0.1)");
        });
        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background-color", "transparent");
            link.getStyle().set("box-shadow", "none");
        });

        return link;
    }

    @PostConstruct
    public void initUI() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        H1 title = new H1("Aktive Benutzer");
        title.getStyle().set("color", "var(--vaadin-primary-color, var(--lumo-primary-color))");
        title.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");

        grid = new Grid<>(Account.class, false);
        grid.setSizeFull();
        grid.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        grid.getStyle().set("box-shadow", "0 2px 12px rgba(0, 0, 0, 0.08)");

        grid.addColumn(account -> account.getCreatedAt() != null
                        ? DateTimeFormat.UI_DATE_TIME.format(account.getCreatedAt().atZone(ZoneId.systemDefault()))
                        : "-")
                .setHeader("Erstellt am")
                .setSortable(true);
        grid.addColumn(account -> account.getCreatedBy() != null ? account.getCreatedBy() : "-")
                .setHeader("Erstellt von")
                .setSortable(true);
        grid.addColumn(account -> account.getChangedAt() != null
                        ? DateTimeFormat.UI_DATE_TIME.format(account.getChangedAt().atZone(ZoneId.systemDefault()))
                        : "-")
                .setHeader("Geändert am")
                .setSortable(true);
        grid.addColumn(account -> account.getChangedBy() != null ? account.getChangedBy() : "-")
                .setHeader("Geändert von")
                .setSortable(true);
        grid.addColumn(Account::getUuid)
                .setHeader("Account ID")
                .setSortable(true);
        grid.addColumn(Account::getUsername)
                .setHeader("Username")
                .setSortable(true);
        grid.addColumn(account -> account.getAccountRole().getRole().name())
                .setHeader("Rolle")
                .setSortable(true);

        updateGrid();
        content.add(title, grid);
        content.setFlexGrow(1, grid);
        setContent(content);
    }

    private void updateGrid() {
        grid.setItems(accountService.findAll());
    }

    private String getPageTitle() {
        PageTitle titleAnnotation = this.getClass().getAnnotation(PageTitle.class);
        return titleAnnotation != null ? titleAnnotation.value() : "Admin View";
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Class<?> targetView = beforeEnterEvent.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            beforeEnterEvent.rerouteTo("login");
            return;
        }

        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean authorized = Arrays.stream(rolesAllowed.value())
                .anyMatch(userAuthorities::contains);

        if (!authorized) {
            beforeEnterEvent.rerouteTo("login");
        }
    }
}
