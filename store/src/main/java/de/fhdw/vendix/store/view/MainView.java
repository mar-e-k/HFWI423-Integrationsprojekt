package de.fhdw.vendix.store.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
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
import com.vaadin.flow.theme.aura.Aura;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.commons.ui.utlity.DateTimeFormat;
import de.fhdw.vendix.store.persistence.service.other.RegisterRegistryService;
import de.fhdw.vendix.store.utility.RegisterClient;
import de.fhdw.vendix.commons.ui.view.AbstractAdminView;
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
@StyleSheet(Aura.STYLESHEET)
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
        H1 viewTitle = new H1("StoreView");
        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
        liveClockLabel.getStyle().set("font-weight", "bold");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            UI ui = UI.getCurrent(); // UI-Referenz sichern BEVOR async-Aufruf
            ui.getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = ui.getElement().getThemeList();
                        boolean isClientDark = "dark".equals(currentClientTheme);

                        if (isClientDark) {
                            themeList.remove(Lumo.DARK);
                            ui.getPage().executeJs("localStorage.setItem('theme', 'light'); document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            ui.getPage().executeJs("localStorage.setItem('theme', 'dark'); document.documentElement.setAttribute('theme', 'dark');");
                        }
                    });
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        Button logoutButton = new Button("Logout", e -> UI.getCurrent().getPage().setLocation("/logout"));

        Button refreshButton = new Button(new Icon(VaadinIcon.REFRESH), e -> {
            setContent(createContent()); // Grid-Daten frisch laden
        });
        refreshButton.setTooltipText("Kassenübersicht aktualisieren");

        HorizontalLayout rightSection = new HorizontalLayout(liveClockLabel, refreshButton, themeToggleButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

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
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);

        sidebar.add(
                createSidebarLink("Home", VaadinIcon.HOME, MainView.class),
                createSidebarLink("Admin", VaadinIcon.USER, AdminView.class),
                createSidebarLink("Accounts", VaadinIcon.GROUP, RoleView.class),
                createSidebarLink("Kassen", VaadinIcon.CASH, RegisterAddView.class),
                createSidebarLink("Bestand", VaadinIcon.PACKAGE, StockView.class),
                createSidebarLink("Belege", VaadinIcon.RECORDS, DailyReceiptReportingView.class)
        );
        return sidebar;
    }

    private Component createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        title.setText("Connected Kassensystem Instances");

        Grid<RegisterClient> grid = createGrid();
        List<RegisterClient> instances = registerRegistryService.findAllRegistries();
        grid.setItems(instances);

        content.add(title, grid);
        return content;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon, Class<? extends Component> navigationTarget) {
        com.vaadin.flow.component.icon.Icon i = new com.vaadin.flow.component.icon.Icon(icon);
        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");

        RouterLink link = new RouterLink(navigationTarget);
        link.add(i, textSpan);
        link.getStyle().set("display", "flex");
        link.getStyle().set("align-items", "center");
        link.getStyle().set("padding", "var(--lumo-space-s)");
        link.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        link.getStyle().set("transition", "background-color 0.2s");
        link.getStyle().set("text-decoration", "none");
        link.getStyle().set("color", "var(--lumo-body-text-color)");

        return link;
    }

    private Grid<RegisterClient> createGrid() {
        Grid<RegisterClient> grid = new Grid<>(RegisterClient.class, false);

        grid.setWidthFull();

        grid.addColumn(ks -> String.format("Kasse %d (Dev, %s)", ks.getRegister().getId(), ks.getSystemClientDTO().getId()))
                .setHeader("Device")
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
                .setHeader("Registered At")
                .setAutoWidth(true);

        grid.addColumn(ks -> formatter.format(ks.getLastSeen().atZone(ZoneId.systemDefault())))
                .setHeader("Last Seen")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createInstanceLink)
                .setHeader("Open")
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
        Anchor link = new Anchor(url, "Open");
        link.setTarget("_blank");
        return link;
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