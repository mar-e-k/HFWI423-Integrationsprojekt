package de.fhdw.vendix.commons.ui.vaadin.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;

public final class LiveClockComponent extends Span {

    public LiveClockComponent() {
        setId("live-clock-label");
        getStyle().set("font-size", "var(--lumo-font-size-l)");
        getStyle().set("font-weight", "bold");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI.getCurrent().getPage().executeJs("import('./frontend/clock.js').then(module => module.initClock($0));", getElement());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) { // Might want to stop clock interval on detach to avoid memory leak
        super.onDetach(detachEvent);
    }
}