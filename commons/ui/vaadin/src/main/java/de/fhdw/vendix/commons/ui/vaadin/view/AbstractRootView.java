package de.fhdw.vendix.commons.ui.vaadin.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRootView extends Div implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(AbstractRootView.class);

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        log.atInfo().log("Reroute to login");
        beforeEnterEvent.rerouteTo("login");
    }
}