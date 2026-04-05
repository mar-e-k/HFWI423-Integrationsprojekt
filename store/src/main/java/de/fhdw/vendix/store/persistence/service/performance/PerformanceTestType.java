package de.fhdw.vendix.store.persistence.service.performance;

public enum PerformanceTestType {

    LASTTEST("1. Lasttest"),
    STRESSTEST("2. Stresstest"),
    SPIKE_TEST("3. Spike-Test"),
    SOAK_TEST("4. Soak-Test"),
    CAPACITY_TEST("5. Capacity-Test");

    /** Prefix des testname-Attributs im JMX (Substring-Match reicht) */
    private final String jmxPrefix;

    PerformanceTestType(String jmxPrefix) {
        this.jmxPrefix = jmxPrefix;
    }

    public String getJmxPrefix() {
        return jmxPrefix;
    }

    /** Lesbarer Label für die UI (erscheint auf den Vaadin-Buttons) */
    public String getLabel() {
        return switch (this) {
            case LASTTEST      -> "Lasttest (30 User, 5 min)";
            case STRESSTEST    -> "Stresstest (10 → 150 User)";
            case SPIKE_TEST    -> "Spike-Test (5 → 100 User)";
            case SOAK_TEST     -> "Soak-Test (30 User, 30 min)";
            case CAPACITY_TEST -> "Capacity-Test (stufenweise bis 100 User)";
        };
    }
}
