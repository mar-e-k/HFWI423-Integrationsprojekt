package de.fhdw.vendix.orchestrator.ui.application;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.api.domain.account_role.Role;
import de.fhdw.vendix.orchestrator.core.domain.performance.PerformanceTestService;
import de.fhdw.vendix.orchestrator.core.domain.performance.TestType;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.util.Arrays;

@Route(value = "performance", layout = OrchestratorAppLayout.class)
@RolesAllowed(Role.ROLE_ADMIN)
public class PerformanceTestView extends VerticalLayout {

    private static final String GRAFANA_K6_URL      = "http://localhost:3000/d/vendix-k6-lasttests?orgId=1&refresh=5s&kiosk=tv";
    private static final String GRAFANA_LOGS_URL     = "http://localhost:3000/d/vendix-logs?orgId=1&refresh=10s&kiosk=tv";
    private static final String K6_WEB_DASHBOARD_URL = "http://localhost:5665";

    private final PerformanceTestService testService;

    private final RadioButtonGroup<TestType> testSelector   = new RadioButtonGroup<>();
    private final Button                     startButton    = new Button("Test starten", VaadinIcon.PLAY.create());
    private final Button                     stopButton     = new Button("Test stoppen", VaadinIcon.STOP.create());
    private final TextArea                   logArea        = new TextArea("k6 Output");
    private final Span                       statusBadge    = new Span("● Kein Test aktiv");
    private final ProgressBar                progressBar    = new ProgressBar(0.0, 1.0);
    private final Span                       progressLabel  = new Span("–");
    private final Span                       remainingLabel = new Span("–");
    private final HorizontalLayout           progressRow    = new HorizontalLayout();

    public PerformanceTestView(PerformanceTestService testService) {
        this.testService = testService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Tests",               buildTestsTab());
        tabs.add("k6 Dashboard",        buildGrafanaTab(GRAFANA_K6_URL,       "k6 Lasttest-Dashboard"));
        tabs.add("Logs (Loki)",         buildGrafanaTab(GRAFANA_LOGS_URL,      "Logs & Exceptions"));
        tabs.add("k6 Live (Port 5665)", buildK6LiveTab());
        add(tabs);
    }

    // ─── Tab 1: Teststeuerung ──────────────────────────────────────────────────

    private VerticalLayout buildTestsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        H2 title = new H2("k6 Lasttest-Szenarien");
        Paragraph subtitle = new Paragraph(
                "Wähle eines der 5 Szenarien und starte den Test. " +
                        "k6 läuft im Docker-Container und pusht Metriken live an Prometheus."
        );

        testSelector.setLabel("Testszenario auswählen");
        testSelector.setItems(TestType.values());
        testSelector.setItemLabelGenerator(TestType::getDisplayName);
        testSelector.setValue(TestType.LASTTEST);

        Div descBox = new Div();
        descBox.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");
        Paragraph descText  = new Paragraph();
        Span      paramSpan = new Span();
        paramSpan.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-color)");
        descBox.add(descText, new Hr(), paramSpan);
        updateDesc(TestType.LASTTEST, descText, paramSpan);
        testSelector.addValueChangeListener(e -> updateDesc(e.getValue(), descText, paramSpan));

        progressBar.setWidthFull();
        progressBar.setValue(0.0);
        progressBar.setVisible(false);

        progressLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "bold");
        remainingLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        progressRow.add(progressLabel, remainingLabel);
        progressRow.setAlignItems(Alignment.CENTER);
        progressRow.setSpacing(true);
        progressRow.setVisible(false);

        statusBadge.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        startButton.addClickListener(e -> startTest());

        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.addClickListener(e -> stopTest());

        HorizontalLayout buttonRow = new HorizontalLayout(startButton, stopButton, statusBadge);
        buttonRow.setAlignItems(Alignment.CENTER);

        logArea.setWidthFull();
        logArea.setHeight("380px");
        logArea.setReadOnly(true);
        logArea.getStyle().set("font-family", "monospace").set("font-size", "11px");

        Button clearBtn = new Button("Log leeren", VaadinIcon.TRASH.create());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> logArea.clear());

        layout.add(title, subtitle, testSelector, descBox,
                progressBar, progressRow, buttonRow, clearBtn, logArea);
        return layout;
    }

    private void updateDesc(TestType type, Paragraph desc, Span params) {
        desc.setText(type.getDescription());
        params.setText("⚙ " + type.getParameters());
    }

    // ─── Tab 2 & 3: Grafana IFrames ───────────────────────────────────────────

    private VerticalLayout buildGrafanaTab(String url, String iframeTitle) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        Anchor openExternal = new Anchor(url, "In neuem Tab öffnen ↗");
        openExternal.setTarget("_blank");
        openExternal.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("padding", "4px 12px")
                .set("display", "block");

        IFrame iframe = new IFrame(url);
        iframe.setSizeFull();
        iframe.getStyle().set("border", "none").set("min-height", "calc(100vh - 90px)");
        iframe.setTitle(iframeTitle);

        layout.add(openExternal, iframe);
        layout.expand(iframe);
        return layout;
    }

    // ─── Tab 4: k6 Live Dashboard mit Aktualisier-Button ─────────────────────

    private VerticalLayout buildK6LiveTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        // IFrame-Container — wird per JS neu geladen statt Browser-Refresh
        IFrame iframe = new IFrame(K6_WEB_DASHBOARD_URL);
        iframe.setSizeFull();
        iframe.setId("k6-live-iframe");
        iframe.getStyle().set("border", "none").set("min-height", "calc(100vh - 90px)");
        iframe.setTitle("k6 Web Dashboard");

        // Toolbar mit Aktualisier-Button und externem Link
        Anchor openExternal = new Anchor(K6_WEB_DASHBOARD_URL, "In neuem Tab öffnen ↗");
        openExternal.setTarget("_blank");
        openExternal.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("display", "block");

        Span hint = new Span("Das Dashboard ist nur sichtbar während ein Test läuft.");
        hint.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Aktualisier-Button: lädt nur das IFrame neu, nicht die gesamte Seite
        // → laufender Test wird nicht unterbrochen
        Button refreshBtn = new Button("Dashboard aktualisieren", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e ->
                // Setzt src des IFrames neu → Browser lädt nur das IFrame neu
                refreshBtn.getElement().executeJs(
                        "document.getElementById('k6-live-iframe').src = '" + K6_WEB_DASHBOARD_URL + "';"
                )
        );

        HorizontalLayout toolbar = new HorizontalLayout(refreshBtn, openExternal, hint);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.getStyle().set("padding", "4px 12px");

        layout.add(toolbar, iframe);
        layout.expand(iframe);
        return layout;
    }

    // ─── Test starten/stoppen ──────────────────────────────────────────────────

    private void startTest() {
        TestType selected = testSelector.getValue();
        if (selected == null) {
            showError("Bitte ein Testszenario auswählen.");
            return;
        }

        UI ui = UI.getCurrent();
        logArea.clear();
        appendLog("╔" + "═".repeat(58) + "╗");
        appendLog("║  k6 LASTTEST GESTARTET");
        appendLog("║  Szenario : " + selected.getDisplayName());
        appendLog("║  Parameter: " + selected.getParameters());
        appendLog("║  Dauer    : " + formatDuration(selected.getDurationSeconds()));
        appendLog("╚" + "═".repeat(58) + "╝");
        appendLog("");

        try {
            testService.startTest(selected, line -> ui.access(() -> {
                appendLog(line);
                if (line.startsWith("[FERTIG]")) {
                    appendLog("");
                    appendLog("╔" + "═".repeat(58) + "╗");
                    appendLog("║  TEST BEENDET");
                    appendLog("║  " + line);
                    appendLog("╚" + "═".repeat(58) + "╝");
                    setTestStopped();
                }
            }));
            setTestRunning(selected);

        } catch (IOException | IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showError(msg);
            appendLog("[ERROR] " + msg);
        }
    }

    private void stopTest() {
        testService.stopTest();
        appendLog("");
        appendLog("■ Test manuell gestoppt.");
        setTestStopped();
    }

    // ─── UI-Zustand ────────────────────────────────────────────────────────────

    private void setTestRunning(TestType type) {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        testSelector.setEnabled(false);
        statusBadge.setText("● " + type.getDisplayName() + " läuft...");
        statusBadge.getStyle().set("color", "var(--lumo-success-color)");
        progressBar.setValue(0.0);
        progressBar.setVisible(true);
        progressRow.setVisible(true);
    }

    private void setTestStopped() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        testSelector.setEnabled(true);
        statusBadge.setText("● Kein Test aktiv");
        statusBadge.getStyle().set("color", "var(--lumo-secondary-text-color)");
        progressBar.setValue(0.0);
        progressBar.setVisible(false);
        progressLabel.setText("–");
        remainingLabel.setText("–");
        progressRow.setVisible(false);

        UI ui = UI.getCurrent();
        if (ui != null) ui.setPollInterval(-1);
    }

    // ─── Polling ──────────────────────────────────────────────────────────────

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (testService.isRunning()) {
            testService.getActiveTest().ifPresent(active -> {
                testSelector.setValue(active);
                setTestRunning(active);
                appendLog("[INFO] Test läuft bereits: " + active.getDisplayName());
            });
        }

        UI ui = attachEvent.getUI();
        ui.setPollInterval(1000);
        ui.addPollListener(event -> {
            if (!testService.isRunning()) return;
            double   progress  = testService.getProgress();
            long     elapsed   = testService.getElapsedSeconds();
            TestType test      = testService.getActiveTest().orElse(null);
            if (test == null) return;

            progressBar.setValue(progress);
            long remaining = Math.max(0, test.getDurationSeconds() - elapsed);
            int  pct       = (int) (progress * 100);
            progressLabel.setText(pct + "% (" + formatDuration((int) elapsed) +
                    " / " + formatDuration(test.getDurationSeconds()) + ")");
            remainingLabel.setText("⏳ Noch ca. " + formatDuration((int) remaining));
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detachEvent.getUI().setPollInterval(-1);
    }

    // ─── Hilfsmethoden ────────────────────────────────────────────────────────

    private void appendLog(String line) {
        String current = logArea.getValue();
        String updated = current.isEmpty() ? line : current + "\n" + line;
        String[] lines = updated.split("\n");
        if (lines.length > 400) {
            updated = String.join("\n", Arrays.copyOfRange(lines, lines.length - 400, lines.length));
        }
        logArea.setValue(updated);
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private static String formatDuration(int seconds) {
        if (seconds < 60)   return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + " min " + (seconds % 60) + "s";
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        return h + "h " + m + "min";
    }
}