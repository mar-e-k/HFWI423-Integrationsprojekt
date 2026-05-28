package de.fhdw.vendix.commons.spring.app.bundle.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GlobalLabelFilter extends TurboFilter {

    private final Map<String, String> globalLabels = new ConcurrentHashMap<>();

    public void setLabel(String key, String value) {
        if (value != null) {
            globalLabels.put(key, value);
        }
    }

    @Override
    public FilterReply decide(
            Marker marker,
            Logger logger,
            Level level,
            String format,
            Object[] params,
            Throwable t
    ) {
        globalLabels.forEach(MDC::put);
        return FilterReply.NEUTRAL;
    }
}