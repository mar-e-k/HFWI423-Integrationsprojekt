package de.fhdw.vendix.orchestrator.core.domain.performance;

/**
 * Die 5 k6-Lasttest-Szenarien für das Vendix Kassensystem.
 * Der scenarioKey wird als -e SCENARIO=... an k6 übergeben.
 */
public enum TestType {

    LASTTEST(
            "lasttest",
            "1. Lasttest – Normalbetrieb",
            "Prüft Stabilität bei erwarteter Nutzung: 3 Kassen, 30 Transaktionen/h, " +
                    "18 Artikel/Bon, CASH/CARD variabel, 33 % Rabatt (10/30 %), " +
                    "25 % Pfand, ~1 Storno/min. Erwartung: alle Aktionen < 2000ms.",
            "3 Kassen · 30 Trans/h · 18 Art/Bon · 30 min",
            1800
    ),

    STRESSTEST(
            "stresstest",
            "2. Stresstest – Belastungsgrenze",
            "Überlastet das System gezielt: Start mit 10 Usern, +10 jede Minute " +
                    "bis 150. Pro User 1 Bon/3s mit 3 Artikeln (1 Artikel/s). " +
                    "Ziel: Kipppunkt der Response Time finden.",
            "10 → 150 User · 18 min · kein Pacing",
            1080
    ),

    SPIKE_TEST(
            "spiketest",
            "3. Spike-Test – Lastspitze",
            "Baseline 3 Kassen/6 Trans/min, Spike auf ~20 Kassen/40 Trans/min " +
                    "(Vervierfachung in 2 min). 20 min Hochlast, dann 5 min Recovery. " +
                    "70-80 % Rabattquote aus 46 verschiedenen Rabattstufen.",
            "6 → 40 Trans/min · ~35 min inkl. Recovery",
            2100
    ),

    SOAK_TEST(
            "soaktest",
            "4. Soak-Test – Langzeitstabilität",
            "Simuliert einen 16-Stunden-Tagesverlauf (06–22 Uhr): " +
                    "1→3→5→3→4 Kassen je nach Tageszeit. Ziel: ~2000 Bons. " +
                    "Deckt Memory Leaks, Connection Leaks und Degradation auf.",
            "1–5 Kassen · Tagesverlauf · 16 h",
            57600
    ),

    CAPACITY_TEST(
            "capacitytest",
            "5. Capacity-Test – Kapazitätsgrenze",
            "Multi-dimensionaler Stufentest: VUs wachsen von 5→30 (+5 alle 2 min), " +
                    "Artikel/Bon wachsen parallel von 50→300. Ermittelt den exakten " +
                    "Kipppunkt bei steigender Last und Warenkorb-Größe.",
            "5 → 30 Kassen · 50 → 300 Artikel · ~14 min",
            840
    );

    /** k6 ENV-Variable: wird als -e SCENARIO=... übergeben */
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