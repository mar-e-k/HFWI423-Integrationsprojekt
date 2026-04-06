package de.fhdw.vendix.store.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.service.other.RegisterRegistryService;
import de.fhdw.vendix.store.utility.RegisterClient;
import de.fhdw.vendix.store.view.admin.*;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route("")
@RolesAllowed({AccountRoleEnum.ROLE_ADMIN})
public class MainView extends AppLayout implements BeforeEnterObserver {

    private final H2 title = new H2();
    private final RegisterRegistryService registerRegistryService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public MainView(RegisterRegistryService registerRegistryService) {
        super();
        this.registerRegistryService = registerRegistryService;

        createHeader();
        addToDrawer(createSidebar());
        setContent(createContent());
    }

    private void createHeader() {
        H1 viewTitle = new H1("Vendix Store");
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

        Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH), e -> {
            setContent(createContent());
        });
        refreshButton.setTooltipText("Kassenübersicht aktualisieren");
        refreshButton.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, refreshButton, themeToggleButton, logoutButton);
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
                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
                createSidebarLink("Benutzer", VaadinIcon.USER, AdminView.class),
                createSidebarLink("Accounts", VaadinIcon.GROUP, RoleView.class),
                createSidebarLink("Kassen", VaadinIcon.CASH, RegisterAddView.class),
                createSidebarLink("Bestand", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Belege", VaadinIcon.RECORDS, DailyReceiptReportingView.class),
                createSidebarLink("Lasttests", VaadinIcon.CHART_LINE, PerformanceTestView.class)
        );
        return sidebar;
    }

    private Component createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        title.setText("Verbundene Kassensysteme");
        title.getStyle().set("color", "var(--vaadin-primary-color, var(--lumo-primary-color))");
        title.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");

        Grid<RegisterClient> grid = createGrid();
        List<RegisterClient> instances = registerRegistryService.findAllRegistries();
        grid.setItems(instances);

        content.add(title, grid);
        return content;
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

    private Grid<RegisterClient> createGrid() {
        Grid<RegisterClient> grid = new Grid<>(RegisterClient.class, false);

        grid.setWidthFull();
        grid.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        grid.getStyle().set("box-shadow", "0 2px 12px rgba(0, 0, 0, 0.08)");

        grid.addColumn(ks -> String.format("🖥️ Kasse %d (%s)", ks.getRegister().getId(), ks.getSystemClientDTO().getId()))
                .setHeader("Gerät")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getSystemClientDTO().getHost())
                .setHeader("Host")
                .setAutoWidth(true);

        grid.addColumn(ks -> ks.getSystemClientDTO().getPort())
                .setHeader("Port")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createOnlineBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(ks -> formatter.format(ks.getRegisteredAt().atZone(ZoneId.systemDefault())))
                .setHeader("Registriert am")
                .setAutoWidth(true);

        grid.addColumn(ks -> formatter.format(ks.getLastSeen().atZone(ZoneId.systemDefault())))
                .setHeader("Zuletzt gesehen")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createInstanceLink)
                .setHeader("Aktion")
                .setAutoWidth(true);

        return grid;
    }

    private Component createOnlineBadge(RegisterClient registerClient) {
        Span badge = new Span(registerClient.isOnline() ? "Online" : "Offline");
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList()
                .add(registerClient.isOnline() ? "success" : "error");
        return badge;
    }

    private Component createInstanceLink(RegisterClient registerClient) {
        List<RegisterClient> instances = registerRegistryService.findAllRegistries();
        String name = String.format("Kasse %d", instances.indexOf(registerClient) + 1);
        String url = "http://" + registerClient.getSystemClientDTO().getHost() + ":" + registerClient.getSystemClientDTO().getPort() + "/cashier";
        try {
            url += "?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Anchor anchor = new Anchor(url, "");
        anchor.setTarget("_blank");

        Button button = new Button("Öffnen", new Icon(VaadinIcon.EXTERNAL_LINK));
        button.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        button.getStyle().set("cursor", "pointer");

        anchor.add(button);
        return anchor;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Class<?> targetView = beforeEnterEvent.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean authorized = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authRole -> {
                    for (String requiredRole : rolesAllowed.value()) {
                        if (authRole.equals(requiredRole)) {
                            return true;
                        }
                    }
                    return false;
                });

        if (!authorized) {
            beforeEnterEvent.rerouteTo("login");
        }
    }
}
