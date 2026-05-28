package de.fhdw.vendix.commons.spring.app.bundle.logging;

public enum ObservabilityLabel {
    APP("app"),
    SERVICE_NAME("service_name"),
    SERVICE_ID("service_id"),
    HOST("host"),
    INSTANCE("instance");

    private final String label;
    ObservabilityLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}