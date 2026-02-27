package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import de.fhdw.vendix.security.api.ui.ClassAccessChecker;

public abstract class AbstractView extends BaseView {

    private final ClassAccessChecker accessChecker;

    protected AbstractView(ClassAccessChecker accessChecker) {
        this.accessChecker = accessChecker;
    }

    @Override
    protected boolean isAccessGranted() {
        return accessChecker.hasAccess(this.getClass());
    }

    @Override
    protected void handleUnauthorized(BeforeEnterEvent event) {
        event.rerouteTo("/logout");
    }
}