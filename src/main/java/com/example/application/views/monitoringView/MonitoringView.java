package com.example.application.views.monitoringView;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@PageTitle("Monitoring")
@Route("monitoring")
@Menu(order = 99, icon = LineAwesomeIconUrl.CHART_BAR_SOLID)
public class MonitoringView extends Div {

    // ── Konfiguration ─────────────────────────────────────────────────────────
    private final String jmeterHome;
    private final int    serverPort;

    // ── Scheduler für Live-Update ─────────────────────────────────────────────
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "monitoring-refresh");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> refreshTask;

    // ── JMeter-Prozess ────────────────────────────────────────────────────────
    private volatile Process jmeterProcess;
    private volatile Path    resultFile;
    private volatile long    csvReadOffset = 0;

    // ── Lasttest-State ────────────────────────────────────────────────────────
    private final AtomicBoolean testRunning   = new AtomicBoolean(false);
    private final AtomicLong    totalRequests = new AtomicLong(0);
    private final AtomicLong    successCount  = new AtomicLong(0);
    private final AtomicLong    errorCount    = new AtomicLong(0);
    private final AtomicLong    totalRespMs   = new AtomicLong(0);
    private volatile Instant    testStartTime;
    private volatile String     activeTestName = "";

    // ── Grafana-Metriken (Micrometer) ─────────────────────────────────────────
    private final AtomicLong activeUsersGauge = new AtomicLong(0);
    private final Counter    ltAllCounter;
    private final Counter    ltErrCounter;
    private final Timer      ltTimer;

    // ── Test-UI ───────────────────────────────────────────────────────────────
    private Button stopButton;
    private Span   testStatusBadge;
    private final List<Button> testButtons = new ArrayList<>();

    // ── Live-Metric-Karten ────────────────────────────────────────────────────
    private final MetricCard ltRequestsCard = new MetricCard("Requests",      "total", "#6366f1");
    private final MetricCard ltRpsCard      = new MetricCard("Throughput",    "req/s", "#10b981");
    private final MetricCard ltAvgCard      = new MetricCard("Ø Antwortzeit", "ms",    "#3b82f6");
    private final MetricCard ltErrorCard    = new MetricCard("Fehler",        "total", "#ef4444");
    private final MetricCard ltSuccessCard  = new MetricCard("Erfolgsrate",   "%",     "#10b981");
    private final MetricCard ltElapsedCard  = new MetricCard("Laufzeit",      "s",     "#8b5cf6");

    // ── Endpoint-Definition ───────────────────────────────────────────────────
    /** exampleBody: null = kein Body (GET / Trigger-Actions), sonst JSON-String */
    record EndpointDef(String method, String path, boolean implemented, String exampleBody) {
        EndpointDef(String method, String path, boolean implemented) {
            this(method, path, implemented, null);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public MonitoringView(MeterRegistry meterRegistry,
                          @Value("${server.port:8081}") int serverPort,
                          @Value("${jmeter.home:}") String jmeterHome) {
        this.serverPort = serverPort;
        this.jmeterHome = jmeterHome;

        Gauge.builder("loadtest.active.users", activeUsersGauge, AtomicLong::get)
                .description("Aktive simulierte User im Lasttest").register(meterRegistry);
        Gauge.builder("loadtest.running", testRunning, b -> b.get() ? 1.0 : 0.0)
                .description("1 wenn ein Lasttest gerade laeuft").register(meterRegistry);
        Gauge.builder("loadtest.requests.live", totalRequests, AtomicLong::get)
                .description("Gesamtanfragen im laufenden Test").register(meterRegistry);
        Gauge.builder("loadtest.errors.live", errorCount, AtomicLong::get)
                .description("Fehler im laufenden Test").register(meterRegistry);

        ltAllCounter = Counter.builder("loadtest.requests")
                .tag("outcome", "all").description("Alle Lasttest-Requests").register(meterRegistry);
        ltErrCounter = Counter.builder("loadtest.requests")
                .tag("outcome", "error").description("Fehlgeschlagene Lasttest-Requests").register(meterRegistry);
        ltTimer = Timer.builder("loadtest.response")
                .description("Antwortzeiten der Lasttest-Requests").register(meterRegistry);

        setSizeFull();
        addClassName("view-page");
        buildLayout();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Layout
    // ══════════════════════════════════════════════════════════════════════════

    private void buildLayout() {
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Endpunkte testen",  buildEndpointTab());
        tabs.add("Lasttests",         buildLoadTestTab());
        tabs.add("Grafana Dashboard", buildGrafanaTab());
        tabs.add("JMeter Ergebnisse", buildJMeterGrafanaTab());

        Div card = new Div(tabs);
        card.addClassName("content-card");
        card.getStyle().set("padding", "24px");
        card.setSizeFull();
        add(card);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tab 1: Endpunkte testen
    // ══════════════════════════════════════════════════════════════════════════

    private Div buildEndpointTab() {
        Span intro = new Span(
                "Einzelne Endpunkte direkt aufrufen und Antwort prüfen. " +
                "Ausgegraut = noch nicht implementiert (TODO).");
        intro.getStyle()
                .set("display", "block").set("font-size", "0.82rem").set("color", "#64748b")
                .set("padding", "0 0 16px 0");

        Details articlesSection = buildEndpointSection(
                "Articles", "LogisticMainView", "#6366f1", List.of(
                        new EndpointDef("GET",  "/articles",                       true),
                        new EndpointDef("GET",  "/articles/filter",                true),
                        new EndpointDef("POST", "/articles/{id}/stock",            true,
                                "{\"delta\": 5, \"reason\": \"Lasttest\"}"),
                        new EndpointDef("PUT",  "/articles/{id}/storage-location", true,
                                "{\"storageLocation\": \"Z1.S1.C1\"}")
                ));

        Details storageSection = buildEndpointSection(
                "Storage Locations", "StorageLocationView", "#10b981", List.of(
                        new EndpointDef("GET",    "/storage-locations",      true),
                        new EndpointDef("POST",   "/storage-locations",      true,
                                "{\"storageZone\": \"Zone 1\", \"shelfID\": 99, \"compartmentID\": 99}"),
                        new EndpointDef("DELETE", "/storage-locations/{id}", true),
                        new EndpointDef("POST",   "/storage-locations/sync", true)
                ));

        Details goodsSection = buildEndpointSection(
                "Goods Receipts", "GoodsReceiptView", "#f59e0b", List.of(
                        new EndpointDef("GET",    "/goods-receipts",                            true),
                        new EndpointDef("POST",   "/goods-receipts",                            true,
                                "{\"supplierName\": \"Testlieferant\", \"deliveryNoteNumber\": \"LN-TEST-001\", \"deliveryDate\": \"2026-04-12\"}"),
                        new EndpointDef("POST",   "/goods-receipts/{id}/items",                 true,
                                "{\"articleId\": 1, \"expectedQty\": 5, \"actualQty\": 5, \"defectNotes\": null}"),
                        new EndpointDef("PUT",    "/goods-receipts/{id}/items/{itemId}",        true,
                                "{\"actualQty\": 4, \"defectNotes\": \"Lasttest-Maengel\"}"),
                        new EndpointDef("PUT",    "/goods-receipts/{id}/items/{itemId}/status", true,
                                "{\"status\": \"FREIGEGEBEN\"}"),
                        new EndpointDef("POST",   "/goods-receipts/{id}/complete",              true),
                        new EndpointDef("DELETE", "/goods-receipts/{id}",                       true)
                ));

        Details kommSection = buildEndpointSection(
                "Kommissionen", "OrderPickingView", "#8b5cf6", List.of(
                        new EndpointDef("GET",  "/kommissionen",                                 true),
                        new EndpointDef("POST", "/kommissionen/trigger",                         true),
                        new EndpointDef("PUT",  "/kommissionen/{id}/finish",                     true),
                        new EndpointDef("PUT",  "/kommissionen/{id}/items/{articleId}/quantity", true,
                                "{\"quantity\": 10}")
                ));

        Details miscSection = buildEndpointSection(
                "Sonstige", "Restock / StockChange / NewArticles / Messaging", "#64748b", List.of(
                        new EndpointDef("GET", "/health",           true),
                        new EndpointDef("GET", "/restock",          true),
                        new EndpointDef("GET", "/stock-changes",    true),
                        new EndpointDef("GET", "/new-articles",     true),
                        new EndpointDef("GET", "/messaging-events", true)
                ));

        VerticalLayout layout = new VerticalLayout(
                intro, articlesSection, storageSection, goodsSection, kommSection, miscSection);
        layout.setPadding(false);
        layout.setWidthFull();
        layout.getStyle().set("gap", "8px");

        return new Div(layout);
    }

    private Details buildEndpointSection(String title, String viewName,
                                          String color, List<EndpointDef> endpoints) {
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "700").set("color", color).set("font-size", "0.95rem");
        Span viewSpan = new Span(" – " + viewName);
        viewSpan.getStyle().set("color", "#64748b").set("font-size", "0.82rem");
        Div header = new Div(titleSpan, viewSpan);

        VerticalLayout rows = new VerticalLayout();
        rows.setPadding(false);
        rows.setSpacing(false);
        rows.getStyle().set("gap", "4px");
        for (EndpointDef ep : endpoints) {
            rows.add(buildEndpointRow(ep));
        }

        Details details = new Details(header, rows);
        details.setWidthFull();
        details.getStyle()
                .set("border", "1px solid #e2e8f0").set("border-radius", "10px")
                .set("padding", "12px 16px").set("background", "white");
        return details;
    }

    private com.vaadin.flow.component.Component buildEndpointRow(EndpointDef ep) {
        String methodColor = switch (ep.method()) {
            case "POST"   -> "#3b82f6";
            case "PUT"    -> "#f59e0b";
            case "DELETE" -> "#ef4444";
            default       -> "#10b981"; // GET
        };

        Span methodBadge = new Span(ep.method());
        methodBadge.getStyle()
                .set("background", methodColor + "20").set("color", methodColor)
                .set("font-size", "0.68rem").set("font-weight", "700")
                .set("border-radius", "4px").set("padding", "2px 7px")
                .set("min-width", "54px").set("text-align", "center")
                .set("font-family", "monospace");

        Span pathSpan = new Span("/api/load" + ep.path());
        pathSpan.getStyle()
                .set("font-family", "monospace").set("font-size", "0.82rem")
                .set("color", "#1e293b").set("flex", "1");

        if (!ep.implemented()) {
            Span todoBadge = new Span("TODO");
            todoBadge.getStyle()
                    .set("background", "#fef3c7").set("color", "#92400e")
                    .set("font-size", "0.68rem").set("font-weight", "700")
                    .set("border-radius", "4px").set("padding", "2px 7px");
            HorizontalLayout row = new HorizontalLayout(methodBadge, pathSpan, todoBadge);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setPadding(false);
            row.getStyle().set("gap", "10px").set("padding", "3px 0").set("opacity", "0.55");
            return row;
        }

        // ── Ergebnis-Anzeige ──────────────────────────────────────────────────
        Span statusSpan  = new Span();
        Span timeSpan    = new Span();
        Span summarySpan = new Span();
        statusSpan.getStyle().set("font-size", "0.78rem").set("font-weight", "700").set("min-width", "36px");
        timeSpan.getStyle().set("font-size", "0.78rem").set("color", "#64748b").set("min-width", "55px");
        summarySpan.getStyle().set("font-size", "0.78rem").set("color", "#94a3b8");

        HorizontalLayout resultArea = new HorizontalLayout(statusSpan, timeSpan, summarySpan);
        resultArea.setAlignItems(FlexComponent.Alignment.CENTER);
        resultArea.setPadding(false);
        resultArea.getStyle().set("gap", "6px");
        resultArea.setVisible(false);

        // ── GET: einfacher Testen-Button ──────────────────────────────────────
        if ("GET".equals(ep.method())) {
            Button testBtn = new Button("Testen");
            testBtn.getStyle()
                    .set("font-size", "0.75rem").set("height", "26px")
                    .set("background", methodColor).set("color", "white")
                    .set("border-radius", "6px").set("font-weight", "600")
                    .set("padding", "0 10px");
            testBtn.addClickListener(e -> {
                UI ui = UI.getCurrent();
                resultArea.setVisible(false);
                sendRequest("GET", ep.path(), null, statusSpan, timeSpan, summarySpan, resultArea, testBtn, ui);
            });
            HorizontalLayout row = new HorizontalLayout(methodBadge, pathSpan, testBtn, resultArea);
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setPadding(false);
            row.getStyle().set("gap", "10px").set("padding", "3px 0");
            return row;
        }

        // ── POST / PUT / DELETE: erweiterbare Zeile mit Pfad-Inputs + Body ────

        // Pfad-Variable-Inputs parsen (z.B. {id}, {itemId}, {articleId})
        java.util.regex.Pattern varPattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher    = varPattern.matcher(ep.path());
        java.util.List<String>  varNames   = new java.util.ArrayList<>();
        while (matcher.find()) varNames.add(matcher.group(1));

        java.util.List<TextField> varInputs = new java.util.ArrayList<>();
        HorizontalLayout varRow = new HorizontalLayout();
        varRow.setPadding(false);
        varRow.getStyle().set("gap", "8px").set("flex-wrap", "wrap");

        for (String varName : varNames) {
            TextField input = new TextField(varName);
            input.setPlaceholder("z.B. 1");
            input.getStyle().set("width", "90px");
            input.getElement().setAttribute("theme", "small");
            varInputs.add(input);
            varRow.add(input);
        }

        // Body-Textarea (null = kein Body benoetigt)
        TextArea bodyArea = null;
        if (ep.exampleBody() != null) {
            bodyArea = new TextArea("Request Body (JSON)");
            bodyArea.setValue(ep.exampleBody());
            bodyArea.setWidthFull();
            bodyArea.getStyle()
                    .set("font-family", "monospace").set("font-size", "0.8rem")
                    .set("min-height", "80px");
            bodyArea.getElement().setAttribute("theme", "small");
        }

        // Senden-Button
        Button sendBtn = new Button("▶  Senden");
        sendBtn.getStyle()
                .set("font-size", "0.75rem").set("height", "28px")
                .set("background", methodColor).set("color", "white")
                .set("border-radius", "6px").set("font-weight", "600")
                .set("padding", "0 12px");

        final TextArea finalBodyArea = bodyArea;
        sendBtn.addClickListener(e -> {
            UI ui = UI.getCurrent();
            // Pfad-Variablen ersetzen
            String resolvedPath = ep.path();
            for (int i = 0; i < varNames.size(); i++) {
                String val = varInputs.get(i).getValue().trim();
                resolvedPath = resolvedPath.replace("{" + varNames.get(i) + "}", val.isEmpty() ? "0" : val);
            }
            String body = finalBodyArea != null ? finalBodyArea.getValue() : null;
            resultArea.setVisible(false);
            sendRequest(ep.method(), resolvedPath, body, statusSpan, timeSpan, summarySpan, resultArea, sendBtn, ui);
        });

        // Summary-Zeile (immer sichtbar im Accordion-Header)
        HorizontalLayout summaryRow = new HorizontalLayout(methodBadge, pathSpan, resultArea);
        summaryRow.setAlignItems(FlexComponent.Alignment.CENTER);
        summaryRow.setWidthFull();
        summaryRow.setPadding(false);
        summaryRow.getStyle().set("gap", "10px");

        // Formular-Inhalt
        VerticalLayout formContent = new VerticalLayout();
        formContent.setPadding(false);
        formContent.getStyle().set("gap", "8px").set("padding", "6px 0 2px 0");
        if (!varInputs.isEmpty()) formContent.add(varRow);
        if (bodyArea != null)     formContent.add(bodyArea);
        formContent.add(sendBtn);

        Details details = new Details(summaryRow, formContent);
        details.setWidthFull();
        details.getStyle().set("padding", "3px 0");
        return details;
    }

    private void sendRequest(String method, String path, String body,
                              Span statusSpan, Span timeSpan, Span summarySpan,
                              HorizontalLayout resultArea, Button btn, UI ui) {
        btn.setEnabled(false);
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            long start = System.currentTimeMillis();
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + serverPort + "/api/load" + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json");

                String jsonBody = body != null ? body : "";
                HttpRequest request = switch (method) {
                    case "POST"   -> reqBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
                    case "PUT"    -> reqBuilder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
                    case "DELETE" -> reqBuilder.DELETE().build();
                    default       -> reqBuilder.GET().build();
                };

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                long elapsed = System.currentTimeMillis() - start;

                int    status  = response.statusCode();
                String summary = summarizeBody(response.body());

                ui.access(() -> {
                    statusSpan.setText(String.valueOf(status));
                    statusSpan.getStyle().set("color", status < 300 ? "#10b981" : "#ef4444");
                    timeSpan.setText(elapsed + " ms");
                    summarySpan.setText(summary);
                    resultArea.setVisible(true);
                    btn.setEnabled(true);
                });
            } catch (Exception ex) {
                long elapsed = System.currentTimeMillis() - start;
                ui.access(() -> {
                    statusSpan.setText("ERR");
                    statusSpan.getStyle().set("color", "#ef4444");
                    timeSpan.setText(elapsed + " ms");
                    String msg = ex.getMessage() != null ? ex.getMessage() : "Verbindungsfehler";
                    summarySpan.setText(msg.length() > 50 ? msg.substring(0, 50) + "..." : msg);
                    resultArea.setVisible(true);
                    btn.setEnabled(true);
                });
            }
        });
    }

    private String summarizeBody(String body) {
        if (body == null || body.isBlank()) return "";
        body = body.trim();
        if (body.startsWith("[")) {
            long count = body.chars().filter(c -> c == '{').count();
            return count + " Eintraege";
        }
        if (body.contains("\"totalElements\"")) {
            int idx = body.indexOf("\"totalElements\":");
            if (idx >= 0) {
                String rest = body.substring(idx + 16).trim();
                String num  = rest.replaceAll("[^0-9].*", "");
                if (!num.isEmpty()) return num + " Eintraege";
            }
        }
        return body.length() + " bytes";
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tab 2: Lasttests (gruppiert nach View)
    // ══════════════════════════════════════════════════════════════════════════

    private Div buildLoadTestTab() {

        // ── Intro ─────────────────────────────────────────────────────────────
        Div intro = new Div();
        intro.getStyle()
                .set("background", "#f8faff").set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px").set("padding", "14px 18px")
                .set("margin-bottom", "16px");

        Span introTitle = new Span("JMeter-Lasttests nach View");
        introTitle.getStyle()
                .set("font-size", "1rem").set("font-weight", "700")
                .set("color", "#1e293b").set("display", "block").set("margin-bottom", "6px");
        Span introText = new Span(
                "Jeder Abschnitt entspricht einer View. " +
                "Die Tests laufen über Apache JMeter (parametrisiert) und senden Metriken an InfluxDB/Grafana. " +
                "Es kann jeweils nur ein Test aktiv sein.");
        introText.getStyle()
                .set("font-size", "0.83rem").set("color", "#475569")
                .set("line-height", "1.6").set("display", "block");
        intro.add(introTitle, introText);

        // ── View-Sektionen ────────────────────────────────────────────────────
        Details articlesAcc = buildViewTestSection(
                "Articles",          "LogisticMainView",                "#6366f1", "articles");
        Details storageAcc  = buildViewTestSection(
                "Storage Locations", "StorageLocationView",             "#10b981", "storage");
        Details goodsAcc    = buildViewTestSection(
                "Goods Receipts",    "GoodsReceiptView",                "#f59e0b", "goods");
        Details kommAcc     = buildViewTestSection(
                "Kommissionen",      "OrderPickingView",                "#8b5cf6", "komm");
        Details gesamtAcc   = buildViewTestSection(
                "Gesamt",            "Alle Views kombiniert",           "#64748b", "gesamt");

        VerticalLayout accordions = new VerticalLayout(
                articlesAcc, storageAcc, goodsAcc, kommAcc, gesamtAcc);
        accordions.setPadding(false);
        accordions.setWidthFull();
        accordions.getStyle().set("gap", "8px");

        // ── Szenarien ─────────────────────────────────────────────────────────
        Div szTitle = new Div();
        Span szLabel = new Span("Szenarien");
        szLabel.getStyle()
                .set("font-size", "1rem").set("font-weight", "700")
                .set("color", "#1e293b").set("display", "block")
                .set("margin-top", "8px").set("margin-bottom", "4px");
        szTitle.add(szLabel);

        Details writLoadAcc  = buildScenarioSection(
                "Write-Load",
                "Alle Threads schreiben gleichzeitig: GET articles -> POST stock -> POST goods-receipt -> DELETE",
                "#3b82f6", 20, 10, 60, "Write-Load", "logistik-write-load.jmx");

        Details mixedAcc     = buildScenarioSection(
                "Mixed 80/20",
                "80 % lesende GET-Anfragen (zufällig), 20 % schreibende POST stock-Anfragen",
                "#10b981", 20, 10, 60, "Mixed-Load", "logistik-mixed.jmx");

        Details workflowAcc  = buildScenarioSection(
                "Workflow E2E",
                "Vollständiger Wareneingangs-Workflow als Transaktion: POST receipt -> GET article -> POST item -> PUT status -> POST complete",
                "#f59e0b", 10, 10, 60, "Workflow-E2E", "logistik-workflow.jmx");

        Details conflictAcc  = buildScenarioSection(
                "Concurrent Conflict",
                "Alle 50 Threads greifen gleichzeitig auf denselben Artikel zu (0 s Rampup, kein Think-Time) – testet Locking-Verhalten",
                "#ef4444", 50, 0, 30, "Concurrent-Conflict", "logistik-conflict.jmx");

        VerticalLayout scenarios = new VerticalLayout(
                szTitle, writLoadAcc, mixedAcc, workflowAcc, conflictAcc);
        scenarios.setPadding(false);
        scenarios.setWidthFull();
        scenarios.getStyle().set("gap", "8px");

        // ── Live-Metriken ─────────────────────────────────────────────────────
        HorizontalLayout cardsRow = new HorizontalLayout(
                ltRequestsCard, ltRpsCard, ltAvgCard,
                ltErrorCard, ltSuccessCard, ltElapsedCard);
        cardsRow.setWidthFull();
        cardsRow.setPadding(false);
        cardsRow.getStyle().set("flex-wrap", "wrap").set("gap", "12px");

        // ── Stop + Status ─────────────────────────────────────────────────────
        stopButton = new Button("■  Laufenden Test stoppen");
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.getStyle().set("font-weight", "700");
        stopButton.addClickListener(e -> stopTest());

        testStatusBadge = new Span("Kein Test aktiv");
        testStatusBadge.getStyle()
                .set("font-size", "0.82rem").set("padding", "4px 14px")
                .set("border-radius", "20px").set("background", "#f1f5f9")
                .set("color", "#64748b");

        HorizontalLayout footer = new HorizontalLayout(stopButton, testStatusBadge);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(false);
        footer.getStyle().set("gap", "14px");

        VerticalLayout tab = new VerticalLayout(intro, accordions, scenarios, cardsRow, footer);
        tab.setPadding(false);
        tab.setWidthFull();
        tab.getStyle().set("gap", "16px");

        return new Div(tab);
    }

    private Details buildViewTestSection(String title, String viewName,
                                          String color, String testPrefix) {
        // ── Header ────────────────────────────────────────────────────────────
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "700").set("color", color).set("font-size", "0.95rem");
        Span viewSpan = new Span(" – " + viewName);
        viewSpan.getStyle().set("color", "#64748b").set("font-size", "0.82rem");
        Div header = new Div(titleSpan, viewSpan);

        // ── Testtyp-Buttons ───────────────────────────────────────────────────
        Button soakBtn     = buildTestTypeButton("Soak",     "#6366f1", 10,  10, 120, testPrefix + "-Soak",     "logistik-parametrisiert.jmx");
        Button spikeBtn    = buildTestTypeButton("Spike",    "#ef4444", 100,  2,  40, testPrefix + "-Spike",    "logistik-parametrisiert.jmx");
        Button capacityBtn = buildTestTypeButton("Capacity", "#f59e0b",  50, 45,  90, testPrefix + "-Capacity", "logistik-parametrisiert.jmx");
        Button stressBtn   = buildTestTypeButton("Stress",   "#dc2626", 150,  5,  60, testPrefix + "-Stress",   "logistik-parametrisiert.jmx");

        HorizontalLayout btnRow = new HorizontalLayout(soakBtn, spikeBtn, capacityBtn, stressBtn);
        btnRow.setPadding(false);
        btnRow.getStyle().set("gap", "8px").set("flex-wrap", "wrap");

        // ── Parameter-Badges ──────────────────────────────────────────────────
        HorizontalLayout paramRow = new HorizontalLayout(
                testParamBadge("Soak",     "10 User · 120 s",  "#6366f1"),
                testParamBadge("Spike",    "100 User · 40 s",  "#ef4444"),
                testParamBadge("Capacity", "50 User · 90 s",   "#f59e0b"),
                testParamBadge("Stress",   "150 User · 60 s",  "#dc2626")
        );
        paramRow.setPadding(false);
        paramRow.getStyle().set("gap", "6px").set("flex-wrap", "wrap");

        VerticalLayout content = new VerticalLayout(paramRow, btnRow);
        content.setPadding(false);
        content.getStyle().set("gap", "10px");

        Details details = new Details(header, content);
        details.setWidthFull();
        details.getStyle()
                .set("border", "1px solid #e2e8f0").set("border-radius", "10px")
                .set("padding", "12px 16px").set("background", "white");
        return details;
    }

    private Details buildScenarioSection(String title, String description,
                                          String color, int threads, int rampup, int duration,
                                          String testName, String jmxFileName) {
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "700").set("color", color).set("font-size", "0.95rem");
        Div header = new Div(titleSpan);

        Span desc = new Span(description);
        desc.getStyle()
                .set("font-size", "0.8rem").set("color", "#64748b")
                .set("display", "block").set("margin-bottom", "10px");

        HorizontalLayout paramRow = new HorizontalLayout(
                testParamBadge("Threads",  String.valueOf(threads),          color),
                testParamBadge("Rampup",   rampup + " s",                    color),
                testParamBadge("Dauer",    duration + " s",                  color),
                testParamBadge("JMX",      jmxFileName,                      "#64748b")
        );
        paramRow.setPadding(false);
        paramRow.getStyle().set("gap", "6px").set("flex-wrap", "wrap").set("margin-bottom", "8px");

        Button runBtn = buildTestTypeButton("Starten", color, threads, rampup, duration, testName, jmxFileName);

        VerticalLayout content = new VerticalLayout(desc, paramRow, runBtn);
        content.setPadding(false);
        content.getStyle().set("gap", "6px");

        Details details = new Details(header, content);
        details.setWidthFull();
        details.getStyle()
                .set("border", "1px solid #e2e8f0").set("border-radius", "10px")
                .set("padding", "12px 16px").set("background", "white");
        return details;
    }

    private Button buildTestTypeButton(String label, String color,
                                        int threads, int rampup, int duration,
                                        String testName, String jmxFileName) {
        Button btn = new Button("▶  " + label);
        btn.getStyle()
                .set("background", color).set("color", "white")
                .set("border-radius", "8px").set("font-weight", "700").set("font-size", "0.82rem")
                .set("box-shadow", "0 2px 8px " + color + "55");
        btn.addClickListener(e -> launchJMeter(testName, threads, rampup, duration, jmxFileName));
        testButtons.add(btn);
        return btn;
    }

    private Div testParamBadge(String label, String value, String color) {
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("color", "#64748b").set("font-size", "0.72rem");
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "700").set("color", color).set("font-size", "0.72rem");

        Div badge = new Div(labelSpan, valueSpan);
        badge.getStyle()
                .set("background", "#f8faff").set("border", "1px solid #e2e8f0")
                .set("border-radius", "6px").set("padding", "3px 10px")
                .set("display", "inline-flex").set("align-items", "center");
        return badge;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tab 3: Grafana iFrame
    // ══════════════════════════════════════════════════════════════════════════

    private Div buildGrafanaTab() {
        Span hint = new Span(
                "Grafana muss unter http://localhost:3000 laufen " +
                "(docker compose up in /monitoring). " +
                "Beim ersten Start kann es 10-20 Sekunden dauern.");
        hint.getStyle()
                .set("display", "block").set("font-size", "0.8rem")
                .set("color", "#64748b").set("padding", "6px 0 12px 0");

        IFrame frame = new IFrame(
                "http://localhost:3000/d/logistik-loadtest-v1" +
                "?orgId=1&kiosk=tv&theme=light&refresh=10s");
        frame.setWidth("100%");
        frame.setHeight("900px");
        frame.getElement().setAttribute("frameborder", "0");
        frame.getElement().setAttribute("allowfullscreen", "true");
        frame.getStyle()
                .set("border-radius", "12px").set("border", "1px solid #e8edf5");

        Div wrapper = new Div(hint, frame);
        wrapper.setWidthFull();
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Tab 4: JMeter Ergebnisse (Grafana)
    // ══════════════════════════════════════════════════════════════════════════

    private Div buildJMeterGrafanaTab() {
        Span hint = new Span(
                "Zeigt JMeter-Testergebnisse aus InfluxDB. " +
                "Starte einen Test im Tab \"Lasttests\", dann hier application & transaction auswaehlen.");
        hint.getStyle()
                .set("display", "block").set("font-size", "0.8rem")
                .set("color", "#64748b").set("padding", "6px 0 12px 0");

        IFrame frame = new IFrame(
                "http://localhost:3000/d/cfieztrek7e9se" +
                "?orgId=1&kiosk=tv&theme=light&refresh=5s");
        frame.setWidth("100%");
        frame.setHeight("900px");
        frame.getElement().setAttribute("frameborder", "0");
        frame.getElement().setAttribute("allowfullscreen", "true");
        frame.getStyle()
                .set("border-radius", "12px").set("border", "1px solid #e8edf5");

        Div wrapper = new Div(hint, frame);
        wrapper.setWidthFull();
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  JMeter-Steuerung
    // ══════════════════════════════════════════════════════════════════════════

    private void launchJMeter(String name, int threads, int rampup, int duration, String jmxFileName) {
        if (testRunning.getAndSet(true)) return;

        String jmeterJar = jmeterHome + "/bin/ApacheJMeter.jar";

        if (jmeterHome.isBlank() || !Path.of(jmeterJar).toFile().exists()) {
            showError("JMeter nicht gefunden. Bitte jmeter.home in application.properties setzen.\n" +
                      "Erwartet: \"" + jmeterJar + "\"");
            testRunning.set(false);
            return;
        }

        Path jmxFile = Path.of("monitoring/jmeter/" + jmxFileName).toAbsolutePath();
        if (!jmxFile.toFile().exists()) {
            showError("JMX-Datei nicht gefunden: " + jmxFile);
            testRunning.set(false);
            return;
        }

        try {
            Path resultsDir = Path.of("monitoring/jmeter/results");
            Files.createDirectories(resultsDir);
            resultFile    = resultsDir.resolve("lasttest-result.csv").toAbsolutePath();
            Files.deleteIfExists(resultFile);
            csvReadOffset = 0;
        } catch (IOException ex) {
            showError("Konnte Ergebnisordner nicht anlegen: " + ex.getMessage());
            testRunning.set(false);
            return;
        }

        resetStats();
        activeTestName = name;
        testStartTime  = Instant.now();
        activeUsersGauge.set(threads);

        setTestButtonsEnabled(false);
        stopButton.setEnabled(true);
        updateStatusBadge(true);

        List<String> cmd = new ArrayList<>(List.of(
                "java", "-jar", jmeterJar,
                "-n",
                "-t", jmxFile.toString(),
                "-l", resultFile.toString(),
                "-Jthreads="  + threads,
                "-Jrampup="   + rampup,
                "-Jduration=" + duration,
                "-Jhost=localhost",
                "-Jport="     + serverPort,
                "-Jtestname=" + name.replace(" ", "-")
        ));

        Notification.show("JMeter wird gestartet...  " + String.join(" ", cmd),
                4000, Notification.Position.BOTTOM_END);

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            StringBuilder jmeterOutput = new StringBuilder();
            try {
                jmeterProcess = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .start();

                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(jmeterProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jmeterOutput.append(line).append("\n");
                    }
                }

                int exitCode = jmeterProcess.waitFor();
                if (exitCode != 0) {
                    String out = jmeterOutput.toString();
                    UI ui = getUI().orElse(null);
                    if (ui != null) ui.access(() ->
                            showError("JMeter Exit-Code " + exitCode + ":\n" +
                                      out.substring(Math.max(0, out.length() - 500))));
                }
            } catch (Exception ex) {
                UI ui = getUI().orElse(null);
                if (ui != null) ui.access(() ->
                        showError("JMeter-Fehler: " + ex.getMessage() + "\nOutput:\n" +
                                  jmeterOutput.substring(Math.max(0, jmeterOutput.length() - 300))));
            } finally {
                pollCsvResults();
                testRunning.set(false);
                activeUsersGauge.set(0);
                UI ui = getUI().orElse(null);
                if (ui != null) {
                    ui.access(() -> {
                        updateLiveResults();
                        setTestButtonsEnabled(true);
                        stopButton.setEnabled(false);
                        updateStatusBadge(false);
                        Notification n = Notification.show(name + " abgeschlossen",
                                4000, Notification.Position.BOTTOM_END);
                        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    });
                }
            }
        });
    }

    private void stopTest() {
        testRunning.set(false);
        if (jmeterProcess != null) jmeterProcess.destroyForcibly();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CSV-Polling
    // ══════════════════════════════════════════════════════════════════════════

    private void pollCsvResults() {
        if (resultFile == null || !resultFile.toFile().exists()) return;
        try (RandomAccessFile raf = new RandomAccessFile(resultFile.toFile(), "r")) {
            raf.seek(csvReadOffset);
            String line;
            while ((line = raf.readLine()) != null) {
                if (line.startsWith("timeStamp") || line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;
                try {
                    long    elapsed = Long.parseLong(cols[1].trim());
                    boolean success = "true".equalsIgnoreCase(cols[7].trim());

                    totalRequests.incrementAndGet();
                    totalRespMs.addAndGet(elapsed);
                    ltAllCounter.increment();
                    ltTimer.record(elapsed, TimeUnit.MILLISECONDS);
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                        ltErrCounter.increment();
                    }
                } catch (NumberFormatException ignored) { }
            }
            csvReadOffset = raf.getFilePointer();
        } catch (IOException ignored) { }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI-Updates
    // ══════════════════════════════════════════════════════════════════════════

    private void updateStatusBadge(boolean running) {
        if (running) {
            testStatusBadge.setText("▶  " + activeTestName + " laeuft (JMeter)...");
            testStatusBadge.getStyle().set("background", "#dcfce7").set("color", "#16a34a");
        } else {
            testStatusBadge.setText("✓  " + activeTestName + " abgeschlossen");
            testStatusBadge.getStyle().set("background", "#ede9fe").set("color", "#6d28d9");
        }
    }

    private void updateLiveResults() {
        if (!testRunning.get() && testStartTime == null) return;

        long total   = totalRequests.get();
        long success = successCount.get();
        long errors  = errorCount.get();
        long respSum = totalRespMs.get();

        long   elapsed = testStartTime != null
                ? Duration.between(testStartTime, Instant.now()).toSeconds() : 0;
        double rps     = elapsed > 0 ? (double) total / elapsed : 0;
        double avgMs   = total > 0 ? (double) respSum / total : 0;
        double succPct = total > 0 ? (double) success / total * 100 : 100;

        ltRequestsCard.setValue(String.valueOf(total),           -1);
        ltRpsCard     .setValue(String.format("%.1f", rps),      -1);
        ltAvgCard     .setValue(String.format("%.0f", avgMs),    -1);
        ltErrorCard   .setValue(String.valueOf(errors),
                total > 0 ? (double) errors / total : 0);
        ltSuccessCard .setValue(String.format("%.1f", succPct),  succPct / 100);
        ltElapsedCard .setValue(String.valueOf(elapsed),         -1);
    }

    private void setTestButtonsEnabled(boolean enabled) {
        testButtons.forEach(b -> b.setEnabled(enabled));
    }

    private void resetStats() {
        totalRequests.set(0); successCount.set(0);
        errorCount.set(0);    totalRespMs.set(0);
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 6000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Attach / Detach
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onAttach(AttachEvent event) {
        UI ui = event.getUI();
        refreshTask = scheduler.scheduleAtFixedRate(() -> ui.access(() -> {
            pollCsvResults();
            updateLiveResults();
        }), 2, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (refreshTask != null) refreshTask.cancel(false);
        stopTest();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MetricCard-Komponente
    // ══════════════════════════════════════════════════════════════════════════

    static class MetricCard extends Div {

        private final Span valueSpan     = new Span("-");
        private final Div  progressBar   = new Div();
        private final Div  progressTrack = new Div();
        private final String accentColor;

        MetricCard(String label, String unit, String color) {
            this.accentColor = color;

            getStyle()
                    .set("background", "white").set("border-radius", "12px")
                    .set("padding", "16px 20px")
                    .set("box-shadow", "0 0 0 1px rgba(99,102,241,0.08), 0 2px 8px rgba(15,23,42,0.07)")
                    .set("min-width", "130px").set("flex", "1")
                    .set("display", "flex").set("flex-direction", "column")
                    .set("gap", "8px").set("transition", "box-shadow 0.2s ease");

            Span labelSpan = new Span(label);
            labelSpan.getStyle()
                    .set("font-size", "0.72rem").set("font-weight", "600")
                    .set("color", "#94a3b8").set("text-transform", "uppercase")
                    .set("letter-spacing", "0.08em");

            valueSpan.getStyle()
                    .set("font-size", "1.6rem").set("font-weight", "800")
                    .set("color", color).set("line-height", "1")
                    .set("letter-spacing", "-0.03em");

            Span unitSpan = new Span(unit.isEmpty() ? "" : " " + unit);
            unitSpan.getStyle()
                    .set("font-size", "0.72rem").set("color", "#94a3b8").set("font-weight", "500");

            progressBar.getStyle()
                    .set("height", "4px").set("border-radius", "999px")
                    .set("background", color).set("width", "0%")
                    .set("transition", "width 0.4s cubic-bezier(0.4,0,0.2,1)");
            progressTrack.add(progressBar);
            progressTrack.getStyle()
                    .set("height", "4px").set("border-radius", "999px")
                    .set("background", "#f1f5f9").set("overflow", "hidden");

            HorizontalLayout valueRow = new HorizontalLayout(valueSpan, unitSpan);
            valueRow.setPadding(false);
            valueRow.setSpacing(false);
            valueRow.setAlignItems(FlexComponent.Alignment.BASELINE);

            add(labelSpan, valueRow, progressTrack);
        }

        void setValue(String text, double fraction) {
            valueSpan.setText(text);
            if (fraction >= 0) {
                double pct = Math.min(fraction * 100, 100);
                progressBar.getStyle().set("width", pct + "%");
                progressBar.getStyle().set("background",
                        pct > 85 ? "#ef4444" : pct > 60 ? "#f59e0b" : accentColor);
                progressTrack.setVisible(true);
            } else {
                progressTrack.setVisible(false);
            }
        }
    }
}
