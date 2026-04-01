package de.fhdw.vendix.store.ui;

import com.vaadin.flow.component.icon.SvgIcon;

public enum VendixIcon {
    GRAFANA("grafana-icon.svg"),
    NEON_DB("neondb-icon.svg"),
    RABBIT_MQ("rabbitmq-icon.svg"),
    CLOUD_AMQP("cloudamqp-icon.svg"),
    OPEN_API("openapi-icon.svg"),
    SWAGGER("swagger-icon.svg");

    private final String iconName;

    VendixIcon(String iconName) {
        this.iconName = iconName;
    }

    public SvgIcon create() {
        return new SvgIcon("icons/" + iconName);
    }
}