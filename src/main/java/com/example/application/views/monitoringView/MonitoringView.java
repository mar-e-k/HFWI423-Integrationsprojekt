package com.example.application.views.monitoringView;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

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

    // ── Infrastruktur ─────────────────────────────────────────────────────────
    private final int serverPort;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();

    private static final List<String> ENDPOINTS = List.of(
            "/api/load/articles?page=0&size=20",
            "/api/load/storage-locations",
            "/api/load/goods-receipts",
            "/api/load/kommissionen"
    );

    // ── Scheduler für Live-Update der Ergebniskarten ──────────────────────────
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "monitoring-refresh");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> refreshTask;

    // ── Lasttest-State ────────────────────────────────────────────────────────
    private final AtomicBoolean testRunning   = new AtomicBoolean(false);
    private final AtomicLong    totalRequests = new AtomicLong(0);
    private final AtomicLong    successCount  = new AtomicLong(0);
    private final AtomicLong    errorCount    = new AtomicLong(0);
    private final AtomicLong    totalRespMs   = new AtomicLong(0);
    private volatile Instant    testStartTime;
    private volatile String     activeTestName = "";
    private ExecutorService     testExecutor;

    // ── Grafana-Metriken (Micrometer) ─────────────────────────────────────────
    private final AtomicLong activeUsersGauge = new AtomicLong(0);
    private Counter ltAllCounter;
    private Counter ltErrCounter;
    private Timer   ltTimer;

    // ── Lasttest-UI ──────────────────────────────────────────────────────────
    private Button stopButton;
    private final List<Button> testButtons = new java.util.ArrayList<>();

    private final MetricCard ltRequestsCard = new MetricCard("Requests",      "total", "#6366f1");
    private final MetricCard ltRpsCard      = new MetricCard("Throughput",    "req/s", "#10b981");
    private final MetricCard ltAvgCard      = new MetricCard("Ø Antwortzeit", "ms",    "#3b82f6");
    private final MetricCard ltErrorCard    = new MetricCard("Fehler",        "total", "#ef4444");
    private final MetricCard ltSuccessCard  = new MetricCard("Erfolgsrate",   "%",     "#10b981");
    private final MetricCard ltElapsedCard  = new MetricCard("Laufzeit",      "s",     "#8b5cf6");
    private Span testStatusBadge;

    // ─────────────────────────────────────────────────────────────────────────

    public MonitoringView(MeterRegistry meterRegistry,
                          @Value("${server.port:8081}") int serverPort) {
        this.serverPort = serverPort;

        Gauge.builder("loadtest.active.users", activeUsersGauge, AtomicLong::get)
                .description("Aktive simulierte User im Lasttest").register(meterRegistry);
        Gauge.builder("loadtest.running", testRunning, b -> b.get() ? 1.0 : 0.0)
                .description("1 wenn ein Lasttest gerade läuft").register(meterRegistry);
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
        tabs.add("Lasttests",         buildLoadTestTab());
        tabs.add("Grafana Dashboard", buildGrafanaTab());

        Div card = new Div(tabs);
        card.addClassName("content-card");
        card.getStyle().set("padding", "24px");
        card.setSizeFull();
        add(card);
    }

    // ── Tab 1: Lasttest-Steuerung ─────────────────────────────────────────────

    private Div buildLoadTestTab() {
        Span desc = new Span(
                "Tests laufen gegen die REST-Endpunkte (/api/load/...) der eigenen App. " +
                "Ergebnisse werden live hier und in Grafana angezeigt.");
        desc.getStyle()
                .set("font-size", "0.82rem").set("color", "#64748b")
                .set("display", "block").set("margin-bottom", "20px");

        // ── Buttons ───────────────────────────────────────────────────────────
        Button soakBtn     = testButton("Soak Test",     "10 User · 2 Min",    "#6366f1",
                "Dauerlast – prüft Stabilität & Memory-Leaks über Zeit");
        Button spikeBtn    = testButton("Spike Test",    "1 → 100 → 1 User",   "#ef4444",
                "Plötzlicher Traffic-Spike – wie reagiert das System?");
        Button capacityBtn = testButton("Capacity Test", "5 → 50 User stufenw.","#f59e0b",
                "Kapazitätsgrenze finden – Last wird schrittweise erhöht");
        Button stressBtn   = testButton("Stress Test",   "Bis zum Limit",       "#dc2626",
                "Extremlast – System bis zur Fehlergrenze treiben");

        soakBtn    .addClickListener(e -> startSoakTest());
        spikeBtn   .addClickListener(e -> startSpikeTest());
        capacityBtn.addClickListener(e -> startCapacityTest());
        stressBtn  .addClickListener(e -> startStressTest());
        testButtons.addAll(List.of(soakBtn, spikeBtn, capacityBtn, stressBtn));

        stopButton = new Button("■  Stoppen");
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.getStyle().set("font-weight", "700").set("min-width", "120px");
        stopButton.addClickListener(e -> stopTest());

        HorizontalLayout buttonRow = new HorizontalLayout(
                soakBtn, spikeBtn, capacityBtn, stressBtn, stopButton);
        buttonRow.setWidthFull();
        buttonRow.getStyle()
                .set("flex-wrap", "wrap").set("gap", "10px").set("align-items", "flex-start");

        // ── Live-Ergebnisse ───────────────────────────────────────────────────
        testStatusBadge = new Span("Kein Test aktiv");
        testStatusBadge.getStyle()
                .set("display", "inline-block")
                .set("padding", "3px 12px").set("border-radius", "999px")
                .set("font-size", "0.75rem").set("font-weight", "700")
                .set("background", "#f1f5f9").set("color", "#64748b")
                .set("margin", "20px 0 10px 0");

        HorizontalLayout resultCards = new HorizontalLayout(
                ltRequestsCard, ltRpsCard, ltAvgCard,
                ltErrorCard, ltSuccessCard, ltElapsedCard);
        resultCards.setWidthFull();
        resultCards.getStyle().set("flex-wrap", "wrap").set("gap", "12px");

        Div resultBox = new Div(testStatusBadge, resultCards);
        resultBox.setWidthFull();
        resultBox.getStyle()
                .set("background", "#fafbff")
                .set("border", "1px solid #e8edf5")
                .set("border-radius", "12px")
                .set("padding", "16px 20px")
                .set("margin-top", "16px");

        VerticalLayout tab = new VerticalLayout(desc, buttonRow, resultBox);
        tab.setPadding(false);
        tab.setSpacing(false);
        tab.setWidthFull();

        Div wrapper = new Div(tab);
        wrapper.setWidthFull();
        return wrapper;
    }

    // ── Tab 2: Grafana iFrame ─────────────────────────────────────────────────

    private Div buildGrafanaTab() {
        Span hint = new Span(
                "Grafana muss unter http://localhost:3000 laufen " +
                "(docker compose up in /monitoring). " +
                "Beim ersten Start kann es 10–20 Sekunden dauern.");
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
                .set("border-radius", "12px")
                .set("border", "1px solid #e8edf5");

        Div wrapper = new Div(hint, frame);
        wrapper.setWidthFull();
        return wrapper;
    }

    // ── Hilfs-Methode: Test-Button ────────────────────────────────────────────

    private Button testButton(String label, String sub, String color, String tooltip) {
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

    private void startSoakTest() {
        startTest("Soak Test", () -> runConcurrentLoad(10, 120_000));
    }

    private void startSpikeTest() {
        startTest("Spike Test", () -> {
            runConcurrentLoad(1,   5_000);
            if (!testRunning.get()) return;
            runConcurrentLoad(100, 30_000);
            if (!testRunning.get()) return;
            runConcurrentLoad(1,   5_000);
        });
    }

    private void startCapacityTest() {
        startTest("Capacity Test", () -> {
            for (int users = 5; users <= 50 && testRunning.get(); users += 5) {
                runConcurrentLoad(users, 15_000);
            }
        });
    }

    private void startStressTest() {
        startTest("Stress Test", () -> {
            for (int users = 10; users <= 150 && testRunning.get(); users += 20) {
                runConcurrentLoad(users, 10_000);
                long total = totalRequests.get();
                if (total > 0 && (double) errorCount.get() / total > 0.20) break;
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

    private void runConcurrentLoad(int users, long durationMs) {
        activeUsersGauge.set(users);
        ExecutorService pool = Executors.newFixedThreadPool(users);
        long deadline = System.currentTimeMillis() + durationMs;

        for (int i = 0; i < users; i++) {
            final int threadIndex = i;
            pool.submit(() -> {
                int idx = threadIndex % ENDPOINTS.size();
                while (testRunning.get() && System.currentTimeMillis() < deadline) {
                    sendRequest(ENDPOINTS.get(idx % ENDPOINTS.size()));
                    idx++;
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
        activeUsersGauge.set(0);
    }

    private void sendRequest(String path) {
        String url = "http://localhost:" + serverPort + path;
        long start = System.currentTimeMillis();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<Void> resp = httpClient.send(req, HttpResponse.BodyHandlers.discarding());
            long ms = System.currentTimeMillis() - start;
            totalRequests.incrementAndGet();
            totalRespMs.addAndGet(ms);
            ltAllCounter.increment();
            ltTimer.record(ms, TimeUnit.MILLISECONDS);
            if (resp.statusCode() < 400) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
                ltErrCounter.increment();
            }
        } catch (Exception e) {
            totalRequests.incrementAndGet();
            errorCount.incrementAndGet();
            ltAllCounter.increment();
            ltErrCounter.increment();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI-Updates
    // ══════════════════════════════════════════════════════════════════════════

    private void updateStatusBadge(boolean running) {
        if (running) {
            testStatusBadge.setText("▶  " + activeTestName + " läuft...");
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

        long   elapsedSec = testStartTime != null
                ? Duration.between(testStartTime, Instant.now()).toSeconds() : 0;
        double rps     = elapsedSec > 0 ? (double) total / elapsedSec : 0;
        double avgMs   = total > 0 ? (double) respSum / total : 0;
        double succPct = total > 0 ? (double) success / total * 100 : 100;

        ltRequestsCard.setValue(String.valueOf(total),              -1);
        ltRpsCard     .setValue(String.format("%.1f", rps),         -1);
        ltAvgCard     .setValue(String.format("%.0f", avgMs),       -1);
        ltErrorCard   .setValue(String.valueOf(errors),
                total > 0 ? (double) errors / total : 0);
        ltSuccessCard .setValue(String.format("%.1f", succPct),
                succPct / 100);
        ltElapsedCard .setValue(String.valueOf(elapsedSec),          -1);
    }

    private void setTestButtonsEnabled(boolean enabled) {
        testButtons.forEach(b -> b.setEnabled(enabled));
    }

    private void resetStats() {
        totalRequests.set(0); successCount.set(0);
        errorCount.set(0);    totalRespMs.set(0);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Attach / Detach
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onAttach(AttachEvent event) {
        UI ui = event.getUI();
        refreshTask = scheduler.scheduleAtFixedRate(
                () -> ui.access(this::updateLiveResults),
                2, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (refreshTask != null) refreshTask.cancel(false);
        stopTest();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MetricCard Komponente
    // ══════════════════════════════════════════════════════════════════════════

    static class MetricCard extends Div {

        private final Span   valueSpan     = new Span("–");
        private final Div    progressBar   = new Div();
        private final Div    progressTrack = new Div();
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
