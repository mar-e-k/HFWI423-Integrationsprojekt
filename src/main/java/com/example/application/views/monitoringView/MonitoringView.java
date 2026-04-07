package com.example.application.views.monitoringView;

import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.example.application.services.StorageLocationService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@PageTitle("Monitoring")
@Route("monitoring")
@Menu(order = 99, icon = LineAwesomeIconUrl.CHART_BAR_SOLID)
public class MonitoringView extends Div {

    // ── Services & Infrastruktur ──────────────────────────────────────────────
    private final MeterRegistry           meterRegistry;
    private final ArticleInfoService      articleInfoService;
    private final KommissionService       kommissionService;
    private final StorageLocationService  storageLocationService;
    private final int                     serverPort;

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean  = ManagementFactory.getThreadMXBean();

    // HttpClient für Lasttests – max. 200 simultane Verbindungen
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    // Endpoints die bei Lasttests abwechselnd angefragt werden
    private static final List<String> ENDPOINTS = List.of(
            "/api/load/articles?page=0&size=20",
            "/api/load/storage-locations",
            "/api/load/goods-receipts",
            "/api/load/kommissionen"
    );

    // ── Refresh-Scheduler für System-Metriken ────────────────────────────────
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "monitoring-refresh");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> refreshTask;

    // ── Lasttest-State ────────────────────────────────────────────────────────
    private final AtomicBoolean testRunning    = new AtomicBoolean(false);
    private final AtomicLong    totalRequests  = new AtomicLong(0);
    private final AtomicLong    successCount   = new AtomicLong(0);
    private final AtomicLong    errorCount     = new AtomicLong(0);
    private final AtomicLong    totalRespMs    = new AtomicLong(0);
    private volatile Instant    testStartTime;
    private volatile String     activeTestName = "";
    private ExecutorService     testExecutor;

    // ── System-Metrikkarten ──────────────────────────────────────────────────
    private final MetricCard heapCard    = new MetricCard("Heap Memory",   "MB",    "#6366f1");
    private final MetricCard threadsCard = new MetricCard("Live Threads",  "",      "#8b5cf6");
    private final MetricCard cpuCard     = new MetricCard("CPU Usage",     "%",     "#06b6d4");
    private final MetricCard httpCard    = new MetricCard("HTTP Requests", "total", "#10b981");
    private final MetricCard errorCard   = new MetricCard("HTTP Errors",   "total", "#f59e0b");
    private final MetricCard avgRespCard = new MetricCard("Ø Response",    "ms",    "#3b82f6");
    private final MetricCard articlesCard= new MetricCard("Artikel",       "ges.",  "#a855f7");
    private final MetricCard kommCard    = new MetricCard("Kommissionen",  "offen", "#ec4899");
    private final MetricCard storageCard = new MetricCard("Lagerplätze",   "frei",  "#14b8a6");
    private final Span lastUpdated       = new Span();

    // ── Lasttest-UI ──────────────────────────────────────────────────────────
    private Button stopButton;
    private final List<Button> testButtons = new java.util.ArrayList<>();

    // Live-Ergebnis-Karten
    private final MetricCard ltRequestsCard = new MetricCard("Requests",      "total", "#6366f1");
    private final MetricCard ltRpsCard      = new MetricCard("Throughput",    "req/s", "#10b981");
    private final MetricCard ltAvgCard      = new MetricCard("Ø Antwortzeit", "ms",    "#3b82f6");
    private final MetricCard ltErrorCard    = new MetricCard("Fehler",        "total", "#ef4444");
    private final MetricCard ltSuccessCard  = new MetricCard("Erfolgsrate",   "%",     "#10b981");
    private final MetricCard ltElapsedCard  = new MetricCard("Laufzeit",      "s",     "#8b5cf6");
    private Span testStatusBadge;
    private Div  testResultSection;

    // ─────────────────────────────────────────────────────────────────────────

    public MonitoringView(MeterRegistry meterRegistry,
                          ArticleInfoService articleInfoService,
                          KommissionService kommissionService,
                          StorageLocationService storageLocationService,
                          @Value("${server.port:8081}") int serverPort) {
        this.meterRegistry        = meterRegistry;
        this.articleInfoService   = articleInfoService;
        this.kommissionService    = kommissionService;
        this.storageLocationService = storageLocationService;
        this.serverPort           = serverPort;

        setSizeFull();
        addClassName("view-page");
        buildLayout();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Layout
    // ══════════════════════════════════════════════════════════════════════════

    private void buildLayout() {
        Div jvmSection  = section("JVM & System",   heapCard, threadsCard, cpuCard);
        Div httpSection = section("HTTP Traffic",   httpCard, errorCard, avgRespCard);
        Div bizSection  = section("Business",       articlesCard, kommCard, storageCard);

        lastUpdated.getStyle()
                .set("font-size", "0.78rem")
                .set("color", "#94a3b8")
                .set("padding", "4px 0 12px 2px");

        Div loadTestSection = buildLoadTestSection();

        VerticalLayout content = new VerticalLayout(
                lastUpdated, jvmSection, httpSection, bizSection, loadTestSection);
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();

        Div card = new Div(content);
        card.addClassName("content-card");
        card.getStyle().set("padding", "24px");
        card.setSizeFull();
        add(card);
    }

    private Div section(String title, MetricCard... cards) {
        H3 heading = new H3(title);
        heading.getStyle()
                .set("font-size", "0.75rem").set("font-weight", "700")
                .set("color", "#6366f1").set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em").set("margin", "20px 0 10px 0");

        HorizontalLayout row = new HorizontalLayout(cards);
        row.setWidthFull();
        row.getStyle().set("flex-wrap", "wrap").set("gap", "12px");

        Div sec = new Div(heading, row);
        sec.setWidthFull();
        return sec;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Lasttest-Sektion
    // ══════════════════════════════════════════════════════════════════════════

    private Div buildLoadTestSection() {
        H3 heading = new H3("Lasttests");
        heading.getStyle()
                .set("font-size", "0.75rem").set("font-weight", "700")
                .set("color", "#6366f1").set("text-transform", "uppercase")
                .set("letter-spacing", "0.1em").set("margin", "28px 0 4px 0");

        // Beschreibungstext
        Span desc = new Span("Tests laufen gegen die eigenen REST-Endpunkte (/api/load/...) und " +
                "messen Durchsatz, Antwortzeit und Fehlerrate unter Last.");
        desc.getStyle()
                .set("font-size", "0.82rem").set("color", "#64748b")
                .set("display", "block").set("margin-bottom", "14px");

        // ── Test-Buttons ──────────────────────────────────────────────────────
        Button soakBtn     = testButton("Soak Test",     "10 User · 2 Min",   "#6366f1",
                "Dauerlast – prüft Stabilität & Memory-Leaks über Zeit");
        Button spikeBtn    = testButton("Spike Test",    "1→100→1 User",      "#ef4444",
                "Plötzlicher Traffic-Spike – wie reagiert das System?");
        Button capacityBtn = testButton("Capacity Test", "5→50 User stufenw.","#f59e0b",
                "Kapazitätsgrenze finden – Last wird schrittweise erhöht");
        Button stressBtn   = testButton("Stress Test",   "Bis zum Limit",     "#dc2626",
                "Extremlast – System bis zur Fehlergrenze treiben");

        soakBtn.addClickListener(e     -> startSoakTest());
        spikeBtn.addClickListener(e    -> startSpikeTest());
        capacityBtn.addClickListener(e -> startCapacityTest());
        stressBtn.addClickListener(e   -> startStressTest());

        testButtons.addAll(List.of(soakBtn, spikeBtn, capacityBtn, stressBtn));

        // ── Stop-Button ───────────────────────────────────────────────────────
        stopButton = new Button("■  Stoppen");
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.getStyle()
                .set("font-weight", "700").set("min-width", "120px");
        stopButton.addClickListener(e -> stopTest());

        HorizontalLayout buttonRow = new HorizontalLayout(soakBtn, spikeBtn, capacityBtn, stressBtn, stopButton);
        buttonRow.setWidthFull();
        buttonRow.getStyle().set("flex-wrap", "wrap").set("gap", "10px").set("align-items", "flex-start");

        // ── Live-Ergebnisse ───────────────────────────────────────────────────
        testStatusBadge = new Span("Kein Test aktiv");
        testStatusBadge.getStyle()
                .set("display", "inline-block")
                .set("padding", "3px 12px").set("border-radius", "999px")
                .set("font-size", "0.75rem").set("font-weight", "700")
                .set("background", "#f1f5f9").set("color", "#64748b")
                .set("margin", "14px 0 8px 0");

        HorizontalLayout resultCards = new HorizontalLayout(
                ltRequestsCard, ltRpsCard, ltAvgCard,
                ltErrorCard, ltSuccessCard, ltElapsedCard);
        resultCards.setWidthFull();
        resultCards.getStyle().set("flex-wrap", "wrap").set("gap", "12px");

        testResultSection = new Div(testStatusBadge, resultCards);
        testResultSection.setWidthFull();
        testResultSection.getStyle()
                .set("background", "#fafbff")
                .set("border", "1px solid #e8edf5")
                .set("border-radius", "12px")
                .set("padding", "16px 20px")
                .set("margin-top", "12px");

        Div section = new Div(heading, desc, buttonRow, testResultSection);
        section.setWidthFull();
        return section;
    }

    private Button testButton(String label, String sub, String color, String tooltip) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "700").set("display", "block");

        Span subSpan = new Span(sub);
        subSpan.getStyle()
                .set("font-size", "0.72rem").set("opacity", "0.75")
                .set("display", "block").set("font-weight", "400");

        VerticalLayout inner = new VerticalLayout(labelSpan, subSpan);
        inner.setPadding(false);
        inner.setSpacing(false);

        Button btn = new Button(label + " · " + sub);
        btn.getElement().setProperty("title", tooltip);
        btn.getStyle()
                .set("background", color).set("color", "white")
                .set("border-radius", "10px").set("font-weight", "600")
                .set("padding", "10px 16px").set("white-space", "nowrap")
                .set("box-shadow", "0 2px 8px " + color + "55");
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test-Implementierungen
    // ══════════════════════════════════════════════════════════════════════════

    /** Soak Test: 10 User, 2 Minuten Dauerlast */
    private void startSoakTest() {
        startTest("Soak Test", () -> {
            int users = 10;
            long durationMs = 120_000;
            runConcurrentLoad(users, durationMs);
        });
    }

    /** Spike Test: 1 → 100 User sofort, 30 s halten, dann Stop */
    private void startSpikeTest() {
        startTest("Spike Test", () -> {
            // Phase 1: Warmup mit 1 User (5 s)
            runConcurrentLoad(1, 5_000);
            if (!testRunning.get()) return;

            // Phase 2: Spike auf 100 User (30 s)
            runConcurrentLoad(100, 30_000);
            if (!testRunning.get()) return;

            // Phase 3: Zurück auf 1 User (5 s)
            runConcurrentLoad(1, 5_000);
        });
    }

    /** Capacity Test: 5 → 50 User, alle 15 s um +5 erhöhen */
    private void startCapacityTest() {
        startTest("Capacity Test", () -> {
            for (int users = 5; users <= 50 && testRunning.get(); users += 5) {
                runConcurrentLoad(users, 15_000);
            }
        });
    }

    /** Stress Test: 10 → 150 User, alle 10 s um +20 – bis Fehlerrate > 20% */
    private void startStressTest() {
        startTest("Stress Test", () -> {
            for (int users = 10; users <= 150 && testRunning.get(); users += 20) {
                runConcurrentLoad(users, 10_000);

                // Fehlerrate prüfen → abbrechen falls > 20%
                long total = totalRequests.get();
                long errors = errorCount.get();
                if (total > 0 && (double) errors / total > 0.20) {
                    break;
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Test-Steuerung
    // ══════════════════════════════════════════════════════════════════════════

    private void startTest(String name, Runnable testLogic) {
        if (testRunning.getAndSet(true)) return;

        resetStats();
        activeTestName = name;
        testStartTime  = Instant.now();

        setTestButtonsEnabled(false);
        stopButton.setEnabled(true);
        updateStatusBadge(true);

        testExecutor = Executors.newVirtualThreadPerTaskExecutor();
        testExecutor.submit(() -> {
            try {
                testLogic.run();
            } finally {
                testRunning.set(false);
                testExecutor.shutdown();
                UI ui = getUI().orElse(null);
                if (ui != null) {
                    ui.access(() -> {
                        setTestButtonsEnabled(true);
                        stopButton.setEnabled(false);
                        updateStatusBadge(false);
                        Notification n = Notification.show(name + " abgeschlossen", 4000,
                                Notification.Position.BOTTOM_END);
                        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    });
                }
            }
        });
    }

    private void stopTest() {
        testRunning.set(false);
        if (testExecutor != null) testExecutor.shutdownNow();
    }

    /**
     * Erzeugt `users` virtuelle Threads, jeder macht HTTP-Requests in einer
     * Schleife für `durationMs` Millisekunden.
     */
    private void runConcurrentLoad(int users, long durationMs) {
        ExecutorService pool = Executors.newFixedThreadPool(users);
        long deadline = System.currentTimeMillis() + durationMs;

        for (int i = 0; i < users; i++) {
            final int threadIndex = i;
            pool.submit(() -> {
                int endpointIdx = threadIndex % ENDPOINTS.size();
                while (testRunning.get() && System.currentTimeMillis() < deadline) {
                    String path = ENDPOINTS.get(endpointIdx % ENDPOINTS.size());
                    sendRequest(path);
                    endpointIdx++;
                }
            });
        }

        pool.shutdown();
        try {
            pool.awaitTermination(durationMs + 5_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdownNow();
    }

    private void sendRequest(String path) {
        String url = "http://localhost:" + serverPort + path;
        long start = System.currentTimeMillis();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            long ms = System.currentTimeMillis() - start;
            totalRequests.incrementAndGet();
            totalRespMs.addAndGet(ms);
            if (resp.statusCode() < 400) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
        } catch (Exception e) {
            totalRequests.incrementAndGet();
            errorCount.incrementAndGet();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI-Updates
    // ══════════════════════════════════════════════════════════════════════════

    private void updateStatusBadge(boolean running) {
        if (running) {
            testStatusBadge.setText("▶  " + activeTestName + " läuft...");
            testStatusBadge.getStyle()
                    .set("background", "#dcfce7").set("color", "#16a34a");
        } else {
            testStatusBadge.setText("✓  " + activeTestName + " abgeschlossen");
            testStatusBadge.getStyle()
                    .set("background", "#ede9fe").set("color", "#6d28d9");
        }
    }

    private void updateLiveResults() {
        if (!testRunning.get() && testStartTime == null) return;

        long total   = totalRequests.get();
        long success = successCount.get();
        long errors  = errorCount.get();
        long respSum = totalRespMs.get();

        long elapsedSec = testStartTime != null
                ? Duration.between(testStartTime, Instant.now()).toSeconds() : 0;
        double rps     = elapsedSec > 0 ? (double) total / elapsedSec : 0;
        double avgMs   = total > 0 ? (double) respSum / total : 0;
        double succPct = total > 0 ? (double) success / total * 100 : 100;

        ltRequestsCard.setValue(String.valueOf(total), -1);
        ltRpsCard     .setValue(String.format("%.1f", rps), -1);
        ltAvgCard     .setValue(String.format("%.0f", avgMs), -1);
        ltErrorCard   .setValue(String.valueOf(errors),
                total > 0 ? (double) errors / total : 0);
        ltSuccessCard .setValue(String.format("%.1f", succPct),
                succPct / 100);
        ltElapsedCard .setValue(String.valueOf(elapsedSec), -1);
    }

    private void setTestButtonsEnabled(boolean enabled) {
        testButtons.forEach(b -> b.setEnabled(enabled));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  System-Metriken Refresh
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onAttach(AttachEvent event) {
        UI ui = event.getUI();
        refreshMetrics(ui);
        refreshTask = scheduler.scheduleAtFixedRate(
                () -> ui.access(() -> {
                    refreshMetrics(ui);
                    updateLiveResults();
                }),
                5, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (refreshTask != null) refreshTask.cancel(false);
        stopTest();
    }

    private void refreshMetrics(UI ui) {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed()  / (1024 * 1024);
        long heapMax  = memoryBean.getHeapMemoryUsage().getMax()   / (1024 * 1024);
        heapCard.setValue(heapUsed + " / " + heapMax, (double) heapUsed / heapMax);
        threadsCard.setValue(String.valueOf(threadBean.getThreadCount()), -1);

        double cpu = getGauge("process.cpu.usage");
        cpuCard.setValue(String.format("%.1f", cpu * 100), cpu);

        double totalHttp = getCounterSum("http.server.requests");
        double errHttp   = getCounterWithTag("http.server.requests", "outcome", "SERVER_ERROR")
                         + getCounterWithTag("http.server.requests", "outcome", "CLIENT_ERROR");
        double avgMs     = getTimerAvgMs("http.server.requests");

        httpCard.setValue(String.format("%.0f", totalHttp), -1);
        errorCard.setValue(String.format("%.0f", errHttp),
                totalHttp > 0 ? errHttp / totalHttp : 0);
        avgRespCard.setValue(String.format("%.1f", avgMs), -1);

        safeSet(articlesCard,
                () -> String.valueOf(articleInfoService.count(null)));
        safeSet(kommCard,
                () -> String.valueOf(kommissionService.getAlleKommissionen().stream()
                        .filter(k -> !Boolean.TRUE.equals(k.getFinished())).count()));
        safeSet(storageCard,
                () -> String.valueOf(storageLocationService.findAll().stream()
                        .filter(s -> "Available".equalsIgnoreCase(s.getStorageStatus())).count()));

        lastUpdated.setText("Zuletzt aktualisiert: " + java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private void resetStats() {
        totalRequests.set(0);
        successCount.set(0);
        errorCount.set(0);
        totalRespMs.set(0);
    }

    private void safeSet(MetricCard card, java.util.function.Supplier<String> fn) {
        try { card.setValue(fn.get(), -1); }
        catch (Exception ignored) { card.setValue("–", -1); }
    }

    private double getGauge(String name) {
        try { return meterRegistry.find(name).gauge().value(); }
        catch (Exception e) { return 0; }
    }

    private double getCounterSum(String name) {
        try { return meterRegistry.find(name).counters().stream()
                .mapToDouble(c -> c.count()).sum(); }
        catch (Exception e) { return 0; }
    }

    private double getCounterWithTag(String name, String key, String value) {
        try { return meterRegistry.find(name).tag(key, value).counters().stream()
                .mapToDouble(c -> c.count()).sum(); }
        catch (Exception e) { return 0; }
    }

    private double getTimerAvgMs(String name) {
        try { return meterRegistry.find(name).timers().stream()
                .filter(t -> t.count() > 0)
                .mapToDouble(t -> t.mean(TimeUnit.MILLISECONDS))
                .average().orElse(0); }
        catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MetricCard Komponente
    // ══════════════════════════════════════════════════════════════════════════

    static class MetricCard extends Div {

        private final Span valueSpan     = new Span("–");
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
