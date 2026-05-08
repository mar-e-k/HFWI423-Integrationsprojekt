package de.fhdw.vendix.orchestrator.ui.application;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.fhdw.vendix.commons.spring.security.keycloak.KeycloakRole;
import de.fhdw.vendix.orchestrator.core.other.performance.PerformanceTestService;
import de.fhdw.vendix.orchestrator.core.other.performance.TestType;
import de.fhdw.vendix.orchestrator.ui.OrchestratorAppLayout;
import jakarta.annotation.security.RolesAllowed;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

@Route(value = "performance", layout = OrchestratorAppLayout.class)
@RolesAllowed(KeycloakRole.Constants.ADMIN)
public class PerformanceTestView extends VerticalLayout {

    private static final String GRAFANA_K6_URL      = "http://localhost:3000/d/vendix-k6-lasttests?orgId=1&refresh=5s&kiosk=tv";
    private static final String GRAFANA_LOGS_URL     = "http://localhost:3000/d/vendix-logs?orgId=1&refresh=10s&kiosk=tv";
    private static final String K6_WEB_DASHBOARD_URL = "http://localhost:5665";

    // Standard Store-URL, die k6 im Docker-Netzwerk nutzt
    private static final String DEFAULT_STORE_URL = "http://host.docker.internal:8081";

    private final PerformanceTestService testService;

    // ── Klassische Lasttest-Tab-Felder ───────────────────────────────────────
    private final RadioButtonGroup<TestType> testSelector   = new RadioButtonGroup<>();
    private final Button                     startButton    = new Button("Test starten", VaadinIcon.PLAY.create());
    private final Button                     stopButton     = new Button("Test stoppen", VaadinIcon.STOP.create());
    private final TextArea                   logArea        = new TextArea("k6 Output");
    private final Span                       statusBadge    = new Span("● Kein Test aktiv");
    private final ProgressBar                progressBar    = new ProgressBar(0.0, 1.0);
    private final Span                       progressLabel  = new Span("–");
    private final Span                       remainingLabel = new Span("–");
    private final HorizontalLayout           progressRow    = new HorizontalLayout();

    // ── Messaging-E2E-Tab-Felder ─────────────────────────────────────────────
    private final TextField    msgStoreUrlField    = new TextField("Store-URL");
    private final IntegerField msgStoreIdField     = new IntegerField("Store-ID");
    private final IntegerField msgOrderRateField   = new IntegerField("Orders / Minute");
    private final NumberField  msgUrgentRatioField = new NumberField("Urgent-Anteil (0.0 – 1.0)");
    private final IntegerField msgVerifyWaitField  = new IntegerField("Wartezeit vor Verifikation (ms)");
    private final Button       msgStartButton      = new Button("E2E-Test starten", VaadinIcon.PLAY.create());
    private final Button       msgStopButton       = new Button("Stoppen", VaadinIcon.STOP.create());
    private final TextArea     msgLogArea          = new TextArea("k6 Output");
    private final Span         msgStatusBadge      = new Span("● Kein Test aktiv");
    private final ProgressBar  msgProgressBar      = new ProgressBar(0.0, 1.0);
    private final Span         msgProgressLabel    = new Span("–");
    private final Span         msgRemainingLabel   = new Span("–");

    public PerformanceTestView(PerformanceTestService testService) {
        this.testService = testService;
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Tests",               buildTestsTab());
        tabs.add("Messaging E2E",       buildMessagingE2ETab());
        tabs.add("k6 Dashboard",        buildGrafanaTab(GRAFANA_K6_URL,  "k6 Lasttest-Dashboard"));
        tabs.add("Logs (Loki)",         buildGrafanaTab(GRAFANA_LOGS_URL, "Logs & Exceptions"));
        tabs.add("k6 Live (Port 5665)", buildK6LiveTab());
        add(tabs);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tab 1: klassische k6-Lasttests
    // ══════════════════════════════════════════════════════════════════════════

    private VerticalLayout buildTestsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        H2 title    = new H2("k6 Lasttest-Szenarien");
        Paragraph subtitle = new Paragraph(
                "Wähle eines der Szenarien und starte den Test. " +
                "k6 läuft im Docker-Container und pusht Metriken live an Prometheus."
        );

        // Alle TestTypes AUSSER Messaging E2E hier anzeigen
        testSelector.setLabel("Testszenario auswählen");
        testSelector.setItems(
                Arrays.stream(TestType.values())
                        .filter(t -> t != TestType.MESSAGING_E2E)
                        .collect(Collectors.toList())
        );
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
        stopButton.addClickListener(e -> stopTest(logArea));

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

    // ══════════════════════════════════════════════════════════════════════════
    // Tab 2: Messaging E2E
    // ══════════════════════════════════════════════════════════════════════════

    private VerticalLayout buildMessagingE2ETab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        H2 title = new H2("Messaging E2E – AMQP-Kreislauf-Test");
        Paragraph beschreibung = new Paragraph(
                "k6 triggert über POST /api/test/order[/urgent] wiederholt Bestellanforderungen im Store. " +
                "Der Store publiziert ArticleOrderEvents an RabbitMQ. " +
                "Die Logistikseite (JMeter) konsumiert die Events und schickt ArticleSentEvents zurück. " +
                "Der Store verarbeitet diese Events und bucht den Bestand hoch. " +
                "k6 prüft anschließend per GET /api/test/store-stock, ob der Bestand tatsächlich gestiegen ist."
        );

        Div hinweis = new Div();
        hinweis.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");
        Span hinweisText = new Span(
                "⚠  Voraussetzung: JMeter läuft auf der Logistikseite und ist mit RabbitMQ verbunden. " +
                "Andernfalls werden die Orders zwar gesendet, aber kein ArticleSentEvent kommt zurück " +
                "und der Bestand steigt nicht — die Verifikation schlägt dann fehl."
        );
        hinweisText.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
        hinweis.add(hinweisText);

        // ── Konfigurationsfelder ─────────────────────────────────────────────

        H3 configTitle = new H3("Konfiguration");

        msgStoreUrlField.setValue(DEFAULT_STORE_URL);
        msgStoreUrlField.setWidth("340px");
        msgStoreUrlField.setHelperText("URL des Stores, die k6 im Docker-Netzwerk erreicht");

        msgStoreIdField.setValue(1);
        msgStoreIdField.setMin(1);
        msgStoreIdField.setWidth("140px");
        msgStoreIdField.setHelperText("Store-ID (muss mit dem laufenden Store übereinstimmen)");

        msgOrderRateField.setValue(30);
        msgOrderRateField.setMin(1);
        msgOrderRateField.setMax(600);
        msgOrderRateField.setWidth("140px");
        msgOrderRateField.setHelperText("Orders pro Minute gesamt");

        msgUrgentRatioField.setValue(0.3);
        msgUrgentRatioField.setMin(0.0);
        msgUrgentRatioField.setMax(1.0);
        msgUrgentRatioField.setStep(0.1);
        msgUrgentRatioField.setWidth("200px");
        msgUrgentRatioField.setHelperText("Anteil an Urgent-Orders (0.0 = nie, 1.0 = immer)");

        msgVerifyWaitField.setValue(2000);
        msgVerifyWaitField.setMin(500);
        msgVerifyWaitField.setMax(30000);
        msgVerifyWaitField.setWidth("200px");
        msgVerifyWaitField.setHelperText("Wartezeit (ms) bis zur Bestandsverifikation — sollte ≥ JMeter-Verarbeitungszeit sein");

        HorizontalLayout row1 = new HorizontalLayout(msgStoreUrlField, msgStoreIdField);
        HorizontalLayout row2 = new HorizontalLayout(msgOrderRateField, msgUrgentRatioField, msgVerifyWaitField);
        row1.setAlignItems(Alignment.BASELINE);
        row2.setAlignItems(Alignment.BASELINE);

        // Zusammenfassung der resultierenden k6-Parameter
        Div paramPreview = new Div();
        paramPreview.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("font-family", "monospace")
                .set("font-size", "var(--lumo-font-size-s)");
        updateParamPreview(paramPreview);

        // Aktualisierung der Vorschau bei jeder Feldänderung
        msgStoreUrlField.addValueChangeListener(e -> updateParamPreview(paramPreview));
        msgStoreIdField.addValueChangeListener(e -> updateParamPreview(paramPreview));
        msgOrderRateField.addValueChangeListener(e -> updateParamPreview(paramPreview));
        msgUrgentRatioField.addValueChangeListener(e -> updateParamPreview(paramPreview));
        msgVerifyWaitField.addValueChangeListener(e -> updateParamPreview(paramPreview));

        // ── Fortschritt ──────────────────────────────────────────────────────

        msgProgressBar.setWidthFull();
        msgProgressBar.setValue(0.0);
        msgProgressBar.setVisible(false);

        msgProgressLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "bold");
        msgRemainingLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout msgProgressRow = new HorizontalLayout(msgProgressLabel, msgRemainingLabel);
        msgProgressRow.setAlignItems(Alignment.CENTER);
        msgProgressRow.setSpacing(true);
        msgProgressRow.setVisible(false);

        msgStatusBadge.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // ── Steuerknöpfe ─────────────────────────────────────────────────────

        msgStartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        msgStartButton.addClickListener(e -> startMessagingE2ETest(msgProgressRow));

        msgStopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        msgStopButton.setEnabled(false);
        msgStopButton.addClickListener(e -> stopTest(msgLogArea));

        HorizontalLayout buttonRow = new HorizontalLayout(msgStartButton, msgStopButton, msgStatusBadge);
        buttonRow.setAlignItems(Alignment.CENTER);

        msgLogArea.setWidthFull();
        msgLogArea.setHeight("350px");
        msgLogArea.setReadOnly(true);
        msgLogArea.getStyle().set("font-family", "monospace").set("font-size", "11px");

        Button clearBtn = new Button("Log leeren", VaadinIcon.TRASH.create());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> msgLogArea.clear());

        layout.add(
                title, beschreibung, hinweis,
                configTitle, row1, row2,
                new Hr(), paramPreview,
                new Hr(),
                msgProgressBar, msgProgressRow,
                buttonRow,
                clearBtn, msgLogArea
        );
        return layout;
    }

    private void updateParamPreview(Div target) {
        target.removeAll();
        String preview = String.format(
                "STORE_URL=%s   STORE_ID=%d   ORDER_RATE=%d/min   URGENT_RATIO=%.1f   VERIFY_WAIT_MS=%d ms   VERIFY_STOCK=true",
                msgStoreUrlField.getValue(),
                msgStoreIdField.getValue() != null ? msgStoreIdField.getValue() : 1,
                msgOrderRateField.getValue() != null ? msgOrderRateField.getValue() : 30,
                msgUrgentRatioField.getValue() != null ? msgUrgentRatioField.getValue() : 0.3,
                msgVerifyWaitField.getValue() != null ? msgVerifyWaitField.getValue() : 2000
        );
        target.add(new Span("k6-Env: " + preview));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tab 3 & 4: Grafana IFrames
    // ══════════════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════════════
    // Tab 5: k6 Live Dashboard
    // ══════════════════════════════════════════════════════════════════════════

    private VerticalLayout buildK6LiveTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        IFrame iframe = new IFrame(K6_WEB_DASHBOARD_URL);
        iframe.setSizeFull();
        iframe.setId("k6-live-iframe");
        iframe.getStyle().set("border", "none").set("min-height", "calc(100vh - 90px)");
        iframe.setTitle("k6 Web Dashboard");

        Anchor openExternal = new Anchor(K6_WEB_DASHBOARD_URL, "In neuem Tab öffnen ↗");
        openExternal.setTarget("_blank");
        openExternal.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("display", "block");

        Span hint = new Span("Das Dashboard ist nur sichtbar während ein Test läuft.");
        hint.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        Button refreshBtn = new Button("Dashboard aktualisieren", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e ->
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

    // ══════════════════════════════════════════════════════════════════════════
    // Test starten / stoppen
    // ══════════════════════════════════════════════════════════════════════════

    /** Startet einen der klassischen k6-Lasttests (Tab 1). */
    private void startTest() {
        TestType selected = testSelector.getValue();
        if (selected == null) {
            showError("Bitte ein Testszenario auswählen.");
            return;
        }

        UI ui = UI.getCurrent();
        logArea.clear();
        appendLog(logArea, "╔" + "═".repeat(58) + "╗");
        appendLog(logArea, "║  k6 LASTTEST GESTARTET");
        appendLog(logArea, "║  Szenario : " + selected.getDisplayName());
        appendLog(logArea, "║  Parameter: " + selected.getParameters());
        appendLog(logArea, "║  Dauer    : " + formatDuration(selected.getDurationSeconds()));
        appendLog(logArea, "╚" + "═".repeat(58) + "╝");
        appendLog(logArea, "");

        try {
            testService.startTest(selected, line -> ui.access(() -> {
                appendLog(logArea, line);
                if (line.startsWith("[FERTIG]")) {
                    appendLog(logArea, "");
                    appendLog(logArea, "╔" + "═".repeat(58) + "╗");
                    appendLog(logArea, "║  TEST BEENDET");
                    appendLog(logArea, "║  " + line);
                    appendLog(logArea, "╚" + "═".repeat(58) + "╝");
                    setTestStopped();
                }
            }));
            setTestRunning(selected);

        } catch (IOException | IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showError(msg);
            appendLog(logArea, "[ERROR] " + msg);
        }
    }

    /** Startet den Messaging-E2E-Test (Tab 2) mit den konfigurierten Parametern. */
    private void startMessagingE2ETest(HorizontalLayout progressRow) {
        UI ui = UI.getCurrent();
        msgLogArea.clear();

        // Konfiguration aus den Feldern lesen
        String  storeUrl    = msgStoreUrlField.getValue();
        int     storeId     = msgStoreIdField.getValue() != null ? msgStoreIdField.getValue() : 1;
        int     orderRate   = msgOrderRateField.getValue() != null ? msgOrderRateField.getValue() : 30;
        double  urgentRatio = msgUrgentRatioField.getValue() != null ? msgUrgentRatioField.getValue() : 0.3;
        int     verifyWait  = msgVerifyWaitField.getValue() != null ? msgVerifyWaitField.getValue() : 2000;

        Map<String, String> extraEnv = new HashMap<>();
        extraEnv.put("STORE_URL",       storeUrl);
        extraEnv.put("STORE_ID",        String.valueOf(storeId));
        extraEnv.put("ORDER_RATE",      String.valueOf(orderRate));
        extraEnv.put("URGENT_RATIO",    String.format(java.util.Locale.US, "%.1f", urgentRatio));
        extraEnv.put("VERIFY_STOCK",    "true");
        extraEnv.put("VERIFY_WAIT_MS",  String.valueOf(verifyWait));

        appendLog(msgLogArea, "╔" + "═".repeat(58) + "╗");
        appendLog(msgLogArea, "║  MESSAGING E2E TEST GESTARTET");
        appendLog(msgLogArea, "║  Store-URL   : " + storeUrl);
        appendLog(msgLogArea, "║  Store-ID    : " + storeId);
        appendLog(msgLogArea, "║  Rate        : " + orderRate + " Orders/min");
        appendLog(msgLogArea, "║  Urgent      : " + Math.round(urgentRatio * 100) + " %");
        appendLog(msgLogArea, "║  Verify-Wait : " + verifyWait + " ms");
        appendLog(msgLogArea, "║  Dauer       : " + formatDuration(TestType.MESSAGING_E2E.getDurationSeconds()));
        appendLog(msgLogArea, "╚" + "═".repeat(58) + "╝");
        appendLog(msgLogArea, "");
        appendLog(msgLogArea, "HINWEIS: Verifikation setzt voraus, dass JMeter auf der Logistik-");
        appendLog(msgLogArea, "         seite läuft und ArticleSentEvents zurückschickt.");
        appendLog(msgLogArea, "");

        try {
            testService.startTest(TestType.MESSAGING_E2E, line -> ui.access(() -> {
                appendLog(msgLogArea, line);
                if (line.startsWith("[FERTIG]")) {
                    appendLog(msgLogArea, "");
                    appendLog(msgLogArea, "╔" + "═".repeat(58) + "╗");
                    appendLog(msgLogArea, "║  TEST BEENDET: " + line);
                    appendLog(msgLogArea, "╚" + "═".repeat(58) + "╝");
                    setMessagingTestStopped(progressRow);
                }
            }), extraEnv);

            setMessagingTestRunning(progressRow);

        } catch (IOException | IllegalStateException e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            showError(msg);
            appendLog(msgLogArea, "[ERROR] " + msg);
        }
    }

    /** Stoppt den laufenden Test (funktioniert für beide Tabs). */
    private void stopTest(TextArea targetLog) {
        testService.stopTest();
        appendLog(targetLog, "");
        appendLog(targetLog, "■ Test manuell gestoppt.");
        if (targetLog == logArea) {
            setTestStopped();
        } else {
            setMessagingTestStopped(null);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UI-Zustand für Tab 1
    // ══════════════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════════════
    // UI-Zustand für Tab 2
    // ══════════════════════════════════════════════════════════════════════════

    private void setMessagingTestRunning(@Nullable HorizontalLayout progressRow) {
        msgStartButton.setEnabled(false);
        msgStopButton.setEnabled(true);
        setMessagingParamFieldsEnabled(false);
        msgStatusBadge.setText("● Messaging E2E läuft...");
        msgStatusBadge.getStyle().set("color", "var(--lumo-success-color)");
        msgProgressBar.setValue(0.0);
        msgProgressBar.setVisible(true);
        if (progressRow != null) progressRow.setVisible(true);
    }

    private void setMessagingTestStopped(@Nullable HorizontalLayout progressRow) {
        msgStartButton.setEnabled(true);
        msgStopButton.setEnabled(false);
        setMessagingParamFieldsEnabled(true);
        msgStatusBadge.setText("● Kein Test aktiv");
        msgStatusBadge.getStyle().set("color", "var(--lumo-secondary-text-color)");
        msgProgressBar.setValue(0.0);
        msgProgressBar.setVisible(false);
        msgProgressLabel.setText("–");
        msgRemainingLabel.setText("–");
        if (progressRow != null) progressRow.setVisible(false);

        UI ui = UI.getCurrent();
        if (ui != null) ui.setPollInterval(-1);
    }

    private void setMessagingParamFieldsEnabled(boolean enabled) {
        msgStoreUrlField.setEnabled(enabled);
        msgStoreIdField.setEnabled(enabled);
        msgOrderRateField.setEnabled(enabled);
        msgUrgentRatioField.setEnabled(enabled);
        msgVerifyWaitField.setEnabled(enabled);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Polling (für beide Tabs)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (testService.isRunning()) {
            testService.getActiveTest().ifPresent(active -> {
                if (active == TestType.MESSAGING_E2E) {
                    setMessagingTestRunning(null);
                    appendLog(msgLogArea, "[INFO] Messaging E2E läuft bereits.");
                } else {
                    testSelector.setValue(active);
                    setTestRunning(active);
                    appendLog(logArea, "[INFO] Test läuft bereits: " + active.getDisplayName());
                }
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

            long remaining = Math.max(0, test.getDurationSeconds() - elapsed);
            int  pct       = (int) (progress * 100);
            String progressText  = pct + "% (" + formatDuration((int) elapsed) +
                    " / " + formatDuration(test.getDurationSeconds()) + ")";
            String remainingText = "⏳ Noch ca. " + formatDuration((int) remaining);

            if (test == TestType.MESSAGING_E2E) {
                msgProgressBar.setValue(progress);
                msgProgressLabel.setText(progressText);
                msgRemainingLabel.setText(remainingText);
            } else {
                progressBar.setValue(progress);
                progressLabel.setText(progressText);
                remainingLabel.setText(remainingText);
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detachEvent.getUI().setPollInterval(-1);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Hilfsmethoden
    // ══════════════════════════════════════════════════════════════════════════

    private void appendLog(TextArea target, String line) {
        String current = target.getValue();
        String updated = current.isEmpty() ? line : current + "\n" + line;
        String[] lines = updated.split("\n");
        if (lines.length > 400) {
            updated = String.join("\n", Arrays.copyOfRange(lines, lines.length - 400, lines.length));
        }
        target.setValue(updated);
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
