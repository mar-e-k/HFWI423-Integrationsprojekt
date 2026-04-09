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
import com.vaadin.flow.component.details.Details;
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

import java.io.IOException;
import java.io.RandomAccessFile;
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
    private volatile Process  jmeterProcess;
    private volatile Path     resultFile;
    private volatile long     csvReadOffset = 0;

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

    // ── Lasttest-UI ──────────────────────────────────────────────────────────
    private Button stopButton;
    private final List<Button> testButtons = new ArrayList<>();

    private final MetricCard ltRequestsCard = new MetricCard("Requests",      "total", "#6366f1");
    private final MetricCard ltRpsCard      = new MetricCard("Throughput",    "req/s", "#10b981");
    private final MetricCard ltAvgCard      = new MetricCard("Ø Antwortzeit", "ms",    "#3b82f6");
    private final MetricCard ltErrorCard    = new MetricCard("Fehler",        "total", "#ef4444");
    private final MetricCard ltSuccessCard  = new MetricCard("Erfolgsrate",   "%",     "#10b981");
    private final MetricCard ltElapsedCard  = new MetricCard("Laufzeit",      "s",     "#8b5cf6");
    private Span testStatusBadge;

    // ─────────────────────────────────────────────────────────────────────────

    public MonitoringView(MeterRegistry meterRegistry,
                          @Value("${server.port:8081}") int serverPort,
                          @Value("${jmeter.home:}") String jmeterHome) {
        this.serverPort  = serverPort;
        this.jmeterHome  = jmeterHome;

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
        tabs.add("Lasttests",              buildLoadTestTab());
        tabs.add("Grafana Dashboard",      buildGrafanaTab());
        tabs.add("JMeter Ergebnisse",      buildJMeterGrafanaTab());

        Div card = new Div(tabs);
        card.addClassName("content-card");
        card.getStyle().set("padding", "24px");
        card.setSizeFull();
        add(card);
    }

    // ── Tab 1: Lasttest-Steuerung ─────────────────────────────────────────────

    private Div buildLoadTestTab() {

        // ── Intro ─────────────────────────────────────────────────────────────
        Div intro = new Div();
        intro.getStyle()
                .set("background", "#f8faff").set("border", "1px solid #e2e8f0")
                .set("border-radius", "12px").set("padding", "16px 20px")
                .set("margin-bottom", "24px");

        Span introTitle = new Span("Lasttests – Übersicht");
        introTitle.getStyle().set("font-size", "1rem").set("font-weight", "700")
                .set("color", "#1e293b").set("display", "block").set("margin-bottom", "8px");

        Span introText = new Span(
                "Die Tests werden über Apache JMeter als Subprozess gestartet und senden HTTP-Anfragen " +
                "gegen die laufende Logistik-Instanz. Messdaten (Latenz, Fehlerrate, Durchsatz) werden " +
                "via InfluxDB-Backend-Listener erfasst und in Grafana visualisiert (Tab \"JMeter Ergebnisse\"). " +
                "Es kann jeweils nur ein Test aktiv sein.");
        introText.getStyle().set("font-size", "0.85rem").set("color", "#475569")
                .set("line-height", "1.6").set("display", "block");

        intro.add(introTitle, introText);

        // ── Test-Karten ───────────────────────────────────────────────────────
        Details soakAcc     = buildTestAccordion(
                "Soak Test", "#6366f1",
                "Dauerlasttest – Langzeitstabilität",
                "Speicherlecks, Ressourcenerschöpfung und Degradierung unter konstanter Last über " +
                "einen längeren Zeitraum identifizieren.",
                "10 virtuelle Nutzer senden über 120 s kontinuierlich Anfragen bei moderater Last.",
                new String[]{"Benutzer", "10"}, new String[]{"Anlaufzeit", "10 s"}, new String[]{"Dauer", "120 s"},
                e -> launchJMeter("Soak Test", 10, 10, 120));

        Details spikeAcc    = buildTestAccordion(
                "Spike Test", "#ef4444",
                "Spitzenlasttest – Reaktion auf Lastspitzen",
                "Verhalten des Systems bei abruptem, massivem Lastanstieg analysieren " +
                "(Recovery-Zeit, Fehlerrate unter Überlast).",
                "100 virtuelle Nutzer starten innerhalb von 2 s – maximale Überlast für 40 s.",
                new String[]{"Benutzer", "100"}, new String[]{"Anlaufzeit", "2 s"}, new String[]{"Dauer", "40 s"},
                e -> launchJMeter("Spike Test", 100, 2, 40));

        Details capacityAcc = buildTestAccordion(
                "Capacity Test", "#f59e0b",
                "Kapazitätstest – Leistungsgrenze ermitteln",
                "Maximale Nutzerzahl bestimmen, bei der definierte Performance-Schwellenwerte " +
                "(Latenz, Fehlerrate) noch eingehalten werden.",
                "50 Nutzer gleichmäßig über 45 s hochgefahren, Dauer 90 s.",
                new String[]{"Benutzer", "50"}, new String[]{"Anlaufzeit", "45 s"}, new String[]{"Dauer", "90 s"},
                e -> launchJMeter("Capacity Test", 50, 45, 90));

        Details stressAcc   = buildTestAccordion(
                "Stress Test", "#dc2626",
                "Stresstest – Systemgrenzen austesten",
                "Belastungsgrenze des Systems bewusst überschreiten und Verhalten " +
                "unter Überlast dokumentieren (Fehlerrate, Timeouts, Absturzverhalten).",
                "150 Nutzer in 5 s – deutlich jenseits des Normalbetriebs für 60 s.",
                new String[]{"Benutzer", "150"}, new String[]{"Anlaufzeit", "5 s"}, new String[]{"Dauer", "60 s"},
                e -> launchJMeter("Stress Test", 150, 5, 60));

        VerticalLayout accordions = new VerticalLayout(soakAcc, spikeAcc, capacityAcc, stressAcc);
        accordions.setWidthFull();
        accordions.setPadding(false);
        accordions.getStyle().set("gap", "8px");

        // ── Stoppen-Button ────────────────────────────────────────────────────
        stopButton = new Button("■  Laufenden Test stoppen");
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopButton.setEnabled(false);
        stopButton.getStyle().set("font-weight", "700");
        stopButton.addClickListener(e -> stopTest());

        testStatusBadge = new Span("Kein Test aktiv");

        VerticalLayout tab = new VerticalLayout(intro, accordions, stopButton);
        tab.setPadding(false);
        tab.setSpacing(false);
        tab.getStyle().set("gap", "0");
        tab.setWidthFull();

        return new Div(tab);
    }

    private Details buildTestAccordion(String title, String color, String subtitle,
                                       String whenToUse, String whatHappens,
                                       String[] param1, String[] param2, String[] param3,
                                       com.vaadin.flow.component.ComponentEventListener<
                                               com.vaadin.flow.component.ClickEvent<Button>> clickListener) {

        // ── Summary (immer sichtbar) ──────────────────────────────────────────
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "700").set("color", color).set("font-size", "0.95rem");

        Span subtitleSpan = new Span(" – " + subtitle);
        subtitleSpan.getStyle().set("color", "#64748b").set("font-size", "0.82rem");

        HorizontalLayout summaryParams = new HorizontalLayout(
                paramBadge(param1[0], param1[1], color),
                paramBadge(param2[0], param2[1], color),
                paramBadge(param3[0], param3[1], color));
        summaryParams.setPadding(false);
        summaryParams.getStyle().set("gap", "6px").set("flex-wrap", "wrap");

        HorizontalLayout summaryRow = new HorizontalLayout(
                new Div(titleSpan, subtitleSpan), summaryParams);
        summaryRow.setWidthFull();
        summaryRow.setAlignItems(FlexComponent.Alignment.CENTER);
        summaryRow.getStyle().set("justify-content", "space-between").set("flex-wrap", "wrap");
        summaryRow.setPadding(false);

        // ── Content (aufgeklappt) ─────────────────────────────────────────────
        Span zielLabel = new Span("Ziel");
        zielLabel.getStyle()
                .set("font-size", "0.72rem").set("font-weight", "700").set("color", "#94a3b8")
                .set("text-transform", "uppercase").set("letter-spacing", "0.06em")
                .set("display", "block").set("margin-bottom", "3px");
        Span zielText = new Span(whenToUse);
        zielText.getStyle()
                .set("font-size", "0.82rem").set("color", "#475569")
                .set("line-height", "1.5").set("display", "block").set("margin-bottom", "12px");

        Span konfLabel = new Span("Konfiguration");
        konfLabel.getStyle()
                .set("font-size", "0.72rem").set("font-weight", "700").set("color", "#94a3b8")
                .set("text-transform", "uppercase").set("letter-spacing", "0.06em")
                .set("display", "block").set("margin-bottom", "3px");
        Span konfText = new Span(whatHappens);
        konfText.getStyle()
                .set("font-size", "0.82rem").set("color", "#475569")
                .set("line-height", "1.5").set("display", "block").set("margin-bottom", "14px");

        Button startBtn = new Button("▶  " + title + " starten");
        startBtn.getStyle()
                .set("background", color).set("color", "white")
                .set("border-radius", "8px").set("font-weight", "700")
                .set("box-shadow", "0 2px 8px " + color + "55");
        startBtn.addClickListener(clickListener);
        testButtons.add(startBtn);

        Div content = new Div(zielLabel, zielText, konfLabel, konfText, startBtn);
        content.getStyle().set("padding", "4px 0 4px 0");

        Details details = new Details(summaryRow, content);
        details.setWidthFull();
        details.getStyle()
                .set("border", "1px solid #e2e8f0").set("border-radius", "10px")
                .set("padding", "12px 16px")
                .set("background", "white");

        return details;
    }

    private Div paramBadge(String label, String value, String color) {
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("color", "#64748b").set("font-size", "0.75rem");
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "700").set("color", color).set("font-size", "0.75rem");

        Div badge = new Div(labelSpan, valueSpan);
        badge.getStyle()
                .set("background", "#f8faff").set("border", "1px solid #e2e8f0")
                .set("border-radius", "6px").set("padding", "3px 10px")
                .set("display", "inline-flex").set("align-items", "center");
        return badge;
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
                .set("border-radius", "12px").set("border", "1px solid #e8edf5");

        Div wrapper = new Div(hint, frame);
        wrapper.setWidthFull();
        return wrapper;
    }

    private Div buildJMeterGrafanaTab() {
        Span hint = new Span(
                "Zeigt JMeter-Testergebnisse aus InfluxDB. " +
                "Starte einen Test im Tab \"Lasttests\", dann hier application & transaction auswählen.");
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

    /**
     * Startet JMeter als Subprocess mit den übergebenen Parametern.
     * Ergebnisse werden in eine temporäre CSV geschrieben, die alle 2 s
     * gepolt und in die Live-Karten + Micrometer-Metriken übernommen wird.
     */
    private void launchJMeter(String name, int threads, int rampup, int duration) {
        if (testRunning.getAndSet(true)) return;

        // ApacheJMeter.jar ermitteln (funktioniert auf allen Plattformen ohne bat-Probleme)
        String jmeterJar = jmeterHome + "/bin/ApacheJMeter.jar";

        if (jmeterHome.isBlank() || !Path.of(jmeterJar).toFile().exists()) {
            showError("JMeter nicht gefunden. Bitte jmeter.home in application.properties setzen.\n" +
                      "Erwartet: \"" + jmeterJar + "\"");
            testRunning.set(false);
            return;
        }

        // JMX-Pfad
        Path jmxFile = Path.of("monitoring/jmeter/logistik-parametrisiert.jmx").toAbsolutePath();
        if (!jmxFile.toFile().exists()) {
            showError("JMX-Datei nicht gefunden: " + jmxFile);
            testRunning.set(false);
            return;
        }

        // Ergebnis-CSV vorbereiten
        try {
            Path resultsDir = Path.of("monitoring/jmeter/results");
            Files.createDirectories(resultsDir);
            resultFile    = resultsDir.resolve("lasttest-result.csv").toAbsolutePath();
            Files.deleteIfExists(resultFile);   // alten Lauf löschen
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

        // JMeter-Kommando: direkt via java -jar (funktioniert auf Windows ohne bat-Probleme)
        List<String> cmd = new ArrayList<>(List.of(
                "java", "-jar", jmeterJar,
                "-n",                          // Non-GUI Modus
                "-t", jmxFile.toString(),      // Test-Plan
                "-l", resultFile.toString(),   // Ergebnis-CSV
                "-Jthreads="   + threads,
                "-Jrampup="    + rampup,
                "-Jduration="  + duration,
                "-Jhost=localhost",
                "-Jport="      + serverPort,
                "-Jtestname="  + name.replace(" ", "-")
        ));

        // Start-Notification mit Kommando
        String cmdPreview = String.join(" ", cmd);
        Notification.show("JMeter wird gestartet…  " + cmdPreview, 4000,
                Notification.Position.BOTTOM_END);

        // JMeter im Hintergrund starten
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            StringBuilder jmeterOutput = new StringBuilder();
            try {
                jmeterProcess = new ProcessBuilder(cmd)
                        .redirectErrorStream(true)
                        .start();

                // Output lesen (verhindert Prozess-Deadlock + ermöglicht Fehlerdiagnose)
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
        if (jmeterProcess != null) jmeterProcess.destroyForcibly();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CSV-Polling  –  liest neue Zeilen aus der JMeter-Ergebnis-CSV
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * JMeter-CSV Format (mit fieldNames=true Header):
     * timeStamp,elapsed,label,responseCode,responseMessage,threadName,
     * dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,...
     *
     * Relevante Spalten (0-basiert):
     *   1 = elapsed (ms)
     *   7 = success (true/false)
     */
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
                } catch (NumberFormatException ignored) { /* Header oder leere Zeile */ }
            }
            csvReadOffset = raf.getFilePointer();
        } catch (IOException ignored) { /* Datei noch nicht vorhanden oder gesperrt */ }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI-Updates
    // ══════════════════════════════════════════════════════════════════════════

    private void updateStatusBadge(boolean running) {
        if (running) {
            testStatusBadge.setText("▶  " + activeTestName + " läuft (JMeter)...");
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

        ltRequestsCard.setValue(String.valueOf(total),             -1);
        ltRpsCard     .setValue(String.format("%.1f", rps),        -1);
        ltAvgCard     .setValue(String.format("%.0f", avgMs),      -1);
        ltErrorCard   .setValue(String.valueOf(errors),
                total > 0 ? (double) errors / total : 0);
        ltSuccessCard .setValue(String.format("%.1f", succPct),
                succPct / 100);
        ltElapsedCard .setValue(String.valueOf(elapsed),            -1);
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
            pollCsvResults();      // neue JMeter-CSV-Zeilen einlesen
            updateLiveResults();   // UI-Karten aktualisieren
        }), 2, 2, TimeUnit.SECONDS);
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
