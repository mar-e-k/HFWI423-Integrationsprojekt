package de.fhdw.vendix.commons.spring.vaadin.layout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import de.fhdw.vendix.commons.spring.vaadin.utility.ErrorPayload;

import java.time.Duration;

public final class GlobalUiExceptionHandler implements ErrorHandler {

    private final Duration duration;

    // TODO: take this from properties
    public GlobalUiExceptionHandler(Duration duration) {
        this.duration = duration;
    }

    @Override
    public void error(ErrorEvent errorEvent) {
        Throwable throwable = DefaultErrorHandler.findRelevantThrowable(errorEvent.getThrowable());

        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }

        ui.access(() -> ErrorOverlay.show(ErrorPayload.fromThrowable(throwable), duration));
    }
}