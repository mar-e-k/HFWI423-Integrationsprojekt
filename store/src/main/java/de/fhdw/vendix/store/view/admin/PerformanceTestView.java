package de.fhdw.vendix.store.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.aura.Aura;
import com.vaadin.flow.theme.lumo.Lumo;
import de.fhdw.vendix.commons.core.persistence.entity.AccountRoleEnum;
import de.fhdw.vendix.store.persistence.service.performance.PerformanceTestProperties;
import de.fhdw.vendix.store.persistence.service.performance.PerformanceTestService;
import de.fhdw.vendix.store.persistence.service.performance.PerformanceTestType;
import de.fhdw.vendix.store.view.MainView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Route("/admin/performance")
@PageTitle("Lasttests")
@RolesAllowed(AccountRoleEnum.ROLE_ADMIN)
@StyleSheet(Aura.STYLESHEET)
public class PerformanceTestView extends AppLayout implements BeforeEnterObserver {

    private final PerformanceTestService    testService;
    private final PerformanceTestProperties props;

    private Span   statusLabel;
    private Span   statusBadge;

    public PerformanceTestView(PerformanceTestService testService,
                               PerformanceTestProperties props) {
        this.testService = testService;
        this.props       = props;

        createHeader();
        addToDrawer(createSidebar());
    }

    // -------------------------------------------------------------------------
    // Header (identisches Pattern wie AdminView)
    // -------------------------------------------------------------------------

    private void createHeader() {
        H1 viewTitle = new H1("Lasttests");
        HorizontalLayout leftSection = new HorizontalLayout(new DrawerToggle(), viewTitle);
        leftSection.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Span liveClockLabel = new Span();
        liveClockLabel.setId("live-clock-label-perf");
        liveClockLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
        liveClockLabel.getStyle().set("font-weight", "bold");

        Button themeToggleButton = new Button(new Icon(VaadinIcon.ADJUST), click -> {
            UI ui = UI.getCurrent();
            ui.getPage().executeJs("return document.documentElement.getAttribute('theme');")
                    .then(String.class, currentClientTheme -> {
                        var themeList = ui.getElement().getThemeList();
                        if ("dark".equals(currentClientTheme)) {
                            themeList.remove(Lumo.DARK);
                            ui.getPage().executeJs(
                                    "localStorage.setItem('theme','light');" +
                                            "document.documentElement.removeAttribute('theme');");
                        } else {
                            themeList.add(Lumo.DARK);
                            ui.getPage().executeJs(
                                    "localStorage.setItem('theme','dark');" +
                                            "document.documentElement.setAttribute('theme','dark');");
                        }
                    });
        });
        themeToggleButton.setTooltipText("Toggle dark mode");

        Button logoutButton = new Button("Logout",
                e -> UI.getCurrent().getPage().setLocation("/logout"));

        HorizontalLayout rightSection = new HorizontalLayout(
                liveClockLabel, themeToggleButton, logoutButton);
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        rightSection.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(leftSection, rightSection);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        addToNavbar(header);

        UI.getCurrent().getPage().executeJs("""
            const label = document.getElementById('live-clock-label-perf');
            if (label) {
                setInterval(() => {
                    const now = new Date();
                    label.textContent = now.toLocaleString('de-DE', {
                        year:'numeric', month:'2-digit', day:'2-digit',
                        hour:'2-digit', minute:'2-digit', second:'2-digit'
                    });
                }, 1000);
            }
        """);
    }

    // -------------------------------------------------------------------------
    // Sidebar (gleiche Links wie alle anderen Views + neuer "Lasttests"-Eintrag)
    // -------------------------------------------------------------------------

    private Component createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(false);
        sidebar.setSpacing(false);
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);

        sidebar.add(
                createSidebarLink("Home",       VaadinIcon.HOME,      MainView.class),
                createSidebarLink("Admin",      VaadinIcon.USER,      AdminView.class),
                createSidebarLink("Accounts",   VaadinIcon.GROUP,     RoleView.class),
                createSidebarLink("Kassen",     VaadinIcon.CASH,      RegisterAddView.class),
                createSidebarLink("Bestand",    VaadinIcon.PACKAGE,   StockView.class),
                createSidebarLink("Belege",     VaadinIcon.RECORDS,   DailyReceiptReportingView.class),
                createSidebarLink("Lasttests",  VaadinIcon.CHART_LINE, PerformanceTestView.class)
        );
        return sidebar;
    }

    private RouterLink createSidebarLink(String text, VaadinIcon icon,
                                         Class<? extends Component> target) {
        Icon i = new Icon(icon);
        Span textSpan = new Span(text);
        textSpan.getStyle().set("margin-left", "var(--lumo-space-m)");

        RouterLink link = new RouterLink(target);
        link.add(i, textSpan);
        link.getStyle()
                .set("display",          "flex")
                .set("align-items",      "center")
                .set("padding",          "var(--lumo-space-s)")
                .set("border-radius",    "var(--lumo-border-radius-m)")
                .set("transition",       "background-color 0.2s")
                .set("text-decoration",  "none")
                .set("color",            "var(--lumo-body-text-color)");
        return link;
    }

    // -------------------------------------------------------------------------
    // Content
    // -------------------------------------------------------------------------

    @PostConstruct
    public void initUI() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        content.add(
                buildStatusSection(),
                buildTestButtonSection(),
                buildLinksSection(),
                buildDashboardSection()
        );

        setContent(content);
    }

    // --- Status-Bereich ---

    private Component buildStatusSection() {
        H2 heading = new H2("Status");

        statusLabel = new Span(testService.getStatusSummary());
        statusLabel.getStyle()
                .set("font-size",  "var(--lumo-font-size-m)")
                .set("display",    "block")
                .set("margin-top", "var(--lumo-space-s)");

        statusBadge = buildStatusBadge();

        Button refreshButton = new Button("Aktualisieren", new Icon(VaadinIcon.REFRESH), e -> {
            statusLabel.setText(testService.getStatusSummary());
            refreshStatusBadge(statusBadge);
        });
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout statusRow = new HorizontalLayout(statusBadge, statusLabel, refreshButton);
        statusRow.setAlignItems(FlexComponent.Alignment.CENTER);
        statusRow.setSpacing(true);

        VerticalLayout section = new VerticalLayout(heading, statusRow);
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border",        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding",       "var(--lumo-space-m)");
        return section;
    }

    private Span buildStatusBadge() {
        Span badge = new Span(testService.isRunning() ? "Läuft" : "Bereit");
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(testService.isRunning() ? "contrast" : "success");
        return badge;
    }

    private void refreshStatusBadge(Span badge) {
        badge.setText(testService.isRunning() ? "Läuft" : "Bereit");
        badge.getElement().getThemeList().clear();
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(testService.isRunning() ? "contrast" : "success");
    }

    // --- Test-Buttons ---

    private Component buildTestButtonSection() {
        H2 heading = new H2("Tests starten");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.getStyle().set("flex-wrap", "wrap");

        for (PerformanceTestType type : PerformanceTestType.values()) {
            Button btn = new Button(type.getLabel(), new Icon(VaadinIcon.PLAY));
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn.setTooltipText("Startet: " + type.getLabel());

            btn.addClickListener(e -> {
                try {
                    testService.startTest(type);
                    statusLabel.setText(testService.getStatusSummary());
                    refreshStatusBadge(statusBadge);
                    showNotification("Test gestartet: " + type.getLabel(),
                            NotificationVariant.LUMO_SUCCESS);
                } catch (IllegalStateException ex) {
                    showNotification(ex.getMessage(), NotificationVariant.LUMO_ERROR);
                } catch (Exception ex) {
                    showNotification("Fehler: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
                }
            });

            buttons.add(btn);
        }

        // Stop-Button
        Button stopBtn = new Button("Test abbrechen", new Icon(VaadinIcon.STOP));
        stopBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopBtn.setTooltipText("Bricht den laufenden Test ab");
        stopBtn.addClickListener(e -> {
            testService.stopTest();
            statusLabel.setText(testService.getStatusSummary());
            refreshStatusBadge(statusBadge);
            showNotification("Test abgebrochen.", NotificationVariant.LUMO_CONTRAST);
        });
        buttons.add(stopBtn);

        VerticalLayout section = new VerticalLayout(heading, buttons);
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border",        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding",       "var(--lumo-space-m)");
        return section;
    }

    // --- Monitoring-Links ---

    private Component buildLinksSection() {
        H2 heading = new H2("Monitoring");

        // Grafana-Link
        Anchor grafanaLink = new Anchor(props.getGrafanaUrl(), "Grafana öffnen →");
        grafanaLink.setTarget("_blank");
        grafanaLink.getStyle().set("margin-right", "var(--lumo-space-l)");

        // Prometheus Targets öffnen
        Anchor prometheusTargetsLink = new Anchor(
                props.getPrometheusUrl() + "/targets", "Prometheus Targets →");
        prometheusTargetsLink.setTarget("_blank");
        prometheusTargetsLink.getStyle().set("margin-right", "var(--lumo-space-l)");

        // Ergebnis-Ordner im Finder/Explorer oeffnen
        Button openFolderBtn = new Button("Ergebnis-Dateien öffnen", new Icon(VaadinIcon.FOLDER_OPEN));
        openFolderBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openFolderBtn.setTooltipText(props.getResultsDir());
        openFolderBtn.addClickListener(e -> {
            try {
                String os  = System.getProperty("os.name").toLowerCase();
                String cmd = os.contains("mac") ? "open" : os.contains("win") ? "explorer" : "xdg-open";
                new ProcessBuilder(cmd, props.getResultsDir())
                        .redirectErrorStream(true)
                        .start();
                showNotification("Ordner geöffnet: " + props.getResultsDir(),
                        NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                showNotification("Fehler: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        // Letzten HTML-Report öffnen (öffnet das Verzeichnis via OS-Befehl server-seitig,
        // da Server und Browser auf derselben Maschine laufen)
        Button openReportBtn = new Button("Letzten Report öffnen", new Icon(VaadinIcon.CHART));
        openReportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        openReportBtn.setTooltipText("Öffnet den zuletzt generierten HTML-Report im Browser");
        openReportBtn.addClickListener(e -> openLatestReport());

        HorizontalLayout links = new HorizontalLayout(
                grafanaLink, prometheusTargetsLink, openFolderBtn, openReportBtn);
        links.setAlignItems(FlexComponent.Alignment.CENTER);
        links.getStyle().set("flex-wrap", "wrap");

        VerticalLayout section = new VerticalLayout(heading, links);
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border",        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding",       "var(--lumo-space-m)");
        return section;
    }

    /** Sucht das neueste report_*-Verzeichnis und öffnet dessen index.html im Browser. */
    private void openLatestReport() {
        try {
            java.io.File resultsDir = new java.io.File(props.getResultsDir());
            if (!resultsDir.exists()) {
                showNotification("Noch keine Reports vorhanden.", NotificationVariant.LUMO_CONTRAST);
                return;
            }
            java.io.File[] reports = resultsDir.listFiles(
                    f -> f.isDirectory() && f.getName().startsWith("report_"));
            if (reports == null || reports.length == 0) {
                showNotification("Noch keine Reports vorhanden.", NotificationVariant.LUMO_CONTRAST);
                return;
            }
            // Neuestes Report-Verzeichnis nach Änderungsdatum sortieren
            java.util.Arrays.sort(reports, java.util.Comparator.comparingLong(java.io.File::lastModified));
            java.io.File latest = reports[reports.length - 1];
            java.io.File index  = new java.io.File(latest, "index.html");
            if (!index.exists()) {
                showNotification("index.html nicht gefunden in: " + latest.getName(),
                        NotificationVariant.LUMO_ERROR);
                return;
            }
            // OS-Befehl: open (Mac) / xdg-open (Linux) / start (Windows)
            String os = System.getProperty("os.name").toLowerCase();
            String cmd = os.contains("mac") ? "open" : os.contains("win") ? "explorer" : "xdg-open";
            new ProcessBuilder(cmd, index.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            showNotification("Report geöffnet: " + latest.getName(), NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            showNotification("Fehler: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    // --- Grafana IFrame ---

    private Component buildDashboardSection() {
        H2 heading = new H2("Grafana Dashboard");

        IFrame frame = new IFrame(props.getDashboardEmbedUrl());
        frame.setWidthFull();
        frame.setHeight("700px");
        frame.getStyle()
                .set("border",        "none")
                .set("border-radius", "var(--lumo-border-radius-l)");

        VerticalLayout section = new VerticalLayout(heading, frame);
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("border",        "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding",       "var(--lumo-space-m)");
        return section;
    }

    // --- Hilfsmethode ---

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 4000,
                Notification.Position.BOTTOM_END);
        notification.addThemeVariants(variant);
    }

    // -------------------------------------------------------------------------
    // Security-Check (identisch mit AdminView)
    // -------------------------------------------------------------------------

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<?> targetView = event.getNavigationTarget();

        RolesAllowed rolesAllowed = targetView.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) return;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            event.rerouteTo("login");
            return;
        }

        Set<String> userAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean authorized = Arrays.stream(rolesAllowed.value())
                .anyMatch(userAuthorities::contains);

        if (!authorized) {
            event.rerouteTo("login");
        }
    }
}