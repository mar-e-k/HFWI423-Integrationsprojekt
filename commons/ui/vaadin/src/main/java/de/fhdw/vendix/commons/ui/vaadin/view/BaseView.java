package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public abstract class BaseView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!isAccessGranted()) {
            handleUnauthorized(event);
        }
    }
    protected abstract boolean isAccessGranted();

    protected void handleUnauthorized(BeforeEnterEvent event) {
        event.rerouteTo("/login");
    }
}