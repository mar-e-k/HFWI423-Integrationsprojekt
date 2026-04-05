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

    /** Lesbarer Label für die UI */
    public String getLabel() {
        return switch (this) {
            case LASTTEST      -> "Lasttest (10 User, 3 min)";
            case STRESSTEST    -> "Stresstest (10 → 500 User)";
            case SPIKE_TEST    -> "Spike-Test (5 → 300 User)";
            case SOAK_TEST     -> "Soak-Test (20 User, 60 min)";
            case CAPACITY_TEST -> "Capacity-Test (stufenweise bis 200 User)";
        };
    }
}
