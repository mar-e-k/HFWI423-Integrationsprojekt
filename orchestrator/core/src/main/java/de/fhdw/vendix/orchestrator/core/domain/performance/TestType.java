package de.fhdw.vendix.orchestrator.core.domain.performance;

/**
 * Die 5 k6-Lasttest-Szenarien für das Vendix Kassensystem.
 * Der scenarioKey wird als -e SCENARIO=... an k6 übergeben.
 */
public enum TestType {

    LASTTEST(
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
            "stresstest",
            "2. Stresstest – Belastungsgrenze",
            "Überlastet das System gezielt: Start mit 10 VUs, +20 alle 60s bis 200. " +
                    "Kein Sleep — maximaler Dauerfeuer. GTIN-Scan + Checkout + Print. " +
                    "Ziel: HikariCP-Pool-Erschöpfung und Tomcat-Sättigung sichtbar machen.",
            "10 → 200 VUs · 12 min Ramp + 3 min Halten · kein Sleep",
            1080
    ),

    SPIKE_TEST(
            "spiketest",
            "3. Spike-Test – Lastspitze",
            "Baseline 6 Trans/min, Spike auf 80 Trans/min in 1 Minute. " +
                    "20 min Hochlast, dann 5 min Recovery. " +
                    "75 % Rabattquote aus 46 Stufen (5–50 %), 15 % Voucher.",
            "6 → 80 Trans/min · ~31 min inkl. Recovery",
            1860
    ),

    SOAK_TEST(
            "soaktest",
            "4. Soak-Test – Memory-Drift",
            "Aggressiver 16-Stunden-Dauerlasttest: 12–30 VUs, kein Sleep. " +
                    "GTIN-Scans + Checkout + Print + Voucher. " +
                    "Ziel: Heap-Wachstum, GC-Drift und Connection-Leaks aufdecken.",
            "12–30 VUs · kein Sleep · 16 h",
            57600
    ),

    CAPACITY_TEST(
            "capacitytest",
            "5. Capacity-Test – Kipppunkt",
            "Multi-dimensionaler Stufentest: VUs wachsen 5→50 (+10 alle 2 min), " +
                    "Artikel/Bon wachsen parallel 100→600. Kein Sleep. " +
                    "Ziel: exakten Kipppunkt bei steigender Last und Payload-Größe finden.",
            "5 → 50 VUs · 100 → 600 Artikel/Bon · ~14 min",
            840
    );

    private final String scenarioKey;
    private final String displayName;
    private final String description;
    private final String parameters;
    private final int    durationSeconds;

    TestType(String scenarioKey, String displayName, String description,
             String parameters, int durationSeconds) {
        this.scenarioKey     = scenarioKey;
        this.displayName     = displayName;
        this.description     = description;
        this.parameters      = parameters;
        this.durationSeconds = durationSeconds;
    }

    public String getScenarioKey()     { return scenarioKey; }
    public String getDisplayName()     { return displayName; }
    public String getDescription()     { return description; }
    public String getParameters()      { return parameters; }
    public int    getDurationSeconds() { return durationSeconds; }
}