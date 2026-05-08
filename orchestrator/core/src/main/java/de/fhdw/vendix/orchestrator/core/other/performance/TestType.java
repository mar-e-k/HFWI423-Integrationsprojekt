package de.fhdw.vendix.orchestrator.core.other.performance;

/**
 * Alle k6-Lasttest-Szenarien für das Vendix Kassensystem.
 *
 * <p>Der {@code scenarioKey} wird als {@code -e SCENARIO=...} an k6 übergeben.
 * Das {@code script} steuert, welche k6-Skript-Datei ausgeführt wird:
 * <ul>
 *   <li>{@code test.js} – die fünf klassischen HTTP/POS-Lasttests</li>
 *   <li>{@code messaging-e2e-test.js} – der AMQP-E2E-Kreislauf-Test</li>
 * </ul>
 */
public enum TestType {

    // ── Klassische HTTP-/POS-Lasttests ──────────────────────────────────────

    LASTTEST(
            "test.js",
            "lasttest",
            "1. Lasttest – Normalbetrieb",
            "Prüft Stabilität bei erwarteter Nutzung: 6 VUs, 180 Trans/h, " +
                    "20 Artikel/Bon mit GTIN-Scan, CASH/CARD/ONLINE variabel, " +
                    "33 % Rabatt (10/30 %), 25 % Pfand, 10 % Voucher, ~1 % Storno. " +
                    "Jeder Bon wird gedruckt (OPEN → PRINTED). Erwartung: P95 < 2000ms.",
            "6 VUs · 180 Trans/h · 20 Art/Bon · 30 min",
            1800
    ),

    STRESSTEST(
            "test.js",
            "stresstest",
            "2. Stresstest – Belastungsgrenze",
            "Überlastet das System gezielt: Start mit 10 VUs, +20 alle 60s bis 200. " +
                    "Kein Sleep — maximaler Dauerfeuer. GTIN-Scan + Checkout + Print. " +
                    "Ziel: HikariCP-Pool-Erschöpfung und Tomcat-Sättigung sichtbar machen.",
            "10 → 200 VUs · 12 min Ramp + 3 min Halten · kein Sleep",
            1080
    ),

    SPIKE_TEST(
            "test.js",
            "spiketest",
            "3. Spike-Test – Lastspitze",
            "Baseline 6 Trans/min, Spike auf 80 Trans/min in 1 Minute. " +
                    "20 min Hochlast, dann 5 min Recovery. " +
                    "75 % Rabattquote aus 46 Stufen (5–50 %), 15 % Voucher.",
            "6 → 80 Trans/min · ~31 min inkl. Recovery",
            1860
    ),

    SOAK_TEST(
            "test.js",
            "soaktest",
            "4. Soak-Test – Memory-Drift",
            "Aggressiver 16-Stunden-Dauerlasttest: 12–30 VUs, kein Sleep. " +
                    "GTIN-Scans + Checkout + Print + Voucher. " +
                    "Ziel: Heap-Wachstum, GC-Drift und Connection-Leaks aufdecken.",
            "12–30 VUs · kein Sleep · 16 h",
            57600
    ),

    CAPACITY_TEST(
            "test.js",
            "capacitytest",
            "5. Capacity-Test – Kipppunkt",
            "Multi-dimensionaler Stufentest: VUs wachsen 5→50 (+10 alle 2 min), " +
                    "Artikel/Bon wachsen parallel 100→600. Kein Sleep. " +
                    "Ziel: exakten Kipppunkt bei steigender Last und Payload-Größe finden.",
            "5 → 50 VUs · 100 → 600 Artikel/Bon · ~14 min",
            840
    ),

    // ── AMQP-Messaging-E2E-Test ─────────────────────────────────────────────

    MESSAGING_E2E(
            "messaging-e2e-test.js",
            "messaging-e2e",
            "6. Messaging E2E – AMQP-Kreislauf",
            "k6 triggert über POST /api/test/order[/urgent] Bestellanforderungen im Store. " +
                    "Der Store publiziert ArticleOrderEvents an RabbitMQ. " +
                    "Die Logistikseite (JMeter) konsumiert die Orders und schickt ArticleSentEvents zurück. " +
                    "Der Store verarbeitet die Events und bucht den Bestand hoch. " +
                    "k6 verifiziert anschließend per GET /api/test/store-stock, dass der Bestand gestiegen ist. " +
                    "Voraussetzung: JMeter auf der Logistikseite läuft und ist mit RabbitMQ verbunden.",
            "30 Orders/min · 5 min · 30 % urgent · Bestand-Verifikation aktiv",
            300
    );

    private final String script;
    private final String scenarioKey;
    private final String displayName;
    private final String description;
    private final String parameters;
    private final int    durationSeconds;

    TestType(String script, String scenarioKey, String displayName,
             String description, String parameters, int durationSeconds) {
        this.script          = script;
        this.scenarioKey     = scenarioKey;
        this.displayName     = displayName;
        this.description     = description;
        this.parameters      = parameters;
        this.durationSeconds = durationSeconds;
    }

    /** Dateiname des k6-Skripts (relativ zu {@code /etc/k6/scripts/}). */
    public String getScript()          { return script; }
    public String getScenarioKey()     { return scenarioKey; }
    public String getDisplayName()     { return displayName; }
    public String getDescription()     { return description; }
    public String getParameters()      { return parameters; }
    public int    getDurationSeconds() { return durationSeconds; }
}
