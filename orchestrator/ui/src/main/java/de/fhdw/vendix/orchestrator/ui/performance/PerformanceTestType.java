package de.fhdw.vendix.orchestrator.ui.performance;

public enum PerformanceTestType {
    LOAD(
            "Lasttest",
            "Prueft die erwartete Nutzung mit drei aktiven Kassen und realistischen Bons.",
            "lasttest"
    ),
    STRESS(
            "Stresstest",
            "Erhoeht die Zahl gleichzeitiger Nutzer schrittweise bis in den Ueberlastbereich.",
            "stresstest"
    ),
    SPIKE(
            "Spike-Test",
            "Faengt ploetzliche Lastspitzen mit stark steigender Bon- und Rabattdichte ab.",
            "spiketest"
    ),
    SOAK(
            "Soak-Test",
            "Haelt die Last ueber lange Zeit stabil, um Langzeiteffekte sichtbar zu machen.",
            "soaktest"
    ),
    CAPACITY(
            "Capacity-Test",
            "Erhoeht Kassen, Bons, Artikel und offene Warenkoerbe bis zur Kapazitaetsgrenze.",
            "capacitytest"
    );

    private final String label;
    private final String description;
    private final String scenarioKey;

    PerformanceTestType(String label, String description, String scenarioKey) {
        this.label = label;
        this.description = description;
        this.scenarioKey = scenarioKey;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public String scenarioKey() {
        return scenarioKey;
    }
}
